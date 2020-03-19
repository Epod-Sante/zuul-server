package ca.uqtr.zuulserver.filter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import io.micrometer.core.instrument.util.IOUtils;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

/*
Type: Zuul filter.
Functionality: Remove the refresh_token from the response and save in a cookies
               Delete the cookies when there is a request to logout.
*/
@Component
public class RefreshTokenAsCookiePostZuulFilter extends ZuulFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Object run() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        logger.info("in zuul filter RefreshTokenAsCookiePostZuulFilter" + ctx.getRequest().getRequestURI());

        final String requestURI = ctx.getRequest().getRequestURI();
        final String requestMethod = ctx.getRequest().getMethod();;
        final String headerMethod = ctx.getRequest().getHeader("Authorization");
        final String params = ctx.getRequest().getParameter("username");

        try {

            final InputStream is = ctx.getResponseDataStream();
            String responseBody = IOUtils.toString(is, StandardCharsets.UTF_8);
            if (responseBody.contains("refresh_token")) {
                final Map<String, Object> responseMap = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
                });

                String username = getUsernameFromJWT((String) responseMap.get("access_token"));
                final String refreshToken = responseMap.get("refresh_token").toString();
                responseMap.remove("refresh_token");
                responseBody = mapper.writeValueAsString(responseMap);
                System.out.println("+++++++++ username =  "+username);
                System.out.println("+++++++++ refreshToken =  "+refreshToken);

                final Cookie cookie = new Cookie(username, refreshToken);
                cookie.setHttpOnly(true);
                // cookie.setSecure(true);
                System.out.println("+++++++++ setPath =  "+ctx.getRequest().getContextPath());
                cookie.setPath(ctx.getRequest().getContextPath());
                cookie.setMaxAge(2592000); // 30 days

                ctx.getResponse().addCookie(cookie);
                System.out.println("+++++++++++cookie  -"+cookie.getName()+"-");
                System.out.println("+++++++++++cookie  "+cookie.getValue());
                System.out.println("+++++++++++extractRefreshToken  "+extractRefreshToken(ctx.getRequest(), username));
                System.out.println("+++++++++++readCookie  "+org.springframework.web.util.WebUtils.getCookie(ctx.getRequest(), username));

            }
            if (requestURI.contains("logingout") && requestMethod.equals("DELETE")) {
                String username = getUsernameFromJWT(headerMethod);
                final Cookie cookie = new Cookie(username, "");
                cookie.setMaxAge(0);
                cookie.setPath(ctx.getRequest().getServletPath());
                ctx.getResponse().addCookie(cookie);
            }
            ctx.setResponseBody(responseBody);

        } catch (final IOException e) {
            logger.error("Error occurred in zuul post filter", e);
        }
        return null;
    }


    private String extractRefreshToken(HttpServletRequest req, String username) {
        Cookie[] cookies = req.getCookies();
        System.out.println(cookies);
        if (cookies != null) {
            System.out.println("-----------------------  00 " + username);
            for (int i = 0; i < cookies.length; i++) {
                System.out.println("-----------------------  11 " + i);
                if (cookies[i].getName().equalsIgnoreCase(username)) {
                    System.out.println("..........." + cookies[i].getValue());
                    return cookies[i].getValue();
                }
            }
        }
        return null;
    }
    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public String filterType() {
        return "post";
    }


    public String getUsernameFromJWT(String jwtToken){
        String[] split_string = jwtToken.split("\\.");
        String base64EncodedBody = split_string[1];

        Base64 base64Url = new Base64(true);
        String body = new String(base64Url.decode(base64EncodedBody));
        JSONObject jsonObject = new JSONObject(body);

        return jsonObject.getString("user_name");
    }
}

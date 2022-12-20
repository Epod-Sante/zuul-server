package ca.uqtr.zuulserver.filter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import ca.uqtr.zuulserver.entity.Token;
import ca.uqtr.zuulserver.repository.TokenRepository;
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
public class RefreshTokenInRedisPostZuulFilter extends ZuulFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper mapper = new ObjectMapper();
    private TokenRepository tokenRepository;

    public RefreshTokenInRedisPostZuulFilter(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public Object run() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        logger.info("in zuul filter RefreshTokenInRedisPostZuulFilter" + ctx.getRequest().getRequestURI());

        final String requestURI = ctx.getRequest().getRequestURI();
        final String requestMethod = ctx.getRequest().getMethod();;
        final String headerMethod = ctx.getRequest().getHeader("Authorization");
        HttpServletRequest request = ctx.getRequest();

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

                tokenRepository.save(new Token(username, refreshToken));

              /* HttpSession session = request.getSession(true);
                session.setAttribute(username, refreshToken);

                System.out.println("+++++++++++cookie  -"+session.getAttribute(username)+"-");*/

            }
            /*if (requestURI.contains("logingout") && requestMethod.equals("DELETE")) {
                String username = getUsernameFromJWT(headerMethod);
                *//*HttpSession session = request.getSession(false);
                session.removeAttribute(username);*//*
                tokenRepository.deleteById(username);
                //session.invalidate();
            }*/
            ctx.setResponseBody(responseBody);

        } catch (final IOException e) {
            logger.error("Error occurred in zuul post filter");
            e.printStackTrace();
        }
        return null;
    }

    private String extractRefreshToken(HttpServletRequest req, String username) {
        Cookie[] cookies = req.getCookies();
        System.out.println(cookies);
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equalsIgnoreCase(username)) {
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

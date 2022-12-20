package ca.uqtr.zuulserver.filter;

import ca.uqtr.zuulserver.repository.TokenRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RefreshTokenFromRedisPreZuulFilter extends ZuulFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private TokenRepository tokenRepository;

    public RefreshTokenFromRedisPreZuulFilter(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @SneakyThrows
    @Override
    public Object run() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        final String requestURI = ctx.getRequest().getRequestURI();
        HttpServletRequest req = ctx.getRequest();
        HttpSession session = req.getSession(false);
        String username = null;
        String refreshToken = null;
        logger.info("in zuul filter RefreshTokenFromRedisPreZuulFilter" + ctx.getRequest().getRequestURI());

        if (ctx.getRequest().getParameter("grant_type") != null && ctx.getRequest().getParameter("grant_type").equals("refresh_token")) {
            username = ctx.getRequest().getParameter("user_name");
            getRefreshToken(ctx, req, session, username);
            //String refreshToken = extractRefreshToken(req, username);
        }
        /*if (!requestURI.contains("/oauth/token") && !requestURI.contains("/oauth/check_token")){
            System.out.println("-----------------------******************** 2222 ");
            String token = ctx.getRequest().getHeader("Authorization");
            System.out.println("-----------------------******************** token "+token);
            token = token.replace("bearer ", "");
            username = getUsernameFromJWT(token);
            System.out.println("----------------------- user = " + username);
            getRefreshToken(ctx, req, session, username);
        }*/
        return null;
    }

    private void getRefreshToken(RequestContext ctx, HttpServletRequest req, HttpSession session, String username) {
        //String refreshToken = (String) session.getAttribute(username);

        String refreshToken = tokenRepository.findById(username).get().getRefreshToken();
        if (refreshToken != null) {
            System.out.println("----------------------- refreshToken  " + refreshToken);
            Map<String, List<String>> newParameterMap = new HashMap<>();
            Map<String, String[]> parameterMap = req.getParameterMap();
            //getting the current parameter
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();
                newParameterMap.put(key, Arrays.asList(values));
            }

            newParameterMap.put("refresh_token", Arrays.asList(refreshToken));
            ctx.setRequestQueryParams(newParameterMap);
        }
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
        return "pre";
    }

    private String extractRefreshToken(HttpServletRequest req, String username) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equalsIgnoreCase(username)) {
                    System.out.println("..........." + cookies[i].getValue());
                    return cookies[i].getValue();
                }
            }
        }
        return null;
    }


    public String getUsernameFromJWT(String jwtToken) {
        String[] split_string = jwtToken.split("\\.");
        String base64EncodedBody = split_string[1];

        Base64 base64Url = new Base64(true);
        String body = new String(base64Url.decode(base64EncodedBody));
        JSONObject jsonObject = new JSONObject(body);

        return jsonObject.getString("user_name");
    }
}

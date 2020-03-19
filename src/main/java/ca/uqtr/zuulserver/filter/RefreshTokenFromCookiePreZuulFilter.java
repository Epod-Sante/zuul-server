package ca.uqtr.zuulserver.filter;

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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class RefreshTokenFromCookiePreZuulFilter extends ZuulFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public Object run() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        logger.info("in zuul filter RefreshTokenFromCookiePreZuulFilter" + ctx.getRequest().getRequestURI());
        if (!ctx.getRequest().getRequestURI().contains("check_token")) {
            String token = ctx.getRequest().getHeader("Authorization");

            System.out.println("----------------------- access=  " + token);
            if (token != null) {
                token = token.replace("bearer ", "");
                String username = getUsernameFromJWT(token);
                System.out.println("-----------------------   " + username);

                HttpServletRequest req = ctx.getRequest();
                String refreshToken = extractRefreshToken(req, username);

                if (refreshToken != null) {

                    Map<String, String[]> param = new HashMap<>();
                    param.put("refresh_token", new String[]{refreshToken});
                    //param.put("grant_type", new String[] { "refresh_token" });
                    ctx.setRequest(new CustomHttpServletRequest(req, param));
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

package ca.uqtr.zuulserver.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
Type: Zuul filter.
Functionality: Add the application client id and secret (auth_db.oauth_client_details table) encoded as base64 to the header (authorization) of the request to the oauth/token (login) URL.
*/
@Component
public class ClientSecretPreZuulFilter extends ZuulFilter {

    protected Logger logger = LoggerFactory.getLogger(ClientSecretPreZuulFilter.class);

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        if (ctx.getRequest().getRequestURI().contains("oauth/token")) {
            byte[] encoded;
            try {
                encoded = Base64.encode("SPA:secret".getBytes("UTF-8"));
                ctx.addZuulRequestHeader("Authorization", "Basic " + new String(encoded));
                System.out.println(new String(encoded));

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
                        param.put("grant_type", new String[] { "refresh_token" });
                        ctx.setRequest(new CustomHttpServletRequest(req, param));
                    }
                }


            } catch (UnsupportedEncodingException e) {
                logger.error("Error occurred in pre filter", e);
            }
        }
        return null;
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

        org.apache.commons.codec.binary.Base64 base64Url = new org.apache.commons.codec.binary.Base64(true);
        String body = new String(base64Url.decode(base64EncodedBody));
        JSONObject jsonObject = new JSONObject(body);

        return jsonObject.getString("user_name");
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public int filterOrder() {
        return -2;
    }

    @Override
    public String filterType() {
        return "pre";
    }
}

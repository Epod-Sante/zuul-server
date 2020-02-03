package ca.uqtr.zuulserver.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

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
            } catch (UnsupportedEncodingException e) {
                logger.error("Error occurred in pre filter", e);
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
        return -2;
    }

    @Override
    public String filterType() {
        return "pre";
    }
}

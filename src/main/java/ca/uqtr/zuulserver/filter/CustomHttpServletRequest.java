package ca.uqtr.zuulserver.filter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/*
Type: Implementation of the HttpServletRequest interface.
Functionality: Add the application client id and secret (auth_db.oauth_client_details table) encoded as base64 to the header (authorization) of the request to the access token in oauth/token (login) URL.
*/
public class CustomHttpServletRequest extends HttpServletRequestWrapper {
    private final Map<String, String[]> additionalParams;
    private final HttpServletRequest request;

    public CustomHttpServletRequest(final HttpServletRequest request, final Map<String, String[]> additionalParams) {
        super(request);
        this.request = request;
        this.additionalParams = additionalParams;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        final Map<String, String[]> map = request.getParameterMap();
        final Map<String, String[]> param = new HashMap<String, String[]>();
        param.putAll(map);
        param.putAll(additionalParams);
        System.out.println(param);
        return param;
    }

}

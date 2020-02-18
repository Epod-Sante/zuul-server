package ca.uqtr.zuulserver.config;

import ca.uqtr.zuulserver.security.CustomClaimVerifier;
import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.store.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

@Configuration
@EnableResourceServer
public class GatewayConfiguration extends ResourceServerConfigurerAdapter {

    @Value("${authorization-server.base-url}")
    private String AuthorizationServerBaseURL;

    @Override
    public void configure(final HttpSecurity http) throws Exception {
       http.authorizeRequests()
                .antMatchers("/api/v1/auth-service/oauth/**",
                        "/api/v1/auth-service/oauth/token",
                        "/api/v1/auth-service/registration",
                        "/api/v1/auth-service/registrationConfirm",
                        "/api/v1/auth-service/create/user",
                        "/api/v1/auth-service/update/password/mail",
                        "/api/v1/auth-service/update/password",
                        "/api/v1/auth-service/user/**",
                        "/api/v1/auth-service/login",
                        "/api/v1/auth-service/loggingout",
                        "/api/v1/auth-service/docs",
                        "/api/v1/auth-service/test",
                        "/api/v1/config-service/**",
                        "/api/v1/patient-service/docs",
                        "/api/v1/patient-service/create/professional")
                .permitAll()
                .and()
                .authorizeRequests()
                //.antMatchers("/api/v1/patient-service/create").hasRole("PROFESSIONAL")
                .antMatchers("/**")
                .authenticated();

    }

    @Override
    public void configure(final ResourceServerSecurityConfigurer config) {
        config.tokenServices(tokenService());
    }

    @Primary
    @Bean
    public RemoteTokenServices tokenService() {
        RemoteTokenServices tokenService = new RemoteTokenServices();
        tokenService.setCheckTokenEndpointUrl(
                AuthorizationServerBaseURL+"/api/v1/auth-service/oauth/check_token");
        tokenService.setClientId("SPA");
        tokenService.setClientSecret("secret");
        tokenService.setAccessTokenConverter(accessTokenConverter());
        return tokenService;
    }

    //Asymmetric keys to sign the token(Public: zuul-service.public.txt and Private: auth-service.keystore.jks keys).
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        Resource resource = new ClassPathResource("public.txt");
        String publicKey;
        try {
            publicKey = IOUtils.toString(resource.getInputStream());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        converter.setVerifierKey(publicKey);
        converter.setJwtClaimsSetVerifier(jwtClaimsSetVerifier());
        return converter;
    }

    //Combine multiple claim verifier
    @Bean
    public JwtClaimsSetVerifier jwtClaimsSetVerifier() {
        return new DelegatingJwtClaimsSetVerifier(Arrays.asList(issuerClaimVerifier(), customJwtClaimVerifier()));
    }

    //The JWT token contains a different value for issuer “iss” claim, a simple InvalidTokenException will be thrown.
    //If the token does contain the issuer “iss” claim, no exception will be thrown and the token is considered valid.
    @Bean
    public JwtClaimsSetVerifier issuerClaimVerifier() {
        try {
            return new IssuerClaimVerifier(new URL("http://localhost:4200"));
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    //Check if the user_name claim exists in our JWT token
    @Bean
    public JwtClaimsSetVerifier customJwtClaimVerifier() {
        return new CustomClaimVerifier();
    }


}

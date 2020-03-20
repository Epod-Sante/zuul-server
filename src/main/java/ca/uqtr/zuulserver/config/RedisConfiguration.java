package ca.uqtr.zuulserver.config;


import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class RedisConfiguration {

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() throws URISyntaxException {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        RedisProperties properties = redisProperties();
        URI redisURI = new URI(System.getenv(properties.getUrl()));
        configuration.setHostName(redisURI.getHost());
        configuration.setPort(redisURI.getPort());

        return new JedisConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() throws URISyntaxException {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
    @Bean
    @Primary
    public RedisProperties redisProperties() {
        return new RedisProperties();
    }

}

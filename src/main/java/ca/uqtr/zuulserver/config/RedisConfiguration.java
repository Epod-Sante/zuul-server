package ca.uqtr.zuulserver.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Protocol;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class RedisConfiguration {

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory redis = new JedisConnectionFactory();
        String redisUrl = System.getenv("REDIS_URL");

        URI redisUri = null;
        try {
            redisUri = new URI(redisUrl);
            redis.setHostName(redisUri.getHost());
            redis.setPort(redisUri.getPort());
            redis.setPassword(redisUri.getUserInfo().split(":",2)[1]);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return redis;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

}

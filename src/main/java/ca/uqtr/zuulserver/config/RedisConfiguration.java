package ca.uqtr.zuulserver.config;


import ca.uqtr.zuulserver.entity.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.cloud.aws.cache.config.annotation.CacheClusterConfig;
//import org.springframework.cloud.aws.cache.config.annotation.EnableElastiCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@EnableCaching
//@EnableElastiCache({@CacheClusterConfig(name = "redis")})
public class RedisConfiguration {


//    @Bean
//    public JedisPool getJedisPool() {
//            return new JedisPool("redis://172.26.3.184:6379");
//
//    }

    @Value("${spring.redis.host}")
    private String redisHostName;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    protected JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHostName, redisPort);
        return new JedisConnectionFactory(configuration);


    }

    @Bean(value = "redisTemplate")
    public RedisTemplate<String,Object> redisTemplate() {
        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        return redisTemplate;
    }

    @Bean(name = "cacheManager")
    public CacheManager cacheManager(JedisConnectionFactory jedisConnectionFactory) {
        return RedisCacheManager.builder(jedisConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
                .build();
    }


}

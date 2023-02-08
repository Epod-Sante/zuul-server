package ca.uqtr.zuulserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@EnableEurekaClient
@EnableZuulProxy
@EnableDiscoveryClient
@Controller
@EnableRedisRepositories
public class ZuulServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulServerApplication.class, args);
    }

    @GetMapping(value = "/status")
    @ResponseBody
    public String getPatientByExpertId(){
        return "zuul server controller";
    }
}

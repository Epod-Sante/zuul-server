package ca.uqtr.zuulserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@AllArgsConstructor
@RedisHash("Token")
public class Token implements Serializable {
    @Id
    private String username;
    private String refreshToken;
}

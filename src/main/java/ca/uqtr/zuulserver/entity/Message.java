package ca.uqtr.zuulserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Message {

    private Timestamp time;
    private String subscriptionId;
}

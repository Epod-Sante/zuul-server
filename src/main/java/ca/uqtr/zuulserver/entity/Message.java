package ca.uqtr.zuulserver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Message implements Serializable {

    @JsonProperty("time")
    private Timestamp time;
    @JsonProperty("subscriptionId")
    private String subscriptionId;
}

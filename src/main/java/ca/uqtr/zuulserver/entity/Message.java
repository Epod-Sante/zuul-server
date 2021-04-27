package ca.uqtr.zuulserver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.sql.Timestamp;

public class Message  implements Serializable {
    @JsonProperty("time")
    private Timestamp time;
    @JsonProperty("subscriptionId")
    private String subscriptionId;

    public Message() {
    }

    public Message(Timestamp time, String subscriptionId) {
        this.time = time;
        this.subscriptionId = subscriptionId;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }


}

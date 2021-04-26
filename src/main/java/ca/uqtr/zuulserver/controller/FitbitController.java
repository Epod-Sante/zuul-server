package ca.uqtr.zuulserver.controller;

import ca.uqtr.zuulserver.entity.Message;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("fitbit")
public class FitbitController {

    final private RabbitMQSender rabbitMQSender;

    @Autowired
    public FitbitController(RabbitMQSender rabbitMQSender) {
        this.rabbitMQSender = rabbitMQSender;
    }


    // @JsonInclude(JsonInclude.Include.NON_NULL)
    @PostMapping("/notifications")
    public ResponseEntity<HttpStatus> getFitBitNotificationData(@RequestBody String responseFromAPI) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        try {
            JSONArray jsonArray = new JSONArray(responseFromAPI);
            JSONObject obj = (JSONObject) jsonArray.get(0);
            String subscriptionId = obj.getString("subscriptionId");
            executorService.schedule(() -> {
                rabbitMQSender.send(new Message(new Timestamp(System.currentTimeMillis()), subscriptionId));
            }, 10, TimeUnit.SECONDS);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } finally {
            executorService.shutdown();
        }
    }

}

package net.wessendorf.microprofile.kafka.consumer;

import net.wessendorf.kafka.cdi.annotation.Consumer;
import net.wessendorf.microprofile.service.ApnsPushSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class PushConsumer {

    @Inject
    private ApnsPushSender sender;

    private Logger logger = LoggerFactory.getLogger(PushConsumer.class);

    /**
     * Simple listener that receives messages from the Kafka broker
     */
    @Consumer(topic = "push_messages_raw", groupId = "myGroupID")
    public void receiver(final String key, final String jsonObject) {
        logger.info("Got message {} with {}", key, jsonObject);

        sender.sendPushNotification(key, jsonObject);
    }
}

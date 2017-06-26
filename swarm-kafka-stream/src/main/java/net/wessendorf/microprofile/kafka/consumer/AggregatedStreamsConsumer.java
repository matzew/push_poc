package net.wessendorf.microprofile.kafka.consumer;

import net.wessendorf.kafka.cdi.annotation.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregatedStreamsConsumer {

    private final Logger logger = LoggerFactory.getLogger(AggregatedStreamsConsumer.class);

    @Consumer(topic = "successMessagesPerJob", groupId = "successMessagesPerJobProcessor")
    public void successMessagesPerJob(final Long metricPerAppleDevice) {

        logger.info("successMessagesPerJob: {}", metricPerAppleDevice);
    }
    @Consumer(topic = "failedMessagesPerJob", groupId = "failedMessagesPerJobProcessor")
    public void failedMessagesPerJob(final Long metricPerAppleDevice) {

        logger.info("failedMessagesPerJob: {}", metricPerAppleDevice);
    }
    @Consumer(topic = "totalMessagesPerJob", groupId = "totalMessagesPerJobProcessor")
    public void totalMessagesPerJob(final Long metricPerAppleDevice) {

        logger.info("totalMessagesPerJob: {}", metricPerAppleDevice);
    }

}

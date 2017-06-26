package net.wessendorf.microprofile.service;

import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.wessendorf.kafka.SimpleKafkaProducer;
import net.wessendorf.kafka.cdi.annotation.Producer;
import net.wessendorf.microprofile.apns.ServiceConstructor;
import net.wessendorf.microprofile.apns.SimpleApnsClientCache;
import net.wessendorf.microprofile.apns.TokenDB;
import net.wessendorf.microprofile.utils.JsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;

public class ApnsPushSender {

    private final Logger logger = LoggerFactory.getLogger(ApnsPushSender.class);

    @Producer(topic = "push_messages_metrics")
    private SimpleKafkaProducer<String, String> producer;

    @Inject
    private SimpleApnsClientCache simpleApnsClientCache;


    public void sendPushNotification(final String jobId, final String rawJson) {
        final Map<String, Map<String, String>> pushMsg = JsonConverter.transform(rawJson);
        final String payload = createPushPayload(pushMsg.get("message").get("alert"), pushMsg.get("message").get("sound"));

        logger.info("Submitted Push request to APNs for further processing....");
        distributePayload(jobId, payload);
    }

    private void distributePayload(final String pushJobID, final String payload) {

        final List<String> tokens = TokenDB.loadDeviceTokens();


        ApnsClient apnsClient = null;
        {
            try {
                apnsClient = receiveApnsConnection("net.wessendorf.aerodoc");
            } catch (IllegalArgumentException iae) {
                logger.error(iae.getMessage(), iae);
            }
        }

        if (apnsClient.isConnected()) {

            for (final String token : tokens) {

                final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, "net.wessendorf.foo.bar.doz", payload);
                final Future<PushNotificationResponse<SimpleApnsPushNotification>> notificationSendFuture = apnsClient.sendNotification(pushNotification);

                notificationSendFuture.addListener(new GenericFutureListener<Future<? super PushNotificationResponse<SimpleApnsPushNotification>>>() {
                    @Override
                    public void operationComplete(Future<? super PushNotificationResponse<SimpleApnsPushNotification>> future) throws Exception {

                        // we could submit "something" to APNs
                        if (future.isSuccess()) {
                            handlePushNotificationResponsePerToken(pushJobID, notificationSendFuture.get());
                        }
                    }
                });
            }

        } else {
            logger.error("Unable to send notifications, client is not connected");
        }
    }



    // ----------------------------- helper ---------------

    private String createPushPayload(final String message, final String sound) {
        final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();

        // only set badge if needed/included in user's payload
        payloadBuilder
                .setAlertBody(message)
                .setSoundFileName(sound);

        return payloadBuilder.buildWithDefaultMaximumLength();
    }

    private void handlePushNotificationResponsePerToken(final String jobID, final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse ) {

        final String deviceToken = pushNotificationResponse.getPushNotification().getToken();

        if (pushNotificationResponse.isAccepted()) {
            logger.trace("Push notification for '{}' (payload={})", deviceToken, pushNotificationResponse.getPushNotification().getPayload());

            producer.send(jobID, "Success");
        } else {
            final String rejectReason = pushNotificationResponse.getRejectionReason();
            logger.trace("Push Message has been rejected with reason: {}", rejectReason);


            producer.send(jobID, "Rejected");


            // token is either invalid, or did just expire
            if ((pushNotificationResponse.getTokenInvalidationTimestamp() != null) || ("BadDeviceToken".equals(rejectReason))) {
                logger.info(rejectReason + ", removing token: " + deviceToken);
            }
        }
    }


    private ApnsClient receiveApnsConnection(final String topic) {
        return simpleApnsClientCache.getApnsClientForVariant(topic, new ServiceConstructor<ApnsClient>() {
            @Override
            public ApnsClient construct() {

                final ApnsClient apnsClient = buildApnsClient();

                // connect and wait:
                connectToDestinations(apnsClient);

                // APNS client has auto-reconnect, but let's log when that happens
                apnsClient.getReconnectionFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        logger.trace("Reconnecting to APNs");
                    }
                });
                return apnsClient;
            }
        });
    }


    private ApnsClient buildApnsClient() {

        final ApnsClient apnsClient;
        {
            try {
                apnsClient =new ApnsClientBuilder()
                        .setClientCredentials(Thread.currentThread().getContextClassLoader().getResourceAsStream("/empty.p12"), "empty")
                        .build();

                return apnsClient;

            } catch (Exception e) {
                logger.error("error construting apns client", e);
            }
        }
        // indicating an incomplete service
        throw new IllegalArgumentException("Not able to construct APNS client");
    }

    private synchronized void connectToDestinations(final ApnsClient apnsClient) {

        logger.debug("connecting to APNs");
        final Future<Void> connectFuture = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
        try {
            connectFuture.await();
        } catch (InterruptedException e) {
            logger.error("Error connecting to APNs", e);
        }
    }

}

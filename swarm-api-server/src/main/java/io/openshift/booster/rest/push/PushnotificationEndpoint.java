package io.openshift.booster.rest.push;

import io.openshift.booster.rest.GreetingEndpoint;
import io.openshift.booster.util.JsonConverter;
import net.wessendorf.kafka.SimpleKafkaProducer;
import net.wessendorf.kafka.cdi.annotation.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

@Path("/push")
public class PushnotificationEndpoint {

    private final Logger logger = LoggerFactory.getLogger(GreetingEndpoint.PushnotificationEndpoint.class);

    @Producer(topic = "push_messages_raw")
    private SimpleKafkaProducer<String, String> producer;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response processPushRequest(final Map<String, Map<String, String>> pushMsg) {
        final String pushJobID = UUID.randomUUID().toString();

        final String jsonPayload = JsonConverter.toJsonString(pushMsg);

        if (! jsonPayload.isEmpty()) {

            logger.info("publishing push message to topic");
            logger.trace("payload {}", jsonPayload);
            producer.send(pushJobID, jsonPayload);
        }

        // 202 is enough - we have no guarantee anyways...
        return Response.accepted().build();
    }
}

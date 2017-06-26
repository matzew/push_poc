package net.wessendorf.microprofile.utils;

import net.wessendorf.microprofile.kafka.streams.StreamProcessor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import java.util.Properties;

import static net.wessendorf.microprofile.utils.EnvironmentResolver.resolveKafkaService;

@ApplicationScoped
public class StreamProcessorRegistrationHook {

    private Logger logger = LoggerFactory.getLogger(StreamProcessorRegistrationHook.class);

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {

        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-pipe");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, resolveKafkaService());
        props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        //props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 3000L);

        // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


        final StreamProcessor processor = new StreamProcessor();
        processor.startProcessing(props);
    }
}

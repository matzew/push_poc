package net.wessendorf.microprofile.config;

import net.wessendorf.kafka.cdi.annotation.KafkaConfig;

@KafkaConfig(
        bootstrapServers = "#{KAFKA_SERVICE_HOST}:#{KAFKA_SERVICE_PORT}"
)
public class ClusterConfig {
}

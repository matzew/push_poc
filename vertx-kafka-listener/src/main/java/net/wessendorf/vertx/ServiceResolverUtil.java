package net.wessendorf.vertx;

public final class ServiceResolverUtil {

    private ServiceResolverUtil() {
        // no-op
    }

    private static String KAFKA_SERVICE_HOST = "KAFKA_SERVICE_HOST";
    private static String KAFKA_SERVICE_PORT = "KAFKA_SERVICE_PORT";


    public static String resolveKafkaService() {

        return (resolveHost() + ":" + resolvePort());
    }

    private static String resolveHost() {

        return resolver(KAFKA_SERVICE_HOST);
    }
    private static String resolvePort() {

        return resolver(KAFKA_SERVICE_PORT);
    }

    private static String resolver(final String variable) {

        String value = System.getProperty(variable);
        if (value == null) {
            value = System.getenv(variable);
        }
        if (value == null) {
            throw new RuntimeException("Could not resolve: " + variable);
        }
        return value;
    }




}

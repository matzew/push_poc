package io.openshift.booster.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class JsonConverter {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static Logger LOGGER = LoggerFactory.getLogger(JsonConverter.class);

    public static String toJsonString(final Map<String, Map<String, String>> pushMsg) {

        try {
            return OBJECT_MAPPER.writeValueAsString(pushMsg);
        } catch (JsonProcessingException e) {
            LOGGER.error("processing error", e);
            return "";
        }
    }
}

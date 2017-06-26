package net.wessendorf.microprofile.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Created by matzew on 6/14/17.
 */
public final class JsonConverter {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static Logger LOGGER = LoggerFactory.getLogger(JsonConverter.class);


    public static Map<String, Map<String, String>> transform(final String json) {
        try {
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (IOException e) {
            LOGGER.error("processing error", e);
            return Collections.emptyMap();
        }
    }
}

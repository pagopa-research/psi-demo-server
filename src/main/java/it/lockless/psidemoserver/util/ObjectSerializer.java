package it.lockless.psidemoserver.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import psi.exception.CustomRuntimeException;

import java.util.Base64;

/**
 * Helper class that serializes/deserializes objects to/from JSON strings
 */
public class ObjectSerializer {

    private ObjectSerializer(){}

    public static <T> String convertObjectToString(T object){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        JsonNode jsonNodeJSON = objectMapper.valueToTree(object);
        try {
            byte[] jsonNodeBytes = objectMapper.writeValueAsBytes(jsonNodeJSON);
            return Base64.getEncoder().encodeToString(jsonNodeBytes);
        } catch (JsonProcessingException e) {
            throw new CustomRuntimeException("Impossible to convert object to base64");
        }
    }

    public static <T> T convertStringToObject(String base64, Class<T> typeParameterClass){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String decodedCursor = new String(Base64.getDecoder().decode(base64));
        try {
            JsonNode jsonNode =  new ObjectMapper().readTree(decodedCursor);
            return objectMapper.treeToValue(jsonNode, typeParameterClass);
        } catch (JsonProcessingException e) {
            throw new CustomRuntimeException("Impossible to convert base64 to object");
        }
    }
}

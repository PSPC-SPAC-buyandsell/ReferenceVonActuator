/*
Copyright 2017-2018 Government of Canada - Public Services and Procurement Canada - buyandsell.gc.ca

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package ca.gc.pspc.referencevonactuator.intg;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public abstract class JsonUtil {
    static ObjectMapper defaultMapper;

    public static JsonNode getJsonNodeFromClasspath(String name) throws JsonLoadException {
        JsonNode node = null;
        try {
            InputStream is1 = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(name);
            if (is1 != null)
                node = getDefaultMapper().readTree(is1);
        }
        catch (Exception e) {
            throw new JsonLoadException(e);
        }
        return node;
    }

    public static JsonNode getJsonNodeFromStringContent(String content) throws JsonLoadException {
        JsonNode node;
        try {
            node = getDefaultMapper().readTree(content);
        }
        catch (Exception e) {
            throw new JsonLoadException(e);
        }
        return node;
    }

    public static JsonNode jsonObject() {
        return getDefaultMapper().createObjectNode();
    }

    public static JsonNode jsonArray() {
        return getDefaultMapper().createArrayNode();
    }

    public static JsonNode toJsonNode(Map<String, ?> data) {
        JsonNode jsonNode = getDefaultMapper().convertValue(data, JsonNode.class);
        return jsonNode;
    }

    public static JsonNode toJsonNode(List<?> data) {
        JsonNode jsonNode = getDefaultMapper().convertValue(data, JsonNode.class);
        return jsonNode;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertToMap(JsonNode jsonNode) {
        return getDefaultMapper().convertValue(jsonNode, Map.class);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> convertToList(JsonNode jsonNode) {
        return getDefaultMapper().convertValue(jsonNode, List.class);
    }

    public static String pprint(JsonNode jsonNode) throws JsonValidateException {
        try {
            return getDefaultMapper().writeValueAsString(jsonNode);
        }
        catch (JsonProcessingException e) {
            throw new JsonValidateException(e);
        }
    }

    public static String pprint(Map<String, ?> data) throws JsonValidateException {
        return pprint(toJsonNode(data));
    }

    public static String pprint(List<?> data) throws JsonValidateException {
        return pprint(toJsonNode(data));
    }

    public static ObjectMapper getDefaultMapper() {
        if (defaultMapper == null) {
            defaultMapper = new ObjectMapper()
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false /* force ISO8601 */)
                    .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
                    .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)
                    .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true)
                    .configure(SerializationFeature.INDENT_OUTPUT, true);// .setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            defaultMapper.setDateFormat(df);
        }
        return defaultMapper;
    }
}

package ca.gc.pspc.referencevonactuator.vonconnector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import ca.gc.pspc.referencevonactuator.intg.JsonUtil;

public class ProtoUtil {
    /**
     * Return list of schema key dicts json dicts for "schemata" specification in protocol tokens.
     *
     * @param schemaKeys
     *     list of SchemaKey instances
     * @return json list of schema key json dicts for "schemata" specification in protocol tokens.
     */
    public static ArrayNode listSchemata(SchemaKey... schemaKeys) {
        ArrayNode rv = (ArrayNode)JsonUtil.jsonArray();
        for (SchemaKey sKey : schemaKeys) {
            Map<String, String> protoSchemaKey = new HashMap<>();
            protoSchemaKey.put("origin-did", sKey.getOriginDid());
            protoSchemaKey.put("name", sKey.getName());
            protoSchemaKey.put("version", sKey.getVersion());
            rv.add(JsonUtil.toJsonNode(protoSchemaKey));
        }
        return rv;
    }

    /**
     * Return attr-match list entry (json node) for specification in protocol tokens.
     * 
     * @param sKey
     *      schema key instance
     * @param matches
     *      mapping of attribute names and values to match from schema per input schema key
     * @return json node with attr-match list entry for specification in protocol tokens.
     */
    public static ObjectNode attrMatch(SchemaKey sKey, ObjectNode matches) {
        Map<String, JsonNode> rv = new HashMap<>();

        Map<String, String> protoSchemaKey = new HashMap<>();
        protoSchemaKey.put("origin-did", sKey.getOriginDid());
        protoSchemaKey.put("name", sKey.getName());
        protoSchemaKey.put("version", sKey.getVersion());
        rv.put("schema", JsonUtil.toJsonNode(protoSchemaKey));

        rv.put("match", matches);

        return (ObjectNode)JsonUtil.toJsonNode(rv);
    }

    /**
     * Return (inner) predicate-match entry for its "match" list of predicate match specifications.
     * 
     * @param attr
     *      attribute name
     * @param predType
     *      predicate type - specify ">="
     * @param value
     *      comparison value
     * @return one ObjectNode entry for "match" list specification in "predicate-match" protocol tokens.
     */
    public static ObjectNode predMatchMatch(String attr, String predType, int value) {
        ObjectNode rv = (ObjectNode)JsonUtil.jsonObject();
        rv.set("attr", new TextNode(attr));
        rv.set("pred-type", new TextNode(predType));
        rv.set("value", new IntNode(value));

        return rv;
    }

    /**
     * Return predicate-match list entry for specification in protocol tokens.
     * 
     * @param sKey
     *      schema key
     * @param matches
     *      all matches, each with schema attribute, predicate type, and comparison value (as predMatchMatch() builds)
     * @return
     *      one (ObjectNode) for "predicate-match" list specification in protocol tokens
     */
    public static ObjectNode predMatch(SchemaKey sKey, ArrayNode matches) {
        ObjectNode rv = (ObjectNode)JsonUtil.jsonObject();

        ObjectNode schema = (ObjectNode)JsonUtil.jsonObject();
        schema.set("origin-did", new TextNode(sKey.getOriginDid()));
        schema.set("name", new TextNode(sKey.getName()));
        schema.set("version", new TextNode(sKey.getVersion()));
        rv.set("schema", schema);

        rv.set("match", matches);
        return rv;
    }

    /**
     * Return requested-attrs (json node) list entry for specification in protocol tokens.
     * 
     * @param sKey
     *      schema key instance
     * @param attr_names
     *      attribute names to request from schema as input schema key specifies
     * @return one (json node) list entry for "requested-attrs" list of nodes within protocol tokens
     */
    public static ObjectNode reqAttrs(SchemaKey sKey, String... attrNames) {
        Map<String, JsonNode> rv = new HashMap<>();

        Map<String, String> protoSchemaKey = new HashMap<>();
        protoSchemaKey.put("origin-did", sKey.getOriginDid());
        protoSchemaKey.put("name", sKey.getName());
        protoSchemaKey.put("version", sKey.getVersion());
        rv.put("schema", JsonUtil.toJsonNode(protoSchemaKey));

        JsonNode protoNames = JsonUtil.toJsonNode(Arrays.asList(attrNames));
        rv.put("names", protoNames);

        return (ObjectNode)JsonUtil.toJsonNode(rv);
    }
}

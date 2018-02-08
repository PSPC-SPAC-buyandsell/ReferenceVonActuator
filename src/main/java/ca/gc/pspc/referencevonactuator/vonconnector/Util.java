package ca.gc.pspc.referencevonactuator.vonconnector;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Util {
    /**
     * Decode encoded claim values.
     *
     * @param value
     *     the numeric string to decode
     * @return the decoded value (String or null)
     */
    public static String decode(String value) {
        final BigInteger POW_2_32 = new BigInteger("4294967296");
        BigInteger bigValue;
        try {
            bigValue = new BigInteger(value);
            if ((BigInteger.ZERO.compareTo(bigValue) <= 0) && (bigValue.compareTo(POW_2_32) < 0)) {
                return bigValue.toString();
            }
        }
        catch (NumberFormatException x) {
            throw new IllegalArgumentException(String.format(
                "decode() requires a numeric string; [%s] fails",
                value));
        }

        BigInteger i = bigValue.subtract(POW_2_32);
        if (i.compareTo(BigInteger.ZERO) == 0) {
            return ""; // special case: empty string encodes as 4294967296
        }
        else if (i.compareTo(BigInteger.ONE) == 0) {
            return null; // sentinel 2**32 + 1
        }

        byte[] bytes = i.toByteArray();
        if (bytes.length % 2 != 0) {
            throw new IllegalArgumentException("Encoded value does not decode to an even number of UTF-8 characters");
        }
        StringBuffer rv = new StringBuffer();
        for (int j = 0; j < bytes.length / 2; j++) { // unhexlify
            int top = Character.digit(bytes[2 * j], 16);
            int bot = Character.digit(bytes[2 * j + 1], 16);
            rv.append((char)((top << 4) + bot));
        }
        return rv.toString();
    }

    /**
     * Find indy-sdk claims matching input filter from within input claims structure. Return presentable map
     * from referents to attribute-value pairs for corresponding claim, allowing end-user selection of claim
     * of interest, which further code can then specify by referent via <code>pruneClaims()</code>.
     *
     * @param claimsNode
     *     claims structure (at <code>["claims"]</code>) as HolderProver agent returns in response
     *     to POST <code>claims-request</code> message type;
     *     e.g., {
     *         "attrs": {
     *             "attr0_uuid": [
     *                 {
     *                     "referent": "claim::00000000-0000-0000-0000-000000000000",
     *                     "attrs": {
     *                         "attr0": "2",
     *                         "attr1": "Hello",
     *                         "attr2": "World"
     *                     },
     *                     "issuer_did": "Q4zqM7aXqm7gDQkUVLng9h",
     *                     "schema_key": {
     *                          "did": "Q4zqM7aX...",
     *                          "name": "bc-reg",
     *                          "version": "1.0"
     *                     },
     *                     "revoc_reg_seq_no": null
     *                 },
     *                 {
     *                     "referent": "claim::00000000-0000-0000-0000-111111111111",
     *                     "attrs": {
     *                         "attr0": "1",
     *                         "attr1": "Nice",
     *                         "attr2": "Tractor"
     *                     },
     *                     "issuer_did": "Q4zqM7aXqm7gDQkUVLng9h",
     *                     "schema_key": {
     *                          "did": "Q4zqM7aX...",
     *                          "name": "bc-reg",
     *                          "version": "1.0"
     *                     },
     *                     "revoc_reg_seq_no": null
     *                 }
     *             ],
     *             "attr1_uuid": [
     *                 {
     *                     "referent": "claim::00000000-0000-0000-0000-000000000000",
     *                     "attrs": {
     *                         "attr0": "2",
     *                         "attr1": "Hello",
     *                         "attr2": "World"
     *                     },
     *                     "issuer_did": "Q4zqM7aXqm7gDQkUVLng9h",
     *                     "schema_key": {
     *                          "did": "Q4zqM7aX...",
     *                          "name": "bc-reg",
     *                          "version": "1.0"
     *                     },
     *                     "revoc_reg_seq_no": null
     *                 },
     *                 {
     *                     "referent": "claim::00000000-0000-0000-0000-111111111111",
     *                     "attrs": {
     *                         "attr0": "1",
     *                         "attr1": "Nice",
     *                         "attr2": "Tractor"
     *                     },
     *                     "issuer_did": "Q4zqM7aXqm7gDQkUVLng9h",
     *                     "schema_key": {
     *                          "did": "Q4zqM7aX...",
     *                          "name": "bc-reg",
     *                          "version": "1.0"
     *                     },
     *                     "revoc_reg_seq_no": null
     *                 }
     *             ],
     *             "attr2_uuid": [
     *                 {
     *                     "referent": "claim::00000000-0000-0000-0000-000000000000",
     *                     "attrs": {
     *                         "attr0": "2",
     *                         "attr1": "Hello",
     *                         "attr2": "World"
     *                     },
     *                     "issuer_did": "Q4zqM7aXqm7gDQkUVLng9h",
     *                     "schema_key": {
     *                          "did": "Q4zqM7aX...",
     *                          "name": "bc-reg",
     *                          "version": "1.0"
     *                     },
     *                     "revoc_reg_seq_no": null
     *                 },
     *                 {
     *                     "referent": "claim::00000000-0000-0000-0000-111111111111",
     *                     "attrs": {
     *                         "attr0": "1",
     *                         "attr1": "Nice",
     *                         "attr2": "Tractor"
     *                     },
     *                     "issuer_did": "Q4zqM7aXqm7gDQkUVLng9h",
     *                     "schema_key": {
     *                          "did": "Q4zqM7aX...",
     *                          "name": "bc-reg",
     *                          "version": "1.0"
     *                     },
     *                     "revoc_reg_seq_no": null
     *                 }
     *             ]
     *         }
     *     }
     * @param filt
     *      map from schema key to json dict with schema attribute names and values to match;
     *      e.g.,
     *      {
     *          new SchemaKey("Q4zqM7aX...", "bc-reg", "1.0"): {
     *              "attr0": "2",
     *              "attr1": "Hello"
     *          }
     *      }
     * @return human-legible map, referent to a json node with attributes and values matching input filter,
     *     for presentation so that an end-user can choose the claim(s) of interest.
     */
    public static Map<String, JsonNode> claimsFor(JsonNode claimsNode, Map<SchemaKey, ObjectNode> filt) {
        JsonNode uuid2claimsNode = claimsNode.get("attrs");
        if (uuid2claimsNode == null) {
            return null;
        }

        Map<String, JsonNode> rv = new HashMap<>();
        Iterator<String> referents = uuid2claimsNode.fieldNames();
        while (referents.hasNext()) {
            String referent = referents.next();
            JsonNode innerClaimsNode = uuid2claimsNode.get(referent);
            Iterator<JsonNode> innerClaims = innerClaimsNode.elements();
            while (innerClaims.hasNext()) {
                JsonNode innerClaimNode = innerClaims.next();
                String innerClaimReferent = innerClaimNode.get("referent").textValue();
                if (rv.keySet().contains(innerClaimReferent)) {
                    continue;
                }

                JsonNode innerClaimAttrsNode = innerClaimNode.get("attrs");

                if (filt == null) {
                    rv.put(innerClaimReferent, innerClaimAttrsNode);
                    continue;
                }

                SchemaKey claimSchemaKey = new SchemaKey((ObjectNode)innerClaimNode.get("schema_key"));
                if (!filt.containsKey(claimSchemaKey)) {
                    continue;
                }

                Iterator<String> filtAttrs = filt.get(claimSchemaKey).fieldNames();
                boolean isBumf = false;
                while (!isBumf && filtAttrs.hasNext()) {
                    String filtAttr = filtAttrs.next();
                    if (!innerClaimAttrsNode.has(filtAttr)) {
                        isBumf = true;
                        continue;
                    }
                    if (!innerClaimAttrsNode.get(filtAttr).equals(filt.get(claimSchemaKey).get(filtAttr))) {
                        isBumf = true;
                        continue;
                    }

                }
                if (isBumf) {
                    continue;
                }

                rv.put(innerClaimReferent, innerClaimAttrsNode);
            }
        }

        return rv;
    }

    /**
     * Given a claims structure and a list of referents (wallet claim-uuids),
     * return a map from each referent to its corresponding schema key instance.
     * 
     * @param claimsNode
     *     claims structure (at <code>["claims"]</code>) as HolderProver agent returns in response
     *     to POST <code>claims-request</code> message type
     * @param referents
     *     the set of referents, as specified in claims json structure, whose corresponding claims to retain
     *
     * @return schema key per referent (empty Map if no such referents present)
     */
    public static Map<String, SchemaKey> schemaKeysFor(ObjectNode claimsNode, Set<String> referents) {
        Map<String, SchemaKey> rv = new HashMap<>();
        JsonNode uuid2claimsNode = claimsNode.get("attrs");
        if (uuid2claimsNode == null) {
            return rv;
        }

        Iterator<String> attrUuids = uuid2claimsNode.fieldNames();
        while (attrUuids.hasNext()) {
            String attrUuid = attrUuids.next();
            JsonNode innerClaimsNode = uuid2claimsNode.get(attrUuid);
            Iterator<JsonNode> innerClaims = innerClaimsNode.elements();
            while (innerClaims.hasNext()) {
                JsonNode innerClaimNode = innerClaims.next();
                String innerClaimReferent = innerClaimNode.get("referent").textValue();
                if (rv.keySet().contains(innerClaimReferent)) {
                    continue;
                }

                rv.put(innerClaimReferent, new SchemaKey((ObjectNode)innerClaimNode.get("schema_key")));
            }
        }

        return rv;
    }

    /**
     * Strip all claims out of the input json claims structure that do not match any of the input referents.
     *
     * @param claimsNode
     *     claims structure (at <code>["claims"]</code>) as HolderProver agent returns in response
     *     to POST <code>claims-request</code> message type
     * @param referents
     *     the set of referents, as specified in claims json structure, whose corresponding claims to retain
     *
     * @return the reduced claims json structure
     */
    public static ObjectNode pruneClaims(ObjectNode claimsNode, Set<String> referents) {
        ObjectNode rv = claimsNode;
        JsonNode uuid2claimsNode = rv.get("attrs");
        Iterator<String> attrUuids = uuid2claimsNode.fieldNames();
        while (attrUuids.hasNext()) {
            String attrUuid = attrUuids.next(); // attr0_uuid
            ArrayNode claimsByUuidNode = (ArrayNode)uuid2claimsNode.get(attrUuid); // [{...}, {...}, ... {...}]
            int lenClaimsByUuidNode = claimsByUuidNode.size();
            List<Integer> bumf = new ArrayList<>();
            for (int i = 0; i < lenClaimsByUuidNode; i++) {
                JsonNode innerClaim = claimsByUuidNode.get(i);
                String innerClaimUuid = innerClaim.get("referent").textValue(); // "claim::b2d9f990-..."
                if (!referents.contains(innerClaimUuid)) {
                    bumf.add(i);
                }
            }
            ListIterator<Integer> bumfIter = bumf.listIterator(bumf.size());
            while (bumfIter.hasPrevious()) { // reverse, to retain indices through deletion
                claimsByUuidNode.remove(bumfIter.previous());
            }
        }

        return rv;
    }

    /**
     * Fetch revealed attributes from input proof and return map from referents to nested
     * maps from attribute names to (decoded) values, for processing as further claims downstream.
     *
     * @param proofNode
     *     proof structure (at <code>["proof"]</code>) as HolderProver agent returns in response
     *     to POST <proof-request> message type -- at present only single-claim structure is supported
     *
     * @return map between referents and nested maps from revealed attribute names to (decoded) values.
     */
    public static Map<String, Map<String, String>> revealedAttrs(JsonNode proofNode) {
        Map<String, Map<String, String>> rv = new HashMap<>();
        JsonNode proofsNode = proofNode.get("proof").get("proofs");
        Iterator<String> referents = proofsNode.fieldNames();
        while (referents.hasNext()) {
            String referent = referents.next();
            JsonNode revealedNode = proofsNode
                .get(referent)
                .get("primary_proof")
                .get("eq_proof")
                .get("revealed_attrs");

            Map<String, String> revealed = new HashMap<>();
            Iterator<String> attrNames = revealedNode.fieldNames();
            while (attrNames.hasNext()) {
                String attrName = attrNames.next();
                revealed.put(attrName, decode(revealedNode.get(attrName).textValue()));
            }

            rv.put(referent, revealed);
        }

        return rv;
    }

    /**
     * Return database field name corresponding to input: convert to lower case, replaces camel case or
     * runs of punctuation with single underscore.
     * 
     * @param raw
     *      descriptor for field name (e.g., <code>Host Name</code> for conversion to <code>host_name</code>)
     * 
     * @return database field name.
     */
    public static String toField(String raw) {
        if ((raw == null) || raw.length() == 0) {
            return raw;
        }

        String rv = raw.trim().replaceAll("(\\p{Upper}+)", "_$1");
        rv = rv.toLowerCase().replaceAll("[\\p{Punct}\\p{Blank}]+", "_");
        if (rv.startsWith("_") && !raw.startsWith("_")) {
            rv = rv.replaceFirst("_", "");
        }

        return rv;
    }

    /**
     * Convert python-dict pretty-print to JSON heuristically: replace None values with null, single-quoted
     * string values with double-quoted.
     * 
     * @param ppp
     *      python pretty-print of JSON dict
     *
     * @return JSON string with correct quotes and nulls
     */
    public static String pydictpp2Json(String ppp) {
        return ppp.replaceAll(": None", ": null").replaceAll("'([^']+)'([:\\],}])", "\"$1\"$2");
    }

}

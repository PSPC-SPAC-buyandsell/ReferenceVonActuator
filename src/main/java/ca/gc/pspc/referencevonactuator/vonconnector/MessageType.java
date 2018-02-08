package ca.gc.pspc.referencevonactuator.vonconnector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.gc.pspc.referencevonactuator.intg.JsonLoadException;
import ca.gc.pspc.referencevonactuator.intg.JsonUtil;

/**
 * <code>MessageType</code> enum encapsulates operations and attributes particular to message types in scope.
 */
public enum MessageType {
    CLAIM_CREATE("claim-create", false),
    CLAIM_HELLO("claim-hello", true),
    CLAIM_REQUEST("claim-request", true),
    CLAIMS_RESET("claims-reset", false),
    CLAIM_STORE("claim-store", true),
    PROOF_REQUEST("proof-request", true),
    PROOF_REQUEST_BY_REFERENT("proof-request-by-referent", true),
    SCHEMA_LOOKUP("schema-lookup", false),
    VERIFICATION_REQUEST("verification-request", true);

    private String slug;
    private boolean mayProxy;

    private MessageType(String slug, boolean mayProxy) {
        this.slug = slug;
        this.mayProxy = mayProxy;
    }

    /**
     * Return slug identifying current message type in:
     * <ul>
     *     <li>URLs</li>
     *     <li>template files from <code>protocol/</code> directory</li>.
     * </ul>
     * 
     * @return slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Marshall json node for current message type with specification for input proxy DID
     * and arguments for interpolation.
     *
     * @param proxyDid
     *     DID of agent to which to proxy (null for none, handling directly)
     * @param tplArgs
     *     arguments for interpolation (i.e., ordered substitution for <code>%s</code> markers) in template
     *
     * @return json node with complete message
     * 
     * @throws JsonLoadException
     *     in case of missing or corrupt template file, or inability to marshall message to json node 
     */
    public JsonNode form(String proxyDid, Object... tplArgs) throws JsonLoadException {
        JsonNode rv = null;
        URL protoTplLoc = Thread.currentThread().getContextClassLoader().getResource(String.format(
                "von-connector/protocol/%s.json", slug));

        try {
            Path p = Paths.get(protoTplLoc.toURI());
            String template = new String(Files.readAllBytes(p));
            ObjectNode objNode = (ObjectNode)JsonUtil.getDefaultMapper().readTree(String.format(template, tplArgs));
            if (proxyDid != null) {
                if (mayProxy) {
                    ObjectNode proxy = (ObjectNode)objNode.get("data");
                    proxy.put("proxy-did", proxyDid);
                }
                else {
                    System.out.println(String.format(
                            "WARN: Message type [%s] does not accept proxy-did [%s]", slug, proxyDid));
                }
            }
            rv = (JsonNode)objNode;
        }
        catch (URISyntaxException | IOException x) {
            throw new JsonLoadException(x);
        }

        return rv;
    }
}

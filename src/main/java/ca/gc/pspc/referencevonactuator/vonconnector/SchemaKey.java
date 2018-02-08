package ca.gc.pspc.referencevonactuator.vonconnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * <code>SchemaKey</code> encapsulates schema key data (origin DID, name, version).
 */
public class SchemaKey {

    private String originDid;
    private String name;
    private String version;

    /**
     * Construct schema key on origin DID, name, version.
     * 
     * @param originDid origin DID
     * @param name schema name
     * @param version schema version
     */
    public SchemaKey(String originDid, String name, String version) {
        this.originDid = originDid;
        this.name = name;
        this.version = version;
    }

    /**
     * Construct schema key from json node with schema key data (did/issuer/identifier/etc., name, version).
     * 
     * @param schemaKeyNode schema key specifier with three key/value pairs for origin DID, name, version
     */
    public SchemaKey(ObjectNode schemaKeyNode) {
        if (schemaKeyNode.isContainerNode() && schemaKeyNode.size() == 3) {
            this.name = schemaKeyNode.get("name").textValue();
            this.version = schemaKeyNode.get("version").textValue();
            List<String> fields = new ArrayList<>();
            schemaKeyNode.fieldNames().forEachRemaining(fields::add);
            fields.remove("name");
            fields.remove("version");
            this.originDid = schemaKeyNode.get(fields.get(0)).textValue();
        }
        else {
            throw new IllegalArgumentException(String.format(
                "Specification %s does not correspond to a schema key",
                schemaKeyNode.toString()));
        }
    }

    /**
     * Return origin DID.
     *
     * @return origin DID
     */
    public String getOriginDid() {
        return originDid;
    }

    /**
     * Return name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Return version.
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Return equality comparison (for use in object hashing).
     *
     * @return true if equal
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SchemaKey)) {
            return false;
        }
        SchemaKey sKey = (SchemaKey)o;
        return Objects.equals(originDid, sKey.originDid) &&
            Objects.equals(name, sKey.name) &&
            Objects.equals(version, sKey.version);
    }

    /**
     * Generate hash code and return.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(originDid, name, version);
    }

    /**
     * Return pretty-print.
     * 
     * @return pretty-print
     */
    public String toString() {
        return String.format("SchemaKey(%s, %s, %s)", originDid, name, version);
    }
}

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

package ca.gc.pspc.referencevonactuator.vonconnector;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;

import ca.gc.pspc.referencevonactuator.intg.JsonUtil;

/**
 * <code>Agent</code> enum encapsulates operations and attributes particular to agents in scope:
 * <ul>
 *     <li>Trust Anchor</li>
 *     <li>SRI agent</li>
 *     <li>PSPC Org Book</li>
 *     <li>BC Org Book</li>
 *     <li>BC Registrar</li>.
 * </ul>
 */
public enum Agent {
    TRUST_ANCHOR("trust-anchor"),
    SRI("sri"),
    PSPC_ORG_BOOK("pspc-org-book"),
    BC_ORG_BOOK("bc-org-book"),
    BC_REGISTRAR("bc-registrar");

    private String profile;
    private String host;
    private int port;
    private String did;

    private Agent(String profile) {
        this.profile = profile;

        HttpURLConnection con = null;
        try {
            host = Config.getInstance().get(String.format("agent.%s.host", profile));
            port = Integer.parseInt(Config.getInstance().get(String.format("agent.%s.port", profile)));

            JsonNode node = getGetResponse("did");
            did = node.textValue();
        }
        catch (IOException | NumberFormatException x) {
            // still in static context: abandon startup sequence
            x.printStackTrace();
            System.exit(1);
        }
        finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    /**
     * Given a profile, returns its corresponding agent instance (null for no match).
     * 
     * @param profile
     *     The profile corresponding to the desired agent
     * 
     * @return agent instance
     */
    public static Agent forProfile(String profile) {
        for (Agent ag : values()) {
            if (ag.profile.equalsIgnoreCase(profile)) {
                return ag;
            }
        }

        return null;
    }

    /**
     * Returns profile, for logging/error messages and configuration attribute names.
     * 
     * @return profile 
     */
    public String toString() {
        return profile;
    }

    /**
     * Returns host.
     * 
     * @return host 
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns port.
     * 
     * @return port 
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns URL for agent given URL suffix following API and version (e.g., <code>api/v0/</code>).
     * Method injects protocol, host, port, API and version, and <code>format=json</code> parameter.
     * 
     * @param suffix
     *     URL fragment following API and version
     *
     * @return URL for current agent on input suffix.
     */
    public URL urlFor(String suffix) {
        String urlSuffix = String.format("api/v0/%s", suffix == null ? "" : suffix).replaceAll("/+$", "");
        try {
            return new URL(String.format("http://%s:%d/%s?format=json", host, port, urlSuffix));
        }
        catch (MalformedURLException x) {
            return null;
        }
    }

    /**
     * Get DID of current agent, pre-loaded at startup.
     * 
     * @return DID
     */
    public String getDid() {
        return did;
    }

    /**
     * POSTs message to current agent and returns response as json node. The method identifies the json
     * template file (in the <code>protocol/</code> directory from the input <code>MessageType</code> and
     * interpolates input (<code>String</code>) arguments (and the DID for the proxy agent if applicable),
     * then POSTs the result to the current agent, marshalls the result into a json node and returns it.
     * 
     * @param proxyTo
     *     instance of <code>Agent</code> to which to proxy input message; null to handle directly (no proxy)
     * @param msgType
     *     instance of <code>MessageType</code> to POST to agent
     * @param args
     *     (<code>String</code>) arguments to populate message to POST to agent
     *
     * @return json node with response
     * 
     * @throws IOException if agent does not respond with HTTP 200 response code
     */
    public JsonNode getPostResponse(
            Agent proxyTo,
            MessageType msgType,
            Object... args) throws IOException {
        assert (Stream.of(args).allMatch(s -> s instanceof String));

        URL url = urlFor(msgType.getSlug());
        HttpURLConnection con = HttpMethod.POST.getConn(url);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        JsonNode msgNode = msgType.form(proxyTo == null ? null : proxyTo.getDid(), args);
        wr.write(msgNode.toString().getBytes(StandardCharsets.UTF_8));
        wr.close();

        int httpRc = con.getResponseCode();
        if (httpRc != 200) {
            throw new IOException(String.format("Agent [%s] returned [%d]", profile, httpRc));
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        JsonNode rv = JsonUtil.getDefaultMapper().readTree(content.toString());
        return rv;
    }

    /**
     * Issues GET request to current agent, marshalls response into json node for return.
     * 
     * @param urlSuffix
     *     URL path component following API and version
     * 
     * @return json node in response from current agent
     * 
     * @throws IOException if agent does not respond with HTTP 200 response code
     */
    public JsonNode getGetResponse(String urlSuffix) throws IOException {
        URL url = urlFor(urlSuffix);
        HttpURLConnection con = HttpMethod.GET.getConn(url);
        int httpRc = con.getResponseCode();
        if (httpRc != 200) {
            throw new IOException(String.format("Agent [%s] returned HTTP %d", profile, httpRc));
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        JsonNode rv = JsonUtil.getDefaultMapper().readTree(content.toString());
        return rv;
    }
}

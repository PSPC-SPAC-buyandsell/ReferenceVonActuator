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
package ca.gc.pspc.referencevonactuator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import ca.gc.pspc.referencevonactuator.intg.JsonUtil;
import ca.gc.pspc.referencevonactuator.intg.JsonValidateException;
import ca.gc.pspc.referencevonactuator.vonconnector.Agent;
import ca.gc.pspc.referencevonactuator.vonconnector.Config;
import ca.gc.pspc.referencevonactuator.vonconnector.HttpMethod;
import ca.gc.pspc.referencevonactuator.vonconnector.MessageType;
import ca.gc.pspc.referencevonactuator.vonconnector.ProtoUtil;
import ca.gc.pspc.referencevonactuator.vonconnector.SchemaKey;
import ca.gc.pspc.referencevonactuator.vonconnector.Util;

public class ReferenceActuator {

    /*
     * Sample:
     * {
          "legal_entity_id" : {
            "name" : "legal_entity_id",
            "restrictions" : [ {
              "schema_key" : {
                "name" : "incorporation.bc_registries",
                "version" : "1.0.30",
                "did" : "27TL9VHhcQNok9QvHLVx1a"
              }
            } ]
          },
          "corp_num" : {
            "name" : "corp_num",
            "restrictions" : [ {
              "schema_key" : {
                "name" : "incorporation.bc_registries",
                "version" : "1.0.30",
                "did" : "27TL9VHhcQNok9QvHLVx1a"
              }
            } ]
          },
          "legal_name" : {
            "name" : "legal_name",
            "restrictions" : [ {
              "schema_key" : {
                "name" : "incorporation.bc_registries",
                "version" : "1.0.30",
                "did" : "27TL9VHhcQNok9QvHLVx1a"
              }
            } ]
          },
          "org_type" : {
            "name" : "org_type",
            "restrictions" : [ {
              "schema_key" : {
                "name" : "incorporation.bc_registries",
                "version" : "1.0.30",
                "did" : "27TL9VHhcQNok9QvHLVx1a"
              }
            } ]
          },
          "addressee" : {
            "name" : "addressee",
            "restrictions" : [ {
              "schema_key" : {
                "name" : "incorporation.bc_registries",
                "version" : "1.0.30",
                "did" : "27TL9VHhcQNok9QvHLVx1a"
              }
            } ]
          },
          "address_line_1" : {
            "name" : "address_line_1",
            "restrictions" : [ {
              "schema_key" : {
                "name" : "incorporation.bc_registries",
                "version" : "1.0.30",
                "did" : "27TL9VHhcQNok9QvHLVx1a"
              }
            } ]
          },
          "address_line_2" : {
            "name" : "address_line_2",
            "restrictions" : [ {
              "schema_key" : {
                "name" : "incorporation.bc_registries",
                "version" : "1.0.30",
                "did" : "27TL9VHhcQNok9QvHLVx1a"
              }
            } ]
          },
          "city" : {
            "name" : "city",
            "restrictions" : [ {
              "schema_key" : {
                "name" : "incorporation.bc_registries",
                "version" : "1.0.30",
                "did" : "27TL9VHhcQNok9QvHLVx1a"
              }
            } ]
          },
          "province" : {
            "name" : "province",
            "restrictions" : [ {
              "schema_key" : {
                "name" : "incorporation.bc_registries",
                "version" : "1.0.30",
                "did" : "27TL9VHhcQNok9QvHLVx1a"
              }
            } ]
          },
          "postal_code" : {
            "name" : "postal_code",
            "restrictions" : [ {
              "schema_key" : {
                "name" : "incorporation.bc_registries",
                "version" : "1.0.30",
                "did" : "27TL9VHhcQNok9QvHLVx1a"
              }
            } ]
          },
          "country" : {
            "name" : "country",
            "restrictions" : [ {
              "schema_key" : {
                "name" : "incorporation.bc_registries",
                "version" : "1.0.30",
                "did" : "27TL9VHhcQNok9QvHLVx1a"
              }
            } ]
          },
          "effective_date" : {
            "name" : "effective_date",
            "restrictions" : [ {
              "schema_key" : {
                "name" : "incorporation.bc_registries",
                "version" : "1.0.30",
                "did" : "27TL9VHhcQNok9QvHLVx1a"
              }
            } ]
          },
          "end_date" : {
            "name" : "end_date",
            "restrictions" : [ {
              "schema_key" : {
                "name" : "incorporation.bc_registries",
                "version" : "1.0.30",
                "did" : "27TL9VHhcQNok9QvHLVx1a"
              }
            } ]
          }
        }
    
     * GOOD:
        {
            "filters": {
                "legal_entity_id": "c914cd7d-1f44-44b2-a0d3-c0bea12067fa"
            },
            "proof_request": {
                "name": "incorporation.bc_registries",
                "version": "1.0.0",
                "nonce": "1986273812765872",
                "requested_attrs": {
                    "address_line_1": {
                        "name": "address_line_1",
                        "restrictions": [
                            {
                                "schema_key": {
                                    "did": "27TL9VHhcQNok9QvHLVx1a",
                                    "name": "incorporation.bc_registries",
                                    "version": "1.0.20"
                                }
                            }
                        ]
                    },
                    "address_line_2": {
                        "name": "address_line_2",
                        "restrictions": [
                            {
                                "schema_key": {
                                    "did": "27TL9VHhcQNok9QvHLVx1a",
                                    "name": "incorporation.bc_registries",
                                    "version": "1.0.20"
                                }
                            }
                        ]
                    },
                    "addressee": {
                        "name": "addressee",
                        "restrictions": [
                            {
                                "schema_key": {
                                    "did": "27TL9VHhcQNok9QvHLVx1a",
                                    "name": "incorporation.bc_registries",
                                    "version": "1.0.20"
                                }
                            }
                        ]
                    },
                    "city": {
                        "name": "city",
                        "restrictions": [
                            {
                                "schema_key": {
                                    "did": "27TL9VHhcQNok9QvHLVx1a",
                                    "name": "incorporation.bc_registries",
                                    "version": "1.0.20"
                                }
                            }
                        ]
                    },
                    "corp_num": {
                        "name": "corp_num",
                        "restrictions": [
                            {
                                "schema_key": {
                                    "did": "27TL9VHhcQNok9QvHLVx1a",
                                    "name": "incorporation.bc_registries",
                                    "version": "1.0.20"
                                }
                            }
                        ]
                    },
                    "country": {
                        "name": "country",
                        "restrictions": [
                            {
                                "schema_key": {
                                    "did": "27TL9VHhcQNok9QvHLVx1a",
                                    "name": "incorporation.bc_registries",
                                    "version": "1.0.20"
                                }
                            }
                        ]
                    },
                    "effective_date": {
                        "name": "effective_date",
                        "restrictions": [
                            {
                                "schema_key": {
                                    "did": "27TL9VHhcQNok9QvHLVx1a",
                                    "name": "incorporation.bc_registries",
                                    "version": "1.0.20"
                                }
                            }
                        ]
                    },
                    "end_date": {
                        "name": "end_date",
                        "restrictions": [
                            {
                                "schema_key": {
                                    "did": "27TL9VHhcQNok9QvHLVx1a",
                                    "name": "incorporation.bc_registries",
                                    "version": "1.0.20"
                                }
                            }
                        ]
                    },
                    "legal_entity_id": {
                        "name": "legal_entity_id",
                        "restrictions": [
                            {
                                "schema_key": {
                                    "did": "27TL9VHhcQNok9QvHLVx1a",
                                    "name": "incorporation.bc_registries",
                                    "version": "1.0.20"
                                }
                            }
                        ]
                    },
                    "legal_name": {
                        "name": "legal_name",
                        "restrictions": [
                            {
                                "schema_key": {
                                    "did": "27TL9VHhcQNok9QvHLVx1a",
                                    "name": "incorporation.bc_registries",
                                    "version": "1.0.20"
                                }
                            }
                        ]
                    },
                    "org_type": {
                        "name": "org_type",
                        "restrictions": [
                            {
                                "schema_key": {
                                    "did": "27TL9VHhcQNok9QvHLVx1a",
                                    "name": "incorporation.bc_registries",
                                    "version": "1.0.20"
                                }
                            }
                        ]
                    },
                    "postal_code": {
                        "name": "postal_code",
                        "restrictions": [
                            {
                                "schema_key": {
                                    "did": "27TL9VHhcQNok9QvHLVx1a",
                                    "name": "incorporation.bc_registries",
                                    "version": "1.0.20"
                                }
                            }
                        ]
                    },
                    "province": {
                        "name": "province",
                        "restrictions": [
                            {
                                "schema_key": {
                                    "did": "27TL9VHhcQNok9QvHLVx1a",
                                    "name": "incorporation.bc_registries",
                                    "version": "1.0.20"
                                }
                            }
                        ]
                    }
                },
                "requested_predicates": {}
            }
        }
     */

    private static JsonNode doReqAttrs(SchemaKey sKey, String... attrNames) {
        ObjectNode rv = (ObjectNode)JsonUtil.jsonObject();

        Map<String, String> schemaKey = new HashMap<>();
        schemaKey.put("did", sKey.getOriginDid());
        schemaKey.put("name", sKey.getName());
        schemaKey.put("version", sKey.getVersion());

        for (String attrName : attrNames) {
            Map<String, JsonNode> reqAttr = new HashMap<>();
            reqAttr.put("name", new TextNode(attrName));

            List<Map<String, Map<String, String>>> restrictions = new ArrayList<>();
            Map<String, Map<String, String>> restrictionSchemaKey = new HashMap<>();
            restrictionSchemaKey.put("schema_key", schemaKey);
            restrictions.add(restrictionSchemaKey);
            reqAttr.put("restrictions", JsonUtil.toJsonNode(restrictions));

            rv.set(attrName, JsonUtil.toJsonNode(reqAttr));
        }

        return rv;
    }

    public static void __main(String[] args) throws IOException, JsonValidateException {
        // Get schema attr_names from schema of interest
        String legalEntityId = "bf2b560f-4553-4c09-995c-b021d7663c42"; // nothing burgers
        SchemaKey sKey = new SchemaKey(
            "27TL9VHhcQNok9QvHLVx1a",
            "incorporation.bc_registries",
            "1.0.30");
        String[] schemaAttrs = new String[] {
            "legal_entity_id",
            "corp_num",
            "legal_name",
            "org_type",
            "addressee",
            "address_line_1",
            "address_line_2",
            "city",
            "province",
            "postal_code",
            "country",
            "effective_date",
            "end_date"
        };

        Map<String, String> filters = new HashMap<>();
        filters.put("legal_entity_id", legalEntityId);
        ObjectNode form = (ObjectNode)JsonUtil.jsonObject();
        form.set("filters", JsonUtil.toJsonNode(filters));

        JsonNode reqAttrs = doReqAttrs(sKey, schemaAttrs);
        ObjectNode proofRequest = (ObjectNode)JsonUtil.jsonObject();
        proofRequest.set("name", new TextNode("proof-req"));
        proofRequest.set("version", new TextNode("1.0"));
        proofRequest.set("nonce", new TextNode(Long.toString(System.currentTimeMillis())));
        proofRequest.set("requested_attrs", reqAttrs);
        form.set("proof_request", proofRequest);
        System.out.println(JsonUtil.pprint(form));

        /*
        URL schemataUrl = new URL(
            "https://github.com/bcgov/permitify/blob/master/site_templates/bc_registries/schemas.json");
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("X-CSRFToken", "8BTOz0ScuKzsMS7JgL0UXIwHfZsLsynNN4BgMsSPTt848aYpRuRLFWAYODK7uTN9");
        
        HttpURLConnection con = HttpMethod.GET.getConn(schemataUrl, headers);
        int httpRc = con.getResponseCode();
        if (httpRc != 200) {
            throw new IOException(String.format("GBC Org Book returned HTTP %d", httpRc));
        }
        
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine + "\r\n");
        }
        in.close();
        System.out.println("\n\n== 0 == HTTP Raw: " + content.toString());
        ArrayNode schemataNode = (ArrayNode)JsonUtil.getDefaultMapper().readTree(content.toString());
        System.out.println("\n\n== 1 == JSON Raw: " + JsonUtil.getDefaultMapper().writeValueAsString(schemataNode));
        // System.out.println("\n\n== 2 == JSON Pretty: " + JsonUtil.pprint(gbcClaimsNode));
        
        Iterator<JsonNode> it = schemataNode.elements();
        JsonNode schemaNode = null;
        while (it.hasNext()) {
            schemaNode = it.next();
            if (schemaNode.get("name").textValue().equals(sKey.getName()) &&
                schemaNode.get("version").textValue().equals(
                    sKey.getVersion())) {
                break;
            }
        }
        if (schemaNode == null) {
            assert (false);
        }
        */

    }

    public static void xx_main(String[] args) throws IOException, JsonValidateException {
        URL urlGbcOrgBookClaims = new URL("https://django-devex-von-test.pathfinder.gov.bc.ca/api/v1/verifiableclaims");
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("X-CSRFToken", "8BTOz0ScuKzsMS7JgL0UXIwHfZsLsynNN4BgMsSPTt848aYpRuRLFWAYODK7uTN9");

        HttpURLConnection con = HttpMethod.GET.getConn(urlGbcOrgBookClaims, headers);
        int httpRc = con.getResponseCode();
        if (httpRc != 200) {
            throw new IOException(String.format("GBC Org Book returned HTTP %d", httpRc));
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine + "\r\n");
        }
        in.close();
        System.out.println("\n\n== 0 == HTTP Raw: " + content.toString());
        ArrayNode gbcClaimsNode = (ArrayNode)JsonUtil.getDefaultMapper().readTree(content.toString());
        System.out.println("\n\n== 1 == JSON Raw: " + JsonUtil.getDefaultMapper().writeValueAsString(gbcClaimsNode));
        // System.out.println("\n\n== 2 == JSON Pretty: " + JsonUtil.pprint(gbcClaimsNode));

        Iterator<JsonNode> it = gbcClaimsNode.elements();

        String aClaimJson = null;
        while (it.hasNext()) {
            JsonNode claimJsonNode = it.next().get("claimJSON");
            if (claimJsonNode == null) {
                continue;
            }
            aClaimJson = claimJsonNode.textValue();
        }
        System.out.println("First non-null claim JSON: " + aClaimJson);
        JsonNode claimSalmonNode = gbcClaimsNode.get(743);
        String salmonClaimJson = claimSalmonNode.get("claimJSON").textValue();
        System.out.println("Salmon claim: " + salmonClaimJson);
        String fixedJson = Util.pydictpp2Json(salmonClaimJson);
        System.out.println(fixedJson);

        JsonNode salmonClaimJsonNode = JsonUtil.getDefaultMapper().readTree(fixedJson);
        System.out.println(JsonUtil.pprint(salmonClaimJsonNode));

        JsonNode claimWaccamawNode = gbcClaimsNode.get(733);
        String waccamawClaimJson = claimWaccamawNode.get("claimJSON").textValue();
        System.out.println("Waccamaw claim: " + waccamawClaimJson);
        fixedJson = Util.pydictpp2Json(waccamawClaimJson);
        System.out.println(fixedJson);

        JsonNode waccamawClaimJsonNode = JsonUtil.getDefaultMapper().readTree(fixedJson);
        System.out.println(JsonUtil.pprint(waccamawClaimJsonNode));

        Properties xlateProps = Config.getInstance().getPrefixed("schema.attr2field.");
        JsonNode waccamawClaimNode = waccamawClaimJsonNode.get("claim");
        Iterator<String> wacIt = waccamawClaimNode.fieldNames();
        while (wacIt.hasNext()) {
            String claimAttr = wacIt.next();
            if (xlateProps.containsKey(claimAttr)) {
                System.out.println(String.format(
                    "%s = %s",
                    xlateProps.getProperty(claimAttr),
                    waccamawClaimNode.get(claimAttr).get(0).textValue()));
            }
        }
    }

    public static void main(String[] args) throws IOException, JsonValidateException {
        // 1. Set up data structures
        Map<String, Agent> did2agent = new HashMap<>();
        Map<String, String> profile2did = new HashMap<>();
        Map<SchemaKey, JsonNode> schemaStore = new HashMap<>();
        Map<String, SchemaKey> S_KEY = new HashMap<>();

        // 2. Get demo agent wrappers' DIDs; get and store any originated schema configured as of interest
        for (Agent agent : Agent.values()) {
            String profile = agent.toString();
            String did = agent.getDid();
            profile2did.put(profile, did);
            did2agent.put(did, agent);

            System.out.println(String.format(
                "\n\n== 0.%d == %s: %s",
                agent.ordinal(),
                agent.toString(),
                agent.getDid()));

            Properties schemata = Config.getInstance().getPrefixed(String.format("agent.%s.schema.", profile));
            for (String name : schemata.stringPropertyNames()) {
                String[] versions = schemata.getProperty(name).split("\\s*,\\s*");
                for (String version : versions) {
                    SchemaKey sKey = new SchemaKey(did, name, version);
                    schemaStore.put(
                        sKey,
                        Agent.TRUST_ANCHOR.getPostResponse(
                            null,
                            MessageType.SCHEMA_LOOKUP,
                            sKey.getOriginDid(),
                            sKey.getName(),
                            sKey.getVersion()));
                }
            }
        }

        S_KEY.put("BC", new SchemaKey(profile2did.get("bc-registrar"), "bc-reg", "1.0"));
        S_KEY.put("SRI-1.0", new SchemaKey(profile2did.get("sri"), "sri", "1.0"));
        S_KEY.put("SRI-1.1", new SchemaKey(profile2did.get("sri"), "sri", "1.1"));
        S_KEY.put("GREEN", new SchemaKey(profile2did.get("sri"), "green", "1.0"));

        Map<SchemaKey, JsonNode> claimNode = new HashMap<>();

        int idx = 0;
        for (SchemaKey sKey : schemaStore.keySet()) {
            System.out.println(String.format(
                "\n\n== 1.%d == Schema [%s]: %s",
                idx++,
                sKey,
                JsonUtil.pprint(schemaStore.get(sKey))));
        }

        // 4. BC Org Book, PSPC org book (as HolderProvers) take claims-reset directive, restore state to base line
        JsonNode emptyRespNode = null;
        idx = 0;
        for (Agent ag : new Agent[] {Agent.BC_ORG_BOOK, Agent.PSPC_ORG_BOOK}) {
            emptyRespNode = ag.getPostResponse(
                null,
                MessageType.CLAIMS_RESET);
            assert (emptyRespNode.size() == 0);
        }

        // 5. Issuers send claim-hello to HolderProvers
        Map<SchemaKey, JsonNode> claimReq = new HashMap<>();
        idx = 0;
        for (SchemaKey sKey : schemaStore.keySet()) {
            String originDid = sKey.getOriginDid();
            Agent holderProver = Agent.forProfile(
                originDid == Agent.BC_REGISTRAR.getDid()
                    ? "bc-org-book"
                    : "pspc-org-book");
            claimReq.put(
                sKey,
                did2agent.get(originDid).getPostResponse(
                    holderProver,
                    MessageType.CLAIM_HELLO,
                    sKey.getOriginDid(),
                    sKey.getName(),
                    sKey.getVersion(),
                    originDid));
            System.out.println(String.format(
                "\n\n== 2.%d == claim-req from BC hello %s",
                idx++,
                JsonUtil.pprint(claimReq.get(sKey))));
            assert (claimReq.get(sKey).size() > 0);
        }

        // 6. BC Registrar creates claims and stores at BC Org Book (HolderProver)
        Map<SchemaKey, List<JsonNode>> claimDataNode = new HashMap<>();
        List<JsonNode> bcClaimsDataNode = new ArrayList<>();
        Map<String, Object> bcClaimData = new HashMap<>();

        bcClaimData.put("id", 1);
        bcClaimData.put("busId", 11121398);
        bcClaimData.put("orgTypeId", 2);
        bcClaimData.put("jurisdictionId", 1);
        bcClaimData.put("legalName", "The Original House of Pies");
        bcClaimData.put("effectiveDate", "2010-10-10");
        bcClaimData.put("endDate", null);
        bcClaimsDataNode.add(JsonUtil.toJsonNode(bcClaimData));

        bcClaimData.put("id", 2);
        bcClaimData.put("busId", 11133333);
        bcClaimData.put("orgTypeId", 1);
        bcClaimData.put("jurisdictionId", 1);
        bcClaimData.put("legalName", "Planet Cake");
        bcClaimData.put("effectiveDate", "2011-10-01");
        bcClaimData.put("endDate", null);
        bcClaimsDataNode.add(JsonUtil.toJsonNode(bcClaimData));

        bcClaimData.put("id", 3);
        bcClaimData.put("busId", 11144444);
        bcClaimData.put("orgTypeId", 2);
        bcClaimData.put("jurisdictionId", 1);
        bcClaimData.put("legalName", "Tart City");
        bcClaimData.put("effectiveDate", "2012-12-01");
        bcClaimData.put("endDate", null);
        bcClaimsDataNode.add(JsonUtil.toJsonNode(bcClaimData));

        claimDataNode.put(S_KEY.get("BC"), bcClaimsDataNode);

        idx = 0;
        for (SchemaKey sKey : claimDataNode.keySet()) {
            for (JsonNode c : claimDataNode.get(sKey)) {
                claimNode.put(sKey, Agent.BC_REGISTRAR.getPostResponse(
                    null,
                    MessageType.CLAIM_CREATE,
                    claimReq.get(sKey).toString(),
                    c.toString()));
                System.out.println(String.format(
                    "\n\n== 3.%d == BC claim: %s",
                    idx++,
                    JsonUtil.pprint(claimNode.get(sKey))));
                assert (claimNode.get(sKey).size() > 0);

                // claim-store, BC registrar agent proxy to BC org book agent
                emptyRespNode = Agent.BC_REGISTRAR.getPostResponse(
                    Agent.BC_ORG_BOOK,
                    MessageType.CLAIM_STORE,
                    claimNode.get(sKey).toString());
                assert (emptyRespNode.size() == 0);
            }
        }

        // 7. SRI agent proxies to BC Org Book as HolderProver to find claims; actuator filters post hoc
        JsonNode bcClaimsAllNode = Agent.SRI.getPostResponse(
            Agent.BC_ORG_BOOK,
            MessageType.CLAIM_REQUEST,
            ProtoUtil.listSchemata(S_KEY.get("BC")).toString(),
            JsonUtil.jsonArray().toString(),
            JsonUtil.jsonArray().toString(),
            JsonUtil.jsonArray().toString());
        assert (bcClaimsAllNode.size() > 0);
        System.out.println(String.format(
            "\n\n== 4 == All BC claims, no filter: %s",
            JsonUtil.pprint(bcClaimsAllNode)));

        Map<SchemaKey, ObjectNode> filt = new HashMap<>();
        filt.put(
            S_KEY.get("BC"),
            (ObjectNode)((ObjectNode)JsonUtil.jsonObject()).set(
                "legalName",
                claimDataNode.get(S_KEY.get("BC")).get(2).get("legalName")));
        Map<String, ?> bcDisplayPrunedFiltPostHoc = Util.claimsFor(bcClaimsAllNode.get("claims"), filt);
        System.out.println(String.format(
            "\n\n== 5 == BC display claims filtered post-hoc matching %s: %s",
            claimDataNode.get(S_KEY.get("BC")).get(2).get("legalName").textValue(),
            JsonUtil.pprint(bcDisplayPrunedFiltPostHoc)));

        try { // exercise proof restriction to one claim per attribute
            Agent.SRI.getPostResponse(
                Agent.BC_ORG_BOOK,
                MessageType.PROOF_REQUEST,
                ProtoUtil.listSchemata(S_KEY.get("BC")).toString(),
                JsonUtil.jsonArray().toString(),
                JsonUtil.jsonArray().toString(),
                JsonUtil.jsonArray().toString());
            assert false;
        }
        catch (IOException x) {
            // Expect 500: indy-sdk proves claims on only one claim def at a time
        }

        ObjectNode bcDisplayPruned = Util.pruneClaims(
            (ObjectNode)bcClaimsAllNode.get("claims"),
            bcDisplayPrunedFiltPostHoc.keySet());
        System.out.println(String.format(
            "\n\n== 6 == BC claims stripped down: %s",
            JsonUtil.pprint(bcDisplayPruned)));

        filt.clear();
        filt.put(S_KEY.get("BC"), (ObjectNode)JsonUtil.jsonObject());
        for (String k : new String[] {"jurisdictionId", "busId"}) {
            filt.get(S_KEY.get("BC")).set(k, claimDataNode.get(S_KEY.get("BC")).get(2).get(k));
        }
        ArrayNode claimFiltAttrMatches = (ArrayNode)JsonUtil.jsonArray();
        ObjectNode attrMatches = (ObjectNode)JsonUtil.jsonObject();
        for (String k : new String[] {"jurisdictionId", "busId"}) {
            attrMatches.set(k, claimDataNode.get(S_KEY.get("BC")).get(2).get(k));
        }
        claimFiltAttrMatches.add(ProtoUtil.attrMatch(S_KEY.get("BC"), attrMatches));
        JsonNode bcClaimsPreFiltNode = Agent.SRI.getPostResponse(
            Agent.BC_ORG_BOOK,
            MessageType.CLAIM_REQUEST,
            ProtoUtil.listSchemata(S_KEY.get("BC")).toString(),
            claimFiltAttrMatches.toString(),
            JsonUtil.jsonArray().toString(),
            JsonUtil.jsonArray().toString());
        assert (bcClaimsPreFiltNode.size() > 0);

        System.out.println(String.format(
            "\n\n== 7 == BC claims filtered a priori %s",
            JsonUtil.pprint(bcClaimsPreFiltNode)));
        Map<String, ?> bcDisplayPrunedPreFilt = Util.claimsFor(bcClaimsAllNode.get("claims"), null);
        System.out.println(String.format(
            "\n\n== 8 == BC display claims filtered a priori matching %s: %s",
            claimDataNode.get(S_KEY.get("BC")).get(2).get("legalName").textValue(),
            JsonUtil.pprint(bcDisplayPrunedPreFilt)));
        assert (bcDisplayPrunedPreFilt.keySet().equals(bcDisplayPrunedFiltPostHoc.keySet()));
        assert (bcDisplayPrunedPreFilt.size() == 1);

        // 8. BC Org Book as HolderProver creates proof and responds to request for proof (by filter)
        JsonNode bcProofRespNode = Agent.SRI.getPostResponse(
            Agent.BC_ORG_BOOK,
            MessageType.PROOF_REQUEST,
            ProtoUtil.listSchemata(S_KEY.get("BC")).toString(),
            claimFiltAttrMatches.toString(),
            JsonUtil.jsonArray().toString(),
            JsonUtil.jsonArray().toString());
        System.out.println(String.format(
            "\n\n== 9 == proof (req by filter): %s",
            JsonUtil.pprint(bcProofRespNode)));
        assert (bcProofRespNode.size() > 0);

        // 9. SRI agent as Verifier verifies proof (by filter)
        JsonNode sriBcVerificationRespNode = Agent.SRI.getPostResponse(
            null,
            MessageType.VERIFICATION_REQUEST,
            bcProofRespNode.get("proof-req").toString(),
            bcProofRespNode.get("proof").toString());
        System.out.println(String.format(
            "\n\n== 10 == SRI agent verifies BC proof (by filter) as %s",
            JsonUtil.pprint(sriBcVerificationRespNode)));
        assert (sriBcVerificationRespNode.asBoolean());

        // 10. BC Org Book agent (as HolderProver) creates proof (by referent)
        List<String> bcReferents = new ArrayList<>();
        bcDisplayPrunedPreFilt.keySet().forEach(bcReferents::add);
        SchemaKey[] sKeys = Util.schemaKeysFor(
            (ObjectNode)bcClaimsPreFiltNode.get("claims"),
            new HashSet<String>(bcReferents)).values().stream().toArray(SchemaKey[]::new);
        bcProofRespNode = Agent.SRI.getPostResponse(
            Agent.BC_ORG_BOOK,
            MessageType.PROOF_REQUEST_BY_REFERENT,
            ProtoUtil.listSchemata(sKeys).toString(),
            JsonUtil.toJsonNode(bcReferents).toString(),
            JsonUtil.jsonArray().toString());
        assert (bcProofRespNode.size() > 0);

        // 11. BC Org Book agent (as HolderProver) creates non-proof by non-referent
        try {
            Agent.SRI.getPostResponse(
                Agent.BC_ORG_BOOK,
                MessageType.PROOF_REQUEST_BY_REFERENT,
                ProtoUtil.listSchemata(sKeys).toString(),
                JsonUtil.toJsonNode(Arrays.asList(
                    new String[] {"claim::ffffffff-ffff-ffff-ffff-ffffffffffff"})).toString(),
                JsonUtil.jsonArray().toString());
            assert false;
        }
        catch (IOException x) {
            // expect 500 here, no such claim; carry on
        }

        // 12. SRI Agent as Verifier verifies proof (by referent)
        sriBcVerificationRespNode = Agent.SRI.getPostResponse(
            null,
            MessageType.VERIFICATION_REQUEST,
            bcProofRespNode.get("proof-req").toString(),
            bcProofRespNode.get("proof").toString());
        System.out.println(String.format(
            "\n\n== 11 == SRI agent verifies BC proof (by referent=%s) verifies as %s",
            JsonUtil.toJsonNode(bcReferents),
            JsonUtil.pprint(sriBcVerificationRespNode)));
        assert (sriBcVerificationRespNode.asBoolean());

        // 13. BC Org Book agent (as HolderProver) finds claims by predicate on default attr-match, req-attrs w/schema
        ArrayNode predMatchMatches = (ArrayNode)JsonUtil.jsonArray();
        for (String k : new String[] {"id"}) {
            predMatchMatches.add(ProtoUtil.predMatchMatch(
                k,
                ">=",
                claimDataNode.get(S_KEY.get("BC")).get(2).get("id").asInt()));
        }
        ArrayNode predMatches = (ArrayNode)JsonUtil.jsonArray();
        predMatches.add(ProtoUtil.predMatch(S_KEY.get("BC"), predMatchMatches));
        ArrayNode requestedAttrs = (ArrayNode)JsonUtil.jsonArray();
        requestedAttrs.add(ProtoUtil.reqAttrs(S_KEY.get("BC")));

        JsonNode claimsFoundPredNode = Agent.SRI.getPostResponse(
            Agent.BC_ORG_BOOK,
            MessageType.CLAIM_REQUEST,
            ProtoUtil.listSchemata(S_KEY.get("BC")).toString(),
            JsonUtil.jsonArray().toString(),
            predMatches.toString(),
            requestedAttrs.toString());

        Set<String> bcSchemaAttrsLessId = new HashSet<>();
        Iterator<JsonNode> it = schemaStore.get(S_KEY.get("BC")).get("data").get("attr_names").iterator();
        while (it.hasNext()) {
            String txt = it.next().textValue();
            if (txt.equals("id")) {
                continue;
            }
            bcSchemaAttrsLessId.add(txt);
        }
        Set<String> claimAttrs = new HashSet<>();
        JsonNode reqAttrsNode = claimsFoundPredNode.get("proof-req").get("requested_attrs");
        Iterator<String> attrUuids = reqAttrsNode.fieldNames();
        while (attrUuids.hasNext()) {
            String attrUuid = attrUuids.next();
            claimAttrs.add(reqAttrsNode.get(attrUuid).get("name").textValue());
        }
        assert (claimAttrs.equals(bcSchemaAttrsLessId));
        claimAttrs = new HashSet<>();
        reqAttrsNode = claimsFoundPredNode.get("proof-req").get("requested_predicates");
        attrUuids = reqAttrsNode.fieldNames();
        while (attrUuids.hasNext()) {
            String attrUuid = attrUuids.next();
            claimAttrs.add(reqAttrsNode.get(attrUuid).get("attr_name").textValue());
        }
        assert (claimAttrs.contains("id") && claimAttrs.size() == 1);

        // 14. BC Org Book agent (as HolderProver) finds claims by predicate on default attr-match and req-attrs
        predMatches.add(ProtoUtil.predMatch(S_KEY.get("BC"), predMatchMatches));
        claimsFoundPredNode = Agent.SRI.getPostResponse(
            Agent.BC_ORG_BOOK,
            MessageType.CLAIM_REQUEST,
            ProtoUtil.listSchemata(S_KEY.get("BC")).toString(),
            JsonUtil.jsonArray().toString(),
            predMatches.toString(),
            JsonUtil.jsonArray().toString());
        claimAttrs = new HashSet<>();
        reqAttrsNode = claimsFoundPredNode.get("proof-req").get("requested_attrs");
        attrUuids = reqAttrsNode.fieldNames();
        while (attrUuids.hasNext()) {
            String attrUuid = attrUuids.next();
            claimAttrs.add(reqAttrsNode.get(attrUuid).get("name").textValue());
        }
        assert (claimAttrs.equals(bcSchemaAttrsLessId));
        claimAttrs = new HashSet<>();
        reqAttrsNode = claimsFoundPredNode.get("proof-req").get("requested_predicates");
        attrUuids = reqAttrsNode.fieldNames();
        while (attrUuids.hasNext()) {
            String attrUuid = attrUuids.next();
            claimAttrs.add(reqAttrsNode.get(attrUuid).get("attr_name").textValue());
        }
        assert (claimAttrs.contains("id") && claimAttrs.size() == 1);
        System.out.println(String.format(
            "\n\n== 12 == BC claims structure by predicate: %s",
            JsonUtil.pprint(claimsFoundPredNode)));
        Map<String, JsonNode> bcDisplayPred = Util.claimsFor(claimsFoundPredNode.get("claims"), null);
        System.out.println(String.format(
            "\n\n== 13 == BC display claims by predicate: %s",
            JsonUtil.pprint(bcDisplayPred)));
        assert (bcDisplayPred.size() == 1);

        // 15. BC Org Book agent (as HolderProver) creates proof by predicate, default req-attrs
        predMatchMatches = (ArrayNode)JsonUtil.jsonArray();
        for (String k : new String[] {"id", "orgTypeId"}) {
            predMatchMatches.add(ProtoUtil.predMatchMatch(k, ">=", 2));
        }
        predMatches = (ArrayNode)JsonUtil.jsonArray();
        predMatches.add(ProtoUtil.predMatch(S_KEY.get("BC"), predMatchMatches));
        JsonNode bcProofRespPredNode = Agent.SRI.getPostResponse(
            Agent.BC_ORG_BOOK,
            MessageType.PROOF_REQUEST,
            ProtoUtil.listSchemata(S_KEY.get("BC")).toString(),
            JsonUtil.jsonArray().toString(),
            predMatches.toString(),
            JsonUtil.jsonArray().toString());
        assert (bcProofRespPredNode.size() > 0);
        System.out.println(String.format(
            "\n\n== 14 == BC proof by predicates id, orgTypeId >= 2: %s",
            JsonUtil.pprint(bcProofRespPredNode)));
        Map<String, Map<String, String>> revealedPred = Util.revealedAttrs(bcProofRespPredNode.get("proof"));
        System.out.println(String.format(
            "\n\n== 15 == BC proof revealed attrs by predicates id, orgTypeId >= 2: %s",
            JsonUtil.pprint(revealedPred)));
        assert (revealedPred.size() == 1);
        Set<String> bcProofPredRevealedAttrs = revealedPred.values().iterator().next().keySet();
        Set<String> bcSchemaAttrsLessIdOrgTypeId = new HashSet<>();
        it = schemaStore.get(S_KEY.get("BC")).get("data").get("attr_names").iterator();
        while (it.hasNext()) {
            String txt = it.next().textValue();
            if (txt.equals("id") || txt.equals("orgTypeId")) {
                continue;
            }
            bcSchemaAttrsLessIdOrgTypeId.add(txt);
        }
        assert (bcProofPredRevealedAttrs.equals(bcSchemaAttrsLessIdOrgTypeId));

        // 16. SRI Agent as Verifier verifies proof (by predicates)
        sriBcVerificationRespNode = Agent.SRI.getPostResponse(
            null,
            MessageType.VERIFICATION_REQUEST,
            bcProofRespPredNode.get("proof-req").toString(),
            bcProofRespPredNode.get("proof").toString());
        System.out.println(String.format(
            "\n\n== 16 == SRI agent verifies BC proof by predicates id, orgTypeId >= 2 as %s",
            JsonUtil.pprint(sriBcVerificationRespNode)));
        assert (sriBcVerificationRespNode.asBoolean());

        // 17. Create and store SRI registration completion claims, green claims from verified proof + extra data
        String yyyyMmDd = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String bcReferent = bcReferents.get(0); // it's unique
        Map<String, String> revealed = Util.revealedAttrs(bcProofRespNode.get("proof")).get(bcReferent);

        List<JsonNode> sri10ClaimsDataNode = new ArrayList<>();
        Map<String, Object> sri10ClaimData = new HashMap<>();
        for (String k : revealed.keySet()) {
            if (JsonUtil.convertToList(
                schemaStore.get(
                    S_KEY.get("SRI-1.0")).get("data").get("attr_names")).contains(k)) {
                sri10ClaimData.put(k, revealed.get(k));
            }
        }
        sri10ClaimData.put("sriRegDate", yyyyMmDd);
        sri10ClaimsDataNode.add(JsonUtil.toJsonNode(sri10ClaimData));
        claimDataNode.put(S_KEY.get("SRI-1.0"), sri10ClaimsDataNode);

        List<JsonNode> sri11ClaimsDataNode = new ArrayList<>();
        Map<String, Object> sri11ClaimData = new HashMap<>();
        for (String k : revealed.keySet()) {
            if (JsonUtil.convertToList(
                schemaStore.get(
                    S_KEY.get("SRI-1.1")).get("data").get("attr_names")).contains(k)) {
                sri11ClaimData.put(k, revealed.get(k));
            }
        }
        sri11ClaimData.put("sriRegDate", yyyyMmDd);
        sri11ClaimData.put("businessLang", "EN-CA");
        sri11ClaimsDataNode.add(JsonUtil.toJsonNode(sri11ClaimData));
        claimDataNode.put(S_KEY.get("SRI-1.1"), sri11ClaimsDataNode);

        List<JsonNode> greenClaimsDataNode = new ArrayList<>();
        Map<String, Object> greenClaimData = new HashMap<>();
        for (String k : revealed.keySet()) {
            if (JsonUtil.convertToList(
                schemaStore.get(
                    S_KEY.get("GREEN")).get("data").get("attr_names")).contains(k)) {
                greenClaimData.put(k, revealed.get(k));
            }
        }
        greenClaimData.put("greenLevel", "Silver");
        greenClaimData.put("auditDate", yyyyMmDd);
        greenClaimsDataNode.add(JsonUtil.toJsonNode(greenClaimData));
        claimDataNode.put(S_KEY.get("GREEN"), greenClaimsDataNode);

        idx = 0;
        for (SchemaKey sKey : claimDataNode.keySet()) {
            if (sKey.equals(S_KEY.get("BC"))) {
                continue;
            }
            for (JsonNode cNode : claimDataNode.get(sKey)) {
                System.out.println(String.format(
                    "\n\n== 17.%d == Data for SRI claim on [%s v%s]: %s",
                    idx++,
                    sKey.getName(),
                    sKey.getVersion(),
                    JsonUtil.pprint(cNode)));
            }
        }

        idx = 0;
        for (SchemaKey sKey : claimDataNode.keySet()) {
            if (sKey.equals(S_KEY.get("BC"))) {
                continue;
            }
            for (JsonNode cNode : claimDataNode.get(sKey)) {
                claimNode.put(sKey, Agent.SRI.getPostResponse(
                    null,
                    MessageType.CLAIM_CREATE,
                    claimReq.get(sKey).toString(),
                    cNode.toString()));
                assert (claimNode.get(sKey).size() > 0);

                System.out.println(String.format(
                    "\n\n== 18.%d == [%s v%s] claim : %s",
                    idx++,
                    sKey.getName(),
                    sKey.getVersion(),
                    JsonUtil.pprint(claimNode.get(sKey))));

                emptyRespNode = did2agent.get(sKey.getOriginDid()).getPostResponse(
                    Agent.PSPC_ORG_BOOK,
                    MessageType.CLAIM_STORE,
                    claimNode.get(sKey).toString());
                assert (emptyRespNode.size() == 0);
            }
        }

        // 18. SRI agent proxies to PSPC Org Book agent (as HolderProver) to find all claims, one schema at a time
        idx = 0;
        for (SchemaKey sKey : claimDataNode.keySet()) {
            if (sKey.equals(S_KEY.get("BC"))) {
                continue;
            }
            JsonNode sriClaimNode = Agent.SRI.getPostResponse(
                Agent.PSPC_ORG_BOOK,
                MessageType.CLAIM_REQUEST,
                ProtoUtil.listSchemata(sKey).toString(),
                JsonUtil.jsonArray().toString(),
                JsonUtil.jsonArray().toString(),
                JsonUtil.jsonArray().toString());
            System.out.println(String.format(
                "\n\n== 19.%d.0 == SRI claims on [%s v%s], no filter: %s",
                idx,
                sKey.getName(),
                sKey.getVersion(),
                JsonUtil.pprint(sriClaimNode)));
            assert sriClaimNode.get("claims").get("attrs").size() == schemaStore.get(sKey).get("data").get("attr_names")
                .size();

            claimFiltAttrMatches = (ArrayNode)JsonUtil.jsonArray();
            ObjectNode sriAttrMatches = (ObjectNode)JsonUtil.jsonObject();
            claimFiltAttrMatches.add(ProtoUtil.attrMatch(sKey, sriAttrMatches));
            sriClaimNode = Agent.SRI.getPostResponse(
                Agent.PSPC_ORG_BOOK,
                MessageType.CLAIM_REQUEST,
                JsonUtil.jsonArray().toString(),
                claimFiltAttrMatches.toString(),
                JsonUtil.jsonArray().toString(),
                JsonUtil.jsonArray().toString());
            System.out.println(String.format(
                "\n\n== 19.%d.1 == SRI claims, filter for all attrs in schema [%s v%s]: %s",
                idx++,
                sKey.getName(),
                sKey.getVersion(),
                JsonUtil.pprint(sriClaimNode)));
            assert sriClaimNode.get("claims").get("attrs").size() == schemaStore.get(sKey).get("data").get("attr_names")
                .size();
        }

        // 19. SRI agent proxies to PSPC Org Book (as HolderProver) to find all claims, for all schemata, on first attr
        requestedAttrs = (ArrayNode)JsonUtil.jsonArray();
        for (SchemaKey sKey : claimDataNode.keySet()) {
            if (sKey.equals(S_KEY.get("BC"))) {
                continue;
            }
            requestedAttrs.add(ProtoUtil.reqAttrs(
                sKey,
                new String[] {schemaStore.get(sKey).get("data").get("attr_names").get(0).textValue()}));
        }
        JsonNode sriClaimsAllFirstAttrNode = Agent.SRI.getPostResponse(
            Agent.PSPC_ORG_BOOK,
            MessageType.CLAIM_REQUEST,
            ProtoUtil.listSchemata(claimDataNode
                .keySet()
                .stream()
                .filter(sKey -> !sKey.equals(S_KEY.get("BC")))
                .toArray(SchemaKey[]::new)).toString(),
            JsonUtil.jsonArray().toString(),
            JsonUtil.jsonArray().toString(),
            requestedAttrs.toString());
        System.out.println(String.format(
            "\n\n== 20 == All SRI claims at PSPC Org Book, first attr only: %s",
            JsonUtil.pprint(sriClaimsAllFirstAttrNode)));
        assert (sriClaimsAllFirstAttrNode.size() > 0);

        // 20. SRI agent proxies to PSPC Org Book (as HolderProver) to find all claims, on all schemata at once
        JsonNode sriClaimsAllNode = Agent.SRI.getPostResponse(
            Agent.PSPC_ORG_BOOK,
            MessageType.CLAIM_REQUEST,
            ProtoUtil.listSchemata(claimDataNode
                .keySet()
                .stream()
                .filter(sKey -> !sKey.equals(S_KEY.get("BC")))
                .toArray(SchemaKey[]::new)).toString(),
            JsonUtil.jsonArray().toString(),
            JsonUtil.jsonArray().toString(),
            JsonUtil.jsonArray().toString());
        System.out.println(String.format(
            "\n\n== 21 == All SRI claims at PSPC Org Book, all attrs: %s",
            JsonUtil.pprint(sriClaimsAllNode)));
        assert (sriClaimsAllNode.size() > 0);
        Map<String, JsonNode> sriDisplay = Util.claimsFor(sriClaimsAllNode.get("claims"), null);
        System.out.println(String.format(
            "\n\n== 22 == All SRI claims at PSPC Org Book by referent: %s",
            JsonUtil.pprint(sriDisplay)));

        // 21. SRI agent proxies to PSPC Org Book agent (as HolderProver) to create (multi-claim) proof
        JsonNode sriProofRespNode = Agent.SRI.getPostResponse(
            Agent.PSPC_ORG_BOOK,
            MessageType.PROOF_REQUEST,
            ProtoUtil.listSchemata(claimDataNode
                .keySet()
                .stream()
                .filter(sKey -> !sKey.equals(S_KEY.get("BC")))
                .toArray(SchemaKey[]::new)).toString(),
            JsonUtil.jsonArray().toString(),
            JsonUtil.jsonArray().toString(),
            JsonUtil.jsonArray().toString());
        System.out.println(String.format(
            "\n\n== 23 == PSPC Org Book proof response on all claims: %s",
            JsonUtil.pprint(sriProofRespNode)));
        assert (sriProofRespNode.size() > 0);
        assert (sriProofRespNode.get("proof").get("proof").get("proofs").size() == sriDisplay.size());

        // 22. SRI agent (as Verifier) verifies proof
        JsonNode sriVerificationRespNode = Agent.SRI.getPostResponse(
            null,
            MessageType.VERIFICATION_REQUEST,
            sriProofRespNode.get("proof-req").toString(),
            sriProofRespNode.get("proof").toString());
        System.out.println(String.format(
            "\n\n== 24 == SRI agent verifies proof (by empty filter) as: %s",
            JsonUtil.pprint(sriVerificationRespNode)));
        assert (sriVerificationRespNode.asBoolean());

        // 23. SRI agent proxies to PSPC Org Book agent (as HolderProver) to create (multi-claim) proof by referent
        sriProofRespNode = Agent.SRI.getPostResponse(
            Agent.PSPC_ORG_BOOK,
            MessageType.PROOF_REQUEST_BY_REFERENT,
            ProtoUtil.listSchemata(claimDataNode
                .keySet()
                .stream()
                .filter(sKey -> !sKey.equals(S_KEY.get("BC")))
                .toArray(SchemaKey[]::new)).toString(),
            JsonUtil.toJsonNode(sriDisplay.keySet().stream().collect(Collectors.toList())).toString(),
            JsonUtil.jsonArray().toString());
        System.out.println(String.format(
            "\n\n== 25 == PSPC org book proof response on referents %s: %s",
            JsonUtil.toJsonNode(sriDisplay.keySet().stream().collect(Collectors.toList())),
            JsonUtil.pprint(sriProofRespNode)));
        assert (sriProofRespNode.size() > 0);

        // 24. SRI agent (as Verifier) verifies proof
        sriVerificationRespNode = Agent.SRI.getPostResponse(
            null,
            MessageType.VERIFICATION_REQUEST,
            sriProofRespNode.get("proof-req").toString(),
            sriProofRespNode.get("proof").toString());
        System.out.println(String.format(
            "\n\n== 26 == SRI agent verifies proof on referents %s as: %s",
            JsonUtil.toJsonNode(sriDisplay.keySet().stream().collect(Collectors.toList())),
            JsonUtil.pprint(sriVerificationRespNode)));
        assert (sriVerificationRespNode.asBoolean());

        // 25. SRI agent proxies to PSPC Org Book to create multi-claim proof by ref, schemata implicit, not legalName
        requestedAttrs = (ArrayNode)JsonUtil.jsonArray();
        for (SchemaKey sKey : claimDataNode.keySet()) {
            if (sKey.equals(S_KEY.get("BC"))) {
                continue;
            }
            requestedAttrs.add(ProtoUtil.reqAttrs(
                sKey,
                JsonUtil.convertToList(schemaStore
                    .get(sKey)
                    .get("data")
                    .get("attr_names"))
                    .stream()
                    .filter(a -> !a.equals("legalName"))
                    .collect(Collectors.toList()).toArray(new String[0])));
        }
        sriProofRespNode = Agent.SRI.getPostResponse(
            Agent.PSPC_ORG_BOOK,
            MessageType.PROOF_REQUEST_BY_REFERENT,
            JsonUtil.jsonArray().toString(),
            JsonUtil.toJsonNode(sriDisplay.keySet().stream().collect(Collectors.toList())).toString(),
            requestedAttrs.toString());
        System.out.println(String.format(
            "\n\n== 27 == PSPC org book proof response, schemata implicit, referents %s, not legalName: %s",
            JsonUtil.toJsonNode(sriDisplay.keySet().stream().collect(Collectors.toList())),
            JsonUtil.pprint(sriProofRespNode)));
        assert (sriProofRespNode.size() > 0);
        Map<String, Map<String, String>> sriRevealed = Util.revealedAttrs(sriProofRespNode.get("proof"));
        System.out.println(String.format(
            "\n\n== 28 == Revealed attrs for above: %s",
            JsonUtil.pprint(sriRevealed)));
        Map<String, Integer> schemaAttrNotLegalNameCounter = new HashMap<>();
        for (SchemaKey sKey : schemaStore.keySet()) {
            if (sKey.equals(S_KEY.get("BC"))) {
                continue;
            }
            it = schemaStore.get(sKey).get("data").get("attr_names").iterator();
            while (it.hasNext()) {
                String attrName = it.next().textValue();
                if (attrName.equals("legalName")) {
                    continue;
                }
                int count = schemaAttrNotLegalNameCounter.getOrDefault(attrName, 0);
                schemaAttrNotLegalNameCounter.put(attrName, ++count);
            }
        }
        Map<String, Integer> proofAttrCounter = new HashMap<>();
        for (String referent : sriRevealed.keySet()) {
            for (String attrName : sriRevealed.get(referent).keySet()) {
                int count = proofAttrCounter.getOrDefault(attrName, 0);
                proofAttrCounter.put(attrName, ++count);
            }
        }
        assert (proofAttrCounter.equals(schemaAttrNotLegalNameCounter));

        // 26. SRI agent (as Verifier) verifies proof
        sriVerificationRespNode = Agent.SRI.getPostResponse(
            null,
            MessageType.VERIFICATION_REQUEST,
            sriProofRespNode.get("proof-req").toString(),
            sriProofRespNode.get("proof").toString());
        System.out.println(String.format(
            "\n\n== 29 == SRI agent verifies proof on referents %s as: %s",
            JsonUtil.toJsonNode(sriDisplay.keySet().stream().collect(Collectors.toList())),
            JsonUtil.pprint(sriVerificationRespNode)));
        assert (sriVerificationRespNode.asBoolean());

        // 27. SRI agent proxies to PSPC Org Book (as HolderProver) to create proof on req-attrs for green schema attrs
        sriProofRespNode = Agent.SRI.getPostResponse(
            Agent.PSPC_ORG_BOOK,
            MessageType.PROOF_REQUEST,
            JsonUtil.jsonArray().toString(),
            JsonUtil.jsonArray().toString(),
            JsonUtil.jsonArray().toString(),
            JsonUtil.toJsonNode(Arrays.asList(
                new ObjectNode[] {ProtoUtil.reqAttrs(S_KEY.get("GREEN"), new String[] {})})).toString());
        System.out.println(String.format(
            "\n\n== 30 == PSPC org book proof response to green claims response: %s",
            JsonUtil.pprint(sriProofRespNode)));
        assert (sriProofRespNode.size() > 0);
        Set<String> greenAttrs = new HashSet<>();
        Iterator<JsonNode> greenSchemaAttrs = schemaStore
            .get(S_KEY.get("GREEN"))
            .get("data")
            .get("attr_names")
            .iterator();
        while (greenSchemaAttrs.hasNext()) {
            greenAttrs.add(greenSchemaAttrs.next().textValue());
        }
        Set<String> greenProofAttrs = new HashSet<>();
        JsonNode reqAttrs = sriProofRespNode.get("proof-req").get("requested_attrs");
        attrUuids = reqAttrs.fieldNames();
        while (attrUuids.hasNext()) {
            String attrUuid = attrUuids.next();
            greenProofAttrs.add(reqAttrs.get(attrUuid).get("name").textValue());
        }
        assert (greenProofAttrs.equals(greenAttrs));

        // 28. SRI agent (as Verifier) verifies proof
        sriVerificationRespNode = Agent.SRI.getPostResponse(
            null,
            MessageType.VERIFICATION_REQUEST,
            sriProofRespNode.get("proof-req").toString(),
            sriProofRespNode.get("proof").toString());
        System.out.println(String.format(
            "\n\n== 31 == SRI agent verifies proof on [%s v%s] attrs as: %s",
            S_KEY.get("GREEN").getName(),
            S_KEY.get("GREEN").getVersion(),
            JsonUtil.pprint(sriVerificationRespNode)));
        assert (sriVerificationRespNode.asBoolean());

        // 29. Exercise helper GET TXN call
        int seqNo = schemaStore.get(S_KEY.get("BC")).get("seqNo").asInt(); // there will be a real transaction here
        JsonNode node = Agent.SRI.getGetResponse(String.format("txn/%d", seqNo));
        System.out.println(String.format(
            "\n\n== 32 == ledger transaction by #%d: %s",
            seqNo,
            JsonUtil.pprint(node)));
        assert (node.size() > 0);

        // 30. txn# non-existence case
        node = Agent.SRI.getGetResponse(String.format("txn/99999"));
        System.out.println(String.format(
            "\n\n== 33 == txn #99999: %s",
            JsonUtil.pprint(node)));
        assert (node.size() == 0);
    }
}

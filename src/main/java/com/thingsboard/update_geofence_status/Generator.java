package com.thingsboard.update_geofence_status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.id.AssetId;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;

import javax.annotation.PostConstruct;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static org.thingsboard.server.common.data.DataConstants.SERVER_SCOPE;

@Component
@RequiredArgsConstructor
@Slf4j
public class Generator {

    private final String host = "https://fms-dev.iotsquared.io"; //todo update this
    private final String email = "your_email"; //todo update this
    private final String password = "your_password"; //todo update this

    private final static ObjectMapper mapper = new ObjectMapper();
    private final HttpRequester http;


    @PostConstruct
    @SneakyThrows
    public void generate() {
        try (RestClient client = new RestClient(host)) {
            client.login(email, password);
            updateActivityStatusOnGeofence(client);
            log.info("Finished update");
        }
    }

    private List<String> getAllAssetsOfType(RestClient client, String type) throws JsonProcessingException {
        List<String> assetIds = new ArrayList<>();
        HttpResponse<String> response = http.sendGetRequest(host + "/api/tenant/assets?page=0&pageSize=200&type=" + type, client.getToken());
        if (response.statusCode() != 200) {
            log.error("Failed to get all vehicles [status :{}, response :{}]", response.statusCode(), response.body());
            return assetIds;
        }
        JsonNode pageData = mapper.readTree(response.body());
        pageData.get("data").forEach(
                assetNode -> {
                    String id = assetNode.get("id").get("id").asText();
                    assetIds.add(id);
                }
        );
        return assetIds;
    }

    private void updateActivityStatusOnGeofence(RestClient client) throws JsonProcessingException {
        List<String> ids = getAllAssetsOfType(client, "zone");
        ids.addAll(getAllAssetsOfType(client, "poi"));
        ObjectNode node = mapper.createObjectNode();
        node.put("status", "NOT_ACTIVE");
        int i = 1;
        for (String id : ids) {
            List<AttributeKvEntry> entry = client.getAttributeKvEntries(AssetId.fromString(id), List.of("status"));
            if (!entry.isEmpty() && entry.get(0).getStrValue().orElse("ACTIVE").equals("DISABLE")) {
                client.saveEntityAttributesV1(AssetId.fromString(id), SERVER_SCOPE, node);
                log.info("sat new value");
            }
            log.info("Processed {}/{}", i, ids.size());
            i++;
        }
    }
}

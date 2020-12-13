package com.soprasteria.accesscontrol.filter.accesscontroldatafilter.service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Stateless
public class DataShieldFilterDataService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataShieldFilterDataService.class);

	public JsonNode removeFilterProperties(String filterData, String filterPermissions) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode permissionNode = mapper.readTree(filterPermissions);
		JsonNode dataNode = mapper.readTree(filterData);
		JsonNode filteredDataNode = mapper.readTree(filterData);
		if (dataNode.isObject()) {
			LOGGER.debug("filtering object data by using permissions");
			filterDataByPermission(permissionNode, dataNode, filteredDataNode);
		} else if (dataNode.isArray()) {
			LOGGER.debug("filtering array data by using permissions");
			for (int i = 0; i < dataNode.size(); i++) {
				filterDataByPermission(permissionNode, dataNode.get(i), filteredDataNode.get(i));
			}
		}
		return filteredDataNode;
	}

	private void filterDataByPermission(JsonNode permissionNode, JsonNode dataNode, JsonNode filteredNode) {
		Instant start = Instant.now();
		Iterator<Map.Entry<String, JsonNode>> iter = dataNode.fields();
		while (iter.hasNext()) {
			Map.Entry<String, JsonNode> entry = iter.next();
			if (entry.getValue().isObject()) {
				filterDataByPermission(permissionNode, entry.getValue(),
						filteredNode.findValue(entry.getKey()));
			} else if (entry.getValue().isArray()) {
				for (int i = 0; i < entry.getValue().size(); i++) {
					filterDataByPermission(permissionNode, entry.getValue().get(i),
							filteredNode.findValue(entry.getKey()).get(i));
				}
			} else if (null != entry.getValue() && entry.getValue().isValueNode()) {

				JsonNode permissions = permissionNode.findValue(entry.getKey());
				if (null != filteredNode && filteredNode.isObject() && null != permissions && permissions.isObject()
						&& null != permissions.get("isRead") && !permissions.get("isRead").asBoolean()) {
					LOGGER.debug("atrribute name {} and its permission {}", entry.getKey(), permissions.get("isRead"));
					ObjectNode filteredDataNode = (ObjectNode) filteredNode;
					if (null != filteredDataNode.findParent(entry.getKey())) {
						filteredDataNode.findParent(entry.getKey()).remove(entry.getKey());
					}
				}
			}
		}
		Instant end = Instant.now();
		LOGGER.info("time taken to filter data {}", Duration.between(start, end));
	}
}

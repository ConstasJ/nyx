package com.mooltiverse.oss.nyx.services.gitea;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.util.*;

abstract class GiteaEntity {
    /**
     * The private instance of the API reference.
     */
    private final com.mooltiverse.oss.nyx.services.gitea.API api;

    /**
     * The private instance of the user attributes map.
     */
    private final Map<String, Object> attributes;

    /**
     * Creates the entity modelled by the given attributes.
     *
     * @param api the reference to the API used to communicate with the remote end. Can't be {@code null}
     * @param attributes the map of attributes for this object. Can't be {@code null}
     *
     * @throws NullPointerException if the given attributes map is {@code null}
     * @throws IllegalArgumentException if the map of attributes is empty
     */
    protected GiteaEntity(com.mooltiverse.oss.nyx.services.gitea.API api, Map<String, Object> attributes) {
        super();
        Objects.requireNonNull(api, "The API reference cannot be null");
        Objects.requireNonNull(attributes, "The map of attributes cannot be null");
        if (attributes.isEmpty())
            throw new IllegalArgumentException("Attributes can't be empty");
        this.api = api;
        this.attributes = attributes;
    }

    /**
     * Reads all the attributes of the given object and puts them in the resulting map.
     *
     * @param node the node to read the attributes from
     *
     * @return the map with all the attributes from the given node
     *
     * @throws NullPointerException if the given node is {@code null}
     */
    protected static Map<String, Object> toAttributeMap(JsonNode node) {
        Objects.requireNonNull(node, "Can't parse attributes from a null node");
        Map<String, Object> res = new Hashtable<String, Object>();

        Iterator<Map.Entry<String,JsonNode>> fieldsIterator = node.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String,JsonNode> field = fieldsIterator.next();
            // There is no way to retrieve a generic Object value so let's get them all as text
            res.put(field.getKey(), field.getValue().asText());
        }

        return res;
    }

    /**
     * Reads all the attributes of the given object immediate children and puts them in the resulting maps,
     * one map for each child.
     *
     * @param node the node to read the attributes from
     *
     * @return the maps with all the attributes from the immediate children of the given node
     *
     * @throws NullPointerException if the given node is {@code null}
     */
    protected static List<Map<String, Object>> toAttributeMaps(JsonNode node) {
        Objects.requireNonNull(node, "Can't parse attributes from a null node");
        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();

        if (JsonNodeType.ARRAY.equals(node.getNodeType())) {
            // it's a collection
            Iterator<JsonNode> nodesIterator = node.elements();
            while (nodesIterator.hasNext()) {
                JsonNode itemNode = nodesIterator.next();
                res.add(toAttributeMap(itemNode));
            }
        }
        else {
            // it's a simple node, just return one item with its attributes
            return List.<Map<String, Object>>of(toAttributeMap(node));
        }

        return res;
    }

    /**
     * Returns the internal API reference.
     *
     * @return the internal API reference.
     */
    protected com.mooltiverse.oss.nyx.services.gitea.API getAPI() {
        return api;
    }

    /**
     * Returns the map of attributes this object is built on
     *
     * @return the map of attributes this object is built on
     */
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
}

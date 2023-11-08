package com.nulli.analyzer.neoloader.config

import groovy.json.JsonSlurper

/**
 * Maps LDAP Attributes to Neo4J Node properties
 *
 * @Version 0.1
 *
 * 3Edges - November 2015
 * Created by ababeanu on 2015-11-24.
 */
class PropertyMap {

    private Map PropMappings;

    // CONSTANTS
    private static final String DEFAULT_MAPPING_FILE_NAME = "resources/propertyMaps.json";

    /**
     * Creates Mappings from LDAP Attributes to Graph Node Properties, as defined in the
     * DEFAULT mappings JSON file
     */
    PropertyMap() {
        def jsonSlurper = new JsonSlurper()
        def jsonObject = jsonSlurper.parse(new File(DEFAULT_MAPPING_FILE_NAME).toURI().toURL())
        PropMappings = (Map) jsonObject
    }

    /**
     * Creates Mappings from LDAP Attributes to Graph Node Properties, as defined in the
     * supplied mappings JSON file
     *
     * @param MapFileName The file name that contains the mappings, in JSON format
     */
    PropertyMap(String MapFileName) {
        def jsonSlurper = new JsonSlurper()
        def jsonObject = jsonSlurper.parse(new File(MapFileName).toURI().toURL());
        PropMappings = (Map) jsonObject
    }

    // ACCESSORS

    Map getPropMappings() {
        return PropMappings
    }
}

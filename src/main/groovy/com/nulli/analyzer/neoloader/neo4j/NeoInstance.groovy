package com.nulli.analyzer.neoloader.neo4j

import com.nulli.analyzer.neoloader.config.NeoConfiguration
import com.nulli.analyzer.neoloader.config.PropertyMap
import com.nulli.analyzer.neoloader.model.Entity
import groovy.util.logging.Log
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator

/**
 *
 * Represents an instance of a Neo4J Graph database.
 * Provifde an interface to Neo4J, it uses Neo4J's REST API
 * interact with the Graph Database.
 *
 * Nulli Secundus Inc. - November 2015
 * Created by ababeanu on 2015-11-23.
 */
@Log
class NeoInstance {

    private NeoConfiguration config;
    private String restBaseURL;
    private Map mappings;

    /**
     * Constructor.
     * Instantiates a new NeoInstance object from the given config
     *
     * @param cfg the NeoConfiguration to use for Graph DB
     */
    NeoInstance (NeoConfiguration cfg) {
        this.config = cfg
        this.restBaseURL = "http://" + cfg.getHost() + ":" + cfg.getNeoPort()
        // Use default LDAP to Neo attribute mappings
        this.mappings = new PropertyMap().getPropMappings()
    }

    /**
     * Constructor.
     * Instantiates a new NeoInstance object from the given config
     *
     * @param cfg the NeoConfiguration to use for Graph DB
     * @param maps a Map representation of JSON LDAP to Neo Property mappings
     */
    NeoInstance (NeoConfiguration cfg, Map maps) {
        this.config = cfg
        this.restBaseURL = "http://" + cfg.getHost() + ":" + cfg.getNeoPort() + "/db/data"
        // Use supplied LDAP to Neo attribute mappings
        this.mappings = maps
    }

    /**
     * Creates a Neo4J Node for the given Entity.
     * The method uses MERGE on the DN Property, which
     * serves as unique identifier; i.e., it will create a
     * new Node only if a Node with the same DN doesn't exist
     * already.
     *
     * @param entity A NeoLoader Entity:  instance of an LDAP Entity
     * @return TRUE if the creation succeeded, FALSE otherwise
     */
    boolean createNode (Entity entity) {

        def success = false

        log.fine "------------ createNode - Entering. "

        def crCypher = '"MERGE (n:' + entity.getEntityType() + ' ' + buildJsonProps(entity) + ') RETURN id(n)"'
        log.fine "createNode - Cypher statement = ${crCypher}"

        // Create Node through REST
        def client = new RESTClient( restBaseURL  )
        client.auth.basic config.getUser(), config.getPassword()
        def resp = (HttpResponseDecorator) client.post(
                path : '/db/data/transaction/commit',
                requestContentType : ContentType.JSON,
                headers: ["Accept": "application/json; charset=UTF-8","Authorization": config.getAuthorization()],
                body : '{"statements" : [ { "statement": ' + crCypher + '} ]}'
        )

        if (resp.getStatus() == 200) {
            log.fine ("createNode - New node succesfully created.")
            success = true
        } else {
            log.severe ("createNode - New node creation failure : " + String.valueOf(resp.status))
            System.out.println ("createNode - New node creation failure : " + String.valueOf(resp.status))
        }

        log.fine "------------ createNode - Exiting. "

        return success
    }

    /**
     * Creates a Neo4J Relationship between the 2 given Entities,
     * identified by their DN. The relationship is assigned the given properties.
     *
     * @param entity1
     * @param entity2
     * @param props A Map of Key=Value properties to assign to the new relationship
     * @return
     */
    boolean createRelationship (Entity entity1, Entity entity2, Map props) {

        // TODO

        return true;
    }

    /**
     * Translates a Map of LDAP Attributes to a JSON String of Neo4J Node properties
     *
     * @param ent an Entity encapsulating the node to create in Neo4J
     * @return a JSON String with the Name=Value pairs of the mapped attributes.
     * All non-mapped attributes are ignored.
     */
    String buildJsonProps (Entity ent) {

        log.info "BuildJsonProps - Entering. "

        // Initializations
        def jsonProps = "{ "
        def attributes = ent.getAttributes()
        /* DEBUG: *
        for (String k: attributes.keySet()) {
            log.info "BuildJsonProps - Attrib key: ${k}, val = ${attributes.get(k)}"
        }
        /* */

        // Get the Attributes to Property mappings for the current Entity
        def entTp = ent.getEntityType()
        log.info "BuildJsonProps - Processing ${entTp}. "
        /* DEBUG:
        for (String k: mappings.keySet()) {
            log.info "BuildJsonProps - mapping key: ${k}"
        }
        */
        def entMappings = (Map) mappings.get(entTp);
        /* DEBUG: *
        for (String k: entMappings.keySet()) {
            log.info "BuildJsonProps - Mapping Key: ${k}, val = ${entMappings.get(k)}"
        }
        /* */

        // Loop through attributes and build properties JSON: Nb of attributes
        def nbMappings = entMappings.size()
        def cnt = 0

        // Process Attributes
        entMappings.each {name, value ->
            cnt++
            if (value == "dn") {
                // Special case for DN: it's not an array
                String dn = attributes.get(value)
                jsonProps += name + ":'" + dn + "'"
            } else {
                // We only take the 1st value of Mutli-Valued LDAP Attributes
                String[] attrAry = attributes.get(value)
                jsonProps += name + ":'" + attrAry[0] + "'"
            }
            if (cnt < nbMappings)
                jsonProps += ","
        }

        jsonProps += " }"

        log.info "BuildJsonProps - JSON props = ${jsonProps} . "

        return jsonProps
    }




}

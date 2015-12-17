package com.nulli.analyzer.neoloader.neo4j

import com.nulli.analyzer.neoloader.config.NeoConfiguration
import com.nulli.analyzer.neoloader.config.PropertyMap
import com.nulli.analyzer.neoloader.connector.ConnectorEntities
import com.nulli.analyzer.neoloader.model.Entity
import groovy.util.logging.Log
import groovyx.net.http.RESTClient
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovy.json.JsonSlurper

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

    private NeoConfiguration config
    private String restBaseURL
    private Map mappings

    /**
     * Constructor.
     * Instantiates a new NeoInstance object from the given config
     *
     * @param cfg the NeoConfiguration to use for Graph DB
     * @param attrMap A Map of attribute mappings between LDAP and Neo
     */
    NeoInstance (NeoConfiguration cfg, Map attrMap) {
        this.config = cfg
        this.restBaseURL = "http://" + cfg.getHost() + ":" + cfg.getNeoPort()
        this.mappings = attrMap
    }

    /**
     * Creates a Neo4J Node for the given Entity.
     * The method uses MERGE on the DN Property, which
     * serves as unique identifier; i.e., it will create a
     * new Node only if a Node with the same DN doesn't exist
     * already.
     *
     * @param entity A NeoLoader Entity:  instance of an LDAP Entity
     * @return A String: the ID of the newly created node, or "" if the creation failed
     */
    String createNode (Entity entity) {

        def id = ""

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

        if (resp.getStatus() < 400) {
            id = extractNodeID((String) resp.getData())
            log.fine ("createNode - New node ${id} succesfully created.")
        } else {
            log.severe ("createNode - New node creation failure : " + String.valueOf(resp.status))
            System.out.println ("createNode - New node creation failure : " + String.valueOf(resp.status))
        }

        log.fine "------------ createNode - Exiting. "

        return id
    }

    /**
     * Creates a Neo4J Relationship between the 2 given Entities,
     * identified by their DN. The relationship is assigned the given properties.
     *
     * @param Id1 a String representing the Neo4j ID of the node that is the origin of the relationship
     * @param Id2 a String representing the Neo4j ID of the node that is the target of the relationship
     * @param RelType A String: the type of the relationship. Becomes the Neo4J Label of the relationship.
     * @param props A Map of Key=Value properties to assign to the new relationship
     * @return
     */
    boolean createRelationship (String Id1, String Id2, String RelType, Map props) {

        log.fine "------------ createRelationship : ${Id1} -> ${Id2} : Entering..."

        // Inits
        boolean success = false
        // Target Node URL
        def target = restBaseURL + '/db/data/node/' + Id2
        // REST request Body:
        def body = '{"to" : "' + target + '",' + ' "type" : ' + '"' + RelType + '"'
        if ((props) && (props.size() > 0)) {
            // If rel. properties are supplied, add them to the body
            body += ', ' + buildRelJsonProps(props)
        }
        body += ' }'

        // Create Relationship through REST
        def client = new RESTClient( restBaseURL  )
        client.auth.basic config.getUser(), config.getPassword()
        def resp = (HttpResponseDecorator) client.post(
                path : '/db/data/node/' + Id1 + '/relationships',
                requestContentType : ContentType.JSON,
                headers: ["Accept": "application/json; charset=UTF-8","Authorization": config.getAuthorization()],
                body : body
        )

        if (resp.getStatus() < 400) {
            log.fine ("createRelationship - New relationship succesfully created.")
            success = true
        } else {
            log.severe ("createRelationship - New Rel. creation failure : " + String.valueOf(resp.status))
            System.out.println ("createRelationship - New Rel. creation failure : " + String.valueOf(resp.status))
        }

        log.fine "------------ createRelationship - Exiting. "

        return success;
    }

    /**
     * Translates a Map of LDAP Attributes to a JSON String of Neo4J Node properties
     *
     * @param ent an Entity encapsulating the node to create in Neo4J
     * @return a JSON String with the Name=Value pairs of the mapped attributes.
     * All non-mapped attributes are ignored.
     */
    String buildJsonProps (Entity ent) {

        log.fine "BuildJsonProps - Entering. "

        // Initializations
        def jsonProps = "{ "
        def attributes = ent.getAttributes()
        /* DEBUG: *
        for (String k: attributes.keySet()) {
            log.info "BuildJsonProps - Attrib key: ${k}, val = ${attributes.get(k)}"
        }
        /* */

        def entTp = ent.getEntityType()
        log.fine "BuildJsonProps - Processing ${entTp}. "

        // Inits
        def entMappings = (Map) mappings.get(entTp); // Get mappings for current entity
        def nbAttrs = attributes.size()
        def cnt = 0

        // Process Attributes
        attributes.each {name, value ->
            cnt++
            def String val = value[0]
            // Use the Mapped propertty names in Neo4J:
            jsonProps += (name == "dn") ? entMappings.get(name) + ":'" + value + "'" : entMappings.get(name) + ":'" + val.replaceAll(/\'/, ' ') + "'"
            if (cnt < nbAttrs)
                jsonProps += ","
        }

        jsonProps += " }"

        log.fine "BuildJsonProps - JSON props = ${jsonProps} . "

        return jsonProps
    }

    /**
     * Builds a JSON string of Neo4F Relationship Properties from a given Map.
     * Example output:
     "data" : {
        "foo" : "bar"
     }
     *
     * @param props A Map of Key=Value properties
     * @return a JSON String
     */
    String buildRelJsonProps (Map props) {

        log.fine "buildRelJsonProps - Entering. "

        // Inits
        def jsonRelProps = ''
        def nbProps = props.size()

        if (nbProps > 0) {
            // There are properties to process
            def cnt = 0
            jsonRelProps += '"data" : { '
            props.each { name, value ->
                cnt++
                jsonRelProps += '"' + name + '" : "' + value + '"'
                if (cnt < nbProps)
                    jsonRelProps += ","
            }

            jsonRelProps += ' }'
        }

        log.fine "buildRelJsonProps - JSON REL props = ${jsonRelProps} . "
        return jsonRelProps
    }

    // PRIVATE

    /**
     * Extracts the ID from a Neo4J Node creation Rest request
     * Sample Response:
     *
     {errors=[], results=[{columns=[id(n)], data=[{row=[8626]}]}]}
     *
     *
     * @param Response The Response string from the Neo4J Server, pre-parsed
     * @return the new Node iD
     */
    private String extractNodeID (String resp) {

        log.fine "exctractNodeID - Response = ${resp}"

        def str = resp.substring(resp.indexOf("row") + 5)
        def id = str.substring(0,str.indexOf("]"))

        log.fine "exctractNodeID - ID = ${id}"

        return id
    }




}

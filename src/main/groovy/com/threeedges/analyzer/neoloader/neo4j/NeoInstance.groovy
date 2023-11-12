package com.threeedges.analyzer.neoloader.neo4j

import com.threeedges.analyzer.neoloader.config.NeoConfiguration
import com.threeedges.analyzer.neoloader.model.Entity
import groovy.util.logging.Log
// Neo4J
import org.neo4j.driver.*;

/**
 *
 * Represents an instance of a Neo4J Graph database.
 * Provifde an interface to Neo4J, it uses Neo4J's REST API
 * interact with the Graph Database.
 *
 * 3Edges - November 2015
 * Created by ababeanu on 2015-11-23.
 */
@Log
class NeoInstance {

    private NeoConfiguration config
    private String neoURI
    private Map mappings
    private Driver dbDriver;
    private SessionConfig sessionConfig;

    /**
     * Constructor.
     * Instantiates a new NeoInstance object from the given config
     *
     * @param cfg the NeoConfiguration to use for Graph DB
     * @param attrMap A Map of attribute mappings between LDAP and Neo
     */
    NeoInstance (NeoConfiguration cfg, Map attrMap) {
        this.config = cfg
        //old: this.neoURI = "neo4j+s://${cfg.getHost()}}/db/${cfg.getDatabase()}"
        this.neoURI = "${cfg.getNeoScheme()}://${cfg.getHost()}:${cfg.getNeoPort()}"
        this.dbDriver = GraphDatabase.driver(neoURI, AuthTokens.basic(config.getUser(), config.getPassword()));
        this.mappings = attrMap
        // Set Driver database config
        var builder = SessionConfig.builder();
        if (config.getDatabase() != null && !config.getDatabase().isBlank()) {
            builder.withDatabase(config.getDatabase());
        }
        this.sessionConfig = builder.build();
    }

    /**
     * Creates a Neo4J Node for the given Entity.
     * The method uses MERGE on the DN Property, which
     * serves as unique identifier; i.e., it will create a
     * new Node only if a Node with the same DN doesn't exist
     * already.
     *
     * @param entity A NeoLoader Entity:  instance of an LDAP Entity
     * @return An integer: the ID of the newly created node, or -1 if the creation failed
     */
    Integer createNode (Entity entity) {

        // DEBUG:
        //log.info("\nUsing Neo4J database: ${this.neoURI}/${config.getDatabase()}\n")

        // Merge Cypher query:
        def id = 0
        log.fine "------------ createNode - Entering. "
        String crCypher = 'CREATE (n:' + entity.getEntityType() + ' ' + buildJsonProps(entity) + ') RETURN id(n)'
        log.fine "createNode - Cypher statement = ${crCypher}"

        // Create Node through BOLT
        try (Session session = dbDriver.session(sessionConfig))
        {
            // Wrapping a Cypher Query in a Managed Transaction for writes to handle connection
            // problems and transient errors using an automatic retry mechanism.
            id = session.executeWrite(tx -> {
                var query = new Query(crCypher)
                var result = tx.run(query)
                /* DEBUG - log results : *
                result.list().getFirst().asMap().forEach {k, v -> log.info "${k}:${v}"}
                /* */
                return result.single().get(0).asInt()
            });
        } catch (Exception e ) {
            log.severe ("createNode - New node creation failure, Cause: " + e.toString())
            return -1
        } finally {
        }
        log.info ("createNode - New node ${id} succesfully created.")
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
    boolean createRelationship (Integer Id1, Integer Id2, String RelType, Map props) {

        log.fine "------------ createRelationship : ${Id1} -> ${Id2} : Entering..."

        // Create Rel cypher:
        String crCypher = "MATCH (source) where ID(source)=${Id1} MATCH (target) WHERE ID(target)=${Id2} " +
                "CREATE (source)-[r:${RelType} ${buildRelJsonProps(props)}]->(target) return id(r)"

        log.fine "createNode - Cypher statement = ${crCypher}"

        // Create Node through BOLT
        def id = 0
        try (Session session = dbDriver.session(sessionConfig))
        {
            // Wrapping a Cypher Query in a Managed Transaction for writes to handle connection
            // problems and transient errors using an automatic retry mechanism.
            id = session.executeWrite(tx -> {
                var query = new Query(crCypher)
                var result = tx.run(query)
                return result.single().get(0).asInt()
            })
        } catch (Exception e ) {
            log.severe ("createRelationship - New rel creation failure, Cause : " + e.toString())
            return false
        }
        log.info ("createRelationship - New relationship ${id} succesfully created.")
        log.fine "------------ createRelationship - Exiting. "
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

        log.fine "BuildJsonProps - Entering. "

        // Generate random UUID
        def nodeID = UUID.randomUUID().toString()

        // Initializations
        def jsonProps = "{ ID: '${nodeID}',"
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
            // Use the Mapped property names in Neo4J:
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
            jsonRelProps += '{ '
            props.each { name, value ->
                cnt++
                jsonRelProps += name + ' : "' + value + '"'
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

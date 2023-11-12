package com.threeedges.analyzer.neoloader

import com.threeedges.analyzer.neoloader.config.LdapConfiguration
import com.threeedges.analyzer.neoloader.config.NeoConfiguration
import com.threeedges.analyzer.neoloader.config.PropertyMap
import com.threeedges.analyzer.neoloader.connector.ConnectorEntities
import com.threeedges.analyzer.neoloader.connector.ldap.LdapFacade
import com.threeedges.analyzer.neoloader.model.Person
import com.threeedges.analyzer.neoloader.model.Group
import com.threeedges.analyzer.neoloader.neo4j.NeoInstance
import com.threeedges.analyzer.neoloader.neo4j.RelationshipType
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.LDAPSearchException
import groovy.util.logging.Log

/**
 * Orchestrates the Loading of the LDAP Entries to Neo4j.
 * - Reads Users and Groups from LDAP
 * - Created corresponding vertices and edges in the graph.
 *
 * 3Edges - March 2015
 * Created by ababeanu on 2015-03-11.
 */
@Log
class LoaderProcessor {

    private LdapConfiguration config
    private NeoInstance neoServer
    private LdapFacade ldap
    private Map mappings
    private HashMap<String,String> EntityDN2ID
    private ArrayList<Group> groups
    
    /**
     * Constructor.
     * Instantiates a new Processor with the given Configuration. Creates a Connection Pool
     * to the configured LDAP Directory.
     *
     * @param cfg the LdapConfiguration to use for the searches
     * @param neoCfg the Neo4J COnfiguration to use for the writes
     */
    LoaderProcessor (LdapConfiguration cfg, NeoConfiguration neoCfg) throws LDAPException {
        this.config = cfg;
        this.ldap = new LdapFacade(cfg)
        this.mappings = new PropertyMap().getPropMappings()
        this.EntityDN2ID = new HashMap<String,String> ()
        this.neoServer = new NeoInstance(neoCfg, this.mappings)
    }

    /**
     * Orchestrates the Neo4J loads: triggers LDAP reads and Neo Writes for all supported Entities.
     */
    void  orchestrateLoad () {

        // Timer
        def before = System.currentTimeMillis()

        // 1st load users
        processUsers()

        // Then load groups
        // This also populates the 'groups' private property
        processGroups()

        // DEBUG
        /* */
        EntityDN2ID.each { k,v ->
            log.fine "Key= ${k}, V=${v}"
        }
        /* */

        // The create Group Relationships to Groups and Persons
        processGroupRels()

        // Release memory
        groups = new ArrayList<Group>()
        EntityDN2ID = new HashMap<String,String> ()

        // Timer
        def after = System.currentTimeMillis()
        def delta = after - before
        def deltaSecs = delta/1000
        def deltaMin = deltaSecs/60

        log.info "Processing time: ${delta} ms = ${deltaSecs} s = ${deltaMin} m."
    }

    /**
     * Creates all group relationships using the LDAP Search results fetched in prior tasks.
     */
    private processGroupRels () {

        log.info "**** processGroupRels - Entering: creating Group relationships..."

        def relCnt = 0
        groups.each { g ->
            // Inits
            def String[] members = g.getMembers()
            def gDN = g.getDn()

            // Create MEMBER_OF Relationship from each member
            members.each {m ->

                log.fine "processGroupRels - Member = ${m}."

                // Get the Neo4J IDs of the corresponding nodes
                def Integer memberID = EntityDN2ID.get(m.toLowerCase())
                log.fine "processGroupRels - Member ID = ${memberID}."
                def Integer groupID = EntityDN2ID.get(gDN.toLowerCase())
                log.fine "processGroupRels - Group ID = ${groupID}."

                // Create relationship if both nodes are found
                if ((memberID) && (memberID != "") && (groupID) && (groupID != "")) {
                    if (neoServer.createRelationship(memberID, groupID,
                            RelationshipType.MEMBER_OF.name(), new HashMap())) {
                        log.fine "processGroupRels - Successfully created Member Rel: ${memberID}->${groupID}"
                        relCnt++
                    } else {
                        log.warning "processGroupRels - Failed to create Rel: ${memberID}->${groupID}"
                        System.out.println("processGroupRels - Failed to create Rel: ${memberID}->${groupID}")
                    }
                } else {
                    log.warning "Could not create Member relationship ${memberID} -> ${groupID}"
                    log.info "member DN = ${m}"
                    log.info "Group DN = ${gDN}"
                }
            }

            // Create PART_OF relationships: group hierarchy
            def String parentDN = g.getParent()
            log.fine "Parent DN = ${parentDN}"

            if ((parentDN) && (parentDN != '')) {
                // A Parent exists
                log.fine "processGroupRels - Found Parent of ${gDN} : ${parentDN}"

                // Get the Neo4J IDs of the corresponding nodes
                def String g1 = EntityDN2ID.get(gDN.toLowerCase())
                log.fine "processGroupRels - g1 ID = ${g1}"
                def String parent = EntityDN2ID.get(parentDN.toLowerCase())
                log.fine "processGroupRels - parent ID = ${parent}"

                // The Group has a parent
                // Create relationship if both nodes are found
                if ((g1) && (g1 != "") && (parent) && (parent != "")) {
                    if (neoServer.createRelationship(g1, parent,
                            RelationshipType.PART_OF.name(), new HashMap())) {
                        log.fine "processGroupRels - Successfully created Parent Rel: ${g1}->${parent}"
                        relCnt++
                    } else {
                        log.warning "processGroupRels - Failed to create Rel: ${g1}->${parent}"
                        System.out.println("processGroupRels - Failed to create Rel: ${g1}->${parent}")

                    }
                } else {
                    log.fine "Group ${g1} has no parent (${parent})."
                }
            }
        }

        log.info "**** processGroupRels - Exiting: created ${relCnt} Group relationships."
        System.out.println("processGroupRels - Created ${relCnt} Group relationships.")

    }

    /**
     * Searches LDAP for User Accounts and creates corresponding Vertices in the Graph D
     * TODO: Exception handling: catch LDAPSearchException
     */
    private processUsers () throws LDAPSearchException {

        log.fine "processUsers - Entering."

        // Determine search attributes based on mappings
        def entMappings = (Map) mappings.get(ConnectorEntities.Person.name());

        // Search users
        def ArrayList<Person> users = this.ldap.search(ConnectorEntities.Person, new ArrayList(entMappings.keySet()))

        // Process users: Create Neo4J Nodes
        log.fine "processUsers -- Calling Neo Create..."
        users.each { u ->
            def dn = u.getDn()
            // Create Node
            def newNodeID = neoServer.createNode(u)
            log.fine "processUsers -- New Node ID = ${newNodeID}."

            // Handle result: store new node id
            if ((newNodeID) && (newNodeID >= 0)) {
                // New Node
                log.fine "processUsers - Sucessfully created Neo Node ${newNodeID} for user ${dn}"
                EntityDN2ID.put(dn.toLowerCase(),newNodeID)
            } else {
                log.severe "processUsers - Failed to create Neo Node for user ${dn} !"
                // System.out.println("processUsers - Failed to create Neo Node for user: " + dn)
            }
        }

        log.info "processUsers - User processing complete. Processed ${String.valueOf(users.size())} Users."
        // System.out.println ("processUsers - User processing complete. Processed Users." + String.valueOf(users.size()))

    }

    /**
     * Searches LDAP for User Groups and creates corresponding Vertices in the Graph, as well as Relationships
     * from the group to all User members or Group parent
     *
     * @param searchDn A String: the LDAP Search Base to search Groups from.
     */
    private processGroups () throws LDAPSearchException {

        log.info "processGroups - Entering."

        // Determine search attributes based on mappings
        def entMappings = (Map) mappings.get(ConnectorEntities.Group.name());
        // Fetch Group members in all cases
        entMappings.put(config.getMemberAttribute(),config.getMemberAttribute())

        // Search Groups
        groups = ldap.search(ConnectorEntities.Group, entMappings.values())

        // Process Group: Create Neo4J Nodes
        log.fine "processGroups -- Calling Neo Create..."
        groups.each { g ->
            def dn = g.getDn()
            log.fine "processGroups -- Node dn = ${dn} - parent = ${g.getParent()}."
            // Create new Node
            def newNodeID = neoServer.createNode(g)
            log.fine "processGroups -- New Node ID = ${newNodeID}."
            // Handle result: store new node id
            if ((newNodeID) && (newNodeID >= 0)) {
                log.fine "processGroups - Sucessfully created Neo Node for group ${dn}"
                EntityDN2ID.put(dn.toLowerCase(),newNodeID)
            } else {
                log.severe "processGroups - Failed to create Neo Node for group ${dn} !"
                System.out.println("processGroups - Failed to create Neo Node for group: " + dn)
            }
        }

        log.info "processGroups - User processing complete. Processed ${String.valueOf(groups.size())} groups."
        // System.out.println ("processGroups - User processing complete. Processed Groups." + String.valueOf(groups.size()))

    }

}

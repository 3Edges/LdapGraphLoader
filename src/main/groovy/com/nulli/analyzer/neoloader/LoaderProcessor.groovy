package com.nulli.analyzer.neoloader

import com.nulli.analyzer.neoloader.config.LdapConfiguration
import com.nulli.analyzer.neoloader.config.NeoConfiguration
import com.nulli.analyzer.neoloader.connector.ConnectorEntities
import com.nulli.analyzer.neoloader.connector.ldap.LdapFacade
import com.nulli.analyzer.neoloader.model.Person
import com.nulli.analyzer.neoloader.model.Group
import com.nulli.analyzer.neoloader.neo4j.NeoInstance
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.LDAPSearchException
import groovy.util.logging.Log

/**
 * Orchestrates the Loading of the LDAP Entries to Neo4j.
 * - Reads Users and Groups from LDAP
 * - Created corresponding vertices and edges in the graph.
 *
 * Nulli Secundus Inc. - March 2015
 * Created by ababeanu on 15-03-11.
 */
@Log
class LoaderProcessor {

    private LdapConfiguration config;
    private NeoInstance neoServer;
    private LdapFacade ldap;
    private HashMap<String,String> EntityDN2ID;
    
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
        this.neoServer = new NeoInstance(neoCfg)
        this.ldap = new LdapFacade(cfg)
    }

    /**
     * Orchestrates the Neo4J loads: triggers LDAP reads and Neo Writes for all supported Entities.
     */
    void  orchestrateLoad () {
        // 1st load users
        processUsers()

        // Then load groups
        // Start by 1st level Groups
        processGroups()

    }

    /**
     * Searches LDAP for User Accounts and creates corresponding Vertices in the Graph D
     * TODO: Exception handling: catch LDAPSearchException
     */
    private processUsers () throws LDAPSearchException {

        log.info "processUsers - Entering."

        // Search users
        def ArrayList<Person> users = this.ldap.search(ConnectorEntities.Person)

        // Process users: Create Neo4J Nodes
        log.fine "processUsers -- Calling Neo Create..."
        for (Person u : users) {
            // Create Node
            if (neoServer.createNode(u)) {
                log.fine "processUsers - Sucessfully created Neo Node for user ${u.getDn()}"
            } else {
                log.severe "processUsers - Failed to create Neo Node for user ${u.getDn()} !"
                System.out.println("processUsers - Failed to create Neo Node for user: " + u.getDn())
            }
        }

        log.info "processUsers - User processing complete. Processed ${String.valueOf(users.size())} Users."
        System.out.println ("processUsers - User processing complete. Processed Users." + String.valueOf(users.size()))

    }

    /**
     * Searches LDAP for User Groups and creates corresponding Vertices in the Graph, as well as Relationships
     * from the group to all User members or Group parent
     *
     * @param searchDn A String: the LDAP Search Base to search Groups from.
     */
    private processGroups () throws LDAPSearchException {

        log.info "processGroups - Entering."

        // Search Groups
        def ArrayList<Group> groups = this.ldap.search(ConnectorEntities.Group)

        // Process Group: Create Neo4J Nodes
        log.fine "processGroups -- Calling Neo Create..."
        for (Group g : groups) {
            // Create Node
            if (neoServer.createNode(g)) {
                log.fine "processGroups - Sucessfully created Neo Node for group ${g.getDn()}"
            } else {
                log.severe "processGroups - Failed to create Neo Node for group ${g.getDn()} !"
                System.out.println("processGroups - Failed to create Neo Node for group: " + g.getDn())
            }
        }

        log.info "processGroups - User processing complete. Processed ${String.valueOf(groups.size())} groups."
        System.out.println ("processGroups - User processing complete. Processed Groups." + String.valueOf(groups.size()))

    }

}

package com.nulli.analyzer.neoloader

import com.nulli.analyzer.neoloader.config.LdapConfiguration
import com.nulli.analyzer.neoloader.config.NeoConfiguration
import com.nulli.analyzer.neoloader.ldap.ConnectionPool
import com.nulli.analyzer.neoloader.model.User
import com.nulli.analyzer.neoloader.neo4j.NeoInstance
import com.unboundid.ldap.sdk.Entry
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.SearchResult
import com.unboundid.ldap.sdk.SearchResultEntry
import com.unboundid.ldap.sdk.SearchScope
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.Attribute;

import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl;
import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.util.LDAPTestUtils
import groovy.util.logging.Log
import org.codehaus.groovy.runtime.StackTraceUtils;

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
    private LDAPConnectionPool pool;
    private NeoInstance neoServer;

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
        this.pool = new ConnectionPool(cfg).getPool();
        this.neoServer = new NeoInstance(neoCfg)
    }

    /**
     * Searches LDAP for User Accounts and creates corresponding Vertices in the Graph DB
     * TODO: make separate method to build Search Filters for each mapped Entity.
     *
     * @param searchDn A String: the LDAP Search Base to search users from.
     */
    public processUsers () throws LDAPSearchException {

        log.info "processUsers - Entering."

        // Init
        int numSearches = 0;
        int totalEntriesReturned = 0;
        String searchDn = config.getBaseDN();


        // Perform a search to retrieve all users in the server, but only retrieving
        // the configured PageSize at a time.

        // Search Filter for USERS
        // TODO: Refactor LDAP Search as a separate method. Consider connector.
        log.info "processUsers -- Processing Users ..."
        System.out.println ("Processing LDAP Users...")
        SearchRequest searchRequest = new SearchRequest(searchDn,
                SearchScope.SUB, Filter.createEqualityFilter("objectClass", config.getUserObjClass()));

        // Pagination Cookie - keeps track of where the Search is at
        ASN1OctetString resumeCookie = null;
        while (true)
        {
            searchRequest.setControls(
                    new SimplePagedResultsControl(config.getPageSize(), resumeCookie));

            // Perform LDAP Search
            SearchResult searchResult;
            try {
                searchResult = this.pool.search(searchRequest);
            } catch (LDAPSearchException e) {
                log.severe "Failed to search LDAP with the configured filter."
                log.severe  StackTraceUtils.sanitize(e).toString()
            }

            // Count Results
            numSearches++;
            totalEntriesReturned += searchResult.getEntryCount();
            log.info "processUsers - Found ${totalEntriesReturned} LDAP Entries in Paged Search Nb ${numSearches}."

            log.info "processUsers - Creating Neo4J Nodes..."
            for (SearchResultEntry e : searchResult.getSearchEntries())
            {
                // Create Neo nodes for each found entry
                log.fine "processUsers - Found Entry = ${e.getDN()} . "
//                System.out.println ("Found DN = " + e.getDN());
//                System.out.println ("-- " + e.getAttribute("uid").getName() +" = " + e.getAttribute("uid").getValue());
                // result.getDN(), "uid: " + result.getAttribute("uid").getValue()

                // Instantiating User Entiry object
                User u = new User()
                u.setDn(e.getDN())

                def attribs = [:]
                def attrIt = e.getAttributes().iterator()
                while (attrIt.hasNext()) {
                    Attribute a = (Attribute) attrIt.next()
                    attribs.put(a.getName(), a.getValues())
                }
                u.setAttributes(attribs)
                log.fine "processUsers -- Calling Neo Create..."

                // Create Node
                if (neoServer.createNode(u)) {
                    log.info "processUsers - Sucessfully created Neo Node for user ${e.getDN()}"
                } else {
                    log.severe "processUsers - Failed to create Neo Node for user ${e.getDN()} !"
                    System.out.println("processUsers - Failed to create Neo Node for user: " + e.getDN())
                }
            }

            // Get results control to determine if there are more rows on the server.
            // Asserts whether the result is paginated
            LDAPTestUtils.assertHasControl(searchResult, SimplePagedResultsControl.PAGED_RESULTS_OID);
            // Gets the results control
            SimplePagedResultsControl responseControl = SimplePagedResultsControl.get(searchResult);
            // Check if more are expected
            if (responseControl.moreResultsToReturn())
            {
                // The resume cookie can be included in the simple paged results
                // control included in the next search to get the next page of results.
                resumeCookie = responseControl.getCookie();
            }
            else
            {
                break;
            }
        }

        log.info "processUsers - User processing complete. Processed ${totalEntriesReturned} Users."
        System.out.println ("processUsers - User processing complete. Processed Users." + String.valueOf(totalEntriesReturned))

    }
}

package com.nulli.analyzer.neoloader

import com.nulli.analyzer.neoloader.config.LdapConfiguration
import com.nulli.analyzer.neoloader.ldap.ConnectionPool
import com.nulli.analyzer.neoloader.model.User

import com.unboundid.ldap.sdk.Entry
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.SearchResult
import com.unboundid.ldap.sdk.SearchResultEntry
import com.unboundid.ldap.sdk.SearchScope
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPSearchException;

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

    /**
     * Constructor.
     * Instantiates a new Processor with the given Configuration. Creates a Connection Pool
     * to the configured LDAP Directory.
     *
     * @param cfg the LdapConfiguration to use for the searches
     */
    LoaderProcessor (LdapConfiguration cfg) throws LDAPException {
        this.config = cfg;
        this.pool = new ConnectionPool(cfg).getPool();

    }

    /**
     * Searches LDAP for User Accounts and creates corresponding Vertices in the Graph DB
     *
     * @param searchDn A String: the LDAP Search Base to search users from.
     */
    public processUsers () throws LDAPSearchException {

        // Init
        int numSearches = 0;
        int totalEntriesReturned = 0;
        String searchDn = config.getBaseDN();


        // Perform a search to retrieve all users in the server, but only retrieving
        // the configured PageSize at a time.

        // Search Filter
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

            for (SearchResultEntry e : searchResult.getSearchEntries())
            {
                // TODO: Do something with each entry...
                System.out.println ("Found DN = " + e.getDN());
                System.out.println ("-- " + e.getAttribute("uid").getName() +" = " + e.getAttribute("uid").getValue());
                // result.getDN(), "uid: " + result.getAttribute("uid").getValue()
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

    }
}

package com.nulli.analyzer.neoloader.connector.ldap

import com.nulli.analyzer.neoloader.config.LdapConfiguration
import com.nulli.analyzer.neoloader.connector.ConnectorEntities
import com.nulli.analyzer.neoloader.connector.ConnectorFacade
import com.nulli.analyzer.neoloader.ldap.ConnectionPool
import com.nulli.analyzer.neoloader.model.Entity
import com.nulli.analyzer.neoloader.model.Person
import com.nulli.analyzer.neoloader.model.Group
import com.unboundid.asn1.ASN1OctetString
import com.unboundid.ldap.sdk.SearchResult
import com.unboundid.ldap.sdk.SearchResultEntry
import com.unboundid.ldap.sdk.SearchScope
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl
import com.unboundid.util.LDAPTestUtils
import org.codehaus.groovy.runtime.StackTraceUtils;
import groovy.util.logging.Log

/**
 * Interfaces with a given LDAP directory, as defined in the supplied configuration.
 *
 * Created by ababeanu on 2015-11-30.
 */
@Log
class LdapFacade implements ConnectorFacade {

    private LdapConfiguration config
    private LDAPConnectionPool pool

    /**
     * Constructor.
     *
     * @param cfg an LdapConfiguration object encapsulating the connectivity params for the source LDAP Dir.
     */
    LdapFacade (LdapConfiguration cfg) {
        config = cfg;
        pool = new ConnectionPool(cfg).getPool();
    }

    /**
     * Search Entities
     *
     * @param EntityType A ConnectorEntities value representing the type of entity to search for.
     * @return An ArrayList of Entity objects.
     */
    ArrayList<Entity> search (ConnectorEntities EntityType) {

        log.info "LDAP Search - Entering."

        // Init
        int numSearches = 0;
        int totalEntriesReturned = 0;
        ArrayList<Entity> Results = new ArrayList<Entity> ()

        // Build Search filter.
        SearchRequest searchRequest = buildSearchFilter (EntityType);

        log.info "Ldap Search -- Processing ${EntityType}s..."
        System.out.println ("Processing LDAP ${EntityType}s...")

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
            log.info "Ldap Search - Found ${totalEntriesReturned} LDAP Entries in Paged Search Nb ${numSearches}."

            for (SearchResultEntry e : searchResult.getSearchEntries())
            {
                log.fine "Ldap Search - Found Entry = ${e.getDN()} . "
                // Instantiating  Entity object
                Entity u = createEntity(EntityType, e)
                // Store in results Array
                Results.add(u)
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

        log.info "Ldap Search - ${EntityType} processing complete. Processed ${totalEntriesReturned} ${EntityType}s."
        System.out.println ("Ldap Search - ${EntityType} processing complete. Nb of Processed ${EntityType}s: " + String.valueOf(totalEntriesReturned))

        return Results
    }

    //******************
    // Private Utilities
    //******************

    //---------------
    // Create LDAP Search Filter
    //---------------
    private SearchRequest buildSearchFilter (ConnectorEntities et) {

        switch (et) {
            case ConnectorEntities.Person:
                return new SearchRequest(config.getUserBaseDN(),
                        SearchScope.SUB, Filter.createEqualityFilter("objectClass", config.getUserObjClass()))

            case ConnectorEntities.Group:
                return new SearchRequest(config.getGroupBaseDN(),
                        SearchScope.SUB, Filter.createEqualityFilter("objectClass", config.getGroupObjClass()))
        }
    }

    //---------------
    // Create Entity
    //---------------
    private Entity createEntity (ConnectorEntities et, SearchResultEntry e) {
        switch (et) {
            case ConnectorEntities.Person:
                return createUser(e)

            case ConnectorEntities.Group:
                return createGroup(e)
        }
    }

    //-------------------
    // Create User Entity
    //-------------------
    private  Person createUser(SearchResultEntry e) {

        Person u = new Person()
        def dn = e.getDN()

        u.setDn(dn)
        // Extract user attribs from LDAP Search results
        def attribs = [:]
        def attrIt = e.getAttributes().iterator()
        while (attrIt.hasNext()) {
            Attribute a = (Attribute) attrIt.next()
            attribs.put(a.getName(), a.getValues())
        }
        //add DN
        attribs.put("dn", dn)
        // Add Attributes
        u.setAttributes(attribs)
        // Add Parent DN
        u.setParent(getParentDN(dn))

        return u
    }

    //---------------------
    // Create Group Entity
    //---------------------
    private  Group createGroup(SearchResultEntry e) {

        Group g = new Group()
        def dn = e.getDN()

        g.setDn(dn)
        // Extract user attribs from LDAP Search results
        def attribs = [:]
        def attrIt = e.getAttributes().iterator()
        while (attrIt.hasNext()) {
            Attribute a = (Attribute) attrIt.next()
            def attrName = a.getName()
            // Add members as separate Array
            if (attrName == config.getMemberAttribute()) {
                g.setMembers(a.getValues())
            } else {
                attribs.put(attrName, a.getValues())
            }
        }
        //add DN
        attribs.put("dn", dn)
        // Add Attributes
        g.setAttributes(attribs)
        // Add Parent DN
        g.setParent(getParentDN(dn))

        return g

    }

    //---------------------
    // Given a DN String, returns the parent DN or "" if the given DN is the base.
    //---------------------
    private String getParentDN (String dn) {

        def String parent

        parent = dn.substring(dn.indexOf(",") +1)

        // Only return a parent if not at the base of the Search tree
        return ((parent == config.getBaseDN()) ||
                (parent == config.getGroupBaseDN()) ||
                (parent == config.getUserBaseDN())) ? "" : parent
    }

}

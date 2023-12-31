package com.threeedges.analyzer.neoloader.ldap

import com.threeedges.analyzer.neoloader.config.LdapConfiguration
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.BindResult
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

import groovy.util.logging.*;
import org.codehaus.groovy.runtime.StackTraceUtils;

/**
 * Creates an LDAP Connection pool to the LDAP server which configuration is supplied through the constructor.
 *
 * 3Edges - March 2015
 * Created by ababeanu on 2015-03-10.
 */
@Log
class ConnectionPool {

    private LDAPConnectionPool Pool;

    /**
     * Constructor
     * Instantiates a Connection pool to the LDAP server which configuration is supplied.
     *
     * @param cfg an LdapConfiguration object encapsulating the config for an LDAP Server.
     */
    ConnectionPool (LdapConfiguration cfg) throws LDAPException {

        // Prepare TLS connection - Trust all certs
        SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
        LDAPConnection connection;
        try {
            // Create a new LDAP connection pool with the given connections established and
            // authenticated to the same server:
            connection = new LDAPConnection(sslUtil.createSSLSocketFactory(),
                    cfg.getLdapHost(), cfg.getLdapPort());
            log.info "LDAPConnection Succesfully Created \n"
        } catch (Exception e) {
            log.severe "Failed to CONNECT to the configured Directory. Check the configuration."
            log.severe  StackTraceUtils.sanitize(e).toString()
            throw e
        }

        BindResult bindResult
        try {
            bindResult = connection.bind(cfg.getBindDN(), cfg.getPassword());
            log.info "Succesfull LDAP bind !"
        } catch (LDAPException e) {
            log.severe "Failed to BIND to the configured Directory. Check the configuration."
            log.severe  StackTraceUtils.sanitize(e).toString()
            throw e
        }

        try {
            this.Pool = new LDAPConnectionPool(connection, cfg.getNbConnections());
            log.info "An LDAPConnection Pool with ${cfg.getNbConnections()} Succesfully Created "
        } catch (LDAPException le) {
            log.severe "Failed to create a Connection Pool to the configured server."
            log.severe  StackTraceUtils.sanitize(le).toString()
            throw le
        }


    }

    // ACCESSORS

    /**
     *  Returns the established connections to the given server.
     *
     * @return and com.unboundid.ldap.sdk.LDAPConnectionPool
     */
    def LDAPConnectionPool getPool() {
        return Pool;
    }
}

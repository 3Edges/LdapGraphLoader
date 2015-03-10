package com.nulli.analyzer.neoloader.ldap

import com.nulli.analyzer.neoloader.config.LdapConfiguration
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.BindResult;

/**
 * Creates an LDAP Connection pool to the LDAP server which configuration is supplied through the constructor.
 *
 * Created by ababeanu on 15-03-10.
 */
class ConnectionPool {

    private LDAPConnectionPool Pool;

    /**
     * Constructor
     * Instantiates a Connection pool to the LDAP server which configuration is supplied.
     *
     * @param cfg an LdapConfiguration object encapsulating the config for an LDAP Server.
     */
    ConnectionPool (LdapConfiguration cfg) {

        // Create a new LDAP connection pool with the given connections established and
        // authenticated to the same server:
        LDAPConnection connection = new LDAPConnection(cfg.getLdapHost(), cfg.getLdapPort());
        BindResult bindResult = connection.bind(cfg.getBindDN(), cfg.getPassword());
        this.Pool = new LDAPConnectionPool(connection, cfg.getNbConnections());

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

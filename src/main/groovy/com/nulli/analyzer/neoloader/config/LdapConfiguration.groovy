package com.nulli.analyzer.neoloader.config

/**
 * Encapsulates the configuration for the source LDAP Directory. The configuration is expected to be provided
 * in a Properties File.
 *
 * @Version 0.1
 *
 * Nulli Secundux Inc. - March 2015
 * Created by ababeanu on 15-03-09.
 */
class LdapConfiguration {
    private String LdapHost;
    private String LdapPort;
    private String BindDN;
    private String Password;
    private String BaseDN;

    // CONSTANTS
    private static final String DEFAULT_CFG_FILE_NAME = "ldapServer.properties";
    private String HOST_PROP = "host";
    private String PORT_PROP = "port";
    private String BINDDN_PROP = "binddn";
    private String PASSWORD_PROP = "password";
    private String BASEDN_PROP = "basedn";

    /**
     * Creates an LDAP Configuration that uses the default properties file name ("ldapServer.properties"),
     * expected to reside locally to the NeoLoader.
     */
    LdapConfiguration () {
        def config = new ConfigSlurper().parse(new File(DEFAULT_CFG_FILE_NAME).toURI().toURL());
        loadCfg(config);
    }

    /**
     * Create an LDAP Configuration that uses the LDAP Connectivity stored in the provided file.
     *
     * @param PropFileName The file name that contains the LDAP configuration properties.
     */
    LdapConfiguration (String PropFileName) {
        def config = new ConfigSlurper().parse(new File(PropFileName).toURI().toURL());
        loadCfg(config);
    }

    // ACCESSORS

    String getLdapHost() {
        return LdapHost
    }

    String getLdapPort() {
        return LdapPort
    }

    String getBindDN() {
        return BindDN
    }

    String getPassword() {
        return Password
    }

    String getBaseDN() {
        return BaseDN
    }

    // PRIVATE METHODS

    /**
     * Loads a given Configuration
     *
     * @param FName A ConfigSlurper ConfigObject, encapsulating the LDAP Properties to Load.
     */
    private void loadCfg (ConfigObject cfg) {
        this.LdapHost = cfg.getProperty(HOST_PROP);
        this.LdapPort = cfg.getProperty(PORT_PROP);
        this.BindDN = cfg.getProperty(BINDDN_PROP);
        this.Password = cfg.getProperty(PASSWORD_PROP);
        this.BaseDN = cfg.getProperty(BASEDN_PROP);
    }
}

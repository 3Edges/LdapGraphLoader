package com.threeedges.analyzer.neoloader.config


/**
 * Encapsulates the configuration for the destination Neo4J Graph server. The configuration is expected to be provided
 * in a Properties File.
 *
 * @Version 0.1
 *
 * 3Edges - November 2015
 * Created by ababeanu on 2015-11-24.
 */
class NeoConfiguration implements LoaderConfig{
    private String Host;
    private int NeoPort;
    private String Database;
    private String User;
    private String Password;
    private String authorization;
    private String idLabel;
    private String groupLabel;
    private String neoScheme;

    // CONSTANTS
    private static final String DEFAULT_CFG_FILE_NAME = "resources/neoServer.properties";
    private final String HOST_PROP = "host";
    private final String PORT_PROP = "port";
    private final String DATABASE_PROP = "database";
    private final String USER_PROP = "user";
    private final String PWD_PROP = "password";
    private final String AUTHORIZATION = "authorization";
    private final String ID_LABEL = "identityLabel";
    private final String GROUP_LABEL = "groupLabel";
    private final String NEO_SCHEME = "neoScheme";

    /**
     * Creates an LDAP Configuration that uses the default properties file name ("ldapServer.properties"),
     * expected to reside locally to the NeoLoader.
     */
    NeoConfiguration() {
        def config = new ConfigSlurper().parse(new File(DEFAULT_CFG_FILE_NAME).toURI().toURL());
        loadCfg(config);
    }

    /**
     * Create an LDAP Configuration that uses the LDAP Connectivity stored in the provided file.
     *
     * @param PropFileName The file name that contains the LDAP configuration properties.
     */
    NeoConfiguration(String PropFileName) {
        def config = new ConfigSlurper().parse(new File(PropFileName).toURI().toURL());
        loadCfg(config);
    }

    // ACCESSORS

    String getHost() {
        return Host
    }

    int getNeoPort() {
        return NeoPort
    }

    String getIdLabel() {
        return idLabel
    }
    String getGroupLabel() {
        return groupLabel
    }

    String getUser() {
        return User
    }

    String getPassword() {
        return Password
    }

    String getAuthorization() {
        return authorization
    }

    String getDatabase() {
        return Database
    }

    String getNeoScheme() {
        return neoScheme
    }


    /**
     * Loads a given Configuration
     *
     * @param FName A ConfigSlurper ConfigObject, encapsulating the LDAP Properties to Load.
     */
     void loadCfg (ConfigObject cfg) {
         this.Host = cfg.getProperty(HOST_PROP);
         this.NeoPort = cfg.getProperty(PORT_PROP);
         this.Database = cfg.getProperty(DATABASE_PROP);
         this.User = cfg.getProperty(USER_PROP);
         this.Password = cfg.getProperty(PWD_PROP);
         this.authorization = cfg.getProperty(AUTHORIZATION);
         this.idLabel = cfg.getProperty(ID_LABEL);
         this.groupLabel = cfg.getProperty(GROUP_LABEL);
         this.neoScheme = cfg.getProperty(NEO_SCHEME)
    }
}



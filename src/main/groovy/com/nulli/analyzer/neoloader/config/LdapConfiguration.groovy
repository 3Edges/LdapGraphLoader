package com.nulli.analyzer.neoloader.config

/**
 * Encapsulates the configuration for the source LDAP Directory. The configuration is expected to be provided
 * in a Properties File.
 *
 * @Version 0.1
 *
 * 3Edges. - March 2015
 * Created by ababeanu on 2015-03-09.
 */
class LdapConfiguration implements LoaderConfig {
    private String LdapHost;
    private int LdapPort;
    private String BindDN;
    private String Password;
    private String BaseDN;
    private String UserBaseDN;
    private String GroupBaseDN;
    private String UserObjClass;
    private String GroupObjClass;
    private String MemberAttribute;
    private int NbConnections;
    private int PageSize;

    // CONSTANTS
    private static final String DEFAULT_CFG_FILE_NAME = "resources/ldapServer.properties";
    private final String HOST_PROP = "host";
    private final String PORT_PROP = "port";
    private final String BINDDN_PROP = "binddn";
    private final String PASSWORD_PROP = "password";
    private final String BASEDN_PROP = "basedn";
    private final String USER_BASEDN_PROP = "userBasedn";
    private final String GROUP_BASEDN_PROP = "groupBasedn";
    private final String USER_OBJCLASS_PROP = "userobjclass";
    private final String GROUP_OBJCLASS_PROP = "groupobjclass";
    private final String GROUP_MEMBER_ATTRIB = "memberattribute";
    private final String NB_CONNECTIONS_PROP = "nbconnections";
    private final String PAGE_SIZE_PROP = "pagesize";

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

    int getLdapPort() {
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

    String getUserObjClass() {
        return UserObjClass
    }

    String getGroupObjClass() {
        return GroupObjClass
    }

    int getNbConnections() {
        return NbConnections
    }

    String getMemberAttribute() {
        return MemberAttribute
    }

    void setMemberAttribute(String memberAttribute) {
        MemberAttribute = memberAttribute
    }

    int getPageSize() {

        return PageSize
    }

    String getUserBaseDN() {
        return UserBaseDN
    }

    void setUserBaseDN(String userBaseDN) {
        UserBaseDN = userBaseDN
    }

    String getGroupBaseDN() {
        return GroupBaseDN
    }

    void setGroupBaseDN(String groupBaseDN) {
        GroupBaseDN = groupBaseDN
    }
/**
     * Loads a given Configuration
     *
     * @param FName A ConfigSlurper ConfigObject, encapsulating the LDAP Properties to Load.
     */
    void loadCfg (ConfigObject cfg) {
        this.LdapHost = cfg.getProperty(HOST_PROP);
        this.LdapPort = cfg.getProperty(PORT_PROP);
        this.BindDN = cfg.getProperty(BINDDN_PROP);
        this.Password = cfg.getProperty(PASSWORD_PROP);
        this.BaseDN = cfg.getProperty(BASEDN_PROP);
        this.UserBaseDN = cfg.getProperty(USER_BASEDN_PROP);
        this.GroupBaseDN = cfg.getProperty(GROUP_BASEDN_PROP);
        this.UserObjClass = cfg.getProperty(USER_OBJCLASS_PROP);
        this.GroupObjClass = cfg.getProperty(GROUP_OBJCLASS_PROP);
        this.MemberAttribute = cfg.get(GROUP_MEMBER_ATTRIB);
        this.NbConnections = cfg.getProperty(NB_CONNECTIONS_PROP);
        this.PageSize = cfg.getProperty(PAGE_SIZE_PROP);
    }
}

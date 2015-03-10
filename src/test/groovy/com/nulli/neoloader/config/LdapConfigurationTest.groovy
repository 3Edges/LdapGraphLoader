package com.nulli.neoloader.config

import spock.lang.Specification;
import com.nulli.analyzer.neoloader.config.LdapConfiguration;

/**
 * Spock tests for the LDAP Configuration
 *
 * Nulli Secundus Inc - March 2015
 * Created by ababeanu on 15-03-09.
 */
class LdapConfigurationTest  extends Specification {

    def "test create LDAP Config"() {
        given:
        def config = new LdapConfiguration (fileName)

        when:
        def host = config.getLdapHost()
        def port = config.getLdapPort()
        def dn = config.getBindDN()
        def pwd = config.getPassword()
        def basedn = config.getBaseDN()

        then:
        host == 'example.com'
        port == '389'
        dn == "cn=Directory Manager"
        pwd == "Password01"
        basedn == "ou=people,dc=example,dc=com"

        where:
        fileName = "src/test/data/ldapServer.properties"
    }

}

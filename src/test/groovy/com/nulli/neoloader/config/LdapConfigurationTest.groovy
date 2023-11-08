package com.nulli.neoloader.config

import spock.lang.Specification;
import com.nulli.analyzer.neoloader.config.LdapConfiguration;

/**
 * Spock tests for the LDAP Configuration
 *
 * 3Edges - March 2015
 * Created by ababeanu on 2015-03-09.
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
        host == 'forgerock1.nullidemo.com'
        port == 1389
        dn == "cn=Directory Manager"
        pwd == "test1234"
        basedn == "dc=nulli,dc=com"

        where:
        fileName = "src/test/data/ldapServer.properties"
    }

    // --------

    def "test create Not Exists LDAP Config"() {

        when:
        def config = new LdapConfiguration (fileName)

        then:
        thrown(java.io.FileNotFoundException)

        where:
        fileName = "Dummy"
    }

}

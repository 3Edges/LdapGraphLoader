/**
 * Groovy CLI script that creates a Neo4j Graph representation of an LDAP Source.
 * The resulting Graph contains 2 types of nodes:
 *  - Users
 *  - Groups
 *
 *  The relationships between the nodes can me modelled as follows:
 *  - User --[MEMBER_OF] --> Group
 *  - Group --[CHILD_OF] --> Group
 *  - Group --[USED_WITH] --> Group
 *
 *  The latter relationship ("USED_WITH") additionally holds a 'weight' attribute, which simply counts the number
 *  of times the related groups have been used together.
 *
 *  To run:
 *  =======
 *  groovy -cp path/to/GraphAnalyzer.jar:runtime/* NeoLoader.groovy
 *
 * Nulli Secundus Inc. - March 2015
 * Created by ababeanu on 15-03-10.
 */


import com.nulli.analyzer.neoloader.LoaderProcessor
import com.nulli.analyzer.neoloader.config.LdapConfiguration;

@Grapes(
    @Grab(group='com.unboundid', module='unboundid-ldapsdk', version='2.3.8')
)
import com.unboundid.ldap.sdk.*;

// Command Line Builder
def cli = new CliBuilder(usage:'NeoLoader.groovy [-l <ldap Config File>] [-n <neo4j config file>] [-h]', width:80 );

// CLI options
cli.with {
    h longOpt: 'help', ('Show Usage Information')
    l longOpt: 'ldap-cfg-file', args: 1, argName: 'LDAP Config File', 'Configuration file that holds the LDAP connectivity config.'
    n longOpt: 'neo4j-cfg-file', args: 1, argName: 'Neo4J Config_File', 'Configuration file that holds the Neo4J connectivity config.'
}
def options = cli.parse(args)

if (options.h) {
    // Help: usage
    cli.usage()
    return
}

def LdapCfg;
def NeoCfg;

// Load LDAP Config
if ( options.l ) {
    try {
        LdapCfg = new LdapConfiguration((String) options.l);
    } catch (FileNotFoundException fnf) {
       System.err.println('The provided LDAP Config File name is invalid, please try again.');
        return;
    }
    System.out.println 'Using LDAP Config: ' + options.l + '...';

} else {
    // Use default config file
    try {
        LdapCfg = new LdapConfiguration();
    } catch (FileNotFoundException fnf) {
        System.err.println('No LDAP Config File name provide and can\'t find the default one. Please supply a valid configuration file and try again.');
        return;
    }
    System.out.println 'Using default LDAP Config ...';
}

// Trigger Loader Processor
// TODO
def Loader = new LoaderProcessor(LdapCfg);
Loader.processUsers();


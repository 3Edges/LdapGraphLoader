# Graph/LDAP Analyzer #

Graph Loader, aka Graph Analyzer v 1.0

### Summary
The Graph Loader and Analyzer tool creates a Neo4J Graph database from data read from LDAP (v 1.0). In the graph, Users and Groups are related using 2 types of relationships:

* `MEMBER_OF` : user group membership
* `PART_OF` : Group hierarchy, relates a child to parent Group relationship

The idea is to then :

1. use the graph to optimize and simplify the LDAP Directory, through the use of relevant CYPHER queries (V 2.0). The optimizations could then be reported and acted upon.
2. use the graph directly in Access Policy decisions

Finally (v 3.0), the Analyzer could apply the suggested optimizations directly to the LDAP directory.

### Features
Currently at *version 1* (December 2015), the Graph Analyzer has the following features:

* Uses the [UnboundID SDK](https://www.ldap.com/unboundid-ldap-sdk-for-java) to perform _paginated_ LDAP Searches on any LDAP Directory source.
* Uses LDAP attribute to Neo4J Property mappings to filter and transform LDAP Attributes
* Uses the Neo4J REST APIs for all Graph I/O operations. The assumption is that the Neo4J server could be anywhere (including the cloud), and thus not embedded.
* All Processing is done in memory with I/O operations kept to a minimum
* Supports USERS and GROUP hierarchies.

### Requirements
The utility is built as a *Gradle* project using the *Groovy* language. It therefore requires:

* The [Groovy](http://www.groovy-lang.org/download.html) language. On Mac: `brew install groovy`.
* [Gradle](http://gradle.org/gradle-download/). On Mac: `brew install gradle`.

### Installation

1. Download the source
2. From the source dir: `gradle build --refresh-dependencies`
3. Update the configuration files to match your environment (location: the `resources` subdir)
4. To Run: `neoLoader.sh` - Or on Windows boxes: 
`groovy -cp build/libs/GraphAnalyzer-0.0.1-SNAPSHOT.jar:runtime/* -Djava.util.logging.config.file=resources/logging.properties NeoLoader.groovy`

### Configuration

The Graph Loader uses the following config files...

#### ldapServer.properties

Gathers all the connectivity and metadata of the source LDAP Directory. In particular:
* `host` : LDAP Server Host
* `port` : LDAP Server Port
* `binddn' : LDAP Admin User DN
* `password` : LDAP User password
* `userBasedn` : The DN of the container under which the User Accounts are stored. The Loader will _NOT_ create a node for this container (on flat DIT, it may contain millions of user accounts, which would imply too many relationships in the graph for a single node).
* `groupBaseDn` : The DN of the container under which the Groups are stored. The Loader will _NOT_ create a node for this container.
* `userobjclass` : The LDAP Objectclass of the User Accounts. Used for searching.
* `groupobjclass` : The LDAP Objectclass of the Groups. Used for searching.

#### 
* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact
package com.nulli.analyzer.neoloader.model

/**
 * Encapsulates an instance of an Entity.
 * An entity is an LDAP entry that can be modeled as a node in Neo4J.
 *
 * Created by ababeanu on 2015-11-24 - 3Edges.
 */
interface Entity {

    /*
     Contractual Methods
     */

    /**
     * Returns the Entity's type. For example: "Person", "Group", etc...
     * @return a String representing the Entity's type
     */
    String getEntityType ()

    /**
     * Returns the Entity's DN
     * @return a String representing the Entity's DN
     */
    String getDn ()

    /**
     * Returns the Entity's Attributes
     * @return a Map representing the Entity's attrbutes.
     */
    Map getAttributes ()

    /**
     * Sets the Entity's DN
     * @param dn a String representing the DB of the Entity
     */
    void setDn (String dn)

    /**
     * Sets the Entity's attributes.
     * @param attribs a Map representing the Entity's attribute = value pairs.
     * Note: the Map values may be Maps themselves. This Map comes for a JSON
     * mapping file.
     */
    void setAttributes (Map attribs)

    /**
     * Stores the DN of the parent entity of the current
     * @param parentDN
     */
    void setParent (String parentDN)

    String getParent ()
}
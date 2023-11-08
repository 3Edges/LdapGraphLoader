package com.nulli.analyzer.neoloader.model

import com.nulli.analyzer.neoloader.connector.ConnectorEntities

/**
 * Encapsulates a Group instance and all relevant attributes.
 *
 * @Version 0.1
 *
 * 3Edges - March 2015
 * Created by ababeanu on 2015-03-10.
 */
class Group implements Entity {
    // User Attributes
    private final String entityType = ConnectorEntities.Group
    private String dn
    private Map attributes
    private String[] members
    private String parentDN

    /**
     * Constructor.
     * Empty, User is a Bean.
     */
    Group () {

    }

    // ACCESSORS

    String getEntityType () {
        return entityType
    }

    String getDn() {
        return dn
    }

    void setDn(String DN) {
        this.dn = DN
    }

    Map getAttributes() {
        return attributes
    }

    void setAttributes(Map attribs) {
        attributes = attribs
    }

    void setParent (String pDN) {
        parentDN = pDN
    }

    String getParent () {
        return parentDN
    }

    // Specific to the Group entity:

    String[] getMembers() {
        return members
    }

    void setMembers(String[] members) {
        this.members = members
    }
}

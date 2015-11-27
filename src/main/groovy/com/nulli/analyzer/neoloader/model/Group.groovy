package com.nulli.analyzer.neoloader.model

/**
 * Encapsulates a Group instance and all relevant attributes.
 *
 * @Version 0.1
 *
 * Nulli Secundus Inc. - March 2015
 * Created by ababeanu on 15-03-10.
 */
class Group implements Entity {
    // User Attributes
    private final String entityType = "Group"
    private String dn
    private Map attributes
    private String[] members

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

    // Specific to the Group entity:

    String[] getMembers() {
        return members
    }

    void setMembers(String[] members) {
        this.members = members
    }
}

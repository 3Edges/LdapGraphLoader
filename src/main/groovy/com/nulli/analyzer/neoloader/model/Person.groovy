package com.nulli.analyzer.neoloader.model

import com.nulli.analyzer.neoloader.connector.ConnectorEntities

/**
 * Encapsulates a User instance and all relevant attributes.
 * NOTE: only a subset of attributes are relevant to the Graph DB
 *
 * @Version 0.1
 *
 * Nulli Secundus Inc. - March 2015
 * Created by ababeanu on 15-03-10.
 */
class Person implements Entity {

    // User Attributes
    private String dn
    private final String entityType = ConnectorEntities.Person
    private Map attributes
    private String parentDN

    /**
     * Constructor.
     * Empty, User is a Bean.
     */
    Person() {

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
}

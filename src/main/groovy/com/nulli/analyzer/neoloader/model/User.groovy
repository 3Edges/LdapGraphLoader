package com.nulli.analyzer.neoloader.model

/**
 * Encapsulates a User instance and all relevant attributes.
 *
 * @Version 0.1
 *
 * Nulli Secundus Inc. - March 2015
 * Created by ababeanu on 15-03-10.
 */
class User {

    // User Attributes
    private String Uid;
    private String DN;
    private String Uuid;

    /**
     * Constructor.
     * Empty, User is a Bean.
     */
    User () {

    }

    // ACCESSORS

    String getUid() {
        return Uid
    }

    void setUid(String uid) {
        Uid = uid
    }

    String getDN() {
        return DN
    }

    void setDN(String DN) {
        this.DN = DN
    }

    String getUuid() {
        return Uuid
    }

    void setUuid(String uuid) {
        Uuid = uuid
    }
}

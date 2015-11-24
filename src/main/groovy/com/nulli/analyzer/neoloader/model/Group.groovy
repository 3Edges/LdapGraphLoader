package com.nulli.analyzer.neoloader.model

/**
 * Encapsulates a Group instance and all relevant attributes.
 *
 * @Version 0.1
 *
 * Nulli Secundus Inc. - March 2015
 * Created by ababeanu on 15-03-10.
 */
class Group {
    // User Attributes
    private String GroupName;
    private String DN;
    private String Uuid;
    private String [] Members;

    /**
     * Constructor.
     * Empty, User is a Bean.
     */
    Group () {

    }

    // ACCESSORS

    String getGroupName() {
        return Uid
    }

    void setGroupName(String uid) {
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

    String[] getMembers() {
        return Members
    }

    void setMembers(String[] members) {
        Members = members
    }
}

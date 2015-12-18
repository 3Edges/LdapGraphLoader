package com.nulli.analyzer.neoloader.connector

import com.nulli.analyzer.neoloader.model.Entity
/**
 * Interfaces with source systems or components. Contains source data to copy to Neo4J
 *
 * Created by ababeanu on 2015-11-30.
 */
interface ConnectorFacade {

    /**
     * Searches the given target for all instances of the given Entity, i.e., searches ALL entities (unfiltered Search).
     * The resultset may be big, intentionally, therefore ensure the RAM of the client can bear the load.
     * The expectation is that all results will be available in-memory in order to improve response times.
     *
     * TODO: Consider using OpenICF to support LDIF, CSV and other loads.
     *
     * @param EntityType A ConnectorEntities value representing the type of entity to search for.
     * @param SearchAttribs A Collection that contains all the attributes to be Searched for and returned.
     * @return An ArrayList of Entity objects.
     */
    ArrayList<Entity> search (ConnectorEntities EntityType, Collection SearchAttribs)
}
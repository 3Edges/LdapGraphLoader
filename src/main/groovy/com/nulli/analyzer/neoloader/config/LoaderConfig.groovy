package com.nulli.analyzer.neoloader.config

/**
 * Represents a DataSource or Neo target configuration object, which encapsulates all data necessary to perform a connection.
 *
 * Created by ababeanu on 2015-11-27.
 */
interface LoaderConfig {

    /**
     * Loads the configuration for the given data target from file.
     * @param cfg an instance of ConfigSlurper that encapsulates the configuration data read from a properties file.
     */
    void loadCfg (ConfigObject cfg)

}
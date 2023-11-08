#!/usr/bin/env bash
groovy -cp build/libs/LdapGraphLoader-1.0.jar:runtime/* -Djava.util.logging.config.file=resources/logging.properties NeoLoader.groovy
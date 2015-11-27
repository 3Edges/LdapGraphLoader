#!/usr/bin/env bash
groovy -cp build/libs/GraphAnalyzer-0.0.1-SNAPSHOT.jar:runtime/* -Djava.util.logging.config.file=resources/logging.properties NeoLoader.groovy
#!/bin/bash
java -Djdbc.drivers=org.postgresql.Driver -DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000 -jar target/esa-wiki-1.0-jar-with-dependencies.jar "$@"
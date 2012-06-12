#!/bin/bash

AS7DIR=/home/matthew/Applications/jboss-as-7.0.1.Final
ARCHIVE=/home/matthew/Dropbox/as7modules.tar.bz2
MYSQL=modules/com/mysql/*

# We have a custom Hibernate libarry for Seam 2
HIBERNATE=modules/org/hibernate/3/*

# This is used in conjunction with the RestEASY client
APACHECOMMONSHTTP=modules/org/apache/commons/http/*

# This needs to be modified to depend on the Apache HTTP client library
RESTEASY=modules/org/jboss/resteasy/resteasy-jaxrs

tar -cjf ${ARCHIVE} -C ${AS7DIR} ${MYSQL} ${HIBERNATE} ${APACHECOMMONSHTTP} ${RESTEASY}
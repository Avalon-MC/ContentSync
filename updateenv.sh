#!/usr/bin/env bash

GRADLE_PROPERTIES_FILE=gradle.properties

function getProperty {
    PROP_KEY=$1
    PROP_VALUE=`cat $GRADLE_PROPERTIES_FILE | grep "$PROP_KEY" | cut -d'=' -f2`
    echo $PROP_VALUE
}

export MINECRAFT_VERSION=$(getProperty "minecraft_version")
export MCMOD_VERSION=$(getProperty "mod_version")

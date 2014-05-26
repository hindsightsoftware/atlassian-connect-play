#!/bin/bash

########################################################################################################################
# Added this script because I couldn't figure out how to make authentication work publishing through SBT. This script will
# install the jar, sources and javadocs of a previously locally published (play publish-local) play module.
# Do NOT deploy SNAPSHOTs as this script only deploys to public (for now)
#
# To run the script you need:
#   - to have PLAY_HOME defined so that it can find the local play repo to deploy artifacts from
#   - specify the parameters:
#       deploy.sh groupId artifactId version
########################################################################################################################

LOCAL_PLAY_HOME=$PLAY_HOME
GROUP_ID=$1
ARTIFACT_ID=$2
VERSION=$3

function deploy {
  mvn install:install-file -DpomFile=$1 -Dfile=$2 -Dclassifier=$3 
}

REPO=$LOCAL_PLAY_HOME/repository/local
POM=$REPO/$GROUP_ID/$ARTIFACT_ID/$VERSION/poms/$ARTIFACT_ID.pom 

echo 'Using POM file at '$POM

JAR=$REPO/$GROUP_ID/$ARTIFACT_ID/$VERSION/jars/$ARTIFACT_ID.jar 
echo 'Deploying Jar at '$JAR
deploy $POM $JAR ''

SRC=$REPO/$GROUP_ID/$ARTIFACT_ID/$VERSION/srcs/$ARTIFACT_ID-sources.jar
echo 'Deploying Sources at '$SRC
deploy $POM $SRC sources

DOC=$REPO/$GROUP_ID/$ARTIFACT_ID/$VERSION/docs/$ARTIFACT_ID-javadoc.jar
echo 'Deploying Javadocs at '$DOC
deploy $POM $DOC javadoc

echo "All done!"

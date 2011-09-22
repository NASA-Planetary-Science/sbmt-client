#!/bin/bash

cd `dirname $0`
. setup.sh


AMICA_DIR=/project/nearsdc/data/ITOKAWA/AMICA/images
INERTIAL_FILE=/project/nearsdc/data/ITOKAWA/AMICA/internal/INERTIAL.TXT

$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_DIR $INERTIAL_FILE

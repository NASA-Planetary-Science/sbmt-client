#!/bin/bash

cd `dirname $0`
. setup.sh


AMICA_DIR=/project/nearsdc/data/ITOKAWA/AMICA/images
INERTIAL_FILE=/project/nearsdc/data/ITOKAWA/AMICA/INERTIAL.TXT

$JAVA_COMMAND edu.jhuapl.near.tools.AmicaBackplanesLatexGenerator $AMICA_DIR $INERTIAL_FILE

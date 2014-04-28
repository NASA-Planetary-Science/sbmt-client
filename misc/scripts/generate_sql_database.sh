#!/bin/bash

cd `dirname $0`

./run_java_program.sh edu.jhuapl.near.tools.DatabaseGeneratorSql 1 eros > sqlgeneration-eros1.log 2>&1 &
./run_java_program.sh edu.jhuapl.near.tools.DatabaseGeneratorSql 2 eros > sqlgeneration-eros2.log 2>&1 &
wait
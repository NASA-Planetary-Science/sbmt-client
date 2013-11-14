#!/bin/bash

cd `dirname $0`
. setup.sh

$JAVA_COMMAND $@

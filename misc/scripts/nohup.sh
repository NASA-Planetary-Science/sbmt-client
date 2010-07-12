#!/bin/bash

# This program runs a given command using the nohup program. The
# purpose of this program is to allow one to use the nohup command
# without having to include the ampersand, &, at the end of the
# line. This makes it easier to use from certain shell scripts.

/usr/bin/nohup $@ >> nohup.out &

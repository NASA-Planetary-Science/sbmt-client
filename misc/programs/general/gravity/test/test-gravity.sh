#!/bin/sh

export PATH=/Users/kahneg1/projects/near/source/git/sbmt-develop/misc/programs/general/build:$PATH

gravity --werner --centers -d 2.1 -r 0.000426209829547 --suffix centers --output-folder `pwd` yorp.obj
gravity --werner --average-vertices -d 2.1 -r 0.000426209829547 --suffix average --output-folder `pwd` yorp.obj
gravity --cheng --centers -d 2.1 -r 0.000426209829547 --suffix centers --output-folder `pwd` yorp.obj
gravity --cheng --average-vertices -d 2.1 -r 0.000426209829547 --suffix average --output-folder `pwd` yorp.obj
gravity --cheng --vertices -d 2.1 -r 0.000426209829547 --suffix vertices --output-folder `pwd` yorp.obj
gravity --cheng --file pointlist.txt --columns 1,2,3 -d 2.1 -r 0.000426209829547 --suffix file-all --output-folder `pwd` yorp.obj
gravity --cheng --file pointlist.txt --columns 1,2,3 -d 2.1 -r 0.000426209829547 --suffix file-100 --output-folder `pwd` --start-index 0 --end-index 100 yorp.obj

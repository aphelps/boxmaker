#!/bin/bash

java -cp BOX.jar com.rahulbotics.boxmaker.BoxMaker -D 2 -H 1 -W 3 -T 0.125 -f ./test.pdf
open test.pdf

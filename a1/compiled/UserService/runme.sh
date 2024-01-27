#!/bin/bash
# Shell script in a1/compiled/UserService

# Set relative paths
project_root=../../
us_bin=./bin
us_lib=../lib

# Run Java program
java -cp "$us_bin:$us_lib/jackson-annotations-2.7.9.jar:$us_lib/jackson-core-2.7.9.jar:$us_lib/jackson-databind-2.7.9.jar" UserServer

#!/bin/bash
# Shell script in a1/compiled/UserService

# Set relative paths
project_root=../../
us_bin=./bin
us_lib=../lib

# Run Java program
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    # Windows
    java -cp "$us_bin;$us_lib/*" UserServer config.json
else
    # Unix-like (Linux, macOS)
    java -cp "$us_bin:$us_lib/*" UserServer config.json
fi
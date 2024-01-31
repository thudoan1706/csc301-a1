#!/bin/bash
# Shell script in a1/compiled/ProductService

# Set relative paths
project_root=../../
ps_bin=./bin
ps_lib=../lib

# Run Java program
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    # Windows
    java -cp "$ps_bin;$ps_lib/*" OrderService config.json
else
    # Unix-like (Linux, macOS)
    java -cp "$ps_bin:$ps_lib/*" OrderService config.json
fi
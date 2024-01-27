#!/bin/bash
# Shell script in a1/compiled/UserService

# Remove all compiled .class files
find ../../ -name '*.class' -delete

# Set relative paths
project_root=../../
us_bin=./bin
us_lib=../lib
srcFile=$project_root/src/UserService/src/main/java/
data=./data/

# Create the bin directory if it doesn't exist
mkdir -p "$us_bin"

# Create the data directory if it doesn't exist
mkdir -p "$data"

# Compile Java files
javac -source 11 -target 11 -d "$us_bin" -cp "$us_lib/jackson-annotations-2.7.9.jar:$us_lib/jackson-core-2.7.9.jar:$us_lib/jackson-databind-2.7.9.jar" "$srcFile"/*.java

# Run Java program
java -cp "$us_bin:$us_lib/jackson-annotations-2.7.9.jar:$us_lib/jackson-core-2.7.9.jar:$us_lib/jackson-databind-2.7.9.jar" UserServer

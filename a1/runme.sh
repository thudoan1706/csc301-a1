#!/bin/bash
# Need to document chmod +x runtime.sh scripts

# Make all .sh scripts executable
find . -type f -name "*.sh" -exec chmod +x {} \;

# Run all .sh scripts
# find . -type f -name "*.sh" -execdir sh {} \;

# Find the 'compiled' directory within any subdirectory
compiled_dir=$(find . -type d -name "compiled")

while getopts ":c" opt; do
  case $opt in
    c)
        us_dir="$compiled_dir/UserService"

        if [ -n "$us_dir" ]; then
            cd "$us_dir"
            ./runme.sh
        else
            echo "Error: 'compiled/UserService' directory not found." >&2
            exit 1
        fi
        ;;
  esac
done
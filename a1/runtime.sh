#!/bin/bash
# Need to document chmod +x runtime.sh scripts

# Make all .sh scripts executable
find . -type f -name "*.sh" -exec chmod +x {} \;

# Run all .sh scripts
find . -type f -name "*.sh" -exec ./{} \;
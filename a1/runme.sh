#!/bin/bash
# Make all .sh scripts executable

# -------- Setup folder-------------
mkdir -p "./compiled"
cp -r "./lib" ./compiled
# User Services
mkdir -p "$compiled_dir/UserService"
cp "user.sh" "$compiled_dir/UserService"
cp "config.json" "$compiled_dir/UserService"


# -------- Set Folder Variable-------------
# Find the 'compiled' directory within any subdirectory
compiled_dir=$(find . -type d -name "compiled")
userSrcFile=./src/UserService/src/main/java/
jar_lib=$compiled_dir/lib/
find . -type f -name "*.sh" -exec chmod +x {} \;


while getopts ":uc" opt; do
  case $opt in
    c)  
        #USER SERVICE
        # Create a user service folder under compiled directory

        data=$compiled_dir/UserService/data

        # Create the bin directory if it doesn't exist
        mkdir -p "$us_bin"
        us_bin=$compiled_dir/UserService/bin

        # Create the data directory if it doesn't exist
        mkdir -p "$data"

        # Compile User Services
        javac -d "$us_bin" -cp "$jar_lib/jackson-annotations-2.7.9.jar:$jar_lib/jackson-core-2.7.9.jar:$jar_lib/jackson-databind-2.7.9.jar" "$userSrcFile"/*.java;;
    
        #TODO OTHER SERVICES
    
    u)
        us_dir="$compiled_dir/UserService"

        if [ -n "$us_dir" ]; then
            cd "$us_dir"
            ./user.sh
        else
            echo "Error: 'compiled/UserService' directory not found." >&2
            exit 1
        fi
        ;;
  esac
done
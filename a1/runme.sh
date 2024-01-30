#!/bin/bash
# Make all .sh scripts executable

# -------- Setup folder-------------
mkdir -p "./compiled"
cp -r "./lib" ./compiled
# User Services
compiled_dir=$(find . -type d -name "compiled")

mkdir -p "$compiled_dir/UserService"
cp "user.sh" "$compiled_dir/UserService"
cp "config.json" "$compiled_dir/UserService"

mkdir -p "$compiled_dir/ProductService"
cp "product.sh" "$compiled_dir/ProductService"
cp "config.json" "$compiled_dir/ProductService"

mkdir -p "$compiled_dir/OrderService"
cp "order.sh" "$compiled_dir/OrderService"
cp "config.json" "$compiled_dir/OrderService"


# -------- Set Folder Variable-------------
# Find the 'compiled' directory within any subdirectory
userSrcFile=./src/UserService/src/main/java/
productSrcFile=./src/ProductService/src/main/java/
orderSrcFile=./src/OrderService/src/main/java/
jar_lib=$compiled_dir/lib
find . -type f -name "*.sh" -exec chmod +x {} \;


while getopts ":ucpow" opt; do
  case $opt in
    c)  
        #USER SERVICE
        # Create the bin directory if it doesn't exist
        us_bin=$compiled_dir/UserService/bin
        mkdir -p "$us_bin"

        # Create the data directory if it doesn't exist
        data=$compiled_dir/UserService/data
        mkdir -p "$data"

        # Compile User Services
        javac -d "$us_bin" -cp "$jar_lib/*" "$userSrcFile"/*.java

        #PRODUCT SERVICE
        # Create the bin directory if it doesn't exist
        ps_bin=$compiled_dir/ProductService/bin
        mkdir -p "$ps_bin"

        # Create the data directory if it doesn't exist
        data=$compiled_dir/ProductService/data
        mkdir -p "$data"

        # Compile Product Services
        javac -d "$ps_bin" -cp "$jar_lib/*" "$productSrcFile"/exceptions/*.java "$productSrcFile"/*.java

        #ORDER SERVICE
        # Create the bin directory if it doesn't exist
        os_bin=$compiled_dir/OrderService/bin
        mkdir -p "$os_bin"


        # Create the data directory if it doesn't exist
        data=$compiled_dir/OrderService/data
        mkdir -p "$data"

        # Compile Order Services
        javac -d "$os_bin" -cp "$jar_lib/*" "$orderSrcFile"/*.java;;

    p)
        ps_dir="$compiled_dir/ProductService"

        if [ -n "$ps_dir" ]; then
            cd "$ps_dir"
            ./product.sh
        else
            echo "Error: 'compiled/ProductService' directory not found." >&2
            exit 1
        fi
        ;;

    o)
        os_dir="$compiled_dir/OrderService"

        if [ -n "$os_dir" ]; then
            cd "$os_dir"
            ./order.sh
        else
            echo "Error: 'compiled/OrderService' directory not found." >&2
            exit 1
        fi
        ;;
        
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
    w)  
        if [ $# -eq 0 ]; then
            echo "Usage: $0 -w <file>"
            exit 1
        fi

        python3 workload_parser.py config.json
    ;;
  esac
done
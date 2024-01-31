jar_lib="./lib"
echo "$jar_lib"
# Ensure Jackson JAR files are in the jar_lib directory

# Check and fix the classpath syntax
classpath="$jar_lib/*"

javadoc -d docs -cp "$classpath" ./src/OrderService/src/main/java/*
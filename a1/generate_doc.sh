
# Ensure Jackson JAR files are in the jar_lib directory
classpath="./lib/*"
order="./src/OrderService/src/main/java/*"
user="./src/UserService/src/main/java/*"
product="./src/ProductService/src/main/java/*.java"
product_exception="./src/ProductService/src/main/java/exceptions/*.java"


# Check and fix the classpath syntax
javadoc -d docs/OrderService -cp "$classpath" $order
javadoc -d docs/UserService -cp "$classpath" $user
javadoc -d docs/ProductService -cp "$classpath" $product $product_exception
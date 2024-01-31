# Service Management Script (runme.sh) - README

## Overview

The `runme.sh` script is designed to simplify the compilation and execution of various services within your project. It provides a set of options to compile services, start individual services, and simulate workloads for testing purposes.

## Usage

### Compiling Services

Use the `-c` option to compile all services in the project. This ensures that the latest changes are incorporated into the binary files.

```bash
./runme.sh -c
```

### Starting Services

#### User Service

Use the `-u` option to start the user service, which is essential for handling user-related functionalities.

```bash
./runme.sh -u
```

#### Product Service

Execute the `-p` option to launch the product service. This service handles operations related to products in your application.

```bash
./runme.sh -p
```

#### Order Service

Start the order service by using the `-o` option. The order service manages all aspects related to orders and transactions.

```bash
./runme.sh -o
```

### Simulating Workload

To simulate a specific workload, run the script with the `-w` option followed by the name of the workload file (e.g., `testworkload.txt`). This allows you to test the services under a predefined workload scenario.

```bash
./runme.sh -w testworkload.txt
```

## Note

Ensure that the necessary dependencies and configurations are in place before running the script. Modify the script or configuration files to suit your required port and ip (config.json)as needed to suit your project's requirements.

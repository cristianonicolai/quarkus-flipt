# Quarkus Flipt Integration Test Example

This project demonstrates how to create a Quarkus application that securely connects to a Flipt instance using a self-signed certificate. It uses Testcontainers to launch a real Flipt container for integration tests, providing a complete, end-to-end example of a secure connection.

## Requirements

Before you begin, ensure you have the following tools installed on your system:

* **Java 21 SDK** (or newer)
* **Apache Maven 3.9.0** (or newer)
* **Docker** (must be running)
* **Quarkus CLI**

---

## Setup and Running the Project

Follow these steps to set up your environment and run the integration tests.

### Step 1: Install the Quarkus CLI

If you don't have the Quarkus Command Line Interface installed, follow the official guide to set it up for your operating system:

* [**Install the Quarkus CLI**](https://quarkus.io/guides/cli-tooling#installing-the-cli)

### Step 2: Generate the Self-Signed Certificate

This project requires a self-signed certificate for the test environment. Use the Quarkus CLI to generate the necessary PKCS#12 keystore and truststore files.

Run the following command from the root of the project directory:

```bash
quarkus tls generate-certificate --name flipt-cert --self-signed
```

This will create a `.certs` directory containing `flipt-cert-keystore.p12` and `flipt-cert-truststore.p12`.

### Step 3: Extract Certificates for the Test Environment

The Flipt container used in the tests requires the certificate and private key to be in PEM format (`.crt` and `.key`). A helper script is provided to extract these from the files you just generated.

Make the script executable and run it:
```bash
chmod +x prepare_test_certs.sh
```

This will create the `server.crt` and `server.key` files inside the `src/test/resources/` directory, which will be used by the test.

### Step 4: Build the Project and Run Tests

Now you are ready to build the project. The following command will compile the code, download dependencies, and execute the integration tests, which will automatically start and configure the Flipt Docker container.

```bash
mvn clean install
```

If all steps were followed correctly, the build should complete successfully, indicating that your Quarkus application was able to connect to the secure, containerized Flipt instance and evaluate the test flag.

## Manually create certificate ( Alternate method )

This test setup requires you to create a few files for the Flipt container's configuration.

1.  **Generate a Self-Signed Certificate:**
    Use `openssl` to create a certificate and key.
    ```bash
    openssl req -x509 -newkey rsa:2048 -nodes \
      -keyout src/test/resources/server.key \
      -out src/test/resources/server.crt \
      -sha256 -days 365 \
      -subj "/CN=localhost"
    ```
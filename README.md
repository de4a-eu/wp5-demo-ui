# wp5-demo-ui

A simple demo UI to trigger sending to the Mock Connector - see https://github.com/de4a-wp5/de4a-connector-mock
Other functionality:
* Create random example messages of all document types
* Validate arbitrary messages against all supported document types

## Building

Prerequisites
* Java 1.8 or later
* Apache Maven 3.6 or later for building

1. Ensure the de4a-commons project is up-to-date and built - see https://github.com/de4a-wp5/de4a-commons
2. Call `mvn clean install`
3. Build the Docker image
    * `docker build --pull -t de4a-wp5-demoui .`
4. Run the Docker image and open the exposed port
    * `docker run -d --name de4a-wp5-demoui -p 8888:8080 de4a-wp5-demoui`
5. Open your browser and locate `http://localhost:8888`
6. Select the parameters and press `Send Mock request`
7. Inspect the message and the results

## Releases

This application is work in progress so it doesn't contain too many releases.
They are only created for specific circumstances.
* v0.1.1 - is a bug fix release for Iteration 1 that provides mocked DO USI redirect URLs as well
* v0.1.0 - marks the version indicates as "working" for "Iteration 1"

# wp5-demo-ui

A simple demo UI to trigger sending to the Mock Connector - see https://github.com/de4a-wp5/de4a-connector-mock

## Building

Prerequisites
* Java 11 or later
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

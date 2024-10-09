# Data Reconciliation
This component identifies which variables are semantically equivalent given their type (e.g., water flow) and their scientific assumptions (e.g., geographical location).

This component is part of a mayor technological solution. More info can be found in: https://github.com/iLink-CyberShARE/workflow-composer-public

## When running
Ontologies for every data reconciliation executions are stored in harddrive. To modify change settings.yml/ontologysavepath

## How to test
Run Maven command: mvn test
Test cases are described in src/test/java
Inputs and expected output are described in src/test/resources

## How to run
Generate a new jar file with: mvn package
Run jar file with: java -jar target/${name-of-jar}.jar server settings.yml
Admin tools at: http://localhost:8081

Endpoint information:
Method: POST
Request payload:
    ignore:A JSON that represents scientific assumptions to be ignored
    availableData: A JSON that represents available variables
    componentCatalog: A JSON that represents all available components
URL: http://localhost:8080/main
Content-Type: application/x-www-form-urlencoded

## How to run - Example using Mozilla RESTClient v3.0.7
1. Create custom header
    Name = Content-Type
    Attribute value = application/x-www-form-urlencoded
2. Set method to POST
3. Set url to http://localhost:8080/main
4. Set Body to
    availableData={}&ignore=[]&componentCatalog={}
5. Expected output: {"error":"An error ocurred"}
    Explanation: No given component catalog and available data was given
6. Modify the body to your own catalog and available data

## How to build a test case
1. Create a folder in src/test/resources
2. Create the following JSON files:
    2.1. availableData.json: This file contains available variables.
    2.2. componentCatalog.json: This file contains available components, their input and output.
    2.3. ignore.son: This file contains scientific asusmptions to be ignored.
    2.4. output.json: This file contains the expected output.
    2.5. README.MD (optional): A description of the test case
3. Create a method in src/test/java/AppTest.java with the following structure:
    3.1. Add the tag @Test, this tag will enable the execution of the test case
    3.2. Within the method create a string whose value is the name of the folder created in step 1.
    3.3. Call the method "runTestCase" sending the string from step 3.2. as a parameter

## Docker
Change "dropwizard-template" to the new proyectt in Dockerfile
Change "dropwizard-template-1.0.0.jar" to the new proyect's jar file in Dockerfile
Change "alejandrovargas/dropwizard-template" to the dockeruser and image name in docker-compose.yml
Change ports to new ports in docker-compose.yml
Add files to dockerignore as needed
Test with:
    docker build -t dockeruser/image_name:latest .
    docker run -p 5005:5001 dockeruser/image_name
Push to docker with:
    docker push dockeruser/image_name

## Authors
Raul Alejandro Vargas Acosta (ravargasaco@miners.utep.edu)

## Acknowledgements

## License
This software code is licensed under the [GNU GENERAL PUBLIC LICENSE v3.0](./LICENSE) and uses third party libraries that are distributed under their own terms (see [LICENSE-3RD-PARTY.md](./LICENSE-3RD-PARTY.md)).

## Copyright
© 2024 - University of Texas at El Paso
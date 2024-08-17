# Capture the Flag Server (Praktikum Software Engineering FSS24)

## Requirements Specification

The **requirements specification** can always be found in [REQUIREMENTS_SPECIFICATION.md](REQUIREMENTS_SPECIFICATION.md).

## Project

Capture the Flag server project which is a Spring-Boot based service.

### Run

You can download the Jar from either the release or the package section. Afterwards, execute CtfApplication.java in Code if you downloaded the whole source code or open the server.jar the following way:
```bash
java -jar server.jar
```
The server is also automatically started by our [UI](https://github.com/sephdoto/CaptureTheFlag) on startup, so the jar does not need to be executed.  

### Integrated Webservice

Configuration (e.g., port) is located in [application.properties](src%2Fmain%2Fresources%2Fapplication.properties).
The default port is 8080 for the standalone jar

## Main Authors 
- Raffay Syed
- Simon Stumpf

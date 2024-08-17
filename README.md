# Capture the Flag Server (Praktikum Software Engineering FSS24)

## Requirements Specification

The **requirements specification** can always be found in [REQUIREMENTS_SPECIFICATION.md](REQUIREMENTS_SPECIFICATION.md).

## Project

Capture the Flag server project which is a Spring-Boot based service.

### Run

You can download the Jar from [releases](https://github.com/sephdoto/CaptureTheFlagServer/releases/tag/publish) or (more current) [packages](https://github.com/sephdoto/CaptureTheFlagServer/packages/2230324). After downloading the ctf-x.x.x.jar, where the x.x.x stand for the version number, you can run the JAR by opening up a terminal where it is located and doing:
```bash
java -jar ctf-x.x.x.jar
```

The server is also automatically started by our [UI](https://github.com/sephdoto/CaptureTheFlag) on startup, so the jar does not need to be executed.

If you downloaded the source code and imported it as a project, you can execute CtfApplication.java to start the webserver.

### Integrated Webservice and Default Port

Configuration (e.g., port) is located in [application.properties](src%2Fmain%2Fresources%2Fapplication.properties). 
The default port is 8888 for the released/packaged jar. Or you can also see the port being used in the Springboot logs

### How to get it working in Maven
You should be able to do the following to get the package working in Maven
Step 1. Make sure to Install the server project in your local maven rep by doing 
```bash
mvn install
```
in the projects root directly or alternatively by using your IDEs built in maven manager.

Please pay close attention to the version number in the pom.xml of the server.

Step 2. Now you can import the package as a dependency in your very own maven project by using the following dependency import code in your pom.xml
```bash
<dependency>
  <groupId>de.uni-mannheim.swt.pse</groupId>
  <artifactId>ctf</artifactId>
  <version>1.1.0</version>
</dependency>
```
Note: Version number can change, so you might have to make sure that it matches up with the one you installed dependency line

IF YOUR CODE HAS ERRORS IMPORTING:
Make sure that the server project verion number and your dependency version numbers match up. Also make sure that the package is installed in your local maven repo properly!

## Main Authors 
- Raffay Syed
- Simon Stumpf
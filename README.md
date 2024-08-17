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
Step 1. Add the following code to your pom.xml file to specify an alternate package repository (Ideally right after the properties block)
```bash
<repositories>
    <repository>
      <id>github</id>
      <url>https://maven.pkg.github.com/sephdoto/CaptureTheFlagServer</url>
    </repository>
</repositories>
```
Step 2. Import the package as a dependency by using the following code in your pom.xml
```bash
<dependency>
  <groupId>de.uni-mannheim.swt.pse</groupId>
  <artifactId>ctf</artifactId>
  <version>1.0.6</version>
</dependency>
```
Note: Version number can change, so you might have to manually change it to the latest one in the dependency line

## Main Authors 
- Raffay Syed
- Simon Stumpf

# Capture the Flag Server (Praktikum Software Engineering FSS24)

## Requirements Specification

The **requirements specification** can always be found in [REQUIREMENTS_SPECIFICATION.md](REQUIREMENTS_SPECIFICATION.md).

## Project

Capture the Flag server project which is a Spring-Boot based service.

### Run

You can download the Jar from either the [release](https://github.com/sephdoto/CaptureTheFlagServer/releases/tag/publish) or the [packages](https://github.com/sephdoto/CaptureTheFlagServer/packages/2230324) section. Afterwards, execute CtfApplication.java in Code if you downloaded the whole source code or open the server.jar the following way:
```bash
java -jar server.jar
```
The server is also automatically started by our [UI](https://github.com/sephdoto/CaptureTheFlag) on startup, so the jar does not need to be executed.  

### Integrated Webservice

Configuration (e.g., port) is located in [application.properties](src%2Fmain%2Fresources%2Fapplication.properties).
The default port is 8080 for the standalone jar

### How to get it working in Maven
You should be able to use the following guide to get the package working in Maven

A TL;DR is to add:
1. The following code to your pom.xml file to specify an alternate package repository
```bash
<reporsitories>
  <reporsitory>
    <id>github</id>
    <url>https://maven.pkg.github.com/sephdoto/CaptureTheFlagServer</url>
  </reporsitory>
</reporsitories>
```
2. Then import the package as a dependency in your Client by providing the following code in your pom.xml
```bash
<dependency>
  <groupId>de.uni-mannheim.swt.pse</groupId>
  <artifactId>ctf</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Main Authors 
- Raffay Syed
- Simon Stumpf

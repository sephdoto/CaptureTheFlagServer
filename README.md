# Capture the Flag Server (Praktikum Software Engineering FSS24)

## Project

Capture the Flag server project which is a Spring-Boot based service.

### Run

You can download the Jar from [releases](https://github.com/sephdoto/CaptureTheFlagServer/releases/tag/publish) or (more current) [packages](https://github.com/sephdoto/CaptureTheFlagServer/packages/2230324). After downloading the ctf-x.x.x.jar, where the x.x.x stands for the version number, you can run it by opening up a terminal where the file is located and doing:

```bash
java -jar ctf-x.x.x.jar
```

The server is also automatically started by our [UI](https://github.com/sephdoto/CaptureTheFlag) on startup, so the jar does not need to be executed.

If you downloaded the source code and imported it as a project, you can execute CtfApplication.java to start the webserver.

### Integrated Webservice and Default Port

Configuration (e.g., port) is located in [application.properties](src%2Fmain%2Fresources%2Fapplication.properties).
The default port is 8888 for the released/packaged jar. Alternatively, you can also see the port being used in the Springboot logs

### How to get it working in Maven for integration testing

You should be able to do the following to get the package working in Maven

Step 1. Import the repo and make sure to Install the server project in your local maven repository by doing 

```bash
mvn install
```

Step 2. Now you can import the package as a dependency in your very own maven project by using the following dependency import code in your pom.xml

```bash
<dependency>
  <groupId>de.uni-mannheim.swt.pse</groupId>
  <artifactId>ctf</artifactId>
  <version>1.1.5</version>
</dependency>
```

Notes:

1. Version number can change, so make sure that they match up!

2. If you want to export your own standalone JAR. You need to make sure that you add add the following plugin in between your < build > < plugins >  tags

```bash
  <plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
  </plugin>
```

The default pom.xml is meant to be integrated into another app and so the plugin cannot be included by default in the pom.xml

IF YOUR CODE HAS ERRORS IMPORTING:
Double check and make sure that the version numbers match between your installed server version number and the version of your depedency import.

## Requirements Specification

The **requirements specification** can always be found in [REQUIREMENTS_SPECIFICATION.md](REQUIREMENTS_SPECIFICATION.md).

## Main Authors

- Raffay Syed
- Simon Stumpf

## Changelog

1.1.4 Simplified installation
1.1.5 Improved Memory management

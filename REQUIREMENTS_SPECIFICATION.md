# Requirements Specification (PSE FSS24)

## Changes / Updates

* 22.02.24: reworded testing tasks to improve understanding
* 22.02.24: Added example for grid format (see [grid format](#grid-format-example-cell-values-squares))

## Project - Capture The Flag

### Gameplay

Possible gameplay and rules are limited by the richness of the map template schema (see [here](#map-template-schema) for details).

#### Rules

1. Each team has a base and a flag, which are located in the center of their half or quadrant of the grid.
2. Each team takes turns moving one piece at a time, with the goal of capturing the opponent's flag while protecting their own.
3. The base and flag(s) are considered immovable objects and cannot be moved.
4. Pieces can capture opponent pieces by occupying the same space as them. A piece can only capture an opponent piece if it has a higher attack power than the opponent piece.
5. If a piece captures an opponent piece, the opponent piece is removed from the board and cannot be used again.
6. The game can also contain blocks that are randomly placed on the grid. Blocks are considered immovable objects and cannot be moved. Pieces cannot occupy the same space as them, and they cannot make movements over blocks.
7. If a team's flag(s) are captured, the game is over and the team that captured the flag wins.
8. If all pieces on a team are captured, the game is over and the team with remaining pieces wins. 
9. The game can also end if a time limit is reached.

#### Winner

A team wins (and the game ends) once (either of the following)

* all the opponents flag(s) are captured,
* all the opponents pieces are captured,
* the opponent(s) give up (depending on the number of teams),
* the total game time is reached (see variations below).

A draw (multiple winners) is also possible.

#### Game Variations

The following limitations can be optionally set in the map template (see [here](#map-template-schema) for details) -

* **total game limit in seconds** - if reached, the winner is the team with the highest number of remaining pieces,
* **the time to move a piece in seconds** - if reached, the move is skipped, and it is the turn of the team next.

### Example

![img.png](img%2Fimg.png)

## Webservice Project Template

The following two core building blocks are given -

1. Client/Server architecture
   1. predefined RESTful interface (several endpoints)
   2. protocol (schema) - JSON representation
   3. Java interface for the game engine
   4. map template schema
2. Existing webservice project (project in this repository)
   1. based on spring boot project
   2. serves as the extension point for development
   3. managed with Maven build tool

### RESTful Interface, Protocol and Schemas

The fully-specified RESTful interface is realized using `spring-web`. It delegates the work to the game engine using the game engine's Java interface (see [Java Interface](#java-interface-game-engine)). It is documented using `spring-doc`. You can find the corresponding controller class here

* [GameSessionController.java](src%2Fmain%2Fjava%2Fde%2Funimannheim%2Fswt%2Fpse%2Fctf%2Fcontroller%2FGameSessionController.java)

Important: To provide a game engine instance to the controller class, you have to change the factory method `public static Game createGameEngine()` in [CtfApplication.java](src%2Fmain%2Fjava%2Fde%2Funimannheim%2Fswt%2Fpse%2Fctf%2FCtfApplication.java) and return your own game engine instance.

Modifications to the controller class are not allowed!

You can explore the RESTful API, its documented endpoints and schemas in an interactive manner using OpenAPI/swagger
* http://localhost:8888/swagger-ui/index.html

For this, either run the Java class `CtfApplication` in your IDE, or build and run the entire project. For example,

```bash
# in the repository root
./mvnw clean install
java -jar target/ctf-0.0.1-SNAPSHOT.jar
```

Note: The actual port can be either specified in [application.properties](src%2Fmain%2Fresources%2Fapplication.properties) or via command line (i.e., overrides properties) as follows

```bash
# build jar (see above)
# in the repository root
java -Dserver.port=8888 -jar target/ctf-0.0.1-SNAPSHOT.jar
```

### Java Interface (Game Engine)

Your game engine must fully implement the following Java interface

* [Game.java](src%2Fmain%2Fjava%2Fde%2Funimannheim%2Fswt%2Fpse%2Fctf%2Fgame%2FGame.java)

It is documented using classic JavaDoc. Requests to the RESTful API (i.e., controller class) are delegated to this interface, and its returned objects are translated into corresponding responses.

### Map Template Schema

The rules of a game are `naturally` limited by the predefined template schema for game maps.

You can find the corresponding Java model (data) classes in this package [map](src%2Fmain%2Fjava%2Fde%2Funimannheim%2Fswt%2Fpse%2Fctf%2Fgame%2Fmap).

The main map template class is located in
* [MapTemplate.java](src%2Fmain%2Fjava%2Fde%2Funimannheim%2Fswt%2Fpse%2Fctf%2Fgame%2Fmap%2FMapTemplate.java)

To illustrate the map template schema, a JSON document example of a 10x10 grid, 2 teams (chess-like pieces, attack power and movements) can be found in
* [10x10_2teams_example.json](src%2Fmain%2Fresources%2Fmaptemplates%2F10x10_2teams_example.json)

The schemas are also documented as part of the RESTful interface (controller section as well as `schemas` section):
* http://localhost:8888/swagger-ui/index.html

### Grid Format Example: Cell Values (Squares)

The [GameState.java](src%2Fmain%2Fjava%2Fde%2Funimannheim%2Fswt%2Fpse%2Fctf%2Fgame%2Fstate%2FGameState.java) class assumes a certain grid format to link cells to teams' bases, pieces and block.

The following (simplified) example of a possible `String[][] grid` value (5x5) depicts the expected format (represented as a JSON array)  

```json
[
   ["b:1", "", "", "", ""],
   ["", "p:1_1", "p:1_2", "p:1_3", ""],
   ["b", "", "", "", "b"],
   ["", "p:2_1", "p:2_2", "p:2_3", ""],
   ["", "", "", "", "b:2"]
]
```

* `""` - denotes an empty cell (i.e., square)
* `b:1` - denotes the base of team `1`
* `p:1_n` and `p:2_n` - denote the pieces of team `1` (and `2`) where `n` is the piece identifier
* `b` - denotes a block

Using this format for cell values, a piece, for instance, can be resolved to a piece object ([Piece.java](src%2Fmain%2Fjava%2Fde%2Funimannheim%2Fswt%2Fpse%2Fctf%2Fgame%2Fstate%2FPiece.java)).

### Constraints

There are several important project-related constraints to satisfy.

#### Predefined Interfaces, Protocol and Schemas

**Modifications** of the interfaces (i.e., code of interface classes) and schemas (protocol, maps and corresponding Java data classes) are not allowed in general.

Even though we did our best to anticipate the requirements of the final game, minor changes to fix apparent issues may be still necessary. In this case, teams can make change requests in a manner transparent to all the teams. Change requests are realized as `push requests` to this repository (via GitLab) that describe the actual concern (issue) including the suggested (code) improvements (i.e., the 'fix').

#### Project Setup, Building and Tools

You are allowed to work with any IDE/code editors (Eclipse, JetBrains IDEA, VSCode etc.) you choose. However, it is important to stress the following two things

1. Do not change the way this webservice project is built (i.e., must remain a Maven-managed project)
2. Communicate with your team members and agree on a set of tools to establish a feasible workflow to avoid broken projects and builds.


## Requirements

### Functional Requirements

1. **Backend** - Develop two independent systems (i.e., independent execution)
   1. Game engine (extend the existing webservice project)
   2. Player clients
      1. three autonomous AI (machine) players (note: AI in the broad sense including your developed algorithms derived from gameplay etc.)
      2. human player client (accepts user inputs)
2. **Frontend** - Graphical user interface (GUI) using JavaFX
   1. Player client GUI - visualize gameplay
      1. visualize autonomous gameplay of AI players
      2. allow human player interactions (user inputs)
      3. allow for custom theming of the board and pieces (two different themes mandatory)
   2. Map editor GUI - create maps on your own
      1. template engine (note: map template schema is predefined!)
      2. let users design new maps visually
      3. render existing maps based on given map template schema
      4. create three _interesting_ and playable maps to demonstrate your systems
3. **Testing** - Demonstrate the correctness of your backend systems/components
      1. (automated) unit testing of all game engine operations (game flow, logic, moves, state etc.) with JUnit5 (https://junit.org/junit5/)
      2. integration testing with your three predefined maps (see above)
      3. demonstrate that your two systems (game engine service and clients) work with any valid map template
         1. this is the prerequisite to compete with other teams
      4. demonstrate that your player clients act autonomously given a webservice that realizes the RESTful interface
      5. automated GUI testing (i.e., creating a set of automated tests) is not necessary

### Non-Functional Requirements

The final software products are required to support the following environments for their execution
* Java JDK 17 (OpenJDK or similar like Eclipse Temurin etc.)
* JavaFX running on all major operating systems (Windows, MacOS, Linux)
* Code and GUI language: english

## Competition (to be announced)

Goal – Compete with your best AI player (your choice) in the final course tournament!

### How?

We plan to do a pair-wise competition (allotted randomly).

1. Team A sends AI player of choice to Team B and vice versa.
2. Both teams run independent competitions and report their results
3. Submit experience report (similar to execution of test plans)
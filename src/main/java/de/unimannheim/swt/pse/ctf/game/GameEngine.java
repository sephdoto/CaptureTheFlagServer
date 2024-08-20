package de.unimannheim.swt.pse.ctf.game;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameOver;
import de.unimannheim.swt.pse.ctf.game.exceptions.InvalidMove;
import de.unimannheim.swt.pse.ctf.game.exceptions.NoMoreTeamSlots;
import de.unimannheim.swt.pse.ctf.game.exceptions.TooManyPiecesException;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Move;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import de.unimannheim.swt.pse.ctf.game.state.Team;
import javafx.scene.paint.Color;

/**
 * Implementation of the {@link Game} interface from Team CFP14
 *
 * @author sistumpf and rsyed
 */
public class GameEngine implements Game {

  // **************************************************
  // Fields
  // **************************************************

  // **************************************************
  // Required by GameEngine
  // **************************************************
  private GameState gameState; // MAIN Data Store for GameEngine
  private Date startedDate = null;
  private Date endDate;
  private boolean weDoneZo; // Setting this to true signals End game
  private Clock currentTime;
  private Map<Integer, String> integerToTeam;
  private Map<String, Integer> teamToInteger;
  private Random random;

  // **************************************************
  // End of Required by GameState
  // **************************************************

  // **************************************************
  // Nice to haves
  // **************************************************
  private MapTemplate copyOfTemplate; // Saves a copy of the template
  private static final Logger LOG = LoggerFactory.getLogger(GameEngine.class);

  // **************************************************
  // END of Nice to haves
  // **************************************************

  // **************************************************
  // Alt Mode Data : Never Asked for directly: Internal use Vars
  // **************************************************
  private boolean timeLimitedGameTrigger;
  private boolean moveTimeLimitedGameTrigger;

  private Clock gameShouldEndBy;
  private Duration totalGameTime;
  private Clock turnEndsBy;
  private Duration turnTime;
  private int graceTime = 1; // Time added to be fair for processing delays
  private GameState nameState;

  // **************************************************
  // END of Alt Mode Data
  // **************************************************
  /**
   * Method creates a game based on the MapTemplate given to it For logic see design and flow
   * documents
   *
   * @author rsyed
   * @param template the {@link MapTemplate} you want to want to use for the game
   */
  @Override
  public GameState create(MapTemplate template) {
    this.copyOfTemplate = template; // Template Copy Box
    this.integerToTeam = Collections.synchronizedMap(new LinkedHashMap<>());
    this.teamToInteger = Collections.synchronizedMap(new LinkedHashMap<>());
    this.weDoneZo = false;
    random = new Random();
    gameState = new GameState();
    gameState.setTeams(new Team[template.getTeams()]);

    // inits the grid with blocks and bases
    new BoardController(gameState, template);
    // Inits Alt Game mode support
    initAltGameModeLogic(template);

    return gameState;
  }

  /**
   * Updates a game and its state based on team join request (add team).
   *
   * <ul>
   *   <li>adds team if a slot is free (array element is null)
   *   <li>if all team slots are finally assigned, implicitly starts the game by picking a starting
   *       team at random
   * </ul>
   *
   * @author rsyed, sistumpf
   * @param teamId Team ID
   * @return Team
   * @throws NoMoreTeamSlots No more team slots available
   */
  @Override
  public Team joinGame(String teamId) {
    if (teamToInteger.containsKey(teamId) || getRemainingTeamSlots() == 0) {
      throw new NoMoreTeamSlots();
    }
    int slot = EngineTools.getNextEmptyTeamSlot(this.gameState);
    Team tempTeam = new BoardController(this.gameState).initializeTeam(slot, copyOfTemplate);
    // Method above sets Flags, Pieces in the Team object, which is already a part of this.gameState

    teamToInteger.put(teamId, slot);
    integerToTeam.put(slot, teamId);

    Team copy = EngineTools.deepCopyTeam(tempTeam);
    copy.setId(teamId);
    canWeStartTheGameUwU();
    return copy;
  }

  /**
   * Here a move, if valid, updates the grid, a pieces position, a teams flags and the time.
   * The GameState with Team Names instead of IDs gets updated too.
   *
   * @author sistumpf
   * @param move {@link Move}
   * @throws InvalidMove Requested move is invalid
   * @throws GameOver Game is over
   */
  @Override
  public void makeMove(Move move) {
    //if piece IDs get send as "17" instead of "p:0_17", this code provides compatibility.
    //note that this only works with incoming moves, last move in GameState cannot be tailored to a specific client.
    if(!move.getPieceId().startsWith("p:" + move.getTeamId() + "_"))
      move.setPieceId("p:" + move.getTeamId() + "_" + move.getPieceId());
    
    if (!new NameIDChanger(integerToTeam, teamToInteger).putMoveIDs(move) 
        || !movePreconditionsMet(move)) 
      throw new InvalidMove();
    EngineTools.computeMove(this.gameState, move);
    afterMoveCleanup();

    NameIDChanger nidChanger = new NameIDChanger(integerToTeam, teamToInteger);
    this.nameState = nidChanger.putGameStateNames(gameState);
  }

  /**
   * A team has to option to give up a game (i.e., losing the game as a result). Assume that a team
   * can only give up if it is its move (turn). If the next teams got no moves left they also get
   * removed. If only one team is left the game ends.
   *
   * @author sistumpf
   * @param teamId Team ID
   */
  @Override
  public void giveUp(String teamId) {
    if (teamToInteger.get(teamId)
        == this.gameState
        .getCurrentTeam()) { // test is also in controller but doppelt gemoppelt hält besser
      EngineTools.removeTeam(gameState, teamToInteger.get(teamId)); // removed and set to null
      this.gameState.setCurrentTeam(EngineTools.getNextTeam(gameState));
    }
    if (EngineTools.removeMovelessTeams(this.gameState)) setGameOver();

    NameIDChanger nidChanger = new NameIDChanger(integerToTeam, teamToInteger);
    this.nameState = nidChanger.putGameStateNames(gameState);
  }

  /**
   * Checks whether a move is valid based on the current game state.
   *
   * @author sistumpf
   * @param move {@link Move}
   * @return true if move is valid based on current game state, false otherwise
   */
  @Override
  public boolean isValidMove(Move move) {
    if (isStarted()) {
      Piece picked =
          Arrays.stream(
              gameState
              .getTeams()[Integer.parseInt(move.getPieceId().split(":")[1].split("_")[0])]
                  .getPieces())
          .filter(p -> p.getId().equals(move.getPieceId()))
          .findFirst()
          .get();
      return EngineTools.getPossibleMoves(this.gameState, picked).stream()
          .anyMatch(i -> i[0] == move.getNewPosition()[0] && i[1] == move.getNewPosition()[1]);
    }
    return false;
  }

  /**
   * Checks whether the Game is started based on the current {@link GameState}.
   *
   * @author rsyed
   * @return true if game is started, false is over
   */
  @Override
  public boolean isStarted() {
    return (!isGameOver() && (getStartedDate() != null));
  }

  /**
   * Checks whether the game is over based on the current {@link GameState}.
   *
   * @author rsyed
   * @return true if game is over, false if game is still running.
   */
  @Override
  public boolean isGameOver() {
    return (this.weDoneZo);
  }

  /**
   * If the game is over a String Array containing all winner IDs is returned. This method relies on
   * the fact that loser teams get set to null in the gameState.teams Array.
   *
   * @author Code: sistumpf
   * @return {@link Team#getId()} if there is a winner
   */
  @Override
  public String[] getWinner() {
    if (!isGameOver()) return new String[] {};
    ArrayList<String> winners = new ArrayList<String>();
    if (this.isGameOver()) {
      for (int i = 0; i < gameState.getTeams().length; i++) {
        if (gameState.getTeams()[i] != null) winners.add(integerToTeam.get(i));
      }
    }

    return winners.toArray(new String[winners.size()]);
  }

  /**
   * Simple Getter for the date the Game Started On
   *
   * @author rsyed
   * @return Start {@link Date} of game
   */
  @Override
  public Date getStartedDate() {
    return startedDate;
  }

  /**
   * Simple Getter for the date the game Ended on
   *
   * @author rsyed
   * @return End date of game
   */
  @Override
  public Date getEndDate() {
    return this.endDate;
  }

  /**
   * Returns the current GameState.
   * If the game is started, the GameState with Team Names (instead of IDs) gets returned.
   *
   * @author sistumpf, rsyed
   * @return Current {@link GameState} of the Session
   */
  @Override
  public GameState getCurrentGameState() {
    if(this.isStarted() || this.isGameOver()) {
      while(this.nameState == null) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {e.printStackTrace();}
      }
      return this.nameState;
    } else {

      //inits the current GameState with Names and IDs
      NameIDChanger nidChanger = new NameIDChanger(integerToTeam, teamToInteger);
      return nidChanger.putGameStateNames(gameState);
    }
  }

  /**
   * Checks how many empty objects are left in the Team[] in the gameState
   *
   * @author rsyed
   * @return number of remaining team slots
   */
  @Override
  public int getRemainingTeamSlots() {
    int counter = 0;
    for (Team t : gameState.getTeams()) {
      if (t == null) {
        counter++;
      }
    }
    return counter;
  }

  // **************************************************
  // Alt Game Mode Methods
  // **************************************************

  /**
   * Method to call from create method while parsing {@link GameTemplate} and inits some prereqs for
   * the Alt Game Mode.
   *
   * @author rsyed
   */
  private void initAltGameModeLogic(MapTemplate template) {
    if ((template.getMoveTimeLimitInSeconds() != -1)
        || (template.getTotalTimeLimitInSeconds() != -1)) {
      this.currentTime = Clock.systemDefaultZone(); // Start BaseClock
      if (template.getMoveTimeLimitInSeconds() != -1) {
        this.moveTimeLimitedGameTrigger = true;
      }
      if (template.getTotalTimeLimitInSeconds() != -1) {
        this.timeLimitedGameTrigger = true;
      }
    } else {
      this.timeLimitedGameTrigger = false;
      this.moveTimeLimitedGameTrigger = false;
    }
  }

  /**
   * Main CONTROLLER for Alt Game Modes Call when Game Starts (teams are full)
   *
   * @author rsyed
   */
  private void startAltGameController() {
    if (moveTimeLimitedGameTrigger) {
      startMoveTimeLimitedGame();
    }
    if (timeLimitedGameTrigger) {
      startTimeLimitedGame();
    }
  }

  /**
   * This method inits some variables and logic to start logic for TimeLimitedGame.
   *
   * @author rsyed
   */
  private void startTimeLimitedGame() {
    this.totalGameTime =
        Duration.ofSeconds(copyOfTemplate.getTotalTimeLimitInSeconds() + graceTime);
    timeLimitedHandler();
  }

  /**
   * This method inits some variables and logic for MoveTimeLimitedGame
   *
   * @author rsyed
   */
  private void startMoveTimeLimitedGame() {
    this.turnTime = Duration.ofSeconds(copyOfTemplate.getMoveTimeLimitInSeconds() + graceTime);
    moveTimeLimitedHander();
  }

  /**
   * Checks how much time is left for the game
   *
   * @author rsyed
   * @return -1 if no total game time limit set, 0 if over, > 0 if seconds remain
   */
  @Override
  public int getRemainingGameTimeInSeconds() {
    if (copyOfTemplate.getTotalTimeLimitInSeconds() == -1) {
      return -1;
    }
    if (isGameOver()) {
      return 0;
    } else {
      try {
        return Math.toIntExact(
            Duration.between(currentTime.instant(), gameShouldEndBy.instant()).getSeconds());
      } catch (Exception e) {
        return 0;
      }
    }
  }

  /**
   * Handler which should be called incase the Game is a TimeLimited Game
   *
   * @author rsyed
   */
  public void timeLimitedHandler() {
    Thread timeLimitedThread =
        new Thread(
            () -> {
              boolean setOnceTrigger = true;
              while (timeLimitedGameTrigger && !isGameOver()) {
                if (isStarted()) {
                  if (setOnceTrigger) {
                    setWhenGameShouldEnd();
                    setOnceTrigger = false;
                  }
                  // Checks if Clock says its past game end time
                  if (currentTime.instant().isAfter(gameShouldEndBy.instant())) {
                    altGameModeGameOverHandler(); // Calls the Handler incase game has to end
                    timeLimitedGameTrigger = false; // Ends the Thread to reclaim resources
                  }
                }
                try { // Checks EVERY 1 second
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  LOG.info("Exception Occured in timeLimitedHandler thread");
                }
              }
            });
    timeLimitedThread.start();
  }

  /**
   * Method returns how many seconds are left in the turn
   *
   * @author rsyed
   * @return -1 if no move time limit set, 0 if over, > 0 if seconds remain
   */
  @Override
  public int getRemainingMoveTimeInSeconds() {
    if (copyOfTemplate.getMoveTimeLimitInSeconds() == -1) {
      return -1;
    }
    if (isGameOver()) {
      return 0;
    } else {
      try {
        return Math.toIntExact(
            Duration.between(currentTime.instant(), turnEndsBy.instant()).getSeconds());
      } catch (Exception e) {
        return 0;
      }
    }
  }

  /**
   * Tells the client the max time they have per move
   *
   * @return Turn time limit in seconds (<= 0 if none)
   */
  @Override
  public int getTurnTimeLimit() {
    return copyOfTemplate.getMoveTimeLimitInSeconds();
  }

  /**
   * Handler which should be called incase the moves are time limited in the game While the trigger
   * is active AND Teams are full, Checks if current time is after the time the turn should end. If
   * so..Switches Current Team to the next available team and resets the time
   *
   * @author rsyed
   */
  public void moveTimeLimitedHander() {
    Thread moveLimitedThread =
        new Thread(
            () -> {
              boolean setOnceTrigger = true;
              while (moveTimeLimitedGameTrigger && !isGameOver()) {
                if (isStarted()) { // If teams are full
                  if (setOnceTrigger) {
                    increaseTurnTimer(); // Block for increasing the timer ONCE for the first turn
                    setOnceTrigger = false;
                  }
                  if (currentTime.instant().isAfter(turnEndsBy.instant())) {
                    this.gameState.setCurrentTeam(EngineTools.getNextTeam(this.gameState));
                    NameIDChanger nidChanger = new NameIDChanger(integerToTeam, teamToInteger);
                    this.nameState = nidChanger.putGameStateNames(gameState);
                    increaseTurnTimer(); // UPDATES when the next turn should end
                  }
                  if (isGameOver()) { // Checks if game is over
                    moveTimeLimitedGameTrigger = false; // Ends the thread if game is over
                  }
                  try {
                    Thread.sleep(160);
                  } catch (InterruptedException e) {
                    LOG.info("Exception Occured in moveTimeLimitedHander thread");
                  }
                }
              }
            });
    moveLimitedThread.start();
  }

  /**
   * Part of turn Time limited Game logic Method to increase the timer. Can be used when a turn has
   * been made. Also called when turn timer expires to set timer for next move Call when turn time
   * expires (already done)
   *
   * @author rsyed
   */
  private void increaseTurnTimer() {
    this.turnEndsBy =
        Clock.fixed(Clock.offset(currentTime, turnTime).instant(), ZoneId.systemDefault());
  }

  /**
   * Part of time limited game Logic. Method called to set the time when the game should end. Auto
   * Set by the handler. Call when handler inits (already done)
   *
   * @author rsyed
   */
  private void setWhenGameShouldEnd() {
    this.gameShouldEndBy =
        Clock.fixed(Clock.offset(currentTime, totalGameTime).instant(), ZoneId.systemDefault());
    this.endDate = Date.from(gameShouldEndBy.instant());
  }

  /**
   * Method which will take care of ending the game for Time limited Alt Mode
   *
   * @author sistumpf
   */
  private void altGameModeGameOverHandler() {
    ArrayList<Team> teamList = new ArrayList<Team>();
    Stream.of(this.gameState.getTeams())
    .forEach(
        team -> {
          if (team != null) teamList.add(team);
        });
    teamList.sort(
        new Comparator<Team>() {
          public int compare(Team team1, Team team2) {
            return team2.getPieces().length - team1.getPieces().length;
          }
        });
    teamList.removeIf(
        new Predicate<Team>() {
          public boolean test(Team team) {
            return teamList.get(0).getPieces().length > team.getPieces().length;
          }
        });
    this.gameState.setTeams(teamList.toArray(new Team[teamList.size()]));
    setGameOver();
  }

  // **************************************************
  // End of Alt Game Mode Methods
  // **************************************************

  // **************************************************
  // Private Internal Methods
  // **************************************************

  /**
   * Cleans up the GameState after a move was made. Updates the timer if the game uses time limits,
   * Updates the current team and removes it if it got no moves left. Repeats the second step till
   * only 1 team is left or the current team got moves.
   *
   * @author sistumpf
   */
  private void afterMoveCleanup() {
    if (this.moveTimeLimitedGameTrigger) increaseTurnTimer();
    gameState.setCurrentTeam(EngineTools.getNextTeam(gameState));
    if (EngineTools.removeMovelessTeams(gameState)) setGameOver();
  }

  /**
   * Returns true if a move is valid. The move is valid if * the game is not over * the piece
   * belongs to the current team * the move complies with the rules
   *
   * @author sistumpf
   * @param move
   * @return true if the move is valid
   */
  private boolean movePreconditionsMet(Move move) {
    if (isGameOver()) {
      throw new GameOver();
    } else if (gameState.getCurrentTeam()
        != Integer.parseInt(move.getPieceId().split(":")[1].split("_")[0])) {
      throw new InvalidMove();
    } else if (!isValidMove(move)) {
      throw new InvalidMove();
    } else {
      return true;
    }
  }

  /**
   * Ends the game internally by setting the endDate Variable,
   * and externally by starting a Thread that delets the GameSession after 1 minute of waiting.
   *
   * @author rsyed
   * @author sistumpf
   */
  private void setGameOver() {
    this.gameState.setCurrentTeam(-1); // Sets current team to -1 to indicate game has ended
    if (this.endDate == null) {
      this.endDate = new Date();
    }
    this.weDoneZo = true;
    new DeleteInformationThread().start();
  }

  /**
   * Checks if we can start the game. Call this after adding a team to the game
   *
   * @throws GameOver if there are too many pieces compared to the map size
   * @author rsyed
   */
  private void canWeStartTheGameUwU() {
    if (getRemainingTeamSlots() == 0) {
      BoardController boardController = new BoardController(this.gameState);
      // Bases and Blocks are placed in the BordController constructor
      try {
        boardController.initPieces(copyOfTemplate.getPlacement()); // Inits pieces on the grid
      } catch (TooManyPiecesException e) {
        throw new GameOver();
      }
      
      setRandomStartingTeam();
      startAltGameController();
      this.startedDate = new Date();
      
      //inits the current GameState with Names and IDs
      NameIDChanger nidChanger = new NameIDChanger(integerToTeam, teamToInteger);
      this.gameState = nidChanger.putGameStateIDs(gameState);
      this.nameState = nidChanger.putGameStateNames(gameState);
    }
  }

  /**
   * Sets a random team as starting team. From 0 to n
   *
   * @author rsyed
   */
  private void setRandomStartingTeam() {
    this.gameState.setCurrentTeam(random.nextInt(this.gameState.getTeams().length));
  }

  /**
   * Helper method returns HEX Codes for colors
   *
   * @author rsyed
   * @return String containing a randomized color as a HEX Code
   */
  static String getRandColor(Team team) {
    int i = 3;
    int r = pseudoRandomColorInt(team, i--);
    int g = pseudoRandomColorInt(team, i--);
    int b = pseudoRandomColorInt(team, i);
    return Color.rgb(r, g, b).toString();
  }

  /**
   * generates a pseudo random int between 0 and 255.
   *
   * @param team which gets turned into a hashed int
   * @param modifier to modify the random seed.
   * @return random int between 0 and 255
   */
  private static int pseudoRandomColorInt(Team team, int modifier) {
    Random random = new Random(hashTeam(team) * modifier);
    return random.nextInt(256);
  }

  /**
   * turns a team into a hash code
   *
   * @param team
   * @return the teams hash code
   */
  private static int hashTeam(Team team) {
    StringBuilder sb =
        new StringBuilder()
        .append(new Random(team.getId().hashCode() * 256).nextInt(256))
        .append(new Random(team.getPieces().length * 256).nextInt(256));
    Stream.of(team.getPieces())
    .forEach(
        piece ->
        sb.append(new Random(piece.getId().hashCode()).nextInt(team.getPieces().length)));
    return sb.toString().hashCode();
  }

  // **************************************************
  // End of Private Internal Methods
  // **************************************************

  /**
   * After the Game has ended and a certain time in seconds (30) has eclipsed, 
   * the attributes values get cleared to save memory.
   * This happens in case no GameSession delete request has been sent by the client.
   * 
   * @author sistumpf
   */
  private class DeleteInformationThread extends Thread {
    public void run() {
      for(int seconds = 30; seconds > 0; seconds--) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          deleteInfo();
        }
      }
      deleteInfo();
    }
    
    private void deleteInfo() {
      nameState = null;
      startedDate = null;
      endDate = null;
      copyOfTemplate = null;
      gameState = null;
      currentTime = null;
      integerToTeam = null;
      teamToInteger = null;
      random = null;
      gameShouldEndBy = null;
      totalGameTime = null;
      turnEndsBy = null;
      turnTime = null;
      System.gc();
    }
  }
}

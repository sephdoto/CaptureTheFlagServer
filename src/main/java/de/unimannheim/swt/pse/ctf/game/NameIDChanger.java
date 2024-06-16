package de.unimannheim.swt.pse.ctf.game;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Move;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import de.unimannheim.swt.pse.ctf.game.state.Team;

/**
 * Contains Methods to alter GameStates and Moves for switching between Team IDs and Names.
 * 
 * @author sistumpf
 */
public class NameIDChanger {
  private Map<Integer, String> integerToTeam;
  private Map<String, Integer> teamToInteger;
  
  /**
   * Initialized the integerToTeam and teamToInteger HashMaps
   * 
   * @param integerToTeam
   * @param teamToInteger
   */
  public NameIDChanger(
      Map<Integer, String> integerToTeam,
      Map<String, Integer> teamToInteger) {
    this.integerToTeam = integerToTeam;
    this.teamToInteger = teamToInteger;
  }
  
  /**
   * Replaces all Team Names with Integer TeamIDs.
   *
   * @author sistumpf
   */
  public GameState putGameStateIDs(GameState gameState) {
    return deepIDlizeGameState(gameState);
  }
  
  /**
   * Replaces all Integer TeamIDs with team names.
   *
   * @author sistumpf
   */
  public GameState putGameStateNames(GameState gameState) {
    return deepNamalizeGameState(gameState);
  }
  
  /**
   * Replaces a Moves team names with the corresponding IDs.
   * 
   * @param move the Move to replace the name
   * @return true if the Moves team names got changed
   */
  public boolean putMoveIDs(Move move) {
    try {
      String teamName = move.getTeamId();
      int teamID = teamToInteger.get(teamName);
      move.setTeamId("" + teamID);
      move.setPieceId(move.getPieceId().replaceFirst("p:" + teamName, "p:" + teamID));
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  /**
   * Replaces a Moves team IDs with the corresponding names.
   * 
   * @param move the Move to replace the name
   * @return true if the Moves team names got changed
   */
  public boolean putMoveNames(Move move) {
    try {
      int teamID = Integer.parseInt(move.getTeamId());
      String teamName = integerToTeam.get(teamID);
      move.setTeamId(teamName);
      move.setPieceId(move.getPieceId().replaceFirst("p:" + teamID, "p:" + teamName));
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  
  
  /**
   * Deep copies a GameState and adjusts some values to be normalized.
   * The Team id gets changed to represent its place in the Array.
   * The Piece id gets adjusted accordingly in teams Array and on the Grid.
   * The last move gets adjusted accordingly, it is assumed that the newPos indexes get are [y,x].
   *
   * @author sistumpf
   * @return a deep copy and normalized version of a given GameState
   */
  private GameState deepIDlizeGameState(GameState gameState) {
    GameState newState = new GameState();
    newState.setCurrentTeam(gameState.getCurrentTeam());
    String[][] grid = new String[gameState.getGrid().length][];
    for (int i = 0; i < gameState.getGrid().length; i++) {
      grid[i] = new String[gameState.getGrid()[i].length];
      System.arraycopy(gameState.getGrid()[i], 0, grid[i], 0, gameState.getGrid()[i].length);
    }
    newState.setGrid(grid);

    Team[] teams = new Team[gameState.getTeams().length];
    for (int i = 0; i < teams.length; i++) {
      if (gameState.getTeams()[i] == null) continue;
      teams[i] = new Team();
      teams[i].setBase(gameState.getTeams()[i].getBase());
      newState.getGrid()[gameState.getTeams()[i].getBase()[0]][gameState.getTeams()[i].getBase()[1]] = "b:" + i;
      teams[i].setFlags(gameState.getTeams()[i].getFlags());
      teams[i].setId("" + i);
      teams[i].setColor(gameState.getTeams()[i].getColor());
      Piece[] pieces = new Piece[gameState.getTeams()[i].getPieces().length];
      for (int j = 0; j < pieces.length; j++) {
        pieces[j] = new Piece();
        pieces[j].setDescription(gameState.getTeams()[i].getPieces()[j].getDescription());
        pieces[j].setId("p:" + i + "_" + j);
        pieces[j].setTeamId("" + i);
        pieces[j].setPosition(gameState.getTeams()[i].getPieces()[j].getPosition().clone());
        newState.getGrid()[pieces[j].getPosition()[0]][pieces[j].getPosition()[1]] = pieces[j].getId();
      }
      teams[i].setPieces(pieces);
    }
    newState.setTeams(teams);

    if(gameState.getLastMove() != null) {
      Move move = new Move();
      for(int i=0; i<teams.length; i++) {
        if (gameState.getTeams()[i] == null) continue;
        for(int j=0; j<teams[i].getPieces().length; j++)
          if(gameState.getTeams()[i].getPieces()[j].getId().equals(gameState.getLastMove().getPieceId())) {
            move.setTeamId("" + i);
            move.setPieceId(newState.getTeams()[i].getPieces()[j].getId());
            break;
          }
      }
      move.setNewPosition(gameState.getLastMove().getNewPosition().clone());
      newState.setLastMove(move);
    }
    
    return newState;
  }
  
  /**
   * Deep copies a GameState and adjusts some values to be normalized.
   * The Team id gets changed to represent its place in the Array.
   * The Piece id gets adjusted accordingly in teams Array and on the Grid.
   * The last move gets adjusted accordingly, it is assumed that the newPos indexes get are [y,x].
   *
   * @author sistumpf
   * @return a deep copy and normalized version of a given GameState
   */
  private GameState deepNamalizeGameState(GameState gameState) {
    GameState newState = new GameState();
    newState.setCurrentTeam(gameState.getCurrentTeam());
    String[][] grid = new String[gameState.getGrid().length][];
    for (int i = 0; i < gameState.getGrid().length; i++) {
      grid[i] = new String[gameState.getGrid()[i].length];
      System.arraycopy(gameState.getGrid()[i], 0, grid[i], 0, gameState.getGrid()[i].length);
    }
    newState.setGrid(grid);

    Team[] teams = new Team[gameState.getTeams().length];
    for (int i = 0; i < teams.length; i++) {
      String teamName = integerToTeam.get(i);
      if (gameState.getTeams()[i] == null) continue;
      teams[i] = new Team();
      teams[i].setBase(gameState.getTeams()[i].getBase());
      newState.getGrid()[gameState.getTeams()[i].getBase()[0]][gameState.getTeams()[i].getBase()[1]] = "b:" + teamName;
      teams[i].setFlags(gameState.getTeams()[i].getFlags());
      teams[i].setId(teamName);
      teams[i].setColor(gameState.getTeams()[i].getColor());
      Piece[] pieces = new Piece[gameState.getTeams()[i].getPieces().length];
      for (int j = 0; j < pieces.length; j++) {
        pieces[j] = new Piece();
        pieces[j].setDescription(gameState.getTeams()[i].getPieces()[j].getDescription());
        String[] pID = gameState.getTeams()[i].getPieces()[j].getId().split("_");
        pieces[j].setId("p:" + teamName + "_" + pID[pID.length -1]);
        pieces[j].setTeamId(teamName);
        pieces[j].setPosition(gameState.getTeams()[i].getPieces()[j].getPosition().clone());
        newState.getGrid()[pieces[j].getPosition()[0]][pieces[j].getPosition()[1]] = pieces[j].getId();
      }
      teams[i].setPieces(pieces);
    }
    newState.setTeams(teams);

    if(gameState.getLastMove() != null) {
      Move move = new Move();
      for(int i=0; i<teams.length; i++) {
        if (gameState.getTeams()[i] == null) continue;
        for(int j=0; j<teams[i].getPieces().length; j++)
          if(gameState.getTeams()[i].getPieces()[j].getId().equals(gameState.getLastMove().getPieceId())) {
            move.setTeamId(integerToTeam.get(i));
            move.setPieceId(newState.getTeams()[i].getPieces()[j].getId());
            break;
          }
      }
      move.setNewPosition(gameState.getLastMove().getNewPosition().clone());
      newState.setLastMove(move);
    }
    
    return newState;
  }
}

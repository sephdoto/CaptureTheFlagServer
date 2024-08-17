package de.unimannheim.swt.pse.ctf.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Move;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for NameIDChanger. It gets tested if changing names and ids works with GameStates and
 * Moves. It is tested for both ID -> Name and Name -> ID.
 *
 * @author sistumpf
 */
class NameIDChangerTest {
  @Test
  void testWithLastMove() {
    Map<Integer, String> integerToTeam = new HashMap<Integer, String>();
    integerToTeam.put(0, "eins");
    integerToTeam.put(1, "zwei");
    Map<String, Integer> teamToInteger = new HashMap<String, Integer>();
    teamToInteger.put("eins", 0);
    teamToInteger.put("zwei", 1);
    NameIDChanger nidChanger = new NameIDChanger(integerToTeam, teamToInteger);

    GameEngine engine = new GameEngine();
    engine.create(TestValues.getTestTemplate());
    engine.joinGame("eins");
    engine.joinGame("zwei");
    GameState gameState = engine.getCurrentGameState();
    Move move = new Move();
    move.setNewPosition(new int[] {0, 0});
    move.setPieceId(gameState.getTeams()[1].getPieces()[0].getId());
    move.setTeamId("zwei");
    gameState.setLastMove(move);

    System.out.println(gameState.getLastMove().getPieceId());
    GameState newGameState = nidChanger.putGameStateIDs(gameState);
    assertTrue(newGameState.getLastMove().getPieceId().length() > 2);
  }

  @Test
  void testPutGameStateNames() {
    Map<Integer, String> integerToTeam = new HashMap<Integer, String>();
    integerToTeam.put(0, "eins");
    integerToTeam.put(1, "zwei");
    Map<String, Integer> teamToInteger = new HashMap<String, Integer>();
    teamToInteger.put("eins", 0);
    teamToInteger.put("zwei", 1);
    NameIDChanger nidChanger = new NameIDChanger(integerToTeam, teamToInteger);

    GameEngine engine = new GameEngine();
    engine.create(TestValues.getTestTemplate());
    engine.joinGame("eins");
    engine.joinGame("zwei");
    GameState gameState = engine.getCurrentGameState();
    gameState = nidChanger.putGameStateIDs(gameState);

    GameState nameState = nidChanger.putGameStateNames(gameState);
    for (int i = 0; i < nameState.getTeams().length; i++) {
      for (int j = 0; j < gameState.getTeams()[i].getPieces().length; j++)
        assertNotEquals(
            gameState.getTeams()[i].getPieces()[j].getId(),
            nameState.getTeams()[i].getPieces()[j].getId());
      int[] gpo = gameState.getTeams()[i].getBase();
      int[] npo = nameState.getTeams()[i].getBase();
      assertNotEquals(gameState.getGrid()[gpo[0]][gpo[1]], nameState.getGrid()[npo[0]][npo[1]]);
    }
  }

  @Test
  void testPutGameStateIDs() {
    Map<Integer, String> integerToTeam = new HashMap<Integer, String>();
    integerToTeam.put(0, "eins");
    integerToTeam.put(1, "zwei");
    Map<String, Integer> teamToInteger = new HashMap<String, Integer>();
    teamToInteger.put("eins", 0);
    teamToInteger.put("zwei", 1);
    NameIDChanger nidChanger = new NameIDChanger(integerToTeam, teamToInteger);

    GameEngine engine = new GameEngine();
    engine.create(TestValues.getTestTemplate());
    engine.joinGame("eins");
    engine.joinGame("zwei");
    GameState gameState = engine.getCurrentGameState();

    GameState nameState = nidChanger.putGameStateNames(gameState);
    gameState = nidChanger.putGameStateIDs(nameState);
    for (int i = 0; i < nameState.getTeams().length; i++) {
      for (int j = 0; j < gameState.getTeams()[i].getPieces().length; j++)
        assertNotEquals(
            gameState.getTeams()[i].getPieces()[j].getId(),
            nameState.getTeams()[i].getPieces()[j].getId());
      int[] gpo = gameState.getTeams()[i].getBase();
      int[] npo = nameState.getTeams()[i].getBase();
      assertNotEquals(gameState.getGrid()[gpo[0]][gpo[1]], nameState.getGrid()[npo[0]][npo[1]]);
    }
  }

  @Test
  void testPutMoveIDs() {
    Map<Integer, String> integerToTeam = new HashMap<Integer, String>();
    integerToTeam.put(0, "eins");
    integerToTeam.put(1, "zwei");
    Map<String, Integer> teamToInteger = new HashMap<String, Integer>();
    teamToInteger.put("eins", 0);
    teamToInteger.put("zwei", 1);
    NameIDChanger nidChanger = new NameIDChanger(integerToTeam, teamToInteger);

    GameEngine engine = new GameEngine();
    engine.create(TestValues.getTestTemplate());
    engine.joinGame("eins");
    engine.joinGame("zwei");
    GameState gameState = engine.getCurrentGameState();

    Move move = new Move();
    move.setNewPosition(new int[] {0, 0});
    move.setPieceId(gameState.getTeams()[1].getPieces()[0].getId());
    move.setTeamId("zwei");

    assertTrue(nidChanger.putMoveIDs(move));
    assertEquals("p:1_0", move.getPieceId());
    assertEquals("1", move.getTeamId());
  }

  @Test
  void testPutMoveNames() {
    Map<Integer, String> integerToTeam = new HashMap<Integer, String>();
    integerToTeam.put(0, "eins");
    integerToTeam.put(1, "zwei");
    Map<String, Integer> teamToInteger = new HashMap<String, Integer>();
    teamToInteger.put("eins", 0);
    teamToInteger.put("zwei", 1);
    NameIDChanger nidChanger = new NameIDChanger(integerToTeam, teamToInteger);

    GameEngine engine = new GameEngine();
    engine.create(TestValues.getTestTemplate());
    engine.joinGame("eins");
    engine.joinGame("zwei");
    GameState gameState = engine.getCurrentGameState();
    gameState = nidChanger.putGameStateIDs(gameState);

    Move move = new Move();
    move.setNewPosition(new int[] {0, 0});
    move.setPieceId(gameState.getTeams()[1].getPieces()[0].getId());
    move.setTeamId("" + 1);

    assertTrue(nidChanger.putMoveNames(move));
    assertEquals("p:zwei_0", move.getPieceId());
    assertEquals("zwei", move.getTeamId());
  }
}

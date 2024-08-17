package de.unimannheim.swt.pse.ctf.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.unimannheim.swt.pse.ctf.game.map.Directions;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import org.junit.jupiter.api.Test;

class EngineToolsTest {
  /**
   * @author sistumpf
   */
  @Test
  void testRemoveMovelessTeams() {
    GameState gameState = TestValues.getTestState();
    gameState
        .getTeams()[1]
        .getPieces()[0]
        .getDescription()
        .getMovement()
        .setDirections(
            new Directions()); // due to the test GameState, all pieces got the same Direction
                               // Object
    assertTrue(EngineTools.removeMovelessTeams(gameState));
    assertEquals(1, EngineTools.numberOfTeamsLeft(gameState));

    gameState = TestValues.getTestState();
    assertFalse(EngineTools.removeMovelessTeams(gameState));
  }

  /**
   * @author sistumpf
   */
  @Test
  void testNumberOfTeamsLeft() {
    GameState gameState = TestValues.getTestState();
    assertEquals(2, EngineTools.numberOfTeamsLeft(gameState));
    gameState.getTeams()[0] = null;
    assertEquals(1, EngineTools.numberOfTeamsLeft(gameState));
  }

  /**
   * @author sistumpf
   */
  @Test
  void testTeamGotMovesLeft() {
    GameState gameState = TestValues.getTestState();
    gameState.setCurrentTeam(0);
    assertTrue(EngineTools.teamGotMovesLeft(gameState, 0));
    gameState.setCurrentTeam(1);
    assertTrue(EngineTools.teamGotMovesLeft(gameState, 1));

    gameState
        .getTeams()[1]
        .getPieces()[0]
        .getDescription()
        .getMovement()
        .setDirections(
            new Directions()); // due to the test GameState, all pieces got the same Direction
                               // Object
    for (Piece piece : gameState.getTeams()[0].getPieces()) {
      piece.setDescription(TestValues.getTestTemplate().getPieces()[1]);
    }
    assertFalse(EngineTools.teamGotMovesLeft(gameState, 1));
    gameState.setCurrentTeam(0);
    assertTrue(EngineTools.teamGotMovesLeft(gameState, 0));
  }
}

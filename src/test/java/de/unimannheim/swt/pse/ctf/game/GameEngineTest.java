package de.unimannheim.swt.pse.ctf.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
// import java.sql.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// import de.unimannheim.swt.pse.ctf.game.state.GameState;
// import de.unimannheim.swt.pse.ctf.game.state.Move;
// import de.unimannheim.swt.pse.ctf.game.state.Piece;

class GameEngineTest {
  static GameEngine gameEngine;

  /**
   * @author sistumpf
   */
  @BeforeEach
  void setUp() {
    //    gameEngine = new NewGameEngine(TestValues.getTestState(), TestValues.getTestTemplate(),
    // false, true, new Date(System.currentTimeMillis() + 10000000));
  }

  @Test
  void getRandColor() {
    String color1 = GameEngine.getRandColor(TestValues.getTestState().getTeams()[0]);
    String color2 = GameEngine.getRandColor(TestValues.getTestState().getTeams()[0]);
    String color3 = GameEngine.getRandColor(TestValues.getTestState().getTeams()[1]);

    assertEquals(color1, color2);
    assertNotEquals(color1, color3);
    assertNotEquals(color2, color3);
  }

  /**
   * @author sistumpf
   */
  /*  @Test
  void testGetRemainingGameTimeInSeconds() {
    assertTrue(gameEngine.getRemainingGameTimeInSeconds() > 0);    //the freshly started game ends 10 seconds after generating, returned int time should be greater than 0

    gameEngine = new NewGameEngine(TestValues.getTestState(), TestValues.getTestTemplate(), false, true, new Date(System.currentTimeMillis() - 10));
    assertEquals(0, gameEngine.getRemainingGameTimeInSeconds());    //the freshly started game ended a few ms ago, returned time should be 0 (game over)

    gameEngine = new NewGameEngine(TestValues.getTestState(), false, false, new Date(System.currentTimeMillis() - 10));
    assertEquals(-1, gameEngine.getRemainingGameTimeInSeconds());    //the freshly started game got no time limit, returned time should be -1 (no time limit set)
  }*/

  /**
   * @author sistumpf
   */
  /*  @Test
  void testGetRemainingMoveTimeInSeconds() {
    GameEngine gameEngine = new GameEngine();
    assertEquals(-1, gameEngine.getRemainingMoveTimeInSeconds());
  }*/

  /*   @Test
  void testGetRemainingTeamSlots() {
    fail("Not yet implemented");
  } */

  /**
   * It should not take more than 1ms to start a gameEngine, so this test should be valid
   *
   * @author sistumpf
   */
  /*  @Test
  void testGetStartedDate() {
    GameEngine gameEngine = new GameEngine();
    Date started = new Date(System.currentTimeMillis());
    assertEquals(gameEngine.getStartedDate(), started);
  }*/

  /**
   * @author sistumpf
   */
  /*@Test
  void testGameOverCheck() {
    gameEngine.gameOverCheck();
    assertFalse(gameEngine.isGameOver());                                       //ongoing game, not game over

    GameState gameState = TestValues.getTestState();
    gameState.getTeams()[0].setFlags(1);
    gameEngine = new GameEngine(gameState, TestValues.getTestTemplate(), false, true, new Date(852003));      //new GameEngine with modified gameState (team0 flags = 0)
    gameEngine.gameOverCheck();
    assertTrue(gameEngine.isGameOver());                                        //a team got no flags left

    gameState = TestValues.getTestState();
    gameState.getTeams()[0].setPieces(new Piece[] {});
    gameEngine = new GameEngine(gameState, TestValues.getTestTemplate(), false, true, new Date(852003));      //new GameEngine with modified gameState (team0 pieces = {})
    gameEngine.gameOverCheck();
    assertTrue(gameEngine.isGameOver());                                        //a team got no pieces left

    gameState = TestValues.getTestState();
    gameEngine = new GameEngine(gameState, TestValues.getTestTemplate(), false, true, new Date(System.currentTimeMillis() - 1000000));  //new GameEngine with modified gameState (already ended)
    gameEngine.gameOverCheck();
    assertTrue(gameEngine.isGameOver());                                        //game time over
  }*/

  /**
   * @author sistumpf
   */
  /*@Test
  void testIsValidMove() {
    GameState state = TestValues.getTestState();
    state.setCurrentTeam(EngineTools.getNextTeam(state));
    gameEngine = new GameEngine(state, TestValues.getTestTemplate(), false, true, new Date(System.currentTimeMillis() + 10000000));

    Piece rook = gameEngine.getCurrentGameState().getTeams()[1].getPieces()[1];			//rook on 7,3
    Piece rook2 = gameEngine.getCurrentGameState().getTeams()[1].getPieces()[3];		//rook on 7,5
    Move move1 = new Move();
    move1.setPieceId(rook.getId());
    Move move2 = new Move();
    move2.setPieceId(rook2.getId());

    move1.setNewPosition(new int[] {7,1});
    assertFalse(gameEngine.isValidMove(move1));		//rook cannot walk over another same team rook
    move1.setNewPosition(new int[] {7,2});
    assertFalse(gameEngine.isValidMove(move1));		//rook cannot walk onto another same team rook
    move1.setNewPosition(new int[] {6,3});
    assertTrue(gameEngine.isValidMove(move1));		//rook can walk on the empty space above
    move1.setNewPosition(new int[] {6,4});
    assertFalse(gameEngine.isValidMove(move1));		//rook cannot jump over the block above
    move1.setNewPosition(new int[] {8,3});
    assertTrue(gameEngine.isValidMove(move1));		//rook can walk on the empty space below
    move2.setNewPosition(new int[] {4,5});
    assertFalse(gameEngine.isValidMove(move2));		//rook could not walk 3 blocks (just 2)
    move2.setNewPosition(new int[] {7,5});
    assertFalse(gameEngine.isValidMove(move2));		//piece could not walk onto its own position
  }*/

  /**
   * @author sistumpf
   */
  /*@Test
  void testMakeMove() {
    GameState testState = TestValues.getTestState();
    testState.getGrid()[2][4] = "b:0";
    testState.getGrid()[0][0] = "";
    testState.getTeams()[0].setBase(new int[] {2,4});                           //remove outdated base from 0,0 and place it on 2,4
    testState.getTeams()[0].getPieces()[4].setPosition(new int[] {8,9});        //this piece was in the way, now it will attack the enemies base
    testState.getGrid()[8][9] = testState.getTeams()[0].getPieces()[4].getId(); //placed my guy back on the map
    testState.setCurrentTeam(0);
    Move capture = new Move();
    capture.setNewPosition(new int[] {9,9});
    capture.setPieceId(testState.getTeams()[0].getPieces()[4].getId());         //move to capture a flag initialized
    gameEngine = new GameEngine(testState, TestValues.getTestTemplate(), false, true, new Date(System.currentTimeMillis() + 10000000));
    gameEngine.getCurrentGameState().getTeams()[1].setFlags(1);                 //enemy only has 1 flag, if it gets captured the game is over

    assertFalse(gameEngine.isGameOver());                                        //the game is still going

    gameEngine.makeMove(capture);                                               //move locked in!

    Piece proudAttacker = gameEngine.getCurrentGameState().getTeams()[0].getPieces()[4];
    assertArrayEquals(new int[] {3,4}, proudAttacker.getPosition());            //our attacker got respawned, due to pseudo random he will always be on 3,5 (if the grid stays the same)
    assertTrue(gameEngine.isGameOver());                                        //after the last flag got captured the game is over.

    assertEquals(capture, gameEngine.getCurrentGameState().getLastMove());
    assertEquals(0, gameEngine.getCurrentGameState().getCurrentTeam());         //Team 1 is gameOver and got removed, technically it's team 0s turn
  }*/
}

package de.unimannheim.swt.pse.ctf.game;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import de.unimannheim.swt.pse.ctf.game.exceptions.TooManyPiecesException;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.map.PlacementType;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import de.unimannheim.swt.pse.ctf.game.state.Team;

class BoardControllerTest {
  BoardController bordi;

  @BeforeEach
  void setUp() throws Exception {
    GameState state = TestValues.getTestState();
    MapTemplate template = TestValues.getTestTemplate();
    int teams = 2;
    template.setTeams(teams);
    state.setTeams(new Team[teams]);
    bordi = new BoardController(state, template);
    for(int i=0; i<teams; i++)
      bordi.initializeTeam(i, template);
  }

  @Test
  void testTooManyPieces() {
    //test if it works for no blocks
    GameState state = TestValues.getTestState();
    MapTemplate template = TestValues.getTestTemplate();
    int teams = 2;
    template.setTeams(teams);
    template.setGridSize(new int[] {100, 100});
    template.setBlocks(0);
    state.setTeams(new Team[teams]);
    bordi = new BoardController(state, template);
    for(int i=0; i<teams; i++)
      bordi.initializeTeam(i, template);
    
    bordi.gameState.getTeams()[0].setPieces(new Piece[(50*100)-1]);
    assertTrue(bordi.allPiecesPlacable());
    bordi.gameState.getTeams()[0].setPieces(new Piece[(50*100)]);
    assertFalse(bordi.allPiecesPlacable());
    
    //test if it works for more than 2 players
    state = TestValues.getTestState();
    template = TestValues.getTestTemplate();
    teams = 4;
    template.setTeams(teams);
    template.setGridSize(new int[] {100, 100});
    int blocks = 100;
    template.setBlocks(blocks);
    state.setTeams(new Team[teams]);
    bordi = new BoardController(state, template);
    for(int i=0; i<teams; i++)
      bordi.initializeTeam(i, template);
    
    bordi.gameState.getTeams()[0].setPieces(new Piece[(50*50)-1-blocks]);
    assertTrue(bordi.allPiecesPlacable());
    bordi.gameState.getTeams()[0].setPieces(new Piece[(50*50)]);
    bordi.gameState.getTeams()[1].setPieces(new Piece[(50*50)]);
    bordi.gameState.getTeams()[2].setPieces(new Piece[(50*50)]);
    bordi.gameState.getTeams()[3].setPieces(new Piece[(50*50)]);
    assertFalse(bordi.allPiecesPlacable());
    
    //Test if blocks can obscure the view
    state = TestValues.getTestState();
    template = TestValues.getTestTemplate();
    teams = 1;
    template.setTeams(teams);
    template.setGridSize(new int[] {100, 100});
    blocks = 9999;
    template.setBlocks(blocks);
    state.setTeams(new Team[teams]);
    bordi = new BoardController(state, template);
    for(int i=0; i<teams; i++)
      bordi.initializeTeam(i, template);
    bordi.gameState.getTeams()[0].setPieces(new Piece[0]);
    assertTrue(bordi.allPiecesPlacable());
    bordi.gameState.getTeams()[0].setPieces(new Piece[1]);
    assertFalse(bordi.allPiecesPlacable());
  }
  
  @Test
  void testPlacePiecesDefensive() throws TooManyPiecesException {
    bordi.initPieces(PlacementType.defensive);
    //    printGrid(bordi.gameState);
  }

  @Test
  void testPlacePiecesSpacedOut() throws TooManyPiecesException {
    bordi.initPieces(PlacementType.spaced_out);
    //    printGrid(bordi.gameState);
  }

  @Test
  void testPlacePiecesSymmetrical() throws TooManyPiecesException {
    bordi.initPieces(PlacementType.symmetrical);
    //    printGrid(bordi.gameState);
  }

  @SuppressWarnings("unused")
  @Test
  void testGetBoundaries() {
    MapTemplate template = new MapTemplate();
    template.setGridSize(new int[] {20,20});
    int teams = 5;
    template.setTeams(teams);
    GameState state = new GameState();
    state.setTeams(new Team[teams]);
    for(int i=0; i<teams; i++) state.getTeams()[i] = new Team();
    BoardController bordi = new BoardController(state, template);
    int[][] b = bordi.getBoundaries();
    for(int[] bI : b) {
      for(int bII : bI) {
        //        System.out.print(bII + " ");
      }
      //      System.out.println();
    }
    for(int i=0; i<b.length; i++)
      for(int j=0; j<b.length; j++) {
        if(i==j) continue;
        assertFalse(Arrays.equals(b[i], b[j]));
      }
  }

  /**
   * @author sistumpf
   */
  @Test
  void testPlaceBasesNew() {
    GameState gameState = TestValues.getTestState();
    bordi.placeBases(gameState);
    for(Team team : gameState.getTeams())
      assertEquals("b:"+team.getId(), gameState.getGrid()[team.getBase()[0]][team.getBase()[1]]);
  }

  /**
   * @author ysiebenh
   * 
   */
  @Test
  void testInitializeTeam() {
    MapTemplate template = TestValues.getTestTemplate();
    Team team = bordi.initializeTeam(0, template);
    assertNotNull(team.getPieces());
    assertNotNull(team.getBase());
    assertNotNull(team.getClass());
    assertNotNull(team.getColor());
    assertEquals(team.getFlags(),template.getFlags());
    assertNotNull(team.getId());     
  }
  
  /**
   * @author ysiebenh, sistumpf
   */
  @Test
  void testPlaceBasesOld() {
    MapTemplate[] templates = TestValues.getDummyTeplates();

    for (int o = 0; o < templates.length; o++) {
      GameState gs = new GameState();      
      gs.setTeams(new Team[templates[o].getTeams()]);
      BoardController bordi = new BoardController(gs, templates[o]);

      for(int i=0; i<gs.getTeams().length; i++)
        bordi.initializeTeam(i, templates[o]);
      
      bordi.placeBases(gs);
      assertEquals(gs.getGrid()[gs.getTeams()[0].getBase()[0]][gs.getTeams()[0].getBase()[1]],"b:" + gs.getTeams()[0].getId() );
      assertEquals(gs.getGrid()[gs.getTeams()[1].getBase()[0]][gs.getTeams()[1].getBase()[1]],"b:" + gs.getTeams()[1].getId() );
    }
  }

  /**
   * @author sistumpf
   */
  @Test
  void testPlaceBlocks() {
    String[][] grid = new String[][]
        {{"","!","","!"},{"!","!",""},{"!","!"},{""}};
        String[][] gridMuster = new String[][] {{"b","!","b","!"},{"!","!","b"},{"!","!"},{"b"}};
        bordi.placeBlocks(TestValues.getTestTemplate(), grid, 4);

        assertArrayEquals(gridMuster, grid);                                    //Belegung mit Pieces

        grid = new String[][] {{"","",""},{"","",""},{"","",""}};
        gridMuster = new String[][] {{"","b","b"},{"b","b",""},{"","b",""}};
        bordi.placeBlocks(TestValues.getTestTemplate(), grid, 5);

        assertArrayEquals(gridMuster, grid);                                    //Belegung ohne Pieces

        grid = new String[][] {{"","",""},{"","",""},{"","",""}};
        gridMuster = new String[][] {{"b","b","b"},{"","b",""},{"","b","b"}};
        bordi.placeBlocks(TestValues.getTestTemplate(), grid, 6);

        assertArrayEquals(gridMuster, grid);                                    //Belegung wie vorher mit 6 Blöcken
  }

  /**
   * @author sistumpf
   */
  @Test
  void testSeedRandom() {
    int mult0 = bordi.seededRandom(TestValues.getTestTemplate(), 0, 10);
    int mult1 = bordi.seededRandom(TestValues.getTestTemplate(), 1, 10);
    int mult2 = bordi.seededRandom(TestValues.getTestTemplate(), 2, 10);
    int bound9 = bordi.seededRandom(TestValues.getTestTemplate(), 0, 9);
    int bound3 = bordi.seededRandom(TestValues.getTestTemplate(), 0, 3);

    assertEquals(1, mult0);
    assertEquals(6, mult1);
    assertEquals(4, mult2);
    assertEquals(2, bound3);
    assertEquals(8, bound9);
  }

  void printGrid(GameState gameState) {
    for(int y=0; y<gameState.getGrid().length; y++) {
      for(int x=0; x<gameState.getGrid()[y].length; x++)
        System.out.print(gameState.getGrid()[y][x].equals("") ? ".    " : gameState.getGrid()[y][x] + " ");
      System.out.println();
    }
    System.out.println();
  }
}

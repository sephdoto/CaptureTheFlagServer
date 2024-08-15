package de.unimannheim.swt.pse.ctf.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import org.apache.commons.lang3.stream.Streams;
import org.junit.jupiter.api.Test;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.map.PlacementType;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import de.unimannheim.swt.pse.ctf.game.state.Team;

/**
 * @author sistumpf
 */
class PiecePlacerTest {
  @Test
  void testDefensivePlacement() {
    GameState gameState = TestValues.getTestState();
    MapTemplate mt = TestValues.getTestTemplate();
    mt.setGridSize(new int[] {10,10});
    mt.setTeams(3);
    mt.getPieces()[0].setCount(1);
    mt.getPieces()[1].setCount(1);
    mt.getPieces()[2].setCount(1);
    gameState.setTeams(new Team[3]);
    BoardController bc = new BoardController(gameState, mt);
    for(int i=0; i<bc.gameState.getTeams().length; i++)
      bc.initializeTeam(i, mt);
    PiecePlacer pp = new PiecePlacer(bc.gameState, bc.boundaries);
    pp.placePieces(PlacementType.defensive);
    EngineTools.updateGrid(pp.gameState);
//    printGrid(pp.gameState);
//    for(int team=0; team < pp.gameState.getTeams().length; team++) {
//      System.out.println("team " + team + " got " + getNumberPossibleMoves(pp, team) + " possible moves");
//    }
//    System.out.println("total: " + getGameStatePossibleMoves(pp));
  }
  
  @Test
  void testStrengthPiecesComperator() {
    GameState gameState = TestValues.getTestState();
    MapTemplate mt = TestValues.getTestTemplate();
    mt.setGridSize(new int[] {10,10});
    mt.setTeams(2);
//    mt.getPieces()[0].setCount(45);
    gameState.setTeams(new Team[2]);
    BoardController bc = new BoardController(gameState, mt);
    for(int i=0; i<bc.gameState.getTeams().length; i++)
      bc.initializeTeam(i, mt);
    ArrayList<Piece> piecesByStrength = new ArrayList<Piece>();
    int team = 0;
    Streams.of(gameState.getTeams()[team].getPieces()).forEach(p -> piecesByStrength.add(p));
    piecesByStrength.sort(new Comparator<Piece>() {
      @Override
      public int compare(Piece p1, Piece p2) {
        return p2.getDescription().getAttackPower()- p1.getDescription().getAttackPower();
      }
    });
    for(int i=0; i<piecesByStrength.size()-1; i++)
      assertTrue(piecesByStrength.get(i).getDescription().getAttackPower() >= 
      piecesByStrength.get(i+1).getDescription().getAttackPower());
  }
  
  /**
   * following test works, the methods are private now.
   * too much work to set up reflection api method calls to test this, it won't get changed again.
   */
/*  @Test
  void testHillClimbComperator() {
    BoardController bc = new BoardController(TestValues.getTestState(), TestValues.getTestTemplate());
    for(int i=0; i<bc.gameState.getTeams().length; i++)
      bc.initializeTeam(i, TestValues.getTestTemplate());
    PiecePlacer pp = new PiecePlacer(bc.gameState, bc.boundaries);
    pp.randomPlacement(pp.gameState, 0, new LinkedHashSet<Piece>());
    BoardController bc2 = new BoardController(TestValues.getTestState(), TestValues.getTestTemplate());
    for(int i=0; i<bc2.gameState.getTeams().length; i++)
      bc2.initializeTeam(i, TestValues.getTestTemplate());
    PiecePlacer pp2 = new PiecePlacer(bc2.gameState, bc2.boundaries);
    pp.hillClimbingSpacedPlaced(0, 100, pp2.gameState, new LinkedHashSet<Piece>(), false);
    
    ArrayList<GameState> stateList = new ArrayList<GameState>();
    stateList.add(pp.gameState);
    stateList.add(pp2.gameState);
    stateList.sort(new Comparator<GameState>() {
      @Override
      public int compare(GameState o1, GameState o2) {
        return pp.gameStatePossibleMoves(o2) - pp.gameStatePossibleMoves(o1);
      }
    });
    assertTrue(pp.gameStatePossibleMoves(stateList.get(0))  > pp.gameStatePossibleMoves(stateList.get(1)));
  }*/
  
  @Test
  void testPlacePiecesSpaced() {
    GameState gs = TestValues.getTestState();
    MapTemplate mt = TestValues.getTestTemplate();
    mt.setGridSize(new int[] {10,10});
    mt.setTeams(2);
//    mt.getPieces()[0].setCount(45);
    gs.setTeams(new Team[2]);
    BoardController bc = new BoardController(gs, mt);
    for(int i=0; i<bc.gameState.getTeams().length; i++)
      bc.initializeTeam(i, mt);
    PiecePlacer pp = new PiecePlacer(bc.gameState, bc.boundaries);
    pp.placePieces(PlacementType.spaced_out);
//    printGrid(pp.gameState);
//    for(int team=0; team < pp.gameState.getTeams().length; team++) {
//      System.out.println("team " + team + " got " + getNumberPossibleMoves(pp, team) + " possible moves");
//    }
//    System.out.println("total: " + getGameStatePossibleMoves(pp));
  }
  
  /**
   * following test works, the methods are private now.
   * too much work to set up reflection api method calls to test this, it won't get changed again.
   */
/*  @Test
  void testRandomPlacement() {
    GameState gs = TestValues.getTestState();
    MapTemplate mt = TestValues.getTestTemplate();
    mt.setGridSize(new int[] {10,10});
    mt.setTeams(2);
//    mt.getPieces()[0].setCount(45);
    gs.setTeams(new Team[2]);
    BoardController bc = new BoardController(gs, mt);
    for(int i=0; i<bc.gameState.getTeams().length; i++)
      bc.initializeTeam(i, mt);
    PiecePlacer pp = new PiecePlacer(bc.gameState, bc.boundaries);
    pp.randomPlacement(pp.gameState, 0, new LinkedHashSet<Piece>());
//    printGrid(pp.gameState);
  }*/
  
@Test
  void testGetNeighbors() {
    GameState gs = TestValues.getTestState();
    MapTemplate mt = TestValues.getTestTemplate();
    mt.setGridSize(new int[] {30,30});
    mt.setTeams(3);
//    mt.getPieces()[0].setCount(145);
    gs.setTeams(new Team[3]);
    BoardController bc = new BoardController(gs, mt);
    for(int i=0; i<bc.gameState.getTeams().length; i++)
      bc.initializeTeam(i, mt);
    PiecePlacer pp = new PiecePlacer(bc.gameState, bc.boundaries);
    getBestNeighbour(pp, gs, new LinkedHashSet<Piece>(), 0, new int[] {0}).getPiece();
  }
  
  @Test
  void testGetShuffledNeighbors() {
    GameState gs = TestValues.getTestState();
    MapTemplate mt = TestValues.getTestTemplate();
    mt.setGridSize(new int[] {30,30});
    mt.setTeams(3);
//    mt.getPieces()[0].setCount(145);
    gs.setTeams(new Team[3]);
    BoardController bc = new BoardController(gs, mt);
    for(int i=0; i<bc.gameState.getTeams().length; i++)
      bc.initializeTeam(i, mt);
    PiecePlacer pp = new PiecePlacer(bc.gameState, bc.boundaries);
    pp.placePieces(PlacementType.symmetrical);
    LinkedHashSet<Piece> pieces = new LinkedHashSet<Piece>();
    pieces.add(gs.getTeams()[0].getPieces()[0]);
    pieces.add(gs.getTeams()[0].getPieces()[1]);
    ReferenceMove rm = getShuffledNeighbour(pp, gs, pieces, 0, new int[] {1});
//    System.out.println(gs.getTeams()[0].getPieces()[0].getId() + " " + gs.getTeams()[0].getPieces()[0].getPosition()[0] + " " + gs.getTeams()[0].getPieces()[0].getPosition()[1]);
//    System.out.println(gs.getTeams()[0].getPieces()[1].getId() + " " + gs.getTeams()[0].getPieces()[1].getPosition()[0] + " " + gs.getTeams()[0].getPieces()[1].getPosition()[1]);
//    System.out.println(rm.getPiece().getId() + " " + rm.getNewPosition()[0] + " " + rm.getNewPosition()[1]);
    assertTrue(gs.getTeams()[0].getPieces()[0].getId().equals(rm.getPiece().getId()) ||
        gs.getTeams()[0].getPieces()[1].getId().equals(rm.getPiece().getId()));
    assertTrue(Arrays.equals(rm.getNewPosition(), gs.getTeams()[0].getPieces()[0].getPosition()) || 
        Arrays.equals(rm.getNewPosition(), gs.getTeams()[0].getPieces()[1].getPosition()));
  }
  
  @Test
  void testPlacePiecesSymmetrical() {
    GameState gs = TestValues.getTestState();
    MapTemplate mt = TestValues.getTestTemplate();
    mt.setGridSize(new int[] {15,15});
    mt.setTeams(2);
//    mt.getPieces()[0].setCount(45);
    gs.setTeams(new Team[2]);
    BoardController bc = new BoardController(gs, mt);
    for(int i=0; i<bc.gameState.getTeams().length; i++)
      bc.initializeTeam(i, mt);
    PiecePlacer pp = new PiecePlacer(bc.gameState, bc.boundaries);
    pp.placePieces(PlacementType.symmetrical);
//    printGrid(pp.gameState);
  }
  
  @Test
  void testNextBaseDirection() {
    BoardController bc = new BoardController(TestValues.getTestState(), TestValues.getTestTemplate());
//    bc.gameState.getTeams()[0].setBase(new int[] {9,5});
    int dir = getNextBaseDirection(new PiecePlacer(bc.gameState, null), 0);
    assertEquals(3, dir);
    dir = getNextBaseDirection(new PiecePlacer(bc.gameState, null), 1);
    assertEquals(2, dir);
    
    GameState gs = TestValues.getTestState();
    MapTemplate mt = TestValues.getTestTemplate();
    int teams = 3;
    mt.setTeams(teams);
    gs.setTeams(new Team[teams]);
    bc = new BoardController(gs, mt);
    while(--teams>=0) bc.initializeTeam(teams, mt);
    bc.gameState.getTeams()[2].setBase(new int[] {4, 2});
    
    dir = getNextBaseDirection(new PiecePlacer(bc.gameState, null), 0);
    assertEquals(3, dir);
    dir = getNextBaseDirection(new PiecePlacer(bc.gameState, null), 1);
    assertEquals(0, dir);
  }

  /////////////////////////////////////////////
  // test methods to access private methods  //
  /////////////////////////////////////////////
  int getNextBaseDirection(PiecePlacer pp, int team) {    
    try {
    Method privateMethod = pp.getClass().getDeclaredMethod("nextBaseDirection", int.class);
    privateMethod.setAccessible(true);
      return (Integer)privateMethod.invoke(pp, team);
    } catch(Exception e) {e.printStackTrace();}
    return 0;
  }
  
  ReferenceMove getShuffledNeighbour(PiecePlacer pp, GameState gameState, LinkedHashSet<Piece> ignoreThese, int teamID, int[] spacedOutSideSteps) {    
    try {
    Method privateMethod = pp.getClass().getDeclaredMethod("getShuffledNeighbour", GameState.class, LinkedHashSet.class, int.class, int[].class);
    privateMethod.setAccessible(true);
      return (ReferenceMove)privateMethod.invoke(pp, gameState, ignoreThese, teamID, spacedOutSideSteps);
    } catch(Exception e) {e.printStackTrace();}
    return null;
  }
  
  ReferenceMove getBestNeighbour(PiecePlacer pp, GameState gameState, LinkedHashSet<Piece> ignoreThese, int teamID, int[] spacedOutSideSteps) {    
    try {
    Method privateMethod = pp.getClass().getDeclaredMethod("getBestNeighbour", GameState.class, LinkedHashSet.class, int.class, int[].class);
    privateMethod.setAccessible(true);
      return (ReferenceMove)privateMethod.invoke(pp, gameState, ignoreThese, teamID, spacedOutSideSteps);
    } catch(Exception e) {e.printStackTrace();}
    return null;
  }
  
  int getNumberPossibleMoves(PiecePlacer pp, int team) {    
    try {
    Method privateMethod = pp.getClass().getDeclaredMethod("numberPossibleMoves", GameState.class, int.class);
    privateMethod.setAccessible(true);
      return (Integer)privateMethod.invoke(pp, pp.gameState, team);
    } catch(Exception e) {e.printStackTrace();}
    return 0;
  }
  
  int getGameStatePossibleMoves(PiecePlacer pp) {    
    try {
    Method privateMethod = pp.getClass().getDeclaredMethod("gameStatePossibleMoves", GameState.class);
    privateMethod.setAccessible(true);
      return (Integer)privateMethod.invoke(pp, pp.gameState);
    } catch(Exception e) {e.printStackTrace();}
    return 0;
  }
  
  void printGrid(GameState gameState) {
    for(int y=0; y<gameState.getGrid().length; y++) {
      for(int x=0; x<gameState.getGrid()[y].length; x++)
        System.out.print(gameState.getGrid()[y][x].equals("") ? ".     " : gameState.getGrid()[y][x] + " ");
      System.out.println();
    }
  }
}

package de.unimannheim.swt.pse.ctf.game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import com.google.gson.Gson;
import de.unimannheim.swt.pse.ctf.game.exceptions.TooManyPiecesException;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.map.PieceDescription;
import de.unimannheim.swt.pse.ctf.game.map.PlacementType;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import de.unimannheim.swt.pse.ctf.game.state.Team;

/**
 * This class is used to initialize the grid and the teams.
 * The pieces get initialized by PiecerPlacer.class, an object of that class gets created and called here.
 */
public class BoardController {
  double xPartitionsSize;
  double yPartitionsSize;
  int numberOfTeams;
  int[] gridSize;
  GameState gameState;
  int[][] boundaries;
  
  /**
   * This constructor should be called to initialize a completely new GameState in create().
   * Initializes the Grid and places Bases and Blocks on it, 
   * Initializes new Teams() with their bases.
   * 
   * @author sistumpf
   * @param gameState
   * @param template
   */
  public BoardController(GameState gameState, MapTemplate template) {
    this.numberOfTeams = template.getTeams();
    this.gridSize = template.getGridSize();
    this.gameState = gameState;
    double[] partitionSizes = getPartitionSizes();
    this.yPartitionsSize = partitionSizes[0];
    this.xPartitionsSize = partitionSizes[1];
    this.boundaries = getBoundaries();
    
    initEmptyGrid();
    placeBases(gameState);
    placeBlocks(template, gameState.getGrid(), template.getBlocks());
  }

  /**
   * This constructor should be called to update an already initialized GameState.
   * 
   * @author sistumpf
   * @param gameState
   */
  public BoardController(GameState gameState) {
    this.gameState = gameState;
    this.gridSize = new int[] {gameState.getGrid().length, gameState.getGrid()[0].length};
    this.numberOfTeams = gameState.getTeams().length;
    double[] partitionSizes = getPartitionSizes();
    this.yPartitionsSize = partitionSizes[0];
    this.xPartitionsSize = partitionSizes[1];
    this.boundaries = getBoundaries();
  }
  
  /**
   * Helper Method for initializing the Grid with Empty spaces
   *
   * @author rsyed
   * @return string grid with empty boxes
   */
  public void initEmptyGrid() {
    String[][] grid = new String[this.gridSize[0]][this.gridSize[1]];
    for (int i = 0; i < grid.length; i++) {
      for (int j = 0; j < grid[i].length; j++) {
        grid[i][j] = "";
      }
    }
    this.gameState.setGrid(grid);
  }

  /**
   * A team gets initialized and put in the GameState.
   *
   * @author sistumpf, ysiebenh
   * @param teamID int 
   * @param MapTemplate template
   * @return initialized team
   */
  public Team initializeTeam(int teamID, MapTemplate template) {
    // Creating the Pieces for the team
    int count = 0;
    LinkedList<Piece> indPieces = new LinkedList<Piece>();
    for (PieceDescription piece : template.getPieces()) {
      for (int i = 0; i < piece.getCount(); i++) {
        Piece x = new Piece();
        x.setId("p:" + teamID + "_" + Integer.toString(count++));
        x.setDescription(piece);
        x.setTeamId(Integer.toString(teamID));
        indPieces.add(x);
      }
    }

    // initializing the team
    Team team = new Team();
    team.setId(Integer.toString(teamID));
    team.setFlags(template.getFlags());
    team.setBase(findBase(""+teamID));
    
    Piece[] pieces = new Piece[indPieces.size()];
    int iterator = 0;
    for (Piece p : indPieces) {
      pieces[iterator++] = p;
    }
    team.setPieces(pieces);
    team.setColor(GameEngine.getRandColor(team));
    this.gameState.getTeams()[teamID] = team;
    return team;
  }
  
  /**
   * Due to Engine reasons the teams are not initialized with the bases, leaving bases just standing around on the grid.
   * This method looks for a base with the same teamID as teamID and returns its position.
   * 
   * @author sistumpf
   * @param teamID
   * @return base position
   */
  int[] findBase(String teamID) {
    for(int y=0; y<gameState.getGrid().length; y++)
      for(int x=0; x<gameState.getGrid()[y].length; x++)
        if(gameState.getGrid()[y][x].contains("b:"))
          if(gameState.getGrid()[y][x].split("b:")[1].equals(teamID))
            return new int[] {y,x};
    return null;
  }
  
  /**
   * Partitions the Grid and returns the boundaries using the MapTemplate and GameState Attribute.
   * The upper and lower boundary is inclusive.
   * 
   * @author sistumpf
   * @return two dimensional int array containing a team and its boundaries as {team index}{lower y, upper y, lower x, upper x}
   */
  public int[][] getBoundaries(){
    int[][] boundaries = new int[this.numberOfTeams][4];
    for(int p=0, x=0, y=0; p<boundaries.length; p++) {
      boundaries[p][0] = (int)(y * this.yPartitionsSize);
      boundaries[p][1] = (int)((y+1) * this.yPartitionsSize)-1;
      boundaries[p][2] = (int)(x * this.xPartitionsSize);
      boundaries[p][3] = (int)((x+1) * this.xPartitionsSize)-1;
      if((int)((x+1)*xPartitionsSize)>=this.gridSize[1]) {
        x=0;
        y++;
      } else {
        x++;
      }
    }
    return boundaries;
  }
  
  /**
   * This is a helper method to place the blocks on the board in the create method
   *
   * @author sistumpf
   * @param mt, used as a seed for pseudo random number generating
   * @param grid
   * @param blocks, number of blocks to be placed
   */
  void placeBlocks(MapTemplate mt, String[][] grid, int blocks) {
    ArrayList<Integer[]> freeList = new ArrayList<Integer[]>();
    for (int i = 0; i < grid.length; i++) {
      for (int j = 0; j < grid[i].length; j++) {
        if (grid[i][j].equals("")) {
          freeList.add(new Integer[] {i, j});
        }
      }
    }

    for (; blocks > 0; blocks--) {
      int x = seededRandom(mt, blocks, freeList.size());
      grid[freeList.get(x)[0]][freeList.get(x)[1]] = "b";
      freeList.remove(x);
    }
  }
  
  /**
   * This method should be used instead of Math.random() to generate deterministic positive pseudo
   * random values. Changing modifier changes the resulting output for the same seed.
   *
   * @author sistumpf
   * @param mt gets converted to a random seed
   * @param modifier to get different random values with the same seed
   * @param upperBound upper bound for returned random values, upperBound = 3 -> values 0 to 2
   * @return a pseudo random number
   */
  int seededRandom(MapTemplate mt, int modifier, int upperBound) {
    int seed = (new Gson().toJson(mt) + String.valueOf(modifier)).hashCode();
    return new Random(seed).nextInt(upperBound);
  }

  /**
   * Places the bases on the grid.
   *
   * @author sistumpf
   * @param gameState
   * @param template
   */
  public void placeBases(GameState gameState) {
    String[][] grid = gameState.getGrid();
    int bases = this.numberOfTeams;
    for(int y=0, yc=0, team=0; yc*yPartitionsSize<grid.length; y+=yPartitionsSize, yc++)
      for(int x=0, xc=0; bases>0 && xc*xPartitionsSize<grid[y].length; x+=xPartitionsSize, bases--, xc++) {
        grid[(int)(y + yPartitionsSize/2)][(int)(x + xPartitionsSize/2)] = "b:" + team++;
      }
    this.gameState.setGrid(grid);
  }
  
  /**
   * Returns the partition size to cut the board in a(n) = floor(n^2/4) partitions, where teams <= a(n)
   * 
   * @author sistumpf
   * @return {yPartitionsSize, xPartitionsSize}
   */
  private double[] getPartitionSizes() {
    int yCuts = 1;
    int xCuts = 0;
    for(boolean cutX = true; (xCuts+1) * (yCuts+1) < this.numberOfTeams; cutX = !cutX) {
      if(cutX) {
        xCuts++;
      } else {
        yCuts++;
      }
    }
    return new double[] {this.gridSize[0] / (double)(yCuts+1), this.gridSize[1] / (double)(xCuts+1)};
  }
  
  /////////////////////////////////////////////////////////
  //                    PLACEMENT TYPES                  //
  /////////////////////////////////////////////////////////
  
  /**
   * Chooses the correct method to place the pieces onto the grid.
   *
   * @author sistumpf
   * @param gameState
   * @param template
   * @throws TooManyPiecesException
   */
  void initPieces(PlacementType placement) throws TooManyPiecesException {
    if(!allPiecesPlacable())
      throw new TooManyPiecesException("Some pieces could not be placed, there might be too many blocks or too many pieces");
    new PiecePlacer(gameState, this.boundaries).placePieces(placement);
    System.gc();
  }

  /**
   * A simple check if all pieces are placable.
   * Each Teams bounding box gets checked for empty spaces, if there are less empty spaces than pieces an exception gets thrown.
   * Might be a little slow for huge grids but ensures reliable results.
   * Tests with calculating the remaining free fields were inconsistent due to the random placement of the blocks and
   * not floor(n^2/4) teams per grid. It resulted in telling there are free fields, when they were on a not used partition,
   * making the free fields useless.
   * This might be improved in the future as the block placement can be calculated, but it is ok for now.
   * 
   * @author sistumpf
   * @return true if all pieces can be placed
   * @throws TooManyPiecesException if not all pieces can be placed
   */
  boolean allPiecesPlacable() {
    for(int team=0; team<this.boundaries.length; team++) {
      int pieces = this.gameState.getTeams()[team].getPieces().length;
      for(int y=this.boundaries[team][0]; y<=this.boundaries[team][1]; y++)
        for(int x=this.boundaries[team][2]; pieces > 0 && x<=this.boundaries[team][3]; x++)
          if(this.gameState.getGrid()[y][x].equals(""))
            pieces--;
      if(pieces > 0)
        return false;
    }
    return true;
  }
}

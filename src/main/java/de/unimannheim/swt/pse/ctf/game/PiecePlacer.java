package de.unimannheim.swt.pse.ctf.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import de.unimannheim.swt.pse.ctf.game.map.PlacementType;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import de.unimannheim.swt.pse.ctf.game.state.Team;

/**
 * This class places pieces on a grid.
 * The blocks and bases should already be initialized, 
 * all teams should be present in the GameState,
 * all their pieces should also be there.
 * @author sistumpf
 */
public class PiecePlacer {
  int spacedOutSideSteps = 10;
  int spacedOutRepetitions = Runtime.getRuntime().availableProcessors();
  GameState gameState;
  //boundaries = a teams rectangular partition of the map, in those boundaries the starter pieces will be placed
  int[][] boundaries;
  //directions: 
  // int[0-3] =    where the team is "facing", if it is 0 - left; 1 - right; 2 - up; 3 - down
  // int[0-3][] =  positions for GameUtilities.updatePos, encoded as mid-left-right
  // int[4-7] =    where the team is "facing", if it is 4 - left; 5 - right; 6 - up; 7 - down
  // int[4-7][] =  positions for GameUtilities.updatePos, encoded as left-right
  int[][] directions = new int[][] {{0,6,4},{1,5,7},{2,4,5},{3,7,6},
    {3,2},{2,3},{0,1},{1,0}};

    /**
     * This Constructor needs a gameState and the team boundaries, the gameState will be altered if placePieces is called.
     * @param gameState
     * @param boundaries
     */
    public PiecePlacer(GameState gameState, int[][] boundaries) {
      this.gameState = gameState;
      this.boundaries = boundaries;
    }

    /**
     * This method shall be called to place the Pieces on the grid.
     * 
     * @author sistumpf
     * @param placement
     */
    public void placePieces(PlacementType placement) {
      switch (placement) {
        case symmetrical:
          placePiecesSymmetrical();
          break;
        case spaced_out:
          placePiecesSpaced(new LinkedHashSet<Piece>(), false);
          break;
        case defensive:
          placePiecesDefensive();
          break;
      }
    }

    /**
     * Places the pieces symmetrical on the grid and in the arrays.
     * The teams are facing the nearest other team,
     * the first three pieces get placed in that direction,
     * then pieces get placed at the right/left side of the base until the boundaries are hit,
     * then the pieces get placed around the base, similar to respawnPiece.
     *
     * @author sistumpf
     */
    private void placePiecesSymmetrical() {
      for (int n=0; n<gameState.getTeams().length; n++) {
        Team team = gameState.getTeams()[n];
        int facing = nextBaseDirection(n);
        int maxPieces = team.getPieces().length;
        int pieces = team.getPieces().length;
        
        //place first 3 pieces in front of base
        for (int i=0; maxPieces - pieces < maxPieces && i<3; i++) {
          int[] newPos = posInFrontOfBase(team.getBase(), facing, i);
          if(safeToPlace(n, newPos)) placePiece(team, maxPieces - pieces--, newPos);
        }
        
        //place to the sides of the base, till boundaries are hit
        for(int boundsHit=0, i=0; boundsHit<2 && maxPieces - pieces < maxPieces; i++) {
          int[] newPos = GameUtilities.updatePos(team.getBase().clone(), this.directions[4+facing][i%2], i/2);
          if(safeToPlace(n, newPos)) {
            placePiece(team, maxPieces-pieces--, newPos);
            boundsHit = 0;
          }else if(positionOutOfBounds(n, newPos)) {
            boundsHit++;
          }
        }
        
        //place remaining pieces with respawn piece logic
        while(maxPieces-pieces<maxPieces) {
          int[] newPos = respawnPiecePosition(team.getBase(), facing);
          if(safeToPlace(n, newPos)) placePiece(team, maxPieces-pieces--, newPos);
        }
      }
    }

    /**
     * Places the Pieces on the Board using a standard hill-climbing algorithm to ensure that every
     * piece has the maximum amount of possible moves.
     * Here the hill climbing algorithm gets called multiple times to ensure the best result.
     * How often it's called depends on the processors cores.
     * The GameState with the most possible moves is chosen to stay, its values are written to this.GameState
     *
     * @author sistumpf
     * @param strongestPieces contains a list of pieces which will be the only affected/not affected pieces by the hill climbing algorithm
     * @param shuffle states if the pieces in strongestPieces will be modified or not. true = only the pieces in strongestPieces swap their positions, false = normal spaced out placement
     */
    private void placePiecesSpaced(LinkedHashSet<Piece> strongestPieces, boolean shuffle) {
      ExecutorService executorService = Executors.newFixedThreadPool(spacedOutRepetitions);
      List<Callable<GameState>> tasks = new LinkedList<>();
      for (int i = 0; i < this.spacedOutRepetitions; i++) {
        final int mod = i;
        tasks.add(
            () -> {
              return hillClimbingSpacedPlaced((Integer.MAX_VALUE / this.spacedOutRepetitions) * mod, mod * this.spacedOutSideSteps, EngineTools.deepCopyGameState(gameState), strongestPieces, shuffle);
            });
      }
      ArrayList<GameState> stateList = new ArrayList<GameState>();
      try {
        List<Future<GameState>> futures = executorService.invokeAll(tasks);
        for(int i=0; i<futures.size(); i++)
          stateList.add(futures.get(i).get());
      } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
      for(int i=0; i<stateList.size(); i++)
        stateList.sort(new Comparator<GameState>() {
          @Override
          public int compare(GameState g1, GameState g2) {
            return gameStatePossibleMoves(g2) - gameStatePossibleMoves(g1);
          }
        });

      this.gameState.setGrid(stateList.get(0).getGrid());
      this.gameState.setTeams(stateList.get(0).getTeams());
      executorService.shutdown();
    }

    /**
     * Placed the pieces on the board, so that the strongest ones are right around the base.
     * Those pieces are swapped by a modified spaced out algorithm to ensure their maximum mobility.
     * All other pieces are placed with the spaced out algorithm.
     * 
     * @author sistumpf
     */
    private void placePiecesDefensive() {
      LinkedHashSet<Piece> strongestPieces = new LinkedHashSet<Piece>();
      for(int n=0; n<gameState.getTeams().length; n++) {
        Team team = gameState.getTeams()[n];
        ArrayList<Piece> piecesByStrength = new ArrayList<Piece>();
        piecesByStrength.addAll(Arrays.asList(team.getPieces()));
        piecesByStrength.sort(new Comparator<Piece>() {
          @Override
          public int compare(Piece p1, Piece p2) {
            return p2.getDescription().getAttackPower() - p1.getDescription().getAttackPower();
          }
        });
        //place strongest 8 pieces around the base
        for(int i=0; i<piecesByStrength.size(); i++) {
          if(i<8) {
            int[] newPos = respawnPiecePosition(team.getBase(), this.nextBaseDirection(n));
            piecesByStrength.get(i).setPosition(newPos);
            gameState.getGrid()[newPos[0]][newPos[1]] = piecesByStrength.get(i).getId();
            strongestPieces.add(piecesByStrength.get(i));
          } else {
            piecesByStrength.get(i).setPosition(new int[] {-1,-1});
          }
        }
      }
      //shuffle pieces
      placePiecesSpaced(strongestPieces, true);
      //place remaining pieces spaced out
      placePiecesSpaced(strongestPieces, false);
    }

    ////////////////////////////////////////////////////
    //        additional helper methods               //
    ////////////////////////////////////////////////////
    /**
     * The hill climbing algorithm for spaced out placement.
     * 
     * @author ysiebenh, sistumpf
     * @param randomModifier used to generate different pseudo-random numbers
     * @param steps how many side steps are allowed for hill climbing
     * @param gameState to modify using the hill climbing algorithm
     * @param strongestPieces contains a list of pieces which will be the only affected/not affected pieces by the hill climbing algorithm
     * @param shuffle states if the pieces in strongestPieces will be modified or not. true = only the pieces in strongestPieces swap their positions, false = normal spaced out placement
     * @return gameState modified by the hill climbing algorithm
     */
    private GameState hillClimbingSpacedPlaced(int randomModifier, int steps, GameState gameState, LinkedHashSet<Piece> strongestPieces, boolean shuffle) {
      strongestPieces = updateReferences(gameState, strongestPieces);
      if(!shuffle)
        randomPlacement(gameState, randomModifier, strongestPieces);
      for (int n=0; n<gameState.getTeams().length; n++) {
        int[] sideSteps = new int[] {steps};
        for(ReferenceMove bestNeighbour = getBestNeighbour(gameState, strongestPieces, shuffle, n, sideSteps);
            bestNeighbour.getPiece() != null;
            bestNeighbour = getBestNeighbour(gameState, strongestPieces, shuffle, n, sideSteps)) {
          if(gameState.getGrid()[bestNeighbour.getNewPosition()[0]][bestNeighbour.getNewPosition()[1]].equals("")) {
            gameState.getGrid()[bestNeighbour.getNewPosition()[0]][bestNeighbour.getNewPosition()[1]]
                = bestNeighbour.getPiece().getId();
            gameState.getGrid()[bestNeighbour.getPiece().getPosition()[0]][bestNeighbour.getPiece().getPosition()[1]]
                = "";
            bestNeighbour.getPiece().setPosition(bestNeighbour.getNewPosition());
          } else {
            int[] oldPos = bestNeighbour.getPiece().getPosition();
            int[] newPos = bestNeighbour.getNewPosition();
            Piece occupant =
                Arrays.stream(
                    gameState.getTeams()[n]
                        .getPieces())
                .filter(p -> p.getId().equals(gameState.getGrid()[newPos[0]][newPos[1]]))
                .findFirst()
                .get();
            bestNeighbour.getPiece().setPosition(newPos);
            occupant.setPosition(oldPos);
            gameState.getGrid()[newPos[0]][newPos[1]] = bestNeighbour.getPiece().getId();
            gameState.getGrid()[oldPos[0]][oldPos[1]] = occupant.getId();
          }
        }
      }
      return gameState;
    }

    /**
     * After deep copying a gameState, the piece references in ignoreThese are not correct anymore.
     * This method "updates" the references by creating a new LinkedSet with the right references.
     * 
     * @author sistumpf
     * @param gameState gameState with references that will be put into strongestPieces. They are found by comparing PieceIDs
     * @param strongestPieces contains a list of pieces which will be the only affected/not affected pieces by the hill climbing algorithm
     * @return strongestPieces but with references from gameState
     */
    private LinkedHashSet<Piece> updateReferences(GameState gameState, LinkedHashSet<Piece> strongestPieces){
      LinkedHashSet<Piece> newIgnores = new LinkedHashSet<Piece>();
      for(Piece piece : strongestPieces) {
        int team = Integer.parseInt(piece.getId().split(":")[1].split("_")[0]);
        for(Piece compare : gameState.getTeams()[team].getPieces())
          if(piece.getId().equals(compare.getId())) {
            newIgnores.add(compare);
          }
      }
      return newIgnores;
    }

    /**
     * Returns the best neighbour based on possible moves.
     * Depending on shuffle, the best neighbour is either the truly best neighbour or the best neighbour with pieces from strongestPieces shuffled positions.
     * This method chooses the right bestNeighbour algorithm accordingly.
     *
     * @author sistumpf
     * @param GameState to analyze
     * @param strongestPieces contains a list of pieces which will be the only affected/not affected pieces by the hill climbing algorithm
     * @param shuffle states if the pieces in strongestPieces will be modified or not. true = only the pieces in strongestPieces swap their positions, false = normal spaced out placement
     * @param teamID for Team to analyze
     * @param spacedOutSideSteps sideSteps the algorithm is allowed to take
     * @return move that leads to the best neighbour
     */
    private ReferenceMove getBestNeighbour(GameState gameState, LinkedHashSet<Piece> strongestPieces, boolean shuffle, int teamID, int[] spacedOutSideSteps) {
      if(!shuffle)
        return getBestNeighbour(gameState, strongestPieces, teamID, spacedOutSideSteps);
      return getShuffledNeighbour(gameState, strongestPieces, teamID, spacedOutSideSteps);
    }


    /**
     * This best neighbour algorithm only swaps the pieces positions from shuffleThese to ensure their mobility.
     * 
     * @author sistumpf
     * @param gameState to analyze and calculate on
     * @param shuffleThese contains only the pieces which will be swapped.
     * @param teamID for Team to analyze
     * @param spacedOutSideSteps sideSteps the algorithm is allowed to take
     * @return the best piece-swap to get more possible moves
     */
    private ReferenceMove getShuffledNeighbour(GameState gameState, LinkedHashSet<Piece> shuffleThese, int teamID, int[] spacedOutSideSteps) {
      gameState.setCurrentTeam(teamID);
      ReferenceMove bestMove = new ReferenceMove(null, new int[] {0,0});
      int bestPossibleMoves = numberPossibleMoves(gameState, teamID);
      for(Piece piece : shuffleThese) {
        int[] oldPos = piece.getPosition().clone();
        for(Piece other : shuffleThese) {
          if(piece == other ||
              Integer.parseInt(piece.getTeamId()) != teamID ||
              !piece.getTeamId().equals(other.getTeamId())) continue;
          int[] newPos = other.getPosition().clone();
          /* The only uses for grid in EngineTools is to check what occupies a position and if an opponent is weaker.
           * Switching two pieces of the same team changes nothing, so the grid doesn't need to be modified here. */
          piece.setPosition(newPos);
          other.setPosition(oldPos);
          int currentPossibleMoves = numberPossibleMoves(gameState, teamID);
          if(currentPossibleMoves > bestPossibleMoves) {
            bestMove.setNewPosition(newPos);
            bestMove.setPiece(piece);
            bestPossibleMoves = currentPossibleMoves;
          } else if (currentPossibleMoves == bestPossibleMoves && spacedOutSideSteps[0] > 0) {
            bestMove.setNewPosition(newPos);
            bestMove.setPiece(piece);
            bestPossibleMoves = currentPossibleMoves;
            spacedOutSideSteps[0]--;
          }
          piece.setPosition(oldPos);
          other.setPosition(newPos);
        }
      }
      return bestMove;
    }

    /**
     * This best neighbour algorithm tries to find the position with the most possible moves by repositioning one piece onto a free field.
     * Pieces from ignoreThese are ignored and won't be repositioned.
     * 
     * @author ysiebenh, sistumpf
     * @param gameState to analyze and calculate on
     * @param ignoreThese contains pieces which will not be repositioned.
     * @param teamID for Team to analyze
     * @param spacedOutSideSteps sideSteps the algorithm is allowed to take
     * @return the best move to get more possible moves
     */
    private ReferenceMove getBestNeighbour(GameState gameState, LinkedHashSet<Piece> ignoreThese, int teamID, int[] spacedOutSideSteps) {
      boolean skipSomePieces = ignoreThese.size() > 0;
      gameState.setCurrentTeam(teamID);
      ReferenceMove bestMove = new ReferenceMove(null, new int[] {0,0});
      int bestPossibleMoves = numberPossibleMoves(gameState, teamID);
      for (Piece piece : gameState.getTeams()[teamID].getPieces()) {
        if(skipSomePieces)
          if(ignoreThese.contains(piece))
            continue;
        int[] oldPos = piece.getPosition().clone();
        for (int y = boundaries[teamID][0]; y <= boundaries[teamID][1]; y++) {
          for (int x = boundaries[teamID][2]; x <= boundaries[teamID][3]; x++) {
            if (gameState.getGrid()[y][x].equals("")) {
              gameState.getGrid()[y][x] = piece.getId();
              gameState.getGrid()[oldPos[0]][oldPos[1]] = "";
              piece.setPosition(new int[] {y, x});
              int currentPossibleMoves = numberPossibleMoves(gameState, teamID);
              if(currentPossibleMoves > bestPossibleMoves) {
                bestMove.setNewPosition(new int[] {y,x});
                bestMove.setPiece(piece);
                bestPossibleMoves = currentPossibleMoves;
              } else if (currentPossibleMoves == bestPossibleMoves && spacedOutSideSteps[0] > 0) {
                bestMove.setNewPosition(new int[] {y,x});
                bestMove.setPiece(piece);
                bestPossibleMoves = currentPossibleMoves;
                spacedOutSideSteps[0]--;
              }
              gameState.getGrid()[oldPos[0]][oldPos[1]] = piece.getId();
              gameState.getGrid()[y][x] = "";
              piece.setPosition(oldPos);
            }
          }
        }
      }
      return bestMove;
    }

    /**
     * Calculates the number of possible moves to be made on a GameState.
     * 
     * @author sistumpf
     * @param gameState to calculate all teams possible moves on
     * @return number of possible moves from a given GameState
     */
    private int gameStatePossibleMoves(GameState gameState) {
      int moves = 0;
      for(int teamId = 0; teamId < gameState.getTeams().length; teamId++)
        moves += numberPossibleMoves(gameState, teamId);
      return moves;
    }

    /**
     * Calculates the number of possible moves a team is able to make.
     * 
     * @author sistumpf
     * @param gameState
     * @param teamId
     * @return number of possible moves
     */
    private int numberPossibleMoves(GameState gameState, int teamId) {
      int moves = 0;
      gameState.setCurrentTeam(teamId);
      for(Piece p : gameState.getTeams()[teamId].getPieces())
        moves += EngineTools.getPossibleMoves(gameState, p).size();
      return moves;
    }

    /**
     * Returns a valid position on which a Piece can be placed on around the base.
     * The positions get checked in circles, so like an onion the pieces get placed in layers around the base.
     * Only if a layer is filled, the next layer gets looked at.
     *
     * @author sistumpf
     * @param basePos the position of the base from the Piece that gets respawned
     * @param facing 0-3 indicating if the team is facing left,right,up or down.
     * @return valid position to place a piece on.
     */
    private int[] respawnPiecePosition(int[] basePos, int facing) {
      int[] xTransforms;
      int[] yTransforms;

      for (int distance = 1; distance < gameState.getGrid().length; distance++) {
        xTransforms = EngineTools.fillXTransformations(new int[distance * 8], distance);
        yTransforms = EngineTools.fillYTransformations(new int[distance * 8], distance);
        int arrayPosModifier = 
            facing == 0 ? 3*(xTransforms.length/4) : 
              facing == 1 ? xTransforms.length/4 : 
                facing == 3 ? 2*(xTransforms.length/4) : 
                  0;

        for (int clockHand = 0; clockHand < distance * 8; clockHand++) {
          int team = Integer.parseInt(gameState.getGrid()[basePos[0]][basePos[1]].split(":")[1]);
          int x = basePos[1] + xTransforms[clockHand];
          int y = basePos[0] + yTransforms[clockHand];
          int[] newPos = new int[] {y, x};
          if (positionOutOfBounds(team, newPos)) continue;

          if (EngineTools.emptyField(gameState.getGrid(), newPos)) {
            for (int i = 0; i<xTransforms.length; i++) {
              x = basePos[1] + xTransforms[(i+arrayPosModifier) % xTransforms.length];
              y = basePos[0] + yTransforms[(i+arrayPosModifier) % xTransforms.length];
              newPos = new int[] {y, x};
              if (positionOutOfBounds(team, newPos)) continue;
              if (EngineTools.emptyField(gameState.getGrid(), newPos)) return newPos;
            }
          }
        }
      }
      return null;
    }

    /**
     * places a piece, given by its index in the team.pieces array, on the position newPos.
     * 
     * @author sistumpf
     * @param team the team to take the piece from
     * @param piece the pieces place in team array
     * @param newPos the new position to place the piece on
     */
    private void placePiece(Team team, int piece, int[] newPos) {
      team.getPieces()[piece].setPosition(newPos);
      gameState.getGrid()[newPos[0]][newPos[1]] = team.getPieces()[piece].getId();
    }

    /**
     * Checks if a position is not out of bounds and the position is empty (no pieces/bases/grids)
     * 
     * @author sistumpf
     * @param newPos a position to check if a piece could be placed there
     * @return true if a piece can be placed safely on the new position
     */
    private boolean safeToPlace(int team, int[] newPos) {
      return !positionOutOfBounds(team, newPos) && 
          GameUtilities.emptyField(gameState.getGrid(), newPos);
    }

    /**
     * Checks if a position is not contained in the teams partition, using this.boundaries
     *    -> {team index}{lower y, upper y, lower x, upper x}
     *    
     * @author sistumpf
     * @param team index
     * @param pos position
     * @return true if the position is out of bounds
     */
    private boolean positionOutOfBounds(int team, int[] pos) {
      return (pos[0] < this.boundaries[team][0] || pos[1] < this.boundaries[team][2] ||
          pos[0] > this.boundaries[team][1] || pos[1] > this.boundaries[team][3]);
    }

    /**
     * Returns the 3 positions in front of the base, which one depends on midLeftRight
     * 
     * @author sistumpf
     * @param base position
     * @param facing : in what direction the team is facing:  0 - left; 1 - right; 2 - up; 3 - down
     * @param midLeftRight : if the position up (0), up-left(1) or up-right(2) is targetted
     * @return updated position
     */
    private int[] posInFrontOfBase(int[] base, int facing, int midLeftRight) {
      return GameUtilities.updatePos(base.clone(), this.directions[facing][midLeftRight], 1);
    }

    /**
     * Returns the direction with the shortest distance to a teams base.
     *
     * @author sistumpf
     * @param ourBase
     * @param enemyBase
     * @return 0 - left; 1 - right; 2 - up; 3 - down
     */
    private int nextBaseDirection(int n) {
      int[] ourBase = gameState.getTeams()[n].getBase();
      HashMap<Double, Integer> distances = new HashMap<Double, Integer>();
      for(int team=0; team<gameState.getTeams().length; team++) {
        if(team == n) continue;
        int[] enemyBase = gameState.getTeams()[team].getBase();
        distances.put(Math.sqrt(Math.pow(ourBase[1] - enemyBase[1], 2) + Math.pow(ourBase[0] - enemyBase[0], 2)), team);
        //      System.out.println("Distance to team " + team + " = " + Math.sqrt(Math.pow(ourBase[1] - enemyBase[1], 2) + Math.pow(ourBase[0] - enemyBase[0], 2)));
      }
      DoubleSummaryStatistics stats = distances.keySet().stream().collect(Collectors.summarizingDouble(Double::doubleValue));
      int nearestTeam = distances.get(stats.getMin());
      //    System.out.println("nearest team: " + nearestTeam + ", base pos: " + gameState.getTeams()[nearestTeam].getBase()[0] + 
      //    "," + gameState.getTeams()[nearestTeam].getBase()[1]);
      int xDifference = ourBase[1] - gameState.getTeams()[nearestTeam].getBase()[1];
      int yDifference = ourBase[0] - gameState.getTeams()[nearestTeam].getBase()[0];
      if(Math.abs(xDifference) > Math.abs(yDifference))
        return xDifference > 0 ? 0 : 1;
        return yDifference > 0 ? 2 : 3;
    }

    /**
     * Places the pieces on the board randomly using the {@link EngineTools#seededRandom(String[][], int, int, int) seededRandom} method
     *
     * @author ysiebenh, sistumpf
     * @param gameState to place the pieces on
     * @param modifier to modify the seeded random
     * @param ignoreThese contains pieces which will not be touched by the random placing algorithm. They remain in their old position.
     */
    private void randomPlacement(GameState gameState, int modifier, LinkedHashSet<Piece> ignoreThese) {
      boolean skipSomePieces = ignoreThese.size() > 0;
      for (int team=0; team<gameState.getTeams().length; team++) {
        for (Piece p : gameState.getTeams()[team].getPieces()) {
          if(skipSomePieces)
            if(ignoreThese.contains(p))
              continue;
          int newY = 0;
          int newX = 0;
          do {
            newY = EngineTools.seededRandom(gameState.getGrid(), modifier++, boundaries[team][1]+1, boundaries[team][0]);
            newX = EngineTools.seededRandom(gameState.getGrid(), 1 - modifier++, boundaries[team][3]+1, boundaries[team][2]);
          } while (!gameState.getGrid()[newY][newX].equals(""));
          p.setPosition(new int[] {newY, newX});
          gameState.getGrid()[newY][newX] = p.getId();
        }
      }
    }
}
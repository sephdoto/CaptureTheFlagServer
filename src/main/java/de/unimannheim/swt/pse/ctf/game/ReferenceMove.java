package de.unimannheim.swt.pse.ctf.game;

import java.util.Arrays;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Move;
import de.unimannheim.swt.pse.ctf.game.state.Piece;

/**
 * This class represents a move as the Move class.
 * The moving Piece is saved by its reference, not its Id, making finding the moved piece easier.
 * @author sistumpf
 */
public class ReferenceMove {

  private Piece piece;
  private int[] newPosition;

  /**
   * Default constructor to initialize a ReferenceMove.
   * @param piece
   * @param newPos
   */
  public ReferenceMove(Piece piece, int[] newPos) {
    this.piece = piece;
    this.newPosition = newPos;
  }

  /**
   * Constructor to initialize a ReferenceMove from a Move.
   * The Piece gets taken from a gameState which gets searched for the PieceID in move.
   * @param gameState
   * @param move
   */
  public ReferenceMove(GameState gameState, Move move) {
    if(move.getPieceId() != null) {
      this.newPosition = move.getNewPosition();
      this.piece =
          Arrays.stream(
              gameState.getTeams()[Integer.parseInt(move.getPieceId().split(":")[1].split("_")[0])]
                  .getPieces())
          .filter(p -> p.getId().equals(move.getPieceId()))
          .findFirst()
          .get();
    }
  }
  
  /**
   * Converts a ReferenceMove back to a normal Move.
   * @return Move representing this ReferenceMove.
   */
  public Move toMove() {
    Move move = new Move();
    move.setNewPosition(this.newPosition);
    if(this.piece != null)
      move.setPieceId(this.piece.getId());
    return move;
  }

  public Piece getPiece() {
    return piece;
  }

  public void setPiece(Piece piece) {
    this.piece = piece;
  }

  public int[] getNewPosition() {
    return newPosition;
  }

  public void setNewPosition(int[] newPosition) {
    this.newPosition = newPosition;
  }
}
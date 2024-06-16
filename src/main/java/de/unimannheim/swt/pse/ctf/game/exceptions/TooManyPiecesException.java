package de.unimannheim.swt.pse.ctf.game.exceptions;
/**
 * Represents an internal exception thrown when the Number of Pieces are more than the slots on the board.
 * @author ysiebenh 
 */
public class TooManyPiecesException extends Exception {
  
  public TooManyPiecesException (String info) {
    super(info); 

    }
}

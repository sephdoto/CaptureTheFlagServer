package de.unimannheim.swt.pse.ctf.game.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Represents a special exception (game is over) that is marked with a HTTP status if thrown. */
@ResponseStatus(value = HttpStatus.GONE, reason = "Game is over")
public class GameOver extends RuntimeException {

  private static final long serialVersionUID = 7614481697480725513L;
}

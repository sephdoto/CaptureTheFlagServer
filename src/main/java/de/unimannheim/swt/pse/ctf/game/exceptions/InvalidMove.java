package de.unimannheim.swt.pse.ctf.game.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Represents a special exception (move request is invalid) that is marked with a HTTP status if
 * thrown.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Move is invalid")
public class InvalidMove extends RuntimeException {

  private static final long serialVersionUID = -6954155582582082740L;
}

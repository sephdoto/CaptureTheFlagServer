package de.unimannheim.swt.pse.ctf.game.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Represents a special exception (move request is forbidden for current team) that is marked with a
 * HTTP status if thrown.
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Move is forbidden for current team")
public class ForbiddenMove extends RuntimeException {

  private static final long serialVersionUID = -4418309672823987152L;
}

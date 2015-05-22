package nl.yenlo.ddld.importing.exceptions;

import java.io.IOException;

/**
 * 
 * @author Philipp Gayret
 *
 */
public class ImportException extends IOException {

	private static final long serialVersionUID = 1L;

	public ImportException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImportException(String message) {
		super(message);
	}

}

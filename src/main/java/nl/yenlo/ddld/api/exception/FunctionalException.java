package nl.yenlo.ddld.api.exception;

/**
 * This is an {@link Exception} that you can throw anywhere whenever a functional error occurs.
 * 
 * @author Philipp Gayret
 */
public class FunctionalException extends RuntimeException {

	private final Kind kind;

	/**
	 * Functional errors.
	 * 
	 * The name of every {@link Kind} of error is used as an identifier; they can be references from JavaScript sources on error handling.
	 */
	public enum Kind {

		REQUIRES_LOGIN("Je bent momenteel niet ingelogd."), //
		IMPORT_SECRET_UNKNOWN("De import secret is incorrect."), //
		IMPORT_SOURCE_UNKNOWN("Er bestaat geen bron voor de opgegeven naam."), // 
		NO_ACTIVE_FACTCHECKS("Er is geen factcheck actief"), //
		LOGIN_PASSWORD_INCORRECT("Het opgegeven wachtwoord is niet correct."), //
		LOGIN_EMAIL_UNKNOWN("Er is geen gebruiker geregistreerd voor dat email adres."), //
		REGISTRATION_EMAIL_DUPLICATE("Er is al een gebruiker geregistreerd op dit email adres."), //
		FACTCHECK_UNKNOWN("Er is geen factcheck voor het opgegeven id."), //
		UNKNOWN_SEARCH_ENGINE("De opgegeven zoekmachine is onbekend."), //
		UNKNOWN_ARTICLE_SOURCE("De opgegeven artikel bron is onbekend."), //
		RESOURCE_NOT_FOUND("Wat je zoekt is hier niet te vinden."), //
		INVALID_AUTHORIZATION("Authorization http header kon niet worden herkend."), //
		CRAWLER_UNKNOWN("Er is geen crawler voor het opgegeven id.");

		private String message;

		public String getMessage() {
			return this.message;
		}

		private Kind(String message) {
			this.message = message;
		}

	}

	private static final long serialVersionUID = 1L;

	public FunctionalException(Kind kind) {
		super(kind.message);
		this.kind = kind;
	}

	public Kind getKind() {
		return this.kind;
	}

}

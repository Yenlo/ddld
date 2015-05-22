package nl.yenlo.ddld.api.exception;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * Handles all the exceptions, FunctionalException will be presented to the user
 * in a fancy way, everything else will be regarded as an internal server error.
 *
 * @author Philipp Gayret
 */
@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {

    private static final String FUNCTIONAL = "functional";
    private static final String TECHNICAL = "technical";

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);
    private static final ObjectMapper MAPPER;
    // jBoss is throwing its own RestEasy NotFoundException which is deprecated,
    // there's also standard ones, so we solve it by just comparing for the name of the thing.
    private static final String NOT_FOUND_EXCEPTION_NAME = "NotFoundException";

    static {
        MAPPER = new ObjectMapper();
        MAPPER.writerWithDefaultPrettyPrinter();
    }

    /**
     * An object that we can convert to JSON.
     *
     * @author Philipp Gayret
     */
    public static class ErrorObject implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String identifier;
        private final String message;
        private final String type;

        public ErrorObject(String type, String identifier, String message) {
            super();
            this.identifier = identifier;
            this.message = message;
            this.type = type;
        }

        public String getIdentifier() {
            return this.identifier;
        }

        public String getMessage() {
            return message;
        }

        public String getType() {
            return type;
        }

    }

    /**
     * Creates a new {@link Response}, containing an {@link ErrorObject}.
     *
     * @param status     http status
     * @param type       error object type
     * @param identifier error object identifier
     * @param message    error object message
     * @return response
     * @throws IOException
     */
    private Response jsonError(Status status, String type, String identifier, String message) throws IOException {
        ErrorObject object = new ErrorObject(type, identifier, message);
        String json = MAPPER.writeValueAsString(object);
        return Response.serverError().entity(json).type(MediaType.APPLICATION_JSON).status(status).build();
    }

    @Override
    public Response toResponse(Exception e) {
        try {
            if (e.getClass().getSimpleName().equals(NOT_FOUND_EXCEPTION_NAME)) {
                LOG.info("Page not found: " + e.getMessage());
                return jsonError(Status.NOT_FOUND, FUNCTIONAL, FunctionalException.Kind.RESOURCE_NOT_FOUND.name(), FunctionalException.Kind.RESOURCE_NOT_FOUND.getMessage());
            } else if (e instanceof FunctionalException) {
                FunctionalException fe = (FunctionalException) e;
                LOG.warn("Functional error: " + e.getMessage());
                return jsonError(Status.BAD_REQUEST, FUNCTIONAL, fe.getKind().name(), fe.getMessage());
            } else {
                LOG.error("Internal server error.", e);
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                e.printStackTrace(printWriter);
                return jsonError(Status.INTERNAL_SERVER_ERROR, TECHNICAL, "TECHNICAL_ERROR", stringWriter.toString());
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("Unable to serialize to JSON", ioe);
        }
    }

}

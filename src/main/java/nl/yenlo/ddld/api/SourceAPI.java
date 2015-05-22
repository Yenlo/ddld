package nl.yenlo.ddld.api;

import nl.yenlo.ddld.db.SourceDAO;
import nl.yenlo.ddld.db.impl.SourceDAOImpl;
import nl.yenlo.ddld.model.Source;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Allows for custom document input.
 *
 * @author Philipp Gayret
 */
@Path("/source/")
public class SourceAPI {

    private final SourceDAO sourceDao = new SourceDAOImpl();

    /**
     * We are consuming this format in the method below.
     */
    public static class PuttableSource {
        public String name;
        public String secret;
    }

    /**
     * Saves a given source.
     *
     * @param puttableSource the source
     */
    @POST
    @Path("/")
    @Produces("application/json")
    @Consumes("application/json")
    public void put(PuttableSource puttableSource) {
        Source source = new Source();
        source.setName(puttableSource.name);
        source.setSecret(puttableSource.secret);
        sourceDao.save(source);
    }

}

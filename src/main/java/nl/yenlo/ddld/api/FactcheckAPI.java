package nl.yenlo.ddld.api;

import nl.yenlo.ddld.api.exception.FunctionalException;
import nl.yenlo.ddld.db.DocumentDAO;
import nl.yenlo.ddld.db.FactcheckDAO;
import nl.yenlo.ddld.db.NoMatchException;
import nl.yenlo.ddld.db.UserDAO;
import nl.yenlo.ddld.db.impl.FactcheckDAOImpl;
import nl.yenlo.ddld.db.impl.UserDAOImpl;
import nl.yenlo.ddld.model.Factcheck;
import nl.yenlo.ddld.model.User;
import nl.yenlo.ddld.model.search.BasicDocument;
import org.elasticsearch.index.query.BoolQueryBuilder;

import javax.ws.rs.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * @author Philipp Gayret
 */
@Path("/factcheck/")
public class FactcheckAPI {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private final FactcheckDAO factcheckDao = new FactcheckDAOImpl();
    private final UserDAO userDao = new UserDAOImpl();
    private final DocumentDAO documentDb = DocumentDAO.getInstance();

    @GET
    @Path("/")
    @Produces("application/json")
    public Collection<Factcheck> list() {
        User user = AuthenticationFilter.getUser();
        return factcheckDao.get(user);
    }

    /**
     * We are consuming this format in the method below.
     */
    public static class PuttableFactcheck {
        public String name;
    }

    /**
     * Creates a new factcheck.
     *
     * @param puttableFactcheck the factcheck
     */
    @POST
    @Path("/")
    @Produces("application/json")
    @Consumes("application/json")
    public void put(PuttableFactcheck puttableFactcheck) {
        User user = AuthenticationFilter.getUser();
        Factcheck factcheck = new Factcheck();
        factcheck.setName(puttableFactcheck.name);
        factcheck.setOwner(user);
        factcheckDao.save(factcheck);
    }

    /**
     * Sets the user's active factcheck.
     *
     * @param factcheckId the factcheck id
     * @throws NoMatchException
     */
    @GET
    @Path("/{factcheckId}/use")
    @Produces("application/json")
    public void use(@PathParam("factcheckId") Integer factcheckId) throws NoMatchException {
        User user = AuthenticationFilter.getUser();
        Factcheck factcheck = factcheckDao.get(user, factcheckId);
        user.setActiveFactcheck(factcheck);
        userDao.save(user);
    }

    /**
     * Deletes a factcheck and all the documents that come with it.
     *
     * @param factcheckId the factcheck id
     * @throws NoMatchException
     */
    @GET
    @Path("/{factcheckId}/delete")
    @Produces("application/json")
    public void delete(@PathParam("factcheckId") final Integer factcheckId) throws NoMatchException {
        User user = AuthenticationFilter.getUser();
        Factcheck factcheck = factcheckDao.get(user, factcheckId);
        Factcheck active = user.getActiveFactcheck();
        if (active != null && factcheck.getId() == active.getId()) {
            user.setActiveFactcheck(null);
        }
        userDao.save(user);
        factcheckDao.delete(factcheck);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                BoolQueryBuilder qb = boolQuery().must(termQuery("db_factcheck_id", factcheckId));
                Iterator<? extends BasicDocument> docs = documentDb.search(qb);
                while (docs.hasNext()) {
                    BasicDocument doc = docs.next();
                    documentDb.delete(doc.getId());
                }
            }
        });
    }

    /**
     * Returns the user's active factcheck.
     *
     * @throws FunctionalException
     */
    @GET
    @Path("/active")
    @Produces("application/json")
    public Integer active() throws FunctionalException {
        User user = AuthenticationFilter.getUser();
        return user.getActiveFactcheckOrErr().getId();
    }

}

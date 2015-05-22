package nl.yenlo.ddld.api;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import nl.yenlo.ddld.api.exception.FunctionalException;
import nl.yenlo.ddld.db.NoMatchException;
import nl.yenlo.ddld.db.UserDAO;
import nl.yenlo.ddld.db.impl.UserDAOImpl;
import nl.yenlo.ddld.model.User;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.UUID;

/**
 * Allows for login checks and registration of users.
 *
 * @author Philipp Gayret
 */
@Path("/session/")
public class SessionAPI {

    private final UserDAO userDao = new UserDAOImpl();
    private final HashFunction hashFunction = Hashing.sha512();

    /**
     * We are consuming this format in the method below.
     */
    public static class LoginUser {
        public String email;
        public String password;

        public void check() {
            if (email == null)
                email = "";
            if (password == null)
                password = "";
        }
    }

    /**
     * @param password the password
     * @param salt     the salt
     * @return hash of the given password / salt combination
     */
    private String buildHash(String password, String salt) {
        Hasher hasher = hashFunction.newHasher();
        hasher.putString(password, Charsets.UTF_8);
        hasher.putString(salt, Charsets.UTF_8);
        return hasher.hash().toString();
    }

    /**
     * Binds a user object to the session if given the correct credentials.
     *
     * @param loginUser the user
     */
    @POST
    @Path("/login")
    @Produces("application/json")
    @Consumes("application/json")
    public void login(LoginUser loginUser) {
        loginUser.check();
        try {
            User user = userDao.get(loginUser.email);
            String hash = this.buildHash(loginUser.password, user.getSalt());
            if (!hash.equals(user.getPassword())) {
                throw new FunctionalException(FunctionalException.Kind.LOGIN_PASSWORD_INCORRECT);
            }
        } catch (NoMatchException e) {
            throw new FunctionalException(FunctionalException.Kind.LOGIN_EMAIL_UNKNOWN);
        }
    }

    /**
     * We are consuming this format in the method below.
     */
    public static class RegisterUser {
        public String email;
        public String password;

        public void check() {
            if (email == null)
                email = "";
            if (password == null)
                password = "";
        }
    }

    /**
     * Checks if the given email is not already registered, if the email is free we set the salt and password and bind the user object to the session.
     *
     * @param registerUser the user
     */
    @POST
    @Path("/register")
    @Produces("application/json")
    @Consumes("application/json")
    public void register(RegisterUser registerUser) {
        registerUser.check();
        if (!userDao.exists(registerUser.email)) {
            User user = new User();
            user.setEmail(registerUser.email);
            user.setSalt(UUID.randomUUID().toString());
            user.setPassword(this.buildHash(registerUser.password, user.getSalt()));
            userDao.save(user);
        } else {
            throw new FunctionalException(FunctionalException.Kind.REGISTRATION_EMAIL_DUPLICATE);
        }
    }

}

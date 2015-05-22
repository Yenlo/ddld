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
import org.jboss.resteasy.util.Base64;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

/**
 * Maintains a {@link ThreadLocal} {@link User} by looking at the incoming request's Authorization header.
 *
 * @author Philipp Gayret
 */
public class AuthenticationFilter implements Filter {

    private static final ThreadLocal<User> requestUsers = new ThreadLocal<User>();

    private final HashFunction hashFunction = Hashing.sha512();
    private final UserDAO userDao = new UserDAOImpl();

    public static User getUserOrNull() {
        return requestUsers.get();
    }

    /**
     * Returns the current request's {@link User}.
     * If none exists a {@link FunctionalException.Kind#REQUIRES_LOGIN} is thrown.
     *
     * @return current request's user
     */
    public static User getUser() {
        User user = requestUsers.get();
        if (user == null) {
            throw new FunctionalException(FunctionalException.Kind.REQUIRES_LOGIN);
        } else {
            return user;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public User findUser(String login, String password) {
        try {
            User user = userDao.get(login);
            String hash = this.buildHash(password, user.getSalt());
            if (hash.equals(user.getPassword())) {
                return user;
            } else {
                throw new FunctionalException(FunctionalException.Kind.LOGIN_PASSWORD_INCORRECT);
            }
        } catch (NoMatchException e) {
            throw new FunctionalException(FunctionalException.Kind.LOGIN_EMAIL_UNKNOWN);
        }
    }

    public User getUser(String authorizationHeader) throws UnsupportedEncodingException, IOException {
        StringTokenizer tokenizer = new StringTokenizer(authorizationHeader);
        if (tokenizer.hasMoreTokens()) {
            String kind = tokenizer.nextToken();
            if (kind.equalsIgnoreCase("Basic")) {
                String credentials = new String(Base64.decode(tokenizer.nextToken()), "UTF-8");
                int separatorPosition = credentials.indexOf(":");
                if (separatorPosition != -1) {
                    String login = credentials.substring(0, separatorPosition).trim();
                    String password = credentials.substring(separatorPosition + 1).trim();
                    return findUser(login, password);
                } else {
                    throw new FunctionalException(FunctionalException.Kind.INVALID_AUTHORIZATION);
                }
            } else {
                throw new FunctionalException(FunctionalException.Kind.INVALID_AUTHORIZATION);
            }
        } else {
            throw new FunctionalException(FunctionalException.Kind.INVALID_AUTHORIZATION);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authorization = httpRequest.getHeader("Authorization");
        if (authorization != null) {
            User user = this.getUser(authorization);
            requestUsers.set(user);
        }
        chain.doFilter(request, response);
        // clear the threadlocal; threads are reused for other requests in jBoss / RestEasy
        requestUsers.set(null);
    }

    /**
     * @param password password
     * @param salt     salt
     * @return hash of the given password / salt combination
     */
    private String buildHash(String password, String salt) {
        Hasher hasher = hashFunction.newHasher();
        hasher.putString(password, Charsets.UTF_8);
        hasher.putString(salt, Charsets.UTF_8);
        return hasher.hash().toString();
    }

    @Override
    public void destroy() {
    }

}
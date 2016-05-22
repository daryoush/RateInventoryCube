package com.mehrsoft.dao;

import com.github.davidmoten.rx.jdbc.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created by ijet on 5/12/16.
 */
public class BaseDao {
    private final static Logger log = LoggerFactory.getLogger(BaseDao.class);
    // see http://www.nailedtothex.org/roller/kyle/tags/tomcat
     Context context;
     DataSource ds;
     Database db;

    @PostConstruct
    public void init() throws ServletException {
        try {
            log.debug("Init BaseDao");
            context = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/jdbc/postgres");
            db = Database
                    .fromDataSource(ds);
            log.debug("Datasource " + ds);
        } catch (NamingException e) {
            throw new ServletException(e);
        }
    }


    public static <T> Optional<T> resolve(Supplier<T> resolver) {
        try {
            T result = resolver.get();
            return Optional.ofNullable(result);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

    private <T> T nullOrValue(Optional<T> x) {
        if (x.isPresent()) return x.get();
        else return null;
    }

    public static <T> T resolveToNullOrVal(Supplier<T> resolver) {
        try {
            return resolver.get();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static void printSQLException(SQLException ex) {

        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (true) {
//                    ignoreSQLException(
//                        ((SQLException)e).
//                                getSQLState()) == false) {

                    e.printStackTrace(System.err);
                    System.err.println("SQLState: " +
                            ((SQLException) e).getSQLState());

                    System.err.println("Error Code: " +
                            ((SQLException) e).getErrorCode());

                    System.err.println("Message: " + e.getMessage());

                    Throwable t = ex.getCause();
                    while (t != null) {
                        System.out.println("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }
}

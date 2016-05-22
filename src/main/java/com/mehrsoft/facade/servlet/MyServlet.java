package com.mehrsoft.facade.servlet;

import com.mehrsoft.service.MyService;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by ijet on 5/6/16.
 */
@javax.inject.Singleton
public class MyServlet extends HttpServlet {

    @javax.inject.Inject
    private MyService myService;

    // see http://www.nailedtothex.org/roller/kyle/tags/tomcat
    private Context context;
    private DataSource dataSource;




    @Override
    public void init() throws ServletException {
        try {
            context = new InitialContext();
            dataSource = (DataSource) context.lookup("java:comp/env/jdbc/postgres");
        } catch (NamingException e) {
            throw new ServletException(e);
        }
    }
    @Override
    public void destroy() {
        if (context != null) {
            try {
                context.close();
            } catch (NamingException e) {
                // no-op
            }
        }
    }


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection cn;
        try {
             cn = dataSource.getConnection();

        } catch (SQLException e) {
            throw new ServletException(e);
        }
        resp.getWriter().write(myService.hello("Guice") + cn);
    }
}
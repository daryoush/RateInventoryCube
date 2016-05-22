package com.mehrsoft.dao;

import com.codahale.metrics.annotation.Timed;
import com.mehrsoft.metrics.MetricsService;
import com.mehrsoft.model.MyTestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by ijet on 5/10/16.
 */
public class MyDao  extends BaseDao {

    private final static Logger log = LoggerFactory.getLogger(MyDao.class);



    String databaseTableName = "public.mytesttable";


    @Inject
    MetricsService myMetricsService;

    @Timed(name = "mydao.getall")
    public Observable<MyTestData> getAllMyTestData() {

        log.debug("Daa getAllMyTestData -- timed");

        String q = "select i, c from mytesttable where i > :mini;";

        return db.select(q)
                .parameter("mini", 10)
                .autoMap(MyTestData.class);

    }

    @Timed(name = "mydao.save")
    public void saveMyTestData(MyTestData x) {
        // TODO  http://blog.shinetech.com/2007/08/04/how-to-close-jdbc-resources-properly-every-time/
                Connection con = null;

                try {
                    con = ds.getConnection();
                    con.setAutoCommit(false);

                    // TODO make the ? to  be names
                    String stmt = "insert into "
                            + databaseTableName
                            + "  as A ( i,c)"
                            + " values (?,?)  "
                            + " ON CONFLICT  (i)  "
                            + " DO update set "
                            + " (c "
                            + ") = "
                            +"( "
                            + "  COALESCE(?, A.c) "
                            +")";

                    PreparedStatement updateStatement = null;


                    try {
                        updateStatement = con.prepareStatement(stmt);
                        int i = 1;

                        //Values
                        updateStatement.setObject(i++, resolveToNullOrVal(() -> x.getI()));
                        updateStatement.setObject(i++, resolveToNullOrVal(() -> x.getC()));

                        //Conflict   -- TODO use names for variables
                        updateStatement.setObject(i++, resolveToNullOrVal(() -> x.getC()));
                        updateStatement.executeUpdate();
                        con.commit();

                    } catch (SQLException e) {
                        printSQLException(e);

                        if (con != null) {
                            try {
                                System.err.print("Transaction is being rolled back");
                                con.rollback();
                            } catch (SQLException excep) {
                                printSQLException(e);

                                excep.printStackTrace();
                            }
                        }

                    } finally {
                        if (updateStatement != null) {
                            updateStatement.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        try {
                            con.setAutoCommit(true);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                con.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }


    }

}

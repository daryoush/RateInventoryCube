package com.mehrsoft.service;

import com.mehrsoft.dao.MyDao;
import com.mehrsoft.model.MyTestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by ijet on 5/6/16.
 */
public class MyServiceImpl implements MyService {

    private final static Logger log = LoggerFactory.getLogger(MyServiceImpl.class);
    @Inject
    MyDao myDao;
    @Override
    public String hello(String name) {
        return "Hello, " + name;
    }

    //Daos return observable
    // Service manipilates observables
    // Service returns the final object that is required by the
    // facade
    // Service may have methods that return observables if one service
    // is used in another
    @Override
    public List<MyTestData> getAllTestDataFromService() {
        log.debug("Service getAllTestDataFromService");
        return myDao.getAllMyTestData().toList().toBlocking().first();
    }

    @Override
    public void saveTestDataOnService(MyTestData x) {
         myDao.saveMyTestData(x);
    }
}

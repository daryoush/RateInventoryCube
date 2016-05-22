package com.mehrsoft.facade.soap;

import com.mehrsoft.service.MyService;
import com.mehrsoft.model.MyTestData;

import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.List;

/**
 * Created by ijet on 5/10/16.
 */
@WebService
public class TestSoapFacade {

    @Inject
    MyService myService;

    @WebMethod
    public void addNumbers(MyTestData x) {
        myService.hello("from add");
    }

    @WebMethod
    public List<MyTestData> getAllSoap() {
        return myService.getAllTestDataFromService();
    }


}


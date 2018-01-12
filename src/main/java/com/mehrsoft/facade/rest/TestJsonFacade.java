package com.mehrsoft.facade.rest;

import com.mehrsoft.service.MyService;
import com.mehrsoft.model.MyTestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by ijet on 5/10/16.
 */
@Path("testjson")
public class TestJsonFacade {
    private final static Logger log = LoggerFactory.getLogger(TestJsonFacade.class);
    @Inject
    MyService myService;

    @GET
    @Path("getAll")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MyTestData>  getAll() {

        log.debug("JSON Facade getAll");
        return  myService.getAllTestDataFromService();
    }


    @POST
    @Path("save")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public int saveTestData(MyTestData x) {
        myService.saveTestDataOnService(x);
        return x.getI();
    }
}

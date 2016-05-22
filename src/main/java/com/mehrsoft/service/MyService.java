package com.mehrsoft.service;

import com.mehrsoft.model.MyTestData;

import java.util.List;

/**
 * Created by ijet on 5/6/16.
 */
public interface MyService {
    String hello(String name);

    List<MyTestData> getAllTestDataFromService();
    void saveTestDataOnService(MyTestData x);
}
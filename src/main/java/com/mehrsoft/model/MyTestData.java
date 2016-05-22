package com.mehrsoft.model;

/**
 * Created by ijet on 5/10/16.
 */
public   class MyTestData {    // Just dummy class to return
    int i;
    String c;


    public MyTestData() {   // has to have the default constructor for webmethod to accept it as argument
    }

    public MyTestData(int i, String c) {
        this.i = i;
        this.c = c;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }


    }

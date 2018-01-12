package com.mehrsoft.service.original;

import java.util.concurrent.ThreadFactory;

/**
 * Created by daryoush_maxsam1 on 12/11/15.
 */
public class NamedThreadFactory implements ThreadFactory {

    int ctr = 0;
    String name;

    public NamedThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(name + "-" + ctr++);
        return t;
    }
}
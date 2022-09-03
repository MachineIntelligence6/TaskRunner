// Copyright (c) 2021 88 CREATIVE PTY LTD

package com.mi6.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ConcurrentBackgroundTaskRunner {

    private static ExecutorService _Instance;

    public static synchronized ExecutorService getInstance(){

        if( null == _Instance ){
            _Instance = Executors.newFixedThreadPool(20, AsyncUtils.createNamedThreadFactory("ConcurrentBackgroundTaskRunner"));
        }
        else{
            // do nothing
        }

        return _Instance;

    }

    private ConcurrentBackgroundTaskRunner() {
    }

    public static void submit(Runnable r) {
        getInstance().submit(r);
    }

    public static void shutdown(){
        if( null != _Instance ){//don't create instance just to shutdown it
            TaskUtils.shutdown( _Instance );
        }
    }

}
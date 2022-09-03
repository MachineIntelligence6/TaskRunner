// Copyright (c) 2021 88 CREATIVE PTY LTD

package com.mi6.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleBackgroundTaskRunner {

    private static ExecutorService _Instance;

    public static synchronized ExecutorService getInstance(){

        if( null == _Instance ){
            _Instance = Executors.newSingleThreadExecutor(
                AsyncUtils.createNamedThreadFactory( "SingleBackgroundTaskRunner" ) );
        }else{
            // do nothing
        }

        return _Instance;

    }

    private SingleBackgroundTaskRunner() {
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
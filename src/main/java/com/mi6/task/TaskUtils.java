package com.mi6.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


public class TaskUtils{

    private TaskUtils(){}

    public static void shutdown( ExecutorService ex ){

        ex.shutdown();
        try{

            // Wait a while for existing tasks to terminate
            if( !ex.awaitTermination( 3, TimeUnit.SECONDS ) ){

                ex.shutdownNow(); // Cancel currently executing tasks

                // Wait a while for tasks to respond to being cancelled
                if( !ex.awaitTermination( 2, TimeUnit.SECONDS ) ){
                    //System.out.println( "Shutdown didn't terminated!" );
                }

            }

        }catch( InterruptedException ie ){

            ex.shutdownNow();// (Re-)Cancel if current thread also interrupted
            // Thread.currentThread().interrupt(); // no need to preserve interrupt status

        }

    }

}

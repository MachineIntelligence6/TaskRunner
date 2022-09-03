package com.mi6.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;

public class AsyncUtils {

    public static ThreadFactory createNamedThreadFactory(String name) {
        return new ThreadFactoryBuilder().setNameFormat(name + "-%d").build();
    }

}
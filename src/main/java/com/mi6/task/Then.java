package com.mi6.task;

public interface Then<T>{
    public void complete(T result, Throwable e);
}

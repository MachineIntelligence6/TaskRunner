// Copyright (c) 2021 88 CREATIVE PTY LTD

package com.mi6.task;

public interface Then<T>{
    public void complete(T result, Throwable e);
}

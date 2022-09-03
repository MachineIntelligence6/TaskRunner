// Copyright (c) 2021 88 CREATIVE PTY LTD

package com.mi6.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.UUID;

public abstract class Task<T> {

    private final PropertyChangeSupport progressListeners = new PropertyChangeSupport(this);
    private final String name;

    public Task() {
        this(null);
    }

    public Task(String name) {
        this.name = (name != null ? name : getClass().getName()) + " [" + UUID.randomUUID() + "]";
    }

    private String getName() {
        return name;
    }

    /**
     *
     * @param uiMessage The current message to show to the user (for foreground tasks)
     */
    protected void setText(String uiMessage) {
        progressListeners.firePropertyChange("message", null, uiMessage);
    }

    /**
     * For ALL Database Transactions<br>
     * and<br>
     * tasks that are memory intensive they must be run
     * on this thread to prevent any out of memory errors, for example
     * Loading a large image or Scaling images.
     */
    public void runSequentiallyInBackground(Then<T> handler) {
        TaskBuddy buddy = new TaskBuddy(this, handler);
        SingleBackgroundTaskRunner.submit(buddy);
    }

    /**
     * For ALL Database Transactions<br>
     * and<br>
     * tasks that are memory intensive they must be run
     * on this thread to prevent any out of memory errors, for example
     * Loading a large image or Scaling images.
     */
    public void runSequentiallyInBackground() {
        SingleBackgroundTaskRunner.submit(new Runnable() {
            @Override
            public void run() {
                execute();
            }
        });
    }


    /**
     * For tasks that require no major amounts of memory but
     * might be blocked, for example an HTTP call.
     * The then handler cna be registered as a callback.
     */
    public void runConcurrentlyInBackground(Then<T> handler) {
        TaskBuddy buddy = new TaskBuddy(this, handler);
        ConcurrentBackgroundTaskRunner.submit(buddy);
    }

    /** run in the current thread synchronously */
    public void runHere(Then<T> handler) {
        TaskBuddy buddy = new TaskBuddy(this, handler);
        buddy.run();
    }

    /** For all tasks that block the UI */
    public void runSequentiallyInForeground() {
        SingleForegroundTaskRunner.submit(this);
    }


    public void runSequentiallyInForeground(Then<T> handler) {
        SingleForegroundTaskRunner.submit(this, handler);
    }

    /**
     * Perform long-running task here
     * @return the result
     */
    protected abstract T execute();

    protected abstract void cleanup();

    public void addListener(PropertyChangeListener pcl) {
        this.progressListeners.addPropertyChangeListener(pcl);
    }

    public void removeListener(PropertyChangeListener pcl) {
        this.progressListeners.removePropertyChangeListener(pcl);
    }

    private void clearListeners() {
        PropertyChangeListener[] all = progressListeners.getPropertyChangeListeners();
        for (PropertyChangeListener pcl: all
             ) {
            progressListeners.removePropertyChangeListener(pcl);
        }
    }

    public static class TaskBuddy<T> implements Runnable {


        private static final Logger LOG = LoggerFactory.getLogger(TaskBuddy.class.getName());

        private final Task<T> task;
        private final Then<T> handler;

        private T result;

        public TaskBuddy(Task<T> task, Then<T> handler) {
            this.task = task;
            this.handler = handler;
            LOG.debug("Queueing " + task.getName());
        }

        @Override
        public void run() {
            LOG.debug("Starting Task: " + task.getName() + " Thread [" + Thread.currentThread().getName() + "]");
            try {
                result = task.execute();
                handler.complete(result, null);
            }catch(Throwable t) {
                LOG.debug("Failed " + task.getName());
                handler.complete(null, t);
            }finally {
                task.clearListeners();
                task.cleanup();
                LOG.debug("Complete " + task.getName());
            }
        }

        public T getResult() {
            return result;
        }

    }
}

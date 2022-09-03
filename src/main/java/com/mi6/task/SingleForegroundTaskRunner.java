package com.mi6.task;

import java.awt.Component;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mi6.task.swing.TpcDisabledGlassPane;

import lombok.extern.slf4j.Slf4j;

/*
    Blocks the UI foreground, should be used to block the user from executing
    activities in the UI
 */
@Slf4j
public class SingleForegroundTaskRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SingleForegroundTaskRunner.class.getName());

    private static ExecutorService _Instance;
    private static JRootPane _rootPane;
    private static JRootPane _MainRootPane;

    private SingleForegroundTaskRunner() {
    }

    public static synchronized ExecutorService getInstance(){
        if( null == _Instance ){
            _Instance = Executors.newSingleThreadExecutor(AsyncUtils.createNamedThreadFactory("SingleForegroundTaskRunner"));
        }
        return _Instance;
    }

    public static void submit(Task t) {
        getInstance().submit(new UIBlockingWrapper(t, null));
    }

    public static void submit(Task t, Then handler) {
        getInstance().submit(new UIBlockingWrapper(t, handler));
    }

    public static void shutdown(){
        if( null != _Instance ){//don't create instance just to shutdown it
            TaskUtils.shutdown( _Instance );
        }
    }
    
    public static void setMainRootPane(JFrame frame) {
    	_MainRootPane = SwingUtilities.getRootPane(frame);
    }
    
    public static void setRootPane(JFrame frame) {
    	_rootPane = SwingUtilities.getRootPane(frame);
    }
    
    public static void setRootPane(JDialog dialog) {
    	_rootPane = SwingUtilities.getRootPane(dialog);
    }

    static class UIBlockingWrapper<T> implements Runnable, PropertyChangeListener {

        static Component origGlassPane = null;
        static TpcDisabledGlassPane glassPane = new TpcDisabledGlassPane();

        private final Task<T> t;
        private final Then<T> callback;

        UIBlockingWrapper(Task<T> t, Then<T> callback) {
            this.t = t;
            if (callback == null) {
                this.callback = new Then<T>() {
                    @Override
                    public void complete(T result, Throwable e) {
                    }
;                };
            }else {
                this.callback = callback;
            }
        }
        
		public JRootPane getRootPane() {
			return _rootPane != null ? _rootPane : _MainRootPane;
		}
        

        @Override
        public void run() {
            // register for updates
            t.addListener(this);
            
            final JRootPane rootPane = getRootPane();
            
            if (origGlassPane == null) {
                origGlassPane = rootPane.getGlassPane();
            }
            

            SwingUtilities.invokeLater( () -> {
            	rootPane.setGlassPane(glassPane);
                glassPane.activate("Loading");
            } );


            try {
                LOG.debug("UIBlockingWrapper START");
                try {
                    T result = t.execute();
                    callback.complete(result, null);
                }catch(Throwable t) {
                    callback.complete(null, t);
                }finally {

                    try {
                        t.cleanup();
                    }catch (Throwable e) {
                        e.printStackTrace();
                    }

                    try {
                        t.removeListener(this);
                    }catch (Throwable e) {
                        e.printStackTrace();
                    }

                }
                LOG.debug("UIBlockingWrapper COMPLETE");
            }finally {
                SwingUtilities.invokeLater( () -> glassPane.deactivate() );
                LOG.debug("UIBlockingWrapper END");
                _rootPane = null;
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("message")) {
                // updates the status message on the ui.
                glassPane.setMessage(evt.getNewValue().toString());
            }
        }
    }

}
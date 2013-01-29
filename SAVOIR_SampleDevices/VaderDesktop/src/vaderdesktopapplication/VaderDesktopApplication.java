// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * VaderDesktopApplication.java
 */

package vaderdesktopapplication;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class VaderDesktopApplication extends SingleFrameApplication {

    public static int initHeartBeat = 0;
    @Override
    protected void initialize(String[] args){
        if(args.length == 1){
            initHeartBeat = Integer.valueOf(args[0]);
            
        }
    }
    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new VaderDesktopView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of VaderDesktopApplication
     */
    public static VaderDesktopApplication getApplication() {
        return Application.getInstance(VaderDesktopApplication.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(VaderDesktopApplication.class, args);
    }
}

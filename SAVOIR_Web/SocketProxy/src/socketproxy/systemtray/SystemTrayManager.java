// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy.systemtray;

import socketproxy.*;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.atomic.AtomicInteger;



public class SystemTrayManager {

	// static SessionComposer frame = null;
	static SystemTray tray;
	static PopupMenu popup;
	static TrayIcon trayIcon;
    public static HeartbeatThread heartbeatThrd = new HeartbeatThread();
	static SystemTrayManager instance = null;
   
    private AtomicInteger heartbeatCounter = new AtomicInteger(0);

    private int heartbeatCounterThreshold = 5;

	public static SystemTrayManager getInstance() {
		if (instance != null) {
			return instance;
		} else {
			instance = new SystemTrayManager();
			return instance;
		}
	}

	private SystemTrayManager() {

		if (SystemTray.isSupported()) {

			tray = SystemTray.getSystemTray();
			popup = new PopupMenu();

			MouseListener mouseListener = new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						// savoirGUI.setVisible(true);
					} else if (e.getClickCount() == 1) {
						/*
						 * System.out.println("Tray Icon - Mouse clicked once!");
						 */
					}
				}

				public void mouseEntered(MouseEvent e) {
					System.out.println("Tray Icon - Mouse entered!");
				}

				public void mouseExited(MouseEvent e) {
					System.out.println("Tray Icon - Mouse exited!");
				}

				public void mousePressed(MouseEvent e) {
					System.out.println("Tray Icon - Mouse pressed!");
				}

				public void mouseReleased(MouseEvent e) {
					System.out.println("Tray Icon - Mouse released!");
				}
			};

			ActionListener exitListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Exiting...");
					//socketServer.stopSocketServer();
                    System.exit(0);
				}
			};

			ActionListener aboutListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("About - The SAVOIR Server Bridge creates a bridge\n"
						+ "between the client machine and the SAVOIR server.");
				}
			};



			MenuItem defaultItem = new MenuItem("Exit");
			MenuItem defaultItem2 = new MenuItem("About");
			defaultItem.addActionListener(exitListener);
			defaultItem2.addActionListener(aboutListener);

			popup.add(defaultItem2);
			popup.addSeparator();
			popup.add(defaultItem);

			ActionListener actionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					/*
					 * trayIcon.displayMessage("Action Event",
					 * "An Action Event Has Been Performed!",
					 * TrayIcon.MessageType.INFO);
					 */
				}
			};

			trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(
					this.getClass().getResource("/images/savoirLogoDarkSmall.png")),
					"SAVOIR Server Bridge", popup);
			
			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(actionListener);
			trayIcon.addMouseListener(mouseListener);

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println("TrayIcon could not be added.");
			}

			trayIcon.displayMessage("SAVOIR2.0 System Tray Manager",
					"Manager has been minimized to the system tray.",
					TrayIcon.MessageType.INFO);
//            heartbeatThrd = new HeartbeatThread();
//            heartbeatThrd.start();

		} else {
			// System Tray is not supported
		}

	}

  
	public void showInfoMessage(String message) {
		trayIcon.displayMessage("SAVOIR", message, TrayIcon.MessageType.INFO);
	}

	public void showErrorMessage(String message) {
		trayIcon.displayMessage("SAVOIR", message, TrayIcon.MessageType.ERROR);
	}

	public void showWarningMessage(String message) {
		trayIcon
				.displayMessage("SAVOIR", message, TrayIcon.MessageType.WARNING);
	}

    public void increaseOneOfHeartbeatCounter(){
        heartbeatCounter.incrementAndGet();
    }

    public void decreaseOneOfheartbeatCounter(){
        if (heartbeatCounter.get() > 0) {
            heartbeatCounter.decrementAndGet();
            //System.out.println(heartbeatCounter.get());
        }
    }

    public boolean isHeartbeatOk(){
        if(heartbeatCounter.get() <= heartbeatCounterThreshold){
            return true;
        }else{
            return false;
        }

    }

}

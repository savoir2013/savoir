// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * VaderDesktopView.java
 */

package vaderdesktopapplication;

import vaderdesktopapplication.jmsbusinterface.VaderBusInterfaceConsumerThread;
import vaderdesktopapplication.jmsbusinterface.VaderBusInterfaceProducerThread;
import vaderdesktopapplication.jmsbusinterface.VaderMsgProcessor;
import java.util.Observable;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import java.util.Observer;
import java.util.Properties;
import java.util.*;
/**
 * The application's main frame.
 */
public class VaderDesktopView extends FrameView implements Observer{

    public VaderDesktopView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        initVaderDesktop();
    }
    private VaderSound vaderSound;
    private Vader theVader;
    private int reportStatusBeatInterval;
    private ResourceBundle resource = ResourceBundle.getBundle("vaderdesktopapplication.resources.VaderBI",Locale.getDefault());
    private void initVaderDesktop(){
        //theHeart = new Vader();
        //beatSound = new HeartBeatNewSound();
        //theHeart.addObserver(this);

//        try {
//            File f = new File("resources/EKGBI.properties");
//            if (f.exists()) {
//                Properties pro = new Properties();
//                FileInputStream in = new FileInputStream(f);
//                pro.load(in);
//                System.out.println("All key are given: " + pro.keySet());

                url = resource.getString("SavoirJMSURL");
                topicToSavoir = resource.getString("topicToSavoir");
                topicFromSavoir = resource.getString("topicFromSavoir");
                reportStatusBeatInterval = Integer.valueOf(resource.getString("reportStatusBeatInterval"));
//            } else {
//
//                System.out.println("File not found!");
//            }
//        } catch (IOException ex) {
//            System.out.println(ex.toString());
//        }
        System.out.println("URL:" + url + "\ntoSavoir:" + topicToSavoir + "\nfromSavoir:" + topicFromSavoir + "\nreportStatusBeatInterval:" + reportStatusBeatInterval);
//        if(busMsgSender ==  null){
//            busMsgSender = new VaderBusInterfaceProducerThread();
//            busMsgSender.initProducer(url, topicToSavoir);
//        }
//        if(processor == null){
//            processor = new VaderMsgProcessor();
//            processor.initProcessor(busMsgSender, this);
//        }

//        if(this.busMsgReceiver == null){
//            busMsgReceiver = new VaderBusInterfaceConsumerThread();
//            busMsgReceiver.initConsumer(url, this.topicFromSavoir, processor);
//        }
        
        if (VaderDesktopApplication.initHeartBeat != 0) {
//            busMsgReceiver.start();
//            if (busMsgSender.running == false) {
//                busMsgSender.running = true;
//                busMsgSender.start();
//                System.out.println("Bus Interface thread get started!!");
//            }
//            ekgStatjTextField.setText("The EKG Bus interface threads are started!!");
            adjustHeartbeatRate(VaderDesktopApplication.initHeartBeat);
        }

    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = VaderDesktopApplication.getApplication().getMainFrame();
            aboutBox = new VaderDesktopAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        VaderDesktopApplication.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        vaderHBjTextField = new javax.swing.JTextField();
        playjButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        stopPlayjButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        startBIjMenuItem = new javax.swing.JMenuItem();
        stopBIjMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        vaderStatjTextField = new javax.swing.JTextField();

        mainPanel.setName("mainPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(vaderdesktopapplication.VaderDesktopApplication.class).getContext().getResourceMap(VaderDesktopView.class);
        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        vaderHBjTextField.setText(resourceMap.getString("heartbeatjTextField.text")); // NOI18N
        vaderHBjTextField.setToolTipText(resourceMap.getString("heartbeatjTextField.toolTipText")); // NOI18N
        vaderHBjTextField.setName("heartbeatjTextField"); // NOI18N

        playjButton.setText(resourceMap.getString("playjButton.text")); // NOI18N
        playjButton.setName("playjButton"); // NOI18N
        playjButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playjButtonMouseClicked(evt);
            }
        });

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        stopPlayjButton.setText(resourceMap.getString("stopPlayjButton.text")); // NOI18N
        stopPlayjButton.setEnabled(false);
        stopPlayjButton.setName("stopPlayjButton"); // NOI18N
        stopPlayjButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stopPlayjButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                                .addComponent(playjButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(stopPlayjButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(vaderHBjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jLabel2)))
                .addContainerGap(168, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(76, 76, 76)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vaderHBjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(62, 62, 62)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(playjButton)
                    .addComponent(stopPlayjButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(8, 8, 8))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(vaderdesktopapplication.VaderDesktopApplication.class).getContext().getActionMap(VaderDesktopView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        startBIjMenuItem.setText(resourceMap.getString("startBIjMenuItem.text")); // NOI18N
        startBIjMenuItem.setName("startBIjMenuItem"); // NOI18N
        startBIjMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startBIjMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(startBIjMenuItem);

        stopBIjMenuItem.setText(resourceMap.getString("stopBIjMenuItem.text")); // NOI18N
        stopBIjMenuItem.setName("stopBIjMenuItem"); // NOI18N
        stopBIjMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopBIjMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(stopBIjMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        vaderStatjTextField.setText(resourceMap.getString("ekgStatTextField.text")); // NOI18N
        vaderStatjTextField.setName("ekgStatTextField"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, statusPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(statusMessageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(vaderStatjTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE))
                    .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(statusMessageLabel)
                            .addComponent(statusAnimationLabel)
                            .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3))
                    .addComponent(vaderStatjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void playjButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playjButtonMouseClicked
        // TODO add your handling code here:
        //HeartBeatNewSound beat = new HeartBeatNewSound();
        //beat.play();
        
        long hd = Long.valueOf(this.vaderHBjTextField.getText().trim());
        //startHeartbeat();
        this.adjustHeartbeatRate(hd);
        this.playjButton.setEnabled(false);
        this.stopPlayjButton.setEnabled(true);
    }//GEN-LAST:event_playjButtonMouseClicked

    private void stopBIjMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopBIjMenuItemActionPerformed
        // TODO add your handling code here:
        if(busMsgSender.running == false){
           vaderStatjTextField.setText("The vader Bus interface threads has been stopped!!");
           return;
        }
        busMsgReceiver.terminateConnenction();
        busMsgSender.running = false;
        vaderStatjTextField.setText("The vader Bus interface threads are stopped!!");
    }//GEN-LAST:event_stopBIjMenuItemActionPerformed

    private void startBIjMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startBIjMenuItemActionPerformed
        // TODO add your handling code here:
        if(busMsgSender ==  null || !busMsgSender.running){
            busMsgSender = new VaderBusInterfaceProducerThread();
            busMsgSender.initProducer(url, topicToSavoir);
            processor = new VaderMsgProcessor();
            processor.initProcessor(busMsgSender, this);
        } else {

            vaderStatjTextField.setText("The vader Bus interface threads has been started!!");
            return;

        }
//        if(processor == null){
//            processor = new VaderMsgProcessor();
//            processor.initProcessor(busMsgSender, this);
//        }

        if(this.busMsgReceiver == null || !busMsgReceiver.isRunning()){
            busMsgReceiver = new VaderBusInterfaceConsumerThread();
            busMsgReceiver.initConsumer(url, this.topicFromSavoir, processor);
            busMsgReceiver.start();
        }
        if(busMsgSender.running == false){
            busMsgSender.running = true;
            busMsgSender.start();
            System.out.println("Bus Interface thread get started!!");
        }
        vaderStatjTextField.setText("The vader Bus interface threads are started!!");
    }//GEN-LAST:event_startBIjMenuItemActionPerformed

    private void stopPlayjButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stopPlayjButtonMouseClicked
        // TODO add your handling code here:
        terminateVaderDesktop();
        this.stopPlayjButton.setEnabled(false);
        this.playjButton.setEnabled(true);
    }//GEN-LAST:event_stopPlayjButtonMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton playjButton;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JMenuItem startBIjMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JMenuItem stopBIjMenuItem;
    private javax.swing.JButton stopPlayjButton;
    private javax.swing.JTextField vaderHBjTextField;
    private javax.swing.JTextField vaderStatjTextField;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;

    private static String url;
    private static String topicToSavoir;
    private static String topicFromSavoir;
    private VaderBusInterfaceConsumerThread busMsgReceiver;
    private VaderBusInterfaceProducerThread busMsgSender;
    private VaderMsgProcessor processor;
    private int countsOfBeat = 0;
    public void update(Observable o, Object arg) {
        //throw new UnsupportedOperationException("Not supported yet.");
        if(vaderSound != null){
            vaderSound.play();
            countsOfBeat = countsOfBeat + 1;
            if(countsOfBeat >= reportStatusBeatInterval){
                this.processor.reportStatus(theVader.rate());
                countsOfBeat = 0;
            }
        }
    }

    public void terminateVaderDesktop(){
        if(theVader != null){
            theVader.stopHeartBeat();
            theVader = null;
            
        }
        if(vaderSound != null){
            vaderSound.stop();
            vaderSound = null;
        }

    }
    public void adjustHeartbeatRate(long rate){
        theVader = new Vader();
        vaderSound = new VaderSound();
        theVader.addObserver(this);
        theVader.adjustHeartbeatRate(rate);
        theVader.startHeartBeat();
//        if(!(theHeart.isRunning())){
//            theHeart.startHeartBeat();
//        }
        this.vaderHBjTextField.setText(String.valueOf(rate));
        vaderStatjTextField.setText("Respiration Rate has been set to " + rate);
        this.playjButton.setEnabled(false);
        this.stopPlayjButton.setEnabled(true);
    }

    public void startHeartbeat(){
//        if(theHeart != null && !theHeart.isRunning()){
//          theHeart.startHeartBeat();
//        }else{
//            theHeart = new Vader();
//            beatSound = new HeartBeatNewSound();
//            theHeart.addObserver(this);
//        }
        
        theVader.startHeartBeat();
    }

    public void stopHeartbeat(){
        if(theVader != null){
            theVader.stopHeartBeat();
        }
        vaderStatjTextField.setText("Respiration Rate has been stopped!");
        this.playjButton.setEnabled(true);
        this.stopPlayjButton.setEnabled(false);
    }
}

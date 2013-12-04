/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yajasi.JungleJepps.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileFilter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.yajasi.JungleJepps.Field;
import org.yajasi.JungleJepps.db.*;
import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.pdf.Repository;
import com.toedter.calendar.JCalendar;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author Timothy
 */
public class NewJFrame extends javax.swing.JFrame {
  private final SettingsManager settings;
  private final DatabaseConnection db;
  private Runway runway;
  
  /**
   * Creates new form NewJFrame
   */
  public NewJFrame() {
    settings = DatabaseManager.getSettings();
    db = DatabaseManager.getDatabase();
    loadRunway();
    Save.setSaveOnChange(true);
    runway.dump();
    
    initComponents();
  }
  
 private ComboBoxModel getAircraftIds() {
      String[] ids;
      try{
          ids = db.getAllAircraftIds();
      }catch(DatabaseException e){
          showError(e);
          ids = new String[]{"<Error Loading Aircrafts>"};
      }    
      
      return newComboBoxModel(ids);
  }
  
  private ComboBoxModel getRunwayIds() {
      String[] runs;
      try{
          runs = db.getAllRunwayIds();
      }catch(DatabaseException e){
          showError(e);
          runs = new String[]{"<Error Loading Runways>"};
      }    
      
      return newComboBoxModel(runs);
  }
  
  private String getRunwayId() {
     String id;
  
     //Get the runway id from the component if the component is ready
     if(runwayComboBox != null) 
        id = (String) runwayComboBox.getSelectedItem();
     else //Otherwise get it from the last used settings
        id = settings.getStringForKey(Settings.LAST_RUNWAY);
     
     if(id == null || id.isEmpty())
     {
         String[] ids;
         try {
             ids = db.getAllRunwayIds();
         } catch (DatabaseException ex) {
             ids = null;
         }
         if( ids != null && ids.length > 0 )
         {
             id = ids[0];
         }
         else
         {
             id = "";
         }
     }
     
     return id;
  }
 
  private String getAircraftId() {
    String id;
  
     //Get the runway id from the component if the component is ready
     if(aircraftChooser != null) 
        id = (String) aircraftChooser.getSelectedItem();
     else //Otherwise get it from the last used settings
         id = settings.getStringForKey(Settings.LAST_AIRCRAFT);
          if(id == null || id.isEmpty())
     {
         String[] ids;
         try {
             ids = db.getAllAircraftIds();
         } catch (DatabaseException ex) {
             ids = null;
         }
         if( ids != null && ids.length > 0 )
         {
             id = ids[0];
         }
         else
         {
             id = "";
         }
     }
          
     return id;  
  }
  
   private String getField(Object key){
      if( !(key instanceof Field) )
          throw new IllegalArgumentException("Object key must be of ENUM Type Field");
      
      return runway.get( (Field) key);
  }
  
     
  private void loadRunway() {
    String runwayId = getRunwayId();
    String aircraftId = getAircraftId();
    
    try {
        runway = db.getRunway(runwayId, aircraftId);
        Save.setRunway(runway);
    }catch(DatabaseException e){
        e.printStackTrace();
        showError(e);
    }
  }
   
  private void showError(Exception e){
      
  }

  
   private class JJComboBoxModel extends AbstractListModel implements ComboBoxModel{
         private String item;
         private String[] options;
         
         public JJComboBoxModel(String[] opts){
            this.options = opts;   
         }
         
         @Override
         public void setSelectedItem(Object anItem) { 
             item = (String) anItem; 
         }
         
         @Override
         public Object getSelectedItem() { 
             return item; 
         }
         
         @Override
         public int getSize() { 
             return options.length; 
         }
         
         @Override
         public Object getElementAt(int index) { 
             return options[index]; 
         }
    }
    
    private class JJDefaultsComboBoxModel extends JJComboBoxModel {
         private Field field;
         private final static String defaultChangeOption = "--Change Defaults--";
         private javax.swing.JComboBox comboBox;
         
         public JJDefaultsComboBoxModel(javax.swing.JComboBox comboBox, Field field){
            //Construct upper model
            super( null ); 
            
            this.comboBox = comboBox;
            
            //Get current defualts
            String[] defaults  = settings.getDefaults(field);
            
            //The the defaults into the super.options String[] 
            loadDefaults(defaults);
            
            //Keep a handle on the field to load the 
            this.field = field;
         }
         
         //Helper function
         private void loadDefaults(String[] defs){
            //Construct new array with an extra spot at the bottom
            super.options = new String[defs.length + 1];
            
            System.arraycopy(defs, 0, super.options, 0, defs.length);
            
            //Add default item to bottom of list
            super.options[ super.options.length - 1 ] = defaultChangeOption;
         }
         
         private ComboBoxModel getThisModel(){
             return this;
         }
         
         @Override
         public void setSelectedItem(Object anItem){
             if( defaultChangeOption.equals(anItem) )
             {
                 StringBuilder initializer = new StringBuilder();
                 //Loop through every option except the one to change defaults
                 for(int i = 0; i < super.options.length - 1; i++)
                 {
                     if( !super.options[i].isEmpty() ) //If there is a default
                     {
                        initializer.append( super.options[i] );
                        initializer.append("\n");
                     }
                 }
                 
                 final JDialog popup = new JDialog();
                 popup.setTitle("Choose menu defaults for <" + settings.getLabel(field) + ">");
                 popup.setSize(400, 300);
                 
                 javax.swing.JPanel innerPanel = new javax.swing.JPanel(); //will hold label and text area
                 
                 //Graphical Layout:
                 //<Label>    <text area>
                 //<Cancel>   <Save>
                 innerPanel.setLayout(new GridLayout(2, 2)); 
                 
                 final javax.swing.JTextArea defaults = new javax.swing.JTextArea();
                 javax.swing.JButton cancel = new javax.swing.JButton("Cancel");
                 javax.swing.JButton save = new javax.swing.JButton("Save");


                 cancel.addActionListener(new ActionListener(){
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         //Discard window
                         popup.dispose();
                     }
                 });
                 
                 save.addActionListener(new ActionListener(){
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         //Get and split resultant into String[]
                         String result = defaults.getText();
                         String[] newDefaults = result.trim().split("\n");

                         //Save defaults in settings
                         settings.setDefaults(field, newDefaults);
                         
                         //Save the new settings in this runtime
                         loadDefaults(newDefaults);
                         
                         comboBox.setModel( getThisModel() );
                         comboBox.repaint();

                         //Discard window
                         popup.dispose();
                     }
                 });
                 
                 defaults.setText( initializer.toString() );
                 innerPanel.add( new JLabel("Menu Defaults (on separate lines)" ) );
                 innerPanel.add(defaults); 
                 innerPanel.add(cancel);
                 innerPanel.add(save);
                 
                 popup.add(innerPanel);
                 popup.setVisible(true);
                 
             }
             else //if not the default option
             {
                 //Set the model box to this item
                super.setSelectedItem(anItem);
             }
         }
         
        
    }
  
    private ComboBoxModel newComboBoxModel(String[] options){
        return new JJComboBoxModel(options);
    }
    
    
    private File showImageChooser(){
        FileFilter imageFilter = new FileFilter() {

        @Override
        public boolean accept(File f) {
          if (f.isDirectory()) {
            return true;
          }
          
          String extention = getExtension(f);
          
          if( extention != null && (
                extention.equals("jpg") ||
                extention.equals("png"))) { 
            return true;
          }
          
          return false;
        }

        @Override
        public String getDescription() {
          return "Image Files (*.jpg, *.png)";
        }
      };
              
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select New Image File");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(imageFilter);
        chooser.setAccessory(new ImagePreview(chooser));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }      
    }
    
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }


  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aircraftChooser = new javax.swing.JComboBox();
        aircraftChooserLabel = new javax.swing.JLabel();
        tabView = new javax.swing.JTabbedPane();
        runwayTab = new javax.swing.JPanel();
        publishButton = new javax.swing.JButton();
        previewViewButton = new javax.swing.JButton();
        runwayMainScroll = new javax.swing.JScrollPane();
        runwayMain = new javax.swing.JPanel();
        runwayComboBox = new javax.swing.JComboBox();
        aircraftDisclaimerLabel = new javax.swing.JLabel();
        runwayChooserLabel = new javax.swing.JLabel();
        changeLogScroll = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        imagePickerFieldset = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        topographyFieldset = new javax.swing.JPanel();
        elevationLabel = new javax.swing.JLabel();
        lengthLabel = new javax.swing.JLabel();
        widthLabel = new javax.swing.JLabel();
        elevationHL = new javax.swing.JCheckBox();
        jLabel15 = new javax.swing.JLabel();
        lengthHL = new javax.swing.JCheckBox();
        widthHL = new javax.swing.JCheckBox();
        tdzoneLabel = new javax.swing.JLabel();
        iasAdjustLabel = new javax.swing.JLabel();
        iasAdjustHL = new javax.swing.JCheckBox();
        tdZoneHL = new javax.swing.JCheckBox();
        elevation = new javax.swing.JTextField();
        length = new javax.swing.JTextField();
        width = new javax.swing.JTextField();
        tdZone = new javax.swing.JTextField();
        iasAdjust = new javax.swing.JTextField();
        longitude = new javax.swing.JTextField();
        longLabel = new javax.swing.JLabel();
        latLabel = new javax.swing.JLabel();
        latitude = new javax.swing.JTextField();
        inspectionFieldset = new javax.swing.JPanel();
        inspectionCheckbox = new javax.swing.JCheckBox();
        inspectionCompletedLabel = new javax.swing.JLabel();
        inspectionCompleted = new javax.swing.JComboBox();
        pilotInspectionLabel = new javax.swing.JLabel();
        pilotInspection = new javax.swing.JComboBox();
        inspectionDateLabel = new javax.swing.JLabel();
        inspectionDate = new javax.swing.JTextField();
        inspectionDateChooser = new javax.swing.JButton();
        inspectionLabel = new javax.swing.JLabel();
        informationFieldset = new javax.swing.JPanel();
        classification = new javax.swing.JComboBox();
        classificationLabel = new javax.swing.JLabel();
        freq1Label = new javax.swing.JLabel();
        freq2Label = new javax.swing.JLabel();
        freq1 = new javax.swing.JComboBox();
        freq2 = new javax.swing.JComboBox();
        langGreetLabel = new javax.swing.JLabel();
        langGreet = new javax.swing.JTextField();
        precipLabel = new javax.swing.JLabel();
        precip = new javax.swing.JComboBox();
        precipHL = new javax.swing.JCheckBox();
        runwaysFieldset = new javax.swing.JPanel();
        bTakeoffWeightLabel = new javax.swing.JLabel();
        bLandingWeightLabel = new javax.swing.JLabel();
        runwayBName = new javax.swing.JTextField();
        bTakeoffWeight = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        bTakeoffWeightNotes = new javax.swing.JTextArea();
        bLandingWeight = new javax.swing.JTextField();
        jScrollPane5 = new javax.swing.JScrollPane();
        bLandingWeightNotes = new javax.swing.JTextArea();
        jCheckBox9 = new javax.swing.JCheckBox();
        jLabel26 = new javax.swing.JLabel();
        jCheckBox16 = new javax.swing.JCheckBox();
        jCheckBox17 = new javax.swing.JCheckBox();
        jCheckBox18 = new javax.swing.JCheckBox();
        jCheckBox19 = new javax.swing.JCheckBox();
        runwayBLabel = new javax.swing.JLabel();
        runwayAName = new javax.swing.JTextField();
        aTakeoffWeight = new javax.swing.JTextField();
        jScrollPane23 = new javax.swing.JScrollPane();
        aTakeoffWeightNotes = new javax.swing.JTextArea();
        aLandingWeight = new javax.swing.JTextField();
        jScrollPane24 = new javax.swing.JScrollPane();
        aLandingWeightNotes = new javax.swing.JTextArea();
        aLandingWeightLabel = new javax.swing.JLabel();
        aTakeoffWeightLabel = new javax.swing.JLabel();
        runwayALabel = new javax.swing.JLabel();
        jLabel102 = new javax.swing.JLabel();
        jCheckBox24 = new javax.swing.JCheckBox();
        jCheckBox25 = new javax.swing.JCheckBox();
        jCheckBox26 = new javax.swing.JCheckBox();
        jCheckBox27 = new javax.swing.JCheckBox();
        jCheckBox10 = new javax.swing.JCheckBox();
        notesTab = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTextArea5 = new javax.swing.JTextArea();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTextArea6 = new javax.swing.JTextArea();
        jScrollPane8 = new javax.swing.JScrollPane();
        jTextArea7 = new javax.swing.JTextArea();
        jScrollPane9 = new javax.swing.JScrollPane();
        jTextArea8 = new javax.swing.JTextArea();
        jScrollPane10 = new javax.swing.JScrollPane();
        jTextArea9 = new javax.swing.JTextArea();
        jScrollPane11 = new javax.swing.JScrollPane();
        jTextArea10 = new javax.swing.JTextArea();
        jScrollPane12 = new javax.swing.JScrollPane();
        jTextArea11 = new javax.swing.JTextArea();
        jLabel39 = new javax.swing.JLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        jTextArea12 = new javax.swing.JTextArea();
        jLabel40 = new javax.swing.JLabel();
        jScrollPane14 = new javax.swing.JScrollPane();
        jTextArea13 = new javax.swing.JTextArea();
        jLabel41 = new javax.swing.JLabel();
        jScrollPane15 = new javax.swing.JScrollPane();
        jTextArea14 = new javax.swing.JTextArea();
        jLabel42 = new javax.swing.JLabel();
        jScrollPane16 = new javax.swing.JScrollPane();
        jTextArea15 = new javax.swing.JTextArea();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jScrollPane17 = new javax.swing.JScrollPane();
        jTextArea16 = new javax.swing.JTextArea();
        jScrollPane18 = new javax.swing.JScrollPane();
        jTextArea17 = new javax.swing.JTextArea();
        jScrollPane19 = new javax.swing.JScrollPane();
        jTextArea18 = new javax.swing.JTextArea();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        documentsTab = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jScrollPane21 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane20 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        jPanel13 = new javax.swing.JPanel();
        jLabel52 = new javax.swing.JLabel();
        jCheckBox20 = new javax.swing.JCheckBox();
        jCheckBox21 = new javax.swing.JCheckBox();
        jCheckBox22 = new javax.swing.JCheckBox();
        jButton5 = new javax.swing.JButton();
        settingsTab = new javax.swing.JPanel();
        settingsMainScroll = new javax.swing.JScrollPane();
        settingsMain = new javax.swing.JPanel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        jLabel69 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        jCheckBox23 = new javax.swing.JCheckBox();
        repoPath = new javax.swing.JTextField();
        repoPathBrowse = new javax.swing.JButton();
        primaryDbUri = new javax.swing.JTextField();
        primaryDbClassPath = new javax.swing.JTextField();
        adminPass = new javax.swing.JTextField();
        adminEmail = new javax.swing.JTextField();
        altitude = new javax.swing.JComboBox();
        weight = new javax.swing.JComboBox();
        distance = new javax.swing.JComboBox();
        dimension = new javax.swing.JComboBox();
        distanceConvert = new javax.swing.JTextField();
        longLatFormat = new javax.swing.JComboBox();
        trueCourse = new javax.swing.JCheckBox();
        magVariation = new javax.swing.JTextField();
        secondaryRunway = new javax.swing.JComboBox();
        homeRunway = new javax.swing.JComboBox();
        defExpiration = new javax.swing.JTextField();
        page2DisclaimerScroll = new javax.swing.JScrollPane();
        page2Disclaimer = new javax.swing.JTextArea();
        page1DisclaimerScroll = new javax.swing.JScrollPane();
        page1Disclaimer = new javax.swing.JTextArea();
        settingsLabelsScroll = new javax.swing.JScrollPane();
        settingsLabels = new javax.swing.JPanel();
        jLabel72 = new javax.swing.JLabel();
        jLabel73 = new javax.swing.JLabel();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();
        jLabel77 = new javax.swing.JLabel();
        jLabel78 = new javax.swing.JLabel();
        jLabel79 = new javax.swing.JLabel();
        jLabel80 = new javax.swing.JLabel();
        jLabel81 = new javax.swing.JLabel();
        jLabel82 = new javax.swing.JLabel();
        jLabel83 = new javax.swing.JLabel();
        jLabel84 = new javax.swing.JLabel();
        jLabel85 = new javax.swing.JLabel();
        jLabel86 = new javax.swing.JLabel();
        jTextField16 = new javax.swing.JTextField();
        jTextField17 = new javax.swing.JTextField();
        jTextField18 = new javax.swing.JTextField();
        jTextField19 = new javax.swing.JTextField();
        jTextField20 = new javax.swing.JTextField();
        jTextField21 = new javax.swing.JTextField();
        jTextField22 = new javax.swing.JTextField();
        jTextField23 = new javax.swing.JTextField();
        jTextField24 = new javax.swing.JTextField();
        jTextField25 = new javax.swing.JTextField();
        jTextField26 = new javax.swing.JTextField();
        jTextField27 = new javax.swing.JTextField();
        jTextField28 = new javax.swing.JTextField();
        jTextField29 = new javax.swing.JTextField();
        jTextField30 = new javax.swing.JTextField();
        jLabel87 = new javax.swing.JLabel();
        jTextField31 = new javax.swing.JTextField();
        jLabel88 = new javax.swing.JLabel();
        jTextField32 = new javax.swing.JTextField();
        jLabel89 = new javax.swing.JLabel();
        jLabel90 = new javax.swing.JLabel();
        jLabel91 = new javax.swing.JLabel();
        jLabel92 = new javax.swing.JLabel();
        jTextField33 = new javax.swing.JTextField();
        jTextField34 = new javax.swing.JTextField();
        jTextField35 = new javax.swing.JTextField();
        jTextField36 = new javax.swing.JTextField();
        jLabel93 = new javax.swing.JLabel();
        jTextField37 = new javax.swing.JTextField();
        jLabel94 = new javax.swing.JLabel();
        jTextField38 = new javax.swing.JTextField();
        jLabel95 = new javax.swing.JLabel();
        jLabel96 = new javax.swing.JLabel();
        jLabel97 = new javax.swing.JLabel();
        jLabel98 = new javax.swing.JLabel();
        jTextField39 = new javax.swing.JTextField();
        jTextField40 = new javax.swing.JTextField();
        jTextField41 = new javax.swing.JTextField();
        jTextField42 = new javax.swing.JTextField();
        jLabel99 = new javax.swing.JLabel();
        jTextField43 = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel100 = new javax.swing.JLabel();
        jLabel101 = new javax.swing.JLabel();
        jScrollPane22 = new javax.swing.JScrollPane();
        jPanel6 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Jungle Jepps - Desktop Application");
        setPreferredSize(new java.awt.Dimension(1024, 768));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        aircraftChooser.setModel(getAircraftIds());
        aircraftChooser.setSelectedItem(settings.get(Settings.LAST_AIRCRAFT));
        new OnChange(aircraftChooser){
            public void onChange(String value){
                settings.setValue(Settings.LAST_AIRCRAFT, value);
            }
        };

        aircraftChooserLabel.setLabelFor(aircraftChooser);
        aircraftChooserLabel.setText("Aircraft Type");

        publishButton.setText("Publish / Unavailable");

        previewViewButton.setText("Preview / View");
        previewViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewViewButtonActionPerformed(evt);
            }
        });

        runwayMainScroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        runwayMain.setLocation(new java.awt.Point(1255, 1140));
        runwayMain.setMaximumSize(new java.awt.Dimension(1255, 1140));
        runwayMain.setPreferredSize(new java.awt.Dimension(1255, 1140));

        runwayComboBox.setModel(getRunwayIds());
        runwayComboBox.setSelectedItem(settings.get(Settings.LAST_RUNWAY)
        );
        runwayComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runwayComboBoxActionPerformed(evt);
            }
        });
        new OnChange(runwayComboBox){
            public void onChange(String value){
                settings.setValue(Settings.LAST_RUNWAY, value);
            }
        };

        aircraftDisclaimerLabel.setText("This Diagram is for " + getAircraftId() + " aircraft only.");

        runwayChooserLabel.setText(settings.getLabel(Field.RUNWAY_IDENTIFIER));

        changeLogScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Change Log"));

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Date", "User", "Change"
            }
        ));
        changeLogScroll.setViewportView(jTable2);
        if (jTable2.getColumnModel().getColumnCount() > 0) {
            jTable2.getColumnModel().getColumn(2).setHeaderValue("Change");
        }

        imagePickerFieldset.setBorder(javax.swing.BorderFactory.createTitledBorder("Image"));
        imagePickerFieldset.setName("test"); // NOI18N

        jButton3.setIcon(new ImageIcon(getField(Field.IMAGE_PATH)));
        jButton3.setText("Select New Image File");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel17.setIcon(new ImageIcon(getField(Field.IMAGE_PATH)));

        javax.swing.GroupLayout imagePickerFieldsetLayout = new javax.swing.GroupLayout(imagePickerFieldset);
        imagePickerFieldset.setLayout(imagePickerFieldsetLayout);
        imagePickerFieldsetLayout.setHorizontalGroup(
            imagePickerFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imagePickerFieldsetLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(imagePickerFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(imagePickerFieldsetLayout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addGap(0, 299, Short.MAX_VALUE)))
                .addContainerGap())
        );
        imagePickerFieldsetLayout.setVerticalGroup(
            imagePickerFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imagePickerFieldsetLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jButton3)
                .addContainerGap())
        );

        topographyFieldset.setBorder(javax.swing.BorderFactory.createTitledBorder("Topography"));

        elevationLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        elevationLabel.setText(settings.getLabel(Field.ELEVATION) + " (" + settings.getStringForKey(Settings.ALTITUDE_UNITS) + ')');

    lengthLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lengthLabel.setText(String.format("%s (%s)", settings.getLabel(Field.LENGTH), settings.getStringForKey(Settings.DIMENSION_UNITS)));

    widthLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    widthLabel.setText(String.format("%s (%s)", settings.getLabel(Field.WIDTH_TEXT), settings.getStringForKey(Settings.DIMENSION_UNITS)));

    elevationHL.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            elevationHLActionPerformed(evt);
        }
    });

    jLabel15.setText("Highlight");

    lengthHL.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            lengthHLActionPerformed(evt);
        }
    });

    widthHL.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            widthHLActionPerformed(evt);
        }
    });

    tdzoneLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    tdzoneLabel.setText(settings.getLabel(Field.TDZ_SLOPE));

    iasAdjustLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    iasAdjustLabel.setText(settings.getLabel(Field.IAS_ADJUSTMENT)
    );

    iasAdjustHL.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            iasAdjustHLActionPerformed(evt);
        }
    });

    tdZoneHL.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            tdZoneHLActionPerformed(evt);
        }
    });

    elevation.setText(getField(Field.ELEVATION));
    elevation.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            elevationActionPerformed(evt);
        }
    });
    Save.onChange(elevation, Field.ELEVATION);

    length.setText(getField(Field.LENGTH));
    length.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            lengthActionPerformed(evt);
        }
    });
    Save.onChange(length, Field.LENGTH);

    width.setText(getField(Field.WIDTH_TEXT));
    width.setMaximumSize(new java.awt.Dimension(180, 28));
    width.setMinimumSize(new java.awt.Dimension(180, 28));
    width.setPreferredSize(new java.awt.Dimension(180, 28));
    width.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            widthActionPerformed(evt);
        }
    });
    Save.onChange(width, Field.WIDTH_TEXT);

    tdZone.setText(getField(Field.TDZ_SLOPE));
    tdZone.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            tdZoneActionPerformed(evt);
        }
    });
    Save.onChange(tdZone, Field.TDZ_SLOPE);

    iasAdjust.setText(getField(Field.IAS_ADJUSTMENT));
    iasAdjust.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            iasAdjustActionPerformed(evt);
        }
    });
    Save.onChange(iasAdjust, Field.IAS_ADJUSTMENT);

    longitude.setText(getField(Field.LONGITUDE));
    longitude.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            longitudeActionPerformed(evt);
        }
    });
    Save.onChange(longitude, Field.LONGITUDE);

    longLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    longLabel.setText(settings.getLabel(Field.LONGITUDE));

    latLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    latLabel.setText(settings.getLabel(Field.LATITUDE));
    latLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

    latitude.setText(getField(Field.LATITUDE));
    latitude.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            latitudeActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout topographyFieldsetLayout = new javax.swing.GroupLayout(topographyFieldset);
    topographyFieldset.setLayout(topographyFieldsetLayout);
    topographyFieldsetLayout.setHorizontalGroup(
        topographyFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(topographyFieldsetLayout.createSequentialGroup()
            .addGap(5, 5, 5)
            .addGroup(topographyFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(longLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addComponent(elevationLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lengthLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(widthLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tdzoneLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(iasAdjustLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(latLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(18, 18, 18)
            .addGroup(topographyFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(elevation)
                .addComponent(length)
                .addComponent(width, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                .addComponent(tdZone)
                .addComponent(latitude, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(iasAdjust)
                .addComponent(longitude, javax.swing.GroupLayout.Alignment.TRAILING))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(topographyFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(tdZoneHL)
                .addComponent(iasAdjustHL)
                .addComponent(widthHL)
                .addComponent(lengthHL)
                .addComponent(elevationHL)
                .addComponent(jLabel15)))
    );

    topographyFieldsetLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {elevationLabel, iasAdjustLabel, latLabel, lengthLabel, longLabel, tdzoneLabel, widthLabel});

    topographyFieldsetLayout.setVerticalGroup(
        topographyFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(topographyFieldsetLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel15)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(topographyFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(elevationLabel)
                .addComponent(elevationHL)
                .addComponent(elevation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(topographyFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(lengthLabel)
                .addComponent(lengthHL)
                .addComponent(length, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(topographyFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(widthHL)
                .addComponent(widthLabel)
                .addComponent(width, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(topographyFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(tdzoneLabel)
                .addComponent(tdZoneHL)
                .addComponent(tdZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(topographyFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(iasAdjustLabel)
                .addComponent(iasAdjustHL)
                .addComponent(iasAdjust, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(65, 65, 65)
            .addGroup(topographyFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(longitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(longLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(topographyFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(latLabel)
                .addComponent(latitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(30, 30, 30))
    );

    topographyFieldsetLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {elevationLabel, iasAdjustLabel, latLabel, lengthLabel, longLabel, tdzoneLabel, widthLabel});

    inspectionFieldset.setBorder(javax.swing.BorderFactory.createTitledBorder("Inspection"));
    inspectionFieldset.setMaximumSize(new java.awt.Dimension(250, 155));
    inspectionFieldset.setMinimumSize(new java.awt.Dimension(250, 155));

    inspectionCompletedLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    inspectionCompletedLabel.setText(settings.getLabel(Field.INSPECTION_DATE));
    inspectionCompletedLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    inspectionCompletedLabel.setMaximumSize(new java.awt.Dimension(90, 16));
    inspectionCompletedLabel.setMinimumSize(new java.awt.Dimension(90, 16));

    inspectionCompleted.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    inspectionCompleted.setSelectedItem(getField(Field.INSPECTION_DUE));

    pilotInspectionLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    pilotInspectionLabel.setText(settings.getLabel(Field.INSPECTOR_NAME));
    pilotInspectionLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

    pilotInspection.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

    inspectionDateLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    inspectionDateLabel.setText(settings.getLabel(Field.INSPECTION_DUE));

    inspectionDate.setText(getField(Field.INSPECTION_DATE));
    inspectionDate.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            inspectionDateActionPerformed(evt);
        }
    });

    inspectionDateChooser.setText("Select");
    inspectionDateChooser.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            inspectionDateChooserActionPerformed(evt);
        }
    });

    inspectionLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    inspectionLabel.setText(settings.getLabel(Field.INSPECTION_NA));
    inspectionLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    inspectionLabel.setMaximumSize(new java.awt.Dimension(90, 16));
    inspectionLabel.setMinimumSize(new java.awt.Dimension(90, 16));
    inspectionLabel.setPreferredSize(new java.awt.Dimension(90, 16));

    javax.swing.GroupLayout inspectionFieldsetLayout = new javax.swing.GroupLayout(inspectionFieldset);
    inspectionFieldset.setLayout(inspectionFieldsetLayout);
    inspectionFieldsetLayout.setHorizontalGroup(
        inspectionFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(inspectionFieldsetLayout.createSequentialGroup()
            .addGroup(inspectionFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(inspectionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(inspectionCompletedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(pilotInspectionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(inspectionDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(12, 12, 12)
            .addGroup(inspectionFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(inspectionFieldsetLayout.createSequentialGroup()
                    .addComponent(inspectionDate, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(inspectionDateChooser))
                .addGroup(inspectionFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, inspectionFieldsetLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inspectionCheckbox)
                        .addGap(298, 298, 298))
                    .addComponent(inspectionCompleted, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pilotInspection, javax.swing.GroupLayout.Alignment.LEADING, 0, 349, Short.MAX_VALUE)))
            .addContainerGap())
    );

    inspectionFieldsetLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {inspectionCompletedLabel, inspectionDateLabel, inspectionLabel, pilotInspectionLabel});

    inspectionFieldsetLayout.setVerticalGroup(
        inspectionFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(inspectionFieldsetLayout.createSequentialGroup()
            .addContainerGap(7, Short.MAX_VALUE)
            .addGroup(inspectionFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(inspectionCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(inspectionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(inspectionFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(inspectionCompletedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(inspectionCompleted, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(inspectionFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(pilotInspectionLabel)
                .addComponent(pilotInspection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(inspectionFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(inspectionFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inspectionDateLabel)
                    .addComponent(inspectionDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(inspectionDateChooser)))
    );

    informationFieldset.setBorder(javax.swing.BorderFactory.createTitledBorder("Information"));

    classification.setModel(new JJDefaultsComboBoxModel(classification, Field.CLASSIFICATION));
    classification.setSelectedItem(getField(Field.CLASSIFICATION));

    classificationLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    classificationLabel.setText(settings.getLabel(Field.CLASSIFICATION));

    freq1Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    freq1Label.setText(settings.getLabel(Field.FREQUENCY_1));

    freq2Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    freq2Label.setText(settings.getLabel(Field.FREQUENCY_2));

    freq1.setModel(new JJDefaultsComboBoxModel(freq1, Field.FREQUENCY_1));
    freq1.setSelectedItem(getField(Field.FREQUENCY_1));
    Save.onChange(freq1, Field.FREQUENCY_1);

    freq2.setModel(new JJDefaultsComboBoxModel(freq2, Field.FREQUENCY_2));
    freq2.setSelectedItem(getField(Field.FREQUENCY_2));
    Save.onChange(freq2, Field.FREQUENCY_2);

    langGreetLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    langGreetLabel.setText(settings.getLabel(Field.LANGUAGE_GREET));

    langGreet.setText(getField(Field.LANGUAGE_GREET));
    langGreet.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            langGreetActionPerformed(evt);
        }
    });
    Save.onChange(langGreet, Field.LANGUAGE_GREET);

    precipLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    precipLabel.setText(settings.getLabel(Field.PRECIPITATION_ON_SCREEN));

    precip.setModel(new JJDefaultsComboBoxModel(precip, Field.CLASSIFICATION));
    precip.setSelectedItem(getField(Field.CLASSIFICATION));
    Save.onChange(precip, Field.CLASSIFICATION);

    precipHL.setText("Highlight");
    precipHL.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            precipHLActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout informationFieldsetLayout = new javax.swing.GroupLayout(informationFieldset);
    informationFieldset.setLayout(informationFieldsetLayout);
    informationFieldsetLayout.setHorizontalGroup(
        informationFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(informationFieldsetLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(informationFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(classificationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(freq1Label, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(freq2Label, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(langGreetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(precipLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(informationFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(precipHL)
                .addComponent(freq1, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(freq2, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(langGreet, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(precip, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(classification, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(35, Short.MAX_VALUE))
    );

    informationFieldsetLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {classification, freq1, freq2, langGreet, precip});

    informationFieldsetLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {classificationLabel, freq1Label, freq2Label, langGreetLabel, precipLabel});

    informationFieldsetLayout.setVerticalGroup(
        informationFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(informationFieldsetLayout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(informationFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(classificationLabel)
                .addComponent(classification, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(informationFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(freq1Label)
                .addComponent(freq1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(informationFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(freq2Label)
                .addComponent(freq2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(informationFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(langGreetLabel)
                .addComponent(langGreet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(informationFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(precipLabel)
                .addComponent(precip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(precipHL))
    );

    informationFieldsetLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {classificationLabel, freq1Label, freq2Label, langGreetLabel, precipLabel});

    runwaysFieldset.setBorder(javax.swing.BorderFactory.createTitledBorder("Runways"));

    bTakeoffWeightLabel.setText("Takeoff Weight");

    bLandingWeightLabel.setText("Landing Weight");

    runwayBName.setText(getField(Field.RUNWAY_B));
    Save.onChange(runwayBName, Field.RUNWAY_B);

    bTakeoffWeight.setText(getField(Field.B_TAKEOFF_RESTRICTION));
    Save.onChange(bTakeoffWeight, Field.B_TAKEOFF_RESTRICTION);

    bTakeoffWeightNotes.setColumns(20);
    bTakeoffWeightNotes.setRows(5);
    bTakeoffWeightNotes.setText(getField(Field.A_TAKEOFF_NOTE));
    Save.onChange(bTakeoffWeightNotes, Field.B_TAKEOFF_NOTE);
    jScrollPane3.setViewportView(bTakeoffWeightNotes);

    bLandingWeight.setText(getField(Field.B_LANDING_RESTRICTION));
    Save.onChange(bLandingWeight, Field.B_LANDING_RESTRICTION);

    bLandingWeightNotes.setColumns(20);
    bLandingWeightNotes.setRows(5);
    bLandingWeightNotes.setText(getField(Field.B_LANDING_NOTE));
    Save.onChange(bLandingWeightNotes, Field.B_LANDING_NOTE);
    jScrollPane5.setViewportView(bLandingWeightNotes);

    jCheckBox9.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBox9ActionPerformed(evt);
        }
    });

    jLabel26.setText("Highlight");

    jCheckBox16.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBox16ActionPerformed(evt);
        }
    });

    jCheckBox17.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBox17ActionPerformed(evt);
        }
    });

    jCheckBox18.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBox18ActionPerformed(evt);
        }
    });

    jCheckBox19.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBox19ActionPerformed(evt);
        }
    });

    runwayBLabel.setText("Runway B");

    runwayAName.setText(getField(Field.RUNWAY_A));
    Save.onChange(runwayAName, Field.RUNWAY_A);

    aTakeoffWeight.setText(getField(Field.A_TAKEOFF_RESTRICTION));
    Save.onChange(aTakeoffWeight, Field.A_TAKEOFF_RESTRICTION);

    aTakeoffWeightNotes.setColumns(20);
    aTakeoffWeightNotes.setLineWrap(true);
    aTakeoffWeightNotes.setRows(5);
    aTakeoffWeightNotes.setText(getField(Field.A_TAKEOFF_NOTE));
    Save.onChange(aTakeoffWeightNotes, Field.A_TAKEOFF_NOTE);
    jScrollPane23.setViewportView(aTakeoffWeightNotes);

    aLandingWeight.setText(getField(Field.A_LANDING_RESTRICTION));
    Save.onChange(aLandingWeight, Field.A_LANDING_RESTRICTION);

    aLandingWeightNotes.setColumns(20);
    aLandingWeightNotes.setRows(5);
    aLandingWeightNotes.setText(getField(Field.A_LANDING_NOTE));
    Save.onChange(aLandingWeightNotes, Field.A_LANDING_NOTE);
    jScrollPane24.setViewportView(aLandingWeightNotes);

    aLandingWeightLabel.setText("Landing Weight");

    aTakeoffWeightLabel.setText("Takeoff Weight");

    runwayALabel.setText("Runway A");

    jLabel102.setText("Highlight");

    jCheckBox24.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBox24ActionPerformed(evt);
        }
    });

    jCheckBox25.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBox25ActionPerformed(evt);
        }
    });

    jCheckBox26.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBox26ActionPerformed(evt);
        }
    });

    jCheckBox27.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBox27ActionPerformed(evt);
        }
    });

    jCheckBox10.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBox10ActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout runwaysFieldsetLayout = new javax.swing.GroupLayout(runwaysFieldset);
    runwaysFieldset.setLayout(runwaysFieldsetLayout);
    runwaysFieldsetLayout.setHorizontalGroup(
        runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(runwaysFieldsetLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, runwaysFieldsetLayout.createSequentialGroup()
                                .addComponent(aLandingWeightLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, runwaysFieldsetLayout.createSequentialGroup()
                                .addComponent(aTakeoffWeightLabel)
                                .addGap(2, 2, 2)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, runwaysFieldsetLayout.createSequentialGroup()
                            .addComponent(runwayALabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(runwayAName, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(aTakeoffWeight, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(aLandingWeight, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane24)
                                .addComponent(jScrollPane23, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jCheckBox25)
                        .addComponent(jCheckBox24)
                        .addComponent(jCheckBox26)
                        .addComponent(jCheckBox27)
                        .addComponent(jCheckBox10)))
                .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                    .addGap(388, 388, 388)
                    .addComponent(jLabel102)))
            .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                            .addGap(59, 59, 59)
                            .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, runwaysFieldsetLayout.createSequentialGroup()
                                    .addComponent(bLandingWeightLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, runwaysFieldsetLayout.createSequentialGroup()
                                    .addComponent(bTakeoffWeightLabel)
                                    .addGap(2, 2, 2))))
                        .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(runwayBLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(runwayBName, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(bTakeoffWeight, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(bLandingWeight, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                                .addComponent(jScrollPane3))))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jCheckBox19)
                        .addComponent(jCheckBox16)
                        .addComponent(jCheckBox17)
                        .addComponent(jCheckBox18)
                        .addComponent(jCheckBox9)))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, runwaysFieldsetLayout.createSequentialGroup()
                    .addGap(447, 447, 447)
                    .addComponent(jLabel26)))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    runwaysFieldsetLayout.setVerticalGroup(
        runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(runwaysFieldsetLayout.createSequentialGroup()
            .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel26)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(runwayBLabel)
                        .addComponent(runwayBName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCheckBox16))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bTakeoffWeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bTakeoffWeightLabel))
                        .addComponent(jCheckBox19))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                            .addGap(28, 28, 28)
                            .addComponent(jCheckBox17)))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bLandingWeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bLandingWeightLabel))
                        .addComponent(jCheckBox18))
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                            .addGap(37, 37, 37)
                            .addComponent(jCheckBox9))))
                .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                    .addComponent(jLabel102)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(runwayALabel)
                        .addComponent(runwayAName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCheckBox24))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(aTakeoffWeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(aTakeoffWeightLabel))
                        .addComponent(jCheckBox25))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                            .addGap(28, 28, 28)
                            .addComponent(jCheckBox26)))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(aLandingWeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(aLandingWeightLabel))
                        .addComponent(jCheckBox27))
                    .addGroup(runwaysFieldsetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(runwaysFieldsetLayout.createSequentialGroup()
                            .addGap(37, 37, 37)
                            .addComponent(jCheckBox10)))))
            .addContainerGap())
    );

    javax.swing.GroupLayout runwayMainLayout = new javax.swing.GroupLayout(runwayMain);
    runwayMain.setLayout(runwayMainLayout);
    runwayMainLayout.setHorizontalGroup(
        runwayMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(runwayMainLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(runwayMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(runwayMainLayout.createSequentialGroup()
                    .addComponent(changeLogScroll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(imagePickerFieldset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(runwaysFieldset, javax.swing.GroupLayout.PREFERRED_SIZE, 972, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(runwayMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(runwayMainLayout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(runwayMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(aircraftDisclaimerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(runwayMainLayout.createSequentialGroup()
                                .addComponent(runwayChooserLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(runwayComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(runwayMainLayout.createSequentialGroup()
                        .addGroup(runwayMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(inspectionFieldset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(informationFieldset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(topographyFieldset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addGap(5, 5, 5))
    );

    runwayMainLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {informationFieldset, inspectionFieldset, topographyFieldset});

    runwayMainLayout.setVerticalGroup(
        runwayMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(runwayMainLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(aircraftDisclaimerLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(runwayMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(runwayChooserLabel)
                .addComponent(runwayComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(runwayMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(runwayMainLayout.createSequentialGroup()
                    .addComponent(inspectionFieldset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(informationFieldset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(topographyFieldset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(runwaysFieldset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addGroup(runwayMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(changeLogScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(imagePickerFieldset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(0, 0, 0))
    );

    runwayMainScroll.setViewportView(runwayMain);

    javax.swing.GroupLayout runwayTabLayout = new javax.swing.GroupLayout(runwayTab);
    runwayTab.setLayout(runwayTabLayout);
    runwayTabLayout.setHorizontalGroup(
        runwayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(runwayTabLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(runwayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, runwayTabLayout.createSequentialGroup()
                    .addComponent(runwayMainScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 985, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, 0))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, runwayTabLayout.createSequentialGroup()
                    .addComponent(publishButton)
                    .addGap(18, 18, 18)
                    .addComponent(previewViewButton)))
            .addContainerGap())
    );
    runwayTabLayout.setVerticalGroup(
        runwayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(runwayTabLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(runwayMainScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
            .addGap(18, 18, 18)
            .addGroup(runwayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(previewViewButton)
                .addComponent(publishButton))
            .addContainerGap())
    );

    tabView.addTab(getRunwayId() + " DATA", runwayTab);

    jLabel27.setText("This Diagram is for <Aircraft Type> aircraft only.");

    jLabel28.setText("Click to change");

    jLabel29.setText("icon");

    jLabel30.setText("Page One Text");

    jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel31.setText("Page Two Text");

    jLabel32.setText("Committal Point");

    jLabel33.setText("Go Around");

    jLabel34.setText("Emergency Stop after Landing");

    jLabel35.setText("Surface Description");

    jLabel36.setText("Hazards & Additional Info");

    jLabel37.setText("Aborted Takeoff");

    jLabel38.setText("Departure Engine Failure Option(s)");

    jTextArea5.setColumns(20);
    jTextArea5.setRows(5);
    jTextArea5.setPreferredSize(new java.awt.Dimension(19, 19));
    jScrollPane6.setViewportView(jTextArea5);

    jTextArea6.setColumns(20);
    jTextArea6.setRows(5);
    jTextArea6.setPreferredSize(new java.awt.Dimension(19, 19));
    jScrollPane7.setViewportView(jTextArea6);

    jTextArea7.setColumns(20);
    jTextArea7.setRows(5);
    jScrollPane8.setViewportView(jTextArea7);

    jTextArea8.setColumns(20);
    jTextArea8.setRows(5);
    jTextArea8.setPreferredSize(new java.awt.Dimension(19, 19));
    jScrollPane9.setViewportView(jTextArea8);

    jTextArea9.setColumns(20);
    jTextArea9.setRows(5);
    jTextArea9.setPreferredSize(new java.awt.Dimension(19, 19));
    jScrollPane10.setViewportView(jTextArea9);

    jTextArea10.setColumns(20);
    jTextArea10.setRows(5);
    jTextArea10.setPreferredSize(new java.awt.Dimension(19, 19));
    jScrollPane11.setViewportView(jTextArea10);

    jTextArea11.setColumns(20);
    jTextArea11.setRows(5);
    jTextArea11.setPreferredSize(new java.awt.Dimension(19, 19));
    jScrollPane12.setViewportView(jTextArea11);

    jLabel39.setText("Language Group and Greeting");

    jTextArea12.setColumns(20);
    jTextArea12.setRows(5);
    jScrollPane13.setViewportView(jTextArea12);

    jLabel40.setText("Weather Patterns");

    jTextArea13.setColumns(20);
    jTextArea13.setRows(5);
    jTextArea13.setPreferredSize(new java.awt.Dimension(19, 19));
    jScrollPane14.setViewportView(jTextArea13);

    jLabel41.setText("Explanation of Restrictions");

    jTextArea14.setColumns(20);
    jTextArea14.setRows(5);
    jTextArea14.setPreferredSize(new java.awt.Dimension(19, 19));
    jScrollPane15.setViewportView(jTextArea14);

    jLabel42.setText("Chief Pilot Comments");

    jTextArea15.setColumns(20);
    jTextArea15.setRows(5);
    jTextArea15.setPreferredSize(new java.awt.Dimension(19, 19));
    jScrollPane16.setViewportView(jTextArea15);

    jLabel43.setText("Minimum Number/Type of Wind Indicators");

    jLabel44.setText("Runway Minimum Maintenance Standard");

    jTextArea16.setColumns(20);
    jTextArea16.setRows(5);
    jTextArea16.setPreferredSize(new java.awt.Dimension(19, 19));
    jScrollPane17.setViewportView(jTextArea16);

    jTextArea17.setColumns(20);
    jTextArea17.setRows(5);
    jTextArea17.setPreferredSize(new java.awt.Dimension(19, 19));
    jScrollPane18.setViewportView(jTextArea17);

    jTextArea18.setColumns(20);
    jTextArea18.setRows(5);
    jTextArea18.setPreferredSize(new java.awt.Dimension(19, 19));
    jScrollPane19.setViewportView(jTextArea18);

    jLabel45.setText("Pilot Authority for Runway Below Standard");

    jLabel46.setText("icon");

    jLabel47.setText("icon");

    jLabel48.setText("icon");

    jLabel49.setText("icon");

    jLabel50.setText("icon");

    jLabel51.setText("icon");

    javax.swing.GroupLayout notesTabLayout = new javax.swing.GroupLayout(notesTab);
    notesTab.setLayout(notesTabLayout);
    notesTabLayout.setHorizontalGroup(
        notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(notesTabLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jLabel28)
                .addComponent(jLabel29)
                .addComponent(jLabel46)
                .addComponent(jLabel47)
                .addComponent(jLabel48)
                .addComponent(jLabel49)
                .addComponent(jLabel50)
                .addComponent(jLabel51))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(notesTabLayout.createSequentialGroup()
                    .addGap(88, 88, 88)
                    .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel33)
                        .addComponent(jLabel38)
                        .addComponent(jLabel37)
                        .addComponent(jLabel36)
                        .addComponent(jLabel34)
                        .addComponent(jLabel35)
                        .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel30)
                            .addComponent(jLabel32))))
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(notesTabLayout.createSequentialGroup()
                    .addGap(2, 2, 2)
                    .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                        .addComponent(jScrollPane12, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.Alignment.LEADING))))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane13)
                .addComponent(jScrollPane14)
                .addComponent(jScrollPane15)
                .addComponent(jScrollPane16)
                .addComponent(jScrollPane17)
                .addComponent(jScrollPane18)
                .addComponent(jScrollPane19)
                .addComponent(jLabel31, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(notesTabLayout.createSequentialGroup()
                    .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel39)
                        .addComponent(jLabel40)
                        .addComponent(jLabel41)
                        .addComponent(jLabel42)
                        .addComponent(jLabel43)
                        .addComponent(jLabel44)
                        .addComponent(jLabel45))
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addContainerGap(307, Short.MAX_VALUE))
        .addGroup(notesTabLayout.createSequentialGroup()
            .addGap(10, 10, 10)
            .addComponent(jLabel27)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    notesTabLayout.setVerticalGroup(
        notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(notesTabLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel27)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(notesTabLayout.createSequentialGroup()
                    .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel28)
                        .addComponent(jLabel30))
                    .addGap(8, 8, 8)
                    .addComponent(jLabel32)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jLabel29)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel33)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jLabel46)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel34)
                    .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(notesTabLayout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(notesTabLayout.createSequentialGroup()
                            .addGap(26, 26, 26)
                            .addComponent(jLabel47)))
                    .addGap(1, 1, 1)
                    .addComponent(jLabel35)
                    .addGap(18, 18, 18)
                    .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel48)
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel36)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel49))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel37)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel50))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel38)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(notesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel51))
                    .addGap(0, 0, Short.MAX_VALUE))
                .addGroup(notesTabLayout.createSequentialGroup()
                    .addComponent(jLabel31)
                    .addGap(8, 8, 8)
                    .addComponent(jLabel39)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel40)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel41)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(1, 1, 1)
                    .addComponent(jLabel42)
                    .addGap(18, 18, 18)
                    .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel43)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel44)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel45)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
    );

    tabView.addTab(getRunwayId() + " TEXT", notesTab);

    jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Documents being syced with iOS"));

    jButton4.setText("Add file");

    jList1.setModel(new javax.swing.AbstractListModel() {
        String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
        public int getSize() { return strings.length; }
        public Object getElementAt(int i) { return strings[i]; }
    });
    jScrollPane21.setViewportView(jList1);

    javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
    jPanel11.setLayout(jPanel11Layout);
    jPanel11Layout.setHorizontalGroup(
        jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel11Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane21)
                .addGroup(jPanel11Layout.createSequentialGroup()
                    .addComponent(jButton4)
                    .addGap(0, 97, Short.MAX_VALUE)))
            .addContainerGap())
    );
    jPanel11Layout.setVerticalGroup(
        jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel11Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jScrollPane21, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButton4)
            .addContainerGap())
    );

    jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Reports"));

    jList2.setModel(new javax.swing.AbstractListModel() {
        String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
        public int getSize() { return strings.length; }
        public Object getElementAt(int i) { return strings[i]; }
    });
    jScrollPane20.setViewportView(jList2);

    javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
    jPanel12.setLayout(jPanel12Layout);
    jPanel12Layout.setHorizontalGroup(
        jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel12Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jScrollPane20, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
            .addContainerGap())
    );
    jPanel12Layout.setVerticalGroup(
        jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel12Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jScrollPane20)
            .addContainerGap())
    );

    jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Export as KML"));

    jLabel52.setText("Include");

    jCheckBox20.setText("Length");

    jCheckBox21.setText("Elevation");

    jCheckBox22.setText("Width");

    jButton5.setText("Export");

    javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
    jPanel13.setLayout(jPanel13Layout);
    jPanel13Layout.setHorizontalGroup(
        jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel13Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jCheckBox22)
                .addComponent(jLabel52)
                .addComponent(jCheckBox20)
                .addComponent(jCheckBox21)
                .addComponent(jButton5))
            .addContainerGap(28, Short.MAX_VALUE))
    );
    jPanel13Layout.setVerticalGroup(
        jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel13Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel52)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jCheckBox21)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jCheckBox20)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jCheckBox22)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButton5)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout documentsTabLayout = new javax.swing.GroupLayout(documentsTab);
    documentsTab.setLayout(documentsTabLayout);
    documentsTabLayout.setHorizontalGroup(
        documentsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(documentsTabLayout.createSequentialGroup()
            .addGap(45, 45, 45)
            .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(28, 28, 28)
            .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(179, Short.MAX_VALUE))
    );
    documentsTabLayout.setVerticalGroup(
        documentsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(documentsTabLayout.createSequentialGroup()
            .addGap(83, 83, 83)
            .addGroup(documentsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap(253, Short.MAX_VALUE))
    );

    tabView.addTab("Documents", documentsTab);

    jLabel53.setText("Primary Setup");

    jLabel54.setText("Repository Path");

    jLabel55.setText("Primary Database URI");

    jLabel56.setText("Primary Database Class Path");

    jLabel57.setText("Admin Password");

    jLabel58.setText("Admin Email");

    jLabel59.setText("Altitude Units");

    jLabel60.setText("Weight Units");

    jLabel61.setText("Distance Units");

    jLabel62.setText("Dimension Units");

    jLabel63.setText("Distance Conversion Factor");

    jLabel64.setText("Long/Lat Format");

    jLabel65.setText("Use True Course");

    jLabel66.setText("Magnetic Variation");

    jLabel67.setText("Home Runway");

    jLabel68.setText("Secondary Runway");

    jLabel69.setText("Default Expiration (Days)");

    jLabel70.setText("Page 1 Disclaimer");

    jLabel71.setText("Page 2 Disclaimer");

    jCheckBox23.setSelected(settings.getBooleanForKey(Settings.IS_PRIMARY)
    );
    jCheckBox23.setToolTipText("Each network of Jungle Jepps must have exactly 1 primary instance to control the every other instance.");
    new OnChange(jCheckBox23){     public void onChange(String value){         settings.setValue(Settings.IS_PRIMARY, value);     } };

    repoPath.setText(settings.getStringForKey(Settings.REPOSITORY_PATH)
    );
    repoPath.setToolTipText("The location of the Jungle Jepps Repository");
    repoPath.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            repoPathActionPerformed(evt);
        }
    });
    new OnChange(repoPath){
        public void onChange(String value){
            settings.setValue(Settings.REPOSITORY_PATH, value);
        }
    };

    repoPathBrowse.setText("Browse");
    repoPathBrowse.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            repoPathBrowseActionPerformed(evt);
        }
    });

    primaryDbUri.setText(settings.getStringForKey(Settings.PRIMARY_JDBC_URI)
    );
    primaryDbUri.setToolTipText("Used to setup the Primary Database. Read technical documentation for syntax");
    primaryDbUri.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            primaryDbUriActionPerformed(evt);
        }
    });
    new OnChange(primaryDbUri){
        public void onChange(String value){
            settings.setValue(Settings.PRIMARY_JDBC_URI, value);
        }
    };

    primaryDbClassPath.setText(settings.getStringForKey(Settings.PRIMARY_JDBC_CLASS_PATH)
    );
    primaryDbClassPath.setToolTipText("Read technical documentation for alternate values");
    primaryDbClassPath.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            primaryDbClassPathActionPerformed(evt);
        }
    });
    new OnChange(primaryDbClassPath){
        public void onChange(String value){
            settings.setValue(Settings.PRIMARY_JDBC_CLASS_PATH, value);
        }
    };

    adminPass.setText(settings.getStringForKey(Settings.ADMIN_PASSWORD)
    );
    adminPass.setToolTipText("");
    adminPass.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            adminPassActionPerformed(evt);
        }
    });
    new OnChange(adminPass){
        public void onChange(String value){
            settings.setValue(Settings.ADMIN_PASSWORD, value);
        }
    };

    adminEmail.setText(settings.getStringForKey(Settings.ADMIN_EMAIL)
    );
    adminEmail.setToolTipText("");
    adminEmail.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            adminEmailActionPerformed(evt);
        }
    });
    new OnChange(adminEmail){
        public void onChange(String value){
            settings.setValue(Settings.ADMIN_EMAIL, value);
        }
    };

    altitude.setModel(newComboBoxModel(new String[]{"ft", "m", "yd", "km", "mi"}) );
    altitude.setSelectedItem(settings.getStringForKey(Settings.ALTITUDE_UNITS)
    );
    new OnChange(altitude){
        public void onChange(String value){
            settings.setValue(Settings.ALTITUDE_UNITS, value);
        }
    };

    weight.setModel(newComboBoxModel(new String[]{"lbs", "kg"}) );
    weight.setSelectedItem(settings.getStringForKey(Settings.WEIGHT_UNITS)
    );
    new OnChange(weight){     public void onChange(String value){         settings.setValue(Settings.WEIGHT_UNITS, value);     } };

    distance.setModel(newComboBoxModel(new String[]{"nm"}) );
    distance.setSelectedItem(settings.getStringForKey(Settings.DISTANCE_UNITS)
    );
    new OnChange(distance){     public void onChange(String value){         settings.setValue(Settings.DISTANCE_UNITS, value);     } };

    dimension.setModel(newComboBoxModel(new String[]{"ft", "m", "yd", "km", "mi"}) );
    dimension.setSelectedItem(settings.getStringForKey(Settings.DIMENSION_UNITS)
    );
    new OnChange(dimension){     public void onChange(String value){         settings.setValue(Settings.DIMENSION_UNITS, value);     } };

    distanceConvert.setText(Double.toString(settings.getDoubleForKey(Settings.DISTANCE_CONVERT_FACTOR))
    );
    distanceConvert.setToolTipText("");
    new OnChange(distanceConvert){
        public void onChange(String value){
            settings.setValue(Settings.DISTANCE_CONVERT_FACTOR, Double.valueOf(value) );
        }
    };

    longLatFormat.setModel(newComboBoxModel(new String[]{"DMS", "Decimal"}));
    longLatFormat.setSelectedItem(settings.getStringForKey(Settings.LONG_LAT_FORMAT)
    );
    new OnChange(longLatFormat){
        public void onChange(String value){
            settings.setValue(Settings.LONG_LAT_FORMAT, value);
        }
    };

    trueCourse.setSelected(settings.getBooleanForKey(Settings.IS_TRUE_COURSE)
    );
    trueCourse.setToolTipText("True course or magnetic course");
    new OnChange(trueCourse){
        public void onChange(String value){
            settings.setValue(Settings.IS_TRUE_COURSE, value);
        }
    };

    magVariation.setText(Double.toString(settings.getDoubleForKey(Settings.MAGNETIC_VARIATION))
    );
    magVariation.setToolTipText("Only used if not using true course");
    new OnChange(magVariation){
        public void onChange(String value){
            settings.setValue(Settings.MAGNETIC_VARIATION, Double.valueOf(value) );
        }
    };

    secondaryRunway.setModel(getRunwayIds());
    secondaryRunway.setSelectedItem(settings.getStringForKey(Settings.SECONDARY_RUNWAY)
    );
    secondaryRunway.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            secondaryRunwayActionPerformed(evt);
        }
    });
    new OnChange(secondaryRunway){
        public void onChange(String value){
            settings.setValue(Settings.SECONDARY_RUNWAY, value);
        }
    };

    homeRunway.setModel(getRunwayIds());
    homeRunway.setSelectedItem(settings.getStringForKey(Settings.HOME_RUNWAY)
    );
    homeRunway.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            homeRunwayActionPerformed(evt);
        }
    });
    new OnChange(homeRunway){     public void onChange(String value){         settings.setValue(Settings.HOME_RUNWAY, value);     } };

    defExpiration.setText(settings.getStringForKey(Settings.DEFAULT_EXPIRATION_PERIOD)
    );
    defExpiration.setToolTipText("The default expiration time period in days");
    new OnChange(defExpiration){
        public void onChange(String value){
            settings.setValue(Settings.DEFAULT_EXPIRATION_PERIOD, Integer.valueOf(value) );
        }
    };

    page2Disclaimer.setColumns(20);
    page2Disclaimer.setLineWrap(true);
    page2Disclaimer.setRows(5);
    page2Disclaimer.setText(settings.getStringForKey(Settings.PAGE_2_DISCLAIMER));
    page2Disclaimer.setWrapStyleWord(true);
    new OnChange(page2Disclaimer){     public void onChange(String value){         settings.setValue(Settings.PAGE_2_DISCLAIMER, value);     } };
    page2DisclaimerScroll.setViewportView(page2Disclaimer);

    page1Disclaimer.setColumns(20);
    page1Disclaimer.setLineWrap(true);
    page1Disclaimer.setRows(5);
    page1Disclaimer.setText(settings.getStringForKey(Settings.PAGE_1_DISCLAIMER)
    );
    page1Disclaimer.setWrapStyleWord(true);
    new OnChange(page1Disclaimer){     public void onChange(String value){         settings.setValue(Settings.PAGE_1_DISCLAIMER, value);     } };
    page1DisclaimerScroll.setViewportView(page1Disclaimer);

    javax.swing.GroupLayout settingsMainLayout = new javax.swing.GroupLayout(settingsMain);
    settingsMain.setLayout(settingsMainLayout);
    settingsMainLayout.setHorizontalGroup(
        settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(settingsMainLayout.createSequentialGroup()
            .addGap(10, 10, 10)
            .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel56)
                .addComponent(jLabel55)
                .addComponent(jLabel54)
                .addComponent(jLabel53)
                .addComponent(jLabel59)
                .addComponent(jLabel60)
                .addComponent(jLabel61)
                .addComponent(jLabel62)
                .addComponent(jLabel63)
                .addComponent(jLabel70))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(jCheckBox23)
                .addGroup(settingsMainLayout.createSequentialGroup()
                    .addComponent(repoPath, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(repoPathBrowse))
                .addComponent(primaryDbUri)
                .addComponent(primaryDbClassPath)
                .addComponent(altitude, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(weight, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(distance, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(dimension, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(distanceConvert)
                .addComponent(page1DisclaimerScroll))
            .addGap(20, 20, 20)
            .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel69, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel68, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel67, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel66, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel65, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel64, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel71, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel57, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel58, javax.swing.GroupLayout.Alignment.TRAILING))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(trueCourse)
                .addComponent(magVariation)
                .addComponent(longLatFormat, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(page2DisclaimerScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                .addComponent(defExpiration)
                .addComponent(secondaryRunway, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(homeRunway, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(adminEmail)
                .addComponent(adminPass))
            .addGap(10, 10, 10))
    );
    settingsMainLayout.setVerticalGroup(
        settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(settingsMainLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel53)
                .addComponent(jCheckBox23))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(settingsMainLayout.createSequentialGroup()
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel58)
                        .addComponent(adminEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel57, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(adminPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel64)
                        .addComponent(longLatFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(trueCourse)
                        .addComponent(jLabel65, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel66)
                        .addComponent(magVariation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel67)
                        .addComponent(homeRunway, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel68)
                        .addComponent(secondaryRunway, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel69)
                        .addComponent(defExpiration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel71)
                        .addComponent(page2DisclaimerScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(settingsMainLayout.createSequentialGroup()
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel54)
                        .addComponent(repoPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(repoPathBrowse))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel55)
                        .addComponent(primaryDbUri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel56)
                        .addComponent(primaryDbClassPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel59)
                        .addComponent(altitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel60)
                        .addComponent(weight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel61)
                        .addComponent(distance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel62)
                        .addComponent(dimension, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel63)
                        .addComponent(distanceConvert, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(settingsMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(settingsMainLayout.createSequentialGroup()
                            .addComponent(jLabel70)
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addComponent(page1DisclaimerScroll))))
            .addContainerGap())
    );

    settingsMainScroll.setViewportView(settingsMain);

    jLabel72.setText("Runway Identifier");

    jLabel73.setText("Runway Name");

    jLabel74.setText("Longitude");

    jLabel75.setText("Latitude");

    jLabel76.setText("Inspection N/A");

    jLabel77.setText("Inspection Date");

    jLabel78.setText("Inspector Name");

    jLabel79.setText("Inspection Due");

    jLabel80.setText("Classification");

    jLabel81.setText("Frequency 1");

    jLabel82.setText("Frequency 2");

    jLabel83.setText("Language Greet");

    jLabel84.setText("Elevation");

    jLabel85.setText("Length");

    jLabel86.setText("Width Text");

    jTextField16.setText(settings.getLabel(Field.RUNWAY_IDENTIFIER)
    );
    new OnChange(jTextField16){
        public void onChange(String value){
            settings.setLabel(Field.RUNWAY_IDENTIFIER, value);
        }
    };

    jTextField17.setText(settings.getLabel(Field.RUNWAY_NAME));
    new OnChange(jTextField17){
        public void onChange(String value){
            settings.setLabel(Field.RUNWAY_NAME, value);
        }
    };

    jTextField18.setText(settings.getLabel(Field.LATITUDE)
    );
    new OnChange(jTextField18){
        public void onChange(String value){
            settings.setLabel(Field.LATITUDE, value);
        }
    };

    jTextField19.setText(settings.getLabel(Field.LONGITUDE));
    new OnChange(jTextField19){
        public void onChange(String value){
            settings.setLabel(Field.LONGITUDE, value);
        }
    };

    jTextField20.setText(settings.getLabel(Field.INSPECTION_NA)
    );
    new OnChange(jTextField20){
        public void onChange(String value){
            settings.setLabel(Field.INSPECTION_NA, value);
        }
    };
    jTextField20.addFocusListener(new java.awt.event.FocusAdapter() {
        public void focusLost(java.awt.event.FocusEvent evt) {
            saveFieldLabel(evt);
        }
    });

    jTextField21.setText(settings.getLabel(Field.INSPECTION_DATE));
    new OnChange(jTextField21){
        public void onChange(String value){
            settings.setLabel(Field.INSPECTION_DATE, value);
        }
    };

    jTextField22.setText(settings.getLabel(Field.INSPECTOR_NAME));
    new OnChange(jTextField22){
        public void onChange(String value){
            settings.setLabel(Field.INSPECTOR_NAME, value);
        }
    };

    jTextField23.setText(settings.getLabel(Field.INSPECTION_DUE));
    new OnChange(jTextField23){
        public void onChange(String value){
            settings.setLabel(Field.INSPECTION_DUE, value);
        }
    };

    jTextField24.setText(settings.getLabel(Field.CLASSIFICATION));
    new OnChange(jTextField24){
        public void onChange(String value){
            settings.setLabel(Field.CLASSIFICATION, value);
        }
    };

    jTextField25.setText(settings.getLabel(Field.FREQUENCY_1)
    );
    new OnChange(jTextField25){
        public void onChange(String value){
            settings.setLabel(Field.FREQUENCY_1, value);
        }
    };

    jTextField26.setText(settings.getLabel(Field.FREQUENCY_2)
    );
    new OnChange(jTextField26){
        public void onChange(String value){
            settings.setLabel(Field.FREQUENCY_2, value);
        }
    };

    jTextField27.setText(settings.getLabel(Field.LANGUAGE_GREET)
    );
    new OnChange(jTextField27){
        public void onChange(String value){
            settings.setLabel(Field.LANGUAGE_GREET, value);
        }
    };

    jTextField28.setText(settings.getLabel(Field.ELEVATION));
    new OnChange(jTextField28){
        public void onChange(String value){
            settings.setLabel(Field.ELEVATION, value);
        }
    };

    jTextField29.setText(settings.getLabel(Field.LENGTH));
    new OnChange(jTextField29){
        public void onChange(String value){
            settings.setLabel(Field.LENGTH, value);
        }
    };

    jTextField30.setText(settings.getLabel(Field.WIDTH_TEXT));
    new OnChange(jTextField30){
        public void onChange(String value){
            settings.setLabel(Field.WIDTH_TEXT, value);
        }
    };

    jLabel87.setText("TDZ Slope");

    jTextField31.setText(settings.getLabel(Field.TDZ_SLOPE));
    new OnChange(jTextField31){
        public void onChange(String value){
            settings.setLabel(Field.TDZ_SLOPE, value);
        }
    };

    jLabel88.setText("IAS Adjustment");

    jTextField32.setText(settings.getLabel(Field.IAS_ADJUSTMENT));
    new OnChange(jTextField32){
        public void onChange(String value){
            settings.setLabel(Field.IAS_ADJUSTMENT, value);
        }
    };

    jLabel89.setText("Precipitation On Screen");

    jLabel90.setText("Runway A");

    jLabel91.setText("A Takeoff Restriction");

    jLabel92.setText("A Takeoff Note");

    jTextField33.setText(settings.getLabel(Field.PRECIPITATION_ON_SCREEN));
    new OnChange(jTextField33){
        public void onChange(String value){
            settings.setLabel(Field.PRECIPITATION_ON_SCREEN, value);
        }
    };

    jTextField34.setText(settings.getLabel(Field.RUNWAY_A));
    new OnChange(jTextField34){
        public void onChange(String value){
            settings.setLabel(Field.RUNWAY_A, value);
        }
    };

    jTextField35.setText(settings.getLabel(Field.A_TAKEOFF_RESTRICTION));
    new OnChange(jTextField35){
        public void onChange(String value){
            settings.setLabel(Field.A_TAKEOFF_RESTRICTION, value);
        }
    };

    jTextField36.setText(settings.getLabel(Field.A_TAKEOFF_NOTE));
    new OnChange(jTextField36){
        public void onChange(String value){
            settings.setLabel(Field.A_TAKEOFF_NOTE, value);
        }
    };

    jLabel93.setText("A Landing Restriction");

    jTextField37.setText(settings.getLabel(Field.A_LANDING_RESTRICTION));
    new OnChange(jTextField37){
        public void onChange(String value){
            settings.setLabel(Field.A_LANDING_RESTRICTION, value);
        }
    };

    jLabel94.setText("A Landing Note");

    jTextField38.setText(settings.getLabel(Field.A_LANDING_NOTE));
    new OnChange(jTextField38){
        public void onChange(String value){
            settings.setLabel(Field.A_LANDING_NOTE, value);
        }
    };

    jLabel95.setText("Runway B");

    jLabel96.setText("B Takeoff Restriction");

    jLabel97.setText("B Takeoff Note");

    jLabel98.setText("B Landing Restriction");

    jTextField39.setText(settings.getLabel(Field.RUNWAY_B));
    new OnChange(jTextField39){
        public void onChange(String value){
            settings.setLabel(Field.RUNWAY_B, value);
        }
    };

    jTextField40.setText(settings.getLabel(Field.B_TAKEOFF_RESTRICTION));
    new OnChange(jTextField40){
        public void onChange(String value){
            settings.setLabel(Field.B_TAKEOFF_RESTRICTION, value);
        }
    };

    jTextField41.setText(settings.getLabel(Field.B_TAKEOFF_NOTE));
    new OnChange(jTextField41){
        public void onChange(String value){
            settings.setLabel(Field.B_TAKEOFF_NOTE, value);
        }
    };

    jTextField42.setText(settings.getLabel(Field.B_LANDING_RESTRICTION));
    new OnChange(jTextField42){
        public void onChange(String value){
            settings.setLabel(Field.B_LANDING_RESTRICTION, value);
        }
    };

    jLabel99.setText("B Landing Note");

    jTextField43.setText(settings.getLabel(Field.B_TAKEOFF_NOTE));
    new OnChange(jTextField43){
        public void onChange(String value){
            settings.setLabel(Field.B_LANDING_NOTE, value);
        }
    };

    javax.swing.GroupLayout settingsLabelsLayout = new javax.swing.GroupLayout(settingsLabels);
    settingsLabels.setLayout(settingsLabelsLayout);
    settingsLabelsLayout.setHorizontalGroup(
        settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(settingsLabelsLayout.createSequentialGroup()
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel99)
                .addComponent(jLabel98)
                .addComponent(jLabel97)
                .addComponent(jLabel96)
                .addComponent(jLabel95)
                .addComponent(jLabel94)
                .addComponent(jLabel93)
                .addComponent(jLabel92)
                .addComponent(jLabel91)
                .addComponent(jLabel90)
                .addComponent(jLabel89)
                .addComponent(jLabel88)
                .addComponent(jLabel87)
                .addComponent(jLabel86)
                .addComponent(jLabel85)
                .addComponent(jLabel84)
                .addComponent(jLabel83)
                .addComponent(jLabel81)
                .addComponent(jLabel80)
                .addComponent(jLabel79)
                .addComponent(jLabel78)
                .addComponent(jLabel77)
                .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel75, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel74, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(settingsLabelsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel73)
                            .addComponent(jLabel72)))
                    .addComponent(jLabel76, javax.swing.GroupLayout.Alignment.TRAILING))
                .addComponent(jLabel82))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField18, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField22, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField23, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField24, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField25, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField26, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField27, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField28, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField29, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField30, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField31, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField32, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField33, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField34, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField35, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField36, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField37, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField38, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField39, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField40, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField41, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField42, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField43, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(51, Short.MAX_VALUE))
    );
    settingsLabelsLayout.setVerticalGroup(
        settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(settingsLabelsLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel72)
                .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel73)
                .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel74)
                .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel75)
                .addComponent(jTextField18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel76)
                .addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel77)
                .addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel78)
                .addComponent(jTextField22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel79)
                .addComponent(jTextField23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel80)
                .addComponent(jTextField24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel81)
                .addComponent(jTextField25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextField26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel82))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel83)
                .addComponent(jTextField27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel84)
                .addComponent(jTextField28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel85)
                .addComponent(jTextField29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel86)
                .addComponent(jTextField30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel87)
                .addComponent(jTextField31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel88)
                .addComponent(jTextField32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel89)
                .addComponent(jTextField33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel90)
                .addComponent(jTextField34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel91)
                .addComponent(jTextField35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel92)
                .addComponent(jTextField36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel93)
                .addComponent(jTextField37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel94)
                .addComponent(jTextField38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel95)
                .addComponent(jTextField39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel96)
                .addComponent(jTextField40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel97)
                .addComponent(jTextField41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel98)
                .addComponent(jTextField42, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel99)
                .addComponent(jTextField43, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    settingsLabelsScroll.setViewportView(settingsLabels);

    jLabel100.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
    jLabel100.setText("General Settings");

    jLabel101.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
    jLabel101.setText("Field Labels");

    javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
    jPanel6.setLayout(jPanel6Layout);
    jPanel6Layout.setHorizontalGroup(
        jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 552, Short.MAX_VALUE)
    );
    jPanel6Layout.setVerticalGroup(
        jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 228, Short.MAX_VALUE)
    );

    jScrollPane22.setViewportView(jPanel6);

    javax.swing.GroupLayout settingsTabLayout = new javax.swing.GroupLayout(settingsTab);
    settingsTab.setLayout(settingsTabLayout);
    settingsTabLayout.setHorizontalGroup(
        settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(settingsTabLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 946, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, settingsTabLayout.createSequentialGroup()
                        .addGroup(settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel101)
                            .addGroup(settingsTabLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(settingsLabelsScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel100, javax.swing.GroupLayout.Alignment.LEADING))
                .addComponent(settingsMainScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 977, javax.swing.GroupLayout.PREFERRED_SIZE)))
    );
    settingsTabLayout.setVerticalGroup(
        settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(settingsTabLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel100)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(settingsMainScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(5, 5, 5)
            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel101)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(settingsLabelsScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(53, Short.MAX_VALUE))
    );

    tabView.addTab("Settings", settingsTab);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(tabView, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(aircraftChooserLabel)
            .addGap(18, 18, 18)
            .addComponent(aircraftChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addComponent(tabView, javax.swing.GroupLayout.PREFERRED_SIZE, 673, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(5, 5, 5)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(aircraftChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(aircraftChooserLabel))
            .addContainerGap())
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents

  private void previewViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewViewButtonActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_previewViewButtonActionPerformed

  private void latitudeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_latitudeActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_latitudeActionPerformed

  private void longitudeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_longitudeActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_longitudeActionPerformed

  private void inspectionDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inspectionDateActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_inspectionDateActionPerformed

  private void langGreetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_langGreetActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_langGreetActionPerformed

  private void precipHLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_precipHLActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_precipHLActionPerformed

  private void elevationHLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elevationHLActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_elevationHLActionPerformed

  private void lengthHLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lengthHLActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_lengthHLActionPerformed

  private void widthHLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_widthHLActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_widthHLActionPerformed

  private void iasAdjustHLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iasAdjustHLActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_iasAdjustHLActionPerformed

  private void tdZoneHLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tdZoneHLActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_tdZoneHLActionPerformed

  private void jCheckBox9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox9ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox9ActionPerformed

  private void elevationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elevationActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_elevationActionPerformed

  private void lengthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lengthActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_lengthActionPerformed

  private void widthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_widthActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_widthActionPerformed

  private void tdZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tdZoneActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_tdZoneActionPerformed

  private void iasAdjustActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iasAdjustActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_iasAdjustActionPerformed

  private void jCheckBox16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox16ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox16ActionPerformed

  private void jCheckBox17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox17ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox17ActionPerformed

  private void jCheckBox18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox18ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox18ActionPerformed

  private void jCheckBox19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox19ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox19ActionPerformed

    private void homeRunwayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homeRunwayActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_homeRunwayActionPerformed

    private void secondaryRunwayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secondaryRunwayActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_secondaryRunwayActionPerformed

    private void adminEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_adminEmailActionPerformed

    private void adminPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminPassActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_adminPassActionPerformed

    private void primaryDbClassPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_primaryDbClassPathActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_primaryDbClassPathActionPerformed

    private void primaryDbUriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_primaryDbUriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_primaryDbUriActionPerformed

    private void repoPathBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_repoPathBrowseActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose Repository Location");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath() + File.separator;
            repoPath.setText( path );
            settings.setValue(Settings.REPOSITORY_PATH, path);
        }
    }//GEN-LAST:event_repoPathBrowseActionPerformed

    private void repoPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_repoPathActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_repoPathActionPerformed

    private void saveFieldLabel(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_saveFieldLabel
        // TODO add your handling code here:
    }//GEN-LAST:event_saveFieldLabel

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      try {
          db.close();
          settings.save();
      } catch (DatabaseException ex) {
          Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
      }
    }//GEN-LAST:event_formWindowClosing

  private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    File image = showImageChooser();
    if(image != null){
      try {
        image = Repository.copyImageFile(runway, image);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      
      runway.put(Field.IMAGE_PATH, image.getAbsolutePath());
      if(runway.isModified()){
        runway.save();
      }
      jLabel17.setIcon(new ImageIcon(image.getAbsolutePath()));
    }
  }//GEN-LAST:event_jButton3ActionPerformed

  private void inspectionDateChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inspectionDateChooserActionPerformed
    
    JCalendar cal = new JCalendar(new Date());
    
    cal.addPropertyChangeListener(new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if("calendar".equals(evt.getPropertyName())) {
          inspectionDate.setText(((GregorianCalendar) evt.getNewValue()).getTime().toString());

        }
      }
    });
    
    JDialog d = new JDialog(this);
    d.getContentPane().add(cal);
    d.pack();
    d.setLocationRelativeTo(this);
    d.setVisible(true);
  }//GEN-LAST:event_inspectionDateChooserActionPerformed

    private void runwayComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runwayComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_runwayComboBoxActionPerformed

    private void jCheckBox24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox24ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox24ActionPerformed

    private void jCheckBox25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox25ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox25ActionPerformed

    private void jCheckBox26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox26ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox26ActionPerformed

    private void jCheckBox27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox27ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox27ActionPerformed

    private void jCheckBox10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox10ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox10ActionPerformed

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

    
    
    javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    
    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        new NewJFrame().setVisible(true);
      }
    });
  }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField aLandingWeight;
    private javax.swing.JLabel aLandingWeightLabel;
    private javax.swing.JTextArea aLandingWeightNotes;
    private javax.swing.JTextField aTakeoffWeight;
    private javax.swing.JLabel aTakeoffWeightLabel;
    private javax.swing.JTextArea aTakeoffWeightNotes;
    private javax.swing.JTextField adminEmail;
    private javax.swing.JTextField adminPass;
    private javax.swing.JComboBox aircraftChooser;
    private javax.swing.JLabel aircraftChooserLabel;
    private javax.swing.JLabel aircraftDisclaimerLabel;
    private javax.swing.JComboBox altitude;
    private javax.swing.JTextField bLandingWeight;
    private javax.swing.JLabel bLandingWeightLabel;
    private javax.swing.JTextArea bLandingWeightNotes;
    private javax.swing.JTextField bTakeoffWeight;
    private javax.swing.JLabel bTakeoffWeightLabel;
    private javax.swing.JTextArea bTakeoffWeightNotes;
    private javax.swing.JScrollPane changeLogScroll;
    private javax.swing.JComboBox classification;
    private javax.swing.JLabel classificationLabel;
    private javax.swing.JTextField defExpiration;
    private javax.swing.JComboBox dimension;
    private javax.swing.JComboBox distance;
    private javax.swing.JTextField distanceConvert;
    private javax.swing.JPanel documentsTab;
    private javax.swing.JTextField elevation;
    private javax.swing.JCheckBox elevationHL;
    private javax.swing.JLabel elevationLabel;
    private javax.swing.JComboBox freq1;
    private javax.swing.JLabel freq1Label;
    private javax.swing.JComboBox freq2;
    private javax.swing.JLabel freq2Label;
    private javax.swing.JComboBox homeRunway;
    private javax.swing.JTextField iasAdjust;
    private javax.swing.JCheckBox iasAdjustHL;
    private javax.swing.JLabel iasAdjustLabel;
    private javax.swing.JPanel imagePickerFieldset;
    private javax.swing.JPanel informationFieldset;
    private javax.swing.JCheckBox inspectionCheckbox;
    private javax.swing.JComboBox inspectionCompleted;
    private javax.swing.JLabel inspectionCompletedLabel;
    private javax.swing.JTextField inspectionDate;
    private javax.swing.JButton inspectionDateChooser;
    private javax.swing.JLabel inspectionDateLabel;
    private javax.swing.JPanel inspectionFieldset;
    private javax.swing.JLabel inspectionLabel;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox16;
    private javax.swing.JCheckBox jCheckBox17;
    private javax.swing.JCheckBox jCheckBox18;
    private javax.swing.JCheckBox jCheckBox19;
    private javax.swing.JCheckBox jCheckBox20;
    private javax.swing.JCheckBox jCheckBox21;
    private javax.swing.JCheckBox jCheckBox22;
    private javax.swing.JCheckBox jCheckBox23;
    private javax.swing.JCheckBox jCheckBox24;
    private javax.swing.JCheckBox jCheckBox25;
    private javax.swing.JCheckBox jCheckBox26;
    private javax.swing.JCheckBox jCheckBox27;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JLabel jLabel100;
    private javax.swing.JLabel jLabel101;
    private javax.swing.JLabel jLabel102;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel91;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JLabel jLabel93;
    private javax.swing.JLabel jLabel94;
    private javax.swing.JLabel jLabel95;
    private javax.swing.JLabel jLabel96;
    private javax.swing.JLabel jLabel97;
    private javax.swing.JLabel jLabel98;
    private javax.swing.JLabel jLabel99;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane20;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane23;
    private javax.swing.JScrollPane jScrollPane24;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextArea jTextArea10;
    private javax.swing.JTextArea jTextArea11;
    private javax.swing.JTextArea jTextArea12;
    private javax.swing.JTextArea jTextArea13;
    private javax.swing.JTextArea jTextArea14;
    private javax.swing.JTextArea jTextArea15;
    private javax.swing.JTextArea jTextArea16;
    private javax.swing.JTextArea jTextArea17;
    private javax.swing.JTextArea jTextArea18;
    private javax.swing.JTextArea jTextArea5;
    private javax.swing.JTextArea jTextArea6;
    private javax.swing.JTextArea jTextArea7;
    private javax.swing.JTextArea jTextArea8;
    private javax.swing.JTextArea jTextArea9;
    private javax.swing.JTextField jTextField16;
    private javax.swing.JTextField jTextField17;
    private javax.swing.JTextField jTextField18;
    private javax.swing.JTextField jTextField19;
    private javax.swing.JTextField jTextField20;
    private javax.swing.JTextField jTextField21;
    private javax.swing.JTextField jTextField22;
    private javax.swing.JTextField jTextField23;
    private javax.swing.JTextField jTextField24;
    private javax.swing.JTextField jTextField25;
    private javax.swing.JTextField jTextField26;
    private javax.swing.JTextField jTextField27;
    private javax.swing.JTextField jTextField28;
    private javax.swing.JTextField jTextField29;
    private javax.swing.JTextField jTextField30;
    private javax.swing.JTextField jTextField31;
    private javax.swing.JTextField jTextField32;
    private javax.swing.JTextField jTextField33;
    private javax.swing.JTextField jTextField34;
    private javax.swing.JTextField jTextField35;
    private javax.swing.JTextField jTextField36;
    private javax.swing.JTextField jTextField37;
    private javax.swing.JTextField jTextField38;
    private javax.swing.JTextField jTextField39;
    private javax.swing.JTextField jTextField40;
    private javax.swing.JTextField jTextField41;
    private javax.swing.JTextField jTextField42;
    private javax.swing.JTextField jTextField43;
    private javax.swing.JTextField langGreet;
    private javax.swing.JLabel langGreetLabel;
    private javax.swing.JLabel latLabel;
    private javax.swing.JTextField latitude;
    private javax.swing.JTextField length;
    private javax.swing.JCheckBox lengthHL;
    private javax.swing.JLabel lengthLabel;
    private javax.swing.JLabel longLabel;
    private javax.swing.JComboBox longLatFormat;
    private javax.swing.JTextField longitude;
    private javax.swing.JTextField magVariation;
    private javax.swing.JPanel notesTab;
    private javax.swing.JTextArea page1Disclaimer;
    private javax.swing.JScrollPane page1DisclaimerScroll;
    private javax.swing.JTextArea page2Disclaimer;
    private javax.swing.JScrollPane page2DisclaimerScroll;
    private javax.swing.JComboBox pilotInspection;
    private javax.swing.JLabel pilotInspectionLabel;
    private javax.swing.JComboBox precip;
    private javax.swing.JCheckBox precipHL;
    private javax.swing.JLabel precipLabel;
    private javax.swing.JButton previewViewButton;
    private javax.swing.JTextField primaryDbClassPath;
    private javax.swing.JTextField primaryDbUri;
    private javax.swing.JButton publishButton;
    private javax.swing.JTextField repoPath;
    private javax.swing.JButton repoPathBrowse;
    private javax.swing.JLabel runwayALabel;
    private javax.swing.JTextField runwayAName;
    private javax.swing.JLabel runwayBLabel;
    private javax.swing.JTextField runwayBName;
    private javax.swing.JLabel runwayChooserLabel;
    private javax.swing.JComboBox runwayComboBox;
    private javax.swing.JPanel runwayMain;
    private javax.swing.JScrollPane runwayMainScroll;
    private javax.swing.JPanel runwayTab;
    private javax.swing.JPanel runwaysFieldset;
    private javax.swing.JComboBox secondaryRunway;
    private javax.swing.JPanel settingsLabels;
    private javax.swing.JScrollPane settingsLabelsScroll;
    private javax.swing.JPanel settingsMain;
    private javax.swing.JScrollPane settingsMainScroll;
    private javax.swing.JPanel settingsTab;
    private javax.swing.JTabbedPane tabView;
    private javax.swing.JTextField tdZone;
    private javax.swing.JCheckBox tdZoneHL;
    private javax.swing.JLabel tdzoneLabel;
    private javax.swing.JPanel topographyFieldset;
    private javax.swing.JCheckBox trueCourse;
    private javax.swing.JComboBox weight;
    private javax.swing.JTextField width;
    private javax.swing.JCheckBox widthHL;
    private javax.swing.JLabel widthLabel;
    // End of variables declaration//GEN-END:variables
}
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
import com.toedter.calendar.JDateChooser;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JFrame;

/**
 *
 * @author Timothy
 */
public class NewJFrame extends javax.swing.JFrame {
  private SettingsManager settings;
  private DatabaseConnection db;
  /**
   * Creates new form NewJFrame
   */
  public NewJFrame() {
    settings = DatabaseManager.getSettings();
    db = DatabaseManager.getDatabase();
    initComponents();
  }
  
  private ComboBoxModel getAircraftIds() {
    try {
      return new javax.swing.DefaultComboBoxModel(DatabaseManager.getDatabase().getAllAircraftIds());
    } catch (DatabaseException ex) {
      ex.printStackTrace();
    }
    return null;
  }
  
  private ComboBoxModel getRunwayIds() {
    try {
      return new javax.swing.DefaultComboBoxModel(DatabaseManager.getDatabase().getAllRunwayIds((String) jComboBox1.getSelectedItem()));
    } catch (DatabaseException ex) {
      ex.printStackTrace();
    }
    return null;
  }
  
  private String getRunwayId() {
     return (String) jComboBox4.getSelectedItem();
  }
  
  private String getAircraftId() {
    return (String) jComboBox1.getSelectedItem();
  }
  
  private String getField(Object key){
    try {
      DatabaseConnection db = DatabaseManager.getDatabase();
      Runway r = db.getRunway( (String)jComboBox4.getSelectedItem(), (String) jComboBox1.getSelectedItem());
      return r.get(key);
    } catch (DatabaseException ex) {
      ex.printStackTrace();
    }
    return null;
    
  }
  
  private void showError(Exception e){
      
  }
  
  private ComboBoxModel runwayComboBox(){
      String[] runs;
      try{
          runs = db.getAllRunwayIds();
      }catch(DatabaseException e){
          showError(e);
          runs = new String[]{"<Error Loading Runways>"};
      }    
      
      return newComboBoxModel(runs);
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
         
         public JJDefaultsComboBoxModel(Field field){
            //Construct upper model
            super( null ); 
            
            //Get current defualts
            String[] defaults  = settings.getDefaults(field);
            
            //The the defaults into the super.options String[] 
            loadDefaults(defaults);
            
            //Add default item to bottom of list
            super.options[ super.options.length - 1 ] = defaultChangeOption;
            
            //Keep a handle on the field to load the 
            this.field = field;
         }
         
         //Helper function
         private void loadDefaults(String[] defs){
            //Construct new array with an extra spot at the bottom
            super.options = new String[defs.length + 1];
            
            //Copy defaults to options array to be displayed in menu drop down
            for(int i = 0; i < defs.length; i++)
                super.options[i] = defs[i];
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
                         String[] newDefaults = result.split("\n");

                         //Save defaults in settings
                         settings.setDefaults(field, newDefaults);
                         
                         //Save the new settings in this runtime
                         loadDefaults(newDefaults);
                         
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

    jComboBox1 = new javax.swing.JComboBox();
    jLabel16 = new javax.swing.JLabel();
    jTabbedPane1 = new javax.swing.JTabbedPane();
    jPanel1 = new javax.swing.JPanel();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
    jButton2 = new javax.swing.JButton();
    jScrollPane23 = new javax.swing.JScrollPane();
    jPanel14 = new javax.swing.JPanel();
    jScrollPane2 = new javax.swing.JScrollPane();
    jTable2 = new javax.swing.JTable();
    jPanel8 = new javax.swing.JPanel();
    jButton3 = new javax.swing.JButton();
    jLabel17 = new javax.swing.JLabel();
    jComboBox4 = new javax.swing.JComboBox();
    jLabel6 = new javax.swing.JLabel();
    jPanel9 = new javax.swing.JPanel();
    jLabel12 = new javax.swing.JLabel();
    jLabel13 = new javax.swing.JLabel();
    jLabel14 = new javax.swing.JLabel();
    jCheckBox3 = new javax.swing.JCheckBox();
    jLabel15 = new javax.swing.JLabel();
    jCheckBox4 = new javax.swing.JCheckBox();
    jCheckBox5 = new javax.swing.JCheckBox();
    jLabel19 = new javax.swing.JLabel();
    jLabel20 = new javax.swing.JLabel();
    jCheckBox6 = new javax.swing.JCheckBox();
    jCheckBox7 = new javax.swing.JCheckBox();
    jTextField11 = new javax.swing.JTextField();
    jTextField12 = new javax.swing.JTextField();
    jTextField13 = new javax.swing.JTextField();
    jTextField14 = new javax.swing.JTextField();
    jTextField15 = new javax.swing.JTextField();
    jPanel4 = new javax.swing.JPanel();
    jCheckBox1 = new javax.swing.JCheckBox();
    jLabel3 = new javax.swing.JLabel();
    jComboBox2 = new javax.swing.JComboBox();
    jLabel4 = new javax.swing.JLabel();
    jComboBox3 = new javax.swing.JComboBox();
    jLabel5 = new javax.swing.JLabel();
    jTextField4 = new javax.swing.JTextField();
    jButton6 = new javax.swing.JButton();
    jLabel18 = new javax.swing.JLabel();
    jPanel5 = new javax.swing.JPanel();
    jComboBox5 = new javax.swing.JComboBox();
    jLabel7 = new javax.swing.JLabel();
    jLabel8 = new javax.swing.JLabel();
    jLabel9 = new javax.swing.JLabel();
    jComboBox6 = new javax.swing.JComboBox();
    jComboBox7 = new javax.swing.JComboBox();
    jLabel10 = new javax.swing.JLabel();
    jTextField3 = new javax.swing.JTextField();
    jLabel11 = new javax.swing.JLabel();
    jComboBox8 = new javax.swing.JComboBox();
    jCheckBox2 = new javax.swing.JCheckBox();
    jPanel3 = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    jTextField1 = new javax.swing.JTextField();
    jTextField2 = new javax.swing.JTextField();
    jLabel1 = new javax.swing.JLabel();
    jPanel10 = new javax.swing.JPanel();
    jLabel21 = new javax.swing.JLabel();
    jLabel22 = new javax.swing.JLabel();
    jLabel23 = new javax.swing.JLabel();
    jTextField5 = new javax.swing.JTextField();
    jTextField6 = new javax.swing.JTextField();
    jTextField7 = new javax.swing.JTextField();
    jScrollPane1 = new javax.swing.JScrollPane();
    jTextArea1 = new javax.swing.JTextArea();
    jScrollPane3 = new javax.swing.JScrollPane();
    jTextArea2 = new javax.swing.JTextArea();
    jScrollPane4 = new javax.swing.JScrollPane();
    jTextArea3 = new javax.swing.JTextArea();
    jTextField8 = new javax.swing.JTextField();
    jTextField9 = new javax.swing.JTextField();
    jTextField10 = new javax.swing.JTextField();
    jScrollPane5 = new javax.swing.JScrollPane();
    jTextArea4 = new javax.swing.JTextArea();
    jLabel24 = new javax.swing.JLabel();
    jCheckBox8 = new javax.swing.JCheckBox();
    jCheckBox10 = new javax.swing.JCheckBox();
    jCheckBox9 = new javax.swing.JCheckBox();
    jCheckBox11 = new javax.swing.JCheckBox();
    jLabel25 = new javax.swing.JLabel();
    jCheckBox12 = new javax.swing.JCheckBox();
    jCheckBox13 = new javax.swing.JCheckBox();
    jCheckBox14 = new javax.swing.JCheckBox();
    jCheckBox15 = new javax.swing.JCheckBox();
    jLabel26 = new javax.swing.JLabel();
    jCheckBox16 = new javax.swing.JCheckBox();
    jCheckBox17 = new javax.swing.JCheckBox();
    jCheckBox18 = new javax.swing.JCheckBox();
    jCheckBox19 = new javax.swing.JCheckBox();
    jButton1 = new javax.swing.JButton();
    jPanel2 = new javax.swing.JPanel();
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
    jPanel7 = new javax.swing.JPanel();
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
    settingsTabView = new javax.swing.JPanel();
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

    jComboBox1.setModel(getAircraftIds());

    jLabel16.setLabelFor(jComboBox1);
    jLabel16.setText("Aircraft Type");

    jButton2.setText("Publish / Unavailable");

    jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Change Log"));

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
    jScrollPane2.setViewportView(jTable2);
    jTable2.getColumnModel().getColumn(2).setHeaderValue("Change");

    jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Image"));
    jPanel8.setName("test"); // NOI18N

    jButton3.setIcon(new ImageIcon(getField(Field.IMAGE_PATH)));
    jButton3.setText("Select New Image File");
    jButton3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton3ActionPerformed(evt);
      }
    });

    jLabel17.setIcon(new ImageIcon(getField(Field.IMAGE_PATH)));

    javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
    jPanel8.setLayout(jPanel8Layout);
    jPanel8Layout.setHorizontalGroup(
      jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel8Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(jPanel8Layout.createSequentialGroup()
            .addComponent(jButton3)
            .addGap(0, 237, Short.MAX_VALUE)))
        .addContainerGap())
    );
    jPanel8Layout.setVerticalGroup(
      jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel8Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGap(18, 18, 18)
        .addComponent(jButton3)
        .addContainerGap())
    );

    jComboBox4.setModel(getRunwayIds());

    jLabel6.setText(SettingsManager.getInstance().getLabel(Field.RUNWAY_IDENTIFIER));

    jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Topography"));

    jLabel12.setText(SettingsManager.getInstance().getLabel(Field.ELEVATION) + " (" + SettingsManager.getInstance().getStringForKey(Settings.ALTITUDE_UNITS) + ')');

  jLabel13.setText("Length (<Units>)");

  jLabel14.setText("Width (<Units>)");

  jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox3ActionPerformed(evt);
    }
  });

  jLabel15.setText("Highlight");

  jCheckBox4.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox4ActionPerformed(evt);
    }
  });

  jCheckBox5.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox5ActionPerformed(evt);
    }
  });

  jLabel19.setText("TD Slope Zone");

  jLabel20.setText("Gnd Speed");

  jCheckBox6.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox6ActionPerformed(evt);
    }
  });

  jCheckBox7.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox7ActionPerformed(evt);
    }
  });

  jTextField11.setText("text");
  jTextField11.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jTextField11ActionPerformed(evt);
    }
  });

  jTextField12.setText("text");
  jTextField12.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jTextField12ActionPerformed(evt);
    }
  });

  jTextField13.setText("text");
  jTextField13.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jTextField13ActionPerformed(evt);
    }
  });

  jTextField14.setText("text");
  jTextField14.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jTextField14ActionPerformed(evt);
    }
  });

  jTextField15.setText("text");
  jTextField15.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jTextField15ActionPerformed(evt);
    }
  });

  javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
  jPanel9.setLayout(jPanel9Layout);
  jPanel9Layout.setHorizontalGroup(
    jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel9Layout.createSequentialGroup()
      .addGap(20, 20, 20)
      .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
        .addComponent(jLabel20)
        .addComponent(jLabel19)
        .addGroup(jPanel9Layout.createSequentialGroup()
          .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(jCheckBox7)
            .addComponent(jCheckBox6)
            .addComponent(jCheckBox5)
            .addComponent(jCheckBox4)
            .addComponent(jCheckBox3)
            .addComponent(jLabel15))
          .addGap(18, 18, 18)
          .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING))))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
        .addComponent(jTextField11, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
        .addComponent(jTextField12)
        .addComponent(jTextField13)
        .addComponent(jTextField14)
        .addComponent(jTextField15))
      .addGap(0, 53, Short.MAX_VALUE))
  );
  jPanel9Layout.setVerticalGroup(
    jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel9Layout.createSequentialGroup()
      .addContainerGap()
      .addComponent(jLabel15)
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(jLabel12)
        .addComponent(jCheckBox3)
        .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(jLabel13)
        .addComponent(jCheckBox4)
        .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(jCheckBox5)
        .addComponent(jLabel14)
        .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(jLabel19)
        .addComponent(jCheckBox7)
        .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(jLabel20)
        .addComponent(jCheckBox6)
        .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addContainerGap(94, Short.MAX_VALUE))
  );

  jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Inspection"));

  jCheckBox1.setText(SettingsManager.getInstance().getLabel(Field.INSPECTION_NA));

  jLabel3.setText(SettingsManager.getInstance().getLabel(Field.INSPECTION_DATE));

  jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

  jLabel4.setText(SettingsManager.getInstance().getLabel(Field.INSPECTOR_NAME));

  jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

  jLabel5.setText(SettingsManager.getInstance().getLabel(Field.INSPECTION_DUE));

  jTextField4.setText("date");
  jTextField4.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jTextField4ActionPerformed(evt);
    }
  });

  jButton6.setText("Select");
  jButton6.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jButton6ActionPerformed(evt);
    }
  });

  javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
  jPanel4.setLayout(jPanel4Layout);
  jPanel4Layout.setHorizontalGroup(
    jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel4Layout.createSequentialGroup()
      .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel4Layout.createSequentialGroup()
          .addContainerGap()
          .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING))
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
            .addComponent(jComboBox3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTextField4)
            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGap(18, 18, 18)
          .addComponent(jButton6))
        .addGroup(jPanel4Layout.createSequentialGroup()
          .addGap(10, 10, 10)
          .addComponent(jCheckBox1)))
      .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
  );
  jPanel4Layout.setVerticalGroup(
    jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel4Layout.createSequentialGroup()
      .addContainerGap()
      .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGroup(jPanel4Layout.createSequentialGroup()
          .addComponent(jCheckBox1)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(jLabel3)))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(jLabel4)
        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(jLabel5)
        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addComponent(jButton6))
      .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
  );

  jLabel18.setText("This Diagram is for " + getAircraftId() + " aircraft only.");

  jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Information"));

  jComboBox5.setModel(new JJDefaultsComboBoxModel(Field.CLASSIFICATION));

  jLabel7.setText(SettingsManager.getInstance().getLabel(Field.CLASSIFICATION));

  jLabel8.setText(SettingsManager.getInstance().getLabel(Field.FREQUENCY_1));

  jLabel9.setText(SettingsManager.getInstance().getLabel(Field.FREQUENCY_2));

  jComboBox6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

  jComboBox7.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

  jLabel10.setText(SettingsManager.getInstance().getLabel(Field.LANGUAGE_GREET));

  jTextField3.setText("text");
  jTextField3.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jTextField3ActionPerformed(evt);
    }
  });

  jLabel11.setText(SettingsManager.getInstance().getLabel(Field.PRECIPITATION_ON_SCREEN));

  jComboBox8.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

  jCheckBox2.setText("Highlight");
  jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox2ActionPerformed(evt);
    }
  });

  javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
  jPanel5.setLayout(jPanel5Layout);
  jPanel5Layout.setHorizontalGroup(
    jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel5Layout.createSequentialGroup()
      .addContainerGap()
      .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel5Layout.createSequentialGroup()
          .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jLabel11)
            .addComponent(jLabel10)
            .addComponent(jLabel9)
            .addComponent(jLabel8)
            .addComponent(jLabel7))
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
          .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jComboBox5, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jComboBox6, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jComboBox7, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTextField3)
            .addComponent(jComboBox8, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addGroup(jPanel5Layout.createSequentialGroup()
          .addComponent(jCheckBox2)
          .addGap(0, 0, Short.MAX_VALUE)))
      .addContainerGap())
  );
  jPanel5Layout.setVerticalGroup(
    jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel5Layout.createSequentialGroup()
      .addContainerGap()
      .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(jLabel7)
        .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(jLabel8)
        .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
      .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(jLabel9)
        .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
      .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(jLabel10)
        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
      .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(jLabel11)
        .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addComponent(jCheckBox2)
      .addContainerGap(47, Short.MAX_VALUE))
  );

  jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Location"));

  jLabel2.setText(SettingsManager.getInstance().getLabel(Field.LONGITUDE));

  jTextField1.setText(getField(Field.LONGITUDE));
  jTextField1.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jTextField1ActionPerformed(evt);
    }
  });

  jTextField2.setText("0");
  jTextField2.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jTextField2ActionPerformed(evt);
    }
  });

  jLabel1.setText(SettingsManager.getInstance().getLabel(Field.LATITUDE));

  javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
  jPanel3.setLayout(jPanel3Layout);
  jPanel3Layout.setHorizontalGroup(
    jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel3Layout.createSequentialGroup()
      .addContainerGap()
      .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
        .addComponent(jTextField1)
        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
  );
  jPanel3Layout.setVerticalGroup(
    jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel3Layout.createSequentialGroup()
      .addContainerGap()
      .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(jLabel2)
        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(jLabel1)
        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
  );

  jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Runways"));

  jLabel21.setText("Runway");

  jLabel22.setText("Takeoff Weight");

  jLabel23.setText("Landing Weight");

  jTextField5.setText("text");

  jTextField6.setText("text");

  jTextField7.setText("text");

  jTextArea1.setColumns(20);
  jTextArea1.setRows(5);
  jScrollPane1.setViewportView(jTextArea1);

  jTextArea2.setColumns(20);
  jTextArea2.setRows(5);
  jScrollPane3.setViewportView(jTextArea2);

  jTextArea3.setColumns(20);
  jTextArea3.setRows(5);
  jScrollPane4.setViewportView(jTextArea3);

  jTextField8.setText("text");

  jTextField9.setText("text");

  jTextField10.setText("text");

  jTextArea4.setColumns(20);
  jTextArea4.setRows(5);
  jScrollPane5.setViewportView(jTextArea4);

  jLabel24.setText("Highlight");

  jCheckBox8.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox8ActionPerformed(evt);
    }
  });

  jCheckBox10.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox10ActionPerformed(evt);
    }
  });

  jCheckBox9.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox9ActionPerformed(evt);
    }
  });

  jCheckBox11.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox11ActionPerformed(evt);
    }
  });

  jLabel25.setText("Highlight");

  jCheckBox12.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox12ActionPerformed(evt);
    }
  });

  jCheckBox13.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox13ActionPerformed(evt);
    }
  });

  jCheckBox14.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox14ActionPerformed(evt);
    }
  });

  jCheckBox15.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jCheckBox15ActionPerformed(evt);
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

  javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
  jPanel10.setLayout(jPanel10Layout);
  jPanel10Layout.setHorizontalGroup(
    jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel10Layout.createSequentialGroup()
      .addContainerGap()
      .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(jCheckBox9)
        .addComponent(jCheckBox8)
        .addComponent(jLabel24)
        .addComponent(jCheckBox10)
        .addComponent(jCheckBox11))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(jLabel21)
        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addGap(18, 18, 18)
      .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(jLabel25)
        .addComponent(jCheckBox12)
        .addComponent(jCheckBox13)
        .addComponent(jCheckBox14)
        .addComponent(jCheckBox15))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
          .addComponent(jLabel22)
          .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(jLabel26)
        .addComponent(jCheckBox16)
        .addComponent(jCheckBox17)
        .addComponent(jCheckBox18)
        .addComponent(jCheckBox19))
      .addGap(18, 18, 18)
      .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
          .addComponent(jLabel23)
          .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addContainerGap(28, Short.MAX_VALUE))
  );
  jPanel10Layout.setVerticalGroup(
    jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel10Layout.createSequentialGroup()
      .addContainerGap()
      .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel10Layout.createSequentialGroup()
          .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(jLabel22)
            .addComponent(jLabel25)
            .addComponent(jLabel21)
            .addComponent(jLabel24))
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(jCheckBox8)
            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jCheckBox12)
            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addGroup(jPanel10Layout.createSequentialGroup()
          .addComponent(jLabel26)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(jCheckBox16))
        .addGroup(jPanel10Layout.createSequentialGroup()
          .addComponent(jLabel23)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
      .addGap(18, 18, 18)
      .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addComponent(jCheckBox11)
        .addComponent(jCheckBox15)
        .addComponent(jCheckBox19)
        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addGap(14, 14, 14)
      .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(jCheckBox9)
        .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addComponent(jCheckBox13)
        .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addComponent(jCheckBox17)
        .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addGap(18, 18, 18)
      .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(jCheckBox10)
        .addComponent(jCheckBox14)
        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addComponent(jCheckBox18)
        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addContainerGap(29, Short.MAX_VALUE))
  );

  javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
  jPanel14.setLayout(jPanel14Layout);
  jPanel14Layout.setHorizontalGroup(
    jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel14Layout.createSequentialGroup()
      .addContainerGap()
      .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jLabel18)
        .addGroup(jPanel14Layout.createSequentialGroup()
          .addComponent(jLabel6)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
          .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGroup(jPanel14Layout.createSequentialGroup()
          .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 515, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGroup(jPanel14Layout.createSequentialGroup()
          .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
          .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
      .addContainerGap(41, Short.MAX_VALUE))
  );
  jPanel14Layout.setVerticalGroup(
    jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel14Layout.createSequentialGroup()
      .addContainerGap()
      .addComponent(jLabel18)
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(jLabel6)
        .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel14Layout.createSequentialGroup()
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
          .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
              .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))
          .addContainerGap(32, Short.MAX_VALUE))
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING))
          .addContainerGap())))
  );

  jScrollPane23.setViewportView(jPanel14);

  jButton1.setText("Preview / View");
  jButton1.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      jButton1ActionPerformed(evt);
    }
  });

  javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
  jPanel1.setLayout(jPanel1Layout);
  jPanel1Layout.setHorizontalGroup(
    jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel1Layout.createSequentialGroup()
      .addContainerGap()
      .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
          .addComponent(jScrollPane23, javax.swing.GroupLayout.DEFAULT_SIZE, 969, Short.MAX_VALUE)
          .addGap(18, 18, 18)
          .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
          .addComponent(jButton2)
          .addGap(18, 18, 18)
          .addComponent(jButton1)))
      .addContainerGap())
  );
  jPanel1Layout.setVerticalGroup(
    jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel1Layout.createSequentialGroup()
      .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
          .addGap(513, 513, 513)
          .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 98, Short.MAX_VALUE))
        .addGroup(jPanel1Layout.createSequentialGroup()
          .addContainerGap()
          .addComponent(jScrollPane23, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
          .addGap(18, 18, 18)))
      .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(jButton1)
        .addComponent(jButton2))
      .addContainerGap())
  );

  jTabbedPane1.addTab(getRunwayId() + " DATA", jPanel1);

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

  javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
  jPanel2.setLayout(jPanel2Layout);
  jPanel2Layout.setHorizontalGroup(
    jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel2Layout.createSequentialGroup()
      .addContainerGap()
      .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(jLabel28)
        .addComponent(jLabel29)
        .addComponent(jLabel46)
        .addComponent(jLabel47)
        .addComponent(jLabel48)
        .addComponent(jLabel49)
        .addComponent(jLabel50)
        .addComponent(jLabel51))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel2Layout.createSequentialGroup()
          .addGap(88, 88, 88)
          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel33)
            .addComponent(jLabel38)
            .addComponent(jLabel37)
            .addComponent(jLabel36)
            .addComponent(jLabel34)
            .addComponent(jLabel35)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(jLabel30)
              .addComponent(jLabel32))))
        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGroup(jPanel2Layout.createSequentialGroup()
          .addGap(2, 2, 2)
          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
            .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
            .addComponent(jScrollPane12, javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane11, javax.swing.GroupLayout.Alignment.LEADING))))
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jScrollPane13)
        .addComponent(jScrollPane14)
        .addComponent(jScrollPane15)
        .addComponent(jScrollPane16)
        .addComponent(jScrollPane17)
        .addComponent(jScrollPane18)
        .addComponent(jScrollPane19)
        .addComponent(jLabel31, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(jPanel2Layout.createSequentialGroup()
          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel39)
            .addComponent(jLabel40)
            .addComponent(jLabel41)
            .addComponent(jLabel42)
            .addComponent(jLabel43)
            .addComponent(jLabel44)
            .addComponent(jLabel45))
          .addGap(0, 57, Short.MAX_VALUE)))
      .addContainerGap(363, Short.MAX_VALUE))
    .addGroup(jPanel2Layout.createSequentialGroup()
      .addGap(10, 10, 10)
      .addComponent(jLabel27)
      .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
  );
  jPanel2Layout.setVerticalGroup(
    jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel2Layout.createSequentialGroup()
      .addContainerGap()
      .addComponent(jLabel27)
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
      .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel2Layout.createSequentialGroup()
          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(jLabel28)
            .addComponent(jLabel30))
          .addGap(8, 8, 8)
          .addComponent(jLabel32)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(jLabel29)
            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(jLabel33)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(jLabel46)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel34)
          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
              .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel2Layout.createSequentialGroup()
              .addGap(26, 26, 26)
              .addComponent(jLabel47)))
          .addGap(1, 1, 1)
          .addComponent(jLabel35)
          .addGap(18, 18, 18)
          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel48)
            .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(jLabel36)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jLabel49))
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(jLabel37)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jLabel50))
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(jLabel38)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jLabel51))
          .addGap(0, 0, Short.MAX_VALUE))
        .addGroup(jPanel2Layout.createSequentialGroup()
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

  jTabbedPane1.addTab(getRunwayId() + " TEXT", jPanel2);

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
      .addComponent(jScrollPane20, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
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

  javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
  jPanel7.setLayout(jPanel7Layout);
  jPanel7Layout.setHorizontalGroup(
    jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel7Layout.createSequentialGroup()
      .addGap(45, 45, 45)
      .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addGap(18, 18, 18)
      .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addGap(18, 18, 18)
      .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
  );
  jPanel7Layout.setVerticalGroup(
    jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(jPanel7Layout.createSequentialGroup()
      .addGap(83, 83, 83)
      .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
        .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(jPanel13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      .addContainerGap(268, Short.MAX_VALUE))
  );

  jTabbedPane1.addTab("Documents", jPanel7);

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
  new OnChange(longLatFormat){     public void onChange(String value){         settings.setValue(Settings.LONG_LAT_FORMAT, value);     } };

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

  secondaryRunway.setModel(runwayComboBox());
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

  homeRunway.setModel(runwayComboBox());
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

  javax.swing.GroupLayout settingsTabViewLayout = new javax.swing.GroupLayout(settingsTabView);
  settingsTabView.setLayout(settingsTabViewLayout);
  settingsTabViewLayout.setHorizontalGroup(
    settingsTabViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(settingsTabViewLayout.createSequentialGroup()
      .addContainerGap()
      .addGroup(settingsTabViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(settingsTabViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 946, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(javax.swing.GroupLayout.Alignment.LEADING, settingsTabViewLayout.createSequentialGroup()
            .addGroup(settingsTabViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel101)
              .addGroup(settingsTabViewLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(settingsLabelsScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(jLabel100, javax.swing.GroupLayout.Alignment.LEADING))
        .addComponent(settingsMainScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 977, javax.swing.GroupLayout.PREFERRED_SIZE)))
  );
  settingsTabViewLayout.setVerticalGroup(
    settingsTabViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(settingsTabViewLayout.createSequentialGroup()
      .addContainerGap()
      .addComponent(jLabel100)
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addComponent(settingsMainScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addGap(5, 5, 5)
      .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addComponent(jLabel101)
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
      .addGroup(settingsTabViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(settingsLabelsScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addContainerGap(62, Short.MAX_VALUE))
  );

  jTabbedPane1.addTab("Settings", settingsTabView);

  javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
  getContentPane().setLayout(layout);
  layout.setHorizontalGroup(
    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(layout.createSequentialGroup()
      .addContainerGap()
      .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
      .addContainerGap(793, Short.MAX_VALUE)
      .addComponent(jLabel16)
      .addGap(18, 18, 18)
      .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addContainerGap())
  );
  layout.setVerticalGroup(
    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
    .addGroup(layout.createSequentialGroup()
      .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 673, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addComponent(jLabel16))
      .addContainerGap())
  );

  pack();
  }// </editor-fold>//GEN-END:initComponents

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jButton1ActionPerformed

  private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jTextField2ActionPerformed

  private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jTextField1ActionPerformed

  private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jTextField4ActionPerformed

  private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jTextField3ActionPerformed

  private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox2ActionPerformed

  private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox3ActionPerformed

  private void jCheckBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox4ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox4ActionPerformed

  private void jCheckBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox5ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox5ActionPerformed

  private void jCheckBox6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox6ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox6ActionPerformed

  private void jCheckBox7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox7ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox7ActionPerformed

  private void jCheckBox8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox8ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox8ActionPerformed

  private void jCheckBox9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox9ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox9ActionPerformed

  private void jCheckBox10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox10ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox10ActionPerformed

  private void jCheckBox11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox11ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox11ActionPerformed

  private void jTextField11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField11ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jTextField11ActionPerformed

  private void jTextField12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField12ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jTextField12ActionPerformed

  private void jTextField13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField13ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jTextField13ActionPerformed

  private void jTextField14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField14ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jTextField14ActionPerformed

  private void jTextField15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField15ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jTextField15ActionPerformed

  private void jCheckBox12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox12ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox12ActionPerformed

  private void jCheckBox13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox13ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox13ActionPerformed

  private void jCheckBox14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox14ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox14ActionPerformed

  private void jCheckBox15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox15ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jCheckBox15ActionPerformed

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
    db = DatabaseManager.getDatabase();
   Runway r = null;
    
   try {
      r = db.getRunway((String) jComboBox4.getSelectedItem(), (String) jComboBox1.getSelectedItem());
      if(r == null){
        System.out.println("null");
      }
    } catch (DatabaseException ex) {
      ex.printStackTrace();
    }
    if(image != null){
      try {
        image = Repository.copyImageFile(r, image);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      
      r.put(Field.IMAGE_PATH, image.getAbsolutePath());
      if(r.isModified()){
        r.save();
      }
      jLabel17.setIcon(new ImageIcon(image.getAbsolutePath()));
    }
  }//GEN-LAST:event_jButton3ActionPerformed

  private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
    
    JCalendar cal = new JCalendar(new Date());
    
    cal.addPropertyChangeListener(new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if("calendar".equals(evt.getPropertyName())) {
          jTextField4.setText(((GregorianCalendar) evt.getNewValue()).getTime().toString());

        }
      }
    });
    
    JDialog d = new JDialog(this);
    d.getContentPane().add(cal);
    d.pack();
    d.setLocationRelativeTo(this);
    d.setVisible(true);
  }//GEN-LAST:event_jButton6ActionPerformed

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
  private javax.swing.JTextField adminEmail;
  private javax.swing.JTextField adminPass;
  private javax.swing.JComboBox altitude;
  private javax.swing.JTextField defExpiration;
  private javax.swing.JComboBox dimension;
  private javax.swing.JComboBox distance;
  private javax.swing.JTextField distanceConvert;
  private javax.swing.Box.Filler filler1;
  private javax.swing.JComboBox homeRunway;
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton2;
  private javax.swing.JButton jButton3;
  private javax.swing.JButton jButton4;
  private javax.swing.JButton jButton5;
  private javax.swing.JButton jButton6;
  private javax.swing.JCheckBox jCheckBox1;
  private javax.swing.JCheckBox jCheckBox10;
  private javax.swing.JCheckBox jCheckBox11;
  private javax.swing.JCheckBox jCheckBox12;
  private javax.swing.JCheckBox jCheckBox13;
  private javax.swing.JCheckBox jCheckBox14;
  private javax.swing.JCheckBox jCheckBox15;
  private javax.swing.JCheckBox jCheckBox16;
  private javax.swing.JCheckBox jCheckBox17;
  private javax.swing.JCheckBox jCheckBox18;
  private javax.swing.JCheckBox jCheckBox19;
  private javax.swing.JCheckBox jCheckBox2;
  private javax.swing.JCheckBox jCheckBox20;
  private javax.swing.JCheckBox jCheckBox21;
  private javax.swing.JCheckBox jCheckBox22;
  private javax.swing.JCheckBox jCheckBox23;
  private javax.swing.JCheckBox jCheckBox3;
  private javax.swing.JCheckBox jCheckBox4;
  private javax.swing.JCheckBox jCheckBox5;
  private javax.swing.JCheckBox jCheckBox6;
  private javax.swing.JCheckBox jCheckBox7;
  private javax.swing.JCheckBox jCheckBox8;
  private javax.swing.JCheckBox jCheckBox9;
  private javax.swing.JComboBox jComboBox1;
  private javax.swing.JComboBox jComboBox2;
  private javax.swing.JComboBox jComboBox3;
  private javax.swing.JComboBox jComboBox4;
  private javax.swing.JComboBox jComboBox5;
  private javax.swing.JComboBox jComboBox6;
  private javax.swing.JComboBox jComboBox7;
  private javax.swing.JComboBox jComboBox8;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel10;
  private javax.swing.JLabel jLabel100;
  private javax.swing.JLabel jLabel101;
  private javax.swing.JLabel jLabel11;
  private javax.swing.JLabel jLabel12;
  private javax.swing.JLabel jLabel13;
  private javax.swing.JLabel jLabel14;
  private javax.swing.JLabel jLabel15;
  private javax.swing.JLabel jLabel16;
  private javax.swing.JLabel jLabel17;
  private javax.swing.JLabel jLabel18;
  private javax.swing.JLabel jLabel19;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel20;
  private javax.swing.JLabel jLabel21;
  private javax.swing.JLabel jLabel22;
  private javax.swing.JLabel jLabel23;
  private javax.swing.JLabel jLabel24;
  private javax.swing.JLabel jLabel25;
  private javax.swing.JLabel jLabel26;
  private javax.swing.JLabel jLabel27;
  private javax.swing.JLabel jLabel28;
  private javax.swing.JLabel jLabel29;
  private javax.swing.JLabel jLabel3;
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
  private javax.swing.JLabel jLabel4;
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
  private javax.swing.JLabel jLabel5;
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
  private javax.swing.JLabel jLabel6;
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
  private javax.swing.JLabel jLabel7;
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
  private javax.swing.JLabel jLabel8;
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
  private javax.swing.JLabel jLabel9;
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
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel10;
  private javax.swing.JPanel jPanel11;
  private javax.swing.JPanel jPanel12;
  private javax.swing.JPanel jPanel13;
  private javax.swing.JPanel jPanel14;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JPanel jPanel6;
  private javax.swing.JPanel jPanel7;
  private javax.swing.JPanel jPanel8;
  private javax.swing.JPanel jPanel9;
  private javax.swing.JScrollPane jScrollPane1;
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
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane20;
  private javax.swing.JScrollPane jScrollPane21;
  private javax.swing.JScrollPane jScrollPane22;
  private javax.swing.JScrollPane jScrollPane23;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JScrollPane jScrollPane4;
  private javax.swing.JScrollPane jScrollPane5;
  private javax.swing.JScrollPane jScrollPane6;
  private javax.swing.JScrollPane jScrollPane7;
  private javax.swing.JScrollPane jScrollPane8;
  private javax.swing.JScrollPane jScrollPane9;
  private javax.swing.JSeparator jSeparator1;
  private javax.swing.JTabbedPane jTabbedPane1;
  private javax.swing.JTable jTable2;
  private javax.swing.JTextArea jTextArea1;
  private javax.swing.JTextArea jTextArea10;
  private javax.swing.JTextArea jTextArea11;
  private javax.swing.JTextArea jTextArea12;
  private javax.swing.JTextArea jTextArea13;
  private javax.swing.JTextArea jTextArea14;
  private javax.swing.JTextArea jTextArea15;
  private javax.swing.JTextArea jTextArea16;
  private javax.swing.JTextArea jTextArea17;
  private javax.swing.JTextArea jTextArea18;
  private javax.swing.JTextArea jTextArea2;
  private javax.swing.JTextArea jTextArea3;
  private javax.swing.JTextArea jTextArea4;
  private javax.swing.JTextArea jTextArea5;
  private javax.swing.JTextArea jTextArea6;
  private javax.swing.JTextArea jTextArea7;
  private javax.swing.JTextArea jTextArea8;
  private javax.swing.JTextArea jTextArea9;
  private javax.swing.JTextField jTextField1;
  private javax.swing.JTextField jTextField10;
  private javax.swing.JTextField jTextField11;
  private javax.swing.JTextField jTextField12;
  private javax.swing.JTextField jTextField13;
  private javax.swing.JTextField jTextField14;
  private javax.swing.JTextField jTextField15;
  private javax.swing.JTextField jTextField16;
  private javax.swing.JTextField jTextField17;
  private javax.swing.JTextField jTextField18;
  private javax.swing.JTextField jTextField19;
  private javax.swing.JTextField jTextField2;
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
  private javax.swing.JTextField jTextField3;
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
  private javax.swing.JTextField jTextField4;
  private javax.swing.JTextField jTextField40;
  private javax.swing.JTextField jTextField41;
  private javax.swing.JTextField jTextField42;
  private javax.swing.JTextField jTextField43;
  private javax.swing.JTextField jTextField5;
  private javax.swing.JTextField jTextField6;
  private javax.swing.JTextField jTextField7;
  private javax.swing.JTextField jTextField8;
  private javax.swing.JTextField jTextField9;
  private javax.swing.JComboBox longLatFormat;
  private javax.swing.JTextField magVariation;
  private javax.swing.JTextArea page1Disclaimer;
  private javax.swing.JScrollPane page1DisclaimerScroll;
  private javax.swing.JTextArea page2Disclaimer;
  private javax.swing.JScrollPane page2DisclaimerScroll;
  private javax.swing.JTextField primaryDbClassPath;
  private javax.swing.JTextField primaryDbUri;
  private javax.swing.JTextField repoPath;
  private javax.swing.JButton repoPathBrowse;
  private javax.swing.JComboBox secondaryRunway;
  private javax.swing.JPanel settingsLabels;
  private javax.swing.JScrollPane settingsLabelsScroll;
  private javax.swing.JPanel settingsMain;
  private javax.swing.JScrollPane settingsMainScroll;
  private javax.swing.JPanel settingsTabView;
  private javax.swing.JCheckBox trueCourse;
  private javax.swing.JComboBox weight;
  // End of variables declaration//GEN-END:variables
}

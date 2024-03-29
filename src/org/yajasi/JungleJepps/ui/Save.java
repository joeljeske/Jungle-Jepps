package org.yajasi.JungleJepps.ui;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

import org.yajasi.JungleJepps.Field;
import org.yajasi.JungleJepps.Runway;

/**
 * This class is used to set a field element to save when changed. 
 * Ther user interface makes frequent calls to initiate the fields
 * to listen for changes and then save. 
 * 
 * @author Joel Jeske
 */
public class Save {
    
    private Save(){}
    
    //Holds the current/open runway for easy saving
    protected static Runway runway;
    
    //Flag for persistant change on field change during field blur
    protected static boolean isSaveImmediate = false;
    
    //Dependency injector for runtime saving
    public static void setRunway(Runway runway){
        Save.runway = runway;
    }
    
    public static void setSaveOnChange(boolean isSaveImmediate){
        Save.isSaveImmediate = isSaveImmediate;
    }    
    
    public static void onChange(JComboBox element, Field Field){
        //Create new listener and inject its field to save immediately
        new Save.SaveOnChange(element).fieldToSave = Field;
    }
    
    public static void onChange(JTextComponent element, Field Field){
        //Create new listener and inject its field to save immediately
        new Save.SaveOnChange(element).fieldToSave = Field;
    }
    
    public static void onChange(JCheckBox element, Field Field){
        //Create new listener and inject its field to save immediately
        new Save.SaveOnChange(element).fieldToSave = Field;
    }
    
    
    
    
    protected static class SaveOnChange extends OnChange {
        protected Field fieldToSave;
                
        public SaveOnChange(JComboBox field) {
            super(field);
        }
        
        public SaveOnChange(JTextComponent field){
            super(field);
        }
        
        public SaveOnChange(JCheckBox field){
            super(field);
        }
        
        @Override
        public void onChange(String value) {
            //Save to runtime object always
            Save.runway.put(fieldToSave, value);
            
            //If set to save immediately
            if(Save.isSaveImmediate)
                Save.runway.save(); //Save persistantly to DB
        }
        
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.yajasi.JungleJepps.ui;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

import org.yajasi.JungleJepps.Field;
import org.yajasi.JungleJepps.Runway;

/**
 *
 * @author joeljeske14
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
    
    public static void onChange(JComboBox element, Field field){
        //Create new listener and inject its field to save immediately
        new Save.SaveOnChange(element).fieldToSave = field;
    }
    
    public static void onChange(JTextComponent element, Field field){
        //Create new listener and inject its field to save immediately
        new Save.SaveOnChange(element).fieldToSave = field;
    }
    
    public static void onChange(JCheckBox element, Field field){
        //Create new listener and inject its field to save immediately
        new Save.SaveOnChange(element).fieldToSave = field;
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

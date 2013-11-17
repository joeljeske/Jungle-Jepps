/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.yajasi.JungleJepps.ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

/**
 *
 * @author joeljeske14
 */
public abstract class OnChange  {
    
    private String old;
    private int index;
    
    public OnChange(final JComboBox menu){
        // Listen for focus events to see if the value has changed
        menu.addFocusListener(new FocusListener(){
            @Override
            public void focusGained(FocusEvent e) {
            	//Save the original value of the field
                index = menu.getSelectedIndex();
            }

            @Override
            public void focusLost(FocusEvent e) {
            	int newIndex = menu.getSelectedIndex();
            	
                if( index != newIndex ) //If the value has changed
                {
                    // Set value for future changes
                    index = newIndex;
                    
                    // Call the change listener
                    String text = menu.getItemAt(index).toString();
                    onChange( text );
                }
            } 
        });
    }
    
    public OnChange(final JTextComponent field){
        // Listen for focus events to see if the value has changed
        field.addFocusListener(new FocusListener(){
            @Override
            public void focusGained(FocusEvent e) {
            	//Save the original value of the field
                old = field.getText();
            }

            @Override
            public void focusLost(FocusEvent e) {
            	String newVal = field.getText();
            	
                if( !old.equals(newVal) ) //If the value has changed
                {
                    // Clear the value in memory
                    old = null; 
                    
                    // Call the change listener
                    onChange( newVal );
                }
            } 
        });
    }
    
     public OnChange(final JCheckBox field){
        // Listen for focus events to see if the value has changed
        field.addFocusListener(new FocusListener(){
            @Override
            public void focusGained(FocusEvent e) {
            	//Save the original value of the field
                index = field.isSelected() ? 1 : 0;
            }

            @Override
            public void focusLost(FocusEvent e) {
                int newIndex = field.isSelected() ? 1 : 0;
            	
                if( index != newIndex ) //If the value has changed
                {
                    // Set value for future changes
                    index = newIndex; 
                    
                    // Call the change listener
                    String value = (index == 1 ? "true" : "false");
                    onChange( value );
                }
            } 
        });
    }
    
    
    
    public abstract void onChange(String value);
}

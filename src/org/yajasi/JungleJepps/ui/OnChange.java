/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.yajasi.JungleJepps.ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

/**
 *
 * @author joeljeske14
 */
public abstract class OnChange  {
    
    private String old; 
    
    public OnChange(final JTextField field){
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
    
    public abstract void onChange(String value);
}

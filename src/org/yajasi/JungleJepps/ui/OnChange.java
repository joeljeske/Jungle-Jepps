/////////////////////////////////////////////////////////////////////////
// Author: Joel Jeske
// File: OnChange.java
// Class: org.yajasi.JungleJepps.ui.OnChange
//
// Target Platform: Java Virtual Machine 
// Development Platform: Apple OS X 10.9
// Development Environment: NetBeans 7.4 OS X
// 
// Project: Jungle Jepps - Desktop
// Copyright 2013 YAJASI. All rights reserved. 
// 
// Objective: This class is used to simplify an OnChange listener for GUI
// objects. Various constructor types accepts different GUI elements and 
// listens for change in focus. If the element has a new state or value 
// when the focus is lost, the listener is notified with the new String 
// representation of this value. 
//
/////////////////////////////////////////////////////////////////////////


package org.yajasi.JungleJepps.ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

/**
 * This class can listen on various Java Swing elements 
 * for changes in value when the focus is lost.  
 * The listener must implement the onChange(String) handler
 * which will be called when the GUI element changes. 
 * 
 * @author Joel Jeske
 */
public abstract class OnChange  {
    
	//Holds the String for various text elements 
    private String old;
    
    //Holds the index into the combo box element 
    private int index;
    
    /**
     * Use this constructor to listen for changes on a combo box
     * @param menu
     */
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
    
    /**
     * Use this constructor to listen for changes on a text-based element
     * @param field
     */
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
    
    /**
     * Use this constructor to listen for change on a checkbox element 
     * @param field
     */
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
    
    
    /**
     * This method will be called when the selected GUI element was changed
     * and the focus is then lost.
     * @param value
     */
    public abstract void onChange(String value);
}

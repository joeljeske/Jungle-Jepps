#Jungle Jepps - Desktop Application

##Abstract
This repository contains the work completed by the below team for YAJASI on Jungle Jepps software suite. Jungle Jepps was created and designed to manage proprietary runway information, primarily for remote runways in various areas of the world where larger charting companies do not find a financial draw to research these hidden air strips.  
Jungle Jepps Desktop will be managed by a pilot who will hold primary responsibility for the correctness of the information. The application will take the data in a form-like manner and store it in a previously configured database on the local machine or on a network attached machine. The application will be able to later recall the information and compile the various fields into a PDF document acting as a the runway chart. This runway chart will be stored automatically in a previously configured repository on the local machine or on a network attached machine. The application will service requests from a iOS iPad application and serve the PDF documents to be stored on the iOS filesystem for later recall at the pilots choosing, primarily during flight.  


##Members
####LeTourneau Students  
* Joel Jeske
* Jesse Sjoblom
* Timothy Feucht
* Dillon Yadon

####YAJASI
* Zach Osterloo


##Supporting Documents
####SRS
* Author: Zach Osterloo
* Purpose: Specifies the software requirements for the Desktop and Mobile applications in the Jungle Jepps software suite
* Current Version: v1.0.1
* Path: [/docs/JungleJepps-SRS.pdf]()

####Design Document
* Author: LeTourneau Team
* Purpose: Clarifies the approach to the implementation decided by the developers. 
* Current Version: v0.1.0
* Path: [/docs/Preliminary-Design.pdf]()

##Repository Structure
This repository is primarily organized in 4 folders.  
####[/src/]()
Contains the Java source code for the application in addition to various CSS, and HTML documents acting as a template for the PDF document. The Java files are organized into standard class path and package structure with the primary package namely, "org.yajasi.JungleJepps", containing the majority of the application.  
####[/lib/]()
Contains the included libraries in use in the application. Should be packaged in a .jar.  
####[/doc/]()
Contains sparse documentation for both the included libraries and various documents for Jungle Jepps desktop application.  
####[/uml/]()
Contains various UML diagrams including class, data flow, and use case. 


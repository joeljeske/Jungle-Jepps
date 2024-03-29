package org.yajasi.JungleJepps.pdf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.*;
import org.xhtmlrenderer.simple.PDFRenderer;
import org.xml.sax.SAXException;
import org.yajasi.JungleJepps.Field;
import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.ValueByEnum;
import org.yajasi.JungleJepps.ValueByEnum.JungleJeppsEnum;
import org.yajasi.JungleJepps.db.DatabaseManager;
import org.yajasi.JungleJepps.db.Settings;
import org.yajasi.JungleJepps.db.SettingsManager;
import org.yajasi.JungleJepps.pdf.Repository;

import com.lowagie.text.DocumentException;

public class HtmlPreparer {
	
	private static final String HOOK_TAG = "data";
	private static final String LABEL_TAG = "label";
	private static final String UNIT_TAG = "unit";
	
	private static final String FIELD_ATTR = "field";
	private static final String UNIT_ATTR = "type";
	private static final String REMOVED_ATTR = "removable='true'";
	private static final String HIGHLIGHT_ATTR = "highlight";
	
	private static final String TOPO_DOM_ID = "topo_map";
	private static final String TEMPLATE_URI = String.format("runtime%cxhtml%cnew-template.html", File.separatorChar, File.separatorChar);
	
	private Document dom;
	private String parentUrl;
	
	// Does simple test to demo functionality
	public static void main(String[] args) throws IOException {
		Runway runway = new Runway();
		for(Field f : Field.values())
			runway.put(f, f.toString());
		
		runway.put(Field.IMAGE_PATH, "kiwi_image.png");

		publish(runway);
	}
	
	
	public static File publish(Runway runway) throws IOException{
		File output = Repository.getPublishLocation(runway);
		return publish(runway, output);
	}
	
	public static File publish(Runway runway, File output){
		File out = new HtmlPreparer().prepareAndPublish(runway, output);
		runway.put(Field.PDF_PATH, out.getAbsolutePath());
		return out;
	}
	
	/**
	 * Private constructor to simulate static use
	 */
	private HtmlPreparer(){}

	/**
	 * Single front end to PDF preparer. Can overload method with other
	 * options. To change parameters, the constants must be modified.
	 * @param runway
	 * @param image
	 * @param output 
	 * @param outputUrl
	 */
	public File prepareAndPublish(Runway runway, File output){
		dom = null;
		parentUrl = null;
		
		loadTemplate();
		injectData(runway);
		
		try {			
			publishPDF(output);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	/**
	 * Saves the DOM as a PDF using PDF Generator class
	 * @param output
	 * @throws IOException
	 * @throws DocumentException
	 */
	private void publishPDF(File output) throws IOException, DocumentException{
		if(dom == null) return;
		PDFRenderer.renderToPDF(dom, parentUrl, output.getAbsolutePath());
	}
	
	/**
	 * Called to load the default template using the TEMPLATE_URI 
	 * Constant. Initializes the DOM document.
	 */
	private void loadTemplate(){
        loadTemplate( new File(Repository.JAR_FOLDER, TEMPLATE_URI) );
	}

	/**
	 * Initializes the DOM document with the passed File.
	 * @param template
	 */
	private void loadTemplate(File template){
		// Factory for building DOMs
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
		
        try {
			// Create DOM builder
			docBuilder = docBuilderFactory.newDocumentBuilder();
			// Parse in file
	        this.dom = (Document) docBuilder.parse ( template );
	        
	        // Save Parents path for later use by HTML parser (e.g. relative paths...)
	        File parent = template.getParentFile();
			parentUrl = (parent == null ? "" : parent.toURI().toURL().toExternalForm());
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Injects data into text nodes in the DOM. This method traverses every 
	 * tag that's name matches HOOK_TAG and looks for a value for the key of the 
	 * attribute value for the attribute with the name that matches HOOK_NAME_ATTR.
	 * The value found in dataMap replaces the textNode in the DOM.
	 * @param runway
	 */
	private void injectData(Runway runway){
		SettingsManager settings = DatabaseManager.getSettings();
		
		inject( runway, HOOK_TAG, FIELD_ATTR, Field.class, true);
		inject( settings, LABEL_TAG, FIELD_ATTR, Field.class, false);
		inject( settings, UNIT_TAG, UNIT_ATTR, Settings.class, false);
		highlight(runway, HIGHLIGHT_ATTR, FIELD_ATTR);
		
		String path = new File( runway.get(Field.IMAGE_PATH) ).getAbsolutePath();
		injectImage( path );
	}
	
	private void highlight(Runway runway, final String HL_ATTR, final String ID_ATTR){
		// Setup a XPath evaluator to query through the DOM
		XPath xPath = XPathFactory.newInstance().newXPath();
		
		//Find all nodes with a field and highlight attribute
		String query = "//*[@field][@highlight]";

		// Execute search
		NodeList highlightable = null;
		try {
			highlightable = (NodeList) xPath.evaluate(query, dom.getDocumentElement(), XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		//Loop through all the nodes found that could be highlighted
		for(int i = 0; i < highlightable.getLength(); i++)
		{
			Node node = highlightable.item(i);
			
			if(node == null)
				continue;
			
        	if(node.getNodeType() == Node.ELEMENT_NODE)
        	{
            	//The Field enum value
        		String id = node.getAttributes().getNamedItem( ID_ATTR ).getNodeValue();
            	
        		//Add the "_HL" to the node's field attribute value
        		id += "_HL";
        		
        		//Get the actual ENUM
            	Field field = Field.valueOf( id.toUpperCase() );
            	
            	// The string value of "true" or "false" if the field should be highlighted
            	String isHighlighted = runway.get(field);
            	
            	//Set the attribute node to be "true" or "false". CSS takes care the of the highlighting
            	node.getAttributes().getNamedItem( HL_ATTR ).setNodeValue( isHighlighted );
        	}

		}	
	}
	
	/**
	 * Injects data into text nodes in the DOM. This method traverses every 
	 * tag that's name matches tagName and looks for a value for the key of the 
	 * attribute value for the attribute with the name that matches idAttribute.
	 * The value found in dataMap replaces the textNode in the DOM.
	 * @param source
	 * @param TAG
	 * @param ATTR
	 * @param type
	 */
	private void inject(ValueByEnum source, final String TAG, final String ATTR, Class type, Boolean removeNulls){
		// Setup a XPath evaluator to query through the DOM
		XPath xPath = XPathFactory.newInstance().newXPath();
		
		// Get all the tags to loop through to inject data
		NodeList tags = dom.getElementsByTagName( TAG );	
		
		// Go through every node
        for(int i = 0; i < tags.getLength(); i++)
        {
        	Node tag = tags.item(i);
        	
        	if(tag == null)
        		continue;
        	
        	if(tag.getNodeType() == Node.ELEMENT_NODE)
        	{
            	String id = tag.getAttributes().getNamedItem( ATTR ).getNodeValue();
            	
            	ValueByEnum.JungleJeppsEnum val;
            	try {
            		val = (JungleJeppsEnum) Enum.valueOf(type, id.toUpperCase());
            		
            		//The value is not the ENUM
            	} catch(IllegalArgumentException e){ 
            		e.printStackTrace();
            		continue;
            	}
            	String text = source.get( val ); 
            	
            	
            	if( text == null || text.trim().isEmpty() ) // If there is no data to inject
            	{
            		if( removeNulls )
                	{
	            		// Example:  //*[@field='<field_name>'][@removable='true']
	            		String xPathString = "//*[@" + ATTR + "='" + id + "'][@" + REMOVED_ATTR + "]";
	            		
	            		try {
	            			NodeList removables = (NodeList) xPath.evaluate(xPathString, dom.getDocumentElement(), XPathConstants.NODESET);
							hideElements(removables);
						} catch (XPathExpressionException e) {
							e.printStackTrace();
						}
	            		
                	}// If remove nulls
            	}
            	else // If there is data to inject
            	{
            		text = ( text == null ? "" : text );
            		tag.setTextContent( text );
            		
            		System.out.println("--Injected--    " + id + ": " + text);
            	}
        	} // if element node
        } // for all data tags
	}
	
	// Adds css to node list to display:none
	private void hideElements(NodeList nodes){
		String css = "display:none";
		addCss( nodes, css );
	}
	
	// Adds specified css to the style attribute on a nodelist
	private void addCss(NodeList nodes, String css){
		for(int i = 0; i < nodes.getLength(); i++)
		{
			Element node = (Element) nodes.item(i);
			Node styleAttr = node.getAttributes().getNamedItem("style");
			String style = styleAttr == null ? ""  : styleAttr.getTextContent();
		
			style = (style == null || style.isEmpty() ) ? "" : style + "; ";
			style += css;
			node.setAttribute("style", css);
		}
	}
	
	private void injectImage(String imageUrl){
		final String IMAGE_URL_ATTRIBUTE_NAME = "src";
		final String IMAGE_TAG_NAME = "img";
		final String ID_ATTR = "id";
		
		if(dom == null) return;
		
		// Pretty the parameter
		imageUrl = ( imageUrl == null ? "" : imageUrl );

		// Get the image node
		NodeList imgs = dom.getElementsByTagName(IMAGE_TAG_NAME);
		
		// Loop through all the images nodes
		for(int i = 0; i < imgs.getLength(); i++)
		{
			Node img = imgs.item(i);
			
			// If the current node is an element (IT SHOULD BE)
			if( img.getNodeType() == Node.ELEMENT_NODE )
			{
				// Get a reference to the Attribute map
				NamedNodeMap attrs = img.getAttributes();
				
				// Get the attribute with the name matching HOOK_NAME_ATTR (e.g. "id")
				Node id = attrs.getNamedItem( ID_ATTR );
				
				// If there is and ID attribute node and the text equals TOPO_DOM_ID
				if( id != null && TOPO_DOM_ID.equalsIgnoreCase( id.getTextContent() ) )
				{
					Node src;
					// Find the src attribute
					src = attrs.getNamedItem( IMAGE_URL_ATTRIBUTE_NAME );
					
					// If there was not a source attribute
					if( src == null)
					{
						//Make and add a source attribute
						src = dom.createAttribute( IMAGE_URL_ATTRIBUTE_NAME );
						attrs.setNamedItem( src );
					}
					
					// Set the text node to contain the imageUrl 
					// <img src="C:\Users\User\Public\JungleJepps\Repository\KIWI\C-5\topo.jpg";
					src.setTextContent( imageUrl );
					
				}
			}
		}
		
		
	}

	
}

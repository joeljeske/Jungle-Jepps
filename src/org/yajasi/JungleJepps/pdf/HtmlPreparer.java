package org.yajasi.JungleJepps.pdf;

import java.io.File;
import java.io.IOException;

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
import org.yajasi.JungleJepps.pdf.Repository;

import com.lowagie.text.DocumentException;

public class HtmlPreparer {
	
	private static final String HOOK_TAG = "data";
	private static final String LABEL_TAG = "label";
	private static final String FIELD_ATTR = "field";
	private static final String REMOVED_ATTR = "removable='true'";
	
	private static final String TOPO_DOM_ID = "topo_image";
	private static final String TEMPLATE_URI = "src/xhtml/new-template.html";
	
	private Document dom;
	private String parentUrl;
	
	// Does simple test to demo functionality
	public static void main(String[] args) {
		Runway runway = null;
		publish(runway);
	}
	
	
	public static File publish(Runway runway){
		File output = Repository.getPublishLocation(runway);
		return publish(runway, output);
	}
	
	public static File publish(Runway runway, File output){
		String imageUrl = runway.getField(Field.IMAGE_PATH);
		return new HtmlPreparer().prepareAndPublish(runway, imageUrl, output);
	}
	
	/**
	 * Private constructor to simulate static use
	 */
	private HtmlPreparer(){}

	/**
	 * Single front end to PDF preparer. Can overload method with other
	 * options. To change parameters, the constants must be modified.
	 * @param dataMap
	 * @param outputUrl
	 */
	public File prepareAndPublish(Runway runway, String imageUrl, File output){
		dom = null;
		parentUrl = null;
		
		loadTemplate();
		injectData(runway);

		injectImage(imageUrl);
		
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
	 * @param outputUrl
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
        File template = new File( TEMPLATE_URI );
        loadTemplate( template );
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
	        File parent = template.getAbsoluteFile().getParentFile();
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
	 * @param dataMap
	 */
	private void injectData(Runway runway){
		injectData(runway, HOOK_TAG, LABEL_TAG, FIELD_ATTR, REMOVED_ATTR);
	}
	
	/**
	 * Injects data into text nodes in the DOM. This method traverses every 
	 * tag that's name matches tagName and looks for a value for the key of the 
	 * attribute value for the attribute with the name that matches idAttribute.
	 * The value found in dataMap replaces the textNode in the DOM.
	 * @param dataMap
	 * @param tagName
	 * @param idAttribute
	 */
	private void injectData(Runway runway, final String TAG, final String LABEL_TAG, final String ID_ATTRIBUTE, final String REMOVED_ATTR){
		// Setup a XPath evaluator to query through the DOM
		XPath xPath = XPathFactory.newInstance().newXPath();
		
		// Get all the tags to loop through to inject data
		NodeList tags = dom.getElementsByTagName( TAG );	
		
		// Go through every node
        for(int i = 0; i < tags.getLength(); i++)
        {
        	Node tag = tags.item(i);

        	if(tag.getNodeType() == Node.ELEMENT_NODE)
        	{
            	String id = tag.getAttributes().getNamedItem( ID_ATTRIBUTE ).getNodeValue();
            	String text = runway.getField( Field.valueOf(id) );
            	
            	// FOR TESTING WITHOUT DATA ONLY
            	text = tag.getTextContent(); 
            	
            	if( text == null || text.trim().isEmpty() ) // If there is no data to inject
            	{
            		// Example:  //*[@field='<field_name>'][@removable='true']
            		String xPathString = "//*[@" + ID_ATTRIBUTE + "='" + id + "'][@" + REMOVED_ATTR + "]";
            		
            		try {
            			NodeList removables = (NodeList) xPath.evaluate(xPathString, dom.getDocumentElement(), XPathConstants.NODESET);
						hideElements(removables);
					} catch (XPathExpressionException e) {
						e.printStackTrace();
					}
            	}
            	else // If there is data to inject
            	{
            		text = ( text == null ? "" : text );
            		tag.setTextContent( text );
            	}
        	} // if element node
        } // for all data tags
        
        tags = dom.getElementsByTagName(LABEL_TAG);
        
        for(int i = 0; i < tags.getLength(); i++)
        {
        	Node tag = tags.item(i);
        	if(tag.getNodeType() == Node.ELEMENT_NODE)
        	{
        		
        		
        	} // tag is element node
        } //for all label tags
    	
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
				Node id = attrs.getNamedItem( FIELD_ATTR );
				
				// If there is and ID attribute node and the text equals TOPO_DOM_ID
				if( id != null && TOPO_DOM_ID.equalsIgnoreCase( id.getTextContent() ) )
				{
					// Find the src attribute
					Node src = attrs.getNamedItem( IMAGE_URL_ATTRIBUTE_NAME );
					
					// If there was not a source attribute
					if( src == null)
					{
						//Make and add a source attribute
						src = dom.createAttribute( IMAGE_URL_ATTRIBUTE_NAME );
						attrs.setNamedItem( src );
					}
					
					// Set the text node to contain the imageUrl 
					// <img src="C:\Users\User\Public\JungleJepps\Repository\KIWI\B747\topo.jpg";
					src.setTextContent( imageUrl );
				}
			}
		}
		
		
	}

	
}

package org.yajasi.JungleJepps.pdf;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xhtmlrenderer.simple.PDFRenderer;
import org.xml.sax.SAXException;

import com.lowagie.text.DocumentException;

public class HtmlPreparer {
	
	private static final String HOOK_TAG = "hook"; 
	private static final String HOOK_NAME_ATTR = "id";
	private static final String TOPO_DOM_ID = "topo_image";
	private static final String TEMPLATE_URI = "src/xhtml/template.html";
	
	private Document dom;
	private String parentUrl;
	
	// Does simple test to demo functionality
	public static void main(String[] args) {
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("strip_name", "PREPARED");
		String outputURL = "./pdf-repo/output.pdf"; 
		String imageURL = "./images/kiwi_image.png"; 
		
		HtmlPreparer prep = new HtmlPreparer();
		prep.prepareAndPublish(dataMap, imageURL, outputURL);
	}
	
	/**
	 * Single front end to PDF preparer. Can overload method with other
	 * options. To change parameters, the constants must be modified.
	 * @param dataMap
	 * @param outputUrl
	 */
	public void prepareAndPublish(Map<String,String> dataMap, String imageUrl, String outputUrl){
		dom = null;
		parentUrl = null;
		
		loadTemplate();
		injectData(dataMap);
		injectImage(imageUrl);

		try {
			publishPDF(outputUrl);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the DOM as a PDF using PDF Generator class
	 * @param outputUrl
	 * @throws IOException
	 * @throws DocumentException
	 */
	private void publishPDF(String outputUrl) throws IOException, DocumentException{
		if(dom == null) return;
		
		PDFRenderer.renderToPDF(dom, parentUrl, outputUrl);
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
	private void injectData(Map<String, String> dataMap){
		injectData(dataMap, HOOK_TAG, HOOK_NAME_ATTR);
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
	private void injectData(Map<String, String> dataMap, final String tagName, final String idAttribute){
		if(dom == null) return;
		NodeList hooks = dom.getElementsByTagName( tagName );
        int numHooks = hooks.getLength();

        for(int i = 0; i < numHooks; i++)
        {

        	Node hook = hooks.item(i);

        	if(hook.getNodeType() == Node.ELEMENT_NODE)
        	{
            	String id = hook.getAttributes().getNamedItem( idAttribute ).getNodeValue();
            	String textNode = dataMap.get( id );
            	
            	textNode = ( textNode == null ? "" : textNode );
            	hook.setTextContent( textNode );
        	}
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
				Node id = attrs.getNamedItem( HOOK_NAME_ATTR );
				
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

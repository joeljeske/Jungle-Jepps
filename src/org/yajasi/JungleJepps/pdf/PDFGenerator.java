package org.yajasi.JungleJepps.pdf;

import java.io.File;
import java.io.IOException;

import org.xhtmlrenderer.simple.PDFRenderer;

import com.lowagie.text.DocumentException;

public class PDFGenerator {
	
	public static void main(String[] args) throws IOException, DocumentException{
		PDFGenerator gen = new PDFGenerator();
		gen.publish();
	}
	
	public void publish() throws IOException, DocumentException{
		
		// XHTML File path assuming project directory is root
		File file = new File("./src/xhtml/test.html");
		
		/*
		 * The Renderer can accept a file or an InputStream.
		 * It seems like an realized InputStream might be best
		 * because it could take care of the data injection
		 * into the xhtml template  
		 */
		PDFRenderer.renderToPDF(file, "./pdf-repo/output.pdf");
		
	}
	
}

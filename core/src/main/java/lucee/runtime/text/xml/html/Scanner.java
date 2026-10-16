package lucee.runtime.text.xml.html;

import java.io.IOException;
import java.io.Reader;

import org.xml.sax.SAXException;

/**
 * An interface allowing Parser to invoke scanners.
 **/

public interface Scanner {

	/**
	 * Invoke a scanner.
	 * 
	 * @param r A source of characters to scan
	 * @param h A ScanHandler to report events to
	 **/

	public void scan(Reader r, ScanHandler h) throws IOException, SAXException;

	/**
	 * Reset the embedded locator.
	 * 
	 * @param publicid The publicid of the source
	 * @param systemid The systemid of the source
	 **/

	public void resetDocumentLocator(String publicid, String systemid);

	/**
	 * Signal to the scanner to start CDATA content mode.
	 **/

	public void startCDATA();

}

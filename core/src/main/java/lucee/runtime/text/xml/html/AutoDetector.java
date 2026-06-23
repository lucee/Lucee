package lucee.runtime.text.xml.html;

import java.io.InputStream;
import java.io.Reader;

/**
 * Classes which accept an InputStream and provide a Reader which figures out the encoding of the
 * InputStream and reads characters from it should conform to this interface.
 * 
 * @see java.io.InputStream
 * @see java.io.Reader
 */

public interface AutoDetector {

	/**
	 * Given an InputStream, return a suitable Reader that understands the presumed character encoding
	 * of that InputStream. If bytes are consumed from the InputStream in the process, they <i>must</i>
	 * be pushed back onto the InputStream so that they can be reinterpreted as characters.
	 * 
	 * @param i The InputStream
	 * @return A Reader that reads from the InputStream
	 */

	public Reader autoDetectingReader(InputStream i);

}

package lucee.runtime.text.xml.html;

import org.xml.sax.SAXException;

/**
 * An interface that Scanners use to report events in the input stream.
 **/

public interface ScanHandler {
	/**
	 * Reports an attribute name without a value.
	 **/

	public void adup(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports an attribute name; a value will follow.
	 **/

	public void aname(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports an attribute value.
	 **/

	public void aval(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports the content of a CDATA section (not a CDATA element)
	 */
	public void cdsect(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports a <!....> declaration - typically a DOCTYPE
	 */

	public void decl(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports an entity reference or character reference.
	 **/

	public void entity(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports EOF.
	 **/

	public void eof(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports an end-tag.
	 **/

	public void etag(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports the general identifier (element type name) of a start-tag.
	 **/

	public void gi(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports character content.
	 **/

	public void pcdata(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports the data part of a processing instruction.
	 **/

	public void pi(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports the target part of a processing instruction.
	 **/

	public void pitarget(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports the close of a start-tag.
	 **/

	public void stagc(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports the close of an empty-tag.
	 **/

	public void stage(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Reports a comment.
	 **/

	public void cmnt(char[] buff, int offset, int length) throws SAXException;

	/**
	 * Returns the value of the last entity or character reference reported.
	 **/

	public int getEntity();
}

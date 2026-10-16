package lucee.runtime.text.xml.html;

import java.io.PrintWriter;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * A ContentHandler that generates PYX format instead of XML. Primarily useful for debugging.
 **/
public class PYXWriter implements ScanHandler, ContentHandler, LexicalHandler {

	private PrintWriter theWriter; // where we write to
	private static char[] dummy = new char[1];
	private String attrName; // saved attribute name

	// ScanHandler implementation

	@Override
	public void adup(char[] buff, int offset, int length) throws SAXException {
		theWriter.println(attrName);
		attrName = null;
	}

	@Override
	public void aname(char[] buff, int offset, int length) throws SAXException {
		theWriter.print('A');
		theWriter.write(buff, offset, length);
		theWriter.print(' ');
		attrName = new String(buff, offset, length);
	}

	@Override
	public void aval(char[] buff, int offset, int length) throws SAXException {
		theWriter.write(buff, offset, length);
		theWriter.println();
		attrName = null;
	}

	@Override
	public void cmnt(char[] buff, int offset, int length) throws SAXException {
		// theWriter.print('!');
		// theWriter.write(buff, offset, length);
		// theWriter.println();
	}

	@Override
	public void entity(char[] buff, int offset, int length) throws SAXException {}

	@Override
	public int getEntity() {
		return 0;
	}

	@Override
	public void eof(char[] buff, int offset, int length) throws SAXException {
		theWriter.close();
	}

	@Override
	public void etag(char[] buff, int offset, int length) throws SAXException {
		theWriter.print(')');
		theWriter.write(buff, offset, length);
		theWriter.println();
	}

	@Override
	public void decl(char[] buff, int offset, int length) throws SAXException {}

	@Override
	public void gi(char[] buff, int offset, int length) throws SAXException {
		theWriter.print('(');
		theWriter.write(buff, offset, length);
		theWriter.println();
	}

	@Override
	public void cdsect(char[] buff, int offset, int length) throws SAXException {
		pcdata(buff, offset, length);
	}

	@Override
	public void pcdata(char[] buff, int offset, int length) throws SAXException {
		if (length == 0) return; // nothing to do
		boolean inProgress = false;
		length += offset;
		for (int i = offset; i < length; i++) {
			if (buff[i] == '\n') {
				if (inProgress) {
					theWriter.println();
				}
				theWriter.println("-\\n");
				inProgress = false;
			}
			else {
				if (!inProgress) {
					theWriter.print('-');
				}
				switch (buff[i]) {
				case '\t':
					theWriter.print("\\t");
					break;
				case '\\':
					theWriter.print("\\\\");
					break;
				default:
					theWriter.print(buff[i]);
				}
				inProgress = true;
			}
		}
		if (inProgress) {
			theWriter.println();
		}
	}

	@Override
	public void pitarget(char[] buff, int offset, int length) throws SAXException {
		theWriter.print('?');
		theWriter.write(buff, offset, length);
		theWriter.write(' ');
	}

	@Override
	public void pi(char[] buff, int offset, int length) throws SAXException {
		theWriter.write(buff, offset, length);
		theWriter.println();
	}

	@Override
	public void stagc(char[] buff, int offset, int length) throws SAXException {
		// theWriter.println("!"); // FIXME
	}

	@Override
	public void stage(char[] buff, int offset, int length) throws SAXException {
		theWriter.println("!"); // FIXME
	}

	// SAX ContentHandler implementation

	@Override
	public void characters(char[] buff, int offset, int length) throws SAXException {
		pcdata(buff, offset, length);
	}

	@Override
	public void endDocument() throws SAXException {
		theWriter.close();
	}

	@Override
	public void endElement(String uri, String localname, String qname) throws SAXException {
		if (qname.length() == 0) qname = localname;
		theWriter.print(')');
		theWriter.println(qname);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {}

	@Override
	public void ignorableWhitespace(char[] buff, int offset, int length) throws SAXException {
		characters(buff, offset, length);
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		theWriter.print('?');
		theWriter.print(target);
		theWriter.print(' ');
		theWriter.println(data);
	}

	@Override
	public void setDocumentLocator(Locator locator) {}

	@Override
	public void skippedEntity(String name) throws SAXException {}

	@Override
	public void startDocument() throws SAXException {}

	@Override
	public void startElement(String uri, String localname, String qname, Attributes atts) throws SAXException {
		if (qname.length() == 0) qname = localname;
		theWriter.print('(');
		theWriter.println(qname);
		int length = atts.getLength();
		for (int i = 0; i < length; i++) {
			qname = atts.getQName(i);
			if (qname.length() == 0) qname = atts.getLocalName(i);
			theWriter.print('A');
			// theWriter.print(atts.getType(i)); // DEBUG
			theWriter.print(qname);
			theWriter.print(' ');
			theWriter.println(atts.getValue(i));
		}
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {}

	// Default LexicalHandler implementation

	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		cmnt(ch, start, length);
	}

	@Override
	public void endCDATA() throws SAXException {}

	@Override
	public void endDTD() throws SAXException {}

	@Override
	public void endEntity(String name) throws SAXException {}

	@Override
	public void startCDATA() throws SAXException {}

	@Override
	public void startDTD(String name, String publicId, String systemId) throws SAXException {}

	@Override
	public void startEntity(String name) throws SAXException {}

	// Constructor

	public PYXWriter(Writer w) {
		if (w instanceof PrintWriter) {
			theWriter = (PrintWriter) w;
		}
		else {
			theWriter = new PrintWriter(w);
		}
	}
}

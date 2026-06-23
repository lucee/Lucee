package lucee.runtime.text.xml.html.jaxp;

import java.io.IOException;

import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * This is a simpler adapter class that allows using SAX1 interface on top of basic SAX2
 * implementation, such as TagSoup.
 *
 * @author Tatu Saloranta (cowtowncoder@yahoo.com)
 * @deprecated
 */
@Deprecated
public class SAX1ParserAdapter implements org.xml.sax.Parser {
	final XMLReader xmlReader;

	public SAX1ParserAdapter(XMLReader xr) {
		xmlReader = xr;
	}

	// Sax1 API impl

	@Override
	public void parse(InputSource source) throws SAXException {
		try {
			xmlReader.parse(source);
		}
		catch (IOException ioe) {
			throw new SAXException(ioe);
		}
	}

	@Override
	public void parse(String systemId) throws SAXException {
		try {
			xmlReader.parse(systemId);
		}
		catch (IOException ioe) {
			throw new SAXException(ioe);
		}
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public void setDocumentHandler(DocumentHandler h) {
		xmlReader.setContentHandler(new DocHandlerWrapper(h));
	}

	@Override
	public void setDTDHandler(DTDHandler h) {
		xmlReader.setDTDHandler(h);
	}

	@Override
	public void setEntityResolver(EntityResolver r) {
		xmlReader.setEntityResolver(r);
	}

	@Override
	public void setErrorHandler(ErrorHandler h) {
		xmlReader.setErrorHandler(h);
	}

	@Override
	public void setLocale(java.util.Locale locale) throws SAXException {
		/*
		 * I have no idea what this is supposed to do... so let's throw an exception
		 */
		throw new SAXNotSupportedException("TagSoup does not implement setLocale() method");
	}

	// Helper classes:

	/**
	 * We need another helper class to deal with differences between Sax2 handler (content handler), and
	 * Sax1 handler (document handler)
	 * 
	 * @deprecated
	 */
	@Deprecated
	final static class DocHandlerWrapper implements ContentHandler {
		final DocumentHandler docHandler;

		final AttributesWrapper mAttrWrapper = new AttributesWrapper();

		/**
		 * @deprecated
		 */
		@Deprecated
		DocHandlerWrapper(DocumentHandler h) {
			docHandler = h;
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			docHandler.characters(ch, start, length);
		}

		@Override
		public void endDocument() throws SAXException {
			docHandler.endDocument();
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName == null) {
				qName = localName;
			}
			docHandler.endElement(qName);
		}

		@Override
		public void endPrefixMapping(String prefix) {
			// no equivalent in SAX1, ignore
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
			docHandler.ignorableWhitespace(ch, start, length);
		}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {
			docHandler.processingInstruction(target, data);
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			docHandler.setDocumentLocator(locator);
		}

		@Override
		public void skippedEntity(String name) {
			// no equivalent in SAX1, ignore
		}

		@Override
		public void startDocument() throws SAXException {
			docHandler.startDocument();
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
			if (qName == null) {
				qName = localName;
			}
			// Also, need to wrap Attributes to look like AttributeLost
			mAttrWrapper.setAttributes(attrs);
			docHandler.startElement(qName, mAttrWrapper);
		}

		@Override
		public void startPrefixMapping(String prefix, String uri) {
			// no equivalent in SAX1, ignore
		}
	}

	/**
	 * And one more helper to deal with attribute access differences
	 * 
	 * @deprecated
	 */
	@Deprecated
	final static class AttributesWrapper implements AttributeList {
		Attributes attrs;

		public AttributesWrapper() {}

		public void setAttributes(Attributes a) {
			attrs = a;
		}

		@Override
		public int getLength() {
			return attrs.getLength();
		}

		@Override
		public String getName(int i) {
			String n = attrs.getQName(i);
			return (n == null) ? attrs.getLocalName(i) : n;
		}

		@Override
		public String getType(int i) {
			return attrs.getType(i);
		}

		@Override
		public String getType(String name) {
			return attrs.getType(name);
		}

		@Override
		public String getValue(int i) {
			return attrs.getValue(i);
		}

		@Override
		public String getValue(String name) {
			return attrs.getValue(name);
		}
	}
}

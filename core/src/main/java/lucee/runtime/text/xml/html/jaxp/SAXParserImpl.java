package lucee.runtime.text.xml.html.jaxp;

import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.SAXParser;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import lucee.runtime.text.xml.html.Parser;

/**
 * This is a simple implementation of JAXP {@link SAXParser}, to allow easier integration of TagSoup
 * with the default JDK xml processing stack.
 *
 * @author Tatu Saloranta (cowtowncoder@yahoo.com)
 */
public class SAXParserImpl extends SAXParser {
	final lucee.runtime.text.xml.html.Parser parser;

	protected SAXParserImpl() // used by factory, for prototypes
	{
		super();
		parser = new lucee.runtime.text.xml.html.Parser();
	}

	public static SAXParserImpl newInstance(Map features) throws SAXException {
		SAXParserImpl parser = new SAXParserImpl();
		if (features != null) {
			Iterator it = features.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				parser.setFeature((String) entry.getKey(), ((Boolean) entry.getValue()).booleanValue());
			}
		}
		return parser;
	}

	// // // JAXP API implementation:

	/**
	 * To support SAX1 interface, we'll need to use an adapter.
	 * 
	 * @deprecated
	 */
	@Deprecated
	@Override
	public org.xml.sax.Parser getParser() throws SAXException {
		return new SAX1ParserAdapter(parser);
	}

	@Override
	public XMLReader getXMLReader() {
		return parser;
	}

	@Override
	public boolean isNamespaceAware() {
		try {
			return parser.getFeature(Parser.namespacesFeature);
		}
		catch (SAXException sex) { // should never happen... so:
			throw new RuntimeException(sex.getMessage());
		}
	}

	@Override
	public boolean isValidating() {
		try {
			return parser.getFeature(Parser.validationFeature);
		}
		catch (SAXException sex) { // should never happen... so:
			throw new RuntimeException(sex.getMessage());
		}
	}

	@Override
	public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
		parser.setProperty(name, value);
	}

	@Override
	public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		return parser.getProperty(name);
	}

	// // // Additional convenience methods

	public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
		parser.setFeature(name, value);
	}

	public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		return parser.getFeature(name);
	}
}

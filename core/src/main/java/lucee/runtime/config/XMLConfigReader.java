package lucee.runtime.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import lucee.print;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.lang.StringUtil;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 *
 * Die FunctionLibFactory ist der Produzent fuer eine oder mehrere FunctionLib, d.H. ueber statische
 * Methoden (get, getDir) koennen FunctionLibs geladen werden. Die FunctionLibFactory erbt sich vom
 * DefaultHandler.
 */
public final class XMLConfigReader extends DefaultHandler implements LexicalHandler {

	private XMLReader xmlReader;

	private Struct root = new StructImpl();
	private Struct current;
	// private Struct parent;
	private Stack<Struct> ancestor = new Stack<>();
	private boolean trimBody;

	private ReadRule readRule;
	private NameRule nameRule;

	public XMLConfigReader(Resource file, boolean trimBody, ReadRule readRule, NameRule nameRule) throws SAXException, IOException {
		super();
		current = root;

		this.trimBody = trimBody;
		this.readRule = readRule;
		this.nameRule = nameRule;
		Reader r = null;
		try {
			init(new InputSource(r = IOUtil.getReader(file.getInputStream(), (Charset) null)));
		}
		finally {
			IOUtil.closeEL(r);
		}
	}

	private void init(InputSource is) throws SAXException, IOException {
		xmlReader = XMLUtil.createXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.setErrorHandler(this);
		// xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", this);
		xmlReader.parse(is);
	}

	@Override
	public void startElement(String uri, String name, String qName, Attributes attrs) {
		qName = nameRule.translate(qName);
		Struct parent = current;
		ancestor.add(parent);
		current = new StructImpl(Struct.TYPE_LINKED);

		// attrs
		int len = attrs.getLength();
		for (int i = 0; i < len; i++) {
			current.setEL(nameRule.translate(attrs.getQName(i)), attrs.getValue(i));
		}

		Object existing = parent.get(qName, null);
		if (!readRule.asArray(qName)) {
			if (existing instanceof Array) {
				((Array) existing).appendEL(current);
			}
			else if (existing instanceof Struct) {
				Array arr = new ArrayImpl();
				arr.appendEL(existing);
				arr.appendEL(current);
				parent.setEL(qName, arr);
			}
			else {
				parent.setEL(qName, current);
			}
		}
		else {
			if (existing != null) {
				((Array) existing).appendEL(current);
			}
			else {
				Array arr = new ArrayImpl();
				arr.appendEL(current);
				parent.setEL(qName, arr);
			}
		}
	}

	@Override
	public void endElement(String uri, String name, String qName) {
		qName = nameRule.translate(qName);
		if (trimBody) {
			String body = (String) current.get("_body_", null);
			if (body != null) {
				if (StringUtil.isEmpty(body, true)) current.remove("_body_");
				else current.setEL("_body_", body.trim());
			}
		}
		current = ancestor.pop();
	}

	@Override
	public void characters(char ch[], int start, int length) {
		String body = (String) current.get("_body_", null);
		if (body == null) current.put("_body_", new String(ch, start, length));
		else current.put("_body_", body + new String(ch, start, length));
	}

	public Struct getData() {
		return root;
	}

	@Override
	public void comment(char ch[], int start, int length) throws SAXException {
		String comment = (String) current.get("_comment_", null);
		if (comment == null) current.put("_comment_", new String(ch, start, length));
		else current.put("_comment_", comment + new String(ch, start, length));
	}

	@Override
	public void endCDATA() throws SAXException {}

	@Override
	public void endDTD() throws SAXException {}

	@Override
	public void endEntity(String arg0) throws SAXException {}

	@Override
	public void startCDATA() throws SAXException {}

	@Override
	public void startDTD(String arg0, String arg1, String arg2) throws SAXException {}

	@Override
	public void startEntity(String arg0) throws SAXException {}

	public static void main(String[] args) throws Exception {
		Resource res = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test/lucee-server/context/lucee-server.xml");
		res = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test/lucee-server/context/test.xml");
		res = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test/webapps/ROOT/WEB-INF/lucee/lucee-web.xml.cfm");
		res = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Projects/Lucee/Lucee5/core/src/main/java/resource/config/web.xml");
		Resource trg = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Projects/Lucee/Lucee5/core/src/main/java/resource/config/web.json");

		XMLConfigReader reader = new XMLConfigReader(res, true, new ReadRule(), new NameRule());
		String str = ser(reader.getData().get("cfLuceeConfiguration"));
		IOUtil.write(trg, str, CharsetUtil.UTF8, false);
		print.e(str);

		// Object result = new JSONExpressionInterpreter().interpret(null, str);
		// print.e(result);

	}

	private static String ser(Object var) throws PageException {
		try {
			JSONConverter json = new JSONConverter(true, Charset.forName("UTF-8"), JSONDateFormat.PATTERN_CF, true, true);

			// TODO get secure prefix from application.cfc
			return json.serialize(null, var, SerializationSettings.SERIALIZE_AS_ROW);
		}
		catch (ConverterException e) {
			throw Caster.toPageException(e);
		}
	}

	public static class ReadRule {
		private Set<String> names = new HashSet<>();

		public ReadRule() {
			this.names.add("data-source");
			this.names.add("label");

		}

		public boolean asArray(String name) {
			return names.contains(name);
		}
	}

	public static class NameRule {

		public String translate(String name) {
			int last = 0;
			int index;

			while ((index = name.indexOf('-', last)) != -1) {
				if (index + 1 == name.length()) break;
				name = new StringBuilder(name.substring(0, index)).append(Character.toUpperCase(name.charAt(index + 1))).append(name.substring(index + 2)).toString();
				last = index + 1;
			}
			return name;
		}
	}

}

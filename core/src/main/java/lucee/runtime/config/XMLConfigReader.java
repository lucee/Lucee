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
public final class XMLConfigReader extends DefaultHandler {
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

	private XMLReader xmlReader;

	private Struct root = new StructImpl();
	private Struct current;
	// private Struct parent;
	private Stack<Struct> ancestor = new Stack<>();
	private boolean trimBody;

	private ReadRule readRule;

	public XMLConfigReader(Resource file, boolean trimBody, ReadRule readRule) throws SAXException, IOException {
		super();
		current = root;

		this.trimBody = trimBody;
		this.readRule = readRule;
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
		// xmlReader.setEntityResolver(new FunctionLibEntityResolver());
		xmlReader.parse(is);
	}

	@Override
	public void startElement(String uri, String name, String qName, Attributes attrs) {
		Struct parent = current;
		ancestor.add(parent);
		current = new StructImpl(Struct.TYPE_LINKED);

		// attrs
		int len = attrs.getLength();
		for (int i = 0; i < len; i++) {
			current.setEL(attrs.getQName(i), attrs.getValue(i));
		}

		Object existing = parent.get(qName, null);
		if (!readRule.asArray(qName)) {
			if (existing instanceof Array) {
				((Array) existing).appendEL(current);
			}
			else if (existing instanceof Struct) {
				Array arr = new ArrayImpl();
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
		if (trimBody) {
			String body = (String) current.get("body", null);
			if (body != null) {
				if (StringUtil.isEmpty(body, true)) current.remove("body");
				else current.setEL("body", body.trim());
			}
		}
		current = ancestor.pop();
	}

	@Override
	public void characters(char ch[], int start, int length) {
		String body = (String) current.get("body", null);
		if (body == null) current.put("body", new String(ch, start, length));
		else current.put("body", body + new String(ch, start, length));
	}

	public Struct getData() {
		return root;
	}

	public static void main(String[] args) throws Exception {

		if (true) return;
		Resource res = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test/lucee-server/context/lucee-server.xml");
		res = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test/lucee-server/context/test.xml");
		res = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test/webapps/ROOT/WEB-INF/lucee/lucee-web.xml.cfm");
		res = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Projects/Lucee/Lucee5/core/src/main/java/resource/config/server.xml");
		Resource trg = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Projects/Lucee/Lucee5/core/src/main/java/resource/config/server.json");

		XMLConfigReader reader = new XMLConfigReader(res, true, new ReadRule());
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

}

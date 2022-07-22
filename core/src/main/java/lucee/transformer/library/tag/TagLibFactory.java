/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.transformer.library.tag;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.collection.MapFactory;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Identification;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.cfml.evaluator.ChildEvaluator;
import lucee.transformer.cfml.evaluator.TagEvaluator;
import lucee.transformer.library.function.FunctionLibFactory;

/**
 * Die Klasse TagLibFactory liest die XML Repraesentation einer TLD ein und laedt diese in eine
 * Objektstruktur. Sie tut dieses mithilfe eines Sax Parser. Die Klasse kann sowohl einzelne Files
 * oder gar ganze Verzeichnisse von TLD laden.
 */
public final class TagLibFactory extends DefaultHandler {
	/**
	 * Field <code>TYPE_CFML</code>
	 */
	public final static short TYPE_CFML = 0;
	/**
	 * Field <code>TYPE_JSP</code>
	 */
	public final static short TYPE_JSP = 1;

	// private short type=TYPE_CFML;

	private XMLReader xmlReader;

	private static Map<String, TagLib> hashLib = MapFactory.<String, TagLib>getConcurrentMap();
	private static TagLib[] systemTLDs = new TagLib[2];
	private final TagLib lib;

	private TagLibTag tag;
	private boolean insideTag = false;
	private boolean insideScript = false;
	// private boolean insideBundle=false;

	private TagLibTagAttr att;
	private boolean insideAtt = false;

	private String inside;
	private StringBuffer content = new StringBuffer();
	private TagLibTagScript script;
	private Attributes attributes;
	private final Identification id;
	// System default tld
	// private final static String TLD_1_0= "/resource/tld/web-cfmtaglibrary_1_0";

	private final static String TLD_BASE = "/resource/tld/core-base.tld";
	private final static String TLD_CFML = "/resource/tld/core-cfml.tld";
	private final static String TLD_LUCEE = "/resource/tld/core-lucee.tld";

	/**
	 * Privater Konstruktor, der als Eingabe die TLD als File Objekt erhaelt.
	 * 
	 * @param saxParser String Klassenpfad zum Sax Parser.
	 * @param file File Objekt auf die TLD.
	 * @throws TagLibException
	 * @throws IOException
	 */
	private TagLibFactory(TagLib lib, Resource res, Identification id) throws TagLibException {
		this.id = id;
		this.lib = lib == null ? new TagLib() : lib;
		Reader r = null;
		try {
			InputSource is = new InputSource(r = IOUtil.getReader(res.getInputStream(), (Charset) null));
			is.setSystemId(res.getPath());
			init(is);
		}
		catch (IOException e) {
			throw new TagLibException(e);
		}
		finally {
			try {
				IOUtil.close(r);
			}
			catch (IOException e) {
				throw new TagLibException(e);
			}
		}
	}

	/**
	 * Privater Konstruktor, der als Eingabe die TLD als File Objekt erhaelt.
	 * 
	 * @param saxParser String Klassenpfad zum Sax Parser.
	 * @param file File Objekt auf die TLD.
	 * @throws TagLibException
	 */
	private TagLibFactory(TagLib lib, InputStream stream, Identification id) throws TagLibException {
		this.id = id;
		this.lib = lib == null ? new TagLib() : lib;
		try {
			InputSource is = new InputSource(IOUtil.getReader(stream, SystemUtil.getCharset()));
			// is.setSystemId(file.toString());
			init(is);
		}
		catch (IOException e) {
			throw new TagLibException(e);
		}
	}

	/**
	 * Privater Konstruktor nur mit Sax Parser Definition, liest Default TLD vom System ein.
	 * 
	 * @param saxParser String Klassenpfad zum Sax Parser.
	 * @throws TagLibException
	 */
	private TagLibFactory(TagLib lib, String systemTLD, Identification id) throws TagLibException {
		this.id = id;
		this.lib = lib == null ? new TagLib() : lib;
		InputSource is = new InputSource(this.getClass().getResourceAsStream(systemTLD));
		init(is);
		this.lib.setIsCore(true);
	}

	/**
	 * Generelle Initialisierungsmetode der Konstruktoren.
	 * 
	 * @param saxParser String Klassenpfad zum Sax Parser.
	 * @param is InputStream auf die TLD.
	 * @throws TagLibException
	 */
	private void init(InputSource is) throws TagLibException {
		// print.dumpStack();
		try {
			xmlReader = XMLUtil.createXMLReader();
			xmlReader.setContentHandler(this);
			xmlReader.setErrorHandler(this);
			xmlReader.setEntityResolver(new TagLibEntityResolver());
			xmlReader.parse(is);
		}
		catch (IOException e) {

			// String fileName=is.getSystemId();
			// String message="IOException: ";
			// if(fileName!=null) message+="In File ["+fileName+"], ";
			throw new TagLibException(e);
		}
		catch (SAXException e) {
			// String fileName=is.getSystemId();
			// String message="SAXException: ";
			// if(fileName!=null) message+="In File ["+fileName+"], ";
			throw new TagLibException(e);
		}

	}

	/**
	 * Geerbte Methode von org.xml.sax.ContentHandler, wird bei durchparsen des XML, beim Auftreten
	 * eines Start-Tag aufgerufen.
	 * 
	 * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
	 */
	@Override
	public void startElement(String uri, String name, String qName, Attributes attributes) {

		inside = qName;
		this.attributes = attributes;
		if (qName.equals("tag")) startTag();
		else if (qName.equals("attribute")) startAtt();
		else if (qName.equals("script")) startScript();

	}

	/**
	 * Geerbte Methode von org.xml.sax.ContentHandler, wird bei durchparsen des XML, beim auftreten
	 * eines End-Tag aufgerufen.
	 * 
	 * @see org.xml.sax.ContentHandler#endElement(String, String, String)
	 */
	@Override
	public void endElement(String uri, String name, String qName) {
		setContent(content.toString().trim());
		content = new StringBuffer();
		inside = "";
		/*
		 * if(tag!=null && tag.getName().equalsIgnoreCase("input")) {
		 * print.ln(tag.getName()+"-"+att.getName()+":"+inside+"-"+insideTag+"-"+insideAtt);
		 * 
		 * }
		 */
		if (qName.equals("tag")) endTag();
		else if (qName.equals("attribute")) endAtt();
		else if (qName.equals("script")) endScript();

	}

	/**
	 * Geerbte Methode von org.xml.sax.ContentHandler, wird bei durchparsen des XML, zum einlesen des
	 * Content eines Body Element aufgerufen.
	 * 
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		content.append(new String(ch, start, length));
	}

	private void setContent(String value) {
		if (insideTag) {
			// Att Args
			if (insideAtt) {
				// description?
				// Name
				if (inside.equals("name")) att.setName(value);
				// alias
				else if (inside.equals("alias")) att.setAlias(value);

				// Values
				else if (inside.equals("values")) att.setValues(value);

				// Value Delimiter
				else if (inside.equals("value-delimiter")) att.setValueDelimiter(value);

				else if (inside.equals("introduced")) att.setIntroduced(value);

				// Required
				else if (inside.equals("required")) att.setRequired(Caster.toBooleanValue(value, false));
				// Rtexprvalue
				else if (inside.equals("rtexprvalue")) att.setRtexpr(Caster.toBooleanValue(value, false));
				// Type
				else if (inside.equals("type")) att.setType(value);
				// Default-Value
				else if (inside.equals("default-value")) att.setDefaultValue(value);
				// undefined-Value
				else if (inside.equals("undefined-value")) att.setUndefinedValue(value);
				// status
				else if (inside.equals("status")) att.setStatus(toStatus(value));
				// Description
				else if (inside.equals("description")) att.setDescription(value);
				// No-Name
				else if (inside.equals("noname")) att.setNoname(Caster.toBooleanValue(value, false));
				// default
				else if (inside.equals("default")) att.isDefault(Caster.toBooleanValue(value, false));
				else if (inside.equals("script-support")) att.setScriptSupport(value);
			}
			else if (insideScript) {
				// type
				if (inside.equals("type")) script.setType(TagLibTagScript.toType(value, TagLibTagScript.TYPE_NONE));
				if (inside.equals("rtexprvalue")) script.setRtexpr(Caster.toBooleanValue(value, false));
				if (inside.equals("context")) script.setContext(value);

			}
			// Tag Args
			else {
				// TODO TEI-class
				// Name
				if (inside.equals("name")) {
					tag.setName(value);
				}
				// TAG - Class
				else if (inside.equals("tag-class")) tag.setTagClassDefinition(value, id, attributes);
				else if (inside.equals("tagclass")) tag.setTagClassDefinition(value, id, attributes);
				// status
				else if (inside.equals("status")) tag.setStatus(toStatus(value));
				// TAG - description
				else if (inside.equals("description")) tag.setDescription(value);
				else if (inside.equals("introduced")) tag.setIntroduced(value);
				// TTE - Class
				else if (inside.equals("tte")) tag.setTagEval(toTagEvaluator(value));
				else if (inside.equals("tte-class")) tag.setTTEClassDefinition(value, id, attributes);
				// TTT - Class
				else if (inside.equals("ttt-class")) tag.setTTTClassDefinition(value, id, attributes);
				// TDBT - Class
				else if (inside.equals("tdbt-class")) tag.setTDBTClassDefinition(value, id, attributes);
				// TDBT - Class
				else if (inside.equals("att-class")) tag.setAttributeEvaluatorClassDefinition(value, id, attributes);
				// Body Content
				else if (inside.equals("body-content") || inside.equals("bodycontent")) {
					tag.setBodyContent(value);
				}
				// allow-removing-literal
				else if (inside.equals("allow-removing-literal")) {
					tag.setAllowRemovingLiteral(Caster.toBooleanValue(value, false));
				}
				else if (inside.equals("att-default-value")) tag.setAttributeUndefinedValue(value);
				else if (inside.equals("att-undefined-value")) tag.setAttributeUndefinedValue(value);

				// Handle Exceptions
				else if (inside.equals("handle-exception")) {
					tag.setHandleExceptions(Caster.toBooleanValue(value, false));
				}
				// Appendix
				else if (inside.equals("appendix")) {
					tag.setAppendix(Caster.toBooleanValue(value, false));
				}
				// Body rtexprvalue
				else if (inside.equals("body-rtexprvalue")) {
					tag.setParseBody(Caster.toBooleanValue(value, false));
				}
				// Att - min
				else if (inside.equals("attribute-min")) tag.setMin(Integer.parseInt(value));
				// Att - max
				else if (inside.equals("attribute-max")) tag.setMax(Integer.parseInt(value));
				// Att Type
				else if (inside.equals("attribute-type")) {
					int type = TagLibTag.ATTRIBUTE_TYPE_FIXED;
					if (value.toLowerCase().equals("fix")) type = TagLibTag.ATTRIBUTE_TYPE_FIXED;
					else if (value.toLowerCase().equals("fixed")) type = TagLibTag.ATTRIBUTE_TYPE_FIXED;
					else if (value.toLowerCase().equals("dynamic")) type = TagLibTag.ATTRIBUTE_TYPE_DYNAMIC;
					else if (value.toLowerCase().equals("noname")) type = TagLibTag.ATTRIBUTE_TYPE_NONAME;
					else if (value.toLowerCase().equals("mixed")) type = TagLibTag.ATTRIBUTE_TYPE_MIXED;
					else if (value.toLowerCase().equals("fulldynamic")) type = TagLibTag.ATTRIBUTE_TYPE_DYNAMIC;// deprecated
					tag.setAttributeType(type);
				}
			}
		}
		// Tag Lib
		else {
			// TagLib Typ
			if (inside.equals("jspversion")) {
				// type=TYPE_JSP;
				lib.setType("jsp");
			}
			else if (inside.equals("cfml-version")) {
				// type=TYPE_CFML;
				lib.setType("cfml");
			}

			// EL Class
			else if (inside.equals("el-class")) lib.setELClass(value, id, attributes);
			// Name-Space
			else if (inside.equals("name-space")) lib.setNameSpace(value);
			// Name Space Sep
			else if (inside.equals("name-space-separator")) lib.setNameSpaceSeperator(value);
			// short-name
			else if (inside.equals("short-name")) lib.setShortName(value);
			else if (inside.equals("shortname")) lib.setShortName(value);
			// display-name
			else if (inside.equals("display-name")) lib.setDisplayName(value);
			else if (inside.equals("displayname")) lib.setDisplayName(value);
			// ignore-unknow-tags
			else if (inside.equals("ignore-unknow-tags")) lib.setIgnoreUnknowTags(Caster.toBooleanValue(value, false));

			else if (inside.equals("uri")) {
				try {
					lib.setUri(value);
				}
				catch (URISyntaxException e) {
				}
			}
			else if (inside.equals("description")) lib.setDescription(value);

		}
	}

	private static TagEvaluator toTagEvaluator(String value) {
		String[] arr = ListUtil.listToStringArray(value, ':');
		if (arr.length == 2 && arr[0].trim().equalsIgnoreCase("parent")) {
			String parent = arr[1].trim();
			return new ChildEvaluator(parent);
		}
		throw new RuntimeException(value + " is not supported as the definition, you can do for example [parent:<parent-name>]!");
	}

	/**
	 * Wird jedesmal wenn das Tag tag beginnt aufgerufen, um intern in einen anderen Zustand zu
	 * gelangen.
	 */
	private void startTag() {
		tag = new TagLibTag(lib);
		insideTag = true;
	}

	/**
	 * Wird jedesmal wenn das Tag tag endet aufgerufen, um intern in einen anderen Zustand zu gelangen.
	 */
	private void endTag() {
		lib.setTag(tag);
		insideTag = false;
	}

	private void startScript() {
		script = new TagLibTagScript(tag);
		insideScript = true;
	}

	/**
	 * Wird jedesmal wenn das Tag tag endet aufgerufen, um intern in einen anderen Zustand zu gelangen.
	 */
	private void endScript() {
		tag.setScript(script);
		insideScript = false;
	}

	/**
	 * Wird jedesmal wenn das Tag attribute beginnt aufgerufen, um intern in einen anderen Zustand zu
	 * gelangen.
	 */
	private void startAtt() {
		att = new TagLibTagAttr(tag);
		insideAtt = true;
	}

	/**
	 * Wird jedesmal wenn das Tag tag endet aufgerufen, um intern in einen anderen Zustand zu gelangen.
	 */
	private void endAtt() {
		tag.setAttribute(att);
		insideAtt = false;
	}

	/**
	 * Gibt die interne TagLib zurueck.
	 * 
	 * @return Interne Repraesentation der zu erstellenden TagLib.
	 */
	private TagLib getLib() {
		return lib;
	}

	/**
	 * TagLib werden innerhalb der Factory in einer HashMap gecacht, so das diese einmalig von der
	 * Factory geladen werden. Diese Methode gibt eine gecachte TagLib anhand dessen key zurueck, falls
	 * diese noch nicht im Cache existiert, gibt die Methode null zurueck.
	 * 
	 * @param key Absoluter Filepfad zur TLD.
	 * @return TagLib
	 */
	private static TagLib getHashLib(String key) {
		return hashLib.get(key);
	}

	/**
	 * Laedt mehrere TagLib's die innerhalb eines Verzeichnisses liegen.
	 * 
	 * @param dir Verzeichnis im dem die TagLib's liegen.
	 * @param saxParser Definition des Sax Parser mit dem die TagLib's eingelesen werden sollen.
	 * @return TagLib's als Array
	 * @throws TagLibException
	 */
	public static TagLib[] loadFromDirectory(Resource dir, Identification id) throws TagLibException {
		if (!dir.isDirectory()) return new TagLib[0];
		ArrayList<TagLib> arr = new ArrayList<TagLib>();

		Resource[] files = dir.listResources(new ExtensionResourceFilter(new String[] { "tld", "tldx" }));
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) arr.add(TagLibFactory.loadFromFile(files[i], id));

		}
		return arr.toArray(new TagLib[arr.size()]);
	}

	/**
	 * Laedt eine einzelne TagLib.
	 * 
	 * @param file TLD die geladen werden soll.
	 * @param saxParser Definition des Sax Parser mit dem die TagLib eingelsesen werden soll.
	 * @return TagLib
	 * @throws TagLibException
	 */
	public static TagLib loadFromFile(Resource res, Identification id) throws TagLibException {

		// Read in XML
		TagLib lib = TagLibFactory.getHashLib(FunctionLibFactory.id(res));
		if (lib == null) {
			lib = new TagLibFactory(null, res, id).getLib();
			TagLibFactory.hashLib.put(FunctionLibFactory.id(res), lib);
		}
		lib.setSource(res.toString());
		return lib;
	}

	/**
	 * Laedt eine einzelne TagLib.
	 * 
	 * @param file TLD die geladen werden soll.
	 * @param saxParser Definition des Sax Parser mit dem die TagLib eingelsesen werden soll.
	 * @return TagLib
	 * @throws TagLibException
	 */
	public static TagLib loadFromStream(InputStream is, Identification id) throws TagLibException {
		return new TagLibFactory(null, is, id).getLib();
	}

	/**
	 * Laedt die Systeminterne TLD.
	 * 
	 * @param saxParser Definition des Sax Parser mit dem die FunctionLib eingelsesen werden soll.
	 * @return FunctionLib
	 * @throws TagLibException
	 */
	private static TagLib[] loadFromSystem(Identification id) throws TagLibException {

		if (systemTLDs[CFMLEngine.DIALECT_CFML] == null) {
			TagLib cfml = new TagLibFactory(null, TLD_BASE, id).getLib();
			TagLib lucee = cfml.duplicate(false);
			systemTLDs[CFMLEngine.DIALECT_CFML] = new TagLibFactory(cfml, TLD_CFML, id).getLib();
			systemTLDs[CFMLEngine.DIALECT_LUCEE] = new TagLibFactory(lucee, TLD_LUCEE, id).getLib();
		}
		return systemTLDs;
	}

	public static TagLib loadFromSystem(int dialect, Identification id) throws TagLibException {
		return loadFromSystem(id)[dialect];
	}

	public static TagLib[] loadFrom(Resource res, Identification id) throws TagLibException {
		if (res.isDirectory()) return loadFromDirectory(res, id);
		if (res.isFile()) return new TagLib[] { loadFromFile(res, id) };
		throw new TagLibException("can not load tag library descriptor from [" + res + "]");
	}

	/**
	 * return one FunctionLib contain content of all given Function Libs
	 * 
	 * @param tlds
	 * @return combined function lib
	 */
	public static TagLib combineTLDs(TagLib[] tlds) {
		TagLib tl = new TagLib();
		if (ArrayUtil.isEmpty(tlds)) return tl;

		setAttributes(tlds[0], tl);

		// add functions
		for (int i = 0; i < tlds.length; i++) {
			copyTags(tlds[i], tl);
		}
		return tl;
	}

	public static TagLib combineTLDs(Set tlds) {
		TagLib newTL = new TagLib(), tmp;
		if (tlds.size() == 0) return newTL;

		Iterator it = tlds.iterator();
		int count = 0;
		while (it.hasNext()) {
			tmp = (TagLib) it.next();
			if (count++ == 0) setAttributes(tmp, newTL);
			copyTags(tmp, newTL);
		}
		return newTL;
	}

	private static void setAttributes(TagLib extTL, TagLib newTL) {
		newTL.setDescription(extTL.getDescription());
		newTL.setDisplayName(extTL.getDisplayName());
		newTL.setELClassDefinition(extTL.getELClassDefinition());
		newTL.setIsCore(extTL.isCore());
		newTL.setNameSpace(extTL.getNameSpace());
		newTL.setNameSpaceSeperator(extTL.getNameSpaceSeparator());
		newTL.setShortName(extTL.getShortName());
		newTL.setSource(extTL.getSource());
		newTL.setType(extTL.getType());
		newTL.setUri(extTL.getUri());

	}

	private static void copyTags(TagLib extTL, TagLib newTL) {
		Iterator it = extTL.getTags().entrySet().iterator();
		TagLibTag tlt;
		while (it.hasNext()) {
			tlt = (TagLibTag) ((Map.Entry) it.next()).getValue(); // TODO function must be duplicated because it gets a new FunctionLib assigned
			newTL.setTag(tlt);
		}
	}

	public static short toStatus(String value) {
		value = value.trim().toLowerCase();
		if ("deprecated".equals(value)) return TagLib.STATUS_DEPRECATED;
		if ("dep".equals(value)) return TagLib.STATUS_DEPRECATED;

		if ("unimplemented".equals(value)) return TagLib.STATUS_UNIMPLEMENTED;
		if ("unimplemeted".equals(value)) return TagLib.STATUS_UNIMPLEMENTED;
		if ("notimplemented".equals(value)) return TagLib.STATUS_UNIMPLEMENTED;
		if ("not-implemented".equals(value)) return TagLib.STATUS_UNIMPLEMENTED;
		if ("hidden".equals(value)) return TagLib.STATUS_HIDDEN;

		return TagLib.STATUS_IMPLEMENTED;
	}

	public static String toStatus(short value) {
		switch (value) {
		case TagLib.STATUS_DEPRECATED:
			return "deprecated";
		case TagLib.STATUS_UNIMPLEMENTED:
			return "unimplemented";
		case TagLib.STATUS_HIDDEN:
			return "hidden";
		}
		return "implemented";
	}

}
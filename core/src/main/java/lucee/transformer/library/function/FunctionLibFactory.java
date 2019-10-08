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
package lucee.transformer.library.function;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.digest.Hash;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Identification;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.util.ArrayUtil;
import lucee.transformer.library.tag.TagLibFactory;

/**
 *
 * Die FunctionLibFactory ist der Produzent fuer eine oder mehrere FunctionLib, d.H. ueber statische
 * Methoden (get, getDir) koennen FunctionLibs geladen werden. Die FunctionLibFactory erbt sich vom
 * DefaultHandler.
 */
public final class FunctionLibFactory extends DefaultHandler {
	private XMLReader xmlReader;
	// private File file;
	private boolean insideFunction = false, insideAttribute = false, insideReturn = false, insideBundle = false;
	private String inside;
	private StringBuilder content = new StringBuilder();

	private static Map<String, FunctionLib> hashLib = new HashMap<String, FunctionLib>();
	private static FunctionLib[] systemFLDs = new FunctionLib[2];
	private final FunctionLib lib;
	private FunctionLibFunction function;

	private FunctionLibFunctionArg arg;
	private Attributes attributes;
	private final Identification id;
	private final boolean core;

	// private final static String FLD_1_0= "/resource/fld/web-cfmfunctionlibrary_1_0";
	private final static String FLD_BASE = "/resource/fld/core-base.fld";
	private final static String FLD_CFML = "/resource/fld/core-cfml.fld";
	private final static String FLD_LUCEE = "/resource/fld/core-lucee.fld";

	/**
	 * Privater Konstruktor, der als Eingabe die FLD als InputStream erhaelt.
	 * 
	 * @param saxParser String Klassenpfad zum Sax Parser.
	 * @param is InputStream auf die TLD.
	 * @throws FunctionLibException
	 * 
	 *             private FunctionLibFactory(String saxParser,InputSource is) throws
	 *             FunctionLibException { super(); init(saxParser,is); }
	 */

	/**
	 * Privater Konstruktor, der als Eingabe die FLD als File Objekt erhaelt.
	 * 
	 * @param saxParser String Klassenpfad zum Sax Parser.
	 * @param file File Objekt auf die TLD.
	 * @throws FunctionLibException
	 */
	private FunctionLibFactory(FunctionLib lib, Resource file, Identification id, boolean core) throws FunctionLibException {
		super();
		this.id = id;
		this.lib = lib == null ? new FunctionLib() : lib;
		this.core = core;

		Reader r = null;
		try {
			init(new InputSource(r = IOUtil.getReader(file.getInputStream(), (Charset) null)));
		}
		catch (IOException e) {
			throw new FunctionLibException("File not found: " + e.getMessage());
		}
		finally {
			IOUtil.closeEL(r);
		}
	}

	/**
	 * Privater Konstruktor nur mit Sax Parser Definition, liest Default FLD vom System ein.
	 * 
	 * @param saxParser String Klassenpfad zum Sax Parser.
	 * @throws FunctionLibException
	 */
	private FunctionLibFactory(FunctionLib lib, String systemFLD, Identification id, boolean core) throws FunctionLibException {
		super();
		this.id = id;
		this.lib = lib == null ? new FunctionLib() : lib;
		this.core = core;
		InputSource is = new InputSource(this.getClass().getResourceAsStream(systemFLD));
		init(is);
	}

	/**
	 * Generelle Initialisierungsmetode der Konstruktoren.
	 * 
	 * @param saxParser String Klassenpfad zum Sax Parser.
	 * @param is InputStream auf die TLD.
	 * @throws FunctionLibException
	 */
	private void init(InputSource is) throws FunctionLibException {

		try {

			xmlReader = XMLUtil.createXMLReader();
			xmlReader.setContentHandler(this);
			xmlReader.setErrorHandler(this);
			xmlReader.setEntityResolver(new FunctionLibEntityResolver());
			xmlReader.parse(is);
		}
		catch (IOException e) {

			throw new FunctionLibException("IO Exception: " + e.getMessage());
		}
		catch (SAXException e) {
			throw new FunctionLibException("SaxException: " + e.getMessage());
		}

	}

	/**
	 * Geerbte Methode von org.xml.sax.ContentHandler, wird bei durchparsen des XML, beim Auftreten
	 * eines Start-Tag aufgerufen.
	 * 
	 * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
	 */
	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
		// Start Function
		inside = qName;
		this.attributes = atts;

		if (qName.equals("function")) startFunction();
		else if (qName.equals("argument")) startArg();
		else if (qName.equals("return")) startReturn();
		else if (qName.equals("bundle")) startBundle();
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
		content = new StringBuilder();
		inside = "";
		if (qName.equals("function")) endFunction();
		else if (qName.equals("argument")) endArg();
		else if (qName.equals("return")) endReturn();
		else if (qName.equals("bundle")) endBundle();

	}

	/**
	 * Wird jedesmal wenn das Tag function beginnt aufgerufen, um intern in einen anderen Zustand zu
	 * gelangen.
	 */
	private void startFunction() {
		function = new FunctionLibFunction(core);
		insideFunction = true;
	}

	/**
	 * Wird jedesmal wenn das Tag function endet aufgerufen, um intern in einen anderen Zustand zu
	 * gelangen.
	 */
	private void endFunction() {
		lib.setFunction(function);
		insideFunction = false;
	}

	/**
	 * Wird jedesmal wenn das Tag argument beginnt aufgerufen, um intern in einen anderen Zustand zu
	 * gelangen.
	 */
	private void startArg() {
		insideAttribute = true;
		arg = new FunctionLibFunctionArg();
	}

	/**
	 * Wird jedesmal wenn das Tag argument endet aufgerufen, um intern in einen anderen Zustand zu
	 * gelangen.
	 */
	private void endArg() {
		function.setArg(arg);
		insideAttribute = false;
	}

	/**
	 * Wird jedesmal wenn das Tag return beginnt aufgerufen, um intern in einen anderen Zustand zu
	 * gelangen.
	 */
	private void startReturn() {
		insideReturn = true;
	}

	/**
	 * Wird jedesmal wenn das Tag return endet aufgerufen, um intern in einen anderen Zustand zu
	 * gelangen.
	 */
	private void endReturn() {
		insideReturn = false;
	}

	private void startBundle() {
		insideBundle = true;
	}

	private void endBundle() {
		insideBundle = false;
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
		if (insideFunction) {
			// Attributes Value
			if (insideAttribute) {
				if (inside.equals("type")) arg.setType(value);
				else if (inside.equals("name")) arg.setName(value);
				else if (inside.equals("default")) arg.setDefaultValue(value);
				else if (inside.equals("default-value")) arg.setDefaultValue(value); // deprecated
				else if (inside.equals("status")) arg.setStatus(TagLibFactory.toStatus(value));
				else if (inside.equals("description")) arg.setDescription(value);
				else if (inside.equals("alias")) arg.setAlias(value);
				else if (inside.equals("introduced")) arg.setIntroduced(value);

				else if (inside.equals("required")) {
					arg.setRequired(value);
					if (arg.isRequired()) function.setArgMin(function.getArgMin() + 1);
				}
			}
			// Return Values
			else if (insideReturn) {
				if (inside.equals("type")) function.setReturn(value);
			}
			else if (insideBundle) {
				if (inside.equals("class")) function.setFunctionClass(value, id, attributes);
				// if(inside.equals("name")) function.setBundleName(value);
				// if(inside.equals("version")) function.setBundleVersion(value);
			}

			// Function Value
			else {
				if (inside.equals("name")) function.setName(value);

				else if (inside.equals("class")) function.setFunctionClass(value, id, attributes);

				else if (inside.equals("tte-class")) function.setTTEClass(value, id, attributes);
				if (inside.equals("keywords")) function.setKeywords(value);

				else if (inside.equals("introduced")) function.setIntroduced(value);

				else if (inside.equals("description")) function.setDescription(value);

				else if (inside.equals("member-name")) function.setMemberName(value);
				else if (inside.equals("member-position")) function.setMemberPosition(Caster.toIntValue(value, 1));
				else if (inside.equals("member-chaining")) function.setMemberChaining(Caster.toBooleanValue(value, false));
				else if (inside.equals("member-type")) function.setMemberType(value);

				else if (inside.equals("status")) function.setStatus(TagLibFactory.toStatus(value));

				else if (inside.equals("argument-type")) function.setArgType(value.equalsIgnoreCase("dynamic") ? FunctionLibFunction.ARG_DYNAMIC : FunctionLibFunction.ARG_FIX);

				else if (inside.equals("argument-min")) function.setArgMin(Integer.parseInt(value));

				else if (inside.equals("argument-max")) function.setArgMax(Integer.parseInt(value));
			}

		}
		else {
			// function lib values
			if (inside.equals("flib-version")) lib.setVersion(value);
			else if (inside.equals("short-name")) lib.setShortName(value);
			else if (inside.equals("uri")) {
				try {
					lib.setUri(value);
				}
				catch (URISyntaxException e) {}
			}
			else if (inside.equals("display-name")) lib.setDisplayName(value);
			else if (inside.equals("description")) lib.setDescription(value);
		}
	}

	/**
	 * Gibt die interne FunctionLib zurueck.
	 * 
	 * @return Interne Repraesentation der zu erstellenden FunctionLib.
	 */
	private FunctionLib getLib() {
		return lib;
	}

	/**
	 * Laedt mehrere FunctionLib's die innerhalb eines Verzeichnisses liegen.
	 * 
	 * @param dir Verzeichnis im dem die FunctionLib's liegen.
	 * @param saxParser Definition des Sax Parser mit dem die FunctionLib's eingelesen werden sollen.
	 * @return FunctionLib's als Array
	 * @throws FunctionLibException
	 */
	public static FunctionLib[] loadFromDirectory(Resource dir, Identification id) throws FunctionLibException {
		if (!dir.isDirectory()) return new FunctionLib[0];
		ArrayList<FunctionLib> arr = new ArrayList<FunctionLib>();

		Resource[] files = dir.listResources(new ExtensionResourceFilter(new String[] { "fld", "fldx" }));
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) arr.add(FunctionLibFactory.loadFromFile(files[i], id));
		}

		return arr.toArray(new FunctionLib[arr.size()]);
	}

	/**
	 * Laedt eine einzelne FunctionLib.
	 * 
	 * @param res FLD die geladen werden soll.
	 * @param saxParser Definition des Sax Parser mit dem die FunctionLib eingelsesen werden soll.
	 * @return FunctionLib
	 * @throws FunctionLibException
	 */
	public static FunctionLib loadFromFile(Resource res, Identification id) throws FunctionLibException {
		// Read in XML
		FunctionLib lib = FunctionLibFactory.hashLib.get(id(res));// getHashLib(file.getAbsolutePath());
		if (lib == null) {
			lib = new FunctionLibFactory(null, res, id, false).getLib();
			FunctionLibFactory.hashLib.put(id(res), lib);
		}
		lib.setSource(res.toString());

		return lib;
	}

	/**
	 * does not involve the content to create an id, value returned is based on metadata of the file
	 * (lastmodified,size)
	 * 
	 * @param res
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String id(Resource res) {
		String str = ResourceUtil.getCanonicalPathEL(res) + "|" + res.length() + "|" + res.lastModified();
		try {
			return Hash.md5(str);
		}
		catch (NoSuchAlgorithmException e) {
			return Caster.toString(HashUtil.create64BitHash(str));
		}
	}

	/**
	 * Laedt die Systeminterne FLD.
	 * 
	 * @param saxParser Definition des Sax Parser mit dem die FunctionLib eingelsesen werden soll.
	 * @return FunctionLib
	 * @throws FunctionLibException
	 */
	public static FunctionLib[] loadFromSystem(Identification id) throws FunctionLibException {
		if (systemFLDs[CFMLEngine.DIALECT_CFML] == null) {
			FunctionLib cfml = new FunctionLibFactory(null, FLD_BASE, id, true).getLib();
			FunctionLib lucee = cfml.duplicate(false);
			systemFLDs[CFMLEngine.DIALECT_CFML] = new FunctionLibFactory(cfml, FLD_CFML, id, true).getLib();
			systemFLDs[CFMLEngine.DIALECT_LUCEE] = new FunctionLibFactory(lucee, FLD_LUCEE, id, true).getLib();
		}
		return systemFLDs;
	}

	public static FunctionLib loadFromSystem(int dialect, Identification id) throws FunctionLibException {
		return loadFromSystem(id)[dialect];
	}

	/**
	 * return one FunctionLib contain content of all given Function Libs
	 * 
	 * @param flds
	 * @return combined function lib
	 */
	public static FunctionLib combineFLDs(FunctionLib[] flds) {
		FunctionLib fl = new FunctionLib();
		if (ArrayUtil.isEmpty(flds)) return fl;

		setAttributes(flds[0], fl);

		// add functions
		for (int i = 0; i < flds.length; i++) {
			copyFunctions(flds[i], fl);
		}
		return fl;
	}

	public static FunctionLib combineFLDs(Set flds) {
		FunctionLib newFL = new FunctionLib(), tmp;
		if (flds.size() == 0) return newFL;

		Iterator it = flds.iterator();
		int count = 0;
		while (it.hasNext()) {
			tmp = (FunctionLib) it.next();
			if (count++ == 0) setAttributes(tmp, newFL);
			copyFunctions(tmp, newFL);
		}
		return newFL;
	}

	/**
	 * copy function from one FunctionLib to another
	 * 
	 * @param extFL
	 * @param newFL
	 */
	private static void copyFunctions(FunctionLib extFL, FunctionLib newFL) {
		Iterator<Entry<String, FunctionLibFunction>> it = extFL.getFunctions().entrySet().iterator();
		FunctionLibFunction flf;
		while (it.hasNext()) {
			flf = it.next().getValue(); // TODO function must be duplicated because it gets a new FunctionLib assigned
			newFL.setFunction(flf);
		}
	}

	/**
	 * copy attributes from old fld to the new
	 * 
	 * @param extFL
	 * @param newFL
	 */
	private static void setAttributes(FunctionLib extFL, FunctionLib newFL) {
		newFL.setDescription(extFL.getDescription());
		newFL.setDisplayName(extFL.getDisplayName());
		newFL.setShortName(extFL.getShortName());
		newFL.setUri(extFL.getUri());
		newFL.setVersion(extFL.getVersion());
	}

}
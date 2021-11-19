/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.text.feed;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.CastableArray;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public final class FeedHandler extends DefaultHandler {

	private XMLReader xmlReader;

	// private StringBuffer content=new StringBuffer();

	private int deep = 0;
	private FeedStruct data;
	private String path = "";
	private Collection.Key inside;
	private Stack<FeedStruct> parents = new Stack<FeedStruct>();
	private FeedDeclaration decl;
	private Map<String, String> root = new HashMap<String, String>();
	private boolean hasDC;
	private boolean isAtom;

	private boolean inAuthor;

	private boolean inEntry;

	/**
	 * Constructor of the class
	 * 
	 * @param res
	 * @throws IOException
	 * @throws SAXException
	 */
	public FeedHandler(Resource res) throws IOException, SAXException {
		InputStream is = null;
		try {
			InputSource source = new InputSource(is = res.getInputStream());
			source.setSystemId(res.getPath());

			init(source);
		}
		finally {
			IOUtil.close(is);
		}
	}

	public FeedHandler(InputSource is) throws IOException, SAXException {
		init(is);

	}

	/**
	 * Constructor of the class
	 * 
	 * @param stream
	 * @throws IOException
	 * @throws SAXException
	 */
	public FeedHandler(InputStream stream) throws IOException, SAXException {
		InputSource is = new InputSource(IOUtil.getReader(stream, SystemUtil.getCharset()));
		init(is);
	}

	private void init(InputSource is) throws SAXException, IOException {
		// print.out("is:"+is);
		hasDC = false;
		data = new FeedStruct();
		xmlReader = XMLUtil.createXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.setErrorHandler(this);
		xmlReader.setDTDHandler(new DummyDTDHandler());
		xmlReader.parse(is);
	}

	/**
	 * @return the hasDC
	 */
	public boolean hasDC() {
		return hasDC;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		if (locator instanceof Locator2) {
			Locator2 locator2 = (Locator2) locator;
			root.put("encoding", locator2.getEncoding());
		}
	}

	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
		deep++;
		name = name(name, qName);

		if ("entry".equals(name)) inEntry = true;
		else if ("author".equals(name)) inAuthor = true;

		if (qName.startsWith("dc:")) {
			name = "dc_" + name;
			hasDC = true;
		}

		inside = KeyImpl.getInstance(name);
		if (StringUtil.isEmpty(path)) path = name;
		else {
			path += "." + name;
		}
		if (decl == null) {
			String decName = name;
			String version = atts.getValue("version");
			if ("feed".equals(decName)) {
				if (!StringUtil.isEmpty(version)) decName = "atom_" + version;
				else decName = "atom_1.0";
			}
			else {
				if (!StringUtil.isEmpty(version)) decName += "_" + version;
			}
			decl = FeedDeclaration.getInstance(decName);
			root.put("version", decName);
			isAtom = decl.getType().equals("atom");

		}

		FeedStruct sct = new FeedStruct(path, inside, uri);

		// attributes
		Map<String, String> attrs = getAttributes(atts, path);
		if (attrs != null) {
			Entry<String, String> entry;
			Iterator<Entry<String, String>> it = attrs.entrySet().iterator();
			sct.setHasAttribute(true);
			while (it.hasNext()) {
				entry = it.next();
				sct.setEL(entry.getKey(), entry.getValue());
			}
		}

		// assign
		if (!isAtom || deep < 4) {
			Object obj = data.get(inside, null);
			if (obj instanceof Array) {
				((Array) obj).appendEL(sct);
			}
			else if (obj instanceof FeedStruct) {
				Array arr = new ArrayImpl();
				arr.appendEL(obj);
				arr.appendEL(sct);
				data.setEL(inside, arr);
			}
			else if (obj instanceof String) {
				// wenn wert schon existiert wird castableArray in setContent erstellt
			}
			else {
				El el = decl.getDeclaration().get(path);
				if (el != null && (el.getQuantity() == El.QUANTITY_0_N || el.getQuantity() == El.QUANTITY_1_N)) {
					Array arr = new ArrayImpl();
					arr.appendEL(sct);
					data.setEL(inside, arr);
				}
				else data.setEL(inside, sct);

			}
		}
		parents.add(data);
		data = sct;
	}

	private String name(String name, String qName) {
		if (!StringUtil.isEmpty(name, true)) return name;
		return ListUtil.last(qName, ':');
	}

	@Override
	public void endElement(String uri, String name, String qName) {
		name = name(name, qName);

		if ("entry".equals(name)) inEntry = false;
		else if ("author".equals(name)) inAuthor = false;
		deep--;
		if (isAtom && deep >= (inEntry && inAuthor ? 4 : 3)) {
			String content = data.getString();
			Key[] keys = data.keys();
			StringBuilder sb = new StringBuilder();
			sb.append("<");
			sb.append(qName);

			// xmlns
			if (!parents.peek().getUri().equals(uri)) {
				sb.append(" xmlns=\"");
				sb.append(uri);
				sb.append("\"");
			}

			for (int i = 0; i < keys.length; i++) {
				sb.append(" ");
				sb.append(keys[i].getString());
				sb.append("=\"");
				sb.append(Caster.toString(data.get(keys[i], ""), ""));
				sb.append("\"");

			}

			if (!StringUtil.isEmpty(content)) {
				sb.append(">");
				sb.append(content);
				sb.append("</" + qName + ">");
			}
			else sb.append("/>");

			data = parents.pop();
			data.append(sb.toString().trim());
			// setContent(sb.toString().trim());

			path = data.getPath();
			inside = data.getInside();
			return;
		}

		// setContent(content.toString().trim());
		setContent(data.getString().trim());

		data = parents.pop();
		path = data.getPath();
		inside = data.getInside();
	}

	@Override
	public void characters(char ch[], int start, int length) {
		data.append(new String(ch, start, length));
		// content.append(new String(ch,start,length));
	}

	private void setContent(String value) {
		// print.out(path+":"+inside);
		if (StringUtil.isEmpty(inside)) return;

		if (data.hasAttribute()) {
			if (!StringUtil.isEmpty(value)) setEl(data, KeyConstants._value, value);
		}
		else {
			FeedStruct parent = parents.peek();
			setEl(parent, inside, value);
		}

	}

	private void setEl(Struct sct, Collection.Key key, String value) {
		Object existing = sct.get(key, null);

		if (existing instanceof CastableArray) {
			((CastableArray) existing).appendEL(value);
		}
		else if (existing instanceof String) {
			CastableArray ca = new CastableArray(existing);
			ca.appendEL(existing);
			ca.appendEL(value);
			sct.setEL(key, ca);
		}
		else sct.setEL(key, Caster.toString(value));

		/*
		 * if(existing instanceof Struct)sct.setEL(key,value); else if(existing instanceof
		 * Array)((Array)existing).appendEL(value); else if(existing!=null){ CastableArray ca=new
		 * CastableArray(existing); ca.appendEL(existing); ca.appendEL(value); sct.setEL(key,ca); } else
		 */
	}

	private Map<String, String> getAttributes(Attributes attrs, String path) {
		El el = decl.getDeclaration().get(path);

		int len = attrs.getLength();
		if ((el == null || el.getAttrs() == null) && len == 0) return null;

		Map<String, String> map = new HashMap<String, String>();
		if (el != null) {
			Attr[] defaults = el.getAttrs();
			if (defaults != null) {
				for (int i = 0; i < defaults.length; i++) {
					if (defaults[i].hasDefaultValue()) map.put(defaults[i].getName(), defaults[i].getDefaultValue());
				}
			}
		}
		for (int i = 0; i < len; i++) {
			map.put(attrs.getQName(i), attrs.getValue(i));
		}
		return map;
	}

	/**
	 * @return the properties
	 */
	public Struct getData() {
		return data;
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		Struct def = new StructImpl();
		Key[] entryLevel = decl.getEntryLevel();
		for (int i = 0; i < entryLevel.length; i++) {
			data = (FeedStruct) data.get(entryLevel[i], def);
		}
		data.putAll(root);
	}

}

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
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class RSSHandler extends DefaultHandler {

	private static final Key RSSLINK = KeyImpl.getInstance("RSSLINK");
	private static final Key CONTENT = KeyImpl.getInstance("CONTENT");

	private static final Key LINK = KeyImpl.getInstance("LINK");
	private static final Key DESCRIPTION = KeyImpl.getInstance("DESCRIPTION");

	private static Collection.Key[] COLUMNS = new Collection.Key[] { KeyImpl.getInstance("AUTHOREMAIL"), KeyImpl.getInstance("AUTHORNAME"), KeyImpl.getInstance("AUTHORURI"),
			KeyImpl.getInstance("CATEGORYLABEL"), KeyImpl.getInstance("CATEGORYSCHEME"), KeyImpl.getInstance("CATEGORYTERM"), KeyImpl.getInstance("COMMENTS"), CONTENT,
			KeyImpl.getInstance("CONTENTMODE"), KeyImpl.getInstance("CONTENTSRC"), KeyImpl.getInstance("CONTENTTYPE"), KeyImpl.getInstance("CONTRIBUTOREMAIL"),
			KeyImpl.getInstance("CONTRIBUTORNAME"), KeyImpl.getInstance("CONTRIBUTORURI"), KeyImpl.getInstance("CREATEDDATE"), KeyImpl.getInstance("EXPIRATIONDATE"),
			KeyConstants._ID, KeyImpl.getInstance("IDPERMALINK"), KeyImpl.getInstance("LINKHREF"), KeyImpl.getInstance("LINKHREFLANG"), KeyImpl.getInstance("LINKLENGTH"),
			KeyImpl.getInstance("LINKREL"), KeyImpl.getInstance("LINKTITLE"), KeyImpl.getInstance("LINKTYPE"), KeyImpl.getInstance("PUBLISHEDDATE"), KeyImpl.getInstance("RIGHTS"),
			RSSLINK, KeyImpl.getInstance("SOURCE"), KeyImpl.getInstance("SOURCEURL"), KeyImpl.getInstance("SUMMARY"), KeyImpl.getInstance("SUMMARYMODE"),
			KeyImpl.getInstance("SUMMARYSRC"), KeyImpl.getInstance("SUMMARYTYPE"), KeyImpl.getInstance("TITLE"), KeyImpl.getInstance("TITLETYPE"),
			KeyImpl.getInstance("UPDATEDDATE"), KeyImpl.getInstance("URI"), KeyImpl.getInstance("XMLBASE") };

	private XMLReader xmlReader;

	private String lcInside;
	private StringBuffer content = new StringBuffer();

	private boolean insideImage;
	private boolean insideItem;

	private Struct image;
	private Struct properties;
	private Query items;

	private Collection.Key inside;

	/**
	 * Constructor of the class
	 * 
	 * @param res
	 * @throws IOException
	 * @throws SAXException
	 * @throws DatabaseException
	 */
	public RSSHandler(Resource res) throws IOException, SAXException, DatabaseException {
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

	/**
	 * Constructor of the class
	 * 
	 * @param stream
	 * @throws IOException
	 * @throws SAXException
	 * @throws DatabaseException
	 */
	public RSSHandler(InputStream stream) throws IOException, SAXException, DatabaseException {
		InputSource is = new InputSource(IOUtil.getReader(stream, SystemUtil.getCharset()));
		init(is);
	}

	private void init(InputSource is) throws SAXException, IOException, DatabaseException {
		properties = new StructImpl();
		items = new QueryImpl(COLUMNS, 0, "query");
		xmlReader = XMLUtil.createXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.setErrorHandler(this);

		// xmlReader.setEntityResolver(new TagLibEntityResolver());
		xmlReader.parse(is);

		// properties.setEL("encoding",is.getEncoding());

	}

	@Override
	public void setDocumentLocator(Locator locator) {
		if (locator instanceof Locator2) {
			Locator2 locator2 = (Locator2) locator;
			properties.setEL("encoding", locator2.getEncoding());
		}
	}

	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
		inside = KeyImpl.getInstance(qName);
		lcInside = qName.toLowerCase();
		if (lcInside.equals("image")) insideImage = true;
		else if (qName.equals("item")) {
			items.addRow();
			insideItem = true;
		}
		else if (lcInside.equals("rss")) {
			String version = atts.getValue("version");
			if (!StringUtil.isEmpty(version)) properties.setEL("version", "rss_" + version);
		}

		/*
		 * / cloud else if(!insideItem && lcInside.equals("cloud")) {
		 * 
		 * 
		 * 
		 * String url = atts.getValue("url"); if(!StringUtil.isEmpty(url))items.setAtEL("LINKHREF",
		 * items.getRowCount(), url); String length = atts.getValue("length");
		 * if(!StringUtil.isEmpty(length))items.setAtEL("LINKLENGTH", items.getRowCount(), length); String
		 * type = atts.getValue("type"); if(!StringUtil.isEmpty(type))items.setAtEL("LINKTYPE",
		 * items.getRowCount(), type); }
		 */

		// enclosure
		else if (insideItem && lcInside.equals("enclosure")) {
			String url = atts.getValue("url");
			if (!StringUtil.isEmpty(url)) items.setAtEL("LINKHREF", items.getRowCount(), url);
			String length = atts.getValue("length");
			if (!StringUtil.isEmpty(length)) items.setAtEL("LINKLENGTH", items.getRowCount(), length);
			String type = atts.getValue("type");
			if (!StringUtil.isEmpty(type)) items.setAtEL("LINKTYPE", items.getRowCount(), type);
		}

		else if (atts.getLength() > 0) {
			int len = atts.getLength();
			Struct sct = new StructImpl();
			for (int i = 0; i < len; i++) {
				sct.setEL(atts.getQName(i), atts.getValue(i));
			}
			properties.setEL(inside, sct);
		}
	}

	@Override
	public void endElement(String uri, String name, String qName) {
		setContent(content.toString().trim());
		content = new StringBuffer();
		inside = null;
		lcInside = "";

		if (qName.equals("image")) insideImage = false;
		if (qName.equals("item")) insideItem = false;
	}

	@Override
	public void characters(char ch[], int start, int length) {
		content.append(new String(ch, start, length));
	}

	private void setContent(String value) {
		if (StringUtil.isEmpty(lcInside)) return;

		if (insideImage) {
			if (image == null) {
				image = new StructImpl();
				properties.setEL("image", image);
			}
			image.setEL(inside, value);
		}
		else if (insideItem) {
			try {
				items.setAt(toItemColumn(inside), items.getRowCount(), value);
			}
			catch (PageException e) {
				// print.err(inside);
			}

		}
		else {
			if (!(StringUtil.isEmpty(value, true) && properties.containsKey(inside))) properties.setEL(inside, value);
		}
	}

	private Collection.Key toItemColumn(Collection.Key key) {
		if (key.equalsIgnoreCase(LINK)) return RSSLINK;
		else if (key.equalsIgnoreCase(DESCRIPTION)) return CONTENT;
		return key;
	}

	/**
	 * @return the properties
	 */
	public Struct getProperties() {
		return properties;
	}

	/**
	 * @return the items
	 */
	public Query getItems() {
		return items;
	}
}

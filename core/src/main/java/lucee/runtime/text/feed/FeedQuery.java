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

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.CastableArray;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class FeedQuery {

	public static final Collection.Key VERSION = KeyConstants._VERSION;
	public static final Collection.Key ITEM = KeyConstants._ITEM;
	public static final Collection.Key ENTRY = KeyConstants._ENTRY;

	public static final Collection.Key AUTHOREMAIL = KeyImpl.getInstance("AUTHOREMAIL");
	public static final Collection.Key AUTHORNAME = KeyImpl.getInstance("AUTHORNAME");
	public static final Collection.Key AUTHORURI = KeyImpl.getInstance("AUTHORURI");
	public static final Collection.Key AUTHOR = KeyImpl.getInstance("AUTHOR");
	public static final Collection.Key CATEGORYLABEL = KeyImpl.getInstance("CATEGORYLABEL");
	public static final Collection.Key CATEGORYSCHEME = KeyImpl.getInstance("CATEGORYSCHEME");
	public static final Collection.Key CATEGORYTERM = KeyImpl.getInstance("CATEGORYTERM");
	public static final Collection.Key CATEGORY = KeyImpl.getInstance("CATEGORY");
	public static final Collection.Key COMMENTS = KeyConstants._COMMENTS;
	public static final Collection.Key CONTENT = KeyConstants._CONTENT;
	public static final Collection.Key CONTENTMODE = KeyImpl.getInstance("CONTENTMODE");
	public static final Collection.Key CONTENTSRC = KeyImpl.getInstance("CONTENTSRC");
	public static final Collection.Key CONTENTTYPE = KeyImpl.getInstance("CONTENTTYPE");
	public static final Collection.Key CONTRIBUTOREMAIL = KeyImpl.getInstance("CONTRIBUTOREMAIL");
	public static final Collection.Key CONTRIBUTORNAME = KeyImpl.getInstance("CONTRIBUTORNAME");
	public static final Collection.Key CONTRIBUTORURI = KeyImpl.getInstance("CONTRIBUTORURI");
	public static final Collection.Key CONTRIBUTOR = KeyImpl.getInstance("CONTRIBUTOR");
	public static final Collection.Key CREATEDDATE = KeyImpl.getInstance("CREATEDDATE");
	public static final Collection.Key CREATED = KeyImpl.getInstance("CREATED");
	public static final Collection.Key EXPIRATIONDATE = KeyImpl.getInstance("EXPIRATIONDATE");
	public static final Collection.Key ID = KeyConstants._ID;
	public static final Collection.Key IDPERMALINK = KeyImpl.getInstance("IDPERMALINK");
	public static final Collection.Key LINKHREF = KeyImpl.getInstance("LINKHREF");
	public static final Collection.Key LINKHREFLANG = KeyImpl.getInstance("LINKHREFLANG");
	public static final Collection.Key LINKLENGTH = KeyImpl.getInstance("LINKLENGTH");
	public static final Collection.Key LINKREL = KeyImpl.getInstance("LINKREL");
	public static final Collection.Key LINKTITLE = KeyImpl.getInstance("LINKTITLE");
	public static final Collection.Key LINKTYPE = KeyImpl.getInstance("LINKTYPE");
	public static final Collection.Key PUBLISHEDDATE = KeyImpl.getInstance("PUBLISHEDDATE");
	public static final Collection.Key PUBLISHED = KeyImpl.getInstance("PUBLISHED");
	public static final Collection.Key PUBDATE = KeyImpl.getInstance("pubDate");
	public static final Collection.Key RDF_ABOUT = KeyImpl.getInstance("rdf:about");

	public static final Collection.Key RIGHTS = KeyImpl.getInstance("RIGHTS");
	public static final Collection.Key RSSLINK = KeyImpl.getInstance("RSSLINK");
	public static final Collection.Key SOURCE = KeyConstants._SOURCE;
	public static final Collection.Key SOURCEURL = KeyImpl.getInstance("SOURCEURL");
	public static final Collection.Key SUMMARY = KeyImpl.getInstance("SUMMARY");
	public static final Collection.Key SUMMARYMODE = KeyImpl.getInstance("SUMMARYMODE");
	public static final Collection.Key SUMMARYSRC = KeyImpl.getInstance("SUMMARYSRC");
	public static final Collection.Key SUMMARYTYPE = KeyImpl.getInstance("SUMMARYTYPE");
	public static final Collection.Key TITLE = KeyImpl.getInstance("TITLE");
	public static final Collection.Key TITLETYPE = KeyImpl.getInstance("TITLETYPE");
	public static final Collection.Key UPDATEDDATE = KeyImpl.getInstance("UPDATEDDATE");
	public static final Collection.Key URI = KeyImpl.getInstance("URI");
	public static final Collection.Key XMLBASE = KeyImpl.getInstance("XMLBASE");
	public static final Collection.Key GUID = KeyConstants._guid;
	public static final Collection.Key ENCLOSURE = KeyImpl.getInstance("enclosure");
	public static final Collection.Key LINK = KeyConstants._link;
	public static final Collection.Key MODE = KeyConstants._mode;
	public static final Collection.Key TEXT = KeyConstants._text;
	public static final Collection.Key DOMAIN = KeyConstants._domain;
	public static final Collection.Key ISSUED = KeyImpl.getInstance("issued");
	public static final Collection.Key COPYRIGHT = KeyImpl.getInstance("copyright");
	public static final Collection.Key SRC = KeyConstants._src;
	public static final Collection.Key UPDATED = KeyConstants._updated;
	public static final Collection.Key MODIFIED = KeyImpl.getInstance("modified");
	public static final Collection.Key URL = KeyConstants._url;
	public static final Collection.Key LENGTH = KeyConstants._length;
	public static final Collection.Key ISPERMALINK = KeyImpl.getInstance("isPermaLink");

	public static final Collection.Key DC_CONTRIBUTOR = KeyImpl.getInstance("DC_CONTRIBUTOR");
	public static final Collection.Key DC_COVERAGE = KeyImpl.getInstance("DC_COVERAGE");
	public static final Collection.Key DC_CREATOR = KeyImpl.getInstance("DC_CREATOR");
	public static final Collection.Key DC_DATE = KeyImpl.getInstance("DC_DATE");
	public static final Collection.Key DC_DESCRIPTION = KeyImpl.getInstance("DC_DESCRIPTION");
	public static final Collection.Key DC_FORMAT = KeyImpl.getInstance("DC_FORMAT");
	public static final Collection.Key DC_IDENTIFIER = KeyImpl.getInstance("DC_IDENTIFIER");
	public static final Collection.Key DC_LANGUAGE = KeyImpl.getInstance("DC_LANGUAGE");
	public static final Collection.Key DC_PUBLISHER = KeyImpl.getInstance("DC_PUBLISHER");
	public static final Collection.Key DC_RELATION = KeyImpl.getInstance("DC_RELATION");
	public static final Collection.Key DC_RIGHT = KeyImpl.getInstance("DC_RIGHTS");
	public static final Collection.Key DC_SOURCE = KeyImpl.getInstance("DC_SOURCE");
	public static final Collection.Key DC_TITLE = KeyImpl.getInstance("DC_TITLE");
	public static final Collection.Key DC_TYPE = KeyImpl.getInstance("DC_TYPE");

	public static final Collection.Key DC_SUBJECT_TAXONOMYURI = KeyImpl.getInstance("DC_SUBJECT_TAXONOMYURI");
	public static final Collection.Key DC_SUBJECT_VALUE = KeyImpl.getInstance("DC_SUBJECT_VALUE");
	public static final Collection.Key DC_SUBJECT = KeyImpl.getInstance("DC_SUBJECT");

	private static Collection.Key[] COLUMNS = new Collection.Key[] { AUTHOREMAIL, AUTHORNAME, AUTHORURI, CATEGORYLABEL, CATEGORYSCHEME, CATEGORYTERM, COMMENTS, CONTENT,
			CONTENTMODE, CONTENTSRC, CONTENTTYPE, CONTRIBUTOREMAIL, CONTRIBUTORNAME, CONTRIBUTORURI, CREATEDDATE, EXPIRATIONDATE, ID, IDPERMALINK, LINKHREF, LINKHREFLANG,
			LINKLENGTH, LINKREL, LINKTITLE, LINKTYPE, PUBLISHEDDATE, RIGHTS, RSSLINK, SOURCE, SOURCEURL, SUMMARY, SUMMARYMODE, SUMMARYSRC, SUMMARYTYPE, TITLE, TITLETYPE,
			UPDATEDDATE, URI, XMLBASE };

	private static Collection.Key[] COLUMNS_WITH_DC = new Collection.Key[] { AUTHOREMAIL, AUTHORNAME, AUTHORURI, CATEGORYLABEL, CATEGORYSCHEME, CATEGORYTERM, COMMENTS, CONTENT,
			CONTENTMODE, CONTENTSRC, CONTENTTYPE, CONTRIBUTOREMAIL, CONTRIBUTORNAME, CONTRIBUTORURI, CREATEDDATE,

			DC_CONTRIBUTOR, DC_COVERAGE, DC_CREATOR, DC_DATE, DC_DESCRIPTION, DC_FORMAT, DC_IDENTIFIER, DC_LANGUAGE, DC_PUBLISHER, DC_RELATION, DC_RIGHT, DC_SOURCE, DC_TITLE,
			DC_TYPE, DC_SUBJECT_TAXONOMYURI, DC_SUBJECT_VALUE,

			EXPIRATIONDATE, ID, IDPERMALINK, LINKHREF, LINKHREFLANG, LINKLENGTH, LINKREL, LINKTITLE, LINKTYPE, PUBLISHEDDATE, RIGHTS, RSSLINK, SOURCE, SOURCEURL, SUMMARY,
			SUMMARYMODE, SUMMARYSRC, SUMMARYTYPE, TITLE, TITLETYPE, UPDATEDDATE, URI, XMLBASE };

	public static Query toQuery(Struct data, boolean hasDC) throws DatabaseException {
		Query qry = new QueryImpl(hasDC ? COLUMNS_WITH_DC : COLUMNS, 0, "");

		String version = Caster.toString(data.get(VERSION, ""), "");
		Array items = null;
		if (StringUtil.startsWithIgnoreCase(version, "rss") || StringUtil.startsWithIgnoreCase(version, "rdf")) {
			items = Caster.toArray(data.get(ITEM, null), null);
			if (items == null) {
				Struct sct = Caster.toStruct(data.get(version, null), null, false);
				if (sct != null) {
					items = Caster.toArray(sct.get(ITEM, null), null);
				}
			}
			return toQuery(true, qry, items);
		}
		else if (StringUtil.startsWithIgnoreCase(version, "atom")) {
			items = Caster.toArray(data.get(ENTRY, null), null);
			return toQuery(false, qry, items);
		}
		return qry;
	}

	private static Query toQuery(boolean isRss, Query qry, Array items) {
		if (items == null) return qry;

		int len = items.size();
		Struct item;
		int row = 0;
		Iterator<Entry<Key, Object>> it;
		Entry<Key, Object> e;
		for (int i = 1; i <= len; i++) {
			item = Caster.toStruct(items.get(i, null), null, false);
			if (item == null) continue;
			qry.addRow();
			row++;
			it = item.entryIterator();
			while (it.hasNext()) {
				e = it.next();
				if (isRss) setQueryValueRSS(qry, e.getKey(), e.getValue(), row);
				else setQueryValueAtom(qry, e.getKey(), e.getValue(), row);
			}

		}

		return qry;
	}

	private static void setQueryValueAtom(Query qry, Key key, Object value, int row) {

		if (key.equals(AUTHOR)) {
			Struct sct = toStruct(value);

			if (sct != null) {
				qry.setAtEL(AUTHOREMAIL, row, sct.get("email", null));
				qry.setAtEL(AUTHORNAME, row, sct.get("name", null));
				qry.setAtEL(AUTHORURI, row, sct.get("uri", null));
			}
		}
		if (key.equals(CATEGORY)) {
			Struct sct = toStruct(value);
			if (sct != null) {
				qry.setAtEL(CATEGORYLABEL, row, sct.get("label", null));
				qry.setAtEL(CATEGORYSCHEME, row, sct.get("scheme", null));
				qry.setAtEL(CATEGORYTERM, row, sct.get("term", null));
			}
			// else qry.setAtEL(CATEGORYLABEL, row, getValue(value));
		}
		else if (key.equals(COMMENTS)) {
			qry.setAtEL(COMMENTS, row, getValue(value));
		}
		else if (key.equals(CONTENT)) {
			Struct sct = toStruct(value);
			if (sct != null) {
				qry.setAtEL(CONTENT, row, getValue(sct));
				qry.setAtEL(CONTENTMODE, row, sct.get(MODE, null));
				qry.setAtEL(CONTENTSRC, row, sct.get(SRC, null));
				qry.setAtEL(CONTENTTYPE, row, sct.get(KeyConstants._type, null));
				qry.setAtEL(XMLBASE, row, sct.get("xml:base", null));
			}
			else qry.setAtEL(CONTENT, row, getValue(value));
		}
		else if (key.equals(CONTRIBUTOR)) {
			Struct sct = toStruct(value);
			if (sct != null) {
				qry.setAtEL(CONTRIBUTOREMAIL, row, sct.get("email", null));
				qry.setAtEL(CONTRIBUTORNAME, row, sct.get(KeyConstants._name, null));
				qry.setAtEL(CONTRIBUTORURI, row, sct.get("uri", null));
			}
		}
		else if (key.equals(CREATED)) {
			qry.setAtEL(CREATEDDATE, row, getValue(value));
		}
		else if (key.equals(ID)) {
			qry.setAtEL(ID, row, getValue(value));
		}
		else if (key.equals(LINK)) {
			Struct sct = toStruct(value);
			if (sct != null) {
				qry.setAtEL(LINKHREF, row, sct.get("href", null));
				qry.setAtEL(LINKHREFLANG, row, sct.get("hreflang", null));
				qry.setAtEL(LINKLENGTH, row, sct.get(LENGTH, null));
				qry.setAtEL(LINKREL, row, sct.get("rel", null));
				qry.setAtEL(LINKTITLE, row, sct.get(TITLE, null));
				qry.setAtEL(LINKTYPE, row, sct.get(KeyConstants._type, null));
			}
		}
		else if (key.equals(PUBLISHED)) {
			qry.setAtEL(PUBLISHEDDATE, row, getValue(value));
		}
		else if (key.equals(ISSUED)) {
			qry.setAtEL(PUBLISHEDDATE, row, getValue(value));
		}
		else if (key.equals(RIGHTS)) {
			qry.setAtEL(RIGHTS, row, getValue(value));
		}
		else if (key.equals(COPYRIGHT)) {
			qry.setAtEL(RIGHTS, row, getValue(value));
		}
		else if (key.equals(SUMMARY)) {
			Struct sct = toStruct(value);
			if (sct != null) {
				qry.setAtEL(SUMMARY, row, getValue(sct));
				qry.setAtEL(SUMMARYMODE, row, sct.get(MODE, null));
				qry.setAtEL(SUMMARYSRC, row, sct.get(SRC, null));
				qry.setAtEL(SUMMARYTYPE, row, sct.get(KeyConstants._type, null));
			}
			else qry.setAtEL(SUMMARY, row, getValue(value));
		}
		else if (key.equals(TITLE)) {
			Struct sct = toStruct(value);
			if (sct != null) {
				qry.setAtEL(TITLE, row, getValue(sct));
				qry.setAtEL(TITLETYPE, row, sct.get(KeyConstants._type, null));
			}
			else qry.setAtEL(TITLE, row, getValue(value));
		}
		else if (key.equals(UPDATED)) {
			qry.setAtEL(UPDATEDDATE, row, getValue(value));
		}
		else if (key.equals(MODIFIED)) {
			qry.setAtEL(UPDATEDDATE, row, getValue(value));
		}
	}

	private static void setQueryValueRSS(Query qry, Key key, Object value, int row) {

		if (key.equals(AUTHOR)) {
			qry.setAtEL(AUTHOREMAIL, row, getValue(value));
		}
		else if (key.equals(CATEGORY)) {
			Struct sct = toStruct(value);

			if (sct != null) {
				qry.setAtEL(CATEGORYLABEL, row, getValue(sct));
				qry.setAtEL(CATEGORYSCHEME, row, sct.get(DOMAIN, null));
			}
			else qry.setAtEL(CATEGORYLABEL, row, getValue(value));
		}
		else if (key.equals(COMMENTS)) {
			qry.setAtEL(COMMENTS, row, getValue(value));
		}
		else if (key.equals(KeyConstants._description)) {
			qry.setAtEL(CONTENT, row, getValue(value));
		}
		else if (key.equals(EXPIRATIONDATE)) {
			qry.setAtEL(EXPIRATIONDATE, row, getValue(value));
		}
		else if (key.equals(GUID)) {
			Struct sct = toStruct(value);

			if (sct != null) {
				qry.setAtEL(ID, row, getValue(sct));
				qry.setAtEL(IDPERMALINK, row, sct.get(ISPERMALINK, null));
			}
			else qry.setAtEL(ID, row, getValue(value));
		}
		else if (key.equals(ENCLOSURE)) {
			Struct sct = toStruct(value);
			if (sct != null) {
				qry.setAtEL(LINKHREF, row, sct.get(URL, null));
				qry.setAtEL(LINKLENGTH, row, sct.get(LENGTH, null));
				qry.setAtEL(LINKTYPE, row, sct.get(KeyConstants._type, null));
			}
		}
		else if (key.equals(PUBDATE)) {
			qry.setAtEL(PUBLISHEDDATE, row, getValue(value));
		}
		else if (key.equals(RDF_ABOUT)) {
			qry.setAtEL(URI, row, getValue(value));
		}
		else if (key.equals(LINK)) {

			Struct sct = toStruct(value);

			if (sct != null) {
				qry.setAtEL(RSSLINK, row, getValue(sct));
				Object v = sct.get(RDF_ABOUT, null);
				if (v != null) qry.setAtEL(URI, row, v);
			}
			else qry.setAtEL(RSSLINK, row, getValue(value));
		}
		else if (key.equals(SOURCE)) {
			Struct sct = toStruct(value);

			if (sct != null) {
				qry.setAtEL(SOURCE, row, getValue(sct));
				qry.setAtEL(SOURCEURL, row, sct.get(URL, null));
			}
			else qry.setAtEL(SOURCE, row, getValue(value));
		}
		else if (key.equals(SUMMARY)) {
			Struct sct = toStruct(value);

			if (sct != null) {
				qry.setAtEL(SUMMARY, row, getValue(sct));
				qry.setAtEL(SUMMARYMODE, row, sct.get(MODE, null));
				qry.setAtEL(SUMMARYTYPE, row, sct.get(KeyConstants._type, null));
			}
			else qry.setAtEL(SUMMARY, row, getValue(value));
		}
		else if (key.equals(TITLE)) {
			qry.setAtEL(TITLE, row, getValue(value));
		}

		// Dublin Core
		if (key.getLowerString().startsWith("dc_")) {

			if (key.equals(DC_CONTRIBUTOR)) {
				qry.setAtEL(DC_CONTRIBUTOR, row, getValue(value));
			}
			else if (key.equals(DC_COVERAGE)) {
				qry.setAtEL(DC_COVERAGE, row, getValue(value));
			}
			else if (key.equals(DC_CREATOR)) {
				qry.setAtEL(DC_CREATOR, row, getValue(value));
			}
			else if (key.equals(DC_DATE)) {
				qry.setAtEL(DC_DATE, row, getValue(value));
			}
			else if (key.equals(DC_DESCRIPTION)) {
				qry.setAtEL(DC_DESCRIPTION, row, getValue(value));
			}
			else if (key.equals(DC_FORMAT)) {
				qry.setAtEL(DC_FORMAT, row, getValue(value));
			}
			else if (key.equals(DC_IDENTIFIER)) {
				qry.setAtEL(DC_IDENTIFIER, row, getValue(value));
			}
			else if (key.equals(DC_LANGUAGE)) {
				qry.setAtEL(DC_LANGUAGE, row, getValue(value));
			}
			else if (key.equals(DC_PUBLISHER)) {
				qry.setAtEL(DC_PUBLISHER, row, getValue(value));
			}
			else if (key.equals(DC_RELATION)) {
				qry.setAtEL(DC_RELATION, row, getValue(value));
			}
			else if (key.equals(DC_RIGHT)) {
				qry.setAtEL(DC_RIGHT, row, getValue(value));
			}
			else if (key.equals(DC_SOURCE)) {
				qry.setAtEL(DC_SOURCE, row, getValue(value));
			}
			else if (key.equals(DC_SUBJECT_TAXONOMYURI)) {
				qry.setAtEL(DC_SUBJECT_TAXONOMYURI, row, getValue(value));
			}
			else if (key.equals(DC_SUBJECT)) {
				qry.setAtEL(DC_SUBJECT_VALUE, row, getValue(value));
			}
			else if (key.equals(DC_TITLE)) {
				qry.setAtEL(DC_TITLE, row, getValue(value));
			}
			else if (key.equals(DC_TYPE)) {
				qry.setAtEL(DC_TYPE, row, getValue(value));
			}
		}

	}

	public static Object getValue(Object value) {
		return getValue(value, false);
	}

	public static Object getValue(Object value, boolean includeChildren) {
		if (value instanceof Struct) return getValue((Struct) value, includeChildren);
		return Caster.toString(value, null);
	}

	public static Object getValue(Struct sct, boolean includeChildren) {
		Object obj = sct.get(KeyConstants._value, null);
		if (obj == null) obj = sct.get(TEXT, null);
		return obj;
	}

	private static Struct toStruct(Object value) {
		if (value instanceof Struct) return (Struct) value;

		if (value instanceof Array) {
			Struct sct = new StructImpl(), row;
			Array arr = (Array) value;
			int len = arr.size();
			// Key[] keys;
			Iterator<Entry<Key, Object>> it;
			Entry<Key, Object> e;
			String nw;
			Object ext;
			for (int i = 1; i <= len; i++) {
				row = Caster.toStruct(arr.get(i, null), null, false);
				if (row == null) continue;
				it = row.entryIterator();
				// keys = row.keys();
				while (it.hasNext()) {
					e = it.next();
					ext = sct.get(e.getKey(), null);
					nw = Caster.toString(e.getValue(), null);
					if (nw != null) {
						if (ext == null) sct.setEL(e.getKey(), nw);
						else if (ext instanceof CastableArray) {
							((CastableArray) ext).appendEL(nw);
						}
						else {
							CastableArray ca = new CastableArray();
							ca.appendEL(Caster.toString(ext, null));
							ca.appendEL(nw);
							sct.setEL(e.getKey(), ca);
						}

					}
				}
			}
			return sct;
		}

		return null;
	}

	public static Query toQuery(Query qry) {
		return qry;
	}
}
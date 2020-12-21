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
package lucee.runtime.tag;

import java.util.Iterator;
import java.util.Map;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.search.AddionalAttrs;
import lucee.runtime.search.SearchCollection;
import lucee.runtime.search.SearchData;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.search.SearchException;
import lucee.runtime.search.SuggestionItem;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public final class Search extends TagImpl {

	private static final String[] EMPTY = new String[0];

	private static final int SUGGESTIONS_ALWAYS = Integer.MAX_VALUE;
	private static final int SUGGESTIONS_NEVER = -1;

	private static final lucee.runtime.type.Collection.Key FOUND = KeyImpl.getInstance("found");
	private static final lucee.runtime.type.Collection.Key SEARCHED = KeyImpl.getInstance("searched");
	private static final lucee.runtime.type.Collection.Key KEYWORDS = KeyImpl.getInstance("keywords");
	private static final lucee.runtime.type.Collection.Key KEYWORD_SCORE = KeyImpl.getInstance("keywordScore");

	/** Specifies the criteria type for the search. */
	private short type = SearchCollection.SEARCH_TYPE_SIMPLE;

	/** Specifies the maximum number of entries for index queries. If omitted, all rows are returned. */
	private int maxrows = -1;

	/** Specifies the criteria for the search following the syntactic rules specified by type. */
	private String criteria = "";

	/** Specifies the first row number to be retrieved. Default is 1. */
	private int startrow = 1;

	/**
	 * The logical collection name that is the target of the search operation or an external collection
	 ** with fully qualified path.
	 */
	private SearchCollection[] collections;

	/** A name for the search query. */
	private String name;

	private String[] category = EMPTY;
	private String categoryTree = "";
	private String status;
	private int suggestions = SUGGESTIONS_NEVER;
	private int contextPassages = 0;
	private int contextBytes = 300;
	private String contextHighlightBegin = "<b>";
	private String contextHighlightEnd = "</b>";
	private String previousCriteria;

	// private int spellCheckMaxLevel=10;
	// private String result=null;

	@Override
	public void release() {
		super.release();
		type = SearchCollection.SEARCH_TYPE_SIMPLE;
		maxrows = -1;
		criteria = "";
		startrow = 1;
		collections = null;

		category = EMPTY;
		categoryTree = "";
		status = null;
		suggestions = SUGGESTIONS_NEVER;
		contextPassages = 0;
		contextBytes = 300;
		contextHighlightBegin = "<b>";
		contextHighlightEnd = "</b>";
		previousCriteria = null;

		// spellCheckMaxLevel=10;
		// result=null;

	}

	/**
	 * set the value type Specifies the criteria type for the search.
	 * 
	 * @param type value to set
	 * @throws ApplicationException
	 **/
	public void setType(String type) throws ApplicationException {
		if (type == null) return;
		type = type.toLowerCase().trim();
		if (type.equals("simple")) this.type = SearchCollection.SEARCH_TYPE_SIMPLE;
		else if (type.equals("explicit")) this.type = SearchCollection.SEARCH_TYPE_EXPLICIT;
		else throw new ApplicationException("attribute type of tag search has an invalid value, valid values are [simple,explicit] now is [" + type + "]");

	}

	/**
	 * set the value maxrows Specifies the maximum number of entries for index queries. If omitted, all
	 * rows are returned.
	 * 
	 * @param maxrows value to set
	 **/
	public void setMaxrows(double maxrows) {
		this.maxrows = (int) maxrows;
	}

	/**
	 * set the value criteria Specifies the criteria for the search following the syntactic rules
	 * specified by type.
	 * 
	 * @param criteria value to set
	 **/
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	/**
	 * set the value startrow Specifies the first row number to be retrieved. Default is 1.
	 * 
	 * @param startrow value to set
	 **/
	public void setStartrow(double startrow) {
		this.startrow = (int) startrow;
	}

	/**
	 * set the value collection The logical collection name that is the target of the search operation
	 * or an external collection with fully qualified path.
	 * 
	 * @param collection value to set
	 * @throws PageException
	 **/
	public void setCollection(String collection) throws PageException {
		String[] collNames = ListUtil.toStringArrayTrim(ListUtil.listToArrayRemoveEmpty(collection, ','));
		collections = new SearchCollection[collNames.length];
		SearchEngine se = pageContext.getConfig().getSearchEngine(pageContext);
		try {
			for (int i = 0; i < collections.length; i++) {
				collections[i] = se.getCollectionByName(collNames[i]);
			}
		}
		catch (SearchException e) {
			collections = null;
			throw Caster.toPageException(e);
		}
	}

	/**
	 * set the value language
	 * 
	 * @param language value to set
	 **/
	public void setLanguage(String language) {
		// DeprecatedUtil.tagAttribute(pageContext,"Search", "language");
	}

	/**
	 * set the value external
	 * 
	 * @param external value to set
	 * @throws ApplicationException
	 **/
	public void setExternal(boolean external) throws ApplicationException {
		// DeprecatedUtil.tagAttribute(pageContext,"Search", "external");
	}

	/**
	 * set the value name A name for the search query.
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param category the category to set
	 * @throws ApplicationException
	 */
	public void setCategory(String listCategories) {
		if (StringUtil.isEmpty(listCategories)) return;
		this.category = ListUtil.trimItems(ListUtil.listToStringArray(listCategories, ','));
	}

	/**
	 * @param categoryTree the categoryTree to set
	 * @throws ApplicationException
	 */
	public void setCategorytree(String categoryTree) {
		if (StringUtil.isEmpty(categoryTree)) return;
		categoryTree = categoryTree.replace('\\', '/').trim();
		if (StringUtil.startsWith(categoryTree, '/')) categoryTree = categoryTree.substring(1);
		if (!StringUtil.endsWith(categoryTree, '/') && categoryTree.length() > 0) categoryTree += "/";
		this.categoryTree = categoryTree;
	}

	/**
	 * @param contextBytes the contextBytes to set
	 * @throws ApplicationException
	 */
	public void setContextbytes(double contextBytes) throws ApplicationException {
		this.contextBytes = (int) contextBytes;
	}

	/**
	 * @param contextHighlightBegin the contextHighlightBegin to set
	 * @throws ApplicationException
	 */
	public void setContexthighlightbegin(String contextHighlightBegin) {
		this.contextHighlightBegin = contextHighlightBegin;
	}

	/**
	 * @param contextHighlightEnd the contextHighlightEnd to set
	 * @throws ApplicationException
	 */
	public void setContexthighlightend(String contextHighlightEnd) {
		this.contextHighlightEnd = contextHighlightEnd;
	}

	/**
	 * @param contextPassages the contextPassages to set
	 * @throws ApplicationException
	 */
	public void setContextpassages(double contextPassages) throws ApplicationException {
		this.contextPassages = (int) contextPassages;
	}

	/**
	 * @param previousCriteria the previousCriteria to set
	 * @throws ApplicationException
	 */
	public void setPreviouscriteria(String previousCriteria) throws ApplicationException {
		this.previousCriteria = previousCriteria;
		throw new ApplicationException("attribute previousCriteria for tag search is not supported yet");
		// TODO impl tag attribute
	}

	/**
	 * @param status the status to set
	 * @throws ApplicationException
	 */
	public void setStatus(String status) {
		if (!StringUtil.isEmpty(status)) this.status = status;
	}

	/**
	 * @param suggestions the suggestions to set
	 * @throws ApplicationException
	 */
	public void setSuggestions(String suggestions) throws PageException {
		if (StringUtil.isEmpty(suggestions)) return;
		suggestions = suggestions.trim().toLowerCase();
		if ("always".equals(suggestions)) this.suggestions = SUGGESTIONS_ALWAYS;
		else if ("never".equals(suggestions)) this.suggestions = SUGGESTIONS_NEVER;
		else if (Decision.isNumber(suggestions)) {
			this.suggestions = Caster.toIntValue(suggestions);
		}
		else throw new ApplicationException("attribute suggestions has an invalid value [" + suggestions + "], valid values are [always,never,<positive numeric value>]");

	}

	@Override
	public int doStartTag() throws PageException {
		// SerialNumber sn = pageContext.getConfig().getSerialNumber();
		// if(sn.getVersion()==SerialNumber.VERSION_COMMUNITY)
		// throw new SecurityException("no access to this functionality with the "+sn.getStringVersion()+"
		// version of Lucee");
		final String v = "VARCHAR", d = "DOUBLE";
		String[] cols = new String[] { "title", "url", "summary", "score", "recordssearched", "key", "custom1", "custom2", "custom3", "custom4", "categoryTree", "category",
				"context", "size", "rank", "author", "type", "collection" };

		// TODO support context
		String[] types = new String[] { v, v, v, d, d, v, v, v, v, v, v, v, v, d, d, v, v, v };
		SearchData data = pageContext.getConfig().getSearchEngine(pageContext).createSearchData(suggestions);
		SuggestionItem item = null;// this is already here to make sure the classloader load this sinstance

		lucee.runtime.type.Query qry = new QueryImpl(cols, types, 0, "query");

		SearchCollection collection;
		long time = System.currentTimeMillis();
		AddionalAttrs.setAddionalAttrs(contextBytes, contextPassages, contextHighlightBegin, contextHighlightEnd);
		try {
			for (int i = 0; i < collections.length; i++) {
				collection = collections[i];
				startrow = collection.search(data, qry, criteria, collection.getLanguage(), type, startrow, maxrows, categoryTree, category);

				if (maxrows >= 0 && qry.getRecordcount() >= maxrows) break;
			}
			pageContext.setVariable(name, qry);
		}
		catch (SearchException se) {
			throw Caster.toPageException(se);
		}
		finally {
			AddionalAttrs.removeAddionalAttrs();
		}

		time = System.currentTimeMillis() - time;
		Double recSearched = new Double(data.getRecordsSearched());
		int len = qry.getRecordcount();
		for (int i = 1; i <= len; i++) {
			qry.setAt("recordssearched", i, recSearched);
		}

		// status
		if (status != null) {
			Struct sct = new StructImpl();
			pageContext.setVariable(status, sct);
			sct.set(FOUND, new Double(qry.getRecordcount()));
			sct.set(SEARCHED, recSearched);
			sct.set(KeyConstants._time, new Double(time));

			// TODO impl this values

			Map s = data.getSuggestion();
			if (s.size() > 0) {
				String key;

				Iterator it = s.keySet().iterator();
				Struct keywords = new StructImpl();
				Struct keywordScore = new StructImpl();
				sct.set(KEYWORDS, keywords);
				sct.set(KEYWORD_SCORE, keywordScore);
				Object obj;

				while (it.hasNext()) {
					key = (String) it.next();

					// the problem is a conflict between the SuggestionItem version from core and extension
					obj = s.get(key);
					if (obj instanceof SuggestionItem) {
						item = (SuggestionItem) obj;
						keywords.set(key, item.getKeywords());
						keywordScore.set(key, item.getKeywordScore());
					}
					else {
						Class clazz = obj.getClass();
						try {
							keywords.set(key, clazz.getMethod("getKeywords", new Class[0]).invoke(obj, new Object[0]));
							keywordScore.set(key, clazz.getMethod("getKeywordScore", new Class[0]).invoke(obj, new Object[0]));
						}
						catch (Exception e) {}
					}

				}

				String query = data.getSuggestionQuery();
				if (query != null) {
					String html = StringUtil.replace(query, "<suggestion>", "<b>", false);
					html = StringUtil.replace(html, "</suggestion>", "</b>", false);
					sct.set("suggestedQueryHTML", html);

					String plain = StringUtil.replace(query, "<suggestion>", "", false);
					plain = StringUtil.replace(plain, "</suggestion>", "", false);
					sct.set("suggestedQuery", plain);
				}

			}

			// if(suggestions!=SUGGESTIONS_NEVER)sct.set("suggestedQuery", "");
			// sct.set("keywords", "");
			// sct.set("keywordScore", "");

		}

		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

}
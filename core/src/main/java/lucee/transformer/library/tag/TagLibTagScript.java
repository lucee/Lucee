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
package lucee.transformer.library.tag;

import java.util.Iterator;

import lucee.commons.lang.StringUtil;

public final class TagLibTagScript {

	public static final short TYPE_NONE = 0;
	public static final short TYPE_SINGLE = 1;
	public static final short TYPE_MULTIPLE = 2;

	public static final short CTX_OTHER = -1;
	public static final short CTX_NONE = 0;
	public static final short CTX_IF = 1;
	public static final short CTX_ELSE_IF = 2;
	public static final short CTX_ELSE = 3;
	public static final short CTX_FOR = 4;
	public static final short CTX_WHILE = 5;
	public static final short CTX_DO_WHILE = 6;
	public static final short CTX_CFC = 7;
	public static final short CTX_INTERFACE = 8;
	public static final short CTX_FUNCTION = 9;
	public static final short CTX_BLOCK = 10;
	public static final short CTX_FINALLY = 11;
	public static final short CTX_SWITCH = 12;
	public static final short CTX_TRY = 13;
	public static final short CTX_CATCH = 14;
	public static final short CTX_TRANSACTION = 15;
	public static final short CTX_THREAD = 16;
	public static final short CTX_SAVECONTENT = 17;
	public static final short CTX_LOCK = 18;
	public static final short CTX_LOOP = 19;
	public static final short CTX_QUERY = 20;
	public static final short CTX_ZIP = 21;
	public static final short CTX_STATIC = 22;

	private final static TagLibTagAttr UNDEFINED = new TagLibTagAttr(null);

	private TagLibTag tag;
	private boolean rtexpr;
	private short type = TYPE_NONE;
	private TagLibTagAttr singleAttr = UNDEFINED;
	private short context = CTX_OTHER;

	public TagLibTagScript(TagLibTag tag) {
		this.tag = tag;
	}

	public void setType(short type) {
		this.type = type;
	}

	public void setRtexpr(boolean rtexpr) {
		this.rtexpr = rtexpr;
	}

	/**
	 * @return the tag
	 */
	public TagLibTag getTag() {
		return tag;
	}

	/**
	 * @return the rtexpr
	 */
	public boolean getRtexpr() {
		return rtexpr;
	}

	/**
	 * @return the type
	 */
	public short getType() {
		return type;
	}

	public TagLibTagAttr getSingleAttr() {
		if (singleAttr == UNDEFINED) {
			singleAttr = null;
			Iterator<TagLibTagAttr> it = tag.getAttributes().values().iterator();
			TagLibTagAttr attr;
			while (it.hasNext()) {
				attr = it.next();
				if (attr.getScriptSupport() != TagLibTagAttr.SCRIPT_SUPPORT_NONE) {
					singleAttr = attr;
					break;
				}
			}
		}
		return singleAttr;
	}

	public void setContext(String str) {
		if (!StringUtil.isEmpty(str, true)) {
			str = str.trim().toLowerCase();
			if ("none".equals(str)) this.context = CTX_NONE;
			else if ("if".equals(str)) this.context = CTX_IF;
			else if ("elseif".equals(str)) this.context = CTX_ELSE_IF;
			else if ("else".equals(str)) this.context = CTX_ELSE;
			else if ("for".equals(str)) this.context = CTX_FOR;
			else if ("while".equals(str)) this.context = CTX_WHILE;
			else if ("dowhile".equals(str)) this.context = CTX_DO_WHILE;
			else if ("cfc".equals(str)) this.context = CTX_CFC;
			else if ("component".equals(str)) this.context = CTX_CFC;
			else if ("class".equals(str)) this.context = CTX_CFC;
			else if ("interface".equals(str)) this.context = CTX_INTERFACE;
			else if ("function".equals(str)) this.context = CTX_FUNCTION;
			else if ("block".equals(str)) this.context = CTX_BLOCK;
			else if ("finally".equals(str)) this.context = CTX_FINALLY;
			else if ("switch".equals(str)) this.context = CTX_SWITCH;
			else if ("try".equals(str)) this.context = CTX_TRY;
			else if ("catch".equals(str)) this.context = CTX_CATCH;
			else if ("transaction".equals(str)) this.context = CTX_TRANSACTION;
			else if ("thread".equals(str)) this.context = CTX_THREAD;
			else if ("savecontent".equals(str)) this.context = CTX_SAVECONTENT;
			else if ("lock".equals(str)) this.context = CTX_LOCK;
			else if ("loop".equals(str)) this.context = CTX_LOOP;
			else if ("query".equals(str)) this.context = CTX_QUERY;
			else if ("zip".equals(str)) this.context = CTX_ZIP;
		}
	}

	/**
	 * @return the context
	 */
	public short getContext() {
		return context;
	}

	public static short toType(String type, short defaultValue) {
		if (!StringUtil.isEmpty(type, true)) {
			type = type.trim().toLowerCase();
			if ("single".equals(type)) return TYPE_SINGLE;
			else if ("multiple".equals(type)) return TYPE_MULTIPLE;
			else if ("none".equals(type)) return TYPE_NONE;
		}
		return defaultValue;
	}

	public static String toType(short type, String defaultValue) {
		if (type == TYPE_MULTIPLE) return "multiple";
		if (type == TYPE_SINGLE) return "single";
		if (type == TYPE_NONE) return "none";
		return defaultValue;
	}

}
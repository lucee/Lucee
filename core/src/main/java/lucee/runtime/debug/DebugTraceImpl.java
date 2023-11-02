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
package lucee.runtime.debug;

import lucee.commons.lang.StringUtil;

public final class DebugTraceImpl implements DebugTrace {

	private static final long serialVersionUID = -3619310656845433643L;

	private int type;
	private String category;
	private String text;
	private String template;
	private int line;
	private String varValue;
	private long time;
	private String varName;
	private String action;

	public DebugTraceImpl(int type, String category, String text, String template, int line, String action, String varName, String varValue, long time) {
		this.type = type;
		this.category = category;
		this.text = text;
		this.template = template;
		this.line = line;
		this.varName = varName;
		this.varValue = varValue;
		this.time = (time < 0) ? 0 : time;
		this.action = StringUtil.emptyIfNull(action);
	}

	/**
	 * @return the category
	 */
	@Override
	public String getCategory() {
		return category;
	}

	/**
	 * @return the line
	 */
	@Override
	public int getLine() {
		return line;
	}

	/**
	 * @return the template
	 */
	@Override
	public String getTemplate() {
		return template;
	}

	/**
	 * @return the text
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * @return the time
	 */
	@Override
	public long getTime() {
		return time;
	}

	/**
	 * @return the type
	 */
	@Override
	public int getType() {
		return type;
	}

	/**
	 * @return the var value
	 */
	@Override
	public String getVarValue() {
		return varValue;
	}

	@Override
	public String getVarName() {
		return varName;
	}

	@Override
	public String getAction() {
		return action;
	}

	public static int toType(String type, int defaultValue) {
		if (type == null) return defaultValue;
		type = type.toLowerCase().trim();
		if (type.startsWith("info")) return TYPE_INFO;
		if (type.startsWith("debug")) return TYPE_DEBUG;
		if (type.startsWith("warn")) return TYPE_WARN;
		if (type.startsWith("error")) return TYPE_ERROR;
		if (type.startsWith("fatal")) return TYPE_FATAL;
		if (type.startsWith("trace")) return TYPE_TRACE;

		return defaultValue;
	}

	public static String toType(int type, String defaultValue) {
		switch (type) {
		case TYPE_INFO:
			return "INFO";
		case TYPE_DEBUG:
			return "DEBUG";
		case TYPE_WARN:
			return "WARN";
		case TYPE_ERROR:
			return "ERROR";
		case TYPE_FATAL:
			return "FATAL";
		case TYPE_TRACE:
			return "TRACE";
		default:
			return defaultValue;
		}
	}

}
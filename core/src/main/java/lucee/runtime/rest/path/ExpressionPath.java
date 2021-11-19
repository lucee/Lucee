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
package lucee.runtime.rest.path;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lucee.runtime.type.Struct;

public class ExpressionPath extends Path {

	private Pattern pattern;
	private List<String> variables;

	public ExpressionPath(Pattern pattern, List<String> variables) {
		this.pattern = pattern;
		this.variables = variables;
	}

	public static Path getInstance(String path) {
		/*
		 * TODO handle if a pattern already has defined a group
		 */

		int last = -1, startIndex, endIndex = 0, index;
		String content, variableName, regexPart;
		StringBuilder regex = new StringBuilder();
		List<String> variables = new ArrayList<String>();
		while ((startIndex = path.indexOf('{', last)) != -1) {
			if (last + 1 < startIndex) {
				delimiter(variables, regex, path.substring(last + 1, startIndex));
			}

			endIndex = path.indexOf('}', startIndex + 1);
			if (endIndex == -1) return new LiteralPath(path);

			content = path.substring(startIndex + 1, endIndex);
			index = content.indexOf(':');
			if (index != -1) {
				variableName = content.substring(0, index).trim();
				regexPart = content.substring(index + 1).trim();
			}
			else {
				variableName = content.trim();
				regexPart = ".+";
			}
			regex.append('(');
			regex.append(regexPart);
			regex.append(')');
			variables.add(variableName);
			// print.e(variableName);
			// print.e(regexPart);
			last = endIndex;
		}

		if (endIndex + 1 < path.length()) delimiter(variables, regex, path.substring(endIndex + 1));

		// regex.append("(.*)");

		Pattern pattern = Pattern.compile(regex.toString());
		// print.e(regex);
		// print.e(variables);
		return new ExpressionPath(pattern, variables);
	}

	private static void delimiter(List<String> variables, StringBuilder regex, String delimiter) {
		variables.add(null);
		regex.append('(');
		/*
		 * print.e(delimiter+":"+Pattern.quote(delimiter)); StringBuilder sb=new StringBuilder(); int
		 * len=delimiter.length(); char c; for (int i=0; i<len; i++) { c=delimiter.charAt(i); switch(c){
		 * case '.': sb.append("\\.");break; case '?': sb.append("\\?");break; case '\\':
		 * sb.append("\\\\");break; case '^': sb.append("\\^");break; case '$': sb.append("\\$");break; case
		 * '+': sb.append("\\+");break; default: sb.append(c); break; } }
		 */

		regex.append(Pattern.quote(delimiter));
		regex.append(')');
	}

	@Override
	public boolean match(Struct result, String path) {
		String var;
		Matcher m = pattern.matcher(path);
		boolean hasMatches = m.find();
		if (!hasMatches) return false;

		if (hasMatches) {
			// Get all groups for this match
			int len = m.groupCount();
			for (int i = 1; i <= len; i++) {
				String groupStr = m.group(i);
				var = variables.get(i - 1);
				if (var != null) result.setEL(var, groupStr.trim());
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "expression:" + pattern.pattern();
	}
}
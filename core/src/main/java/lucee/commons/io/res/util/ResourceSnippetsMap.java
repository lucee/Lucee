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
package lucee.commons.io.res.util;

import java.util.Map;

import lucee.commons.collection.LinkedHashMapMaxSize;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.SerializableObject;
import lucee.runtime.PageSource;

public class ResourceSnippetsMap {

	/* methods that access these Map objects should take care of synchronization */
	private final Map<String, String> sources;
	private final Map<String, ResourceSnippet> snippets;
	private final Object sync = new SerializableObject();

	public ResourceSnippetsMap(int maxSnippets, int maxSources) {

		sources = new LinkedHashMapMaxSize<String, String>(maxSources);
		snippets = new LinkedHashMapMaxSize<String, ResourceSnippet>(maxSnippets);
	}

	/**
	 * this method accesses the underlying Map(s) and is therefore synchronized
	 *
	 * @param ps
	 * @param startPos
	 * @param endPos
	 * @param charset
	 * @return
	 */
	public ResourceSnippet getSnippet(PageSource ps, int startPos, int endPos, String charset) {
		String keySnp = calcKey(ps, startPos, endPos);
		synchronized (sync) {
			ResourceSnippet snippet = snippets.get(keySnp);
			if (snippet == null) {
				Resource res = ps.getResource();
				String keyRes = calcKey(res);
				String src = sources.get(keyRes);
				if (src == null) {
					src = ResourceSnippet.getContents(res, charset);
					sources.put(keyRes, src);
				}
				snippet = ResourceSnippet.createResourceSnippet(src, startPos, endPos);
				snippets.put(keySnp, snippet);
			}
			return snippet;
		}
	}

	public static String calcKey(Resource res) {

		return res.getAbsolutePath() + "@" + res.lastModified();
	}

	public static String calcKey(PageSource ps, int startPos, int endPos) {
		return ps.getDisplayPath() + "@" + ps.getLastAccessTime() + ":" + startPos + "-" + endPos;
	}
}
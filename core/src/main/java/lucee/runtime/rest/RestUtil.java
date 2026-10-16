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
package lucee.runtime.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletResponse;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.HTMLEntities;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.rest.path.LiteralPath;
import lucee.runtime.rest.path.Path;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ListUtil;

public final class RestUtil {

	public static String[] splitPath(String path) {
		return ListUtil.listToStringArray(path, '/');
	}

	/**
	 * check if caller path match the cfc path
	 *
	 * @param variables
	 * @param restPath
	 * @param callerPath
	 * @return match until which index of the given cfc path, returns -1 if there is no match
	 */
	public static int matchPath(Struct variables, Path[] restPath, String[] callerPath) {
		if (restPath.length > callerPath.length) return -1;

		int index = 0;
		for (; index < restPath.length; index++) {
			if (!restPath[index].match(variables, callerPath[index])) return -1;
		}
		return index - 1;
	}

	/**
	 * Specificity score for a CFC restpath. Per segment: literal=4,
	 * regex-constrained path-var (e.g. {id:[0-9]+})=2, unconstrained
	 * path-var (e.g. {id})=1. Higher is more specific.
	 *
	 * Mirrors JAX-RS specificity ordering — literal beats regex beats
	 * free var. The 4/2/1 spacing keeps a longer path of less-specific
	 * segments from outranking a shorter literal-heavy path: e.g.
	 * /a/{b} (4+1=5) still beats /{a} (1) for callers like /a/x.
	 */
	public static int score(Path[] restPath) {
		int s = 0;
		for (int i = 0; i < restPath.length; i++) {
			Path p = restPath[i];
			if (p instanceof LiteralPath) s += 4;
			else if (p.isConstrained()) s += 2;
			else s += 1;
		}
		return s;
	}

	/**
	 * Pick the most-specific Source whose restpath matches the caller path.
	 * Ties on score are broken deterministically by raw restpath then by
	 * the source page path, so the result never depends on filesystem order.
	 *
	 * @param sources candidate Sources from a REST mapping
	 * @param callerPath caller path already split via splitPath
	 * @return SourceMatch for the winner, or null if none match
	 */
	public static SourceMatch resolve(List<Source> sources, String[] callerPath) {
		if (sources == null || sources.isEmpty()) return null;

		int bestScore = Integer.MIN_VALUE;
		int bestMatchedIndex = -1;
		Struct bestVariables = null;
		Source best = null;

		for (int i = 0; i < sources.size(); i++) {
			Source src = sources.get(i);
			Struct vars = new StructImpl();
			int idx = matchPath(vars, src.getPath(), callerPath);
			if (idx == -1) continue;
			int sc = score(src.getPath());
			if (best == null || sc > bestScore || (sc == bestScore && tieBreak(src, best) < 0)) {
				bestScore = sc;
				bestMatchedIndex = idx;
				bestVariables = vars;
				best = src;
			}
		}

		if (best == null) return null;
		return new SourceMatch(best, bestMatchedIndex, bestVariables);
	}

	private static int tieBreak(Source a, Source b) {
		int cmp = a.getRawPath().compareTo(b.getRawPath());
		if (cmp != 0) return cmp;
		return a.getPageSource().getDisplayPath().compareTo(b.getPageSource().getDisplayPath());
	}

	/**
	 * Return the restpath strings that appear more than once in the given list,
	 * preserving first-seen order. Used by {@link Mapping#init} to fail
	 * registration loudly when two CFCs claim the same restpath
	 * (LDEV-6306 case 4 — silent shadowing was filesystem-order-dependent).
	 */
	public static List<String> findDuplicates(String[] patterns) {
		List<String> dupes = new ArrayList<String>();
		if (patterns == null || patterns.length < 2) return dupes;
		Set<String> seen = new LinkedHashSet<String>();
		Set<String> reported = new LinkedHashSet<String>();
		for (int i = 0; i < patterns.length; i++) {
			String p = patterns[i];
			if (!seen.add(p) && reported.add(p)) {
				dupes.add(p);
			}
		}
		return dupes;
	}

	/**
	 * Test helper: given an array of restpath patterns and a caller path,
	 * return the index of the most-specific matching pattern, or -1.
	 *
	 * <p><b>NOT A PUBLIC API.</b> This exists so the LDEV-6306 routing logic
	 * can be exercised from CFML tests without a webserver. Production code
	 * must go through {@link #resolve(java.util.List, String[])}, which
	 * shares the same scoring core but operates on real {@link Source}s.
	 * Behaviour and signature of this method may change without notice.
	 */
	public static int resolveIndex(String[] patterns, String callerPath) {
		if (patterns == null || patterns.length == 0) return -1;
		String[] arrPath = splitPath(callerPath);
		int bestIndex = -1;
		int bestScore = Integer.MIN_VALUE;
		String bestRawPath = null;
		for (int i = 0; i < patterns.length; i++) {
			Path[] p = Path.init(patterns[i]);
			Struct vars = new StructImpl();
			int idx = matchPath(vars, p, arrPath);
			if (idx == -1) continue;
			int sc = score(p);
			if (bestRawPath == null || sc > bestScore || (sc == bestScore && patterns[i].compareTo(bestRawPath) < 0)) {
				bestIndex = i;
				bestScore = sc;
				bestRawPath = patterns[i];
			}
		}
		return bestIndex;
	}

	/**
	 * clears the PageContext output buffer andsets the REST response's status code and message
	 *
	 * @param pc
	 * @param status
	 * @param msg
	 */
	public static void setStatus(PageContext pc, int status, String msg, boolean htmlEscapeMessage) {
		pc.clear();
		if (!StringUtil.isEmpty(msg)) {
			try {
				pc.forceWrite(htmlEscapeMessage ? HTMLEntities.escapeHTML(msg) : msg);
			}
			catch (IOException e) {
			}
		}
		HttpServletResponse rsp = pc.getHttpServletResponse();
		rsp.setHeader("Connection", "close"); // IE unter IIS6, Win2K3 und Resin
		rsp.setStatus(status);
	}

	public static void release(Mapping[] mappings) {
		for (int i = 0; i < mappings.length; i++) {
			mappings[i].release();
		}
	}

	public static boolean isMatch(PageContext pc, Mapping mapping, Resource res) {
		Resource p = mapping.getPhysical();
		if (p != null) {
			return p.equals(res);
		}
		return ResourceUtil.toResourceNotExisting(pc, mapping.getStrPhysical()).equals(res);
	}

}
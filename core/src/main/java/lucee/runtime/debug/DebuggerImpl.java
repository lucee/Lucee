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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import lucee.print;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.commons.io.res.util.ResourceSnippet;
import lucee.commons.io.res.util.ResourceSnippetsMap;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.db.SQL;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.CatchBlock;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageExceptionImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.DebugQueryColumn;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.query.QueryResult;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

/**
 * Class to debug the application
 */
public final class DebuggerImpl implements Debugger {
	private static final long serialVersionUID = 3957043879267494311L;

	private static final Collection.Key IMPLICIT_ACCESS = KeyImpl.intern("implicitAccess");
	private static final Collection.Key GENERIC_DATA = KeyImpl.intern("genericData");
	private static final Collection.Key PAGE_PARTS = KeyImpl.intern("pageParts");
	// private static final Collection.Key OUTPUT_LOG= KeyImpl.intern("outputLog");

	private static final int MAX_PARTS = 100;

	private final Map<String, DebugEntryTemplateImpl> entries = new HashMap<String, DebugEntryTemplateImpl>();
	private Map<String, DebugEntryTemplatePartImpl> partEntries;
	private ResourceSnippetsMap snippetsMap = new ResourceSnippetsMap(1024, 128);

	private final List<QueryEntry> queries = new ArrayList<QueryEntry>();
	private final List<DebugTimerImpl> timers = new ArrayList<DebugTimerImpl>();
	private final List<DebugTraceImpl> traces = new ArrayList<DebugTraceImpl>();
	private final List<DebugDump> dumps = new ArrayList<DebugDump>();
	private final List<CatchBlock> exceptions = new ArrayList<CatchBlock>();
	private final Map<String, ImplicitAccessImpl> implicitAccesses = new HashMap<String, ImplicitAccessImpl>();

	private boolean output = true;
	private long lastEntry;
	private long lastTrace;
	private final Array historyId = new ArrayImpl();
	private final Array historyLevel = new ArrayImpl();

	private long starttime = System.currentTimeMillis();

	private DebugOutputLog outputLog;

	private Map<String, Map<String, List<String>>> genericData;

	private TemplateLine abort;

	private ApplicationException outputContext;

	private long queryTime = 0;

	final static Comparator DEBUG_ENTRY_TEMPLATE_COMPARATOR = new DebugEntryTemplateComparator();
	final static Comparator DEBUG_ENTRY_TEMPLATE_PART_COMPARATOR = new DebugEntryTemplatePartComparator();

	private static final Key CACHE_TYPE = KeyImpl.init("cacheType");

	private static final Key[] PAGE_COLUMNS = new Collection.Key[] { KeyConstants._id, KeyConstants._count, KeyConstants._min, KeyConstants._max, KeyConstants._avg,
			KeyConstants._app, KeyConstants._load, KeyConstants._query, KeyConstants._total, KeyConstants._src };
	private static final Key[] QUERY_COLUMNS = new Collection.Key[] { KeyConstants._name, KeyConstants._time, KeyConstants._sql, KeyConstants._src, KeyConstants._line,
			KeyConstants._count, KeyConstants._datasource, KeyConstants._usage, CACHE_TYPE };
	private static final String[] QUERY_COLUMN_TYPES = new String[] { "VARCHAR", "DOUBLE", "VARCHAR", "VARCHAR", "DOUBLE", "DOUBLE", "VARCHAR", "ANY", "VARCHAR" };
	private static final Key[] GEN_DATA_COLUMNS = new Collection.Key[] { KeyConstants._category, KeyConstants._name, KeyConstants._value };
	private static final Key[] TIMER_COLUMNS = new Collection.Key[] { KeyConstants._label, KeyConstants._time, KeyConstants._template };
	private static final Key[] DUMP_COLUMNS = new Collection.Key[] { KeyConstants._output, KeyConstants._template, KeyConstants._line };

	private static final Key[] PAGE_PART_COLUMNS = new Collection.Key[] { KeyConstants._id, KeyConstants._count, KeyConstants._min, KeyConstants._max, KeyConstants._avg,
			KeyConstants._total, KeyConstants._path, KeyConstants._start, KeyConstants._end, KeyConstants._startLine, KeyConstants._endLine, KeyConstants._snippet };

	private static final Key[] TRACES_COLUMNS = new Collection.Key[] { KeyConstants._type, KeyConstants._category, KeyConstants._text, KeyConstants._template, KeyConstants._line,
			KeyConstants._action, KeyConstants._varname, KeyConstants._varvalue, KeyConstants._time };

	private static final Key[] IMPLICIT_ACCESS_COLUMNS = new Collection.Key[] { KeyConstants._template, KeyConstants._line, KeyConstants._scope, KeyConstants._count,
			KeyConstants._name };

	@Override
	public void reset() {
		entries.clear();
		if (partEntries != null) partEntries.clear();
		queries.clear();
		implicitAccesses.clear();
		if (genericData != null) genericData.clear();
		timers.clear();
		traces.clear();
		dumps.clear();
		exceptions.clear();
		historyId.clear();
		historyLevel.clear();
		output = true;
		outputLog = null;
		abort = null;
		outputContext = null;
		queryTime = 0;
	}

	public DebuggerImpl() {}

	@Override
	public DebugEntryTemplate getEntry(PageContext pc, PageSource source) {
		return getEntry(pc, source, null);
	}

	@Override
	public DebugEntryTemplate getEntry(PageContext pc, PageSource source, String key) {
		lastEntry = System.currentTimeMillis();
		String src = DebugEntryTemplateImpl.getSrc(source == null ? "" : source.getDisplayPath(), key);

		DebugEntryTemplateImpl de = entries.get(src);
		if (de != null) {
			de.countPP();
			historyId.appendEL(de.getId());
			historyLevel.appendEL(Caster.toInteger(pc.getCurrentLevel()));
			return de;
		}
		de = new DebugEntryTemplateImpl(source, key);
		entries.put(src, de);
		historyId.appendEL(de.getId());
		historyLevel.appendEL(Caster.toInteger(pc.getCurrentLevel()));
		return de;
	}

	@Override
	public DebugEntryTemplatePart getEntry(PageContext pc, PageSource source, int startPos, int endPos) {
		String src = DebugEntryTemplatePartImpl.getSrc(source == null ? "" : source.getDisplayPath(), startPos, endPos);
		DebugEntryTemplatePartImpl de = null;
		if (partEntries != null) {
			de = partEntries.get(src);
			if (de != null) {
				de.countPP();
				return de;
			}
		}
		else {
			partEntries = new HashMap<String, DebugEntryTemplatePartImpl>();
		}

		ResourceSnippet snippet = snippetsMap.getSnippet(source, startPos, endPos, ((PageContextImpl) pc).getResourceCharset().name());
		de = new DebugEntryTemplatePartImpl(source, startPos, endPos, snippet.getStartLine(), snippet.getEndLine(), snippet.getContent());
		partEntries.put(src, de);
		return de;
	}

	private ArrayList<DebugEntryTemplate> toArray() {
		ArrayList<DebugEntryTemplate> arrPages = new ArrayList<DebugEntryTemplate>(entries.size());
		Iterator<String> it = entries.keySet().iterator();
		while (it.hasNext()) {
			DebugEntryTemplate page = entries.get(it.next());
			page.resetQueryTime();
			arrPages.add(page);

		}
		Collections.sort(arrPages, DEBUG_ENTRY_TEMPLATE_COMPARATOR);

		// Queries
		int len = queries.size();
		QueryEntry entry;
		for (int i = 0; i < len; i++) {
			entry = queries.get(i);
			String path = entry.getSrc();
			Object o = entries.get(path);

			if (o != null) {
				DebugEntryTemplate oe = (DebugEntryTemplate) o;
				oe.updateQueryTime(entry.getExecutionTime());
			}
		}

		return arrPages;
	}

	public static boolean debugQueryUsage(PageContext pageContext, QueryResult qr) {
		if (pageContext.getConfig().debug() && qr instanceof Query) {
			if (((ConfigWebImpl) pageContext.getConfig()).hasDebugOptions(ConfigImpl.DEBUG_QUERY_USAGE)) {
				((Query) qr).enableShowQueryUsage();
				return true;
			}
		}
		return false;
	}

	public static boolean debugQueryUsage(PageContext pageContext, Query qry) {
		if (pageContext.getConfig().debug() && qry instanceof Query) {
			if (((ConfigWebImpl) pageContext.getConfig()).hasDebugOptions(ConfigImpl.DEBUG_QUERY_USAGE)) {
				qry.enableShowQueryUsage();
				return true;
			}
		}
		return false;
	}

	private String _toString(long value) {
		if (value <= 0) return "0";
		return String.valueOf(value);
	}

	private String _toString(int value) {
		if (value <= 0) return "0";
		return String.valueOf(value);
	}

	@Override
	public void addQuery(Query query, String datasource, String name, SQL sql, int recordcount, PageSource src, int time) {
		addQuery(query, datasource, name, sql, recordcount, src, (long) time);
	}

	@Override
	public void addQuery(Query query, String datasource, String name, SQL sql, int recordcount, PageSource src, long time) {
		TemplateLine tl = null;
		if (src != null) tl = new TemplateLine(src.getDisplayPath(), 0);

		queries.add(new QueryResultEntryImpl((QueryResult) query, datasource, name, sql, recordcount, tl, time));
	}

	public void addQuery(QueryResult qr, String datasource, String name, SQL sql, int recordcount, TemplateLine tl, long time) {
		queries.add(new QueryResultEntryImpl(qr, datasource, name, sql, recordcount, tl, time));
	}

	public void addQuery(long time) {
		queryTime += time;
	}

	@Override
	public void setOutput(boolean output) {
		setOutput(output, false);
	}

	public void setOutput(boolean output, boolean listen) {
		this.output = output;
		if (listen) {
			this.outputContext = new ApplicationException("");
		}
	}

	// FUTURE add to inzerface
	public boolean getOutput() {
		return output;
	}

	public PageException getOutputContext() {
		return outputContext;
	}

	@Override
	public List<QueryEntry> getQueries() {
		return queries;
	}

	/**
	 * returns the DebugEntry for the current request's IP address, or null if no template matches the
	 * address
	 * 
	 * @param pc
	 * @return
	 */
	public static lucee.runtime.config.DebugEntry getDebugEntry(PageContext pc) {

		String addr = pc.getHttpServletRequest().getRemoteAddr();
		lucee.runtime.config.DebugEntry debugEntry = ((ConfigImpl) pc.getConfig()).getDebugEntry(addr, null);
		return debugEntry;
	}

	@Override
	public void writeOut(PageContext pc) throws IOException {
		// stop();
		if (!output) return;

		lucee.runtime.config.DebugEntry debugEntry = getDebugEntry(pc);

		if (debugEntry == null) {
			// pc.forceWrite(pc.getConfig().getDefaultDumpWriter().toString(pc,toDumpData(pc,
			// 9999,DumpUtil.toDumpProperties()),true));
			return;
		}

		Struct args = new StructImpl();
		args.setEL(KeyConstants._custom, debugEntry.getCustom());
		try {
			args.setEL(KeyConstants._debugging, pc.getDebugger().getDebuggingData(pc));
		}
		catch (PageException e1) {}

		try {
			String path = debugEntry.getPath();
			PageSource[] arr = ((PageContextImpl) pc).getPageSources(path);
			Page p = PageSourceImpl.loadPage(pc, arr, null);

			// patch for old path
			String fullname = debugEntry.getFullname();
			if (p == null) {
				if (path != null) {
					boolean changed = false;
					if (path.endsWith("/Modern.cfc") || path.endsWith("\\Modern.cfc")) {
						path = "/lucee-server-context/admin/debug/Modern.cfc";
						fullname = "lucee-server-context.admin.debug.Modern";
						changed = true;
					}
					else if (path.endsWith("/Classic.cfc") || path.endsWith("\\Classic.cfc")) {
						path = "/lucee-server-context/admin/debug/Classic.cfc";
						fullname = "lucee-server-context.admin.debug.Classic";
						changed = true;
					}
					else if (path.endsWith("/Comment.cfc") || path.endsWith("\\Comment.cfc")) {
						path = "/lucee-server-context/admin/debug/Comment.cfc";
						fullname = "lucee-server-context.admin.debug.Comment";
						changed = true;
					}
					if (changed) pc.write(
							"<span style='color:red'>Please update your debug template definitions in the Lucee admin by going into the detail view and hit the \"update\" button.</span>");

				}

				arr = ((PageContextImpl) pc).getPageSources(path);
				p = PageSourceImpl.loadPage(pc, arr);
			}

			pc.addPageSource(p.getPageSource(), true);
			try {
				Component c = pc.loadComponent(fullname);
				c.callWithNamedValues(pc, "output", args);
			}
			finally {
				pc.removeLastPageSource(true);
			}
		}
		catch (PageException e) {
			pc.handlePageException(e);
		}
	}

	@Override
	public Struct getDebuggingData(PageContext pc) throws DatabaseException {
		return getDebuggingData(pc, false);
	}

	@Override
	public Struct getDebuggingData(PageContext pc, boolean addAddionalInfo) throws DatabaseException {
		PageContextImpl pci = (PageContextImpl) pc;
		Struct debugging = new StructImpl();

		// datasources
		debugging.setEL(KeyConstants._datasources, ((ConfigImpl) pc.getConfig()).getDatasourceConnectionPool().meta());

		//////////////////////////////////////////
		//////// QUERIES ///////////////////////////
		//////////////////////////////////////////
		long queryTime = 0;
		List<QueryEntry> queries = getQueries();
		Query qryQueries = null;
		if (!queries.isEmpty()) {
			try {
				qryQueries = new QueryImpl(QUERY_COLUMNS, QUERY_COLUMN_TYPES, queries.size(), "query");
			}
			catch (DatabaseException e) {
				qryQueries = new QueryImpl(QUERY_COLUMNS, queries.size(), "query");
			}
			debugging.setEL(KeyConstants._queries, qryQueries);

			Struct qryExe = new StructImpl();
			ListIterator<QueryEntry> qryIt = queries.listIterator();

			int row = 0;
			try {
				QueryEntry qe;
				while (qryIt.hasNext()) {
					row++;
					qe = qryIt.next();
					queryTime += qe.getExecutionTime();
					qryQueries.setAt(KeyConstants._name, row, qe.getName() == null ? "" : qe.getName());
					qryQueries.setAt(KeyConstants._time, row, Long.valueOf(qe.getExecutionTime()));
					qryQueries.setAt(KeyConstants._sql, row, qe.getSQL().toString());
					if (qe instanceof QueryResultEntryImpl) {
						TemplateLine tl = ((QueryResultEntryImpl) qe).getTemplateLine();
						if (tl != null) {
							qryQueries.setAt(KeyConstants._src, row, tl.template);
							qryQueries.setAt(KeyConstants._line, row, tl.line);
						}
					}
					else qryQueries.setAt(KeyConstants._src, row, qe.getSrc());
					qryQueries.setAt(KeyConstants._count, row, Integer.valueOf(qe.getRecordcount()));
					qryQueries.setAt(KeyConstants._datasource, row, qe.getDatasource());
					qryQueries.setAt(CACHE_TYPE, row, qe.getCacheType());

					Struct usage = getUsage(qe);
					if (usage != null) qryQueries.setAt(KeyConstants._usage, row, usage);

					Object o = qryExe.get(KeyImpl.init(qe.getSrc()), null);
					if (o == null) qryExe.setEL(KeyImpl.init(qe.getSrc()), Long.valueOf(qe.getExecutionTime()));
					else qryExe.setEL(KeyImpl.init(qe.getSrc()), Long.valueOf(((Long) o).longValue() + qe.getExecutionTime()));
				}
			}
			catch (PageException dbe) {}
		}
		else {
			queryTime = this.queryTime;
		}

		//////////////////////////////////////////
		//////// PAGES ///////////////////////////
		//////////////////////////////////////////
		long totalTime = 0;
		ArrayList<DebugEntryTemplate> arrPages = null;
		Query qryPage = null;
		if (entries.size() > 0) {
			int row = 0;
			arrPages = toArray();
			int len = arrPages.size();
			qryPage = new QueryImpl(PAGE_COLUMNS, len, "query");
			debugging.setEL(KeyConstants._pages, qryPage);

			try {
				DebugEntryTemplate de;
				// PageSource ps;
				for (int i = 0; i < len; i++) {
					row++;
					de = arrPages.get(i);
					// ps = de.getPageSource();
					totalTime += de.getFileLoadTime() + de.getExeTime();
					qryPage.setAt(KeyConstants._id, row, de.getId());
					qryPage.setAt(KeyConstants._count, row, _toString(de.getCount()));
					qryPage.setAt(KeyConstants._min, row, _toString(de.getMin()));
					qryPage.setAt(KeyConstants._max, row, _toString(de.getMax()));
					qryPage.setAt(KeyConstants._avg, row, _toString(de.getExeTime() / de.getCount()));
					qryPage.setAt(KeyConstants._app, row, _toString(de.getExeTime() - de.getQueryTime()));
					qryPage.setAt(KeyConstants._load, row, _toString(de.getFileLoadTime()));
					qryPage.setAt(KeyConstants._query, row, _toString(de.getQueryTime()));
					qryPage.setAt(KeyConstants._total, row, _toString(de.getFileLoadTime() + de.getExeTime()));
					qryPage.setAt(KeyConstants._src, row, de.getSrc());
				}
			}
			catch (PageException dbe) {}
		}
		else {
			totalTime = pci.getEndTimeNS() > pci.getStartTimeNS() ? pci.getEndTimeNS() - pci.getStartTimeNS() : 0;
		}

		//////////////////////////////////////////
		//////// TIMES ///////////////////////////
		//////////////////////////////////////////
		Struct times = new StructImpl();
		times.setEL(KeyConstants._total, Caster.toDouble(totalTime));
		times.setEL(KeyConstants._query, Caster.toDouble(queryTime));
		debugging.setEL(KeyConstants._times, times);

		//////////////////////////////////////////
		//////// PAGE PARTS ///////////////////////////
		//////////////////////////////////////////
		boolean hasParts = partEntries != null && !partEntries.isEmpty() && arrPages != null && !arrPages.isEmpty();
		int qrySize = 0;
		Query qryPart = null;
		if (hasParts) {
			qryPart = new QueryImpl(PAGE_PART_COLUMNS, qrySize, "query");
			debugging.setEL(PAGE_PARTS, qryPart);
			String slowestTemplate = arrPages.get(0).getPath();
			List<DebugEntryTemplatePart> filteredPartEntries = new ArrayList();
			java.util.Collection<DebugEntryTemplatePartImpl> col = partEntries.values();
			for (DebugEntryTemplatePart detp: col) {

				if (detp.getPath().equals(slowestTemplate)) filteredPartEntries.add(detp);
			}
			qrySize = Math.min(filteredPartEntries.size(), MAX_PARTS);

			int row = 0;
			Collections.sort(filteredPartEntries, DEBUG_ENTRY_TEMPLATE_PART_COMPARATOR);

			DebugEntryTemplatePart[] parts = new DebugEntryTemplatePart[qrySize];

			if (filteredPartEntries.size() > MAX_PARTS) parts = filteredPartEntries.subList(0, MAX_PARTS).toArray(parts);
			else parts = filteredPartEntries.toArray(parts);

			try {
				DebugEntryTemplatePart de;
				// PageSource ps;
				for (int i = 0; i < parts.length; i++) {
					row++;
					de = parts[i];

					qryPart.setAt(KeyConstants._id, row, de.getId());
					qryPart.setAt(KeyConstants._count, row, _toString(de.getCount()));
					qryPart.setAt(KeyConstants._min, row, _toString(de.getMin()));
					qryPart.setAt(KeyConstants._max, row, _toString(de.getMax()));
					qryPart.setAt(KeyConstants._avg, row, _toString(de.getExeTime() / de.getCount()));
					qryPart.setAt(KeyConstants._start, row, _toString(de.getStartPosition()));
					qryPart.setAt(KeyConstants._end, row, _toString(de.getEndPosition()));
					qryPart.setAt(KeyConstants._total, row, _toString(de.getExeTime()));
					qryPart.setAt(KeyConstants._path, row, de.getPath());

					if (de instanceof DebugEntryTemplatePartImpl) {

						qryPart.setAt(KeyConstants._startLine, row, _toString(((DebugEntryTemplatePartImpl) de).getStartLine()));
						qryPart.setAt(KeyConstants._endLine, row, _toString(((DebugEntryTemplatePartImpl) de).getEndLine()));
						qryPart.setAt(KeyConstants._snippet, row, ((DebugEntryTemplatePartImpl) de).getSnippet());
					}
				}
			}
			catch (PageException dbe) {}
		}

		//////////////////////////////////////////
		//////// EXCEPTIONS ///////////////////////////
		//////////////////////////////////////////
		int len = exceptions == null ? 0 : exceptions.size();
		Array arrExceptions = null;
		if (len > 0) {
			arrExceptions = new ArrayImpl();
			debugging.setEL(KeyConstants._exceptions, arrExceptions);

			Iterator<CatchBlock> it = exceptions.iterator();
			while (it.hasNext()) {
				arrExceptions.appendEL(it.next());
			}
		}

		//////////////////////////////////////////
		//////// GENERIC DATA ///////////////////////////
		//////////////////////////////////////////
		Query qryGenData = null;
		Map<String, Map<String, List<String>>> genData = getGenericData();
		if (genData != null && genData.size() > 0) {
			qryGenData = new QueryImpl(GEN_DATA_COLUMNS, 0, "query");
			debugging.setEL(GENERIC_DATA, qryGenData);
			Iterator<Entry<String, Map<String, List<String>>>> it = genData.entrySet().iterator();
			Entry<String, Map<String, List<String>>> e;
			Iterator<Entry<String, List<String>>> itt;
			Entry<String, List<String>> ee;
			String cat;
			int r;
			List<String> list;
			Object val;
			while (it.hasNext()) {
				e = it.next();
				cat = e.getKey();
				itt = e.getValue().entrySet().iterator();
				while (itt.hasNext()) {
					ee = itt.next();
					r = qryGenData.addRow();
					list = ee.getValue();
					if (list.size() == 1) val = list.get(0);
					else val = ListUtil.listToListEL(list, ", ");
					qryGenData.setAtEL(KeyConstants._category, r, cat);
					qryGenData.setAtEL(KeyConstants._name, r, ee.getKey());
					qryGenData.setAtEL(KeyConstants._value, r, val);
				}
			}
		}

		//////////////////////////////////////////
		//////// TIMERS ///////////////////////////
		//////////////////////////////////////////
		len = timers == null ? 0 : timers.size();
		Query qryTimers = null;
		if (len > 0) {
			qryTimers = new QueryImpl(TIMER_COLUMNS, len, "timers");
			debugging.setEL(KeyConstants._timers, qryTimers);
			try {
				Iterator<DebugTimerImpl> it = timers.iterator();
				DebugTimer timer;
				int row = 0;
				while (it.hasNext()) {
					timer = it.next();
					row++;
					qryTimers.setAt(KeyConstants._label, row, timer.getLabel());
					qryTimers.setAt(KeyConstants._template, row, timer.getTemplate());
					qryTimers.setAt(KeyConstants._time, row, Caster.toDouble(timer.getTime()));
				}
			}
			catch (PageException dbe) {}
		}

		//////////////////////////////////////////
		//////// HISTORY ///////////////////////////
		//////////////////////////////////////////
		Query history = new QueryImpl(new Collection.Key[] {}, 0, "history");
		debugging.setEL(KeyConstants._history, history);

		try {
			history.addColumn(KeyConstants._id, historyId);
			history.addColumn(KeyConstants._level, historyLevel);
		}
		catch (PageException e) {}

		//////////////////////////////////////////
		//////// DUMPS ///////////////////////////
		//////////////////////////////////////////
		len = dumps == null ? 0 : dumps.size();
		if (!((ConfigImpl) pc.getConfig()).hasDebugOptions(ConfigImpl.DEBUG_DUMP)) len = 0;
		Query qryDumps = null;
		if (len > 0) {
			qryDumps = new QueryImpl(DUMP_COLUMNS, len, "dumps");
			debugging.setEL(KeyConstants._dumps, qryDumps);
			try {
				Iterator<DebugDump> it = dumps.iterator();
				DebugDump dd;
				int row = 0;
				while (it.hasNext()) {
					dd = it.next();
					row++;
					qryDumps.setAt(KeyConstants._output, row, dd.getOutput());
					if (!StringUtil.isEmpty(dd.getTemplate())) qryDumps.setAt(KeyConstants._template, row, dd.getTemplate());
					if (dd.getLine() > 0) qryDumps.setAt(KeyConstants._line, row, new Double(dd.getLine()));
				}
			}
			catch (PageException dbe) {}
		}

		//////////////////////////////////////////
		//////// TRACES ///////////////////////////
		//////////////////////////////////////////
		len = traces == null ? 0 : traces.size();
		if (!((ConfigImpl) pc.getConfig()).hasDebugOptions(ConfigImpl.DEBUG_TRACING)) len = 0;
		Query qryTraces = null;
		if (len > 0) {
			qryTraces = new QueryImpl(TRACES_COLUMNS, len, "traces");
			debugging.setEL(KeyConstants._traces, qryTraces);
			try {
				Iterator<DebugTraceImpl> it = traces.iterator();
				DebugTraceImpl trace;
				int row = 0;
				while (it.hasNext()) {
					trace = it.next();
					row++;
					qryTraces.setAt(KeyConstants._type, row, DebugTraceImpl.toType(trace.getType(), "INFO"));
					if (!StringUtil.isEmpty(trace.getCategory())) qryTraces.setAt(KeyConstants._category, row, trace.getCategory());
					if (!StringUtil.isEmpty(trace.getText())) qryTraces.setAt(KeyConstants._text, row, trace.getText());
					if (!StringUtil.isEmpty(trace.getTemplate())) qryTraces.setAt(KeyConstants._template, row, trace.getTemplate());
					if (trace.getLine() > 0) qryTraces.setAt(KeyConstants._line, row, new Double(trace.getLine()));
					if (!StringUtil.isEmpty(trace.getAction())) qryTraces.setAt(KeyConstants._action, row, trace.getAction());
					if (!StringUtil.isEmpty(trace.getVarName())) qryTraces.setAt(KeyImpl.init("varname"), row, trace.getVarName());
					if (!StringUtil.isEmpty(trace.getVarValue())) qryTraces.setAt(KeyImpl.init("varvalue"), row, trace.getVarValue());
					qryTraces.setAt(KeyConstants._time, row, new Double(trace.getTime()));
				}
			}
			catch (PageException dbe) {}
		}

		//////////////////////////////////////////
		//////// SCOPE ACCESS ////////////////////
		//////////////////////////////////////////
		len = implicitAccesses == null ? 0 : implicitAccesses.size();
		Query qryImplicitAccesseses = null;
		if (len > 0) {
			qryImplicitAccesseses = new QueryImpl(IMPLICIT_ACCESS_COLUMNS, len, "implicitAccess");
			debugging.setEL(IMPLICIT_ACCESS, qryImplicitAccesseses);
			try {
				Iterator<ImplicitAccessImpl> it = implicitAccesses.values().iterator();
				ImplicitAccessImpl das;
				int row = 0;
				while (it.hasNext()) {
					das = it.next();
					row++;
					qryImplicitAccesseses.setAt(KeyConstants._template, row, das.getTemplate());
					qryImplicitAccesseses.setAt(KeyConstants._line, row, new Double(das.getLine()));
					qryImplicitAccesseses.setAt(KeyConstants._scope, row, das.getScope());
					qryImplicitAccesseses.setAt(KeyConstants._count, row, new Double(das.getCount()));
					qryImplicitAccesseses.setAt(KeyConstants._name, row, das.getName());

				}
			}
			catch (PageException dbe) {}
		}

		//////////////////////////////////////////
		//////// ABORT /////////////////////////
		//////////////////////////////////////////
		if (abort != null) {
			Struct sct = new StructImpl();
			sct.setEL(KeyConstants._template, abort.template);
			sct.setEL(KeyConstants._line, new Double(abort.line));
			debugging.setEL(KeyConstants._abort, sct);
		}

		//////////////////////////////////////////
		//////// SCOPES /////////////////////////
		//////////////////////////////////////////
		if (addAddionalInfo) {
			Struct scopes = new StructImpl();
			scopes.setEL("cgi", pc.cgiScope());
			debugging.setEL(KeyConstants._scope, scopes);
		}

		debugging.setEL(KeyImpl.init("starttime"), new DateTimeImpl(starttime, false));
		debugging.setEL(KeyConstants._id, pci.getRequestId() + "-" + pci.getId());

		return debugging;
	}

	public void setAbort(TemplateLine abort) {
		this.abort = abort;
	}

	public TemplateLine getAbort() {
		return this.abort;
	}

	private static Struct getUsage(QueryEntry qe) throws PageException {
		Query qry = qe.getQry();

		QueryColumn c;
		DebugQueryColumn dqc;
		outer: if (qry != null) {
			Struct usage = null;
			Collection.Key[] columnNames = qry.getColumnNames();
			Collection.Key columnName;
			for (int i = 0; i < columnNames.length; i++) {
				columnName = columnNames[i];
				c = qry.getColumn(columnName);
				if (!(c instanceof DebugQueryColumn)) break outer;
				dqc = (DebugQueryColumn) c;
				if (usage == null) usage = new StructImpl();
				usage.setEL(columnName, Caster.toBoolean(dqc.isUsed()));
			}
			return usage;
		}
		return null;
	}

	@Override
	public DebugTimer addTimer(String label, long time, String template) {
		DebugTimerImpl t;
		timers.add(t = new DebugTimerImpl(label, time, template));
		return t;
	}

	@Override
	public DebugTrace addTrace(int type, String category, String text, PageSource ps, String varName, String varValue) {

		long _lastTrace = (traces.isEmpty()) ? lastEntry : lastTrace;
		lastTrace = System.currentTimeMillis();

		DebugTraceImpl t = new DebugTraceImpl(type, category, text, ps == null ? "unknown template" : ps.getDisplayPath(), SystemUtil.getCurrentContext(null).line, "", varName,
				varValue, lastTrace - _lastTrace);
		traces.add(t);
		return t;
	}

	@Override
	public DebugDump addDump(PageSource ps, String dump) {
		DebugDump dt = new DebugDumpImpl(ps.getDisplayPath(), SystemUtil.getCurrentContext(null).line, dump);
		dumps.add(dt);
		return dt;
	}

	@Override
	public DebugTrace addTrace(int type, String category, String text, String template, int line, String action, String varName, String varValue) {

		long _lastTrace = (traces.isEmpty()) ? lastEntry : lastTrace;
		lastTrace = System.currentTimeMillis();

		DebugTraceImpl t = new DebugTraceImpl(type, category, text, template, line, action, varName, varValue, lastTrace - _lastTrace);
		traces.add(t);
		return t;
	}

	@Override
	public DebugTrace[] getTraces() {
		return getTraces(ThreadLocalPageContext.get());
	}

	@Override
	public DebugTrace[] getTraces(PageContext pc) {
		if (pc != null && ((ConfigImpl) pc.getConfig()).hasDebugOptions(ConfigImpl.DEBUG_TRACING)) return traces.toArray(new DebugTrace[traces.size()]);
		return new DebugTrace[0];
	}

	@Override
	public void addException(Config config, PageException pe) {
		if (exceptions.size() > 1000) return;
		try {
			exceptions.add(((PageExceptionImpl) pe).getCatchBlock(config));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	@Override
	public CatchBlock[] getExceptions() {
		return exceptions.toArray(new CatchBlock[exceptions.size()]);
	}

	@Override
	public void init(Config config) {
		this.starttime = System.currentTimeMillis() + config.getTimeServerOffset();
	}

	@Override
	public void addImplicitAccess(String scope, String name) {
		if (implicitAccesses.size() > 1000) return;
		try {
			SystemUtil.TemplateLine tl = SystemUtil.getCurrentContext(null);
			String key = tl + ":" + scope + ":" + name;
			ImplicitAccessImpl dsc = implicitAccesses.get(key);
			if (dsc != null) dsc.inc();
			else implicitAccesses.put(key, new ImplicitAccessImpl(scope, name, tl.template, tl.line));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	@Override
	public ImplicitAccess[] getImplicitAccesses(int scope, String name) {
		return implicitAccesses.values().toArray(new ImplicitAccessImpl[implicitAccesses.size()]);
	}

	@Override
	public void setOutputLog(DebugOutputLog outputLog) {
		this.outputLog = outputLog;
	}

	public DebugTextFragment[] getOutputTextFragments() {
		return this.outputLog.getFragments();
	}

	public Query getOutputText() throws DatabaseException {
		DebugTextFragment[] fragments = outputLog.getFragments();
		int len = fragments == null ? 0 : fragments.length;
		Query qryOutputLog = new QueryImpl(new Collection.Key[] { KeyConstants._line, KeyConstants._template, KeyConstants._text }, len, "query");

		if (len > 0) {
			for (int i = 0; i < fragments.length; i++) {
				qryOutputLog.setAtEL(KeyConstants._line, i + 1, fragments[i].getLine());
				qryOutputLog.setAtEL(KeyConstants._template, i + 1, fragments[i].getTemplate());
				qryOutputLog.setAtEL(KeyConstants._text, i + 1, fragments[i].getText());
			}
		}
		return qryOutputLog;

	}

	public void resetTraces() {
		traces.clear();
	}

	@Override
	public void addGenericData(String labelCategory, Map<String, String> data) {
		// init generic data if necessary
		if (genericData == null) genericData = new ConcurrentHashMap<String, Map<String, List<String>>>();

		// category
		Map<String, List<String>> cat = genericData.get(labelCategory);
		if (cat == null) genericData.put(labelCategory, cat = new ConcurrentHashMap<String, List<String>>());

		// data
		Iterator<Entry<String, String>> it = data.entrySet().iterator();
		Entry<String, String> e;
		List<String> entry;
		while (it.hasNext()) {
			e = it.next();
			entry = cat.get(e.getKey());
			if (entry == null) {
				cat.put(e.getKey(), entry = new ArrayList<String>());
			}
			entry.add(e.getValue());
		}
	}

	/*
	 * private List<String> createAndFillList(Map<String, List<String>> cat) { Iterator<List<String>> it
	 * = cat.values().iterator(); int size=0; while(it.hasNext()){ size=it.next().size(); break; }
	 * ArrayList<String> list = new ArrayList<String>();
	 * 
	 * // fill with empty values to be on the same level as other columns for(int
	 * i=0;i<size;i++)list.add("");
	 * 
	 * return list; }
	 */

	@Override
	public Map<String, Map<String, List<String>>> getGenericData() {
		return genericData;
	}

	public static void deprecated(PageContext pc, String key, String msg) {
		if (pc.getConfig().debug()) {
			// do we already have set?
			boolean exists = false;
			Map<String, Map<String, List<String>>> gd = pc.getDebugger().getGenericData();
			if (gd != null) {
				Map<String, List<String>> warning = gd.get("Warning");
				if (warning != null) {
					exists = warning.containsKey(key);
				}
			}

			if (!exists) {
				Map<String, String> map = new HashMap<>();
				map.put(key, msg);
				pc.getDebugger().addGenericData("Warning", map);
			}
		}
	}

}

final class DebugEntryTemplateComparator implements Comparator<DebugEntryTemplate> {

	@Override
	public int compare(DebugEntryTemplate de1, DebugEntryTemplate de2) {
		long result = ((de2.getExeTime() + de2.getFileLoadTime()) - (de1.getExeTime() + de1.getFileLoadTime()));
		// we do this additional step to try to avoid ticket LUCEE-2076
		return result > 0L ? 1 : (result < 0L ? -1 : 0);
	}
}

final class DebugEntryTemplatePartComparator implements Comparator<DebugEntryTemplatePart> {

	@Override
	public int compare(DebugEntryTemplatePart de1, DebugEntryTemplatePart de2) {
		long result = de2.getExeTime() - de1.getExeTime();
		// we do this additional step to try to avoid ticket LUCEE-2076
		return result > 0L ? 1 : (result < 0L ? -1 : 0);
	}
}
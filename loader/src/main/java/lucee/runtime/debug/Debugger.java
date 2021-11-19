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
package lucee.runtime.debug;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.db.SQL;
import lucee.runtime.exp.CatchBlock;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;

/**
 * debugger interface
 */
public interface Debugger {

	public void init(Config config);

	/**
	 * reset the debug object
	 */
	public abstract void reset();

	/**
	 * @param pc current PagContext
	 * @param source Page Source for the entry
	 * @return returns a single DebugEntry.
	 */
	public DebugEntryTemplate getEntry(PageContext pc, PageSource source);

	/**
	 * @param pc current PagContext
	 * @param source Page Source for the entry
	 * @param key key
	 * @return returns a single DebugEntry with a key.
	 */
	public DebugEntryTemplate getEntry(PageContext pc, PageSource source, String key);

	/**
	 * returns a single DebugEntry for a specific postion (startPos,endPos in the PageSource)
	 * 
	 * @param pc current PagContext
	 * @param source Page Source for the entry
	 * @param startPos start position in the file
	 * @param endPos end position in the file
	 * @return returns a debug entry.
	 */
	public DebugEntryTemplatePart getEntry(PageContext pc, PageSource source, int startPos, int endPos);

	/**
	 * sets if toHTML print html output info or not
	 * 
	 * @param output The output to set.
	 */
	public abstract void setOutput(boolean output);

	/**
	 * @return Returns the queries.
	 */
	public List<QueryEntry> getQueries();

	/**
	 * @param pc page context
	 * @throws IOException IO Exception
	 */
	public void writeOut(PageContext pc) throws IOException;

	/**
	 * returns the Debugging Info
	 * 
	 * @param pc page context
	 * @return debugging Info
	 * @throws PageException Page Exception
	 */
	public Struct getDebuggingData(PageContext pc) throws PageException;

	public Struct getDebuggingData(PageContext pc, boolean addAddionalInfo) throws PageException;

	/**
	 * adds new Timer info to debug
	 * 
	 * @param label Label
	 * @param exe Execution time
	 * @param template Template
	 * @return debug timer object
	 */
	public DebugTimer addTimer(String label, long exe, String template);

	/**
	 * add new Trace to debug
	 * 
	 * @param type type
	 * @param category category
	 * @param text text
	 * @param page page 
	 * @param varName variable name
	 * @param varValue variable value
	 * @return debug trace object
	 */
	public DebugTrace addTrace(int type, String category, String text, PageSource page, String varName, String varValue);

	public DebugTrace addTrace(int type, String category, String text, String template, int line, String action, String varName, String varValue);

	public abstract DebugTrace[] getTraces();

	public abstract void addException(Config config, PageException pe);

	public CatchBlock[] getExceptions();

	public void addImplicitAccess(String scope, String name);

	public ImplicitAccess[] getImplicitAccesses(int scope, String name);

	/**
	 * add new query execution time
	 * 
	 * @param query query
	 * @param datasource datasource name
	 * @param name name
	 * @param sql sql
	 * @param recordcount recordcount
	 * @param src src
	 * @param time time
	 * @deprecated use instead
	 * @see #addQuery(Query, String, String, SQL, int, PageSource, long)
	 */
	@Deprecated
	public void addQuery(Query query, String datasource, String name, SQL sql, int recordcount, PageSource src, int time);

	/**
	 * add new query execution time
	 * 
	 * @param query query
	 * @param datasource datasource
	 * @param name name
	 * @param sql sql
	 * @param recordcount recordcount
	 * @param src src
	 * @param time time
	 */
	public void addQuery(Query query, String datasource, String name, SQL sql, int recordcount, PageSource src, long time);

	public DebugTrace[] getTraces(PageContext pc);

	/**
	 * 
	 * @param labelCategory the name of the category, multiple records with the same category get
	 *            combined
	 * @param data you wanna show
	 */
	public void addGenericData(String labelCategory, Map<String, String> data);

	/**
	 * returning the generic data set for this request
	 * 
	 * @return a Map organized by category/data-column/data-value
	 */
	public Map<String, Map<String, List<String>>> getGenericData();

	public DebugDump addDump(PageSource ps, String dump);

	public void setOutputLog(DebugOutputLog outputLog);
}
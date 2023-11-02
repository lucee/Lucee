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
package lucee.transformer.bytecode;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.commons.lang.StringUtil;
import lucee.commons.lang.compiler.JavaFunction;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.transformer.Context;
import lucee.transformer.Factory;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.expression.literal.LitString;

public class BytecodeContext implements Context {

	private ClassWriter classWriter;
	private GeneratorAdapter adapter;
	private String className;
	private List<LitString> keys;
	private int count = 0;
	private Method method;
	private boolean doSubFunctions = true;
	// private StaticConstrBytecodeContext staticConstr;
	private ConstrBytecodeContext constr;
	private final boolean suppressWSbeforeArg;
	private final boolean output;
	private Stack<OnFinally> insideFinallies = new Stack<OnFinally>();
	Stack<OnFinally> tcf = new Stack<OnFinally>();
	private int currentTag;
	private int line;
	private BytecodeContext root;
	private boolean writeLog;
	private int rtn = -1;
	private final boolean returnValue;

	private static long _id = 0;

	private synchronized static String id() {
		if (_id < 0) _id = 0;
		return StringUtil.addZeros(++_id, 4);
	}

	private String id = id();
	private Page page;
	protected PageSource ps;

	public BytecodeContext(PageSource ps, ConstrBytecodeContext constr, Page page, List<LitString> keys, ClassWriter classWriter, String className, GeneratorAdapter adapter,
			Method method, boolean writeLog, boolean suppressWSbeforeArg, boolean output, boolean returnValue) {
		this.classWriter = classWriter;
		this.className = className;
		this.writeLog = writeLog;
		this.adapter = adapter;
		this.keys = keys;
		this.method = method;
		// this.staticConstr=statConstr;
		this.constr = constr;
		this.page = page;
		this.suppressWSbeforeArg = suppressWSbeforeArg;
		this.returnValue = returnValue;
		this.output = output;
		if (ps != null) this.ps = ps;
		else if (constr != null) this.ps = constr.ps;
	}

	public BytecodeContext(ConstrBytecodeContext constr, List<LitString> keys, BytecodeContext bc, GeneratorAdapter adapter, Method method) {
		this.classWriter = bc.getClassWriter();
		this.className = bc.getClassName();
		this.writeLog = bc.writeLog();

		this.adapter = adapter;
		this.keys = keys;
		this.method = method;
		// this.staticConstr=statConstr;
		this.constr = constr;
		this.page = bc.getPage();
		this.suppressWSbeforeArg = bc.suppressWSbeforeArg;
		this.returnValue = bc.returnValue;
		this.output = bc.output;
		this.ps = bc.ps;
	}

	@Override
	public Factory getFactory() {
		return page.getFactory();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public int incCount() {
		return ++this.count;
	}

	public void resetCount() {
		this.count = 0;
	}

	/**
	 * @return the adapter
	 */
	public GeneratorAdapter getAdapter() {
		return adapter;
	}

	/**
	 * @param adapter the adapter to set
	 */
	public void setAdapter(BytecodeContext bc) {
		this.adapter = bc.getAdapter();
	}

	/**
	 * @return the classWriter
	 */
	public ClassWriter getClassWriter() {
		return classWriter;
	}

	/**
	 * @param classWriter the classWriter to set
	 */
	public void setClassWriter(ClassWriter classWriter) {
		this.classWriter = classWriter;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	public synchronized int registerKey(LitString lit) {
		// synchronized (keys) {
		int index = keys.indexOf(lit);
		if (index != -1) return index;// calls the toString method of litString

		keys.add(lit);

		return keys.size() - 1;
		// }
	}

	public void registerJavaFunction(JavaFunction jbc) {
		this.page.registerJavaFunction(jbc);
	}

	public List<LitString> getKeys() {
		return keys;
	}

	// private static BytecodeContext staticConstr;

	public void pushOnFinally(OnFinally onFinally) {
		tcf.push(onFinally);
	}

	public void popOnFinally() {
		tcf.pop();
	}

	public Stack<OnFinally> getOnFinallyStack() {
		return tcf;
	}

	/**
	 * @return the method
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * @return the doSubFunctions
	 */
	public boolean doSubFunctions() {
		return doSubFunctions;
	}

	/**
	 * @param doSubFunctions the doSubFunctions to set
	 * @return
	 */
	public boolean changeDoSubFunctions(boolean doSubFunctions) {
		boolean old = this.doSubFunctions;
		this.doSubFunctions = doSubFunctions;
		return old;
	}

	/**
	 * @return the currentTag
	 */
	public int getCurrentTag() {
		return currentTag;
	}

	/**
	 * @param currentTag the currentTag to set
	 */
	public void setCurrentTag(int currentTag) {
		this.currentTag = currentTag;
	}

	public ConstrBytecodeContext getConstructor() {
		return constr;
	}

	public void visitLineNumber(int line) {
		this.line = line;
		getAdapter().visitLineNumber(line, getAdapter().mark());
	}

	public int getLine() {
		return line;
	}

	public BytecodeContext getRoot() {
		return root;
	}

	public void setRoot(BytecodeContext root) {
		this.root = root;
	}

	public boolean writeLog() {
		return this.writeLog;
	}

	public Page getPage() {
		return page;
	}

	public boolean getSupressWSbeforeArg() {
		return suppressWSbeforeArg;
	}

	public boolean getOutput() {
		return output;
	}

	public Config getConfig() {
		if (ps != null) return ps.getMapping().getConfig();
		return ThreadLocalPageContext.getConfig();
	}

	/**
	 * optional value maybe not exists!
	 * 
	 * @return PageSource if available otherwise null
	 */
	public PageSource getPageSource() {
		return ps;
	}

	public void finallyPush(OnFinally onf) {
		insideFinallies.push(onf);
	}

	public OnFinally finallyPop() {
		return insideFinallies.pop();
	}

	public boolean insideFinally(OnFinally onf) {
		Iterator<OnFinally> it = insideFinallies.iterator();
		while (it.hasNext()) {
			if (it.next() == onf) return true;
		}
		return false;
	}

	public void setReturn(int rtn) {
		this.rtn = rtn;
	}

	public int getReturn() {
		return rtn;
	}

	/**
	 * should the Page return the last expression or not
	 * 
	 * @return
	 */
	public boolean returnValue() {
		return returnValue;
	}

}
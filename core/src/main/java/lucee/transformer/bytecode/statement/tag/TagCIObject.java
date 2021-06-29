/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.bytecode.statement.tag;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lucee.print;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.op.Decision;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.StaticBody;
import lucee.transformer.bytecode.statement.FlowControlFinal;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.util.PageSourceCode;
import lucee.transformer.util.SourceCode;

public abstract class TagCIObject extends TagBase {

	private boolean main;
	private String name;

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		_writeOut(bc, true, null);
	}

	@Override
	public void _writeOut(BytecodeContext bc, boolean doReuse) throws TransformerException {
		_writeOut(bc, doReuse, null);
	}

	@Override

	protected void _writeOut(BytecodeContext bc, boolean doReuse, FlowControlFinal fcf) throws TransformerException {
		writeOut(bc.getPage());
	}

	public void writeOut(Page parent) throws TransformerException {

		// TODO better way to get this path?
		PageSourceCode psc = (PageSourceCode) parent.getSourceCode();

		SourceCode sc = parent.getSourceCode().subCFMLString(getStart().pos, getEnd().pos - getStart().pos);
		print.e("+++++++++++++++++++++++");
		print.e(sc.toString());
		print.e("+++++++++++++++++++++++");

		Page page = new Page(parent.getFactory(), parent.getConfig(), sc, this, CFMLEngineFactory.getInstance().getInfo().getFullVersionInfo(), parent.getLastModifed(),
				parent.writeLog(), parent.getSupressWSbeforeArg(), parent.getOutput(), parent.returnValue(), parent.ignoreScopes);
		// page.setIsComponent(true); // MUST can be an interface as well
		page.addStatement(this);

		print.e("getName:" + getName());

		String className = Page.createSubClass(parent.getClassName(), getName(), parent.getSourceCode().getDialect());
		print.e("className:" + className);

		print.e("parent:" + psc.getPageSource().getDisplayPath());
		print.e("parent-ps-classname:" + psc.getPageSource().getClassName());
		print.e("parent-classname:" + parent.getClassName());
		byte[] barr = page.execute(className);

		Resource classFile = psc.getPageSource().getMapping().getClassRootDirectory().getRealResource(page.getClassName() + ".class");
		print.e("classFile:" + parent.getClassName());
		try {
			IOUtil.copy(new ByteArrayInputStream(barr), classFile, true);
		}
		catch (IOException e) {
			new TransformerException(ExceptionUtil.getMessage(e), getStart());
		}
	}

	/**
	 * Constructor of the class
	 * 
	 * @param startLine
	 * @param endLine
	 */
	public TagCIObject(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	@Override
	public FlowControlFinal getFlowControlFinal() {
		return null;
	}

	public void setMain(boolean main) {
		this.main = main;
	}

	public boolean isMain() {
		return main;
	}

	public void setName(String name) throws EvaluatorException {
		if (!Decision.isVariableName(name)) throw new EvaluatorException("component name [" + name + "] is invalid");

		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<StaticBody> getStaticBodies() {
		Body b = getBody();
		List<StaticBody> list = null;
		if (!ASMUtil.isEmpty(b)) {
			Statement stat;
			Iterator<Statement> it = b.getStatements().iterator();
			while (it.hasNext()) {
				stat = it.next();
				// StaticBody
				if (stat instanceof StaticBody) {
					it.remove();
					if (list == null) list = new ArrayList<StaticBody>();
					list.add((StaticBody) stat);
					// return (StaticBody) stat;
				}
			}
		}
		return list;
	}

}
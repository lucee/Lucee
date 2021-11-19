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
package lucee.transformer.bytecode.statement.tag;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.statement.FlowControl;
import lucee.transformer.bytecode.statement.FlowControlFinal;
import lucee.transformer.bytecode.util.ASMUtil;

public final class TagContinue extends TagBase {

	private String label;

	public TagContinue(Factory f, Position start, Position end) {
		super(f, start, end);
		setHasFlowController(true);
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		ASMUtil.leadFlow(bc, this, FlowControl.CONTINUE, label);
	}

	@Override
	public void setParent(Statement parent) {
		super.setParent(parent);
		parent.setHasFlowController(true);
	}

	@Override
	public FlowControlFinal getFlowControlFinal() {
		return null;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
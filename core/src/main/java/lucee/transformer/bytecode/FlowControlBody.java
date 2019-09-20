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
package lucee.transformer.bytecode;

import org.objectweb.asm.Label;

import lucee.transformer.Factory;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.statement.FlowControlBreak;
import lucee.transformer.bytecode.statement.FlowControlContinue;

public abstract class FlowControlBody extends BodyBase implements FlowControlBreak, FlowControlContinue {

	public FlowControlBody(Factory f) {
		super(f);
	}

	private Label end = new Label();

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {

		super._writeOut(bc);
		bc.getAdapter().visitLabel(end);
	}

	@Override
	public Label getBreakLabel() {
		return end;
	}

	@Override
	public Label getContinueLabel() {
		return end;
	}

	@Override
	public String getLabel() {
		return null;
	}
}
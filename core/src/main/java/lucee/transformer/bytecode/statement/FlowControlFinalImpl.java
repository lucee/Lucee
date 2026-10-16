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
package lucee.transformer.bytecode.statement;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.visitor.OnFinally;

public final class FlowControlFinalImpl implements FlowControlFinal {

	// pairs of [entry label, label to go to after the finally code], one per distinct target
	private final List<Label[]> entries = new ArrayList<Label[]>();
	private OnFinally onFinally;

	@Override
	public Label getFinalEntryLabel(Label afterFinalGOTOLabel) {
		for (Label[] entry: entries) {
			if (entry[1] == afterFinalGOTOLabel) return entry[0];
		}
		Label entryLabel = new Label();
		entries.add(new Label[] { entryLabel, afterFinalGOTOLabel });
		return entryLabel;
	}

	@Override
	public void writeOutFinalEntries(BytecodeContext bc) throws TransformerException {
		if (entries.isEmpty()) return;
		GeneratorAdapter ga = bc.getAdapter();
		Label end = new Label();
		ga.visitJumpInsn(Opcodes.GOTO, end); // ignore when coming not from break/continue/retry
		// index loop instead of an iterator, an entry added while the finally code is written still gets written
		Label[] entry;
		for (int i = 0; i < entries.size(); i++) {
			entry = entries.get(i);
			ga.visitLabel(entry[0]);
			onFinally.writeOut(bc);
			ga.visitJumpInsn(Opcodes.GOTO, entry[1]);
		}
		ga.visitLabel(end);
	}

	@Override
	public void setOnFinally(OnFinally onFinally) {
		this.onFinally = onFinally;
	}

	@Override
	public OnFinally getOnFinally() {
		return onFinally;
	}
}
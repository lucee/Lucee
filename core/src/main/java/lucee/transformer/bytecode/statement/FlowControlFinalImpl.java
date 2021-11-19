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

import org.objectweb.asm.Label;

public class FlowControlFinalImpl implements FlowControlFinal {

	private Label entryLabel;
	private Label gotoLabel;

	public FlowControlFinalImpl() {
		this.entryLabel = new Label();
	}

	@Override
	public void setAfterFinalGOTOLabel(Label gotoLabel) {
		this.gotoLabel = gotoLabel;
	}

	@Override
	public Label getAfterFinalGOTOLabel() {
		return gotoLabel;
	}

	@Override
	public Label getFinalEntryLabel() {
		return entryLabel;
	}
}
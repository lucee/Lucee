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
package lucee.transformer.bytecode.statement.tag;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.statement.FlowControlFinal;
import lucee.transformer.bytecode.visitor.ParseBodyVisitor;

public final class TagOutput extends TagGroup {

	public static final int TYPE_QUERY = 0;
	public static final int TYPE_GROUP = 1;
	public static final int TYPE_INNER_GROUP = 2;
	public static final int TYPE_INNER_QUERY = 3;
	public static final int TYPE_NORMAL = 4;

	private int type;

	public TagOutput(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	public static TagOutput getParentTagOutputQuery(Statement stat) throws TransformerException {
		Statement parent = stat.getParent();
		if (parent == null) throw new TransformerException("there is no parent output with query", null);
		else if (parent instanceof TagOutput) {
			if (((TagOutput) parent).hasQuery()) return ((TagOutput) parent);
		}
		return getParentTagOutputQuery(parent);
	}

	public void setType(int type) {
		this.type = type;
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.statement.tag.TagBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		boolean old;
		switch (type) {
		case TYPE_GROUP:
			old = bc.changeDoSubFunctions(false);
			TagGroupUtil.writeOutTypeGroup(this, bc);
			bc.changeDoSubFunctions(old);
			break;
		case TYPE_INNER_GROUP:
			old = bc.changeDoSubFunctions(false);
			TagGroupUtil.writeOutTypeInnerGroup(this, bc);
			bc.changeDoSubFunctions(old);
			break;
		case TYPE_INNER_QUERY:
			old = bc.changeDoSubFunctions(false);
			TagGroupUtil.writeOutTypeInnerQuery(this, bc);
			bc.changeDoSubFunctions(old);
			break;
		case TYPE_NORMAL:
			writeOutTypeNormal(bc);
			break;
		case TYPE_QUERY:
			old = bc.changeDoSubFunctions(false);
			TagGroupUtil.writeOutTypeQuery(this, bc);
			bc.changeDoSubFunctions(old);
			break;

		default:
			throw new TransformerException("invalid type", getStart());
		}
	}

	/**
	 * write out normal query
	 * 
	 * @param adapter
	 * @throws TemplateException
	 */
	private void writeOutTypeNormal(BytecodeContext bc) throws TransformerException {
		ParseBodyVisitor pbv = new ParseBodyVisitor();
		pbv.visitBegin(bc);
		getBody().writeOut(bc);
		pbv.visitEnd(bc);
	}

	@Override
	public short getType() {
		return TAG_OUTPUT;
	}

	@Override
	public FlowControlFinal getFlowControlFinal() {
		return null;
	}

}
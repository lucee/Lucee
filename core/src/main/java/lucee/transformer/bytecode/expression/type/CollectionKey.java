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
package lucee.transformer.bytecode.expression.type;

import org.objectweb.asm.Type;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Types;

public class CollectionKey extends ExpressionBase {

	private String value;

	public CollectionKey(Factory factory, String value) {
		super(factory, null, null);
		this.value = value;
	}

	public CollectionKey(Factory factory, String value, Position start, Position end) {
		super(factory, start, end);
		this.value = value;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		getFactory().registerKey(bc, bc.getFactory().createLitString(value), false);
		return Types.COLLECTION_KEY;
	}

}
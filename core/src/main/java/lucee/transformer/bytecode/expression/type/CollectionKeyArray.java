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
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.ArrayVisitor;

public class CollectionKeyArray extends ExpressionBase {

	private String[] arr;

	public CollectionKeyArray(Factory factory, String[] arr) {
		super(factory, null, null);
		this.arr = arr;
	}

	public CollectionKeyArray(Factory factory, String[] arr, Position start, Position end) {
		super(factory, start, end);
		this.arr = arr;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		ArrayVisitor av = new ArrayVisitor();
		av.visitBegin(adapter, Types.COLLECTION_KEY, arr.length);
		for (int y = 0; y < arr.length; y++) {
			av.visitBeginItem(adapter, y);
			new CollectionKey(getFactory(), arr[y])._writeOut(bc, mode);
			// adapter.push(arr[y]);
			av.visitEndItem(bc.getAdapter());
		}
		av.visitEnd();
		return Types.COLLECTION_KEY_ARRAY;
	}
}
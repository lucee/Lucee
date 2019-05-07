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

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.tag.ThreadTag;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BodyBase;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.Types;

public final class TagThread extends TagBaseNoFinal {

	public static final Type THREAD_TAG = Type.getType(ThreadTag.class);

	private static final Method REGISTER = new Method("register", Types.VOID, new Type[] { Types.PAGE, Types.INT_VALUE });

	private int index;

	public TagThread(Factory f, Position start, Position end) {
		super(f, start, end);
		// print.e("::::"+ASMUtil.getAttributeString(this, "action","run")+":"+hashCode());
	}

	public void init() throws TransformerException {
		String action = ASMUtil.getAttributeString(this, "action", "run");
		// no body
		if (!"run".equalsIgnoreCase(action)) return;

		Page page = ASMUtil.getAncestorPage(this);
		index = page.addThread(this);

	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		String action = ASMUtil.getAttributeString(this, "action", "run");
		// no body
		if (!"run".equalsIgnoreCase(action)) {
			super._writeOut(bc);
			return;
		}

		/*
		 * Attribute name = getAttribute("name"); if(name==null){ addAttribute(new Attribute(false,
		 * "name",bc.getFactory().createLitString("thread"+RandomUtil.createRandomStringLC(20)), "string"));
		 * }
		 */

		GeneratorAdapter adapter = bc.getAdapter();
		// Page page = ASMUtil.getAncestorPage(this);

		// int index=page.addThread(this);
		super._writeOut(bc, false);

		adapter.loadLocal(bc.getCurrentTag());
		adapter.loadThis();
		adapter.push(index);
		adapter.invokeVirtual(THREAD_TAG, REGISTER);

	}

	/**
	 * @see lucee.transformer.bytecode.statement.tag.TagBase#getBody()
	 */
	@Override
	public Body getBody() {
		return new BodyBase(getFactory());
	}

	public Body getRealBody() {
		return super.getBody();
	}

}
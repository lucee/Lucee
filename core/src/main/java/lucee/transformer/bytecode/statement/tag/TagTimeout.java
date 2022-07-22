package lucee.transformer.bytecode.statement.tag;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.tag.Timeout;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BodyBase;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.Types;

public final class TagTimeout extends TagBaseNoFinal implements ATagThread {

	public static final Type TIMEOUT_TAG = Type.getType(Timeout.class);

	private static final Method REGISTER = new Method("register", Types.VOID, new Type[] { Types.PAGE, Types.INT_VALUE });

	private int index;

	public TagTimeout(Factory f, Position start, Position end) {
		super(f, start, end);
		// print.e("::::"+ASMUtil.getAttributeString(this, "action","run")+":"+hashCode());
	}

	public void init() throws TransformerException {
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
		adapter.invokeVirtual(TIMEOUT_TAG, REGISTER);

	}

	/**
	 * @see lucee.transformer.bytecode.statement.tag.TagBase#getBody()
	 */
	@Override
	public Body getBody() {
		return new BodyBase(getFactory());
	}

	@Override
	public Body getRealBody() {
		return super.getBody();
	}

}
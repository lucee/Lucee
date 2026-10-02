package lucee.transformer.bytecode.statement.tag;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.op.Caster;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.statement.FlowControlFinal;
import lucee.transformer.bytecode.util.ASMConstants;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.statement.tag.Attribute;
import lucee.transformer.statement.tag.Tag;

import java.util.Iterator;

/**
 * Optimized bytecode generation for cflog tags.
 * Bypasses tag lifecycle overhead by calling Log tag methods directly.
 */
public final class TagLog extends TagBase {

	private static final Type LOG_TAG_TYPE = Type.getType("Llucee/runtime/tag/Log;");

	private static final Method LOG_CONSTRUCTOR = new Method("<init>", Type.VOID_TYPE, new Type[] {});

	private static final Method SET_PAGE_CONTEXT = new Method("setPageContext", Type.VOID_TYPE, new Type[] {Types.PAGE_CONTEXT});

	private static final Method SET_LOG = new Method("setLog", Type.VOID_TYPE, new Type[] {Types.STRING});
	private static final Method SET_TEXT = new Method("setText", Type.VOID_TYPE, new Type[] {Types.STRING});
	private static final Method SET_TYPE = new Method("setType", Type.VOID_TYPE, new Type[] {Types.STRING});
	private static final Method SET_FILE = new Method("setFile", Type.VOID_TYPE, new Type[] {Types.STRING});
	private static final Method SET_APPLICATION = new Method("setApplication", Type.VOID_TYPE, new Type[] {Type.BOOLEAN_TYPE});
	private static final Method SET_EXCEPTION = new Method("setException", Type.VOID_TYPE, new Type[] {Types.OBJECT});

	private static final Method DO_START_TAG = new Method("doStartTag", Type.INT_TYPE, new Type[] {});

	public TagLog(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	@Override
	public FlowControlFinal getFlowControlFinal() {
		return null;
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		final GeneratorAdapter adapter = bc.getAdapter();
		Tag tag = (Tag) this;

		bc.visitLine(tag.getStart());

		try {
			// Create new Log tag instance
			adapter.newInstance(LOG_TAG_TYPE);
			adapter.dup();
			adapter.invokeConstructor(LOG_TAG_TYPE, LOG_CONSTRUCTOR);

			// Set PageContext
			adapter.dup();
			adapter.loadArg(0); // PageContext
			adapter.invokeVirtual(LOG_TAG_TYPE, SET_PAGE_CONTEXT);

			// Check for attributeCollection - if present, fall back to regular tag processing
			Attribute attributeCollection = tag.getAttribute("attributecollection");
			if (attributeCollection != null) {
				// Fall back to regular tag processing for attributeCollection
				super._writeOut(bc);
				return;
			}

			// Process individual attributes normally
			Iterator<Attribute> it = tag.getAttributes().values().iterator();
			while (it.hasNext()) {
				Attribute attr = it.next();
				String attrName = attr.getName().toLowerCase();

				if ("log".equals(attrName) || "name".equals(attrName)) {
					adapter.dup();
					String literalValue = getLiteralString(attr);
					if (literalValue != null) {
						adapter.push(literalValue);
					} else {
						attr.getValue().writeOut(bc, Expression.MODE_REF);
						adapter.invokeStatic(Types.CASTER, new Method("toString", Types.STRING, new Type[] {Types.OBJECT}));
					}
					adapter.invokeVirtual(LOG_TAG_TYPE, SET_LOG);
				}
				else if ("text".equals(attrName) || "message".equals(attrName)) {
					adapter.dup();
					String literalValue = getLiteralString(attr);
					if (literalValue != null) {
						adapter.push(literalValue);
					} else {
						attr.getValue().writeOut(bc, Expression.MODE_REF);
						adapter.invokeStatic(Types.CASTER, new Method("toString", Types.STRING, new Type[] {Types.OBJECT}));
					}
					adapter.invokeVirtual(LOG_TAG_TYPE, SET_TEXT);
				}
				else if ("type".equals(attrName) || "level".equals(attrName)) {
					adapter.dup();
					String literalValue = getLiteralString(attr);
					if (literalValue != null) {
						adapter.push(literalValue);
					} else {
						attr.getValue().writeOut(bc, Expression.MODE_REF);
						adapter.invokeStatic(Types.CASTER, new Method("toString", Types.STRING, new Type[] {Types.OBJECT}));
					}
					adapter.invokeVirtual(LOG_TAG_TYPE, SET_TYPE);
				}
				else if ("file".equals(attrName)) {
					adapter.dup();
					String literalValue = getLiteralString(attr);
					if (literalValue != null) {
						adapter.push(literalValue);
					} else {
						attr.getValue().writeOut(bc, Expression.MODE_REF);
						adapter.invokeStatic(Types.CASTER, new Method("toString", Types.STRING, new Type[] {Types.OBJECT}));
					}
					adapter.invokeVirtual(LOG_TAG_TYPE, SET_FILE);
				}
				else if ("application".equals(attrName)) {
					adapter.dup();
					String literalValue = getLiteralString(attr);
					if (literalValue != null) {
						adapter.push(Caster.toBoolean(literalValue, true));
					} else {
						attr.getValue().writeOut(bc, Expression.MODE_REF);
						adapter.invokeStatic(Types.CASTER, new Method("toBoolean", Type.BOOLEAN_TYPE, new Type[] {Types.OBJECT}));
					}
					adapter.invokeVirtual(LOG_TAG_TYPE, SET_APPLICATION);
				}
				else if ("exception".equals(attrName)) {
					adapter.dup();
					attr.getValue().writeOut(bc, Expression.MODE_REF);
					adapter.invokeVirtual(LOG_TAG_TYPE, SET_EXCEPTION);
				}
			}

			// Call doStartTag() method
			adapter.invokeVirtual(LOG_TAG_TYPE, DO_START_TAG);
			adapter.pop(); // Discard return value

			bc.visitLine(tag.getEnd());

		} catch (Exception e) {
			if (e instanceof TransformerException) throw (TransformerException) e;
			throw new TransformerException(bc, e, tag.getStart());
		}
	}

	private String getLiteralString(Attribute attr) {
		try {
			if (attr != null && attr.getValue() != null) {
				// Check if this is a literal expression
				if (attr.getValue() instanceof LitString) {
					LitString litString = (LitString) attr.getValue();
					return litString.getString();
				}
				// Fallback: try toString approach for other literal types
				String str = attr.getValue().toString();
				if (str.startsWith("\"") && str.endsWith("\"")) {
					return str.substring(1, str.length() - 1);
				}
				// For simple values without quotes that look like literals
				if (str.matches("^[a-zA-Z0-9_-]+$")) {
					return str;
				}
			}
		}
		catch (Exception e) {
			// Fall back to null if we can't get literal value
		}
		return null;
	}
}
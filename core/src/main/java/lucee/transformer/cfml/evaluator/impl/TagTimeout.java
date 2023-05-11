package lucee.transformer.cfml.evaluator.impl;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLibTag;

public final class TagTimeout extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag, TagLibTag tagLibTag, FunctionLib[] flibs) throws EvaluatorException {
		lucee.transformer.bytecode.statement.tag.TagTimeout tt = (lucee.transformer.bytecode.statement.tag.TagTimeout) tag;
		try {
			tt.init();
		}
		catch (TransformerException te) {
			EvaluatorException ee = new EvaluatorException(te.getMessage());
			ee.initCause(te);
			throw ee;
		}
	}
}
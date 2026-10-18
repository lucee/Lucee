package lucee.runtime.ai.anthropic;

import lucee.runtime.ai.AIModelSupport;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public final class ClaudeModel extends AIModelSupport {

	public ClaudeModel(Struct raw, String charset) throws PageException {
		super(Caster.toString(raw.get(KeyConstants._id)), raw, charset);
	}

	@Override
	public String getLabel() {
		String label = Caster.toString(raw.get("display_name", null), null);
		return label == null ? getName() : label;
	}
}

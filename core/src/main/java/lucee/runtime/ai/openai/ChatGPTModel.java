package lucee.runtime.ai.openai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.lang.StringUtil;
import lucee.runtime.ai.AIModelSupport;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class ChatGPTModel extends AIModelSupport {

	private static Map<String, String> labels = new ConcurrentHashMap<>();

	public ChatGPTModel(Struct raw, String charset) throws PageException {
		super(Caster.toString(raw.get(KeyConstants._id)), raw, charset);
	}

	@Override
	public String getLabel() {
		final String name = getName();
		String label = labels.get(name);
		if (label == null) {
			synchronized (labels) {
				label = labels.get(name);
				if (label == null) {
					StringBuilder sb = new StringBuilder();
					for (String str: ListUtil.listToList(name, '-', true)) {
						if (sb.length() > 0) sb.append('-');
						sb.append(StringUtil.ucFirst(str));
					}
					label = sb.toString();
					labels.put(name, label);
				}
			}
		}
		return label;
	}
}

package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PartsResponse implements Response {

	private final List<Part> parts;

	public PartsResponse(List<Part> parts) {
		this.parts = Collections.unmodifiableList(new ArrayList<>(parts));
	}

	@Override
	public String getAnswer() {
		if (parts.isEmpty()) return null;

		StringBuilder sb = new StringBuilder();
		String text;
		for (Part part: parts) {
			if (!part.isText()) continue;
			text = part.getAsString();
			if (text != null) sb.append(text);
		}
		return sb.length() > 0 ? sb.toString() : null;
	}

	@Override
	public long getTotalTokenUsed() {
		return 0;
	}

	@Override
	public List<Part> getAnswers() {
		return parts;
	}

	@Override
	public boolean isMultiPart() {
		return parts.size() > 1 || (parts.size() == 1 && !parts.get(0).isText());
	}

}

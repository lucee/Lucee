package lucee.runtime.ai;

import java.util.List;

public final class RequestSupport implements Request {

	private String question;
	private List<Part> parts;
	private boolean multiPart = false;

	public RequestSupport(String question) {
		this.question = question;
	}

	public RequestSupport(List<Part> parts) {
		this.parts = parts;
		multiPart = true;
	}

	@Override
	public String getQuestion() {
		return question;
	}

	@Override
	public List<Part> getQuestions() {
		return parts;
	}

	@Override
	public boolean isMultiPart() {
		return multiPart;
	}

	public static Request getInstance(String message, List<Part> parts) {
		if (parts != null) return new RequestSupport(parts);
		return new RequestSupport(message);
	}
}

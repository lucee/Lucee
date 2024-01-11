package lucee.runtime.ai;

public class Request {

	private String[] questions;

	public Request(String[] questions) {
		this.questions = questions;
	}

	public String[] getQuestions() {
		return questions;
	}
}

package lucee.runtime.ai;

public interface Response {
	public String getAnswer();

	public long getTotalTokenUsed();
}

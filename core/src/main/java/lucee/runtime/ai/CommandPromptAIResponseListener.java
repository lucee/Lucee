package lucee.runtime.ai;

public class CommandPromptAIResponseListener implements AIResponseListener {

	public static short OUT = 1;
	public static short ERR = 2;
	private short streamType;

	public CommandPromptAIResponseListener(short streamType) {
		this.streamType = streamType;
	}

	@Override
	public void listen(String part) {
		if (streamType == OUT) System.out.print(part);
		else System.err.print(part);
	}
}

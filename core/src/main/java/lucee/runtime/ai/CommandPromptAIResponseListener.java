package lucee.runtime.ai;

import java.io.PrintStream;

import lucee.runtime.exp.PageException;

public final class CommandPromptAIResponseListener implements AIResponseListener {

	public static short OUT = 1;
	public static short ERR = 2;
	private short streamType;

	public CommandPromptAIResponseListener(short streamType) {
		this.streamType = streamType;
	}

	@Override
	public void listen(String part, int chunkIndex, boolean isComplete) {
		PrintStream stream = streamType == OUT ? System.err : System.err;

		stream.print("-------------- chunk-index:" + chunkIndex + ";complete?" + isComplete + " --------------");
		stream.print(part);
	}

	@Override
	public void listen(Object part, String contentType, int chunkIndex, int partIndex, boolean isComplete) throws PageException {
		PrintStream stream = streamType == OUT ? System.err : System.err;

		stream.print("-------------- content-type:" + contentType + ";part-index:" + partIndex + ";chunk-index:" + chunkIndex + ";complete?" + isComplete + " --------------");
		stream.println(part);
	}
}

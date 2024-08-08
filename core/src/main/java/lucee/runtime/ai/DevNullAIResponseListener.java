package lucee.runtime.ai;

public class DevNullAIResponseListener implements AIResponseListener {

	public static final AIResponseListener INSTANCE = new DevNullAIResponseListener();

	@Override
	public void listen(String part) {
		// do nothing
	}

}

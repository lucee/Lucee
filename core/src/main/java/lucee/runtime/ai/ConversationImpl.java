package lucee.runtime.ai;

public class ConversationImpl implements Conversation {

	private Request req;
	private Response rsp;

	public ConversationImpl(Request req, Response rsp) {
		this.req = req;
		this.rsp = rsp;
	}

	@Override
	public Request getRequest() {
		return req;
	}

	@Override
	public Response getResponse() {
		return rsp;
	}

}

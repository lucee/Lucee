package lucee.runtime.gateway;

// FUTURE add to interface Gateway
public interface GatewaySupport extends Gateway {

	public void setThread(Thread thread);

	public Thread getThread();
}

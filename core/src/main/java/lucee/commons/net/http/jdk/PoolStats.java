package lucee.commons.net.http.jdk;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 * Statistics of the connections of the built-in HTTP engine. The JDK client does not expose its
 * connection pool, so the engine counts itself: a request in progress is a leased connection, a
 * pooled connection that was released with keep-alive is available until the keep-alive timeout of
 * the JDK client (jdk.httpclient.keepalive.timeout) expires.
 */
final class PoolStats {

	private static final long KEEP_ALIVE_MILLIS;
	private static final int MAX;

	static {
		long ka = 30;
		try {
			String str = System.getProperty("jdk.httpclient.keepalive.timeout");
			if (str != null) ka = Long.parseLong(str.trim());
		}
		catch (Exception e) {
		}
		KEEP_ALIVE_MILLIS = Math.max(0, ka) * 1000L;

		// the JDK client has no limit for the connections, only for the number of idle connections it keeps
		int max = 0;
		try {
			String str = System.getProperty("jdk.httpclient.connectionPoolSize");
			if (str != null) max = Integer.parseInt(str.trim());
		}
		catch (Exception e) {
		}
		MAX = max > 0 ? max : Integer.MAX_VALUE;
	}

	private static final class Route {
		private int leased;
		private final ArrayDeque<Long> idle = new ArrayDeque<>(); // release times of the idle connections, oldest first

		private void expire(long now) {
			Iterator<Long> it = idle.iterator();
			while (it.hasNext()) {
				if (now - it.next() >= KEEP_ALIVE_MILLIS) it.remove();
				else break;
			}
		}
	}

	private final Map<String, Route> routes = new ConcurrentHashMap<>();

	static String route(URI uri, ProxyData proxy) {
		String scheme = uri.getScheme() == null ? "http" : uri.getScheme().toLowerCase();
		int port = uri.getPort() == -1 ? ("https".equals(scheme) ? 443 : 80) : uri.getPort();
		String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
		String r = scheme + "://" + host + ":" + port;
		if (proxy != null) r += " via " + proxy.getServer() + ":" + (proxy.getPort() == -1 ? 80 : proxy.getPort());
		return r;
	}

	/**
	 * @return true when the JDK client keeps the connection of the response for the next request
	 */
	static boolean keepAlive(HttpResponse<?> rsp) {
		if (rsp.version() == HttpClient.Version.HTTP_1_1) {
			String conn = rsp.headers().firstValue("connection").orElse("");
			if (conn.toLowerCase().contains("close")) return false;
		}
		return true;
	}

	// all changes of a route are done inside compute of the map, so they are atomic with the removal of an unused route

	/**
	 * a request on the route starts, it uses an idle connection when there is one
	 */
	void lease(String route) {
		long now = System.currentTimeMillis();
		routes.compute(route, (k, r) -> {
			if (r == null) r = new Route();
			r.expire(now);
			r.idle.pollLast();
			r.leased++;
			return r;
		});
	}

	/**
	 * a request on the route is done
	 *
	 * @param keepAlive the connection is kept by the client for the next request
	 */
	void release(String route, boolean keepAlive) {
		long now = System.currentTimeMillis();
		routes.computeIfPresent(route, (k, r) -> {
			if (r.leased > 0) r.leased--;
			if (keepAlive && KEEP_ALIVE_MILLIS > 0 && r.idle.size() < MAX) r.idle.addLast(now);
			return r;
		});
	}

	void clearIdle() {
		for (String key: routes.keySet()) {
			routes.computeIfPresent(key, (k, r) -> {
				r.idle.clear();
				return r.leased == 0 ? null : r;
			});
		}
	}

	Struct toStruct() {
		long now = System.currentTimeMillis();
		int leased = 0, available = 0;
		Struct sctRoutes = new StructImpl();
		for (String key: routes.keySet()) {
			int[] la = new int[2];
			Route rr = routes.computeIfPresent(key, (k, r) -> {
				r.expire(now);
				la[0] = r.leased;
				la[1] = r.idle.size();
				// nothing in use and nothing kept, the route is no longer of interest
				return r.leased == 0 && r.idle.isEmpty() ? null : r;
			});
			if (rr == null) continue;
			leased += la[0];
			available += la[1];
			Struct sct = new StructImpl();
			sct.setEL("leased", la[0]);
			sct.setEL("available", la[1]);
			sct.setEL("pending", 0);
			sct.setEL("max", MAX);
			sct.setEL("route", key);
			sctRoutes.setEL(key, sct);
		}
		Struct sct = new StructImpl();
		sct.setEL("leased", leased);
		sct.setEL("available", available);
		sct.setEL("pending", 0);
		sct.setEL("max", MAX);
		sct.setEL("routes", sctRoutes);
		return sct;
	}
}

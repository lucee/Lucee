package lucee.runtime.config;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class RemoteClientFactory implements PropFactory<RemoteClient> {

	private static RemoteClientFactory instance;

	public static RemoteClientFactory getInstance() {
		if (instance == null) {
			instance = new RemoteClientFactory();
		}
		return instance;
	}

	@Override
	public RemoteClient evaluate(Config config, String name, Object val, short source) throws PageException {

		try {
			Struct client = Caster.toStruct(val);

			// type
			String type = ConfigFactoryImpl.getAttr(config, client, "type");
			if (StringUtil.isEmpty(type)) type = "web";
			// url
			String url = ConfigFactoryImpl.getAttr(config, client, "url");
			String label = ConfigFactoryImpl.getAttr(config, client, "label");
			if (StringUtil.isEmpty(label)) label = url;
			String sUser = ConfigFactoryImpl.getAttr(config, client, "serverUsername");
			String sPass = ConfigUtil.decrypt(ConfigFactoryImpl.getAttr(config, client, "serverPassword"));
			String aPass = ConfigUtil.decrypt(ConfigFactoryImpl.getAttr(config, client, "adminPassword"));
			String aCode = ConfigUtil.decrypt(ConfigFactoryImpl.getAttr(config, client, "securityKey"));
			// if(aCode!=null && aCode.indexOf('-')!=-1)continue;
			String usage = ConfigFactoryImpl.getAttr(config, client, "usage");
			if (usage == null) usage = "";

			String pUrl = ConfigFactoryImpl.getAttr(config, client, "proxyServer");
			int pPort = Caster.toIntValue(ConfigFactoryImpl.getAttr(config, client, "proxyPort"), -1);
			String pUser = ConfigFactoryImpl.getAttr(config, client, "proxyUsername");
			String pPass = ConfigUtil.decrypt(ConfigFactoryImpl.getAttr(config, client, "proxyPassword"));
			ProxyData pd = null;
			if (!StringUtil.isEmpty(pUrl, true)) {
				pd = new ProxyDataImpl();
				pd.setServer(pUrl);
				if (!StringUtil.isEmpty(pUser)) {
					pd.setUsername(pUser);
					pd.setPassword(pPass);
				}
				if (pPort > 0) pd.setPort(pPort);
			}
			return new RemoteClientImpl(label, type, url, sUser, sPass, aPass, pd, aCode, usage);

		}
		catch (Exception ex) {
			throw Caster.toPageException(ex);
		}

	}

	@Override
	public Struct schema(Prop<RemoteClient> prop) {
		Struct s = new StructImpl(Struct.TYPE_LINKED);
		s.setEL(KeyConstants._type, "object");
		s.setEL(KeyConstants._description, "Defines a remote Lucee instance or client used for task distribution and clustering.");

		Struct props = new StructImpl(Struct.TYPE_LINKED);
		s.setEL(KeyConstants._properties, props);

		// type
		props.setEL(KeyImpl.init("type"), PropFactory.createSimple("string", "The type of remote client (e.g., 'web')."));

		// url
		props.setEL(KeyImpl.init("url"), PropFactory.createSimple("string", "The full URL of the remote Lucee instance."));

		// label
		props.setEL(KeyImpl.init("label"), PropFactory.createSimple("string", "A descriptive name for this remote client."));

		// Authentication
		props.setEL(KeyImpl.init("serverUsername"), PropFactory.createSimple("string", "Username for server-level authentication."));
		props.setEL(KeyImpl.init("serverPassword"), PropFactory.createSimple("string", "Password for server-level authentication."));
		props.setEL(KeyImpl.init("adminPassword"), PropFactory.createSimple("string", "The administrator password for the remote instance."));
		props.setEL(KeyImpl.init("securityKey"), PropFactory.createSimple("string", "The security key (API key) for the remote instance."));

		// usage
		props.setEL(KeyImpl.init("usage"), PropFactory.createSimple("string", "Defines the intended use (e.g., 'cluster', 'task')."));

		// Proxy Settings
		props.setEL(KeyImpl.init("proxyServer"), PropFactory.createSimple("string", "Hostname or IP of the proxy server."));
		props.setEL(KeyImpl.init("proxyPort"), PropFactory.createSimple("integer", "The port number of the proxy server."));
		props.setEL(KeyImpl.init("proxyUsername"), PropFactory.createSimple("string", "Username for proxy authentication."));
		props.setEL(KeyImpl.init("proxyPassword"), PropFactory.createSimple("string", "Password for proxy authentication."));

		// Required fields
		Array required = new ArrayImpl();
		required.appendEL("url");
		s.setEL(KeyImpl.init("required"), required);

		return s;
	}

	@Override
	public Object resolvedValue(RemoteClient defaultValue) {
		return defaultValue;
	}

}

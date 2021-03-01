package lucee.runtime.extension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.osgi.OSGiUtil;

public class ExtensionDefintion {

	private String id;
	private Map<String, String> params = new HashMap<String, String>();
	private Resource source;
	private Config config;
	private RHExtension rhe;

	public ExtensionDefintion() {}

	public ExtensionDefintion(String id) {
		this.id = id;
	}

	public ExtensionDefintion(String id, String version) {
		this.id = id;
		setParam("version", version);
	}

	/*
	 * public static ExtensionDefintion getInstanceEL(Config config, Element el) { try { return
	 * getInstance(config, el); } catch (Exception e) { return null; } }
	 * 
	 * 
	 * public static ExtensionDefintion getInstance(Config config, Element el) throws PageException,
	 * IOException, BundleException { String id=el.getAttribute("id"); String
	 * version=el.getAttribute("version"); if(!StringUtil.isEmpty(id) && !StringUtil.isEmpty(version)) {
	 * Resource res = RHExtension.toResource(config, el); ExtensionDefintion ed = new
	 * ExtensionDefintion(id, version); ed.setSource(config, res); return ed; }
	 * 
	 * RHExtension rhe=new RHExtension(config,el); id=rhe.getId(); version=rhe.getVersion();
	 * 
	 * ExtensionDefintion ed=new ExtensionDefintion(id,version); ed.setSource(rhe); return ed; }
	 */

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getSymbolicName() {
		String sn = params.get("symbolic-name");
		if (StringUtil.isEmpty(sn, true)) return getId();
		return sn.trim();
	}

	public void setParam(String name, String value) {
		params.put(name, value);
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getVersion() {
		String version = params.get("version");
		if (StringUtil.isEmpty(version)) version = params.get("extension-version");
		if (StringUtil.isEmpty(version)) return null;
		return version;
	}

	public Version getSince() {
		String since = params.get("since");
		if (StringUtil.isEmpty(since)) return null;
		return OSGiUtil.toVersion(since, null);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getId());
		Iterator<Entry<String, String>> it = params.entrySet().iterator();
		Entry<String, String> e;
		while (it.hasNext()) {
			e = it.next();
			sb.append(';').append(e.getKey()).append('=').append(e.getValue());
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ExtensionDefintion) {
			ExtensionDefintion ed = (ExtensionDefintion) other;
			if (!ed.getId().equalsIgnoreCase(getId())) return false;
			if (ed.getVersion() == null || getVersion() == null) return true;
			return ed.getVersion().equalsIgnoreCase(getVersion());
		}
		else if (other instanceof RHExtension) {
			RHExtension ed = (RHExtension) other;
			if (!ed.getId().equalsIgnoreCase(getId())) return false;
			if (ed.getVersion() == null || getVersion() == null) return true;
			return ed.getVersion().equalsIgnoreCase(getVersion());
		}
		return false;
	}

	public void setSource(RHExtension rhe) {
		this.rhe = rhe;
	}

	public void setSource(Config config, Resource source) {
		this.config = config;
		this.source = source;
	}

	public RHExtension toRHExtension() throws PageException, IOException, BundleException, ConverterException {
		if (rhe != null) return rhe;

		if (source == null) {
			// MUST try to load the Extension
			throw new ApplicationException("ExtensionDefinition does not contain the necessary data to create the requested object.");
		}
		rhe = new RHExtension(config, source, false);
		return rhe;
	}

	public Resource getSource() throws ApplicationException {
		if (source != null) return source;
		if (rhe != null) return rhe.getExtensionFile();
		throw new ApplicationException("ExtensionDefinition does not contain a source.");
	}
}

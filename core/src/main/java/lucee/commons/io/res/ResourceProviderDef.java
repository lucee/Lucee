package lucee.commons.io.res;

import java.util.Map;

import lucee.runtime.db.ClassDefinition;

public class ResourceProviderDef {

	private String scheme;
	private ClassDefinition cd;
	private Map<String, String> args;

	public ResourceProviderDef(String scheme, ClassDefinition cd, Map<String, String> args) {
		this.scheme = scheme;
		this.cd = cd;
		this.args = args;
		// TODO Auto-generated constructor stub
	}

	public String getScheme() {
		return scheme;
	}

	public ClassDefinition getClassDefinition() {
		return cd;
	}

	public Map<String, String> getArgs() {
		return args;
	}
}

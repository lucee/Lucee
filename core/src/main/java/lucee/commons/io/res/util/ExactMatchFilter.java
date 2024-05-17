package lucee.commons.io.res.util;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderPro;
import lucee.commons.io.res.filter.ResourceNameFilter;

public class ExactMatchFilter implements ResourceNameFilter {

	private String name;

	public ExactMatchFilter(String name) {
		this.name = name == null ? "" : name;
	}

	@Override
	public boolean accept(Resource parent, String name) {
		return this.name.equalsIgnoreCase(name);
	}

	public static boolean allowMatching(ResourceProvider provider) {

		if (provider instanceof ResourceProviderPro) {
			return ((ResourceProviderPro) provider).allowMatching();
		}

		return false;
	}
}
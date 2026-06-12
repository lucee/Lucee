package lucee.runtime.config.maven.extensionlist;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lucee.runtime.config.maven.MavenUpdateProvider.Repository;

public final class CompositeExtensionLister implements ExtensionLister {

	private final String name;
	private final boolean fallback;
	private final ExtensionLister[] listers;
	private final ConcurrentHashMap<String, ExtensionLister> lastSuccessful = new ConcurrentHashMap<>();

	public CompositeExtensionLister(boolean fallback, ExtensionLister... listers) {
		this.fallback = fallback;
		this.listers = listers;
		this.name = buildName(fallback, listers);
	}

	private static String buildName(boolean fallback, ExtensionLister[] listers) {
		if (listers.length == 0) return fallback ? "empty-fallback" : "empty-union";
		String separator = fallback ? "-then-" : "+";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < listers.length; i++) {
			if (i > 0) sb.append(separator);
			sb.append(listers[i].getName());
		}
		return sb.toString();
	}

	private static String cacheKey(Repository repo, String groupId) {
		return repo.getUrl() + "|" + groupId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<String> list(Repository repo, String groupId) throws IOException {
		if (fallback) return listFallback(repo, groupId);
		return listUnion(repo, groupId);
	}

	private Set<String> listFallback(Repository repo, String groupId) throws IOException {
		String key = cacheKey(repo, groupId);
		ExtensionLister cached = lastSuccessful.get(key);
		if (cached != null) {
			try {
				Set<String> result = cached.list(repo, groupId);
				if (result != null && !result.isEmpty()) {
					ExtensionListUtil.logDebug(getName(), "using cached lister [" + cached.getName() + "] for group [" + groupId + "]");
					return result;
				}
				lastSuccessful.remove(key, cached);
				ExtensionListUtil.logDebug(getName(), "cached lister [" + cached.getName() + "] returned no artifacts for group [" + groupId + "], trying all listers");
			}
			catch (IOException e) {
				lastSuccessful.remove(key, cached);
				ExtensionListUtil.logWarn(getName(), "cached lister [" + cached.getName() + "] failed for group [" + groupId + "], trying all listers", e);
			}
		}

		ExtensionListUtil.logInfo(getName(), "listing artifacts for group [" + groupId + "] using " + listers.length + " lister(s), fallback=" + fallback);
		IOException last = null;
		for (ExtensionLister lister: listers) {
			try {
				ExtensionListUtil.logDebug(getName(), "trying lister [" + lister.getName() + "] for group [" + groupId + "]");
				Set<String> result = lister.list(repo, groupId);
				if (result != null && !result.isEmpty()) {
					lastSuccessful.put(key, lister);
					ExtensionListUtil.logInfo(getName(), "lister [" + lister.getName() + "] returned " + result.size() + " artifacts for group [" + groupId + "]");
					return result;
				}
				ExtensionListUtil.logDebug(getName(), "lister [" + lister.getName() + "] returned no artifacts for group [" + groupId + "], trying next");
			}
			catch (IOException e) {
				last = e;
				ExtensionListUtil.logWarn(getName(), "lister [" + lister.getName() + "] failed for group [" + groupId + "], trying next", e);
			}
		}
		if (last != null) throw last;
		ExtensionListUtil.logInfo(getName(), "no artifacts found for group [" + groupId + "]");
		return new HashSet<>();
	}

	private Set<String> listUnion(Repository repo, String groupId) throws IOException {
		ExtensionListUtil.logInfo(getName(), "listing artifacts for group [" + groupId + "] using " + listers.length + " lister(s), fallback=" + fallback);
		Set<String> merged = new HashSet<>();
		IOException last = null;
		for (ExtensionLister lister: listers) {
			try {
				ExtensionListUtil.logDebug(getName(), "merging results from lister [" + lister.getName() + "] for group [" + groupId + "]");
				merged.addAll(lister.list(repo, groupId));
			}
			catch (IOException e) {
				last = e;
				ExtensionListUtil.logWarn(getName(), "lister [" + lister.getName() + "] failed for group [" + groupId + "]", e);
			}
		}
		if (merged.isEmpty() && last != null) throw last;
		ExtensionListUtil.logInfo(getName(), "merged " + merged.size() + " artifacts for group [" + groupId + "]");
		return merged;
	}

}

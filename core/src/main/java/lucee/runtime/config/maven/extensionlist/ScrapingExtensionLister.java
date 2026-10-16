package lucee.runtime.config.maven.extensionlist;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import lucee.runtime.config.maven.HtmlDirectoryScraper;
import lucee.runtime.config.maven.MavenUpdateProvider.Repository;

public final class ScrapingExtensionLister implements ExtensionLister {

	private static final HtmlDirectoryScraper SCRAPER = new HtmlDirectoryScraper();

	@Override
	public String getName() {
		return "scraping";
	}

	@Override
	public Set<String> list(Repository repo, String groupId) throws IOException {
		ExtensionListUtil.validateGroupId(groupId);
		String url = repo.getUrl() + groupId.replace('.', '/') + "/";
		ExtensionListUtil.logInfo(getName(), "listing artifacts for group [" + groupId + "] from [" + url + "]");
		Set<String> set = new HashSet<>();
		try {
			SCRAPER.getSubfolderLinks(url, set);
			ExtensionListUtil.logInfo(getName(), "found " + set.size() + " artifacts for group [" + groupId + "]");
			return set;
		}
		catch (IOException e) {
			ExtensionListUtil.logWarn(getName(), "failed listing artifacts for group [" + groupId + "] from [" + url + "]", e);
			throw e;
		}
	}

}

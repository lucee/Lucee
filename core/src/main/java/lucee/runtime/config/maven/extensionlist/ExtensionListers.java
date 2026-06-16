package lucee.runtime.config.maven.extensionlist;

import lucee.commons.lang.StringUtil;

public final class ExtensionListers {

	public static final ExtensionLister SCRAPING = new ScrapingExtensionLister();
	public static final ExtensionLister GROUP_METADATA = new GroupMetadataExtensionLister();
	public static final ExtensionLister SOLR_SEARCH = new SolrSearchExtensionLister();
	public static final ExtensionLister EMPTY = new EmptyExtensionLister();
	public static final ExtensionLister DEFAULT = new CompositeExtensionLister(true, GROUP_METADATA, SCRAPING, SOLR_SEARCH);
	public static final ExtensionLister LUCEE = new CompositeExtensionLister(true, GROUP_METADATA, SCRAPING);
	public static final ExtensionLister CENTRAL = new CompositeExtensionLister(true, SCRAPING, SOLR_SEARCH);

	private static final ExtensionLister[] KNOWN = { SCRAPING, GROUP_METADATA, SOLR_SEARCH, EMPTY, DEFAULT, LUCEE, CENTRAL };

	private ExtensionListers() {}

	public static ExtensionLister resolve(String listingMode, String repoUrl) {
		if (!StringUtil.isEmpty(listingMode, true)) {
			if ("central-search".equalsIgnoreCase(listingMode)) return SOLR_SEARCH;
			for (ExtensionLister lister: KNOWN) {
				if (lister.getName().equalsIgnoreCase(listingMode)) return lister;
			}
			return DEFAULT;
		}
		if (!StringUtil.isEmpty(repoUrl, true)) {
			String url = repoUrl.toLowerCase();
			if (url.contains("maven-central.storage-download.googleapis.com")) return EMPTY;
			if (url.contains("repo1.maven.org")) return CENTRAL;
			if (url.contains("maven.lucee-services.com") || url.contains("cdn.lucee.org")) return LUCEE;
		}
		return DEFAULT;
	}

}

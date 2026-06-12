package lucee.runtime.config.maven.extensionlist;

import lucee.commons.lang.StringUtil;

public final class ExtensionListers {

	public static final ExtensionLister SCRAPING = new ScrapingExtensionLister();
	public static final ExtensionLister GROUP_METADATA = new GroupMetadataExtensionLister();
	public static final ExtensionLister SOLR_SEARCH = new SolrSearchExtensionLister();
	public static final ExtensionLister DEFAULT = new CompositeExtensionLister(true, GROUP_METADATA, SOLR_SEARCH, SCRAPING);
	public static final ExtensionLister LUCEE = new CompositeExtensionLister(true, GROUP_METADATA, SCRAPING);
	public static final ExtensionLister CENTRAL = new CompositeExtensionLister(true, SOLR_SEARCH, SCRAPING);

	private static final ExtensionLister[] KNOWN = { SCRAPING, GROUP_METADATA, SOLR_SEARCH, DEFAULT, LUCEE, CENTRAL };

	private ExtensionListers() {}

	public static ExtensionLister resolve(String listingMode, String repoUrl) {
		if (!StringUtil.isEmpty(listingMode, true)) {
			if ("central-search".equalsIgnoreCase(listingMode)) return SOLR_SEARCH;
			for (ExtensionLister lister: KNOWN) {
				if (lister.getName().equalsIgnoreCase(listingMode)) return lister;
			}
			return DEFAULT;
		}
		return DEFAULT;
	}

}

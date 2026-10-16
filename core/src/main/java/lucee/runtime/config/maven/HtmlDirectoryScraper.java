package lucee.runtime.config.maven;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.httpclient.HTTPResponse4Impl;

public class HtmlDirectoryScraper {

	private static final Pattern LINK_PATTERN = Pattern.compile("<a\\s+href=\"([^\"]+)\"");

	/**
	 * Scrapes HTML directory listing and returns all subfolder links
	 * 
	 * @param url The URL to scrape
	 * @return List of href values that point to subfolders
	 * @throws IOException if network request fails
	 */
	public void getSubfolderLinks(String url, Set<String> set) throws IOException {
		String html = fetchUrl(url);
		if (html == null) return;
		extractSubfolderLinks(html, set);
	}

	/**
	 * Extracts subfolder links from HTML content Links must: not contain "://", not be "..", and end
	 * with "/"
	 * 
	 * @param html The HTML content to parse
	 * @return List of subfolder href values
	 */
	public void extractSubfolderLinks(String html, Set<String> set) {

		Matcher matcher = LINK_PATTERN.matcher(html);
		while (matcher.find()) {
			String href = matcher.group(1);

			if (isSubfolderLink(href)) {
				if (href.endsWith("/")) set.add(href.substring(0, href.length() - 1));
				else set.add(href);
			}
		}
	}

	/**
	 * Checks if a link href points to a subfolder
	 * 
	 * @param href The href value to check
	 * @return true if it's a subfolder link
	 */
	private boolean isSubfolderLink(String href) {
		return !href.contains("://") && // Not an absolute URL
				!href.contains("../") && // Not parent directory
				href.endsWith("/"); // Ends with slash (directory)
	}

	/**
	 * Fetches HTML content from URL
	 * 
	 * @param url The URL to fetch
	 * @return HTML content as string
	 * @throws IOException if request fails
	 */
	private String fetchUrl(String url) throws IOException {
		int[] statusCode = new int[1];
		String body = fetchSingle(url, statusCode);

		// if has /index.html
		if (StringUtil.isEmpty(body, true) && url.endsWith("/")) {
			int[] indexStatusCode = new int[1];
			body = fetchSingle(url + "index.html", indexStatusCode);
			if (indexStatusCode[0] == 404) return null;
			if (indexStatusCode[0] > 0) throw new IOException("HTTP " + indexStatusCode[0] + " for URL: " + url);
			return StringUtil.isEmpty(body, true) ? null : body;
		}

		if (statusCode[0] > 0) throw new IOException("HTTP " + statusCode[0] + " for URL: " + url);
		return body;
	}

	private String fetchSingle(String url, int[] statusCode) throws IOException {
		HTTPResponse4Impl response = HTTPEngine.get(new URL(url), null, null, 2000, 10000, 30000, true, null, "HTML-Directory-Scraper/1.0", null, null, true);
		try {
			int sc = response.getStatusCode();
			if (sc != 200) {
				if (sc != 404) statusCode[0] = sc;
				return null;
			}
			return IOUtil.toString(response.getContentAsStream(), StandardCharsets.UTF_8);
		}
		finally {
			response.close();
		}
	}

	// Example usage
	/*
	 * public static void main(String[] args) { HtmlDirectoryScraper scraper = new
	 * HtmlDirectoryScraper(); String url = "https://repo1.maven.org/maven2/org/lucee/";
	 * 
	 * try { Set<String> subfolders = new HashSet<>(); scraper.getSubfolderLinks(url, subfolders);
	 * 
	 * System.out.println("Found " + subfolders.size() + " subfolders:"); for (String folder:
	 * subfolders) { System.out.println("  " + folder); }
	 * 
	 * } catch (Exception e) { System.err.println("Error: " + e.getMessage()); } }
	 */
}
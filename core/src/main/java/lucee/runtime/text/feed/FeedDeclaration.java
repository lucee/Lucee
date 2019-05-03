/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.text.feed;

import java.util.HashMap;
import java.util.Map;

import lucee.commons.lang.StringUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.util.ListUtil;

public class FeedDeclaration {

	private static Map<String, FeedDeclaration> declarations = new HashMap<String, FeedDeclaration>();
	private static FeedDeclaration defaultDeclaration;
	static {

		// RSS 2.0
		Map<String, El> decl = new HashMap<String, El>();
		decl.put("rss", new El(El.QUANTITY_1, new Attr("version")));
		// rss.channel *
		decl.put("rss.channel.item", new El(El.QUANTITY_0_N, true));
		decl.put("rss.channel.category", new El(El.QUANTITY_0_N, new Attr("domain")));
		decl.put("rss.channel.cloud",
				new El(El.QUANTITY_AUTO, new Attr[] { new Attr("domain"), new Attr("port"), new Attr("path"), new Attr("registerProcedure"), new Attr("protocol") }));
		decl.put("rss.channel.image", new El(El.QUANTITY_AUTO));

		// rss.channel.item *
		decl.put("rss.channel.item.author", new El(El.QUANTITY_0_1));
		decl.put("rss.channel.item.category", new El(El.QUANTITY_0_N, new Attr("domain")));
		decl.put("rss.channel.item.comments", new El(El.QUANTITY_0_1));
		decl.put("rss.channel.item.enclosure", new El(El.QUANTITY_0_N, new Attr[] { new Attr("url"), new Attr("length"), new Attr("type") }));
		decl.put("rss.channel.item.guid", new El(El.QUANTITY_0_1, new Attr("isPermaLink", "true")));
		decl.put("rss.channel.item.pubDate", new El(El.QUANTITY_0_1));
		decl.put("rss.channel.item.source", new El(El.QUANTITY_0_1, new Attr("url")));

		// rss.channel.item +
		decl.put("rss.channel.item.title", new El(El.QUANTITY_1));
		decl.put("rss.channel.item.description", new El(El.QUANTITY_1, new Attr[] {}));// muss auch sein das er value anlegt

		// rss.channel.item ?
		decl.put("rss.channel.item.link", new El(El.QUANTITY_0_1));// muss auch sein das er value anlegt

		// rss.channel.item 1
		decl.put("rss.channel.image.url", new El(El.QUANTITY_1));
		decl.put("rss.channel.image.title", new El(El.QUANTITY_1));
		decl.put("rss.channel.image.link", new El(El.QUANTITY_1));

		// rss.channel.item *
		decl.put("rss.channel.image.width", new El(El.QUANTITY_0_N));
		decl.put("rss.channel.image.height", new El(El.QUANTITY_0_N));
		decl.put("rss.channel.image.description", new El(El.QUANTITY_0_N));

		decl.put("rss.channel.textInput.title", new El(El.QUANTITY_1));
		decl.put("rss.channel.textInput.description", new El(El.QUANTITY_1));
		decl.put("rss.channel.textInput.name", new El(El.QUANTITY_1));
		decl.put("rss.channel.textInput.link", new El(El.QUANTITY_1));

		FeedDeclaration fd = new FeedDeclaration(decl, "rss.channel", "rss_2.0");
		declarations.put("rss", fd);
		declarations.put("rss_2.0", fd);
		declarations.put("rss_2", fd);

		// RSS 0.92
		decl = new HashMap<String, El>();
		decl.put("rss", new El(El.QUANTITY_1, new Attr("version")));
		// rss.channel *
		decl.put("rss.channel.item", new El(El.QUANTITY_0_N));
		decl.put("rss.channel.category", new El(El.QUANTITY_0_N, new Attr("domain")));
		decl.put("rss.channel.cloud",
				new El(El.QUANTITY_AUTO, new Attr[] { new Attr("domain"), new Attr("port"), new Attr("path"), new Attr("registerProcedure"), new Attr("protocol") }));
		decl.put("rss.channel.image", new El(El.QUANTITY_AUTO));

		// rss.channel.item *
		decl.put("rss.channel.item.author", new El(El.QUANTITY_0_1));
		decl.put("rss.channel.item.category", new El(El.QUANTITY_0_N, new Attr("domain", "leer")));
		decl.put("rss.channel.item.comments", new El(El.QUANTITY_0_1));
		decl.put("rss.channel.item.enclosure", new El(El.QUANTITY_0_N, new Attr[] { new Attr("url"), new Attr("length"), new Attr("type") }));
		decl.put("rss.channel.item.guid", new El(El.QUANTITY_0_1, new Attr("isPermaLink", "true")));
		decl.put("rss.channel.item.pubDate", new El(El.QUANTITY_0_1));
		decl.put("rss.channel.item.source", new El(El.QUANTITY_0_1, new Attr("url")));

		// rss.channel.item +
		decl.put("rss.channel.item.title", new El(El.QUANTITY_1));

		// rss.channel.item ?
		decl.put("rss.channel.item.link", new El(El.QUANTITY_0_1));// muss auch sein das er value anlegt

		// rss.channel.item 1
		decl.put("rss.channel.image.url", new El(El.QUANTITY_1));
		decl.put("rss.channel.image.title", new El(El.QUANTITY_1));
		decl.put("rss.channel.image.link", new El(El.QUANTITY_1));

		// rss.channel.item *
		decl.put("rss.channel.image.width", new El(El.QUANTITY_0_N));
		decl.put("rss.channel.image.height", new El(El.QUANTITY_0_N));
		decl.put("rss.channel.image.description", new El(El.QUANTITY_0_N));

		decl.put("rss.channel.textInput.title", new El(El.QUANTITY_1));
		decl.put("rss.channel.textInput.description", new El(El.QUANTITY_1));
		decl.put("rss.channel.textInput.name", new El(El.QUANTITY_1));
		decl.put("rss.channel.textInput.link", new El(El.QUANTITY_1));

		fd = new FeedDeclaration(decl, "rss.channel", "rss_0.92");
		declarations.put("rss_0.92", fd);

		// RSS 0.91
		decl = new HashMap<String, El>();
		decl.put("rss", new El(El.QUANTITY_1, new Attr("version")));
		// rss.channel *
		decl.put("rss.channel.item", new El(El.QUANTITY_0_N));
		decl.put("rss.channel.category", new El(El.QUANTITY_0_N, new Attr("domain")));
		decl.put("rss.channel.cloud",
				new El(El.QUANTITY_AUTO, new Attr[] { new Attr("domain"), new Attr("port"), new Attr("path"), new Attr("registerProcedure"), new Attr("protocol") }));
		decl.put("rss.channel.image", new El(El.QUANTITY_AUTO));

		// rss.channel.item *
		decl.put("rss.channel.item.author", new El(El.QUANTITY_0_1));
		decl.put("rss.channel.item.category", new El(El.QUANTITY_0_N, new Attr("domain", "leer")));
		decl.put("rss.channel.item.comments", new El(El.QUANTITY_0_1));
		decl.put("rss.channel.item.enclosure", new El(El.QUANTITY_0_N, new Attr[] { new Attr("url"), new Attr("length"), new Attr("type") }));
		decl.put("rss.channel.item.guid", new El(El.QUANTITY_0_1, new Attr("isPermaLink", "true")));
		decl.put("rss.channel.item.pubDate", new El(El.QUANTITY_0_1));
		decl.put("rss.channel.item.source", new El(El.QUANTITY_0_1, new Attr("url")));

		// rss.channel.item 1
		decl.put("rss.channel.image.url", new El(El.QUANTITY_1));
		decl.put("rss.channel.image.title", new El(El.QUANTITY_1));
		decl.put("rss.channel.image.link", new El(El.QUANTITY_1));

		// rss.channel.item *
		decl.put("rss.channel.image.width", new El(El.QUANTITY_0_N));
		decl.put("rss.channel.image.height", new El(El.QUANTITY_0_N));
		decl.put("rss.channel.image.description", new El(El.QUANTITY_0_N));

		decl.put("rss.channel.textInput.title", new El(El.QUANTITY_1));
		decl.put("rss.channel.textInput.description", new El(El.QUANTITY_1));
		decl.put("rss.channel.textInput.name", new El(El.QUANTITY_1));
		decl.put("rss.channel.textInput.link", new El(El.QUANTITY_1));

		fd = new FeedDeclaration(decl, "rss.channel", "rss_0.91");
		declarations.put("rss_0.91", fd);

		// ATOM
		decl = new HashMap<String, El>();
		decl.put("feed", new El(El.QUANTITY_1, new Attr("version")));

		decl.put("feed.author.name", new El(El.QUANTITY_1));
		decl.put("feed.title", new El(El.QUANTITY_1, new Attr[] {}));
		decl.put("feed.link", new El(El.QUANTITY_0_N));
		decl.put("feed.entry", new El(El.QUANTITY_0_N));
		decl.put("feed.entry.author", new El(El.QUANTITY_0_N));
		decl.put("feed.entry.contributor", new El(El.QUANTITY_0_N));
		decl.put("feed.entry.content", new El(El.QUANTITY_0_N));
		decl.put("feed.entry.link", new El(El.QUANTITY_0_N));
		decl.put("feed.entry.title", new El(El.QUANTITY_1, new Attr[] {}));
		decl.put("feed.entry.summary", new El(El.QUANTITY_1, new Attr[] { new Attr("type", "text/plain"), new Attr("mode", "xml") }));

		fd = new FeedDeclaration(decl, "feed", "atom");
		declarations.put("atom", fd);

		defaultDeclaration = new FeedDeclaration(new HashMap<String, El>(), null, "custom");
	}

	private Map<String, El> declaration;
	private Key[] entryLevel;
	private String type;

	private FeedDeclaration(Map<String, El> declaration, String entryLevel, String type) {
		this.declaration = declaration;

		if (!StringUtil.isEmpty(entryLevel)) {
			Array array = ListUtil.listToArray(entryLevel, '.');
			this.entryLevel = new Collection.Key[array.size()];
			for (int i = 0; i < this.entryLevel.length; i++) {
				this.entryLevel[i] = KeyImpl.toKey(array.get(i + 1, null), null);
			}
		}
		else this.entryLevel = new Collection.Key[0];
		this.type = type;
	}

	public static FeedDeclaration getInstance(String decName) {
		FeedDeclaration fd = declarations.get(decName);
		if (fd != null) return fd;

		if (StringUtil.startsWithIgnoreCase(decName, "rss")) fd = declarations.get("rss");
		if (fd != null) return fd;

		if (StringUtil.startsWithIgnoreCase(decName, "atom")) fd = declarations.get("atom");
		if (fd != null) return fd;

		return defaultDeclaration;
	}

	class FeedDeclarationItem {

	}

	/**
	 * @return the declaration
	 */
	public Map<String, El> getDeclaration() {
		return declaration;
	}

	/**
	 * @return the entryLevel
	 */
	public Key[] getEntryLevel() {
		return entryLevel;
	}

	public String getType() {
		return type;
	}
}
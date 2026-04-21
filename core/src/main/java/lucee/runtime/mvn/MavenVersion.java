/*
 * Maven version parsing and comparison for Lucee.
 *
 * The tokenisation + comparison algorithm here is ported from Apache
 * Maven Resolver's GenericVersion (maven-resolver-util 2.0.16), which
 * is licensed under the Apache License, Version 2.0:
 *
 *   https://github.com/apache/maven-resolver/blob/maven-resolver-2.0.16/
 *     maven-resolver-util/src/main/java/org/eclipse/aether/util/version/
 *     GenericVersion.java
 *
 * Maven version semantics are subtle enough that this is effectively
 * the only correct implementation — we port it rather than reinvent.
 * See test/general/MavenVersionParsing.cfc for the validation corpus.
 */
package lucee.runtime.mvn;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * A maven version, tokenised into comparable segments.
 *
 * Parses any input string using the canonical maven rules:
 * delimiters <code>.</code>/<code>-</code>/<code>_</code> separate
 * segments; digit/letter transitions also separate; leading zeros
 * stripped from numbers; well-known qualifier names
 * (alpha, beta, milestone, rc, cr, snapshot, ga, final, release, sp)
 * get fixed ranks; <code>min</code>/<code>max</code> at end-of-string
 * are sentinels below/above everything.
 */
public final class MavenVersion implements Comparable<MavenVersion> {

	private final String version;
	private final List<Item> items;

	public MavenVersion(String version) {
		if (version == null) throw new IllegalArgumentException("version cannot be null");
		this.version = version;
		List<Item> list = new ArrayList<>();
		for (Tokenizer t = new Tokenizer(version); t.next();) {
			list.add(t.toItem());
		}
		trimPadding(list);
		this.items = Collections.unmodifiableList(list);
	}

	public String asString() {
		return version;
	}

	public List<Item> asItems() {
		return items;
	}

	/**
	 * True if any segment is a pre-release qualifier (alpha, beta,
	 * milestone, rc/cr, snapshot). Used by range resolution to exclude
	 * pre-releases from {@link MavenVersionRange#pickHighest(List)} unless
	 * the range spec itself names a pre-release bound.
	 */
	public boolean isPreRelease() {
		for (Item it: items) {
			if (it.kind == Item.KIND_QUALIFIER && ((Integer) it.value).intValue() < 0) return true;
		}
		return false;
	}

	// -------------------------------------------------------------------
	// Padding trim (visible for testing)
	// -------------------------------------------------------------------

	static void trimPadding(List<Item> items) {
		Boolean number = null;
		int end = items.size() - 1;
		for (int i = end; i > 0; i--) {
			Item item = items.get(i);
			if (!Boolean.valueOf(item.isNumber()).equals(number)) {
				end = i;
				number = item.isNumber();
			}
			if (end == i && (i == items.size() - 1 || items.get(i - 1).isNumber() == item.isNumber()) && item.compareTo(null) == 0) {
				items.remove(i);
				end--;
			}
		}
	}

	// -------------------------------------------------------------------
	// Comparison
	// -------------------------------------------------------------------

	@Override
	public int compareTo(MavenVersion obj) {
		final List<Item> these = items;
		final List<Item> those = obj.items;
		boolean number = true;

		for (int index = 0;; index++) {
			if (index >= these.size() && index >= those.size()) return 0;
			if (index >= these.size()) return -comparePadding(those, index, null);
			if (index >= those.size()) return comparePadding(these, index, null);

			Item thisItem = these.get(index);
			Item thatItem = those.get(index);

			if (thisItem.isNumber() != thatItem.isNumber()) {
				if (index == 0) return thisItem.compareTo(thatItem);
				if (number == thisItem.isNumber()) return comparePadding(these, index, number);
				return -comparePadding(those, index, number);
			}
			int rel = thisItem.compareTo(thatItem);
			if (rel != 0) return rel;
			number = thisItem.isNumber();
		}
	}

	private static int comparePadding(List<Item> items, int index, Boolean number) {
		int rel = 0;
		for (int i = index; i < items.size(); i++) {
			Item item = items.get(i);
			if (number != null && number != item.isNumber()) continue;
			rel = item.compareTo(null);
			if (rel != 0) break;
		}
		return rel;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof MavenVersion && compareTo((MavenVersion) o) == 0;
	}

	@Override
	public int hashCode() {
		return items.hashCode();
	}

	@Override
	public String toString() {
		return version;
	}

	// -------------------------------------------------------------------
	// Tokenizer — state machine over the version string
	// -------------------------------------------------------------------

	static final class Tokenizer {

		private static final Integer QUALIFIER_ALPHA = Integer.valueOf(-5);
		private static final Integer QUALIFIER_BETA = Integer.valueOf(-4);
		private static final Integer QUALIFIER_MILESTONE = Integer.valueOf(-3);

		private static final Map<String, Integer> QUALIFIERS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		static {
			QUALIFIERS.put("alpha", QUALIFIER_ALPHA);
			QUALIFIERS.put("beta", QUALIFIER_BETA);
			QUALIFIERS.put("milestone", QUALIFIER_MILESTONE);
			QUALIFIERS.put("cr", Integer.valueOf(-2));
			QUALIFIERS.put("rc", Integer.valueOf(-2));
			QUALIFIERS.put("snapshot", Integer.valueOf(-1));
			QUALIFIERS.put("ga", Integer.valueOf(0));
			QUALIFIERS.put("final", Integer.valueOf(0));
			QUALIFIERS.put("release", Integer.valueOf(0));
			QUALIFIERS.put("", Integer.valueOf(0));
			QUALIFIERS.put("sp", Integer.valueOf(1));
		}

		private final String version;
		private final int versionLength;
		private int index;
		private String token;
		private boolean number;
		private boolean terminatedByNumber;

		Tokenizer(String version) {
			this.version = version.isEmpty() ? "0" : version;
			this.versionLength = this.version.length();
		}

		public boolean next() {
			if (index >= versionLength) return false;

			int state = -2;
			int start = index;
			int end = versionLength;
			terminatedByNumber = false;

			for (; index < versionLength; index++) {
				char c = version.charAt(index);

				if (c == '.' || c == '-' || c == '_') {
					end = index;
					index++;
					break;
				}
				if (c >= '0' && c <= '9') {
					int digit = c - '0';
					if (state == -1) {
						end = index;
						terminatedByNumber = true;
						break;
					}
					if (state == 0) {
						// strip leading zeros
						start++;
					}
					state = (state > 0 || digit > 0) ? 1 : 0;
				}
				else {
					if (state >= 0) {
						end = index;
						break;
					}
					state = -1;
				}
			}

			if (end - start > 0) {
				token = version.substring(start, end);
				number = state >= 0;
			}
			else {
				token = "0";
				number = true;
			}
			return true;
		}

		public Item toItem() {
			if (number) {
				try {
					if (token.length() < 10) {
						return new Item(Item.KIND_INT, Integer.valueOf(Integer.parseInt(token)));
					}
					return new Item(Item.KIND_BIGINT, new BigInteger(token));
				}
				catch (NumberFormatException e) {
					throw new IllegalStateException(e);
				}
			}
			if (index >= version.length()) {
				if ("min".equalsIgnoreCase(token)) return Item.MIN;
				if ("max".equalsIgnoreCase(token)) return Item.MAX;
			}
			if (terminatedByNumber && token.length() == 1) {
				switch (token.charAt(0)) {
				case 'a':
				case 'A':
					return new Item(Item.KIND_QUALIFIER, QUALIFIER_ALPHA);
				case 'b':
				case 'B':
					return new Item(Item.KIND_QUALIFIER, QUALIFIER_BETA);
				case 'm':
				case 'M':
					return new Item(Item.KIND_QUALIFIER, QUALIFIER_MILESTONE);
				default:
				}
			}
			Integer qualifier = QUALIFIERS.get(token);
			if (qualifier != null) return new Item(Item.KIND_QUALIFIER, qualifier);
			return new Item(Item.KIND_STRING, token.toLowerCase(Locale.ENGLISH));
		}
	}

	// -------------------------------------------------------------------
	// Item — one segment of a parsed version
	// -------------------------------------------------------------------

	public static final class Item {

		static final int KIND_MAX = 8;
		static final int KIND_BIGINT = 5;
		static final int KIND_INT = 4;
		static final int KIND_STRING = 3;
		static final int KIND_QUALIFIER = 2;
		static final int KIND_MIN = 0;

		static final Item MAX = new Item(KIND_MAX, "max");
		static final Item MIN = new Item(KIND_MIN, "min");

		private final int kind;
		private final Object value;

		Item(int kind, Object value) {
			this.kind = kind;
			this.value = value;
		}

		public boolean isNumber() {
			// kind != STRING and != QUALIFIER → treated as numeric tier for padding purposes
			return (kind & KIND_QUALIFIER) == 0;
		}

		public int compareTo(Item that) {
			int rel;
			if (that == null) {
				// null denotes the "pad" item (0 or empty qualifier)
				switch (kind) {
				case KIND_MIN:
					rel = -1;
					break;
				case KIND_MAX:
				case KIND_BIGINT:
				case KIND_STRING:
					rel = 1;
					break;
				case KIND_INT:
				case KIND_QUALIFIER:
					rel = ((Integer) value).intValue();
					break;
				default:
					throw new IllegalStateException("unknown version item kind " + kind);
				}
			}
			else {
				rel = kind - that.kind;
				if (rel == 0) {
					switch (kind) {
					case KIND_MAX:
					case KIND_MIN:
						break;
					case KIND_BIGINT:
						rel = ((BigInteger) value).compareTo((BigInteger) that.value);
						break;
					case KIND_INT:
					case KIND_QUALIFIER:
						rel = ((Integer) value).compareTo((Integer) that.value);
						break;
					case KIND_STRING:
						rel = ((String) value).compareToIgnoreCase((String) that.value);
						break;
					default:
						throw new IllegalStateException("unknown version item kind " + kind);
					}
				}
			}
			return rel;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Item && compareTo((Item) obj) == 0;
		}

		@Override
		public int hashCode() {
			return value.hashCode() + kind * 31;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}
}

/*
 * Maven version range parsing for Lucee.
 *
 * Based on Apache Maven Resolver's GenericVersionRange + UnionVersionRange
 * (maven-resolver-util 2.0.16, Apache License 2.0). Extended to handle
 * maven's union-of-ranges form ("[1.0,1.5],[2.0,)") which aether splits
 * at a higher level.
 */
package lucee.runtime.mvn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses maven version range specifications and answers whether a given
 * {@link MavenVersion} is contained.
 *
 * Supported forms:
 * <ul>
 * <li>Bare version <code>1.0</code> — soft pin, treated as <code>[1.0]</code></li>
 * <li>Pinned <code>[1.0]</code></li>
 * <li>Bracketed with inclusive/exclusive bounds: <code>[1.0,2.0)</code>, <code>(1.0,2.0]</code>, etc.</li>
 * <li>Open bounds: <code>[1.0,)</code>, <code>(,1.0]</code></li>
 * <li>Wildcard <code>[1.2.*]</code> — expanded to <code>[1.2.min, 1.2.max]</code></li>
 * <li>Union: <code>[1.0,1.5],[2.0,)</code></li>
 * </ul>
 */
public final class MavenVersionRange {

	private final String spec;
	private final List<SubRange> subs;

	public MavenVersionRange(String spec) {
		if (spec == null) throw new IllegalArgumentException("range spec cannot be null");
		this.spec = spec.trim();
		if (this.spec.isEmpty()) throw new IllegalArgumentException("empty range spec");
		this.subs = Collections.unmodifiableList(parse(this.spec));
	}

	public boolean contains(MavenVersion v) {
		for (SubRange s: subs) {
			if (s.contains(v)) return true;
		}
		return false;
	}

	public boolean contains(String v) {
		return contains(new MavenVersion(v));
	}

	/**
	 * Fast path for specs that resolve to a single authoritative version
	 * without consulting {@code maven-metadata.xml}:
	 *
	 * <ul>
	 * <li>Bare <code>1.0</code> or pinned <code>[1.0]</code>: returns that version string.</li>
	 * <li>Anything else (ranges with bounds, open bounds, wildcards, unions):
	 * returns {@code null} — caller must fetch metadata. Returning the
	 * lower bound of a range like <code>[2.2,3)</code> is wrong: the range
	 * means "any version in [2.2, 3)", not "version 2.2 exists". Many
	 * real-world artifacts (jaxb-runtime, etc.) declare such ranges
	 * where the literal lower bound was never published.</li>
	 * </ul>
	 */
	public String bestEffortSingleVersion() {
		if (subs.size() != 1) return null;
		SubRange s = subs.get(0);
		if (s.isWildcardExpansion) return null;
		if (s.lower != null && s.upper != null && s.lowerInclusive && s.upperInclusive && s.lower.compareTo(s.upper) == 0) {
			return s.lower.asString();
		}
		return null;
	}

	/**
	 * Returns the highest version from the given candidates that is
	 * contained in this range, or {@code null} if none match.
	 *
	 * Pre-release candidates (alpha, beta, milestone, rc/cr, snapshot)
	 * are skipped unless the range spec itself names a pre-release on
	 * either bound — matching maven's default behaviour of not pulling
	 * milestones transitively. Without this, {@code [2.2, 3)} against a
	 * candidate list containing {@code 3.0.0-M5} would pick the milestone
	 * (which by maven compareTo is less than {@code 3.0.0} and therefore
	 * in range) over real releases like {@code 2.3.9}.
	 */
	public MavenVersion pickHighest(List<String> candidates) {
		boolean allowPreRelease = rangeNamesPreRelease();
		MavenVersion best = null;
		for (String cand: candidates) {
			if (cand == null || cand.isEmpty()) continue;
			MavenVersion mv;
			try {
				mv = new MavenVersion(cand);
			}
			catch (RuntimeException ignore) {
				// candidate isn't a valid version string (e.g. a stray .tmp dir
				// name fed in from listLocalVersions) — skip silently
				continue;
			}
			if (!allowPreRelease && mv.isPreRelease()) continue;
			if (contains(mv) && (best == null || mv.compareTo(best) > 0)) best = mv;
		}
		return best;
	}

	private boolean rangeNamesPreRelease() {
		for (SubRange s: subs) {
			if (s.lower != null && s.lower.isPreRelease()) return true;
			if (s.upper != null && s.upper.isPreRelease()) return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return spec;
	}

	// -------------------------------------------------------------------
	// Parsing
	// -------------------------------------------------------------------

	private static List<SubRange> parse(String spec) {
		List<SubRange> out = new ArrayList<>();

		char first = spec.charAt(0);
		if (first != '[' && first != '(') {
			// bare version — pinned
			out.add(SubRange.pinned(new MavenVersion(spec)));
			return out;
		}

		int i = 0;
		int n = spec.length();
		while (i < n) {
			char open = spec.charAt(i);
			if (open != '[' && open != '(') throw new IllegalArgumentException("expected '[' or '(' at position " + i + " in [" + spec + "]");
			int closeIdx = findNextClose(spec, i);
			if (closeIdx < 0) throw new IllegalArgumentException("missing close bracket in [" + spec + "]");
			char close = spec.charAt(closeIdx);
			boolean lowerInclusive = open == '[';
			boolean upperInclusive = close == ']';
			String inner = spec.substring(i + 1, closeIdx);
			out.add(parseInner(inner, lowerInclusive, upperInclusive, spec));
			i = closeIdx + 1;
			if (i < n) {
				if (spec.charAt(i) != ',') throw new IllegalArgumentException("expected ',' between ranges at position " + i + " in [" + spec + "]");
				i++;
			}
		}
		return out;
	}

	private static int findNextClose(String spec, int from) {
		for (int j = from; j < spec.length(); j++) {
			char c = spec.charAt(j);
			if (c == ']' || c == ')') return j;
		}
		return -1;
	}

	private static SubRange parseInner(String inner, boolean lowerInclusive, boolean upperInclusive, String fullSpec) {
		int comma = inner.indexOf(',');
		if (comma < 0) {
			// [something] — either pinned [1.0] or wildcard [1.2.*]
			if (!lowerInclusive || !upperInclusive) throw new IllegalArgumentException("single version must be surrounded by [] in [" + fullSpec + "]");
			String v = inner.trim();
			if (v.endsWith(".*")) {
				String prefix = v.substring(0, v.length() - 1); // includes trailing "."
				SubRange wild = new SubRange(new MavenVersion(prefix + "min"), true, new MavenVersion(prefix + "max"), true);
				wild.isWildcardExpansion = true;
				return wild;
			}
			MavenVersion mv = new MavenVersion(v);
			return SubRange.pinned(mv);
		}
		String lower = inner.substring(0, comma).trim();
		String upper = inner.substring(comma + 1).trim();
		if (upper.indexOf(',') >= 0) throw new IllegalArgumentException("too many versions in range [" + fullSpec + "]");
		MavenVersion lowerV = lower.isEmpty() ? null : new MavenVersion(lower);
		MavenVersion upperV = upper.isEmpty() ? null : new MavenVersion(upper);
		return new SubRange(lowerV, lowerInclusive, upperV, upperInclusive);
	}

	// -------------------------------------------------------------------
	// SubRange
	// -------------------------------------------------------------------

	private static final class SubRange {
		private final MavenVersion lower;
		private final boolean lowerInclusive;
		private final MavenVersion upper;
		private final boolean upperInclusive;
		private boolean isWildcardExpansion;

		SubRange(MavenVersion lower, boolean lowerInclusive, MavenVersion upper, boolean upperInclusive) {
			this.lower = lower;
			this.lowerInclusive = lowerInclusive;
			this.upper = upper;
			this.upperInclusive = upperInclusive;
		}

		static SubRange pinned(MavenVersion v) {
			return new SubRange(v, true, v, true);
		}

		boolean contains(MavenVersion v) {
			if (lower != null) {
				int c = v.compareTo(lower);
				if (lowerInclusive ? c < 0 : c <= 0) return false;
			}
			if (upper != null) {
				int c = v.compareTo(upper);
				if (upperInclusive ? c > 0 : c >= 0) return false;
			}
			return true;
		}
	}
}

package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.util.ListUtil;

/**
 * Resolves the model of an AI connection that has no model configured (setting <code>model</code> missing or empty), the model is
 * resolved once when a session is created and then used for the whole session (LDEV-6618). This is no per request routing.
 * 
 * Resolution order:
 * <ol>
 * <li><code>modelPreference</code> patterns (glob, first pattern with a match wins)</li>
 * <li>built-in fallback patterns of the engine/provider</li>
 * <li>the first chat capable model listed by the provider</li>
 * </ol>
 * When several models match a pattern, the "highest" one wins, see {@link #RANK}.
 * <p>
 * The built-in fallback and the last step only select chat models, models for images, speech, embeddings and so on are skipped (see
 * {@link #isNonChat(String)}). A connection used for image generation should therefore define its model explicitly, or set a
 * <code>modelPreference</code> like <code>gpt-image-*</code>, preference patterns are not limited to chat models.
 */
public final class AIModelResolver {

	/** how long a resolved model is kept before the provider is asked again */
	public static final long CACHE_TIMEOUT = 60L * 60L * 1000L; // 1 hour

	private static final String[] NON_CHAT = new String[] { "embed", "tts", "whisper", "dall-e", "moderation", "audio", "realtime", "image", "transcribe", "rerank",
			"search", "imagen", "veo", "video", "aqa", "guard" };

	private static final Map<String, Cached> cache = new ConcurrentHashMap<>();

	private AIModelResolver() {
	}

	/**
	 * converts the value of the setting <code>modelPreference</code> (array or comma separated list) to a list of patterns
	 */
	public static List<String> toPatterns(Object value) throws PageException {
		List<String> list = new ArrayList<>();
		if (value == null) return list;
		Iterator<?> it;
		if (Decision.isArray(value)) it = Caster.toList(value).iterator();
		else it = ListUtil.listToList(Caster.toString(value), ',', true).iterator();
		String str;
		while (it.hasNext()) {
			str = Caster.toString(it.next(), null);
			if (!StringUtil.isEmpty(str, true)) list.add(str.trim());
		}
		return list;
	}

	public static boolean isGlob(String pattern) {
		return pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1;
	}

	/**
	 * case insensitive glob match, supports <code>*</code> (any number of characters) and <code>?</code> (exactly one character)
	 */
	public static boolean matches(String pattern, String name) {
		if (pattern == null || name == null) return false;
		String p = pattern.trim().toLowerCase(Locale.ENGLISH);
		String n = name.trim().toLowerCase(Locale.ENGLISH);
		int pi = 0, ni = 0, star = -1, mark = 0;
		while (ni < n.length()) {
			if (pi < p.length() && (p.charAt(pi) == '?' || p.charAt(pi) == n.charAt(ni))) {
				pi++;
				ni++;
			}
			else if (pi < p.length() && p.charAt(pi) == '*') {
				star = pi++;
				mark = ni;
			}
			else if (star != -1) {
				pi = star + 1;
				ni = ++mark;
			}
			else return false;
		}
		while (pi < p.length() && p.charAt(pi) == '*')
			pi++;
		return pi == p.length();
	}

	/**
	 * returns true if the name looks like a model that can not be used for chat (embeddings, speech, images, ...)
	 */
	public static boolean isNonChat(String name) {
		String n = name.toLowerCase(Locale.ENGLISH);
		for (String token: NON_CHAT) {
			if (n.indexOf(token) != -1) return true;
		}
		return false;
	}

	/** scale of a minor version that follows a dot, so it is compared as a decimal fraction */
	private static final long DECIMAL_SCALE = 1000000L;

	/**
	 * extracts the version of a model name, these are the numeric parts that follow the leading alphabetic part of the name, for
	 * example "gpt-5.1-mini" -> [5,1], "gpt-4o" -> [4], "claude-sonnet-4-5-20250929" -> [4,5], "gemini-2.5-flash" -> [2,5]. Date like
	 * parts (see {@link #isDatePart(String)}) end the version, so "gpt-4o-2024-11-20" -> [4]. A number that follows a dot is a decimal
	 * fraction, so "grok-4.7" ranks before "grok-4.20" (4.7 > 4.2), these numbers are returned scaled by 1000000 ("4.20" ->
	 * [4,200000]).
	 */
	public static List<Long> version(String name) {
		List<Long> version = new ArrayList<>();
		if (name == null) return version;
		boolean started = false;
		String t;
		for (String raw: rawTokens(name)) {
			boolean afterDot = raw.charAt(0) == '.';
			t = raw.substring(1);
			if (isDatePart(t)) break;
			if (isDigits(t)) {
				version.add(started && afterDot ? decimal(t) : Long.parseLong(t));
				started = true;
			}
			else if (Character.isDigit(t.charAt(0))) {
				// "4o", "70b": the leading number is part of the version, the rest is a variant
				int i = 0;
				while (i < t.length() && Character.isDigit(t.charAt(i)))
					i++;
				version.add(started && afterDot ? decimal(t.substring(0, i)) : Long.parseLong(t.substring(0, i)));
				break;
			}
			else if (started) break;
		}
		return version;
	}

	private static long decimal(String digits) {
		if (digits.length() > 6) digits = digits.substring(0, 6);
		long value = Long.parseLong(digits);
		for (int i = digits.length(); i < 6; i++)
			value *= 10;
		return value;
	}

	/**
	 * a date or snapshot part of a model name, these are numbers with 4 or more digits like "2024" (in "gpt-4o-2024-11-20"), "0309" (in
	 * "grok-4.20-0309") or "20250929"
	 */
	public static boolean isDatePart(String token) {
		return token.length() >= 4 && isDigits(token);
	}

	/**
	 * true if the name contains a date/snapshot part, e.g. "gpt-4o-2024-11-20" but not "gpt-4o"
	 */
	public static boolean isDated(String name) {
		for (String t: tokens(name)) {
			if (isDatePart(t)) return true;
		}
		return false;
	}

	/**
	 * the name without vendor prefix and without date/snapshot parts, "gpt-4o-2024-11-20" -> "gpt-4o"; a date part is followed by
	 * month and day ("2024-11-20"), these 2 digit parts are removed as well
	 */
	public static String undated(String name) {
		StringBuilder sb = new StringBuilder();
		boolean afterDate = false;
		for (String t: tokens(name)) {
			if (isDatePart(t)) {
				afterDate = true;
				continue;
			}
			if (afterDate && t.length() <= 2 && isDigits(t)) continue;
			afterDate = false;
			if (sb.length() > 0) sb.append('-');
			sb.append(t);
		}
		return sb.toString();
	}

	/**
	 * variants that are slower, experimental or special purpose, a model without such a part is preferred over one with it on the same
	 * version, e.g. "grok-4.20-non-reasoning" over "grok-4.20-reasoning"
	 */
	private static final String[] SPECIAL_VARIANTS = new String[] { "reasoning", "thinking", "agent", "preview", "exp", "experimental", "research", "codex" };

	/**
	 * number of special variant parts of the name (see {@link #SPECIAL_VARIANTS}), "non-reasoning" is not counted
	 */
	public static int specialVariants(String name) {
		int count = 0;
		String prev = null;
		for (String t: tokens(name)) {
			String lt = t.toLowerCase(Locale.ENGLISH);
			for (String v: SPECIAL_VARIANTS) {
				if (lt.equals(v) && !"non".equals(prev)) count++;
			}
			prev = lt;
		}
		return count;
	}

	private static List<String> tokens(String name) {
		List<String> tokens = new ArrayList<>();
		for (String raw: rawTokens(name)) {
			tokens.add(raw.substring(1));
		}
		return tokens;
	}

	/**
	 * the alphanumeric parts of the name (without vendor prefix), every part starts with the character that separated it from the
	 * previous part (a space for the first part)
	 */
	private static List<String> rawTokens(String name) {
		List<String> tokens = new ArrayList<>();
		if (name == null) return tokens;
		String n = name;
		// strip a vendor/namespace prefix like "models/" or "meta-llama/"
		int index = n.lastIndexOf('/');
		if (index != -1) n = n.substring(index + 1);
		StringBuilder token = new StringBuilder();
		char sep = ' ';
		for (char c: n.toCharArray()) {
			if (Character.isLetterOrDigit(c)) token.append(c);
			else {
				if (token.length() > 0) tokens.add(sep + token.toString());
				token.setLength(0);
				sep = c;
			}
		}
		if (token.length() > 0) tokens.add(sep + token.toString());
		return tokens;
	}

	private static boolean isDigits(String str) {
		for (char c: str.toCharArray()) {
			if (c < '0' || c > '9') return false;
		}
		return str.length() > 0;
	}

	/**
	 * sorts the best model first:
	 * <ol>
	 * <li>highest version first ("gpt-5.4" before "gpt-4o", "grok-4.7" before "grok-4.20"), dates/snapshots are not part of the
	 * version</li>
	 * <li>fewer special variants first ("grok-4.20-non-reasoning" before "grok-4.20-reasoning")</li>
	 * <li>the shorter name without its date (the base model and not a variant like "-mini" or "-preview")</li>
	 * <li>the undated alias before a dated snapshot ("gpt-4o" before "gpt-4o-2024-11-20")</li>
	 * <li>alphabetically descending, so the newest of several snapshots comes first</li>
	 * </ol>
	 */
	public static final Comparator<String> RANK = new Comparator<String>() {
		@Override
		public int compare(String left, String right) {
			List<Long> l = version(left);
			List<Long> r = version(right);
			int len = Math.max(l.size(), r.size());
			long a, b;
			for (int i = 0; i < len; i++) {
				a = i < l.size() ? l.get(i) : -1L;
				b = i < r.size() ? r.get(i) : -1L;
				if (a != b) return a > b ? -1 : 1;
			}
			int sl = specialVariants(left), sr = specialVariants(right);
			if (sl != sr) return sl - sr;

			String ul = undated(left), ur = undated(right);
			if (ul.length() != ur.length()) return ul.length() - ur.length();

			boolean dl = isDated(left), dr = isDated(right);
			if (dl != dr) return dl ? 1 : -1;

			if (left.length() != right.length()) return left.length() - right.length();
			return right.compareToIgnoreCase(left);
		}
	};

	/**
	 * returns the best model of the given list matching the pattern, or null if no model matches
	 */
	public static String best(List<String> available, String pattern, boolean chatOnly) {
		List<String> matches = new ArrayList<>();
		for (String name: available) {
			if (name == null) continue;
			if (chatOnly && isNonChat(name)) continue;
			if (matches(pattern, name)) matches.add(name);
		}
		if (matches.isEmpty()) return null;
		Collections.sort(matches, RANK);
		return matches.get(0);
	}

	/**
	 * selects a model, this method does no network calls, so it can be tested in isolation
	 * 
	 * @param available models available at the provider, null if they could not be listed
	 * @param preference patterns from the setting "modelPreference", can be null
	 * @param fallback built-in fallback patterns, can be null
	 * @return the selected model, or null if no model could be selected
	 */
	public static String select(List<String> available, List<String> preference, List<String> fallback) {
		// some providers have no (usable) model listing, an empty list is handled like "could not be listed"
		if (available != null && available.isEmpty()) available = null;

		// 1. preference
		if (preference != null) {
			for (String pattern: preference) {
				if (StringUtil.isEmpty(pattern, true)) continue;
				if (available != null) {
					String model = best(available, pattern.trim(), false);
					if (model != null) return model;
				}
				// the models could not be listed, take an explicit name as it is
				else if (!isGlob(pattern)) return pattern.trim();
			}
		}

		// 2. built-in fallback
		if (fallback != null) {
			for (String pattern: fallback) {
				if (available != null) {
					String model = best(available, pattern, true);
					if (model != null) return model;
				}
				else if (!isGlob(pattern)) return pattern;
			}
		}

		// 3. first chat capable model
		if (available != null) {
			for (String name: available) {
				if (name != null && !isNonChat(name)) return name;
			}
		}
		return null;
	}

	/**
	 * resolves the model for the given engine, the result is cached per engine (id) for {@link #CACHE_TIMEOUT}
	 */
	public static String resolve(AIEngineSupport engine) throws PageException {
		String key = engine.getId();
		if (key == null) key = engine.getName();
		Cached cached = key == null ? null : cache.get(key);
		if (cached != null && cached.expires > System.currentTimeMillis()) return cached.model;

		List<String> preference = engine.getModelPreference();
		List<String> fallback = engine.getFallbackModels();

		List<String> available = null;
		PageException listingError = null;
		try {
			available = engine.listModelNames();
		}
		catch (PageException pe) {
			listingError = pe;
		}

		String model = select(available, preference, fallback);
		if (StringUtil.isEmpty(model, true)) {
			StringBuilder msg = new StringBuilder("No model is defined for the AI connection [").append(engine.getName()).append("] (").append(engine.getLabel())
					.append(") and Lucee was unable to pick one");
			if (listingError != null) msg.append(", listing the available models failed: ").append(listingError.getMessage());
			else if (available == null || available.isEmpty()) msg.append(", the provider did not list any models");
			else msg.append(", none of the available models [").append(AIUtil.getModelNamesAsStringList(available)).append("] fits");
			msg.append(". Define the model explicitly or set [modelPreference].");
			ApplicationException ae = new ApplicationException(msg.toString());
			if (listingError != null) ae.initCause(listingError);
			throw ae;
		}

		LogUtil.logx(engine.getConfig(), Log.LEVEL_INFO, "ai", "AI connection [" + engine.getName() + "] (" + engine.getLabel() + ") has no model defined, resolved model to ["
				+ model + "]" + (listingError != null ? " (listing models failed: " + listingError.getMessage() + ")" : ""), "ai", "application");

		if (key != null) cache.put(key, new Cached(model, System.currentTimeMillis() + CACHE_TIMEOUT));
		return model;
	}

	/**
	 * removes the cached model of the given engine, the next call of resolve asks the provider again
	 */
	public static void reset(AIEngine engine) {
		String key = engine.getId();
		if (key == null) key = engine.getName();
		if (key != null) cache.remove(key);
	}

	private static final class Cached {
		private final String model;
		private final long expires;

		private Cached(String model, long expires) {
			this.model = model;
			this.expires = expires;
		}
	}
}

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
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.util.ListUtil;

/**
 * Resolves the model of an AI endpoint configured with <code>model: "auto"</code> (LDEV-6618).
 * 
 * Resolution order:
 * <ol>
 * <li><code>modelPreference</code> patterns (glob, first pattern with a match wins)</li>
 * <li>the provider's own auto router (e.g. OpenRouter)</li>
 * <li>built-in fallback patterns of the engine/provider</li>
 * <li>the first chat capable model listed by the provider</li>
 * </ol>
 * When several models match a pattern, the "highest" one wins, see {@link #RANK}.
 */
public final class AIModelResolver {

	public static final String AUTO = "auto";

	/** how long a resolved model is kept before the provider is asked again */
	public static final long CACHE_TIMEOUT = 60L * 60L * 1000L; // 1 hour

	private static final String[] NON_CHAT = new String[] { "embed", "tts", "whisper", "dall-e", "moderation", "audio", "realtime", "image", "transcribe", "rerank",
			"search", "imagen", "veo", "aqa", "guard" };

	private static final Map<String, Cached> cache = new ConcurrentHashMap<>();

	private AIModelResolver() {
	}

	public static boolean isAuto(String model) {
		return model != null && AUTO.equalsIgnoreCase(model.trim());
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

	/**
	 * extracts the version of a model name, these are the numeric parts that follow the leading alphabetic part of the name (date like
	 * parts with 6 or more digits are ignored), for example "gpt-5.1-mini" -> [5,1], "claude-sonnet-4-5-20250929" -> [4,5],
	 * "gemini-2.5-flash" -> [2,5]
	 */
	public static List<Long> version(String name) {
		List<Long> version = new ArrayList<>();
		if (name == null) return version;
		String n = name;
		// strip a vendor/namespace prefix like "models/" or "meta-llama/"
		int index = n.lastIndexOf('/');
		if (index != -1) n = n.substring(index + 1);

		boolean started = false;
		StringBuilder token = new StringBuilder();
		List<String> tokens = new ArrayList<>();
		for (char c: n.toCharArray()) {
			if (Character.isLetterOrDigit(c)) token.append(c);
			else {
				if (token.length() > 0) tokens.add(token.toString());
				token.setLength(0);
			}
		}
		if (token.length() > 0) tokens.add(token.toString());

		for (String t: tokens) {
			if (isDigits(t)) {
				if (t.length() >= 6) continue; // date
				started = true;
				version.add(Long.parseLong(t));
			}
			else if (started) break;
		}
		return version;
	}

	private static boolean isDigits(String str) {
		for (char c: str.toCharArray()) {
			if (c < '0' || c > '9') return false;
		}
		return str.length() > 0;
	}

	/**
	 * sorts the best model first: highest version first, on the same version the shorter name (the base model and not a variant like
	 * "-mini" or "-preview") first, then alphabetically descending
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
	 * @param providerAuto the provider's own auto model (e.g. "openrouter/auto"), can be null
	 * @param fallback built-in fallback patterns, can be null
	 * @return the selected model, or null if no model could be selected
	 */
	public static String select(List<String> available, List<String> preference, String providerAuto, List<String> fallback) {
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

		// 2. provider auto
		if (!StringUtil.isEmpty(providerAuto, true)) return providerAuto.trim();

		// 3. built-in fallback
		if (fallback != null) {
			for (String pattern: fallback) {
				if (available != null) {
					String model = best(available, pattern, true);
					if (model != null) return model;
				}
				else if (!isGlob(pattern)) return pattern;
			}
		}

		// 4. first chat capable model
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
		String providerAuto = engine.getProviderAutoModel();
		List<String> fallback = engine.getFallbackModels();

		// we only list the models when we need them
		List<String> available = null;
		PageException listingError = null;
		boolean hasPreference = preference != null && !preference.isEmpty();
		if (hasPreference || StringUtil.isEmpty(providerAuto, true)) {
			try {
				available = engine.listModelNames();
			}
			catch (PageException pe) {
				listingError = pe;
			}
		}

		String model = select(available, preference, providerAuto, fallback);
		if (StringUtil.isEmpty(model, true)) {
			StringBuilder msg = new StringBuilder("Unable to resolve the model [auto] for the AI endpoint [").append(engine.getName()).append("] (").append(engine.getLabel())
					.append(")");
			if (listingError != null) msg.append(", listing the available models failed: ").append(listingError.getMessage());
			else if (available == null || available.isEmpty()) msg.append(", the provider did not list any models");
			else msg.append(", none of the available models [").append(AIUtil.getModelNamesAsStringList(available)).append("] fits");
			msg.append(". Define the model explicitly or set [modelPreference].");
			ApplicationException ae = new ApplicationException(msg.toString());
			if (listingError != null) ae.initCause(listingError);
			throw ae;
		}

		LogUtil.logx(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, "ai", "AI endpoint [" + engine.getName() + "] (" + engine.getLabel() + ") resolved model [auto] to ["
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

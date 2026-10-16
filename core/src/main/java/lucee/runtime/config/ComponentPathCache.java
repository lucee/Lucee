package lucee.runtime.config;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import lucee.runtime.CIPage;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class ComponentPathCache {
	private final Map<String, Reference<PageSource>> componentPathCache = new ConcurrentHashMap<>();

	public CIPage getPage(PageContext pc, String pathWithCFC) throws PageException {
		Reference<PageSource> tmp = componentPathCache.get(pathWithCFC.toLowerCase());
		PageSource ps = tmp == null ? null : tmp.get();
		if (ps == null) return null;
		return (CIPage) ps.loadPageThrowTemplateException(pc, false, (Page) null);
	}

	public void put(String pathWithCFC, PageSource ps) {
		// ReferenceMap(ReferenceMap.SOFT,ReferenceMap.SOFT);
		componentPathCache.put(pathWithCFC.toLowerCase(), new SoftReference<PageSource>(ps));
	}

	public void flush() {
		componentPathCache.clear();
	}

	public void clear() {
		componentPathCache.clear();
	}

	public Struct list() {
		Struct sct = new StructImpl();
		Iterator<Entry<String, Reference<PageSource>>> it = componentPathCache.entrySet().iterator();

		Entry<String, Reference<PageSource>> entry;
		while (it.hasNext()) {
			entry = it.next();
			String k = entry.getKey();
			if (k == null) continue;
			Reference<PageSource> v = entry.getValue();
			if (v == null) continue;
			PageSource ps = v.get();
			if (ps == null) continue;
			sct.setEL(KeyImpl.init(k), ps.getDisplayPath());
		}
		return sct;
	}
}

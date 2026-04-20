package lucee.runtime.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lucee.commons.lang.StringUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.mvn.MavenUtil.GAVSO;

public class RHExtensionCollection {

	public static final int TYPE_ALL = 0;
	public static final int TYPE_INSTALLED = 1;
	public static final int TYPE_NOT_INSTALLED = 2;

	private Map<String, Entry> hashes = new HashMap<>();
	private Map<String, Entry> filenames = new HashMap<>();

	public int size() {
		return hashes.size();
	}

	public synchronized Entry put(String hash, String filename, RHExtension rhe) {
		Entry entry = new Entry(hash, filename, rhe);
		hashes.put(hash, entry);
		filenames.put(filename, entry);
		return entry;
	}

	public synchronized boolean containsHash(String hash) {
		return hashes.containsKey(hash);
	}

	public synchronized boolean containsFileName(String filename) {
		return filenames.containsKey(filename);
	}

	public synchronized Entry getByName(String filename) {
		return filenames.get(filename);
	}

	public synchronized Entry getByHash(String hash) {
		return hashes.get(hash);
	}

	public synchronized Entry get(ExtensionDefintion ed) {
		String name = ed.getStorageName(false);
		Entry entry = getByName(name);
		if (entry != null) return entry;

		// get match by gav (unlikely that this happen in the future)
		if (!name.startsWith("mvn_")) {
			GAVSO gav = ed.getGAVSO(ThreadLocalPageContext.getConfigServer(), true);
			if (gav != null) {
				entry = getByName(ExtensionDefintion.getStorageName(gav));
				if (entry != null) {
					return entry;
				}
			}
		}
		if (!ExtensionDefintion.startsWithUUID(name)) {
			if (!StringUtil.isEmpty(ed.getId()) && !StringUtil.isEmpty(ed.getVersion())) {
				entry = getByName(ExtensionDefintion.getStorageName(ed.getId(), ed.getVersion()));
				if (entry != null) {
					return entry;
				}
			}
		}

		for (Map.Entry<String, Entry> e: hashes.entrySet()) {
			if (e.getValue().getRHExtension().equalsTo(ed)) {
				return e.getValue();
			}
		}
		return null;
	}

	public synchronized Entry remove(ExtensionDefintion ext) {
		Entry entry = get(ext);
		if (entry != null) {
			return removeByHash(entry.getHash());
		}
		return null;
	}

	public synchronized Entry remove(RHExtension ext) {
		String match = null;
		for (java.util.Map.Entry<String, Entry> entry: hashes.entrySet()) {
			if (ext.equals(entry.getValue().getRHExtension())) {
				match = entry.getKey();
				break;
			}
		}

		if (match != null) {
			return removeByHash(match);
		}
		return null;
	}

	public synchronized Entry removeByName(String filename) {
		Entry entry = filenames.remove(filename);
		if (entry != null) {
			hashes.remove(entry.getHash());
			return entry;
		}
		return null;
	}

	public synchronized Entry removeByHash(String hash) {
		Entry entry = hashes.remove(hash);
		if (entry != null) {
			filenames.remove(entry.getFilename());
			return entry;
		}
		return null;
	}

	public List<Entry> getExtensions() {
		return getExtensions(TYPE_ALL);
	}

	public synchronized List<Entry> getExtensions(int type) {

		List<Entry> rtn = new ArrayList<>();
		for (Map.Entry<String, Entry> e: hashes.entrySet()) {
			if (TYPE_ALL == type) {
				rtn.add(e.getValue());
			}
			else if (TYPE_INSTALLED == type) {
				if (e.getValue().getRHExtension().installed()) {
					rtn.add(e.getValue());
				}
			}
			else if (TYPE_NOT_INSTALLED == type) {
				if (!e.getValue().getRHExtension().installed()) {
					rtn.add(e.getValue());
				}
			}
			// future conditions here
		}
		return rtn;
	}

	public class Entry {

		private String hash;
		private String filename;
		private RHExtension rhe;

		private Entry(String hash, String filename, RHExtension rhe) {
			this.hash = hash;
			this.filename = filename;
			this.rhe = rhe;
		}

		public String getHash() {
			return hash;
		}

		public String getFilename() {
			return filename;
		}

		public RHExtension getRHExtension() {
			return rhe;
		}

	}
}

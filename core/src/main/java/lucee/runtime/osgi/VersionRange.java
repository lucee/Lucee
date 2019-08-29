package lucee.runtime.osgi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.Version;

import lucee.commons.lang.StringUtil;
import lucee.loader.util.Util;
import lucee.runtime.type.util.ListUtil;

public class VersionRange {

	List<VR> vrs = new ArrayList<VR>();

	public VersionRange(String rawVersionRanges) {
		Iterator<String> it = ListUtil.listToList(rawVersionRanges, ',', true).iterator();
		String str, l, r;
		int index;
		Version f, t;
		while (it.hasNext()) {
			str = it.next();
			if (StringUtil.isEmpty(str, true) || str.equals("-")) continue;
			index = str.indexOf('-');
			if (index == -1) {
				f = OSGiUtil.toVersion(str, null);
				t = null;
			}
			else {
				l = str.substring(0, index).trim();
				r = str.substring(index + 1).trim();
				if (!StringUtil.isEmpty(l, true)) f = OSGiUtil.toVersion(l, null);
				else f = null;
				if (!StringUtil.isEmpty(r, true)) t = OSGiUtil.toVersion(r, null);
				else t = null;
			}
			vrs.add(new VR(f, t));
		}

		// 1-3,5,6-,-9
	}

	public boolean isWithin(Version version) {
		for (VR vr: vrs) {
			if (vr.isWithin(version)) return true;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (VR vr: vrs) {
			if (sb.length() > 0) sb.append(',');
			sb.append(vr.toString());
		}
		return sb.toString();
	}

	private static class VR {
		private Version from;
		private Version to;

		public VR(Version from, Version to) {
			this.from = from;
			this.to = to;
		}

		public boolean isWithin(Version version) {
			if (from != null && Util.isNewerThan(from, version)) return false;
			if (to != null && Util.isNewerThan(version, to)) return false;

			return true;
		}

		@Override
		public String toString() {
			if (from != null && to != null && from.equals(to)) return from.toString();
			return (from == null ? "" : from.toString()) + "-" + (to == null ? "" : to.toString());
		}
	}
}

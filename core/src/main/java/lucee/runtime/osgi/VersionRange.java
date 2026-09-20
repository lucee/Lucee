package lucee.runtime.osgi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.Version;

import lucee.commons.lang.StringUtil;
import lucee.runtime.type.util.ListUtil;

public final class VersionRange implements Serializable {

	private static final long serialVersionUID = 5064857833235093768L;

	List<VR> vrs = new ArrayList<VR>();

	public VersionRange(String rawVersionRanges) {
		Iterator<String> it = ListUtil.listToList(rawVersionRanges, ',', true).iterator();
		String str, l, r;
		int index;
		Version f, t;
		while (it.hasNext()) {
			str = it.next();
			if (StringUtil.isEmpty(str, true) || str.equals("-")) continue;

			// Try parsing as a single version first (e.g., "7.0.1.7-SNAPSHOT")
			Version singleVersion = OSGiUtil.toVersion(str, null);
			if (singleVersion != null) {
				f = singleVersion;
				t = null;
			}
			else {
				// If not a valid single version, try parsing as a range
				index = str.indexOf('-');
				if (index == -1) {
					f = null;
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

	private static class VR implements Serializable {

		private static final long serialVersionUID = 2004939785062801700L;
		// Mark as transient so Java's default serialization ignores them
		private transient Version from;
		private transient Version to;

		public VR(Version from, Version to) {
			this.from = from;
			this.to = to;
		}

		public boolean isWithin(Version version) {
			if (from != null && OSGiUtil.isNewerThan(from, version)) return false;
			if (to != null && OSGiUtil.isNewerThan(version, to)) return false;
			return true;
		}

		@Override
		public String toString() {
			if (from != null && to != null && from.equals(to)) return from.toString();
			return (from == null ? "" : from.toString()) + "-" + (to == null ? "" : to.toString());
		}

		/**
		 * Custom serialization method to handle Version objects
		 * 
		 * @param out the output stream
		 * @throws IOException if an I/O error occurs
		 */
		private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();

			// Write whether from is null
			out.writeBoolean(from != null);
			if (from != null) {
				// Write from version as string representation
				out.writeUTF(from.toString());
			}

			// Write whether to is null
			out.writeBoolean(to != null);
			if (to != null) {
				// Write to version as string representation
				out.writeUTF(to.toString());
			}
		}

		/**
		 * Custom deserialization method to handle Version objects
		 * 
		 * @param in the input stream
		 * @throws IOException if an I/O error occurs
		 * @throws ClassNotFoundException if the class of a serialized object cannot be found
		 */
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();

			// Read whether from was null
			boolean hasFrom = in.readBoolean();
			if (hasFrom) {
				// Read from version from its string representation
				String fromStr = in.readUTF();
				this.from = Version.parseVersion(fromStr);
			}
			else {
				this.from = null;
			}

			// Read whether to was null
			boolean hasTo = in.readBoolean();
			if (hasTo) {
				// Read to version from its string representation
				String toStr = in.readUTF();
				this.to = Version.parseVersion(toStr);
			}
			else {
				this.to = null;
			}
		}
	}
}

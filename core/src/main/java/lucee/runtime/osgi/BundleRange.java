package lucee.runtime.osgi;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.osgi.OSGiUtil.VersionDefinition;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class BundleRange implements Serializable {

	private static final long serialVersionUID = 3461505360112113191L;
	private final String name;
	// private Bundle bundle;
	private VersionRange versionRange;

	public BundleRange(String name) {
		this.name = name == null ? name : name.trim();
	}

	public BundleRange(String name, VersionRange version) throws BundleException {
		this.name = name == null ? name : name.trim();
		if (name == null) throw new IllegalArgumentException("Name cannot be null");
		setVersionRange(version);
	}

	public String getName() {
		return name;
	}

	public boolean matches(Bundle b) {
		if (!b.getSymbolicName().equals(getName())) return false;
		if (versionRange != null) return versionRange.isWithin(b.getVersion());
		return true;
	}

	public boolean matches(BundleFile bf) {

		if (!bf.getSymbolicName().equals(getName())) return false;
		if (versionRange != null) return versionRange.isWithin(bf.getVersion());
		return true;
	}

	public VersionRange getVersionRange() {
		return versionRange;
	}

	public BundleRange setVersionRange(VersionRange versionRange) {
		if (versionRange.from != null || versionRange.to != null) this.versionRange = versionRange;
		return this;
	}

	public static Array toArray(List<BundleRange> list) {
		Struct sct;
		Array arr = new ArrayImpl();
		Iterator<BundleRange> it = list.iterator();
		BundleRange br;
		VersionRange vr;
		VersionDefinition from, to;
		while (it.hasNext()) {
			br = it.next();
			sct = new StructImpl();
			sct.setEL(KeyConstants._bundleName, br.getName());
			vr = br.getVersionRange();
			if (vr != null && !vr.isEmpty()) {
				from = vr.getFrom();
				to = vr.getTo();
				if (from != null) {
					sct.setEL("bundleVersion", from.getVersionAsString());
					sct.setEL("operator", from.getOpAsString());
					if (to != null) {
						sct.setEL("bundleVersionFrom", from.getVersionAsString());
						sct.setEL("operatoFrom", from.getOpAsString());
						sct.setEL("bundleVersionTo", to.getVersionAsString());
						sct.setEL("operatorTo", to.getOpAsString());
					}
				}
			}
			arr.appendEL(sct);
		}
		return arr;
	}

	@Override
	public String toString() {
		return "name:" + name + ";version-range:" + versionRange + ";";
	}

	public static class VersionRange {
		private Version from;
		private Version to;
		private int opFrom;
		private int opTo;

		public VersionRange() {

		}

		public VersionRange(Version from, int opFrom, Version to, int opTo) {
			this.from = from;
			this.to = to;
			this.opFrom = opFrom;
			this.opTo = opTo;
		}

		public VersionRange(String from, int opFrom, String to, int opTo) throws BundleException {
			this.from = OSGiUtil.toVersion(from);
			this.to = OSGiUtil.toVersion(to);
			this.opFrom = opFrom;
			this.opTo = opTo;
		}

		public VersionRange add(String v, int op) throws BundleException {
			return add(OSGiUtil.toVersion(v), op);
		}

		public VersionRange add(Version v, int op) {
			if (from == null) {
				this.from = v;
				this.opFrom = op;
			}
			else if (to == null) {
				this.to = v;
				this.opTo = op;
			}
			return this;
		}

		public boolean isWithin(String v) throws BundleException {
			return isWithin(OSGiUtil.toVersion(v));
		}

		public boolean isWithin(Version v) {

			if (from != null) {
				if (!valid(v, opFrom, from)) return false;
			}
			if (to != null) {
				if (!valid(v, opTo, to)) return false;
			}

			return true;
		}

		private boolean valid(Version left, int op, Version right) {
			if (op == VersionDefinition.EQ) {
				return left.equals(right);
			}
			if (op == VersionDefinition.NEQ) {
				return !left.equals(right);
			}
			if (op == VersionDefinition.LT) {
				return OSGiUtil.isNewerThan(right, left);
			}
			if (op == VersionDefinition.LTE) {
				return left.equals(right) || OSGiUtil.isNewerThan(right, left);
			}
			if (op == VersionDefinition.GT) {
				return OSGiUtil.isNewerThan(left, right);
			}
			if (op == VersionDefinition.GTE) {
				return left.equals(right) || OSGiUtil.isNewerThan(left, right);
			}
			return false;
		}

		@Override
		public String toString() {
			if (to == null) {
				if (opFrom == VersionDefinition.EQ) return from == null ? null : from.toString();
			}

			if (from != null && opFrom == VersionDefinition.GT || opFrom == VersionDefinition.GTE) {
				StringBuilder sb = new StringBuilder();
				sb.append(opFrom == VersionDefinition.GT ? '(' : '[').append(from.toString()).append(',');
				if (to != null) {
					if (opTo == VersionDefinition.LT || opTo == VersionDefinition.LTE) {
						sb.append(to.toString()).append(opTo == VersionDefinition.LT ? ')' : ']');
						return sb.toString();
					}
				}
				else return sb.append(']').toString();
			}

			// TODO better
			StringBuilder sb = new StringBuilder();
			if (from != null) {
				sb.append("from:").append(from).append(';').append(VersionDefinition.toOperator(opFrom, ""));
			}
			sb.append('-');
			if (to != null) {
				sb.append("to:").append(to).append(';').append(VersionDefinition.toOperator(opTo, ""));
			}
			return sb.toString();
		}

		public VersionDefinition getFrom() {
			return new VersionDefinition(from, opFrom, false);
		}

		public VersionDefinition getTo() {
			return new VersionDefinition(to, opTo, false);
		}

		public boolean isEmpty() {
			return from == null && to == null;
		}

	}

	public BundleDefinition toBundleDefintion() {
		if (getVersionRange() == null || getVersionRange().from == null) return new BundleDefinition(name);
		return new BundleDefinition(name, getVersionRange().from);
	}
}
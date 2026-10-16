package lucee.runtime.functions.mvn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.ext.function.Function;
import lucee.runtime.text.xml.XMLUtil;

public final class MavenExport implements Function {

	private static final long serialVersionUID = -6139874520183429716L;

	private static final String WRAPPER_GROUP_ID = "com.example.lucee";
	private static final String WRAPPER_ARTIFACT_ID = "mvn-cache-export";
	private static final String WRAPPER_VERSION = "0.0.0";

	public static String call(PageContext pc) {
		Resource mvnDir = ((ConfigPro) pc.getConfig()).getMavenDir();
		Log log = LogUtil.getLog(pc.getConfig(), "mvn", "application");
		List<Coord> coords = new ArrayList<>();
		if (mvnDir.isDirectory()) {
			walk(mvnDir, new ArrayList<String>(), coords, log);
		}
		Collections.sort(coords, COORD_ORDER);

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n");
		sb.append("\t<modelVersion>4.0.0</modelVersion>\n");
		sb.append("\t<groupId>").append(WRAPPER_GROUP_ID).append("</groupId>\n");
		sb.append("\t<artifactId>").append(WRAPPER_ARTIFACT_ID).append("</artifactId>\n");
		sb.append("\t<version>").append(WRAPPER_VERSION).append("</version>\n");
		sb.append("\t<packaging>pom</packaging>\n");
		if (!coords.isEmpty()) {
			sb.append("\t<dependencies>\n");
			for (Coord c: coords) {
				sb.append("\t\t<dependency>\n");
				sb.append("\t\t\t<groupId>").append(XMLUtil.escapeXMLString(c.groupId)).append("</groupId>\n");
				sb.append("\t\t\t<artifactId>").append(XMLUtil.escapeXMLString(c.artifactId)).append("</artifactId>\n");
				sb.append("\t\t\t<version>").append(XMLUtil.escapeXMLString(c.version)).append("</version>\n");
				if (c.classifier != null) {
					sb.append("\t\t\t<classifier>").append(XMLUtil.escapeXMLString(c.classifier)).append("</classifier>\n");
				}
				sb.append("\t\t</dependency>\n");
			}
			sb.append("\t</dependencies>\n");
		}
		sb.append("</project>\n");
		return sb.toString();
	}

	private static void walk(Resource dir, List<String> segments, List<Coord> out, Log log) {
		Resource[] children = dir.listResources();
		if (children == null) return;
		for (Resource child: children) {
			if (child.isDirectory()) {
				List<String> next = new ArrayList<>(segments.size() + 1);
				next.addAll(segments);
				next.add(child.getName());
				walk(child, next, out, log);
			}
			else if (child.isFile() && child.getName().endsWith(".jar")) {
				Coord c = deriveCoord(segments, child.getName());
				if (c != null) out.add(c);
				else if (log != null) {
					log.debug("mvn", "mavenExport: skipped unparseable jar [" + child.getAbsolutePath() + "] (filename does not match expected {artifact}-{version}[-{classifier}].jar)");
				}
			}
		}
	}

	private static final Comparator<Coord> COORD_ORDER = new Comparator<Coord>() {
		@Override
		public int compare(Coord x, Coord y) {
			int c = x.groupId.compareTo(y.groupId);
			if (c != 0) return c;
			c = x.artifactId.compareTo(y.artifactId);
			if (c != 0) return c;
			c = x.version.compareTo(y.version);
			if (c != 0) return c;
			String xc = x.classifier == null ? "" : x.classifier;
			String yc = y.classifier == null ? "" : y.classifier;
			return xc.compareTo(yc);
		}
	};

	// segments = path from mvnDir to the dir containing the jar
	// expected layout: [g1, g2, ..., a, v] ⇒ at least 3 segments
	private static Coord deriveCoord(List<String> segments, String filename) {
		if (segments.size() < 3) return null;
		String v = segments.get(segments.size() - 1);
		String a = segments.get(segments.size() - 2);
		StringBuilder g = new StringBuilder();
		for (int i = 0; i < segments.size() - 2; i++) {
			if (g.length() > 0) g.append('.');
			g.append(segments.get(i));
		}
		String prefix = a + "-" + v;
		if (!filename.startsWith(prefix) || !filename.endsWith(".jar")) return null;
		String middle = filename.substring(prefix.length(), filename.length() - 4);
		String classifier = null;
		if (!middle.isEmpty()) {
			if (middle.charAt(0) != '-') return null;
			classifier = middle.substring(1);
		}
		return new Coord(g.toString(), a, v, classifier);
	}

	private static final class Coord {
		final String groupId;
		final String artifactId;
		final String version;
		final String classifier;

		Coord(String groupId, String artifactId, String version, String classifier) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
			this.classifier = classifier;
		}
	}
}

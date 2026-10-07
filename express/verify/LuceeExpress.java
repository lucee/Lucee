/*
 * LuceeExpress.java - assemble a Lucee Express directory from Maven repositories (Maven Central by default)
 * instead of downloading the ~78 MB lucee-express-<version>.zip from cdn.lucee.org.
 *
 * Single file, JDK only (Java 11+), no dependencies. Run it directly:
 *
 *   java LuceeExpress.java <luceeVersion> <targetDir> [options]
 *
 * Options:
 *   --repo <url>     Maven repository to use (repeatable, tried in order). Default: https://repo1.maven.org/maven2/
 *                    file: URLs are supported (e.g. a local staging repo).
 *   --cache <dir>    Writable cache in Maven layout. Default: $LUCEE_EXPRESS_CACHE or ~/.m2/repository
 *   --read-cache <d> Extra read-only cache in Maven layout (repeatable), checked after --cache.
 *   --offline        Never touch the network, only use caches.
 *   --force          Replace targetDir if it already exists.
 *
 * What it does:
 *   1. fetches org.lucee:lucee-express:<v>:pom and org.lucee:lucee-express:<v>:zip:template
 *   2. fetches every <dependency> of that POM (Lucee jar, Tomcat distribution tar.gz, javax API jars)
 *   3. verifies each download against the repository's .sha256 (or .sha1) file
 *   4. lays out the directory exactly like the extracted express zip:
 *        type tar.gz/zip  -> extracted into the root, first path segment stripped, webapps/ skipped
 *        type jar         -> lib/ext/<artifactId>-<version>[-<classifier>].jar
 *        template zip     -> extracted over the root last (Lucee's conf/, webapps/ROOT, start scripts, ...)
 *   Repeat runs are served from the cache and download nothing.
 *
 * The class can also be used as a library: new LuceeExpress(repos, cache, readCaches, offline, log).assemble(v, dir)
 * Keep this file in sync with the copy in LuCLI (org.lucee.lucli.server.runtime.LuceeExpressAssembler).
 */
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class LuceeExpress {

    public static final String CENTRAL = "https://repo1.maven.org/maven2/";
    public static final String GROUP_ID = "org.lucee";
    public static final String ARTIFACT_ID = "lucee-express";
    public static final String TEMPLATE_CLASSIFIER = "template";
    /** layout rules this assembler understands (POM property lucee.express.layout) */
    public static final int LAYOUT_VERSION = 1;

    /** Thrown when an artifact exists in none of the repositories/caches (e.g. lucee-express not published for a version). */
    public static class NotFoundException extends IOException {
        public NotFoundException(String msg) { super(msg); }
    }

    /** Thrown when a download does not match the repository's checksum file. Never fall back silently on this. */
    public static class ChecksumException extends IOException {
        public ChecksumException(String msg) { super(msg); }
    }

    /** A Maven coordinate as found in the lucee-express POM. */
    public static final class Dep {
        public final String groupId, artifactId, version, type, classifier;
        public Dep(String g, String a, String v, String t, String c) {
            groupId = g; artifactId = a; version = v; type = (t == null || t.isEmpty()) ? "jar" : t; classifier = c;
        }
        String fileName() { return artifactId + "-" + version + (classifier != null ? "-" + classifier : "") + "." + type; }
        String path() { return groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + fileName(); }
        @Override public String toString() { return groupId + ":" + artifactId + ":" + version + ":" + type + (classifier != null ? ":" + classifier : ""); }
    }

    private final List<String> repos;
    private final Path cache;
    private final List<Path> readCaches;
    private final boolean offline;
    private final Consumer<String> log;
    private final HttpClient http;
    public long bytesDownloaded = 0;

    public LuceeExpress(List<String> repos, Path cache, List<Path> readCaches, boolean offline, Consumer<String> log) {
        this.repos = new ArrayList<>();
        for (String r : (repos == null || repos.isEmpty()) ? List.of(CENTRAL) : repos) this.repos.add(r.endsWith("/") ? r : r + "/");
        this.cache = cache != null ? cache : defaultCache();
        this.readCaches = readCaches != null ? readCaches : List.of();
        this.offline = offline;
        this.log = log != null ? log : s -> {};
        this.http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(Duration.ofSeconds(20)).build();
    }

    public static Path defaultCache() {
        String env = System.getenv("LUCEE_EXPRESS_CACHE");
        if (env != null && !env.isBlank()) return Paths.get(env);
        return Paths.get(System.getProperty("user.home"), ".m2", "repository");
    }

    /** Resolve the POM and return its dependencies (layout input). */
    public List<Dep> dependencies(String luceeVersion) throws IOException {
        Path pom = fetch(new Dep(GROUP_ID, ARTIFACT_ID, luceeVersion, "pom", null));
        return parsePom(pom);
    }

    /** Assemble Lucee Express <luceeVersion> into target (must not exist, unless replace). Returns target. */
    public Path assemble(String luceeVersion, Path target, boolean replace) throws IOException {
        target = target.toAbsolutePath().normalize();
        if (Files.exists(target)) {
            if (!replace) throw new IOException("Target exists: " + target);
            deleteRecursively(target);
        }
        List<Dep> deps = dependencies(luceeVersion);
        Dep templateDep = new Dep(GROUP_ID, ARTIFACT_ID, luceeVersion, "zip", TEMPLATE_CLASSIFIER);

        // resolve everything first, so a failure leaves nothing behind
        Map<Dep, Path> files = new LinkedHashMap<>();
        for (Dep d : deps) files.put(d, fetch(d));
        Path template = fetch(templateDep);

        Files.createDirectories(target.getParent());
        Path tmp = Files.createTempDirectory(target.getParent(), "." + target.getFileName() + ".tmp");
        try {
            try { Files.setPosixFilePermissions(tmp, java.nio.file.attribute.PosixFilePermissions.fromString("rwxr-xr-x")); }
            catch (UnsupportedOperationException ignore) { /* Windows */ }
            // 1. distributions (Tomcat) first
            for (Map.Entry<Dep, Path> e : files.entrySet()) {
                String t = e.getKey().type;
                if (t.equals("tar.gz") || t.equals("tgz")) extractTarGz(e.getValue(), tmp, 1, "webapps/");
                else if (t.equals("zip")) extractZip(e.getValue(), tmp, 1, "webapps/");
            }
            // 2. jars into lib/ext
            Path libExt = Files.createDirectories(tmp.resolve("lib").resolve("ext"));
            for (Map.Entry<Dep, Path> e : files.entrySet()) {
                if (e.getKey().type.equals("jar"))
                    Files.copy(e.getValue(), libExt.resolve(e.getKey().fileName()), StandardCopyOption.REPLACE_EXISTING);
            }
            // 3. Lucee template over everything
            extractZip(template, tmp, 0, null);
            try {
                Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, target);
            }
        } finally {
            if (Files.exists(tmp)) deleteRecursively(tmp);
        }
        return target;
    }

    // ------------------------------------------------------------------ POM

    static List<Dep> parsePom(Path pom) throws IOException {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = f.newDocumentBuilder().parse(pom.toFile());
            Element project = doc.getDocumentElement();
            Map<String, String> props = new HashMap<>();
            Element pe = child(project, "properties");
            if (pe != null) for (Element p : children(pe)) props.put(p.getTagName(), p.getTextContent().trim());
            props.put("project.version", text(project, "version"));
            String layout = props.get("lucee.express.layout");
            if (layout != null && Integer.parseInt(layout) > LAYOUT_VERSION)
                throw new IOException("lucee-express layout " + layout + " is newer than this assembler supports (" + LAYOUT_VERSION + ")");
            List<Dep> out = new ArrayList<>();
            Element depsEl = child(project, "dependencies");
            if (depsEl == null) return out;
            for (Element d : children(depsEl)) {
                if (!d.getTagName().equals("dependency")) continue;
                if ("true".equals(text(d, "optional"))) continue;
                String scope = text(d, "scope");
                if ("test".equals(scope) || "provided".equals(scope)) continue;
                out.add(new Dep(interp(text(d, "groupId"), props), interp(text(d, "artifactId"), props),
                        interp(text(d, "version"), props), interp(text(d, "type"), props), interp(text(d, "classifier"), props)));
            }
            return out;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Cannot parse " + pom + ": " + e.getMessage(), e);
        }
    }

    private static String interp(String s, Map<String, String> props) {
        if (s == null) return null;
        for (int i = 0; i < 10 && s.contains("${"); i++) {
            int a = s.indexOf("${"), b = s.indexOf('}', a);
            if (b < 0) break;
            String v = props.get(s.substring(a + 2, b));
            if (v == null) throw new IllegalArgumentException("Unknown property in POM: " + s);
            s = s.substring(0, a) + v + s.substring(b + 1);
        }
        return s;
    }
    private static Element child(Element e, String name) {
        for (Element c : children(e)) if (c.getTagName().equals(name)) return c;
        return null;
    }
    private static List<Element> children(Element e) {
        List<Element> l = new ArrayList<>();
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) if (n instanceof Element) l.add((Element) n);
        return l;
    }
    private static String text(Element e, String name) {
        Element c = child(e, name);
        return c == null ? null : c.getTextContent().trim();
    }

    // ------------------------------------------------------------------ fetch + verify + cache

    /** Return a verified local file for the coordinate, downloading it into the cache if needed. */
    public Path fetch(Dep d) throws IOException {
        String rel = d.path();
        Path cached = cache.resolve(rel);
        if (Files.isRegularFile(cached)) {
            if (verifyCached(cached)) { log.accept("cache   " + d); return cached; }
            log.accept("cache   " + d + " failed checksum, re-downloading");
            Files.delete(cached);
        }
        for (Path rc : readCaches) {
            Path p = rc.resolve(rel);
            if (Files.isRegularFile(p) && verifyCached(p)) { log.accept("cache   " + d + " (" + rc + ")"); return p; }
        }
        if (offline) throw new NotFoundException(d + " not in cache and --offline is set");

        for (String repo : repos) {
            String url = repo + rel;
            String[] expected = checksum(url);                // {algorithm, hex} or null if artifact absent
            if (expected == null) continue;
            Files.createDirectories(cached.getParent());
            Path part = cached.resolveSibling(cached.getFileName() + ".part");
            log.accept("get     " + url);
            String actual = download(url, part, expected[0]);
            if (!actual.equalsIgnoreCase(expected[1])) {
                Files.deleteIfExists(part);
                throw new ChecksumException("Checksum mismatch for " + url + ": expected " + expected[0] + " " + expected[1] + ", got " + actual);
            }
            Files.writeString(cached.resolveSibling(cached.getFileName() + "." + ext(expected[0])), expected[1].toLowerCase());
            Files.move(part, cached, StandardCopyOption.REPLACE_EXISTING);
            log.accept("fetched " + d + " (" + Files.size(cached) / 1024 + " KB, " + expected[0] + " ok) from " + repo);
            return cached;
        }
        throw new NotFoundException(d + " not found in " + String.join(", ", repos));
    }

    /** Fetch .sha256, falling back to .sha1 (Central always has .sha1). Returns null if neither exists. */
    private String[] checksum(String url) throws IOException {
        for (String alg : new String[] {"SHA-256", "SHA-1"}) {
            String body = getText(url + "." + ext(alg));
            if (body != null) {
                String hex = body.trim().split("\\s+")[0];
                if (hex.matches("[0-9a-fA-F]{40,64}")) return new String[] {alg, hex};
            }
        }
        return null;
    }

    private boolean verifyCached(Path p) throws IOException {
        for (String alg : new String[] {"SHA-256", "SHA-1"}) {
            Path sum = p.resolveSibling(p.getFileName() + "." + ext(alg));
            if (Files.isRegularFile(sum)) return hash(p, alg).equalsIgnoreCase(Files.readString(sum).trim().split("\\s+")[0]);
        }
        return true; // e.g. ~/.m2 files written by Maven itself (Maven verified them on download)
    }

    private static String ext(String alg) { return alg.equals("SHA-1") ? "sha1" : "sha256"; }

    private String getText(String url) throws IOException {
        if (url.startsWith("file:")) {
            Path p = Paths.get(URI.create(url));
            return Files.isRegularFile(p) ? Files.readString(p) : null;
        }
        try {
            HttpResponse<String> r = http.send(req(url), HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() == 404) return null;
            if (r.statusCode() != 200) throw new IOException("HTTP " + r.statusCode() + " for " + url);
            return r.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    private String download(String url, Path dest, String alg) throws IOException {
        MessageDigest md = digest(alg);
        try (InputStream in = open(url); OutputStream out = Files.newOutputStream(dest)) {
            byte[] buf = new byte[65536];
            for (int n; (n = in.read(buf)) > 0; ) { out.write(buf, 0, n); md.update(buf, 0, n); bytesDownloaded += n; }
        }
        return hex(md.digest());
    }

    private InputStream open(String url) throws IOException {
        if (url.startsWith("file:")) return Files.newInputStream(Paths.get(URI.create(url)));
        try {
            HttpResponse<InputStream> r = http.send(req(url), HttpResponse.BodyHandlers.ofInputStream());
            if (r.statusCode() != 200) { r.body().close(); throw new IOException("HTTP " + r.statusCode() + " for " + url); }
            return r.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    private static HttpRequest req(String url) {
        return HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(10))
                .header("User-Agent", "lucee-express-assembler/1").GET().build();
    }

    static String hash(Path p, String alg) throws IOException {
        MessageDigest md = digest(alg);
        try (InputStream in = Files.newInputStream(p)) {
            byte[] buf = new byte[65536];
            for (int n; (n = in.read(buf)) > 0; ) md.update(buf, 0, n);
        }
        return hex(md.digest());
    }
    private static MessageDigest digest(String alg) {
        try { return MessageDigest.getInstance(alg); } catch (Exception e) { throw new IllegalStateException(e); }
    }
    private static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    // ------------------------------------------------------------------ extraction

    /** Strip the first {@code strip} path segments; return null if the entry should be skipped. */
    private static String mapName(String name, int strip, String skipPrefix) {
        name = name.replace('\\', '/');
        while (name.startsWith("./")) name = name.substring(2);
        for (int i = 0; i < strip; i++) {
            int s = name.indexOf('/');
            if (s < 0) return null;
            name = name.substring(s + 1);
        }
        if (name.isEmpty()) return null;
        if (skipPrefix != null && (name.startsWith(skipPrefix) || (name + "/").equals(skipPrefix))) return null;
        return name;
    }

    private static Path safeResolve(Path root, String name) throws IOException {
        Path p = root.resolve(name).normalize();
        if (!p.startsWith(root)) throw new IOException("Archive entry outside target: " + name);
        return p;
    }

    static void extractZip(Path zip, Path root, int strip, String skipPrefix) throws IOException {
        try (ZipInputStream zin = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zip)))) {
            for (ZipEntry e; (e = zin.getNextEntry()) != null; ) {
                String name = mapName(e.getName(), strip, skipPrefix);
                if (name == null) continue;
                Path p = safeResolve(root, name);
                if (e.isDirectory()) { Files.createDirectories(p); continue; }
                Files.createDirectories(p.getParent());
                Files.copy(zin, p, StandardCopyOption.REPLACE_EXISTING);
                if (e.getLastModifiedTime() != null) Files.setLastModifiedTime(p, e.getLastModifiedTime());
                if (name.endsWith(".sh")) makeExecutable(p);
            }
        }
    }

    /** Minimal tar reader (ustar, GNU long names, pax path) - enough for the Apache Tomcat distribution. */
    static void extractTarGz(Path tgz, Path root, int strip, String skipPrefix) throws IOException {
        try (InputStream in = new BufferedInputStream(new GZIPInputStream(Files.newInputStream(tgz), 65536))) {
            byte[] h = new byte[512];
            String longName = null;
            while (readFully(in, h)) {
                if (allZero(h)) break;
                String name = str(h, 0, 100);
                String prefix = str(h, 345, 155);
                if (!prefix.isEmpty() && "ustar".equals(str(h, 257, 5))) name = prefix + "/" + name;
                long size = octal(h, 124, 12);
                int mode = (int) octal(h, 100, 8);
                long mtime = octal(h, 136, 12);
                char type = (char) h[156];
                if (type == 'L' || type == 'x' || type == 'g') {
                    byte[] data = readBytes(in, size);
                    if (type == 'L') longName = new String(data, StandardCharsets.UTF_8).replace("\0", "");
                    if (type == 'x') { String p = paxPath(data); if (p != null) longName = p; }
                    continue;
                }
                if (longName != null) { name = longName; longName = null; }
                String mapped = mapName(name, strip, skipPrefix);
                Path p = mapped == null ? null : safeResolve(root, mapped);
                if (type == '5') {
                    if (p != null) Files.createDirectories(p);
                } else if (type == '0' || type == '\0' || type == '7') {
                    if (p != null) {
                        Files.createDirectories(p.getParent());
                        try (OutputStream out = Files.newOutputStream(p)) { copyN(in, out, size); }
                        Files.setLastModifiedTime(p, java.nio.file.attribute.FileTime.fromMillis(mtime * 1000));
                        if ((mode & 0100) != 0 || mapped.endsWith(".sh")) makeExecutable(p);
                    } else {
                        copyN(in, OutputStream.nullOutputStream(), size);
                    }
                    long pad = (512 - size % 512) % 512;
                    in.skipNBytes(pad);
                    continue;
                } else if (size > 0) {
                    // symlinks/hardlinks/devices: not used by the Tomcat distribution, skip content
                    readBytes(in, size);
                    continue;
                }
            }
        }
    }

    private static String paxPath(byte[] data) {
        String s = new String(data, StandardCharsets.UTF_8);
        for (String line : s.split("\n")) {
            int sp = line.indexOf(' ');
            if (sp > 0 && line.startsWith("path=", sp + 1)) return line.substring(sp + 6);
        }
        return null;
    }
    private static boolean readFully(InputStream in, byte[] b) throws IOException {
        int off = 0;
        while (off < b.length) { int n = in.read(b, off, b.length - off); if (n < 0) return false; off += n; }
        return true;
    }
    private static byte[] readBytes(InputStream in, long size) throws IOException {
        byte[] d = in.readNBytes((int) size);
        in.skipNBytes((512 - size % 512) % 512);
        return d;
    }
    private static void copyN(InputStream in, OutputStream out, long n) throws IOException {
        byte[] buf = new byte[65536];
        while (n > 0) {
            int r = in.read(buf, 0, (int) Math.min(buf.length, n));
            if (r < 0) throw new EOFException("truncated tar");
            out.write(buf, 0, r);
            n -= r;
        }
    }
    private static boolean allZero(byte[] b) { for (byte x : b) if (x != 0) return false; return true; }
    private static String str(byte[] b, int off, int len) {
        int end = off;
        while (end < off + len && b[end] != 0) end++;
        return new String(b, off, end - off, StandardCharsets.UTF_8);
    }
    private static long octal(byte[] b, int off, int len) {
        String s = str(b, off, len).trim();
        return s.isEmpty() ? 0 : Long.parseLong(s, 8);
    }
    private static void makeExecutable(Path p) {
        try {
            Set<PosixFilePermission> perms = new HashSet<>(Files.getPosixFilePermissions(p));
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            if (perms.contains(PosixFilePermission.GROUP_READ)) perms.add(PosixFilePermission.GROUP_EXECUTE);
            if (perms.contains(PosixFilePermission.OTHERS_READ)) perms.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(p, perms);
        } catch (UnsupportedOperationException | IOException e) {
            p.toFile().setExecutable(true);
        }
    }
    static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (var s = Files.walk(root)) {
            for (Path p : (Iterable<Path>) s.sorted(Comparator.reverseOrder())::iterator) Files.delete(p);
        }
    }

    // ------------------------------------------------------------------ CLI

    public static void main(String[] args) throws Exception {
        List<String> pos = new ArrayList<>(), repos = new ArrayList<>();
        List<Path> readCaches = new ArrayList<>();
        Path cache = null;
        boolean offline = false, force = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--repo": repos.add(args[++i]); break;
                case "--cache": cache = Paths.get(args[++i]); break;
                case "--read-cache": readCaches.add(Paths.get(args[++i])); break;
                case "--offline": offline = true; break;
                case "--force": force = true; break;
                default: pos.add(args[i]);
            }
        }
        if (pos.size() != 2) {
            System.err.println("usage: java LuceeExpress.java <luceeVersion> <targetDir> [--repo URL]... [--cache DIR] [--read-cache DIR]... [--offline] [--force]");
            System.exit(2);
        }
        long t0 = System.nanoTime();
        LuceeExpress le = new LuceeExpress(repos, cache, readCaches, offline, s -> System.out.println("  " + s));
        try {
            Path dir = le.assemble(pos.get(0), Paths.get(pos.get(1)), force);
            System.out.printf("Lucee Express %s assembled in %s (%.1f s, %.1f MB downloaded)%n",
                    pos.get(0), dir, (System.nanoTime() - t0) / 1e9, le.bytesDownloaded / 1048576.0);
        } catch (NotFoundException e) {
            System.err.println("not found: " + e.getMessage());
            System.exit(3);
        }
    }
}

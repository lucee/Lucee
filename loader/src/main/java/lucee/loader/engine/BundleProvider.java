package lucee.loader.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.xml.sax.SAXException;

import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;

public final class BundleProvider {
	public static final int CONNECTION_TIMEOUT = 10000;
	private static final long MAX_AGE = 10000;

	private static URL[] DEFAULT_PROVIDER_DETAILSX = null;
	private static URL DEFAULT_PROVIDER_DETAIL_MVN = null;

	private static URL[] defaultProviderDetail;
	private static URL defaultProviderDetailMvn;
	private static Map<String, SoftReference<Map<String, Info[]>>> mappingsCollection = new ConcurrentHashMap<String, SoftReference<Map<String, Info[]>>>();
	private static Map<String, Info[]> mappingsFallback = new ConcurrentHashMap<>();

	private static final Map<String, String> tokens = new ConcurrentHashMap<String, String>();

	private URL[] details;
	private static Map<String, Pair> readers = new HashMap<>();

	static {
		try {
			DEFAULT_PROVIDER_DETAILSX = new URL[] { new URL("https://bundle-download.s3.amazonaws.com/") };
			DEFAULT_PROVIDER_DETAIL_MVN = new URL("https://repo1.maven.org/maven2/");

		}
		catch (Exception e) {
		}
		put(mappingsFallback, "apache.http.components.client", new Info("org.apache.httpcomponents", "httpclient"));
		put(mappingsFallback, "apache.http.components.core", new Info("org.apache.httpcomponents", "httpcore"));
		put(mappingsFallback, "apache.http.components.mime", new Info("org.apache.httpcomponents", "httpmime"));
		put(mappingsFallback, "com.amazonaws.aws-java-sdk-osgi", new Info("com.amazonaws", "aws-java-sdk-osgi"));
		put(mappingsFallback, "com.amazonaws.aws.java.sdk.support", new Info("com.amazonaws", "aws-java-sdk-support"));
		put(mappingsFallback, "com.google.gson", new Info("com.google.code.gson", "gson"));
		put(mappingsFallback, "com.launchdarkly.client", new Info("org.lucee", "launchdarkly"));
		put(mappingsFallback, "ehcache", new Info("net.sf.ehcache", "ehcache-core"));

		put(mappingsFallback, "ESAPI", new Info("org.owasp.esapi", "esapi"));
		put(mappingsFallback, "activiti-osgi", new Info("org.activiti", "activiti-osgi"));

		put(mappingsFallback, "avalon.framework.api", new Info("org.apache.avalon", "avalon-framework", "1"));
		put(mappingsFallback, "com.fasterxml.classmate", new Info("com.fasterxml", "classmate", "1.3.0"));
		put(mappingsFallback, "com.fasterxml.jackson.core.jackson-annotations", new Info("com.fasterxml.jackson.core", "jackson-annotations"));
		put(mappingsFallback, "com.fasterxml.jackson.core.jackson-core", new Info("com.fasterxml.jackson.core", "jackson-core"));
		put(mappingsFallback, "com.fasterxml.jackson.core.jackson-databind", new Info("com.fasterxml.jackson.core", "jackson-databind"));
		put(mappingsFallback, "com.fasterxml.jackson.dataformat.jackson-dataformat-cbor", new Info("com.fasterxml.jackson.dataformat", "jackson-dataformat-cbor"));
		put(mappingsFallback, "com.github.kirviq.dumbster", new Info("com.github.kirviq", "dumbster"));
		put(mappingsFallback, "com.google.guava", new Info("com.google.guava", "guava"));
		put(mappingsFallback, "com.google.guava.failureaccess", new Info("com.google.guava", "failureaccess"));
		put(mappingsFallback, "com.google.protobuf", new Info("com.google.protobuf", "protobuf-java"));
		put(mappingsFallback, "com.googlecode.json-simple", new Info("com.googlecode.json-simple", "json-simple"));
		put(mappingsFallback, "com.googlecode.owasp-java-html-sanitizer", new Info("com.googlecode.owasp-java-html-sanitizer", "owasp-java-html-sanitizer"));
		put(mappingsFallback, "com.microsoft.sqlserver.mssql-jdbc", new Info("com.microsoft.sqlserver", "mssql-jdbc"));

		put(mappingsFallback, "antlr", new Info("antlr", "antlr"));
		put(mappingsFallback, "apache.lucene.analyzers", new Info[0]);
		put(mappingsFallback, "apache.lucene.analyzers.common", new Info[0]);
		put(mappingsFallback, "apache.lucene.core", new Info[0]);
		put(mappingsFallback, "apache.lucene.facet", new Info[0]);
		put(mappingsFallback, "apache.lucene.queries", new Info[0]);
		put(mappingsFallback, "apache.lucene.queryparser", new Info[0]);
		put(mappingsFallback, "apache.poi", new Info[0]);
		put(mappingsFallback, "apache.poi.ooxml", new Info[0]);
		put(mappingsFallback, "apache.poi.ooxml.schemas", new Info[0]);
		put(mappingsFallback, "apache.poi.tm.extractors", new Info[0]);
		put(mappingsFallback, "apache.ws.axis", new Info[0]);
		put(mappingsFallback, "apache.ws.axis.ant", new Info[0]);
		put(mappingsFallback, "apache.xml.xalan", new Info[0]);
		put(mappingsFallback, "apache.xml.xalan.serializer", new Info[0]);
		put(mappingsFallback, "apache.xml.xerces", new Info[0]);
		put(mappingsFallback, "backport.util.concurrent", new Info[0]);
		put(mappingsFallback, "bcprov", new Info[0]);
		put(mappingsFallback, "bcprov.jdk14", new Info[0]);
		put(mappingsFallback, "bouncycastle.mail", new Info[0]);
		put(mappingsFallback, "bouncycastle.prov", new Info[0]);
		put(mappingsFallback, "bouncycastle.tsp", new Info[0]);
		put(mappingsFallback, "chart.extension", new Info[0]);
		put(mappingsFallback, "checker-qual", new Info("org.checkerframework", "checker-qual"));
		put(mappingsFallback, "com.mysql.cj", new Info("com.mysql", "mysql-connector-j"), new Info("mysql", "mysql-connector-java"));
		// put(mappings, "com.mysql.cj", new Info("mysql", "mysql-connector-java"));

		put(mappingsFallback, "com.mysql.jdbc", new Info("mysql", "mysql-connector-java"));
		put(mappingsFallback, "com.naryx.tagfusion.cfx", new Info[0]);
		put(mappingsFallback, "com.sun.jna", new Info("net.java.dev.jna", "jna"));
		put(mappingsFallback, "com.teradata.jdbc", new Info[0]);

		put(mappingsFallback, "com.teradata.tdgss", new Info[0]);
		put(mappingsFallback, "compress.extension", new Info[0]);
		put(mappingsFallback, "concurrent", new Info[0]);
		put(mappingsFallback, "distrokid.extension", new Info[0]);
		put(mappingsFallback, "ehcache.extension", new Info[0]);
		put(mappingsFallback, "esapi.extension", new Info[0]);
		put(mappingsFallback, "findbugsAnnotations", new Info("com.google.code.findbugs", "annotations"));
		put(mappingsFallback, "flex.messaging.common", new Info[0]);
		put(mappingsFallback, "flex.messaging.core", new Info[0]);
		put(mappingsFallback, "flex.messaging.opt", new Info[0]);
		put(mappingsFallback, "flex.messaging.proxy", new Info[0]);
		put(mappingsFallback, "flex.messaging.remoting", new Info[0]);
		put(mappingsFallback, "flying.saucer.core", new Info("org.xhtmlrenderer", "flying-saucer-core"));
		put(mappingsFallback, "flying.saucer.pdf", new Info("org.xhtmlrenderer", "flying-saucer-pdf"));
		put(mappingsFallback, "fonts", new Info[0]);
		put(mappingsFallback, "form.extension", new Info[0]);
		put(mappingsFallback, "fusiondebug.api.server", new Info("com.intergral.fusiondebug", "fusiondebug-api-server"));
		put(mappingsFallback, "hibernate", new Info("org.hibernate", "hibernate-core"));
		put(mappingsFallback, "hibernate.extension", new Info[0]);
		put(mappingsFallback, "hsqldb", new Info[0]);
		put(mappingsFallback, "hypersonic.hsqldb", new Info[0]);
		put(mappingsFallback, "icepdf.core", new Info[0]);
		put(mappingsFallback, "ieffects", new Info[0]);
		put(mappingsFallback, "image.extension", new Info[0]);
		put(mappingsFallback, "jackson-core-asl", new Info("org.codehaus.jackson", "jackson-core-asl"));
		put(mappingsFallback, "jackson-mapper-asl", new Info("org.codehaus.jackson", "jackson-mapper-asl"));
		put(mappingsFallback, "jacob", new Info[0]);
		put(mappingsFallback, "jandex", new Info("org.jboss", "jandex"));
		put(mappingsFallback, "java.xmlbuilder", new Info("com.jamesmurty.utils", "java-xmlbuilder"));
		put(mappingsFallback, "javaparser", new Info[0]);
		put(mappingsFallback, "javassist", new Info("org.javassist", "javassist"));
		put(mappingsFallback, "javasysmon", new Info[0]);
		put(mappingsFallback, "javax.activation", new Info[0]);
		put(mappingsFallback, "javax.el", new Info[0]);
		put(mappingsFallback, "javax.mail", new Info("javax.mail", "mail"));
		put(mappingsFallback, "javax.mail-api", new Info("javax.mail", "javax.mail-api"));
		put(mappingsFallback, "javax.mail.activation", new Info("javax.mail", "mail"));
		put(mappingsFallback, "javax.servlet.jsp-api", new Info("javax.servlet.jsp", "javax.servlet.jsp-api"));
		put(mappingsFallback, "javax.websocket-api", new Info("javax.websocket", "javax.websocket-api"));
		put(mappingsFallback, "jaxb-api", new Info("javax.xml.bind", "jaxb-api"));
		put(mappingsFallback, "jboss.logging.annotations", new Info("org.jboss.logging", "jboss-logging-annotations"));
		put(mappingsFallback, "jboss.transaction", new Info("org.jboss.spec.javax.transaction", "jboss-transaction-api_1.2_spec"));
		put(mappingsFallback, "jcifs", new Info[0]);
		put(mappingsFallback, "jcl.over.slf4j", new Info("org.slf4j", "jcl-over-slf4j"));
		put(mappingsFallback, "jcommon", new Info[0]);
		put(mappingsFallback, "jencrypt", new Info[0]);
		put(mappingsFallback, "jets3t", new Info[0]);
		put(mappingsFallback, "jffmpeg", new Info[0]);
		put(mappingsFallback, "jfreechart", new Info[0]);
		put(mappingsFallback, "jfreechart.patch", new Info[0]);
		put(mappingsFallback, "jline", new Info("jline", "jline"));
		put(mappingsFallback, "jmimemagic", new Info[0]);
		put(mappingsFallback, "joda-convert", new Info("org.joda", "joda-convert"));
		put(mappingsFallback, "joda-time", new Info("joda-time", "joda-time"));
		put(mappingsFallback, "jpedal.gpl", new Info[0]);
		put(mappingsFallback, "jta", new Info("org.jboss.javaee", "jboss-transaction-api"));
		put(mappingsFallback, "jtds", new Info[0]);
		put(mappingsFallback, "junit", new Info[0]);
		put(mappingsFallback, "junit-jupiter", new Info[0]);
		put(mappingsFallback, "junit-jupiter-api", new Info[0]);
		put(mappingsFallback, "junit-jupiter-engine", new Info[0]);
		put(mappingsFallback, "junit-jupiter-params", new Info[0]);
		put(mappingsFallback, "junit-platform-commons", new Info[0]);
		put(mappingsFallback, "junit-platform-engine", new Info[0]);
		put(mappingsFallback, "jython-standalone", new Info("org.python", "jython-standalone"));
		put(mappingsFallback, "log4j", new Info("log4j", "log4j"));
		put(mappingsFallback, "lowagie.itext", new Info[0]);
		put(mappingsFallback, "lucee.image.extension", new Info[0]);
		put(mappingsFallback, "lucene.search.extension", new Info[0]);
		put(mappingsFallback, "memcached", new Info[0]);// there is one on maven, but that one has no OSGi data
		put(mappingsFallback, "memcached.extension", new Info[0]);
		put(mappingsFallback, "metadata.extractor", new Info("com.drewnoakes", "metadata-extractor"));
		put(mappingsFallback, "microsoft.sqljdbc", new Info[0]);
		put(mappingsFallback, "mongodb.extension", new Info[0]);
		put(mappingsFallback, "mssqljdbc4", new Info[0]);
		put(mappingsFallback, "mx4j", new Info[0]);
		put(mappingsFallback, "mx4j.lite", new Info[0]);
		put(mappingsFallback, "net.lingala.zip4j", new Info("net.lingala.zip4j", "zip4j"));
		put(mappingsFallback, "net.sf.ehcache", new Info("net.sf.ehcache", "ehcache"));
		put(mappingsFallback, "net.twentyonesolutions.luceeapps", new Info("net.twentyonesolutions", "lucee-apps"));
		put(mappingsFallback, "net.twentyonesolutions.luceewebsocket", new Info("net.twentyonesolutions", "lucee-websocket"));
		put(mappingsFallback, "nu.xom", new Info[0]);
		put(mappingsFallback, "ojdbc14", new Info[0]);
		put(mappingsFallback, "ojdbc6", new Info[0]);
		put(mappingsFallback, "ojdbc7", new Info[0]);
		put(mappingsFallback, "openamf", new Info[0]);
		put(mappingsFallback, "openamf.astranslator", new Info[0]);
		put(mappingsFallback, "org.activiti.engine", new Info("org.activiti", "activiti-engine"));
		put(mappingsFallback, "org.apache.commons.cli", new Info("commons-cli", "commons-cli"));
		put(mappingsFallback, "org.apache.commons.codec", new Info("commons-codec", "commons-codec"));
		put(mappingsFallback, "org.apache.commons.collections", new Info("commons-collections", "commons-collections"));
		put(mappingsFallback, "org.apache.commons.collections4", new Info("org.apache.commons", "commons-collections4"));
		put(mappingsFallback, "org.apache.commons.commons-codec", new Info("commons-codec", "commons-codec"));
		put(mappingsFallback, "org.apache.commons.commons-collections4", new Info("org.apache.commons", "commons-collections4"));
		put(mappingsFallback, "org.apache.commons.commons-compress", new Info("org.apache.commons", "commons-compress"));
		put(mappingsFallback, "org.apache.commons.commons-imaging", new Info("org.apache.commons", "commons-imaging"));
		put(mappingsFallback, "org.apache.commons.commons-io", new Info("commons-io", "commons-io"));
		put(mappingsFallback, "org.apache.commons.commons-net", new Info("commons-net", "commons-net"));
		put(mappingsFallback, "org.apache.commons.commons-pool2", new Info("org.apache.commons", "commons-pool2"));
		put(mappingsFallback, "org.apache.commons.commons-text", new Info("org.apache.commons", "commons-text"));
		put(mappingsFallback, "org.apache.commons.compress", new Info("org.apache.commons", "commons-compress"));
		put(mappingsFallback, "org.apache.commons.discovery", new Info[0]);

		put(mappingsFallback, "org.apache.commons.email", new Info("org.apache.commons", "commons-email"));
		put(mappingsFallback, "org.apache.commons.fileupload", new Info("commons-fileupload", "commons-fileupload"));
		put(mappingsFallback, "org.apache.commons.httpclient", new Info("org.apache.httpcomponents", "httpclient"));
		put(mappingsFallback, "org.apache.commons.httpcore", new Info("org.apache.httpcomponents", "httpcore"));
		put(mappingsFallback, "org.apache.commons.httpmime", new Info("org.apache.httpcomponents", "httpmime"));
		put(mappingsFallback, "org.apache.commons.image", new Info("org.apache.commons", "commons-imaging"));
		put(mappingsFallback, "org.apache.commons.io", new Info("commons-io", "commons-io"));
		put(mappingsFallback, "org.apache.commons.lang", new Info("commons-lang", "commons-lang"));
		put(mappingsFallback, "org.apache.commons.lang3", new Info("org.apache.commons", "commons-lang3"));
		put(mappingsFallback, "org.apache.commons.logging", new Info("commons-logging", "commons-logging"));
		put(mappingsFallback, "org.apache.commons.logging.adapters", new Info("commons-logging", "commons-logging"));
		put(mappingsFallback, "org.apache.commons.logging.api", new Info("commons-logging", "commons-logging"));
		put(mappingsFallback, "org.apache.commons.math3", new Info("org.apache.commons", "commons-math3"));
		put(mappingsFallback, "org.apache.commons.net", new Info("commons-net", "commons-net"));
		put(mappingsFallback, "org.apache.commons.pool", new Info("commons-pool", "commons-pool"));
		put(mappingsFallback, "org.apache.commons.pool2", new Info("org.apache.commons", "commons-pool2"));
		put(mappingsFallback, "org.apache.felix.framework", new Info("org.apache.felix", "org.apache.felix.framework"));
		put(mappingsFallback, "org.apache.fop", new Info[0]);
		put(mappingsFallback, "org.apache.hadoop.zookeeper", new Info[0]);
		put(mappingsFallback, "org.apache.logging.log4j.core", new Info("org.apache.logging.log4j", "log4j-core"));
		put(mappingsFallback, "org.apache.oro", new Info[0]);
		put(mappingsFallback, "org.apache.pdfbox", new Info("org.apache.pdfbox", "pdfbox"));
		put(mappingsFallback, "org.apache.pdfbox.fontbox", new Info("org.apache.pdfbox", "fontbox"));
		put(mappingsFallback, "org.apache.pdfbox.jempbox", new Info("org.apache.pdfbox", "jempbox"));
		put(mappingsFallback, "org.apache.poi", new Info[0]);
		put(mappingsFallback, "org.apache.poi.ooxml", new Info[0]);
		put(mappingsFallback, "org.apache.sanselan.sanselan", new Info("org.apache.sanselan", "sanselan"));
		put(mappingsFallback, "org.apache.servicemix.bundles.poi", new Info("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.poi"));
		put(mappingsFallback, "org.apache.tika.core", new Info("org.apache.tika", "tika-core"));
		put(mappingsFallback, "org.apache.tika.parsers", new Info("org.apache.tika", "tika-parsers"));
		put(mappingsFallback, "org.apiguardian.api", new Info[0]);
		put(mappingsFallback, "org.aspectj.lang", new Info[0]);
		put(mappingsFallback, "org.glassfish.javax.json", new Info("org.glassfish", "javax.json"));
		put(mappingsFallback, "org.h2", new Info[0]);
		put(mappingsFallback, "org.hamcrest", new Info[0]);
		put(mappingsFallback, "org.hibernate.common.hibernate-commons-annotations", new Info[0]);
		put(mappingsFallback, "org.hibernate.core", new Info[0]);
		put(mappingsFallback, "org.hibernate.ehcache", new Info[0]);
		put(mappingsFallback, "org.hibernate.javax.persistence.hibernate-jpa-2.1-api", new Info[0]);
		put(mappingsFallback, "org.hsqldb.hsqldb", new Info[0]);
		put(mappingsFallback, "org.infinispan.client-hotrod", new Info("org.infinispan", "infinispan-client-hotrod"));
		put(mappingsFallback, "org.infinispan.commons", new Info("org.lucee", "org.infinispan.commons"));
		put(mappingsFallback, "org.infinispan.protostream", new Info("org.infinispan.protostream", "protostream"));
		put(mappingsFallback, "org.infinispan.query-dsl", new Info("org.infinispan", "infinispan-query-dsl"));
		put(mappingsFallback, "org.infinispan.remote-query-client", new Info("org.infinispan", "infinispan-remote-query-client"));
		put(mappingsFallback, "org.jboss.logging.jboss-logging", new Info("org.jboss.logging", "jboss-logging"));
		put(mappingsFallback, "org.jboss.marshalling.jboss-marshalling-osgi", new Info("org.jboss.marshalling", "jboss-marshalling-osgi"));
		put(mappingsFallback, "org.jfree.chart", new Info("org.jfree", "jfreechart"));
		put(mappingsFallback, "org.jfree.common", new Info("org.jfree", "jcommon"));
		put(mappingsFallback, "org.joda.time", new Info("joda-time", "joda-time"));
		put(mappingsFallback, "org.jsoup", new Info("org.jsoup", "jsoup"));
		put(mappingsFallback, "org.jsr-305", new Info("com.google.code.findbugs", "jsr305"));
		put(mappingsFallback, "org.lucee.antisamy", new Info("org.lucee", "antisamy"));
		put(mappingsFallback, "org.lucee.antlr", new Info("org.lucee", "antlr"));
		put(mappingsFallback, "org.lucee.argon2", new Info("org.lucee", "argon2"));
		put(mappingsFallback, "org.lucee.aws-core", new Info("org.lucee", "awscore"));
		put(mappingsFallback, "org.lucee.aws-java-sdk-core", new Info("org.lucee", "aws-java-sdk-core"));
		put(mappingsFallback, "org.lucee.aws-java-sdk-kms", new Info("org.lucee", "aws-java-sdk-kms"));
		put(mappingsFallback, "org.lucee.aws-java-sdk-s3", new Info("org.lucee", "aws-java-sdk-s3"));
		put(mappingsFallback, "org.lucee.aws-java-sdk-s3-all", new Info("org.lucee", "aws-java-sdk-s3-all"));
		put(mappingsFallback, "org.lucee.aws-jmespath-java", new Info("org.lucee", "aws-jmespath-java"));
		put(mappingsFallback, "org.lucee.aws-java-sdk-secretsmanager-all", new Info("org.lucee", "awssecretsmanager"));
		put(mappingsFallback, "org.lucee.aws-jmespath", new Info("org.lucee", "awsjmespath"));
		put(mappingsFallback, "org.lucee.aws-secretsmanager", new Info("org.lucee", "awssecretsmanager"));
		put(mappingsFallback, "org.lucee.axis", new Info("org.lucee", "axis"));
		put(mappingsFallback, "org.lucee.axis.ant", new Info("org.lucee", "axis-ant"));
		put(mappingsFallback, "org.lucee.axis.extension", new Info[0]);
		put(mappingsFallback, "org.lucee.batik", new Info("org.lucee", "batik"));
		put(mappingsFallback, "org.lucee.batikutil", new Info("org.lucee", "batik-util"));
		put(mappingsFallback, "org.lucee.bouncycastle.bcprov", new Info("org.lucee", "bcprov-jdk15on"));
		put(mappingsFallback, "org.lucee.commons.httpclient", new Info("org.lucee", "commons-httpclient"));
		put(mappingsFallback, "org.lucee.commons.compress", new Info("org.lucee", "commons-compress"));
		put(mappingsFallback, "org.lucee.commons.email", new Info("org.lucee", "commons-email"));
		put(mappingsFallback, "org.lucee.commons.fileupload", new Info("org.lucee", "commons-fileupload"));
		put(mappingsFallback, "org.lucee.commons.io", new Info("commons-io", "commons-io"));
		put(mappingsFallback, "org.lucee.commons.lang", new Info("org.lucee", "commons-lang"));
		put(mappingsFallback, "org.lucee.commons.logging", new Info("org.lucee", "commons-logging"));
		put(mappingsFallback, "org.lucee.commons.logging.adapters", new Info("org.lucee", "commons-logging-adapters"));
		put(mappingsFallback, "org.lucee.commons.logging.api", new Info("org.lucee", "commons-logging-api"));
		put(mappingsFallback, "org.lucee.commons.sanselan", new Info("org.lucee", "commons-sanselan"));
		put(mappingsFallback, "org.lucee.dom4j", new Info("org.lucee", "dom4j"));
		put(mappingsFallback, "org.lucee.ehcache", new Info("org.lucee", "ehcache"));
		put(mappingsFallback, "org.lucee.ehcachecore", new Info("org.lucee", "ehcacheCore"));
		put(mappingsFallback, "org.lucee.esapi", new Info("org.lucee", "esapi"));
		put(mappingsFallback, "org.lucee.esapi-logger", new Info[0]);
		put(mappingsFallback, "org.lucee.exasol", new Info("org.lucee", "exasol"));
		put(mappingsFallback, "org.lucee.flyingSaucerCore", new Info("org.lucee", "flyingSaucerCore"));
		put(mappingsFallback, "org.lucee.flyingSaucerPDF", new Info("org.lucee", "flyingSaucerPDF"));
		put(mappingsFallback, "org.lucee.geoip2", new Info("org.lucee", "geoip2"));
		put(mappingsFallback, "org.lucee.gotson-webp", new Info("org.lucee", "gotson-webp"));
		put(mappingsFallback, "org.lucee.h2", new Info("org.lucee", "h2"));
		put(mappingsFallback, "org.lucee.hsqldb", new Info("org.lucee", "hsqldb"));
		put(mappingsFallback, "org.lucee.httpcomponents.httpclient", new Info("org.lucee", "httpcomponents-httpclient"));
		put(mappingsFallback, "org.lucee.httpcomponents.httpcore", new Info("org.lucee", "httpcomponents-httpcore"));
		put(mappingsFallback, "org.lucee.httpcomponents.httpmime", new Info("org.lucee", "httpcomponents-httpmime"));
		put(mappingsFallback, "org.lucee.imgscalr", new Info("org.lucee", "imgscalr"));
		put(mappingsFallback, "org.lucee.itext", new Info("org.lucee", "itext"));
		put(mappingsFallback, "org.lucee.javassist", new Info("org.lucee", "javassist"));
		put(mappingsFallback, "org.lucee.jaxrpc", new Info("org.lucee", "jaxrpc"));
		put(mappingsFallback, "org.lucee.jboss-logging-processor", new Info("org.lucee", "jboss-logging-processor"));
		put(mappingsFallback, "org.lucee.jcip-annotations", new Info("org.lucee", "jcip-annotations"));
		put(mappingsFallback, "org.lucee.jdeparser", new Info("org.lucee", "jboss-jdeparser"));
		put(mappingsFallback, "org.lucee.jets3t", new Info("org.lucee", "jets3t"));
		put(mappingsFallback, "org.lucee.jmagick", new Info("org.lucee", "jmagick"));
		put(mappingsFallback, "org.lucee.jmimemagic", new Info("org.lucee", "jmimemagic"));
		put(mappingsFallback, "org.lucee.jsch", new Info("org.lucee", "jsch"));
		put(mappingsFallback, "org.lucee.jta", new Info("org.lucee", "jta"));
		put(mappingsFallback, "org.lucee.jzlib", new Info("org.lucee", "jzlib"));
		put(mappingsFallback, "org.lucee.launchdarkly", new Info("org.lucee", "launchdarkly"));
		put(mappingsFallback, "org.lucee.launchdarkly-redis", new Info("org.lucee", "launchdarkly-redis"));
		put(mappingsFallback, "org.lucee.log4j-api", new Info("org.lucee", "log4j-api"));
		put(mappingsFallback, "org.lucee.log4j-core", new Info("org.lucee", "log4j-core"));
		put(mappingsFallback, "org.lucee.maxmind-db", new Info("org.lucee", "maxmind-db"));
		put(mappingsFallback, "org.lucee.metadata-extractor", new Info("org.lucee", "metadata-extractor"));
		put(mappingsFallback, "org.lucee.mssql", new Info("org.lucee", "mssql"));
		put(mappingsFallback, "org.lucee.oracle", new Info("org.lucee", "oracle"));
		put(mappingsFallback, "org.lucee.oro", new Info("org.lucee", "oro"));
		put(mappingsFallback, "org.lucee.oswego-concurrent", new Info("org.lucee", "oswego-concurrent"));
		put(mappingsFallback, "org.lucee.pdfbox", new Info("org.lucee", "pdfbox"));
		put(mappingsFallback, "org.lucee.pdfbox-fontbox", new Info("org.lucee", "pdfbox-fontbox"));
		put(mappingsFallback, "org.lucee.poi-ooxml-schemas", new Info("org.lucee", "poi-ooxml-schemas"));
		put(mappingsFallback, "org.lucee.poi-scratchpad", new Info("org.lucee", "poi-scratchpad"));
		put(mappingsFallback, "org.lucee.portlet", new Info("org.lucee", "portlet"));
		put(mappingsFallback, "org.lucee.postgresql", new Info("org.lucee", "postgresql"));
		put(mappingsFallback, "org.lucee.protoparser", new Info("org.lucee", "protoparser"));
		put(mappingsFallback, "org.lucee.saaj", new Info("org.lucee", "saaj"));
		put(mappingsFallback, "org.lucee.sejda-webp", new Info("org.lucee", "sejda-webp"));
		put(mappingsFallback, "org.lucee.software.amazon.ion", new Info("org.lucee", "software.amazon.ion"));
		put(mappingsFallback, "org.lucee.spymemcached", new Info("org.lucee", "spymemcached"));
		put(mappingsFallback, "org.lucee.tika-core", new Info("org.lucee", "tika-core"));
		put(mappingsFallback, "org.lucee.twelvemonkeys.common-image", new Info("org.lucee", "com.twelvemonkeys.common-image"));
		put(mappingsFallback, "org.lucee.twelvemonkeys.common-io", new Info("org.lucee", "com.twelvemonkeys.common-io"));
		put(mappingsFallback, "org.lucee.twelvemonkeys.common-lang", new Info("org.lucee", "com.twelvemonkeys.common-lang"));
		put(mappingsFallback, "org.lucee.twelvemonkeys.imageio-bmp", new Info("org.lucee", "com.twelvemonkeys.imageio-bmp"));
		put(mappingsFallback, "org.lucee.twelvemonkeys.imageio-core", new Info("org.lucee", "com.twelvemonkeys.imageio-core"));
		put(mappingsFallback, "org.lucee.twelvemonkeys.imageio-icns", new Info("org.lucee", "com.twelvemonkeys.imageio-icns"));
		put(mappingsFallback, "org.lucee.twelvemonkeys.imageio-ico", new Info("org.lucee", "com.twelvemonkeys.imageio-ico"));
		put(mappingsFallback, "org.lucee.twelvemonkeys.imageio-jpeg", new Info("org.lucee", "com.twelvemonkeys.imageio-jpeg"));
		put(mappingsFallback, "org.lucee.twelvemonkeys.imageio-metadata", new Info("org.lucee", "com.twelvemonkeys.imageio-metadata"));
		put(mappingsFallback, "org.lucee.twelvemonkeys.imageio-psd", new Info("org.lucee", "com.twelvemonkeys.imageio-psd"));
		put(mappingsFallback, "org.lucee.twelvemonkeys.imageio-tiff", new Info("org.lucee", "com.twelvemonkeys.imageio-tiff"));
		put(mappingsFallback, "org.lucee.twelvemonkeys.imageio-webp", new Info("org.lucee", "com.twelvemonkeys.imageio-webp"));
		put(mappingsFallback, "org.lucee.txtmark", new Info("org.lucee", "txtmark"));
		put(mappingsFallback, "org.lucee.websocket.extension", new Info[0]);
		put(mappingsFallback, "org.lucee.wsdl4j", new Info("org.lucee", "wsdl4j"));
		put(mappingsFallback, "org.lucee.xalan", new Info("org.lucee", "xalan"));
		put(mappingsFallback, "org.lucee.xalan.serializer", new Info("org.lucee", "xalan-serializer"));
		put(mappingsFallback, "org.lucee.xml", new Info("org.lucee", "xml"));
		put(mappingsFallback, "org.lucee.xml.apis", new Info("org.lucee", "xml-apis"));
		put(mappingsFallback, "org.lucee.xml.apisext", new Info("org.lucee", "xml-apis-ext"));
		put(mappingsFallback, "org.lucee.xml.resolver", new Info("org.lucee", "xml-resolver"));
		put(mappingsFallback, "org.lucee.xml.xerces", new Info("org.lucee", "xml-xerces"));
		put(mappingsFallback, "org.lucee.xmlbeans", new Info("org.lucee", "xmlbeans"));
		put(mappingsFallback, "org.lucee.xmpcore", new Info("org.lucee", "xmpcore"));
		put(mappingsFallback, "org.lucee.zip4j", new Info("org.lucee", "zip4j"));
		put(mappingsFallback, "org.mongodb.bson", new Info("org.mongodb", "bson"));
		put(mappingsFallback, "org.mongodb.driver", new Info("org.mongodb", "mongodb-driver"));
		put(mappingsFallback, "org.mongodb.driver-core", new Info("org.mongodb", "mongodb-driver-core"));
		put(mappingsFallback, "org.mongodb.mongo-java-driver", new Info("org.mongodb", "mongo-java-driver"));
		put(mappingsFallback, "org.objectweb.asm.all", new Info[0]);
		put(mappingsFallback, "org.opentest4j", new Info[0]);
		put(mappingsFallback, "org.postgresql.jdbc", new Info("org.postgresql", "postgresql"));
		put(mappingsFallback, "org.postgresql.jdbc40", new Info("org.postgresql", "postgresql"));
		put(mappingsFallback, "org.postgresql.jdbc41", new Info("org.postgresql", "postgresql"));
		put(mappingsFallback, "org.postgresql.jdbc42", new Info("org.postgresql", "postgresql"));

		put(mappingsFallback, "org.xhtmlrenderer.flying.saucer.core", new Info("org.xhtmlrenderer", "flying-saucer-core"));
		put(mappingsFallback, "pdf.extension", new Info[0]);
		put(mappingsFallback, "postgresql", new Info[0]);
		put(mappingsFallback, "redis.clients.jedis", new Info("redis.clients", "jedis"));
		put(mappingsFallback, "redis.extension", new Info[0]);
		put(mappingsFallback, "redissentinel.extension", new Info[0]);
		put(mappingsFallback, "resolver", new Info[0]);
		put(mappingsFallback, "s3.extension", new Info[0]);
		put(mappingsFallback, "sapdbc", new Info[0]);
		put(mappingsFallback, "sentry", new Info("io.sentry", "sentry"));
		put(mappingsFallback, "sentry.extension", new Info[0]);
		put(mappingsFallback, "sentry-log4j", new Info("io.sentry", "sentry-log4j"));
		put(mappingsFallback, "sentry-log4j2", new Info[0]);
		put(mappingsFallback, "serializer", new Info[0]);
		put(mappingsFallback, "slf4j.api", new Info("org.slf4j", "slf4j-api"));
		put(mappingsFallback, "slf4j.nop", new Info("org.slf4j", "slf4j-nop"));
		put(mappingsFallback, "smtp.dumbster", new Info[0]);
		put(mappingsFallback, "software.amazon.ion.java", new Info("software.amazon.ion", "ion-java"));
		put(mappingsFallback, "ss.css2", new Info[0]);
		put(mappingsFallback, "stax.api", new Info[0]);
		put(mappingsFallback, "stax2-api", new Info("org.codehaus.woodstox", "stax2-api"));
		put(mappingsFallback, "sun.activation", new Info[0]);
		put(mappingsFallback, "sun.jai.codec", new Info[0]);
		put(mappingsFallback, "sun.jai.core", new Info[0]);
		put(mappingsFallback, "sun.jndi.ldap", new Info[0]);
		put(mappingsFallback, "sun.jndi.ldapbp", new Info[0]);
		put(mappingsFallback, "sun.jndi.ldapsec", new Info[0]);
		put(mappingsFallback, "sun.jndi.providerutil", new Info[0]);

		put(mappingsFallback, "sun.mail", new Info[0]);
		put(mappingsFallback, "sun.security.jaas", new Info[0]);
		put(mappingsFallback, "sun.xml.jaxrpc", new Info[0]);
		put(mappingsFallback, "sun.xml.saaj", new Info[0]);
		put(mappingsFallback, "sun.xml.wsdl4j", new Info[0]);
		put(mappingsFallback, "tagsoup", new Info[0]);
		put(mappingsFallback, "w3c.dom", new Info[0]);
		put(mappingsFallback, "woodstox-core-asl", new Info[0]);
		put(mappingsFallback, "xdb", new Info[0]);
		put(mappingsFallback, "xml.apis", new Info[0]);
		put(mappingsFallback, "xmlbeans", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.anim", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.awt.util", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.bridge", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.css", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.dom", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.ext", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.extension", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.gvt", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.parser", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.script", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.svg.dom", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.transcoder", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.util", new Info[0]);
		put(mappingsFallback, "xmlgraphics.batik.xml", new Info[0]);
		put(mappingsFallback, "xmlgraphics.commons", new Info[0]);
		put(mappingsFallback, "xmlparserv2", new Info[0]);
		put(mappingsFallback, "xmpcore", new Info[0]);
		put(mappingsFallback, "zip4j", new Info("net.lingala.zip4j", "zip4j"));

		put(mappingsFallback, "org.apache.felix.main", new Info("org.apache.felix", "org.apache.felix.framework"));
		put(mappingsFallback, "org.lucee.janinocc", new Info("org.lucee", "janino-commons-compiler"));
		put(mappingsFallback, "org.apache.commons.commons-fileupload", new Info("commons-fileupload", "commons-fileupload"));
		put(mappingsFallback, "jakarta.activation-api", new Info("jakarta.activation-api", "jakarta.activation"));
		put(mappingsFallback, "jakarta.mail-api", new Info("jakarta.mail-api", "jakarta.mail"));
		put(mappingsFallback, "com.sun.activation.jakarta.activation", new Info("com.sun.activation", "jakarta.activation"));
		put(mappingsFallback, "com.sun.mail.jakarta.mail", new Info("com.sun.mail", "jakarta.mail"));
		put(mappingsFallback, "org.lucee.greenmail", new Info("org.lucee", "com.icegreen"));
		put(mappingsFallback, "org.lucee.jakarta-activation-mail", new Info("com.sun.mail", "jakarta.mail"));
		put(mappingsFallback, "org.lucee.commons-email-all", new Info("org.lucee", "commons-email-all"));
		put(mappingsFallback, "com.github.f4b6a3.ulid", new Info("com.github.f4b6a3", "ulid-creator"));
		put(mappingsFallback, "org.objectweb.asm.tree.analysis", new Info("org.ow2.asm", "asm-analysis"));
		put(mappingsFallback, "org.objectweb.asm.commons", new Info("org.ow2.asm", "asm-commons"));
		put(mappingsFallback, "org.objectweb.asm.util", new Info("org.ow2.asm", "asm-util"));
		put(mappingsFallback, "org.objectweb.asm", new Info("org.ow2.asm", "asm"));
		put(mappingsFallback, "org.objectweb.asm.tree", new Info("org.ow2.asm", "asm-tree"));
		put(mappingsFallback, "com.sun.xml.bind.jaxb-core", new Info("com.sun.istack", "istack-commons-runtime"));
		put(mappingsFallback, "com.sun.xml.bind.jaxb-impl", new Info("org.glassfish.jaxb", "jaxb-runtime"));
		put(mappingsFallback, "org.lucee.commonmark", new Info("org.lucee", "commonmark"));
		put(mappingsFallback, "org.lucee.argon2-jvm-nolibs", new Info("org.lucee", "argon2-jvm-nolibs"));
		put(mappingsFallback, "org.apache.commons.commons-fileupload2-core", new Info("org.apache.commons", "commons-fileupload2-core"));
	}

	private static void put(Map<String, Info[]> mappings, String name, Info... value) {
		if (mappings.containsKey(name)) throw new RuntimeException(name + " already set");

		mappings.put(name, value);
	}

	public static Map<String, Info[]> getMappings(JarFile luceeCore) {
		if (luceeCore == null) return mappingsFallback;

		SoftReference<Map<String, Info[]>> sr = mappingsCollection.get(luceeCore.getName());
		Map<String, Info[]> mappings = sr != null ? sr.get() : null;

		if (mappings == null) {
			synchronized (createToken("getMappings", luceeCore.getName())) {
				sr = mappingsCollection.get(luceeCore.getName());
				mappings = sr != null ? sr.get() : null;
				if (mappings == null) {
					mappings = readIniFile(luceeCore);
					if (mappings == null) mappings = mappingsFallback;
					else mappingsCollection.put(luceeCore.getName(), new SoftReference<Map<String, Info[]>>(mappings));
				}
			}
		}
		return mappings;
	}

	private static String createToken(String prefix, String name) {
		String str = prefix + ":" + name;
		String lock = tokens.putIfAbsent(str, str);
		if (lock == null) {
			lock = str;
		}
		return lock;
	}

	private static URL[] getDefaultProviderDetail() {
		if (defaultProviderDetail == null) {
			String str = Util.getSystemPropOrEnvVar("lucee.s3.bundle.detail", null);
			if (!Util.isEmpty(str, true)) {
				try {
					defaultProviderDetail = new URL[] { new URL(str.trim()) };
				}
				catch (Exception e) {
				}
			}
			if (defaultProviderDetail == null) defaultProviderDetail = DEFAULT_PROVIDER_DETAILSX;
		}
		return defaultProviderDetail;
	}

	private static URL getDefaultProviderDetailMvn() {
		if (defaultProviderDetailMvn == null) {
			String str = Util.getSystemPropOrEnvVar("lucee.mvn.bundle.detail", null);
			if (!Util.isEmpty(str, true)) {
				try {
					defaultProviderDetailMvn = new URL(str.trim());
				}
				catch (Exception e) {
				}
			}
			if (defaultProviderDetailMvn == null) defaultProviderDetailMvn = DEFAULT_PROVIDER_DETAIL_MVN;
		}
		return defaultProviderDetailMvn;
	}

	private BundleProvider(URL[] details) throws MalformedURLException {

		for (int i = 0; i < details.length; i++) {
			if (!details[i].toExternalForm().endsWith("/")) details[i] = new URL(details[i].toExternalForm() + "/");
		}
		this.details = details;
	}

	public static BundleProvider getInstance() throws MalformedURLException {
		return getInstance(getDefaultProviderDetail());
	}

	public static BundleProvider getInstance(URL[] details) throws MalformedURLException {
		String key = toKey(details);
		Pair pair = readers.get(key);
		if (pair != null && pair.lastModified + MAX_AGE > System.currentTimeMillis()) {
			return pair.bundleProvider;
		}
		BundleProvider reader = new BundleProvider(details);
		readers.put(key, new Pair(System.currentTimeMillis(), reader));
		return reader;
	}

	private static String toKey(URL[] details) {
		StringBuilder sb = new StringBuilder();
		for (URL d: details) {
			sb.append(';').append(d.toExternalForm());
		}
		return sb.toString();
	}

	public URL getBundleAsURL(String bundleName, String bundleVersion) throws PageException, IOException, GeneralSecurityException, SAXException, BundleException {
		return getBundleAsURL(null, bundleName, bundleVersion, null);
	}

	public URL getBundleAsURL(CFMLEngineFactory factory, String bundleName, String bundleVersion, final JarFile luceeCore)
			throws PageException, IOException, GeneralSecurityException, SAXException, BundleException {
		BundleDefinition bd = new BundleDefinition(bundleName, bundleVersion);
		URL url = null;

		// MAVEN: looking for a matching mapping, so we can get from maven
		Info[] infos = getMappings(luceeCore).get(bd.name);
		if (infos != null && infos.length > 0) {

			String v;
			for (Info info: infos) {

				if (factory != null) factory.log(org.apache.felix.resolver.Logger.LOG_INFO, "Found Maven/OSGi mapping [" + info + "] for OSGi bundle [" + bd.name + "]");

				if (!info.isMaven()) continue;
				v = bd.getVersionAsString();
				url = validate(createURL(getDefaultProviderDetailMvn(), info, v), null);
				if (url != null) {
					if (factory != null) factory.log(org.apache.felix.resolver.Logger.LOG_INFO, "Found URL [" + url + "] for OSGi bundle [" + bd.name + "]");
					return url;
				}
				if (v != null && v.endsWith(".0")) {
					v = v.substring(0, v.length() - 2);
					url = validate(createURL(getDefaultProviderDetailMvn(), info, v), null);
					if (url != null) {
						if (factory != null) factory.log(org.apache.felix.resolver.Logger.LOG_INFO, "Found URL [" + url + "] for OSGi bundle [" + bd.name + "]");
						return url;
					}
				}
			}

		}
		else {

			int index = bd.name.lastIndexOf('.');
			String last = index == -1 ? bd.name : bd.name.substring(index + 1);

			url = validate(new URL(getDefaultProviderDetailMvn(), bd.name.replace('.', '/') + "/" + bd.version + "/" + last + "-" + bd.version + ".jar"), null);
			if (url != null) return url;
		}
		// S3: we check for a direct match
		for (URL detail: details) {
			url = validate(new URL(detail, bd.name + "-" + bd.version + ".jar"), null);
			if (url != null) return url;
		}

		throw new IOException("no URL found for bundle [" + bd.name + ":" + bd.version + "]");
	}

	private static Map<String, Info[]> readIniFile(JarFile jarFile) {

		JarEntry entry = jarFile.getJarEntry("META-INF/osgi-maven-mapping.ini");
		if (entry == null) {
			return null; // File not found in jar
		}
		Map<String, List<Info>> mappings = new HashMap<>();
		String section = "", key, value;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)))) {
			String line;
			List<Info> infos = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				if (!line.isEmpty()) {
					if (line.startsWith("[") && line.endsWith("]")) {
						section = line.substring(1, line.length() - 1);
						infos = new ArrayList<>();
						mappings.put(section, infos);
					}
					else if (line.contains("=")) {
						int i = line.indexOf('=');
						key = line.substring(0, i).trim();
						value = line.substring(i + 1).trim();

						i = key.indexOf(':');
						int index;
						if (i != -1) {
							index = Integer.parseInt(key.substring(i + 1));
							key = key.substring(0, i);
						}
						else {
							index = 1;
						}

						Info info;
						// set new info
						if (infos.size() < index) {
							info = new Info();
							infos.add(index - 1, info);
						}
						else {
							info = infos.get(index - 1);
						}

						if ("groupId".equals(key)) info.setGroupId(value);
						else if ("artifactId".equals(key)) info.setArtifactId(value);
						else throw new IOException("key [" + key + "] is invalid, only valid keys are [groupId, artifactId]");
						// infos.add(new Info(key, value));
					}
					// Ignore lines that are not key-value pairs or section headers
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, Info[]> rtn = new ConcurrentHashMap<>(mappings.size());
		Info[] arr;
		int index;
		for (Entry<String, List<Info>> e: mappings.entrySet()) {
			arr = new Info[e.getValue().size()];
			index = 0;
			for (Info i: e.getValue()) {
				arr[index++] = i;
				i.setBundleSymbolicName(e.getKey());
			}
			rtn.put(e.getKey(), arr);
		}

		return rtn;
	}

	private static URL validate(URL url, URL defaultValue) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.setConnectTimeout(BundleProvider.CONNECTION_TIMEOUT);
			conn.connect();
			int code = conn.getResponseCode();
			if (code >= 200 && code < 300) return url;
		}
		catch (Exception e) {
		}
		finally {
			if (conn != null) conn.disconnect();
		}

		return defaultValue;
	}

	private static URL createURL(URL base, Info info, String version) throws MalformedURLException {
		return new URL(base, info.groupId.replace('.', '/') + "/" + info.artifactId + "/" + version + "/" + info.artifactId + "-" + version + ".jar");
	}

	public static class Info {
		private String groupId;
		private String artifactId;
		private String bundleSymbolicName;

		public Info() {

		}

		public Info(String groupId, String artifactId) {
			this.groupId = groupId;
			this.artifactId = artifactId;
		}

		public Info(String groupId, String artifactId, String version) {
			this.groupId = groupId;
			this.artifactId = artifactId;
		}

		public Info setGroupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public Info setArtifactId(String artifactId) {
			this.artifactId = artifactId;
			return this;
		}

		public Info setBundleSymbolicName(String bundleSymbolicName) {
			this.bundleSymbolicName = bundleSymbolicName;
			return this;
		}

		public boolean isMaven() {
			return groupId != null && artifactId != null;
		}

		@Override
		public String toString() {
			return String.format("groupId:%s;artifactId:%s;bundleSymbolicName:%s", groupId, artifactId, bundleSymbolicName);
		}
	}

	public static class BundleDefinition {
		private static final int QUALIFIER_APPENDIX_SNAPSHOT = 1;
		private static final int QUALIFIER_APPENDIX_BETA = 2;
		private static final int QUALIFIER_APPENDIX_RC = 3;
		private static final int QUALIFIER_APPENDIX_OTHER = 4;
		private static final int QUALIFIER_APPENDIX_STABLE = 5;
		private String name;
		private Version version;

		public BundleDefinition(String name, Version version) {
			this.name = name;
			this.version = version;
		}

		public BundleDefinition(String name, String version) throws BundleException {
			this.name = name;
			this.version = toVersion(version);
		}

		public String getVersionAsString() {
			return version == null ? null : version.toString();
		}

		public int compare(BundleDefinition bd) {
			// name
			int cmp = name.compareTo(bd.name);
			if (cmp != 0) return cmp;

			// version
			return compare(version, bd.version);
		}

		private static int compare(final Version left, final Version right) {

			// major
			if (left.getMajor() > right.getMajor()) return 100;
			if (left.getMajor() < right.getMajor()) return -100;

			// minor
			if (left.getMinor() > right.getMinor()) return 50;
			if (left.getMinor() < right.getMinor()) return -50;

			// micro
			if (left.getMicro() > right.getMicro()) return 10;
			if (left.getMicro() < right.getMicro()) return -10;

			// qualifier
			// left
			String q = left.getQualifier();
			int index = q.indexOf('-');
			String qla = index == -1 ? "" : q.substring(index + 1).trim();
			String qln = index == -1 ? q : q.substring(0, index);
			int ql = Util.isEmpty(qln) ? Integer.MIN_VALUE : Integer.parseInt(qln);

			// right
			q = right.getQualifier();
			index = q.indexOf('-');
			String qra = index == -1 ? "" : q.substring(index + 1).trim();
			String qrn = index == -1 ? q : q.substring(0, index);
			int qr = Util.isEmpty(qrn) ? Integer.MIN_VALUE : Integer.parseInt(qrn);

			if (ql > qr) return 5;
			if (ql < qr) return -5;

			int qlan = qualifierAppendix2Number(qla);
			int qran = qualifierAppendix2Number(qra);

			if (qlan > qran) return 2;
			if (qlan < qran) return -2;

			if (qlan == QUALIFIER_APPENDIX_OTHER && qran == QUALIFIER_APPENDIX_OTHER) return left.compareTo(right) > 0 ? 1 : -1;

			return 0;
		}

		private static int qualifierAppendix2Number(String str) {
			if (Util.isEmpty(str, true)) return QUALIFIER_APPENDIX_STABLE;
			if ("SNAPSHOT".equalsIgnoreCase(str)) return QUALIFIER_APPENDIX_SNAPSHOT;
			if ("BETA".equalsIgnoreCase(str)) return QUALIFIER_APPENDIX_BETA;
			if ("RC".equalsIgnoreCase(str)) return QUALIFIER_APPENDIX_RC;
			return QUALIFIER_APPENDIX_OTHER;
		}

	}

	private final static class Pair {
		final long lastModified;
		final BundleProvider bundleProvider;

		public Pair(long lastModified, BundleProvider bundleProvider) {
			this.lastModified = lastModified;
			this.bundleProvider = bundleProvider;
		}
	}

	private static Version toVersion(String version, Version defaultValue) {
		if (Util.isEmpty(version)) return defaultValue;
		// String[] arr = ListUtil.listToStringArray(version, '.');
		String[] arr;
		try {

			arr = version.split("\\.");
			;
		}
		catch (Exception e) {
			return defaultValue; // should not happen
		}

		Integer major, minor, micro;
		String qualifier;

		if (arr.length == 1) {
			major = Integer.parseInt(arr[0]);
			minor = 0;
			micro = 0;
			qualifier = null;
		}
		else if (arr.length == 2) {
			major = Integer.parseInt(arr[0]);
			minor = Integer.parseInt(arr[1]);
			micro = 0;
			qualifier = null;
		}
		else if (arr.length == 3) {
			major = Integer.parseInt(arr[0]);
			minor = Integer.parseInt(arr[1]);
			micro = Integer.parseInt(arr[2]);
			qualifier = null;
		}
		else {
			major = Integer.parseInt(arr[0]);
			minor = Integer.parseInt(arr[1]);
			micro = Integer.parseInt(arr[2]);
			qualifier = arr[3];
		}

		if (major == null || minor == null || micro == null) return defaultValue;

		if (qualifier == null) return new Version(major, minor, micro);
		return new Version(major, minor, micro, qualifier);
	}

	private static Version toVersion(String version) throws BundleException {
		Version v = toVersion(version, null);
		if (v != null) return v;
		throw new BundleException(
				"Given version [" + version + "] is invalid, a valid version is following this pattern <major-number>.<minor-number>.<micro-number>[.<qualifier>]");
	}

}
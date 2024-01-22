package lucee.loader.engine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	private static Map<String, Info[]> mappings = new ConcurrentHashMap<>();

	private URL[] details;
	private static Map<String, Pair> readers = new HashMap<>();

	static {
		try {
			DEFAULT_PROVIDER_DETAILSX = new URL[] { new URL("https://bundle-download.s3.amazonaws.com/") };
			DEFAULT_PROVIDER_DETAIL_MVN = new URL("https://repo1.maven.org/maven2/");

		}
		catch (Exception e) {
		}
		put(mappings, "apache.http.components.client", new Info("org.apache.httpcomponents", "httpclient"));
		put(mappings, "apache.http.components.core", new Info("org.apache.httpcomponents", "httpcore"));
		put(mappings, "apache.http.components.mime", new Info("org.apache.httpcomponents", "httpmime"));
		put(mappings, "com.amazonaws.aws-java-sdk-osgi", new Info("com.amazonaws", "aws-java-sdk-osgi"));
		put(mappings, "com.amazonaws.aws.java.sdk.support", new Info("com.amazonaws", "aws-java-sdk-support"));
		put(mappings, "com.google.gson", new Info("com.google.code.gson", "gson"));
		put(mappings, "com.launchdarkly.client", new Info("commons-codec", "commons-codec"));
		put(mappings, "ehcache", new Info("net.sf.ehcache", "ehcache-core"));

		put(mappings, "ESAPI", new Info("org.owasp.esapi", "esapi"));
		put(mappings, "activiti-osgi", new Info("org.activiti", "activiti-osgi"));

		put(mappings, "avalon.framework.api", new Info("org.apache.avalon", "avalon-framework", "1"));
		put(mappings, "com.fasterxml.classmate", new Info("com.fasterxml", "classmate", "1.3.0"));
		put(mappings, "com.fasterxml.jackson.core.jackson-annotations", new Info("com.fasterxml.jackson.core", "jackson-annotations"));
		put(mappings, "com.fasterxml.jackson.core.jackson-core", new Info("com.fasterxml.jackson.core", "jackson-core"));
		put(mappings, "com.fasterxml.jackson.core.jackson-databind", new Info("com.fasterxml.jackson.core", "jackson-databind"));
		put(mappings, "com.fasterxml.jackson.dataformat.jackson-dataformat-cbor", new Info("com.fasterxml.jackson.dataformat", "jackson-dataformat-cbor"));
		put(mappings, "com.github.kirviq.dumbster", new Info("com.github.kirviq", "dumbster"));
		put(mappings, "com.google.guava", new Info("com.google.guava", "guava"));
		put(mappings, "com.google.guava.failureaccess", new Info("com.google.guava", "failureaccess"));
		put(mappings, "com.google.protobuf", new Info("com.google.protobuf", "protobuf-java"));
		put(mappings, "com.googlecode.json-simple", new Info("com.googlecode.json-simple", "json-simple"));
		put(mappings, "com.googlecode.owasp-java-html-sanitizer", new Info("com.googlecode.owasp-java-html-sanitizer", "owasp-java-html-sanitizer"));
		put(mappings, "com.microsoft.sqlserver.mssql-jdbc", new Info("com.microsoft.sqlserver", "mssql-jdbc"));

		put(mappings, "antlr", new Info("antlr", "antlr"));
		put(mappings, "apache.lucene.analyzers", new Info[0]);
		put(mappings, "apache.lucene.analyzers.common", new Info[0]);
		put(mappings, "apache.lucene.core", new Info[0]);
		put(mappings, "apache.lucene.facet", new Info[0]);
		put(mappings, "apache.lucene.queries", new Info[0]);
		put(mappings, "apache.lucene.queryparser", new Info[0]);
		put(mappings, "apache.poi", new Info[0]);
		put(mappings, "apache.poi.ooxml", new Info[0]);
		put(mappings, "apache.poi.ooxml.schemas", new Info[0]);
		put(mappings, "apache.poi.tm.extractors", new Info[0]);
		put(mappings, "apache.ws.axis", new Info[0]);
		put(mappings, "apache.ws.axis.ant", new Info[0]);
		put(mappings, "apache.xml.xalan", new Info[0]);
		put(mappings, "apache.xml.xalan.serializer", new Info[0]);
		put(mappings, "apache.xml.xerces", new Info[0]);
		put(mappings, "backport.util.concurrent", new Info[0]);
		put(mappings, "bcprov", new Info[0]);
		put(mappings, "bcprov.jdk14", new Info[0]);
		put(mappings, "bouncycastle.mail", new Info[0]);
		put(mappings, "bouncycastle.prov", new Info[0]);
		put(mappings, "bouncycastle.tsp", new Info[0]);
		put(mappings, "chart.extension", new Info[0]);
		put(mappings, "checker-qual", new Info("org.checkerframework", "checker-qual"));
		put(mappings, "com.mysql.cj", new Info("com.mysql", "mysql-connector-j"), new Info("mysql", "mysql-connector-java"));
		// put(mappings, "com.mysql.cj", new Info("mysql", "mysql-connector-java"));

		put(mappings, "com.mysql.jdbc", new Info("mysql", "mysql-connector-java"));
		put(mappings, "com.naryx.tagfusion.cfx", new Info[0]);
		put(mappings, "com.sun.jna", new Info("net.java.dev.jna", "jna"));
		put(mappings, "com.teradata.jdbc", new Info[0]);

		put(mappings, "com.teradata.tdgss", new Info[0]);
		put(mappings, "compress.extension", new Info[0]);
		put(mappings, "concurrent", new Info[0]);
		put(mappings, "distrokid.extension", new Info[0]);
		put(mappings, "ehcache.extension", new Info[0]);
		put(mappings, "esapi.extension", new Info[0]);
		put(mappings, "findbugsAnnotations", new Info("com.google.code.findbugs", "annotations"));
		put(mappings, "flex.messaging.common", new Info[0]);
		put(mappings, "flex.messaging.core", new Info[0]);
		put(mappings, "flex.messaging.opt", new Info[0]);
		put(mappings, "flex.messaging.proxy", new Info[0]);
		put(mappings, "flex.messaging.remoting", new Info[0]);
		put(mappings, "flying.saucer.core", new Info("org.xhtmlrenderer", "flying-saucer-core"));
		put(mappings, "flying.saucer.pdf", new Info("org.xhtmlrenderer", "flying-saucer-pdf"));
		put(mappings, "fonts", new Info[0]);
		put(mappings, "form.extension", new Info[0]);
		put(mappings, "fusiondebug.api.server", new Info("com.intergral.fusiondebug", "fusiondebug-api-server"));
		put(mappings, "hibernate", new Info("org.hibernate", "hibernate-core"));
		put(mappings, "hibernate.extension", new Info[0]);
		put(mappings, "hsqldb", new Info[0]);
		put(mappings, "hypersonic.hsqldb", new Info[0]);
		put(mappings, "icepdf.core", new Info[0]);
		put(mappings, "ieffects", new Info[0]);
		put(mappings, "image.extension", new Info[0]);
		put(mappings, "jackson-core-asl", new Info("org.codehaus.jackson", "jackson-core-asl"));
		put(mappings, "jackson-mapper-asl", new Info("org.codehaus.jackson", "jackson-mapper-asl"));
		put(mappings, "jacob", new Info[0]);
		put(mappings, "jandex", new Info("org.jboss", "jandex"));
		put(mappings, "java.xmlbuilder", new Info("com.jamesmurty.utils", "java-xmlbuilder"));
		put(mappings, "javaparser", new Info[0]);
		put(mappings, "javassist", new Info("org.javassist", "javassist"));
		put(mappings, "javasysmon", new Info[0]);
		put(mappings, "javax.activation", new Info[0]);
		put(mappings, "javax.el", new Info[0]);
		put(mappings, "javax.mail", new Info("javax.mail", "mail"));
		put(mappings, "javax.mail-api", new Info("javax.mail", "javax.mail-api"));
		put(mappings, "javax.mail.activation", new Info("javax.mail", "mail"));
		put(mappings, "javax.servlet.jsp-api", new Info("javax.servlet.jsp", "javax.servlet.jsp-api"));
		put(mappings, "javax.websocket-api", new Info("javax.websocket", "javax.websocket-api"));
		put(mappings, "jaxb-api", new Info("javax.xml.bind", "jaxb-api"));
		put(mappings, "jboss.logging.annotations", new Info("org.jboss.logging", "jboss-logging-annotations"));
		put(mappings, "jboss.transaction", new Info("org.jboss.spec.javax.transaction", "jboss-transaction-api_1.2_spec"));
		put(mappings, "jcifs", new Info[0]);
		put(mappings, "jcl.over.slf4j", new Info("org.slf4j", "jcl-over-slf4j"));
		put(mappings, "jcommon", new Info[0]);
		put(mappings, "jencrypt", new Info[0]);
		put(mappings, "jets3t", new Info[0]);
		put(mappings, "jffmpeg", new Info[0]);
		put(mappings, "jfreechart", new Info[0]);
		put(mappings, "jfreechart.patch", new Info[0]);
		put(mappings, "jline", new Info("jline", "jline"));
		put(mappings, "jmimemagic", new Info[0]);
		put(mappings, "joda-convert", new Info("org.joda", "joda-convert"));
		put(mappings, "joda-time", new Info("joda-time", "joda-time"));
		put(mappings, "jpedal.gpl", new Info[0]);
		put(mappings, "jta", new Info("org.jboss.javaee", "jboss-transaction-api"));
		put(mappings, "jtds", new Info[0]);
		put(mappings, "junit", new Info[0]);
		put(mappings, "junit-jupiter", new Info[0]);
		put(mappings, "junit-jupiter-api", new Info[0]);
		put(mappings, "junit-jupiter-engine", new Info[0]);
		put(mappings, "junit-jupiter-params", new Info[0]);
		put(mappings, "junit-platform-commons", new Info[0]);
		put(mappings, "junit-platform-engine", new Info[0]);
		put(mappings, "jython-standalone", new Info("com.carrotsearch", "java-sizeof"));
		put(mappings, "log4j", new Info("log4j", "log4j"));
		put(mappings, "lowagie.itext", new Info[0]);
		put(mappings, "lucee.image.extension", new Info[0]);
		put(mappings, "lucene.search.extension", new Info[0]);
		put(mappings, "memcached", new Info("com.whalin", "Memcached-Java-Client"));
		put(mappings, "memcached.extension", new Info[0]);
		put(mappings, "metadata.extractor", new Info("com.drewnoakes", "metadata-extractor"));
		put(mappings, "microsoft.sqljdbc", new Info[0]);
		put(mappings, "mongodb.extension", new Info[0]);
		put(mappings, "mssqljdbc4", new Info[0]);
		put(mappings, "mx4j", new Info[0]);
		put(mappings, "mx4j.lite", new Info[0]);
		put(mappings, "net.lingala.zip4j", new Info("net.lingala.zip4j", "zip4j"));
		put(mappings, "net.sf.ehcache", new Info("net.sf.ehcache", "ehcache"));
		put(mappings, "net.twentyonesolutions.luceeapps", new Info("net.twentyonesolutions", "lucee-apps"));
		put(mappings, "net.twentyonesolutions.luceewebsocket", new Info("net.twentyonesolutions", "lucee-websocket"));
		put(mappings, "nu.xom", new Info[0]);
		put(mappings, "ojdbc14", new Info[0]);
		put(mappings, "ojdbc6", new Info[0]);
		put(mappings, "ojdbc7", new Info[0]);
		put(mappings, "openamf", new Info[0]);
		put(mappings, "openamf.astranslator", new Info[0]);
		put(mappings, "org.activiti.engine", new Info("org.activiti", "activiti-engine"));
		put(mappings, "org.apache.commons.cli", new Info("commons-cli", "commons-cli"));
		put(mappings, "org.apache.commons.codec", new Info("commons-codec", "commons-codec"));
		put(mappings, "org.apache.commons.collections", new Info("commons-collections", "commons-collections"));
		put(mappings, "org.apache.commons.collections4", new Info("org.apache.commons", "commons-collections4"));
		put(mappings, "org.apache.commons.commons-codec", new Info("commons-codec", "commons-codec"));
		put(mappings, "org.apache.commons.commons-collections4", new Info("org.apache.commons", "commons-collections4"));
		put(mappings, "org.apache.commons.commons-compress", new Info("org.apache.commons", "commons-compress"));
		put(mappings, "org.apache.commons.commons-imaging", new Info("org.apache.commons", "commons-imaging"));
		put(mappings, "org.apache.commons.commons-io", new Info("commons-io", "commons-io"));
		put(mappings, "org.apache.commons.commons-net", new Info("commons-net", "commons-net"));
		put(mappings, "org.apache.commons.commons-pool2", new Info("org.apache.commons", "commons-pool2"));
		put(mappings, "org.apache.commons.commons-text", new Info("org.apache.commons", "commons-text"));
		put(mappings, "org.apache.commons.compress", new Info("org.apache.commons", "commons-compress"));
		put(mappings, "org.apache.commons.discovery", new Info[0]);

		put(mappings, "org.apache.commons.email", new Info("org.apache.commons", "commons-email"));
		put(mappings, "org.apache.commons.fileupload", new Info("commons-fileupload", "commons-fileupload"));
		put(mappings, "org.apache.commons.httpclient", new Info("org.apache.httpcomponents", "httpclient"));
		put(mappings, "org.apache.commons.httpcore", new Info("org.apache.httpcomponents", "httpcore"));
		put(mappings, "org.apache.commons.httpmime", new Info("org.apache.httpcomponents", "httpmime"));
		put(mappings, "org.apache.commons.image", new Info("org.apache.commons", "commons-imaging"));
		put(mappings, "org.apache.commons.io", new Info("commons-io", "commons-io"));
		put(mappings, "org.apache.commons.lang", new Info("commons-lang", "commons-lang"));
		put(mappings, "org.apache.commons.lang3", new Info("org.apache.commons", "commons-lang3"));
		put(mappings, "org.apache.commons.logging", new Info("commons-logging", "commons-logging"));
		put(mappings, "org.apache.commons.logging.adapters", new Info("commons-logging", "commons-logging"));
		put(mappings, "org.apache.commons.logging.api", new Info("commons-logging", "commons-logging"));
		put(mappings, "org.apache.commons.math3", new Info("org.apache.commons", "commons-math3"));
		put(mappings, "org.apache.commons.net", new Info("commons-net", "commons-net"));
		put(mappings, "org.apache.commons.pool", new Info("commons-pool", "commons-pool"));
		put(mappings, "org.apache.commons.pool2", new Info("org.apache.commons", "commons-pool2"));
		put(mappings, "org.apache.felix.framework", new Info("org.apache.felix", "org.apache.felix.framework"));
		put(mappings, "org.apache.fop", new Info[0]);
		put(mappings, "org.apache.hadoop.zookeeper", new Info[0]);
		put(mappings, "org.apache.logging.log4j.core", new Info("org.apache.logging.log4j", "log4j-core"));
		put(mappings, "org.apache.oro", new Info[0]);
		put(mappings, "org.apache.pdfbox", new Info("org.apache.pdfbox", "pdfbox"));
		put(mappings, "org.apache.pdfbox.fontbox", new Info("org.apache.pdfbox", "fontbox"));
		put(mappings, "org.apache.pdfbox.jempbox", new Info("org.apache.pdfbox", "jempbox"));
		put(mappings, "org.apache.poi", new Info[0]);
		put(mappings, "org.apache.poi.ooxml", new Info[0]);
		put(mappings, "org.apache.sanselan.sanselan", new Info("org.apache.sanselan", "sanselan"));
		put(mappings, "org.apache.servicemix.bundles.poi", new Info("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.poi"));
		put(mappings, "org.apache.tika.core", new Info("org.apache.tika", "tika-core"));
		put(mappings, "org.apache.tika.parsers", new Info("org.apache.tika", "tika-parsers"));
		put(mappings, "org.apiguardian.api", new Info[0]);
		put(mappings, "org.aspectj.lang", new Info[0]);
		put(mappings, "org.glassfish.javax.json", new Info("org.glassfish", "javax.json"));
		put(mappings, "org.h2", new Info[0]);
		put(mappings, "org.hamcrest", new Info[0]);
		put(mappings, "org.hibernate.common.hibernate-commons-annotations", new Info[0]);
		put(mappings, "org.hibernate.core", new Info[0]);
		put(mappings, "org.hibernate.ehcache", new Info[0]);
		put(mappings, "org.hibernate.javax.persistence.hibernate-jpa-2.1-api", new Info[0]);
		put(mappings, "org.hsqldb.hsqldb", new Info[0]);
		put(mappings, "org.infinispan.client-hotrod", new Info("org.infinispan", "infinispan-client-hotrod"));
		put(mappings, "org.infinispan.commons", new Info("org.lucee", "org.infinispan.commons"));
		put(mappings, "org.infinispan.protostream", new Info("com.google.protobuf", "protobuf-java"));
		put(mappings, "org.infinispan.query-dsl", new Info("org.infinispan", "infinispan-query-dsl"));
		put(mappings, "org.infinispan.remote-query-client", new Info("org.infinispan", "infinispan-remote-query-client"));
		put(mappings, "org.jboss.logging.jboss-logging", new Info("org.jboss.logging", "jboss-logging"));
		put(mappings, "org.jboss.marshalling.jboss-marshalling-osgi", new Info("org.jboss.marshalling", "jboss-marshalling-osgi"));
		put(mappings, "org.jfree.chart", new Info("org.jfree", "jfreechart"));
		put(mappings, "org.jfree.common", new Info("org.jfree", "jcommon"));
		put(mappings, "org.joda.time", new Info("joda-time", "joda-time"));
		put(mappings, "org.jsoup", new Info("org.jsoup", "jsoup"));
		put(mappings, "org.jsr-305", new Info("com.google.code.findbugs", "jsr305"));
		put(mappings, "org.lucee.antisamy", new Info("org.lucee", "antisamy"));
		put(mappings, "org.lucee.antlr", new Info("org.lucee", "antlr"));
		put(mappings, "org.lucee.argon2", new Info("org.lucee", "argon2"));
		put(mappings, "org.lucee.aws-core", new Info("org.lucee", "awscore"));
		put(mappings, "org.lucee.aws-java-sdk-core", new Info("org.lucee", "aws-java-sdk-core"));
		put(mappings, "org.lucee.aws-java-sdk-kms", new Info("org.lucee", "aws-java-sdk-kms"));
		put(mappings, "org.lucee.aws-java-sdk-s3", new Info("org.lucee", "aws-java-sdk-s3"));
		put(mappings, "org.lucee.aws-java-sdk-s3-all", new Info("org.lucee", "aws-jmespath-java"));
		put(mappings, "org.lucee.aws-jmespath-java", new Info("org.lucee", "aws-jmespath-java"));
		put(mappings, "org.lucee.aws-java-sdk-secretsmanager-all", new Info("org.lucee", "aws-java-sdk-core"));
		put(mappings, "org.lucee.aws-jmespath", new Info("org.lucee", "awsjmespath"));
		put(mappings, "org.lucee.aws-secretsmanager", new Info("org.lucee", "awssecretsmanager"));
		put(mappings, "org.lucee.axis", new Info("org.lucee", "axis"));
		put(mappings, "org.lucee.axis.ant", new Info("org.lucee", "axis-ant"));
		put(mappings, "org.lucee.axis.extension", new Info[0]);
		put(mappings, "org.lucee.batik", new Info("org.lucee", "batik"));
		put(mappings, "org.lucee.batikutil", new Info("org.lucee", "batik-util"));
		put(mappings, "org.lucee.bouncycastle.bcprov", new Info("org.lucee", "bcprov-jdk15on"));
		put(mappings, "org.lucee.commons.httpclient", new Info("org.lucee", "commons-httpclient"));
		put(mappings, "org.lucee.commons.compress", new Info("org.lucee", "commons-compress"));
		put(mappings, "org.lucee.commons.email", new Info("org.lucee", "commons-email"));
		put(mappings, "org.lucee.commons.fileupload", new Info("org.lucee", "commons-fileupload"));
		put(mappings, "org.lucee.commons.io", new Info("commons-io", "commons-io"));
		put(mappings, "org.lucee.commons.lang", new Info("org.lucee", "commons-lang"));
		put(mappings, "org.lucee.commons.logging", new Info("org.lucee", "commons-logging"));
		put(mappings, "org.lucee.commons.logging.adapters", new Info("org.lucee", "commons-logging-adapters"));
		put(mappings, "org.lucee.commons.logging.api", new Info("org.lucee", "commons-logging-api"));
		put(mappings, "org.lucee.commons.sanselan", new Info("org.lucee", "commons-sanselan"));
		put(mappings, "org.lucee.dom4j", new Info("org.lucee", "dom4j"));
		put(mappings, "org.lucee.ehcache", new Info("org.lucee", "ehcache"));
		put(mappings, "org.lucee.ehcachecore", new Info("org.lucee", "ehcacheCore"));
		put(mappings, "org.lucee.esapi", new Info("org.lucee", "esapi"));
		put(mappings, "org.lucee.esapi-logger", new Info[0]);
		put(mappings, "org.lucee.exasol", new Info("org.lucee", "exasol"));
		put(mappings, "org.lucee.flyingSaucerCore", new Info("org.lucee", "flyingSaucerCore"));
		put(mappings, "org.lucee.flyingSaucerPDF", new Info("org.lucee", "flyingSaucerPDF"));
		put(mappings, "org.lucee.geoip2", new Info("org.lucee", "geoip2"));
		put(mappings, "org.lucee.gotson-webp", new Info("org.lucee", "gotson-webp"));
		put(mappings, "org.lucee.h2", new Info("org.lucee", "h2"));
		put(mappings, "org.lucee.hsqldb", new Info("org.lucee", "hsqldb"));
		put(mappings, "org.lucee.httpcomponents.httpclient", new Info("org.lucee", "httpcomponents-httpclient"));
		put(mappings, "org.lucee.httpcomponents.httpcore", new Info("org.lucee", "httpcomponents-httpcore"));
		put(mappings, "org.lucee.httpcomponents.httpmime", new Info("org.lucee", "httpcomponents-httpmime"));
		put(mappings, "org.lucee.imgscalr", new Info("org.lucee", "imgscalr"));
		put(mappings, "org.lucee.itext", new Info("org.lucee", "itext"));
		put(mappings, "org.lucee.javassist", new Info("org.lucee", "javassist"));
		put(mappings, "org.lucee.jaxrpc", new Info("org.lucee", "jaxrpc"));
		put(mappings, "org.lucee.jboss-logging-processor", new Info("org.lucee", "jboss-logging-processor"));
		put(mappings, "org.lucee.jcip-annotations", new Info("org.lucee", "jcip-annotations"));
		put(mappings, "org.lucee.jdeparser", new Info("org.lucee", "jboss-jdeparser"));
		put(mappings, "org.lucee.jets3t", new Info("org.lucee", "jets3t"));
		put(mappings, "org.lucee.jmagick", new Info("org.lucee", "jmagick"));
		put(mappings, "org.lucee.jmimemagic", new Info("org.lucee", "jmimemagic"));
		put(mappings, "org.lucee.jsch", new Info("org.lucee", "jsch"));
		put(mappings, "org.lucee.jta", new Info("org.lucee", "jta"));
		put(mappings, "org.lucee.jzlib", new Info("org.lucee", "jzlib"));
		put(mappings, "org.lucee.launchdarkly", new Info("org.lucee", "launchdarkly"));
		put(mappings, "org.lucee.launchdarkly-redis", new Info("org.lucee", "launchdarkly-redis"));
		put(mappings, "org.lucee.log4j-api", new Info("org.lucee", "log4j-api"));
		put(mappings, "org.lucee.log4j-core", new Info("org.lucee", "log4j-core"));
		put(mappings, "org.lucee.maxmind-db", new Info("org.lucee", "maxmind-db"));
		put(mappings, "org.lucee.metadata-extractor", new Info("org.lucee", "metadata-extractor"));
		put(mappings, "org.lucee.mssql", new Info("org.lucee", "mssql"));
		put(mappings, "org.lucee.oracle", new Info("org.lucee", "oracle"));
		put(mappings, "org.lucee.oro", new Info("org.lucee", "oro"));
		put(mappings, "org.lucee.oswego-concurrent", new Info("org.lucee", "oswego-concurrent"));
		put(mappings, "org.lucee.pdfbox", new Info("org.lucee", "pdfbox"));
		put(mappings, "org.lucee.pdfbox-fontbox", new Info("org.lucee", "pdfbox-fontbox"));
		put(mappings, "org.lucee.poi-ooxml-schemas", new Info("org.lucee", "poi-ooxml-schemas"));
		put(mappings, "org.lucee.poi-scratchpad", new Info("org.lucee", "poi-scratchpad"));
		put(mappings, "org.lucee.portlet", new Info("org.lucee", "portlet"));
		put(mappings, "org.lucee.postgresql", new Info("org.lucee", "postgresql"));
		put(mappings, "org.lucee.protoparser", new Info("org.lucee", "protoparser"));
		put(mappings, "org.lucee.saaj", new Info("org.lucee", "saaj"));
		put(mappings, "org.lucee.sejda-webp", new Info("org.lucee", "sejda-webp"));
		put(mappings, "org.lucee.software.amazon.ion", new Info("org.lucee", "software.amazon.ion"));
		put(mappings, "org.lucee.spymemcached", new Info("org.lucee", "spymemcached"));
		put(mappings, "org.lucee.tika-core", new Info("org.lucee", "tika-core"));
		put(mappings, "org.lucee.twelvemonkeys.common-image", new Info("org.lucee", "com.twelvemonkeys.common-image"));
		put(mappings, "org.lucee.twelvemonkeys.common-io", new Info("org.lucee", "com.twelvemonkeys.common-io"));
		put(mappings, "org.lucee.twelvemonkeys.common-lang", new Info("org.lucee", "com.twelvemonkeys.common-lang"));
		put(mappings, "org.lucee.twelvemonkeys.imageio-bmp", new Info("org.lucee", "com.twelvemonkeys.imageio-bmp"));
		put(mappings, "org.lucee.twelvemonkeys.imageio-core", new Info("org.lucee", "com.twelvemonkeys.imageio-core"));
		put(mappings, "org.lucee.twelvemonkeys.imageio-icns", new Info("org.lucee", "com.twelvemonkeys.imageio-icns"));
		put(mappings, "org.lucee.twelvemonkeys.imageio-ico", new Info("org.lucee", "com.twelvemonkeys.imageio-ico"));
		put(mappings, "org.lucee.twelvemonkeys.imageio-jpeg", new Info("org.lucee", "com.twelvemonkeys.imageio-jpeg"));
		put(mappings, "org.lucee.twelvemonkeys.imageio-metadata", new Info("org.lucee", "com.twelvemonkeys.imageio-metadata"));
		put(mappings, "org.lucee.twelvemonkeys.imageio-psd", new Info("org.lucee", "com.twelvemonkeys.imageio-psd"));
		put(mappings, "org.lucee.twelvemonkeys.imageio-tiff", new Info("org.lucee", "com.twelvemonkeys.imageio-tiff"));
		put(mappings, "org.lucee.twelvemonkeys.imageio-webp", new Info("org.lucee", "com.twelvemonkeys.imageio-webp"));
		put(mappings, "org.lucee.txtmark", new Info("org.lucee", "txtmark"));
		put(mappings, "org.lucee.websocket.extension", new Info[0]);
		put(mappings, "org.lucee.wsdl4j", new Info("org.lucee", "wsdl4j"));
		put(mappings, "org.lucee.xalan", new Info("org.lucee", "xalan"));
		put(mappings, "org.lucee.xalan.serializer", new Info("org.lucee", "xalan-serializer"));
		put(mappings, "org.lucee.xml", new Info("org.lucee", "xml"));
		put(mappings, "org.lucee.xml.apis", new Info("org.lucee", "xml-apis"));
		put(mappings, "org.lucee.xml.apisext", new Info("org.lucee", "xml-apis-ext"));
		put(mappings, "org.lucee.xml.resolver", new Info("org.lucee", "xml-resolver"));
		put(mappings, "org.lucee.xml.xerces", new Info("org.lucee", "xml-xerces"));
		put(mappings, "org.lucee.xmlbeans", new Info("org.lucee", "xmlbeans"));
		put(mappings, "org.lucee.xmpcore", new Info("org.lucee", "xmpcore"));
		put(mappings, "org.lucee.zip4j", new Info("org.lucee", "zip4j"));
		put(mappings, "org.mongodb.bson", new Info("org.mongodb", "bson"));
		put(mappings, "org.mongodb.driver", new Info("org.mongodb", "mongodb-driver"));
		put(mappings, "org.mongodb.driver-core", new Info("org.mongodb", "mongodb-driver-core"));
		put(mappings, "org.mongodb.mongo-java-driver", new Info("org.mongodb", "mongo-java-driver"));
		put(mappings, "org.objectweb.asm.all", new Info[0]);
		put(mappings, "org.opentest4j", new Info[0]);
		put(mappings, "org.postgresql.jdbc", new Info("org.postgresql", "postgresql"));
		put(mappings, "org.postgresql.jdbc40", new Info("org.postgresql", "postgresql"));
		put(mappings, "org.postgresql.jdbc41", new Info("org.postgresql", "postgresql"));
		put(mappings, "org.postgresql.jdbc42", new Info("org.postgresql", "postgresql"));

		put(mappings, "org.xhtmlrenderer.flying.saucer.core", new Info("org.xhtmlrenderer", "flying-saucer-core"));
		put(mappings, "pdf.extension", new Info[0]);
		put(mappings, "postgresql", new Info[0]);
		put(mappings, "redis.clients.jedis", new Info("redis.clients", "jedis"));
		put(mappings, "redis.extension", new Info[0]);
		put(mappings, "redissentinel.extension", new Info[0]);
		put(mappings, "resolver", new Info[0]);
		put(mappings, "s3.extension", new Info[0]);
		put(mappings, "sapdbc", new Info[0]);
		put(mappings, "sentry", new Info("io.sentry", "sentry"));
		put(mappings, "sentry.extension", new Info[0]);
		put(mappings, "sentry-log4j", new Info("io.sentry", "sentry-log4j"));
		put(mappings, "sentry-log4j2", new Info[0]);
		put(mappings, "serializer", new Info[0]);
		put(mappings, "slf4j.api", new Info("org.slf4j", "slf4j-api"));
		put(mappings, "slf4j.nop", new Info("org.slf4j", "slf4j-nop"));
		put(mappings, "smtp.dumbster", new Info[0]);
		put(mappings, "software.amazon.ion.java", new Info("software.amazon.ion", "ion-java"));
		put(mappings, "ss.css2", new Info[0]);
		put(mappings, "stax.api", new Info[0]);
		put(mappings, "stax2-api", new Info("org.codehaus.woodstox", "stax2-api"));
		put(mappings, "sun.activation", new Info[0]);
		put(mappings, "sun.jai.codec", new Info[0]);
		put(mappings, "sun.jai.core", new Info[0]);
		put(mappings, "sun.jndi.ldap", new Info[0]);
		put(mappings, "sun.jndi.ldapbp", new Info[0]);
		put(mappings, "sun.jndi.ldapsec", new Info[0]);
		put(mappings, "sun.jndi.providerutil", new Info[0]);

		put(mappings, "sun.mail", new Info[0]);
		put(mappings, "sun.security.jaas", new Info[0]);
		put(mappings, "sun.xml.jaxrpc", new Info[0]);
		put(mappings, "sun.xml.saaj", new Info[0]);
		put(mappings, "sun.xml.wsdl4j", new Info[0]);
		put(mappings, "tagsoup", new Info[0]);
		put(mappings, "w3c.dom", new Info[0]);
		put(mappings, "woodstox-core-asl", new Info[0]);
		put(mappings, "xdb", new Info[0]);
		put(mappings, "xml.apis", new Info[0]);
		put(mappings, "xmlbeans", new Info[0]);
		put(mappings, "xmlgraphics.batik.anim", new Info[0]);
		put(mappings, "xmlgraphics.batik.awt.util", new Info[0]);
		put(mappings, "xmlgraphics.batik.bridge", new Info[0]);
		put(mappings, "xmlgraphics.batik.css", new Info[0]);
		put(mappings, "xmlgraphics.batik.dom", new Info[0]);
		put(mappings, "xmlgraphics.batik.ext", new Info[0]);
		put(mappings, "xmlgraphics.batik.extension", new Info[0]);
		put(mappings, "xmlgraphics.batik.gvt", new Info[0]);
		put(mappings, "xmlgraphics.batik.parser", new Info[0]);
		put(mappings, "xmlgraphics.batik.script", new Info[0]);
		put(mappings, "xmlgraphics.batik.svg.dom", new Info[0]);
		put(mappings, "xmlgraphics.batik.transcoder", new Info[0]);
		put(mappings, "xmlgraphics.batik.util", new Info[0]);
		put(mappings, "xmlgraphics.batik.xml", new Info[0]);
		put(mappings, "xmlgraphics.commons", new Info[0]);
		put(mappings, "xmlparserv2", new Info[0]);
		put(mappings, "xmpcore", new Info[0]);
		put(mappings, "zip4j", new Info("net.lingala.zip4j", "zip4j"));

		put(mappings, "org.apache.felix.main", new Info("org.apache.felix", "org.apache.felix.framework"));
		put(mappings, "org.lucee.janinocc", new Info("org.lucee", "janino-commons-compiler"));
		put(mappings, "org.apache.commons.commons-fileupload", new Info("commons-fileupload", "commons-fileupload"));
	}

	private static void put(Map<String, Info[]> mappings, String name, Info... value) {
		if (mappings.containsKey(name)) throw new RuntimeException(name + " already set");

		mappings.put(name, value);
	}

	private static URL[] getDefaultProviderDetail() {
		if (defaultProviderDetail == null) {
			String str = getSystemPropOrEnvVar("lucee.s3.bundle.detail", null);
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
			String str = getSystemPropOrEnvVar("lucee.mvn.bundle.detail", null);
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
		BundleDefinition bd = new BundleDefinition(bundleName, bundleVersion);
		URL url = null;

		// MAVEN: looking for a matching mapping, so we can get from maven
		Info[] infos = mappings.get(bd.name);
		if (infos != null && infos.length > 0) {
			String v;
			for (Info info: infos) {
				if (!info.isMaven()) continue;
				v = bd.getVersionAsString();
				url = validate(createURL(getDefaultProviderDetailMvn(), info, v), null);
				if (url != null) return url;
				if (v != null && v.endsWith(".0")) {
					v = v.substring(0, v.length() - 2);
					url = validate(createURL(getDefaultProviderDetailMvn(), info, v), null);
					if (url != null) return url;
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

	private static class Info {
		private String groupId;
		private String artifactId;
		private String bundleSymbolicName;

		public Info(String groupId, String artifactId) {
			this.groupId = groupId;
			this.artifactId = artifactId;
		}

		public Info(String groupId, String artifactId, String version) {
			this.groupId = groupId;
			this.artifactId = artifactId;
		}

		public boolean isMaven() {
			return groupId != null && artifactId != null;
		}

		@Override
		public String toString() {
			return String.format("groupId:%s;artifactId:%s;bundleSymbolicName:%s", groupId, artifactId, bundleSymbolicName);
		}
	}

	private static String getSystemPropOrEnvVar(String string, Object object) {
		// TODO Auto-generated method stub
		return null;
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
			int qr = Util.isEmpty(qln) ? Integer.MIN_VALUE : Integer.parseInt(qrn);

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
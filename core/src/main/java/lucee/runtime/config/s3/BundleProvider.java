package lucee.runtime.config.s3;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.CodeSource;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.felix.framework.Logger;
import org.osgi.framework.Version;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.print;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.osgi.BundleLoader;
import lucee.loader.util.Util;
import lucee.runtime.config.maven.MavenUpdateProvider;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.Pack200Util;
import lucee.transformer.library.function.FunctionLibEntityResolver;
import lucee.transformer.library.function.FunctionLibException;

public final class BundleProvider extends DefaultHandler {
	public static final int CONNECTION_TIMEOUT = 1000;
	private static final long MAX_AGE = 10000;
	private static final int MAX_REDIRECTS = 10;

	private static URL DEFAULT_PROVIDER_LIST = null;
	private static URL[] DEFAULT_PROVIDER_DETAILS = null;
	private static URL DEFAULT_PROVIDER_DETAIL_MVN = null;

	private static URL defaultProviderList;
	private static URL[] defaultProviderDetail;
	private static URL defaultProviderDetailMvn;
	private static Map<String, Info[]> mappings = new ConcurrentHashMap<>();

	static {
		try {
			DEFAULT_PROVIDER_LIST = new URL("https://bundle-download.s3.amazonaws.com/");
			DEFAULT_PROVIDER_DETAILS = new URL[] { DEFAULT_PROVIDER_LIST };
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
		put(mappings, "memcached", new Info[0]); // there is one on maven, but that one has no OSGi data
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

		/**
		 * ATTENTION we have the same mappings in the loader, ad them there as well
		 * 
		 * MUST add to external file
		 */
	}

	public static URL getDefaultProviderList() {
		if (defaultProviderList == null) {
			String str = SystemUtil.getSystemPropOrEnvVar("lucee.s3.bundle.list", null);
			if (!StringUtil.isEmpty(str, true)) {
				try {
					defaultProviderList = new URL(str.trim());
				}
				catch (Exception e) {
				}
			}
			if (defaultProviderList == null) defaultProviderList = DEFAULT_PROVIDER_LIST;
		}
		return defaultProviderList;
	}

	private static void put(Map<String, Info[]> mappings, String name, Info... value) {
		if (mappings.containsKey(name)) throw new RuntimeException(name + " already set");

		mappings.put(name, value);
	}

	public static URL[] getDefaultProviderDetail() {
		if (defaultProviderDetail == null) {
			String str = SystemUtil.getSystemPropOrEnvVar("lucee.s3.bundle.detail", null);
			if (!StringUtil.isEmpty(str, true)) {
				try {
					defaultProviderDetail = new URL[] { new URL(str.trim()) };
				}
				catch (Exception e) {
				}
			}
			if (defaultProviderDetail == null) defaultProviderDetail = DEFAULT_PROVIDER_DETAILS;
		}
		return defaultProviderDetail;
	}

	public static URL getDefaultProviderDetailMvn() {
		if (defaultProviderDetailMvn == null) {
			String str = SystemUtil.getSystemPropOrEnvVar("lucee.mvn.bundle.detail", null);
			if (!StringUtil.isEmpty(str, true)) {
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

	private XMLReader xmlReader;
	private Stack<String> tree = new Stack<>();
	private StringBuilder content = new StringBuilder();
	private final URL url;
	private boolean insideContents;
	private Map<String, Element> elements = new LinkedHashMap<>();
	private Element element;
	private boolean isTruncated;
	private String lastKey;

	private URL[] details;

	private static Map<String, Pair<Long, BundleProvider>> readers = new HashMap<>();

	private BundleProvider(URL list, URL[] details) throws MalformedURLException {

		if (!list.toExternalForm().endsWith("/")) this.url = new URL(list.toExternalForm() + "/");
		else this.url = list;

		for (int i = 0; i < details.length; i++) {
			if (!details[i].toExternalForm().endsWith("/")) details[i] = new URL(details[i].toExternalForm() + "/");
		}
		this.details = details;
	}

	public static BundleProvider getInstance() throws MalformedURLException {
		return getInstance(getDefaultProviderList(), getDefaultProviderDetail());
	}

	public static BundleProvider getInstance(URL list, URL[] details) throws MalformedURLException {
		String key = toKey(list, details);
		Pair<Long, BundleProvider> pair = readers.get(key);
		if (pair != null && pair.getName().longValue() + MAX_AGE > System.currentTimeMillis()) {
			return pair.getValue();
		}
		BundleProvider reader = new BundleProvider(list, details);
		readers.put(key, new Pair<Long, BundleProvider>(System.currentTimeMillis(), reader));
		return reader;
	}

	private static String toKey(URL list, URL[] details) {
		StringBuilder sb = new StringBuilder().append(list.toExternalForm());
		for (URL d: details) {
			sb.append(';').append(d.toExternalForm());
		}
		return sb.toString();
	}

	public URL getBundleAsURL(BundleDefinition bd, boolean includeS3) throws PageException, MalformedURLException, IOException {
		URL url = _getBundleAsURL(bd, includeS3);
		print.e("download:" + url);
		return url;
	}

	public URL _getBundleAsURL(BundleDefinition bd, boolean includeS3) throws PageException, MalformedURLException, IOException {
		URL url = null;

		// MAVEN: looking for a matching mapping, so we can get from maven
		Info[] infos = mappings.get(bd.getName());
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
			String last = ListUtil.last(bd.getName(), '.');
			url = validate(
					new URL(getDefaultProviderDetailMvn(), bd.getName().replace('.', '/') + "/" + bd.getVersionAsString() + "/" + last + "-" + bd.getVersionAsString() + ".jar"),
					null);
			if (url != null) return url;
		}
		if (!includeS3) throw new IOException("no URL found for bundle [" + bd + "]");
		// S3: we check for a direct match
		for (URL detail: details) {
			url = validate(new URL(detail, bd.getName() + "-" + bd.getVersionAsString() + ".jar"), null);
			if (url != null) return url;
		}

		// S3: we loop through all records and S3 and pick one
		if (url == null) {
			try {
				for (Element e: read()) {
					if (bd.equals(e.getBundleDefinition())) {
						url = e.getJAR();
						if (url != null) return url;
					}
				}
			}
			catch (Exception e) {
				throw ExceptionUtil.toIOException(e);
			}
		}

		throw new IOException("no URL found for bundle [" + bd + "]");
	}

	public InputStream getBundleAsStream(BundleDefinition bd) throws PageException, MalformedURLException, IOException, GeneralSecurityException, SAXException {
		URL url = getBundleAsURL(bd, true);

		if (url != null) {
			HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
			if (rsp != null) {
				int sc = rsp.getStatusCode();
				if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + url + "], status code [" + sc + "]");
			}
			else {
				throw new IOException("unable to invoke [" + url + "], no response.");
			}
			return rsp.getContentAsStream();
		}
		throw new IOException("no bundle found for bundle [" + bd + "]");

	}

	public File downloadBundle(BundleDefinition bd) throws IOException, PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		final File jarDir = eng.getCFMLEngineFactory().getBundleDirectory();

		// before we download we check if we have it bundled
		File jar = deployBundledBundle(jarDir, bd.getName(), bd.getVersionAsString());
		if (jar != null && jar.isFile()) return jar;
		if (jar != null) {
			LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download",
					jar + " should exist but does not (exist?" + jar.exists() + ";file?" + jar.isFile() + ";hidden?" + jar.isHidden() + ")");

		}

		String str = Util._getSystemPropOrEnvVar("lucee.enable.bundle.download", null);
		if (str != null && ("false".equalsIgnoreCase(str) || "no".equalsIgnoreCase(str))) { // we do not use CFMLEngine to cast, because the engine may not exist yet
			throw (new RuntimeException("Lucee is missing the Bundle jar, " + bd.getName() + ":" + bd.getVersionAsString()
					+ ", and has been prevented from downloading it. If this jar is not a core jar, it will need to be manually downloaded and placed in the {{lucee-server}}/context/bundles directory."));
		}

		jar = new File(jarDir, bd.getName() + "-" + bd.getVersionAsString() + (".jar"));

		final URL updateUrl = getBundleAsURL(bd, true);

		LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download",
				"Downloading bundle [" + bd.getName() + ":" + bd.getVersionAsString() + "] from " + updateUrl + " and copying to " + jar);

		int code;
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) updateUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(10000);
			conn.connect();
			code = conn.getResponseCode();
		}
		catch (UnknownHostException e) {
			LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download",
					"Failed to download the bundle  [" + bd.getName() + ":" + bd.getVersionAsString() + "] from [" + updateUrl + "] and copy to [" + jar + "]");
			// remove
			throw new IOException("Failed to download the bundle  [" + bd.getName() + ":" + bd.getVersionAsString() + "] from [" + updateUrl + "] and copy to [" + jar + "]", e);
		}
		// the update provider is not providing a download for this
		if (code != 200) {

			// the update provider can also provide a different (final) location for this
			int count = 1;
			while ((code == 302 || code == 301) && count++ <= MAX_REDIRECTS) {
				String location = conn.getHeaderField("Location");
				// just in case we check invalid names
				if (location == null) location = conn.getHeaderField("location");
				if (location == null) location = conn.getHeaderField("LOCATION");
				LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download", "download redirected:" + location);

				conn.disconnect();
				URL url = new URL(location);
				try {
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(10000);
					conn.connect();
					code = conn.getResponseCode();
				}
				catch (final UnknownHostException e) {
					LogUtil.log("deploy", "bundle-download", e);
					throw new IOException("Failed to download the bundle  [" + bd.getName() + ":" + bd.getVersionAsString() + "] from [" + location + "] and copy to [" + jar + "]",
							e);
				}
			}

			// no download available!
			if (code != 200) {
				final String msg = "Failed to download the bundle for [" + bd.getName() + "] in version [" + bd.getVersionAsString() + "] from [" + updateUrl
						+ "], please download manually and copy to [" + jarDir + "]";
				LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download", msg);
				conn.disconnect();
				throw new IOException(msg);
			}

		}

		IOUtil.copy((InputStream) conn.getContent(), new FileOutputStream(jar), true, true);
		conn.disconnect();
		return jar;
		/*
		 * } else { throw new IOException("File ["+jar.getName()+"] already exists, won't copy new one"); }
		 */
	}

	private File deployBundledBundle(File bundleDirectory, String symbolicName, String symbolicVersion) {
		String sub = "bundles/";
		String nameAndVersion = symbolicName + "|" + symbolicVersion;
		String osgiFileName = symbolicName + "-" + symbolicVersion + ".jar";
		String pack20Ext = ".jar.pack.gz";
		boolean isPack200 = false;

		// first we look for an exact match
		InputStream is = getClass().getResourceAsStream("bundles/" + osgiFileName);
		if (is == null) is = getClass().getResourceAsStream("/bundles/" + osgiFileName);

		if (is != null) LogUtil.log(Logger.LOG_DEBUG, "deploy", "bundle-download", "Found ]/bundles/" + osgiFileName + "] in lucee.jar");
		else LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download", "Could not find [/bundles/" + osgiFileName + "] in lucee.jar");

		if (is == null) {
			is = getClass().getResourceAsStream("bundles/" + osgiFileName + pack20Ext);
			if (is == null) is = getClass().getResourceAsStream("/bundles/" + osgiFileName + pack20Ext);
			isPack200 = true;

			if (is != null) LogUtil.log(Logger.LOG_DEBUG, "deploy", "bundle-download", "Found [/bundles/" + osgiFileName + pack20Ext + "] in lucee.jar");
			else LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download", "Could not find [/bundles/" + osgiFileName + pack20Ext + "] in lucee.jar");
		}
		if (is != null) {
			File temp = null;
			try {
				// copy to temp file
				temp = File.createTempFile("bundle", ".tmp");
				LogUtil.log(Logger.LOG_DEBUG, "deploy", "bundle-download", "Copying [lucee.jar!/bundles/" + osgiFileName + pack20Ext + "] to [" + temp + "]");
				Util.copy(new BufferedInputStream(is), new FileOutputStream(temp), true, true);

				if (isPack200) {
					File temp2 = File.createTempFile("bundle", ".tmp2");
					Pack200Util.pack2Jar(temp, temp2);
					LogUtil.log(Logger.LOG_DEBUG, "deploy", "bundle-download", "Upack [" + temp + "] to [" + temp2 + "]");
					temp.delete();
					temp = temp2;
				}

				// adding bundle
				File trg = new File(bundleDirectory, osgiFileName);
				FileUtil.move(temp, trg);
				LogUtil.log(Logger.LOG_DEBUG, "deploy", "bundle-download", "Adding bundle [" + symbolicName + "] in version [" + symbolicVersion + "] to [" + trg + "]");
				return trg;
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
			finally {
				if (temp != null && temp.exists()) temp.delete();
			}
		}

		// now we search the current jar as an external zip what is slow (we do not support pack200 in this
		// case)
		// this also not works with windows
		if (SystemUtil.isWindows()) return null;
		ZipEntry entry;
		File temp;
		ZipInputStream zis = null;
		try {
			CodeSource src = CFMLEngineFactory.class.getProtectionDomain().getCodeSource();
			if (src == null) return null;
			URL loc = src.getLocation();

			zis = new ZipInputStream(loc.openStream());
			String path, name, bundleInfo;
			int index;
			while ((entry = zis.getNextEntry()) != null) {
				temp = null;
				path = entry.getName().replace('\\', '/');
				if (path.startsWith("/")) path = path.substring(1); // some zip path start with "/" some not
				isPack200 = false;
				if (path.startsWith(sub) && (path.endsWith(".jar") /* || (isPack200=path.endsWith(".jar.pack.gz")) */)) { // ignore non jar files or file from elsewhere
					index = path.lastIndexOf('/') + 1;
					if (index == sub.length()) { // ignore sub directories
						name = path.substring(index);
						temp = null;
						try {
							temp = File.createTempFile("bundle", ".tmp");
							Util.copy(zis, new FileOutputStream(temp), false, true);

							/*
							 * if(isPack200) { File temp2 = File.createTempFile("bundle", ".tmp2"); Pack200Util.pack2Jar(temp,
							 * temp2); temp.delete(); temp=temp2; name=name.substring(0,name.length()-".pack.gz".length()); }
							 */

							bundleInfo = BundleLoader.loadBundleInfo(temp);
							if (bundleInfo != null && nameAndVersion.equals(bundleInfo)) {
								File trg = new File(bundleDirectory, name);
								temp.renameTo(trg);
								LogUtil.log(Logger.LOG_DEBUG, "deploy", "bundle-download",
										"Adding bundle [" + symbolicName + "] in version [" + symbolicVersion + "] to [" + trg + "]");

								return trg;
							}
						}
						finally {
							if (temp != null && temp.exists()) temp.delete();
						}

					}
				}
				zis.closeEntry();
			}
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
		}
		finally {
			Util.closeEL(zis);
		}
		return null;
	}

	// 1146:size:305198;bundle:name:xmlgraphics.batik.awt.util;version:version EQ 1.8.0;;last-mod:{ts
	// '2024-01-14 22:32:05'};

	public List<Element> read() throws IOException, GeneralSecurityException, SAXException, PageException {
		int count = 100;
		URL url = null;

		if (lastKey != null) url = new URL(this.url.toExternalForm() + "?marker=" + lastKey);

		do {
			if (url == null) url = isTruncated ? new URL(this.url.toExternalForm() + "?marker=" + this.lastKey) : this.url;
			HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, BundleProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
			if (rsp != null) {
				int sc = rsp.getStatusCode();
				if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + url + "], status code [" + sc + "]");
			}
			else {
				throw new IOException("unable to invoke [" + url + "], no response.");
			}

			Reader r = null;
			try {
				init(new InputSource(r = IOUtil.getReader(rsp.getContentAsStream(), (Charset) null)));
			}
			finally {
				url = null;
				IOUtil.close(r);
			}

		}
		while (isTruncated || --count == 0);

		List<Element> list = new ArrayList<>();
		for (Element e: elements.values()) {
			list.add(e);
		}

		Collections.sort(list, new Comparator<Element>() {
			@Override
			public int compare(Element l, Element r) {
				int cmp = l.getBundleDefinition().getName().compareTo(r.getBundleDefinition().getName());
				if (cmp != 0) return cmp;
				return OSGiUtil.compare(l.getBundleDefinition().getVersion(), r.getBundleDefinition().getVersion());
			}
		});

		return list;
	}

	/**
	 * Generelle Initialisierungsmetode der Konstruktoren.
	 * 
	 * @param saxParser String Klassenpfad zum Sax Parser.
	 * @param is InputStream auf die TLD.
	 * @throws SAXException
	 * @throws IOException
	 * @throws FunctionLibException
	 */
	private void init(InputSource is) throws SAXException, IOException {
		xmlReader = XMLUtil.createXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.setErrorHandler(this);
		xmlReader.setEntityResolver(new FunctionLibEntityResolver());
		xmlReader.parse(is);

	}

	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
		tree.add(qName);
		if (tree.size() == 2 && "Contents".equals(name)) {
			insideContents = true;
			element = new Element(details);
		}

	}

	@Override
	public void endElement(String uri, String name, String qName) {
		if (tree.size() == 2 && "Contents".equals(name)) {
			insideContents = false;
			if (element.validExtension()) {
				BundleDefinition bd = element.getBundleDefinition();
				if (bd != null) {
					Element existing = elements.get(bd.toString());
					if (existing != null) existing.addKey(element.keys.get(0));
					else {
						element.bd = bd;
						elements.put(bd.toString(), element);
					}
				}
			}
		}
		else if (insideContents) {
			if ("Key".equals(name)) {
				lastKey = content.toString().trim();

				element.addKey(lastKey);
			}
			else if ("LastModified".equals(name)) element.setLastModified(content.toString().trim());
			else if ("ETag".equals(name)) element.setETag(content.toString().trim());
			else if ("Size".equals(name)) element.setSize(content.toString().trim());
		}

		// meta data
		if (tree.size() == 2 && "IsTruncated".equals(name)) {
			isTruncated = Caster.toBooleanValue(content.toString().trim(), false);
			// String tmp = content.toString();
			// if (!StringUtil.isEmpty(tmp, true)) tmpMeta.put(name, tmp.trim());
		}

		content.delete(0, content.length());
		tree.pop();

	}

	@Override
	public void characters(char ch[], int start, int length) {
		content.append(ch, start, length);
	}

	public static URL validate(URL url, URL defaultValue) {
		try {
			HTTPResponse rsp = HTTPEngine4Impl.head(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
			if (rsp != null) {
				int sc = rsp.getStatusCode();
				if (sc >= 200 && sc < 300) return url;
			}
		}
		catch (Exception e) {

		}
		return defaultValue;
	}

	public static class Element {
		private List<String> keys = new ArrayList<>();
		private long size;
		private String etag;
		private DateTime lastMod;
		private BundleDefinition bd;
		private URL[] details;
		private URL jar;

		public Element(URL[] details) {
			this.details = details;
		}

		public boolean validExtension() {
			for (String k: keys) {
				if (k.endsWith(".jar")) return true;
			}
			return false;
		}

		/*
		 * <>4.5.3.020.lco</Key> <LastModified>2018-05-28T20:10:55.000Z</LastModified>
		 * <ETag>"08e36e85b35f6f41380b6b013fc68b97"</ETag> <Size>9718200</Size>
		 */
		public void addKey(String key) {
			keys.add(key);

		}

		public void setSize(String size) {
			this.size = Caster.toLongValue(size, 0L);
		}

		public long getSize() {
			return this.size;
		}

		public URL getJAR() throws MalformedURLException {
			if (jar == null) {
				initFiles();
			}
			return jar;
		}

		private void initFiles() throws MalformedURLException {
			for (String k: keys) {
				if (k.endsWith(".jar")) jar = validate(k);
			}
		}

		private URL validate(String k) throws MalformedURLException {
			URL url;
			for (URL d: details) {
				url = BundleProvider.validate(new URL(d.toExternalForm() + k), null);
				if (url != null) return url;
			}
			return null;
		}

		public void setETag(String etag) {
			this.etag = StringUtil.unwrap(etag);
		}

		public String getETag() {
			return etag;
		}

		public void setLastModified(String lm) {
			this.lastMod = DateCaster.toDateAdvanced(lm, (TimeZone) null, null);
			// 2023-10-20T10:03:41.000Z
			// TODO
		}

		public DateTime getLastModifed() {
			return lastMod;
		}

		public static BundleDefinition toBundleDefinition(String version, BundleDefinition defaultValue) {
			if (!version.endsWith(".jar")) return defaultValue;
			version = version.substring(0, version.length() - 4);

			// an OSGi version number can have "-" ony in the last part, so to find the complete version number
			// we execlude the last part from the search
			int index = version.lastIndexOf('.');
			if (index == -1) return defaultValue;
			String v = version.substring(0, index);
			index = v.lastIndexOf('-');
			if (index == -1) return defaultValue;

			String symbolicName = version.substring(0, index);
			Version symbolicVersion = OSGiUtil.toVersion(version.substring(index + 1), null);
			if (symbolicVersion == null) return defaultValue;
			return new BundleDefinition(symbolicName, symbolicVersion);
		}

		public BundleDefinition getBundleDefinition() {
			if (bd == null) {
				BundleDefinition tmp;
				for (String k: keys) {
					tmp = toBundleDefinition(k, null);
					if (tmp != null) {
						bd = tmp;
						break;
					}
				}
			}
			return bd;
		}

		@Override
		public String toString() {
			return new StringBuilder().append("size:").append(size).append(";bundle:").append(getBundleDefinition()).append(";last-mod:").append(lastMod).toString();
		}
	}

	public static void main(String[] args) throws Exception {
		// memcached)(bundle-version>=3.0.2
		print.e(getInstance().getBundleAsURL(new BundleDefinition("org.lucee.spymemcached", "2.12.3.0001"), true));
		// getInstance().getBundle(new BundleDefinition("com.mysql.cj", "8.0.33"));
		// getInstance().getBundle(new BundleDefinition("com.mysql.cj", "8.0.27"));
		getInstance().createOSGiMavenMapping(); // create java code for OSGi to Maven mapping based on
		// what is in the S3 bucket
		// getInstance().whatcanBeRemovedFromS3(); // show what records can be removed from S3 bucket
		// because we have a working link to Maven

	}

	public void createOSGiMavenMapping() throws PageException, IOException, GeneralSecurityException, SAXException {
		Struct sct = new StructImpl();
		Set<String> has = new HashSet<>();
		Info[] infos;
		Info info;
		for (Element e: read()) {
			infos = mappings.get(e.bd.getName());
			if (infos != null) continue;
			try {
				info = extractMavenInfoFromZip(getBundleAsStream(e.bd));
				if (!info.isOSGi()) {
					info.setBundleSymbolicName(e.bd.getName());
				}
				else if (!e.bd.getName().equals(info.getBundleSymbolicName())) {
					print.e("!!! file name [" + e.bd.getName() + "-" + e.bd.getVersionAsString() + ".jar] differs from symbolic name [" + info.getBundleSymbolicName()
							+ "] in the Manifest.MF");

				}

				if (!has.contains(info.bundleSymbolicName) && info.isComplete()) {
					has.add(info.bundleSymbolicName);
					print.e("put(mappings,\"" + info.bundleSymbolicName + "\", new Info(\"" + info.groupId + "\", \"" + info.artifactId + "\"));");

					info.add(sct);
				}
				else if (!info.isComplete()) {
					// print.e("// put(mappings,\"" + info.bundleSymbolicName + "\", new Info(\"\", \"\"));");
				}
			}
			catch (Exception ex) {
				print.e(e.bd.getName() + ":" + e.bd.getVersionAsString());
				ex.printStackTrace();
			}
		}
	}

	public void whatcanBeRemovedFromS3() throws PageException, IOException, GeneralSecurityException, SAXException {
		URL url;
		for (Element e: read()) {
			url = getBundleAsURL(e.bd, false);

			if (url != null) {
				print.e("// " + (url.toExternalForm()));
				print.e("print.e(getInstance().getBundleAsURL(new BundleDefinition(\"" + e.bd.getName() + "\", \"" + e.bd.getVersion() + "\"),null,true));");

				getInstance().getBundleAsStream(new BundleDefinition(e.bd.getName(), e.bd.getVersion()));
			}

			// name:apache.http.components.mime;version:version EQ 4.4.1;
			// lucee.runtime.config.s3.S3BundleProvider$Info[]{groupId:org.apache.httpcomponents;artifactId:httpcomponents-client;bundleSymbolicName:null

			// name:apache.http.components.mime;version:version EQ 4.5.0;
			// lucee.runtime.config.s3.S3BundleProvider$Info[]{groupId:org.apache.httpcomponents;artifactId:httpcomponents-client;bundleSymbolicName:null

			// https://repo1.maven.org/maven2/org/apache/httpcomponents/httpmime/4.4.1/httpmime-4.4.1.jar
		}
	}

	private static URL createURL(URL base, Info info, String version) throws MalformedURLException {
		return new URL(base, info.groupId.replace('.', '/') + "/" + info.artifactId + "/" + version + "/" + info.artifactId + "-" + version + ".jar");
	}

	public static Info extractMavenInfoFromZip(InputStream is) throws IOException {
		Info info = new Info();

		try (ZipInputStream zipStream = new ZipInputStream(is)) {
			String name;
			ZipEntry entry;
			boolean hasMaven = false;
			String n, g, a, v, content;
			int start, end;
			while ((entry = zipStream.getNextEntry()) != null) {
				name = improve(entry.getName());
				// read Maven properties
				if (!hasMaven && (name).startsWith("META-INF/maven/") && (name).endsWith("/pom.properties")) {

					// Create a Properties object
					Properties prop = new Properties();

					// Load properties from the InputStream
					prop.load(new ByteArrayInputStream(readEntry(zipStream)));
					g = prop.getProperty("groupId");
					a = prop.getProperty("artifactId");
					v = prop.getProperty("version");

					if (!Util.isEmpty(g) && !Util.isEmpty(a) && !Util.isEmpty(v)) {
						info.setGroupId(g);
						info.setArtifactId(a);
						// info.setVersion(v);
						hasMaven = true;
					}
				}
				// read Maven xml
				if (!hasMaven && (name).startsWith("META-INF/maven/") && (name).endsWith("/pom.xml")) {
					content = new String(readEntry(zipStream));
					start = content.indexOf("</parent>");
					if (start == -1) start = 0;
					start = content.indexOf("<groupId>", start);
					end = content.indexOf("</groupId>");
					g = null;
					a = null;
					v = null;
					if (start != -1 && end > start) {
						g = content.substring(start + 9, end);
					}
					start = content.indexOf("<artifactId>", end);
					end = content.indexOf("</artifactId>", end);
					if (start != -1 && end > start) {
						a = content.substring(start + 12, end);
					}
					start = content.indexOf("<version>", end);
					end = content.indexOf("</version>", end);
					if (start != -1 && end > start) {
						v = content.substring(start + 9, end);
					}
					if (!Util.isEmpty(g) && !Util.isEmpty(a) && !Util.isEmpty(v)) {
						info.setGroupId(g);
						info.setArtifactId(a);
						// info.setVersion(v);
						hasMaven = true;
					}
				}

				// read Manifest
				if (entry.getName().equals("META-INF/MANIFEST.MF")) {

					Manifest manifest = new Manifest(new ByteArrayInputStream(readEntry(zipStream)));
					java.util.jar.Attributes attr = manifest.getMainAttributes();
					// id = unwrap(attr.getValue("mapping-id"));
					n = StringUtil.unwrap(attr.getValue("Bundle-SymbolicName"));
					v = StringUtil.unwrap(attr.getValue("Bundle-Version"));
					if (!Util.isEmpty(n, true) && !Util.isEmpty(v, true)) {
						info.setBundleSymbolicName(n);
					}
				}
			}
		}

		return info;
	}

	private static byte[] readEntry(ZipInputStream zipStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int bytesRead;

		while ((bytesRead = zipStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}

		return outputStream.toByteArray();
	}

	private static String improve(String name) {
		if (name == null) return "";
		name = name.replace('\\', '/');
		if (name.startsWith("/")) name = name.substring(1);
		if (name.endsWith("/")) name = name.substring(0, name.length() - 1);
		return name;
	}

	private static class Info {
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

		public String getGroupId() {
			return groupId;
		}

		public void add(Struct sct) throws PageException {
			if (!isComplete()) return;
			Struct data = new StructImpl();
			data.set("groupid", getGroupId());
			data.set("artifactid", getArtifactId());
			data.set("bundleSymbolicName", getBundleSymbolicName());

			sct.set(getBundleSymbolicName(), data);
		}

		public void setGroupId(String groupId) {
			this.groupId = groupId;
		}

		public String getArtifactId() {
			return artifactId;
		}

		public void setArtifactId(String artifactId) {
			this.artifactId = artifactId;
		}

		public boolean isComplete() {
			return isMaven() && isOSGi();
		}

		public boolean isMaven() {
			return groupId != null && artifactId != null;
		}

		public boolean isOSGi() {
			return bundleSymbolicName != null;
		}

		public String getBundleSymbolicName() {
			return bundleSymbolicName;
		}

		public void setBundleSymbolicName(String bundleSymbolicName) {
			this.bundleSymbolicName = bundleSymbolicName;
		}

		@Override
		public String toString() {
			return String.format("groupId:%s;artifactId:%s;bundleSymbolicName:%s", groupId, artifactId, bundleSymbolicName);
		}
	}
}
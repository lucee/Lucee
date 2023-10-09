package lucee.runtime.config.maven;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.transformer.library.function.FunctionLibEntityResolver;
import lucee.transformer.library.function.FunctionLibException;

public final class PomReader extends DefaultHandler {

	private XMLReader xmlReader;
	private Stack<String> tree = new Stack<>();
	private StringBuilder content = new StringBuilder();
	private Map<String, Object> tmpMeta = new HashMap<>();
	private URL url;

	PomReader(URL url) {
		this.url = url;
	}

	public Map<String, Object> read() throws IOException, GeneralSecurityException, SAXException, PageException {

		HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
		if (rsp != null) {
			int sc = rsp.getStatusCode();
			if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + url + "], status code [" + sc + "]");
		}
		else {
			throw new IOException("unable to invoke [" + url + "], no response.");
		}
		Header[] headers = rsp.getAllHeaders();

		Reader r = null;
		try {
			init(new InputSource(r = IOUtil.getReader(rsp.getContentAsStream(), (Charset) null)));
		}
		finally {
			IOUtil.close(r);
		}

		for (Header h: headers) {
			if ("Last-Modified".equals(h.getName()) || "Date".equals(h.getName())) tmpMeta.put(h.getName(), DateCaster.toDateAdvanced(h.getValue(), null));
			else tmpMeta.put(h.getName(), h.getValue());
		}

		return tmpMeta;
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
	}

	/*
	 * ,"modelVersion":xml.XmlRoot.modelVersion.XmlText ,"groupId":xml.XmlRoot.groupId.XmlText
	 * ,"artifactId":xml.XmlRoot.artifactId.XmlText ,"version":xml.XmlRoot.version.XmlText
	 * ,"name":xml.XmlRoot.name.XmlText ,"description":xml.XmlRoot.description.XmlText
	 * ,"groupId":xml.XmlRoot.groupId.XmlText
	 */
	@Override
	public void endElement(String uri, String name, String qName) {
		// print.e(tree.size() + ":" + name + ":" + content.toString().trim());
		// meta data
		if (tree.size() == 2) {
			String tmp = content.toString();
			if (!StringUtil.isEmpty(tmp, true)) tmpMeta.put(name, tmp.trim());
		}

		content.delete(0, content.length());
		tree.pop();

	}

	@Override
	public void characters(char ch[], int start, int length) {
		content.append(ch, start, length);
	}

}
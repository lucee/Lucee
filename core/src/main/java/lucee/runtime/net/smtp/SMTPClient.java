/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime.net.smtp;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;

import com.sun.mail.smtp.SMTPMessage;

import lucee.commons.activation.ResourceDataSource;
import lucee.commons.digest.MD5;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.mail.MailException;
import lucee.runtime.net.mail.MailPart;
import lucee.runtime.net.mail.MailUtil;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.mail.ServerImpl;
import lucee.runtime.net.proxy.Proxy;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.net.smtp.SMTPConnectionPool.SessionAndTransport;
import lucee.runtime.op.Caster;
import lucee.runtime.spooler.ComponentSpoolerTaskListener;
import lucee.runtime.spooler.SpoolerTask;
import lucee.runtime.spooler.SpoolerTaskListener;
import lucee.runtime.spooler.UDFSpoolerTaskListener;
import lucee.runtime.spooler.mail.MailSpoolerTask;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

public final class SMTPClient implements Serializable {

	private static final long serialVersionUID = 5227282806519740328L;

	private static final int SPOOL_UNDEFINED = 0;
	private static final int SPOOL_YES = 1;
	private static final int SPOOL_NO = 2;

	private static final int SSL_NONE = 0;
	private static final int SSL_YES = 1;
	private static final int SSL_NO = 2;

	private static final int TLS_NONE = 0;
	private static final int TLS_YES = 1;
	private static final int TLS_NO = 2;

	private static final String TEXT_HTML = "text/html";
	private static final String TEXT_PLAIN = "text/plain";
	// private static final SerializableObject LOCK = new SerializableObject();

	private static Map<TimeZone, SoftReference<SimpleDateFormat>> formatters = new ConcurrentHashMap<TimeZone, SoftReference<SimpleDateFormat>>();
	// private static final int PORT = 25;

	private int spool = SPOOL_UNDEFINED;

	private int timeout = -1;

	private String plainText;
	private CharSet plainTextCharset;

	private String htmlText;
	private CharSet htmlTextCharset;

	private Attachment[] attachmentz;

	private String[] host;
	private CharSet charset = CharSet.UTF8;
	private InternetAddress from;
	private InternetAddress[] tos;
	private InternetAddress[] bccs;
	private InternetAddress[] ccs;
	private InternetAddress[] rts;
	private InternetAddress[] fts;
	private String subject = "";
	private String xmailer = Constants.NAME + " Mail";
	private Map<String, String> headers = new HashMap<String, String>();
	private int port = -1;

	private String username;
	private String password = "";

	private int ssl = SSL_NONE;
	private int tls = TLS_NONE;

	ProxyData proxyData = new ProxyDataImpl();
	private ArrayList<MailPart> parts;

	private TimeZone timeZone;
	private long lifeTimespan = 100 * 60 * 5;
	private long idleTimespan = 100 * 60 * 1;

	private Object listener;

	public static String getNow(TimeZone tz) {
		tz = ThreadLocalPageContext.getTimeZone(tz);
		SoftReference<SimpleDateFormat> tmp = formatters.get(tz);
		SimpleDateFormat df = tmp == null ? null : tmp.get();
		if (df == null) {
			df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", Locale.US);
			df.setTimeZone(tz);
			formatters.put(tz, new SoftReference<SimpleDateFormat>(df));
		}
		return df.format(new Date());
	}

	public void setSpoolenable(boolean spoolenable) {
		spool = spoolenable ? SPOOL_YES : SPOOL_NO;
	}

	/**
	 * set port of the mailserver
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @param charset the charset to set
	 */
	public void setCharset(Charset charset) {
		this.charset = CharsetUtil.toCharSet(charset);
	}

	public void setCharSet(CharSet charset) {
		this.charset = charset;
	}

	public static ServerImpl toServerImpl(String server, int port, String usr, String pwd, long lifeTimespan, long idleTimespan) throws MailException {
		int index;

		// username/password
		index = server.indexOf('@');
		if (index != -1) {
			usr = server.substring(0, index);
			server = server.substring(index + 1);
			index = usr.indexOf(':');
			if (index != -1) {
				pwd = usr.substring(index + 1);
				usr = usr.substring(0, index);
			}
		}

		// port
		index = server.indexOf(':');
		if (index != -1) {
			try {
				port = Caster.toIntValue(server.substring(index + 1));
			}
			catch (ExpressionException e) {
				throw new MailException(e.getMessage());
			}
			server = server.substring(0, index);
		}

		ServerImpl srv = ServerImpl.getInstance(server, port, usr, pwd, lifeTimespan, idleTimespan, false, false);
		return srv;
	}

	public void setHost(String host) throws PageException {
		if (!StringUtil.isEmpty(host, true)) this.host = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(host, ','));
	}

	public void setLifeTimespan(long life) {
		this.lifeTimespan = life;
	}

	public void setIdleTimespan(long idle) {
		this.idleTimespan = idle;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	public void addTo(InternetAddress to) {
		tos = add(tos, to);
	}

	public void addTo(Object to) throws UnsupportedEncodingException, PageException, MailException {
		InternetAddress[] tmp = MailUtil.toInternetAddresses(to);
		for (int i = 0; i < tmp.length; i++) {
			addTo(tmp[i]);
		}
	}

	public void setFrom(InternetAddress from) {
		this.from = from;
	}

	public void setFrom(Object from) throws UnsupportedEncodingException, MailException, PageException {
		InternetAddress[] addrs = MailUtil.toInternetAddresses(from);
		if (addrs.length == 0) return;
		setFrom(addrs[0]);
	}

	public void addBCC(InternetAddress bcc) {
		bccs = add(bccs, bcc);
	}

	public void addBCC(Object bcc) throws UnsupportedEncodingException, MailException, PageException {
		InternetAddress[] tmp = MailUtil.toInternetAddresses(bcc);
		for (int i = 0; i < tmp.length; i++) {
			addBCC(tmp[i]);
		}
	}

	public void addCC(InternetAddress cc) {
		ccs = add(ccs, cc);
	}

	public void addCC(Object cc) throws UnsupportedEncodingException, MailException, PageException {
		InternetAddress[] tmp = MailUtil.toInternetAddresses(cc);
		for (int i = 0; i < tmp.length; i++) {
			addCC(tmp[i]);
		}
	}

	public void addReplyTo(InternetAddress rt) {
		rts = add(rts, rt);
	}

	public void addReplyTo(Object rt) throws UnsupportedEncodingException, MailException, PageException {
		InternetAddress[] tmp = MailUtil.toInternetAddresses(rt);
		for (int i = 0; i < tmp.length; i++) {
			addReplyTo(tmp[i]);
		}
	}

	public void addFailTo(InternetAddress ft) {
		fts = add(fts, ft);
	}

	public String getHTMLTextAsString() {
		return htmlText;
	}

	public String getPlainTextAsString() {
		return plainText;
	}

	public void addFailTo(Object ft) throws UnsupportedEncodingException, MailException, PageException {
		InternetAddress[] tmp = MailUtil.toInternetAddresses(ft);
		for (int i = 0; i < tmp.length; i++) {
			addFailTo(tmp[i]);
		}
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setXMailer(String xmailer) {
		this.xmailer = xmailer;
	}

	public void setListener(Object listener) throws ApplicationException {
		if (!(listener instanceof UDF) && !(listener instanceof Component) && !dblUDF(listener)) throw new ApplicationException("Listener must be a Function or a Component.");
		this.listener = listener;
	}

	private boolean dblUDF(Object o) {
		if (!(o instanceof Struct)) return false;
		Struct sct = (Struct) o;
		return sct.get("before", null) instanceof UDF || sct.get("after", null) instanceof UDF; // we need "before" OR "after"!
	}

	/**
	 * creates a new expanded array and return it;
	 * 
	 * @param oldArr
	 * @param newValue
	 * @return new expanded array
	 */
	protected static InternetAddress[] add(InternetAddress[] oldArr, InternetAddress newValue) {
		if (oldArr == null) return new InternetAddress[] { newValue };
		// else {
		InternetAddress[] tmp = new InternetAddress[oldArr.length + 1];
		for (int i = 0; i < oldArr.length; i++) {
			tmp[i] = oldArr[i];
		}
		tmp[oldArr.length] = newValue;
		return tmp;
		// }
	}

	protected static Attachment[] add(Attachment[] oldArr, Attachment newValue) {
		if (oldArr == null) return new Attachment[] { newValue };
		// else {
		Attachment[] tmp = new Attachment[oldArr.length + 1];
		for (int i = 0; i < oldArr.length; i++) {
			tmp[i] = oldArr[i];
		}
		tmp[oldArr.length] = newValue;
		return tmp;
		// }
	}

	public static class MimeMessageAndSession {
		public final MimeMessage message;
		public final SessionAndTransport session;

		public MimeMessageAndSession(MimeMessage message, SessionAndTransport session) {
			this.message = message;
			this.session = session;
		}
	}

	private MimeMessageAndSession createMimeMessage(lucee.runtime.config.Config config, String hostName, int port, String username, String password, long lifeTimesan,
			long idleTimespan, boolean tls, boolean ssl, boolean sendPartial, boolean newConnection, boolean userset) throws MessagingException {

		Properties props = (Properties) System.getProperties().clone();
		String strTimeout = Caster.toString(getTimeout(config));

		props.put("mail.smtp.host", hostName);
		props.put("mail.smtp.timeout", strTimeout);
		props.put("mail.smtp.connectiontimeout", strTimeout);
		props.put("mail.smtp.sendpartial", Caster.toString(sendPartial));
		props.put("mail.smtp.userset", userset);

		if (port > 0) {
			props.put("mail.smtp.port", Caster.toString(port));
		}
		if (ssl) {
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.port", Caster.toString(port));
			props.put("mail.smtp.socketFactory.fallback", "false");
		}
		else {
			props.put("mail.smtp.socketFactory.class", "javax.net.SocketFactory");
			props.remove("mail.smtp.socketFactory.port");
			props.remove("mail.smtp.socketFactory.fallback");
		}
		Authenticator auth = null;
		if (!StringUtil.isEmpty(username)) {
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", tls ? "true" : "false");

			props.put("mail.smtp.user", username);
			props.put("mail.smtp.password", password);
			props.put("password", password);
			auth = new SMTPAuthenticator(username, password);
		}
		else {
			props.put("mail.smtp.auth", "false");
			props.remove("mail.smtp.starttls.enable");

			props.remove("mail.smtp.user");
			props.remove("mail.smtp.password");
			props.remove("password");
		}

		SessionAndTransport sat = newConnection ? new SessionAndTransport(hash(props), props, auth, lifeTimesan, idleTimespan)
				: SMTPConnectionPool.getSessionAndTransport(props, hash(props), auth, lifeTimesan, idleTimespan);

		// Contacts
		SMTPMessage msg = new SMTPMessage(sat.session);
		if (from == null) throw new MessagingException("A [from] email address is required to send an email");
		// if(tos==null)throw new MessagingException("A [to] email address is required to send an email");

		checkAddress(from, charset);
		// checkAddress(tos,charset);

		msg.setFrom(from);
		// msg.setRecipients(Message.RecipientType.TO, tos);

		if (tos != null) {
			checkAddress(tos, charset);
			msg.setRecipients(Message.RecipientType.TO, tos);
		}
		if (ccs != null) {
			checkAddress(ccs, charset);
			msg.setRecipients(Message.RecipientType.CC, ccs);
		}
		if (bccs != null) {
			checkAddress(bccs, charset);
			msg.setRecipients(Message.RecipientType.BCC, bccs);
		}
		if (rts != null) {
			checkAddress(rts, charset);
			msg.setReplyTo(rts);
		}
		if (fts != null) {
			checkAddress(fts, charset);
			msg.setEnvelopeFrom(fts[0].toString());
		}

		// Subject and headers
		try {
			msg.setSubject(MailUtil.encode(subject, charset.name()));
		}
		catch (UnsupportedEncodingException e) {
			throw new MessagingException("the encoding " + charset + " is not supported");
		}
		msg.setHeader("X-Mailer", xmailer);

		msg.setHeader("Date", getNow(timeZone));

		Multipart mp = null;

		// only Plain
		if (StringUtil.isEmpty(htmlText)) {
			if (ArrayUtil.isEmpty(attachmentz) && ArrayUtil.isEmpty(parts)) {
				fillPlainText(config, msg);
				setHeaders(msg, headers);
				return new MimeMessageAndSession(msg, sat);
			}
			mp = new MimeMultipart("mixed");
			mp.addBodyPart(getPlainText(config));
		}
		// Only HTML
		else if (StringUtil.isEmpty(plainText)) {
			if (ArrayUtil.isEmpty(attachmentz) && ArrayUtil.isEmpty(parts)) {
				fillHTMLText(config, msg);
				setHeaders(msg, headers);
				return new MimeMessageAndSession(msg, sat);
			}
			mp = new MimeMultipart("mixed");
			mp.addBodyPart(getHTMLText(config));
		}

		// Plain and HTML
		else {
			mp = new MimeMultipart("alternative");
			mp.addBodyPart(getPlainText(config));
			mp.addBodyPart(getHTMLText(config));// this need to be last

			if (!ArrayUtil.isEmpty(attachmentz) || !ArrayUtil.isEmpty(parts)) {
				MimeBodyPart content = new MimeBodyPart();
				content.setContent(mp);
				mp = new MimeMultipart("mixed");
				mp.addBodyPart(content);
			}
		}
		/*
		 * - mixed -- alternative --- text --- related ---- html ---- inline image ---- inline image --
		 * attachment -- attachment
		 * 
		 */

		// parts
		if (!ArrayUtil.isEmpty(parts)) {
			Iterator<MailPart> it = parts.iterator();
			if (mp instanceof MimeMultipart) ((MimeMultipart) mp).setSubType("alternative");
			while (it.hasNext()) {
				mp.addBodyPart(toMimeBodyPart(config, it.next()));
			}
		}

		// Attachments
		if (!ArrayUtil.isEmpty(attachmentz)) {
			for (int i = 0; i < attachmentz.length; i++) {
				mp.addBodyPart(toMimeBodyPart(mp, config, attachmentz[i]));
			}
		}
		msg.setContent(mp);
		setHeaders(msg, headers);

		return new MimeMessageAndSession(msg, sat);
	}

	/*
	 * private static void addMailcaps(ClassLoader cl) { try { Class<?> cCM =
	 * cl.loadClass("javax.activation.CommandMap"); Method getDefaultCommandMap =
	 * cCM.getMethod("getDefaultCommandMap", CLASS_EMPTY); Object oMCM =
	 * getDefaultCommandMap.invoke(null, OBJECT_EMPTY);
	 * 
	 * Method getMimeTypes = oMCM.getClass().getMethod("getMimeTypes", CLASS_EMPTY);
	 * 
	 * Method addMailcap = oMCM.getClass().getMethod("addMailcap", CLASS_STRING); addMailcap(oMCM,
	 * addMailcap,"text/plain;;		x-java-content-handler=com.sun.mail.handlers.text_plain");
	 * addMailcap(oMCM,
	 * addMailcap,"text/html;;		x-java-content-handler=com.sun.mail.handlers.text_html");
	 * addMailcap(oMCM,
	 * addMailcap,"text/xml;;		x-java-content-handler=com.sun.mail.handlers.text_xml");
	 * addMailcap(oMCM,
	 * addMailcap,"multipart/*;;		x-java-content-handler=com.sun.mail.handlers.multipart_mixed; x-java-fallback-entry=true"
	 * ); addMailcap(oMCM,
	 * addMailcap,"message/rfc822;;	x-java-content-handler=com.sun.mail.handlers.message_rfc822"); }
	 * catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);} } private static void addMailcap(Object
	 * oMCM, Method addMailcap, String value) throws IllegalAccessException, IllegalArgumentException,
	 * InvocationTargetException { addMailcap.invoke(oMCM, new Object[]{value}); }
	 */

	private static String hash(Properties props) {
		Enumeration<?> e = props.propertyNames();
		java.util.List<String> names = new ArrayList<String>();
		String str;
		while (e.hasMoreElements()) {
			str = Caster.toString(e.nextElement(), null);
			if (!StringUtil.isEmpty(str) && str.startsWith("mail.smtp.")) names.add(str);

		}
		Collections.sort(names);
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			str = it.next();
			sb.append(str).append(':').append(props.getProperty(str)).append(';');
		}
		str = sb.toString();
		return MD5.getDigestAsString(str, str);

	}

	private static void setHeaders(SMTPMessage msg, Map<String, String> headers) throws MessagingException {
		Iterator<Entry<String, String>> it = headers.entrySet().iterator();
		Entry<String, String> e;
		while (it.hasNext()) {
			e = it.next();
			msg.setHeader(e.getKey(), e.getValue());
		}
	}

	private void checkAddress(InternetAddress[] ias, CharSet charset) { // DIFF 23
		for (int i = 0; i < ias.length; i++) {
			checkAddress(ias[i], charset);
		}
	}

	private void checkAddress(InternetAddress ia, CharSet charset) { // DIFF 23
		try {
			if (!StringUtil.isEmpty(ia.getPersonal())) {
				String personal = MailUtil.encode(ia.getPersonal(), charset.name());
				if (!personal.equals(ia.getPersonal())) ia.setPersonal(personal);
			}
		}
		catch (UnsupportedEncodingException e) {}
	}

	/**
	 * @param plainText
	 */
	public void setPlainText(String plainText) {
		this.plainText = plainText;
		this.plainTextCharset = charset;
	}

	/**
	 * @param plainText
	 * @param plainTextCharset
	 */
	public void setPlainText(String plainText, Charset plainTextCharset) {
		this.plainText = plainText;
		this.plainTextCharset = CharsetUtil.toCharSet(plainTextCharset);
	}

	/**
	 * @param htmlText
	 */
	public void setHTMLText(String htmlText) {
		this.htmlText = htmlText;
		this.htmlTextCharset = charset;
	}

	public boolean hasHTMLText() {
		return htmlText != null;
	}

	public boolean hasPlainText() {
		return plainText != null;
	}

	/**
	 * @param htmlText
	 * @param htmlTextCharset
	 */
	public void setHTMLText(String htmlText, Charset htmlTextCharset) {
		this.htmlText = htmlText;
		this.htmlTextCharset = CharsetUtil.toCharSet(htmlTextCharset);
	}

	public void addAttachment(URL url) {
		Attachment mbp = new Attachment(url);
		attachmentz = add(attachmentz, mbp);
	}

	public void addAttachment(Resource resource, String fileName, String type, String disposition, String contentID, boolean removeAfterSend) {
		Attachment att = new Attachment(resource, fileName, type, disposition, contentID, removeAfterSend);
		attachmentz = add(attachmentz, att);
	}

	public MimeBodyPart toMimeBodyPart(Multipart mp, lucee.runtime.config.Config config, Attachment att) throws MessagingException {

		MimeBodyPart mbp = new MimeBodyPart();

		// set Data Source
		String strRes = att.getAbsolutePath();
		if (!StringUtil.isEmpty(strRes)) {

			mbp.setDataHandler(new DataHandler(new ResourceDataSource(config.getResource(strRes))));
		}
		else mbp.setDataHandler(new DataHandler(new URLDataSource2(att.getURL())));
		//
		String fileName = att.getFileName();
		if (!StringUtil.isAscii(fileName)) {
			try {
				fileName = MimeUtility.encodeText(fileName, "UTF-8", null);
			}
			catch (UnsupportedEncodingException e) {} // that should never happen!
		}
		mbp.setFileName(fileName);
		if (!StringUtil.isEmpty(att.getType())) mbp.setHeader("Content-Type", att.getType());

		String disposition = att.getDisposition();
		if (!StringUtil.isEmpty(disposition)) {

			mbp.setDisposition(disposition);
			if (mp instanceof MimeMultipart && MimePart.INLINE.equalsIgnoreCase(disposition)) {
				((MimeMultipart) mp).setSubType("related");
			}
		}

		if (!StringUtil.isEmpty(att.getContentID())) mbp.setContentID("<" + att.getContentID() + ">");

		return mbp;
	}

	/**
	 * @param file
	 * @throws MessagingException
	 * @throws FileNotFoundException
	 */
	public void addAttachment(Resource file) throws MessagingException {
		addAttachment(file, null, null, null, null, false);
	}

	public void send(PageContext pc, long sendTime) throws MailException, ApplicationException {
		if (plainText == null && htmlText == null) throw new MailException("you must define plaintext or htmltext");
		Server[] servers = ((PageContextImpl) pc).getMailServers();

		ConfigWeb config = pc.getConfig();
		if (ArrayUtil.isEmpty(servers) && ArrayUtil.isEmpty(host)) throw new MailException("no SMTP Server defined");

		if (spool == SPOOL_YES || (spool == SPOOL_UNDEFINED && config.isMailSpoolEnable())) {
			MailSpoolerTask mst = new MailSpoolerTask(this, servers, sendTime);
			if (listener != null) mst.setListener(toListener(mst, listener));
			config.getSpoolerEngine().add(mst);
		}
		else _send(config, servers);
	}

	public static SpoolerTaskListener toListener(SpoolerTask st, Object listener) throws ApplicationException {
		if (listener instanceof Component) return new ComponentSpoolerTaskListener(SystemUtil.getCurrentContext(null), st, (Component) listener);

		if (listener instanceof UDF) return new UDFSpoolerTaskListener(SystemUtil.getCurrentContext(null), st, null, (UDF) listener);

		if (listener instanceof Struct) {
			UDF before = Caster.toFunction(((Struct) listener).get("before", null), null);
			UDF after = Caster.toFunction(((Struct) listener).get("after", null), null);
			return new UDFSpoolerTaskListener(SystemUtil.getCurrentContext(null), st, before, after);
		}
		throw new ApplicationException("cannot convert [" + Caster.toTypeName(listener) + "] to a listener");
	}

	public void _send(lucee.runtime.config.ConfigWeb config, Server[] servers) throws MailException {
		long start = System.nanoTime();
		long _timeout = getTimeout(config);
		try {

			Proxy.start(proxyData);
			Log log = ((ConfigImpl) config).getLog("mail");
			// Server
			// Server[] servers = config.getMailServers();
			if (host != null) {
				int prt;
				String usr, pwd;
				ServerImpl[] nServers = new ServerImpl[host.length];
				for (int i = 0; i < host.length; i++) {
					usr = null;
					pwd = null;
					prt = Server.DEFAULT_PORT;

					if (port > 0) prt = port;
					if (!StringUtil.isEmpty(username)) {
						usr = username;
						pwd = password;
					}

					nServers[i] = toServerImpl(host[i], prt, usr, pwd, lifeTimespan, idleTimespan);
					if (ssl == SSL_YES) nServers[i].setSSL(true);
					if (tls == TLS_YES) nServers[i].setTLS(true);

				}
				servers = nServers;
			}
			if (servers.length == 0) {
				// return;
				throw new MailException("no SMTP Server defined");
			}

			boolean _ssl, _tls;
			for (int i = 0; i < servers.length; i++) {

				Server server = servers[i];
				String _username = null, _password = "";
				// int _port;

				// username/password

				if (server.hasAuthentication()) {
					_username = server.getUsername();
					_password = server.getPassword();
				}

				// tls
				if (tls != TLS_NONE) _tls = tls == TLS_YES;
				else _tls = ((ServerImpl) server).isTLS();

				if (_tls) {
					MailUtil.setSystemPropMailSslProtocols();
				}

				// ssl
				if (ssl != SSL_NONE) _ssl = ssl == SSL_YES;
				else _ssl = ((ServerImpl) server).isSSL();

				MimeMessageAndSession msgSess;
				boolean recyleConnection = ((ServerImpl) server).reuseConnections();
				{// synchronized(LOCK) {
					try {
						msgSess = createMimeMessage(config, server.getHostName(), server.getPort(), _username, _password, ((ServerImpl) server).getLifeTimeSpan(),
								((ServerImpl) server).getIdleTimeSpan(), _tls, _ssl, ((ConfigImpl) config).isMailSendPartial(), !recyleConnection,
								((ConfigImpl) config).isUserset());
					}
					catch (MessagingException e) {
						// listener
						listener(config, server, log, e, System.nanoTime() - start);
						MailException me = new MailException(e.getMessage());
						me.setStackTrace(e.getStackTrace());
						throw me;
					}
					try {
						SerializableObject lock = new SerializableObject();
						SMTPSender sender = new SMTPSender(lock, msgSess, server.getHostName(), server.getPort(), _username, _password, recyleConnection);
						sender.start();
						SystemUtil.wait(lock, _timeout);

						if (!sender.isSent()) {
							Throwable t = sender.getThrowable();
							if (t != null) throw Caster.toPageException(t);

							// stop when still running
							try {
								if (sender.isAlive()) sender.stop();
							}
							catch (Throwable t2) {
								ExceptionUtil.rethrowIfNecessary(t2);
							}

							// after thread is stopped check sent flag again
							if (!sender.isSent()) {
								throw new MessagingException("timeout occurred after " + (_timeout / 1000) + " seconds while sending mail message");
							}
						}
						// could have an exception but was send anyway
						if (sender.getThrowable() != null) {
							Throwable t = sender.getThrowable();
							if (log != null) log.log(Log.LEVEL_ERROR, "send mail", t);
						}
						clean(config, attachmentz);

						listener(config, server, log, null, System.nanoTime() - start);
						break;
					}
					catch (Exception e) {
						LogUtil.log(ThreadLocalPageContext.getConfig(config), SMTPClient.class.getName(), e);
						if (i + 1 == servers.length) {

							listener(config, server, log, e, System.nanoTime() - start);
							MailException me = new MailException(server.getHostName() + " " + ExceptionUtil.getStacktrace(e, true) + ":" + i);
							me.setStackTrace(e.getStackTrace());

							throw me;
						}
					}
				}
			}
		}
		finally {
			Proxy.end();
		}
	}

	private void listener(ConfigWeb config, Server server, Log log, Exception e, long exe) {
		if (e == null) log.info("mail", "mail sent (subject:" + subject + "from:" + toString(from) + "; to:" + toString(tos) + "; cc:" + toString(ccs) + "; bcc:" + toString(bccs)
				+ "; ft:" + toString(fts) + "; rt:" + toString(rts) + ")");
		else log.log(Log.LEVEL_ERROR, "mail", e);

		// listener

		Map<String, Object> props = new HashMap<String, Object>();
		props.put("attachments", this.attachmentz);
		props.put("bccs", this.bccs);
		props.put("ccs", this.ccs);
		props.put("charset", this.charset);
		props.put("from", this.from);
		props.put("fts", this.fts);
		props.put("headers", this.headers);
		props.put("host", server.getHostName());
		props.put("htmlText", this.htmlText);
		props.put("htmlTextCharset", this.htmlTextCharset);
		props.put("parts", this.parts);
		props.put("password", this.password);
		props.put("plainText", this.plainText);
		props.put("plainTextCharset", this.plainTextCharset);
		props.put("port", server.getPort());
		props.put("proxyData", this.proxyData);
		props.put("rts", this.rts);
		props.put("subject", this.subject);
		props.put("timeout", getTimeout(config));
		props.put("timezone", this.timeZone);
		props.put("tos", this.tos);
		props.put("username", this.username);
		props.put("xmailer", this.xmailer);
		((ConfigWebImpl) config).getActionMonitorCollector().log(config, "mail", "Mail", exe, props);

	}

	private static String toString(InternetAddress... ias) {
		if (ArrayUtil.isEmpty(ias)) return "";

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ias.length; i++) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(ias[i].toString());
		}
		return sb.toString();
	}

	private long getTimeout(Config config) {
		return timeout > 0 ? timeout : config.getMailTimeout() * 1000L;
	}

	// remove all atttachements that are marked to remove
	private static void clean(Config config, Attachment[] attachmentz) {
		if (attachmentz != null) for (int i = 0; i < attachmentz.length; i++) {
			if (attachmentz[i].isRemoveAfterSend()) {
				Resource res = config.getResource(attachmentz[i].getAbsolutePath());
				ResourceUtil.removeEL(res, true);
			}
		}
	}

	private MimeBodyPart getHTMLText(Config config) throws MessagingException {
		MimeBodyPart html = new MimeBodyPart();
		fillHTMLText(config, html);
		return html;
	}

	private void fillHTMLText(Config config, MimePart mp) throws MessagingException {
		if (htmlTextCharset == null) htmlTextCharset = getMailDefaultCharset(config);
		mp.setDataHandler(new DataHandler(new StringDataSource(htmlText, TEXT_HTML, htmlTextCharset, 998)));
		mp.setHeader("Content-Transfer-Encoding", "7bit");
		mp.setHeader("Content-Type", TEXT_HTML + "; charset=" + htmlTextCharset);
	}

	private MimeBodyPart getPlainText(Config config) throws MessagingException {
		MimeBodyPart plain = new MimeBodyPart();
		fillPlainText(config, plain);
		return plain;
	}

	private void fillPlainText(Config config, MimePart mp) throws MessagingException {
		if (plainTextCharset == null) plainTextCharset = getMailDefaultCharset(config);
		mp.setDataHandler(new DataHandler(new StringDataSource(plainText != null ? plainText : "", TEXT_PLAIN, plainTextCharset, 998)));
		mp.setHeader("Content-Transfer-Encoding", "7bit");
		mp.setHeader("Content-Type", TEXT_PLAIN + "; charset=" + plainTextCharset);
	}

	private BodyPart toMimeBodyPart(Config config, MailPart part) throws MessagingException {
		CharSet cs = CharsetUtil.toCharSet(part.getCharset());
		if (cs == null) cs = getMailDefaultCharset(config);
		MimeBodyPart mbp = new MimeBodyPart();
		mbp.setDataHandler(new DataHandler(new StringDataSource(part.getBody(), part.getType(), cs, 998)));
		// mbp.setHeader("Content-Transfer-Encoding", "7bit");
		// mbp.setHeader("Content-Type", TEXT_PLAIN+"; charset="+plainTextCharset);
		return mbp;
	}

	private CharSet getMailDefaultCharset(Config config) {
		Charset cs = ThreadLocalPageContext.getConfig(config).getMailDefaultCharset();
		if (cs == null) cs = CharsetUtil.UTF8;
		return CharsetUtil.toCharSet(cs);
	}

	/**
	 * @return the proxyData
	 */
	public ProxyData getProxyData() {
		return proxyData;
	}

	/**
	 * @param proxyData the proxyData to set
	 */
	public void setProxyData(ProxyData proxyData) {
		this.proxyData = proxyData;
	}

	/**
	 * @param ssl the ssl to set
	 */
	public void setSSL(boolean ssl) {
		this.ssl = ssl ? SSL_YES : SSL_NO;
	}

	/**
	 * @param tls the tls to set
	 */
	public void setTLS(boolean tls) {
		this.tls = tls ? TLS_YES : TLS_NO;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @return the from
	 */
	public InternetAddress getFrom() {
		return from;
	}

	/**
	 * @return the tos
	 */
	public InternetAddress[] getTos() {
		return tos;
	}

	/**
	 * @return the bccs
	 */
	public InternetAddress[] getBccs() {
		return bccs;
	}

	/**
	 * @return the ccs
	 */
	public InternetAddress[] getCcs() {
		return ccs;
	}

	public void setPart(MailPart part) {
		if (parts == null) parts = new ArrayList<MailPart>();
		parts.add(part);
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
}

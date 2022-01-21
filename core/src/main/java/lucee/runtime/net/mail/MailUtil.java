/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.net.mail;

import java.io.UnsupportedEncodingException;
import java.net.IDN;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.http.sni.SSLConnectionSocketFactoryImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ListUtil;

public final class MailUtil {

	public static final String SYSTEM_PROP_MAIL_SSL_PROTOCOLS = "mail.smtp.ssl.protocols";

	public static String encode(String text, String encoding) throws UnsupportedEncodingException {
		// print.ln(StringUtil.changeCharset(text,encoding));
		return MimeUtility.encodeText(text, encoding, "Q");
	}

	public static String decode(String text) throws UnsupportedEncodingException {
		return MimeUtility.decodeText(text);
	}

	public static InternetAddress toInternetAddress(Object emails) throws MailException, UnsupportedEncodingException, PageException {
		if (emails instanceof String) {
			return parseEmail(emails, null);
		}
		InternetAddress[] addresses = toInternetAddresses(emails);
		if (addresses != null && addresses.length > 0) return addresses[0];
		throw new MailException("invalid email address definition");// should never come to this!
	}

	public static InternetAddress[] toInternetAddresses(Object emails) throws MailException, UnsupportedEncodingException, PageException {

		if (emails instanceof InternetAddress[]) return (InternetAddress[]) emails;

		else if (emails instanceof String) return fromList((String) emails);

		else if (Decision.isArray(emails)) return fromArray(Caster.toArray(emails));

		else if (Decision.isStruct(emails)) return new InternetAddress[] { fromStruct(Caster.toStruct(emails)) };

		else throw new MailException("e-mail definitions must be one of the following types [string,array,struct], not [" + emails.getClass().getName() + "]");
	}

	private static InternetAddress[] fromArray(Array array) throws MailException, PageException, UnsupportedEncodingException {

		Iterator it = array.valueIterator();
		Object el;
		ArrayList<InternetAddress> pairs = new ArrayList();

		while (it.hasNext()) {
			el = it.next();
			if (Decision.isStruct(el)) {

				pairs.add(fromStruct(Caster.toStruct(el)));
			}
			else {

				InternetAddress addr = parseEmail(Caster.toString(el), null);
				if (addr != null) pairs.add(addr);
			}
		}

		return pairs.toArray(new InternetAddress[pairs.size()]);
	}

	private static InternetAddress fromStruct(Struct sct) throws MailException, UnsupportedEncodingException {

		String name = Caster.toString(sct.get("label", null), null);
		if (name == null) name = Caster.toString(sct.get("name", null), null);

		String email = Caster.toString(sct.get("email", null), null);
		if (email == null) email = Caster.toString(sct.get("e-mail", null), null);
		if (email == null) email = Caster.toString(sct.get("mail", null), null);

		if (StringUtil.isEmpty(email)) throw new MailException("missing e-mail definition in struct");

		if (name == null) name = "";

		return new InternetAddress(email, name);
	}

	private static InternetAddress[] fromList(String strEmails) throws MailException {

		if (StringUtil.isEmpty(strEmails, true)) return new InternetAddress[0];

		Array raw = ListUtil.listWithQuotesToArray(strEmails, ",;", "\"");

		Iterator<Object> it = raw.valueIterator();
		ArrayList<InternetAddress> al = new ArrayList();

		while (it.hasNext()) {

			InternetAddress addr = parseEmail(it.next());

			if (addr != null) al.add(addr);
		}

		return al.toArray(new InternetAddress[al.size()]);
	}

	/**
	 * returns true if the passed value is a in valid email address format
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isValidEmail(Object value) {
		try {
			InternetAddress addr = parseEmail(value, null);
			if (addr != null) {

				String address = addr.getAddress();

				if (address.contains("..")) return false;

				int pos = address.indexOf('@');

				if (pos < 1 || pos == address.length() - 1) return false;

				String local = address.substring(0, pos);
				String domain = address.substring(pos + 1);

				if (local.length() > 64) return false; // local part may only be 64 characters
				if (domain.length() > 255) return false; // domain may only be 255 characters

				if (domain.charAt(0) == '.' || local.charAt(0) == '.' || local.charAt(local.length() - 1) == '.') return false;

				pos = domain.lastIndexOf('.');

				if (pos > 0 && pos < domain.length() - 2) { // test TLD to be at
					// least 2 chars all
					// alpha characters
					if (StringUtil.isAllAlpha(domain.substring(pos + 1))) return true;
					try {
						addr.validate();
						return true;
					}
					catch (AddressException e) {
					}
				}
			}
		}
		catch (Exception e) {
		}
		return false;
	}

	public static InternetAddress parseEmail(Object value) throws MailException {
		InternetAddress ia = parseEmail(value, null);
		if (ia != null) return ia;
		if (value instanceof CharSequence) {
			if (StringUtil.isEmpty(value.toString())) return null;
			throw new MailException("[" + value + "] cannot be converted to an email address");
		}
		throw new MailException("input cannot be converted to an email address");
	}

	/**
	 * returns an InternetAddress object or null if the parsing fails. to be be used in multiple places.
	 * 
	 * @param value
	 * @return
	 */
	public static InternetAddress parseEmail(Object value, InternetAddress defaultValue) {
		String str = Caster.toString(value, "");
		if (StringUtil.isEmpty(str)) return defaultValue;
		if (str.indexOf('@') > -1) {
			try {
				str = fixIDN(str);
				InternetAddress addr = new InternetAddress(str);
				// fixIDN( addr );
				return addr;
			}
			catch (AddressException ex) {
			}
		}
		return defaultValue;
	}

	/**
	 * converts IDN to ASCII if needed
	 * 
	 * @param addr
	 * @return
	 */
	public static String fixIDN(String addr) {
		int pos = addr.indexOf('@');
		if (pos > 0 && pos < addr.length() - 1) {
			String domain = addr.substring(pos + 1);
			if (!StringUtil.isAscii(domain)) {
				domain = IDN.toASCII(domain);
				return addr.substring(0, pos) + "@" + domain;
			}
		}
		return addr;
	}

	/**
	 * This method should be called when TLS is used to ensure that the supported protocols are set.
	 * Some servers, e.g. Outlook365, reject lists with older protocols so we only pass protocols that
	 * start with the prefix "TLS"
	 */
	public static void setSystemPropMailSslProtocols() {
		String protocols = SystemUtil.getSystemPropOrEnvVar(SYSTEM_PROP_MAIL_SSL_PROTOCOLS, "");
		if (protocols.isEmpty()) {
			List<String> supportedProtocols = SSLConnectionSocketFactoryImpl.getSupportedSslProtocols();
			protocols = supportedProtocols.stream().filter(el -> el.startsWith("TLS")).collect(Collectors.joining(" "));
			if (!protocols.isEmpty()) {
				System.setProperty(SYSTEM_PROP_MAIL_SSL_PROTOCOLS, protocols);
				Config config = ThreadLocalPageContext.getConfig();
				if (config != null) config.getLog("mail").info("mail", "Lucee system property " + SYSTEM_PROP_MAIL_SSL_PROTOCOLS + " set to [" + protocols + "]");
			}
		}
	}

}

/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.config;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.osgi.framework.BundleException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import lucee.commons.digest.Hash;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.crypt.BlowfishEasy;
import lucee.runtime.exp.PageException;

public class PasswordImpl implements Password {

	private final String rawPassword;
	private final String password;
	private final String salt;
	private final int type;
	private final int origin;

	private PasswordImpl(int origin, String password, String salt, int type) {
		this.rawPassword = null;
		this.password = password;
		this.salt = salt;
		this.type = type;
		this.origin = origin;
	}

	PasswordImpl(int origin, String rawPassword, String salt) {
		this.rawPassword = rawPassword;
		this.password = hash(rawPassword, salt);
		this.salt = salt;
		this.type = StringUtil.isEmpty(salt) ? HASHED : HASHED_SALTED;
		this.origin = origin;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getSalt() {
		return salt;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public int getOrigin() {
		return origin;
	}

	@Override
	public Password isEqual(Config config, String other) {

		// an already hashed password that matches
		if (password.equals(other)) return this;

		// current password is only hashed
		if (type == HASHED) return this.password.equals(hash(other, null)) ? this : null;
		// current password is hashed and salted
		return this.password.equals(hash(other, salt)) ? this : null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (obj instanceof Password) {
			Password opw = (Password) obj;
			if (password.equals(opw.getPassword())) return true;
			if (obj instanceof PasswordImpl) {
				PasswordImpl pi = (PasswordImpl) obj;
				if (pi.rawPassword != null) {
					if (type == HASHED) return hash(pi.rawPassword, null).equals(password);
					return hash(pi.rawPassword, salt).equals(password);
				}
			}

		}

		if (obj instanceof CharSequence) {
			String str = obj.toString();
			if (password.equals(str)) return true;

			if (type == HASHED) return hash(str, null).equals(password);
			return hash(str, salt).equals(password);
		}

		return false;
	}

	private static String hash(String str, String salt) {
		try {
			return Hash.hash(StringUtil.isEmpty(salt, true) ? str : str + ":" + salt, Hash.ALGORITHM_SHA_256, 5, Hash.ENCODING_HEX);
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * reads the password defined in the Lucee configuration, this can also in older formats (only
	 * hashed or encrypted)
	 * 
	 * @param el
	 * @param salt
	 * @param isDefault
	 * @return
	 */
	public static Password readFromXML(Element el, String salt, boolean isDefault) {
		String prefix = isDefault ? "default-" : "";

		// first we look for the hashed and salted password
		String pw = el.getAttribute(prefix + "hspw");
		if (!StringUtil.isEmpty(pw, true)) {
			// password is only of use when there is a salt as well
			if (salt == null) return null;
			return new PasswordImpl(ORIGIN_HASHED_SALTED, pw, salt, HASHED_SALTED);
		}

		// fall back to password that is hashed but not salted
		pw = el.getAttribute(prefix + "pw");
		if (!StringUtil.isEmpty(pw, true)) {
			return new PasswordImpl(ORIGIN_HASHED, pw, null, HASHED);
		}

		// fall back to encrypted password
		String pwEnc = el.getAttribute(prefix + "password");
		if (!StringUtil.isEmpty(pwEnc, true)) {
			String rawPassword = new BlowfishEasy("tpwisgh").decryptString(pwEnc);
			return new PasswordImpl(ORIGIN_ENCRYPTED, rawPassword, salt);
		}
		return null;
	}

	public static boolean hasPassword(Element el) {
		if (el == null) return false;

		// first we look for the hashed and salted password
		if (!StringUtil.isEmpty(el.getAttribute("hspw"), true)) return el.getAttribute("salt") != null;

		// fall back to password that is hashed but not salted
		if (!StringUtil.isEmpty(el.getAttribute("pw"), true)) return true;

		// fall back to encrypted password
		String pwEnc = el.getAttribute("password");
		if (!StringUtil.isEmpty(pwEnc, true)) return true;

		return false;
	}

	public static Password writeToXML(Element el, String passwordRaw, boolean isDefault) {
		// salt
		String salt = getSalt(el);

		Password pw = new PasswordImpl(ORIGIN_UNKNOW, passwordRaw, salt);
		writeToXML(el, pw, isDefault);
		return pw;
	}

	private static String getSalt(Element el) {
		String salt = el.getAttribute("salt");
		if (StringUtil.isEmpty(salt, true)) throw new RuntimeException("missing salt!");// this should never happen
		return salt.trim();

	}

	public static void writeToXML(Element el, Password pw, boolean isDefault) {
		String prefix = isDefault ? "default-" : "";
		if (pw == null) {
			if (el.hasAttribute(prefix + "hspw")) el.removeAttribute(prefix + "hspw");
			if (el.hasAttribute(prefix + "pw")) el.removeAttribute(prefix + "pw");
			if (el.hasAttribute(prefix + "password")) el.removeAttribute(prefix + "password");
		}
		else {
			// remove backward compatibility
			if (el.hasAttribute(prefix + "pw")) el.removeAttribute(prefix + "pw");
			if (el.hasAttribute(prefix + "password")) el.removeAttribute(prefix + "password");

			if (pw.getType() == HASHED_SALTED) el.setAttribute(prefix + "hspw", pw.getPassword());
			// password is not hashed and salted
			else {
				PasswordImpl pwi;
				if (pw instanceof PasswordImpl && (pwi = ((PasswordImpl) pw)).rawPassword != null) {
					el.setAttribute(prefix + "hspw", hash(pwi.rawPassword, getSalt(el)));
				}
				else {
					el.setAttribute(prefix + "pw", pw.getPassword());// this should never happen
				}
			}
		}
	}

	public static void removeFromXML(Element root, boolean isDefault) {
		writeToXML(root, (Password) null, isDefault);
	}

	public static Password updatePasswordIfNecessary(ConfigImpl config, Password passwordOld, String strPasswordNew) {

		try {
			// is the server context default password used
			boolean defPass = false;
			if (config instanceof ConfigWebImpl) defPass = ((ConfigWebImpl) config).isDefaultPassword();

			int origin = config.getPasswordOrigin();

			// current is old style password and not a default password!
			if ((origin == Password.ORIGIN_HASHED || origin == Password.ORIGIN_ENCRYPTED) && !defPass) {
				// is passord valid!
				if (config.isPasswordEqual(strPasswordNew) != null) {
					// new salt
					String saltn = config.getSalt(); // get salt from context, not from old password that can be different when default password

					// new password
					Password passwordNew = null;
					if (!StringUtil.isEmpty(strPasswordNew, true)) passwordNew = new PasswordImpl(ORIGIN_UNKNOW, strPasswordNew, saltn);

					updatePassword(config, passwordOld, passwordNew);
					return passwordNew;
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		return null;
	}

	/**
	 * 
	 * @param config Config of the context (ConfigServer to set a server level password)
	 * @param strPasswordOld the old password to replace or null if there is no password set yet
	 * @param strPasswordNew the new password
	 * @throws IOException
	 * @throws SAXException
	 * @throws PageException
	 * @throws BundleException
	 */
	public static void updatePassword(ConfigImpl config, String strPasswordOld, String strPasswordNew) throws SAXException, IOException, PageException, BundleException {

		// old salt
		int pwType = config.getPasswordType(); // get type from password
		String salto = config.getPasswordSalt(); // get salt from password
		if (pwType == Password.HASHED) salto = null; // if old password does not use a salt, we do not use a salt to hash

		// new salt
		String saltn = config.getSalt(); // get salt from context, not from old password that can be different when default password

		// old password
		Password passwordOld = null;
		if (!StringUtil.isEmpty(strPasswordOld, true)) passwordOld = new PasswordImpl(ORIGIN_UNKNOW, strPasswordOld, salto);

		// new password
		Password passwordNew = null;
		if (!StringUtil.isEmpty(strPasswordNew, true)) passwordNew = new PasswordImpl(ORIGIN_UNKNOW, strPasswordNew, saltn);

		updatePassword(config, passwordOld, passwordNew);

	}

	public static void updatePassword(ConfigImpl config, Password passwordOld, Password passwordNew) throws SAXException, IOException, PageException, BundleException {
		if (!config.hasPassword()) {
			config.setPassword(passwordNew);
			XMLConfigAdmin admin = XMLConfigAdmin.newInstance(config, passwordNew);
			admin.setPassword(passwordNew);
			admin.storeAndReload();
		}
		else {
			ConfigWebUtil.checkPassword(config, "write", passwordOld);
			ConfigWebUtil.checkGeneralWriteAccess(config, passwordOld);
			XMLConfigAdmin admin = XMLConfigAdmin.newInstance(config, passwordOld);
			admin.setPassword(passwordNew);
			admin.storeAndReload();
		}
	}

	public static Password passwordToCompare(ConfigWeb cw, boolean server, String rawPassword) {
		if (StringUtil.isEmpty(rawPassword, true)) return null;
		ConfigWebImpl cwi = (ConfigWebImpl) cw;
		int pwType;
		String pwSalt;
		if (server) {
			pwType = cwi.getServerPasswordType();
			pwSalt = cwi.getServerPasswordSalt();
		}
		else {
			pwType = cwi.getPasswordType();
			pwSalt = cwi.getPasswordSalt();
		}

		// if the internal password is not using the salt yet, this hash should eigther
		String salt = pwType == Password.HASHED ? null : pwSalt;

		return new PasswordImpl(ORIGIN_UNKNOW, rawPassword, salt);
	}
}
/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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

import lucee.commons.digest.Hash;
import lucee.commons.io.SystemUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.crypt.BlowfishEasy;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public final class PasswordImpl implements Password {

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

	public static Password read(Config config, String name, String value, String salt) {

		// "adminPasswordDefault"

		String prefix = "admin";
		String prefixOlder = "";
		String env = "lucee.admin.password";

		// first we look for the hashed and salted password
		// preferred adminDefaultHSPW adminHSPW
		if ((prefix + "hspw").equalsIgnoreCase(name) || (prefixOlder + "hspw").equalsIgnoreCase(name)) {
			return new PasswordImpl(ORIGIN_HASHED_SALTED, value, salt, HASHED_SALTED);
		}

		// fall back to password that is hashed but not salted
		// preferred adminDefaultPW adminPW
		if ((prefix + "pw").equalsIgnoreCase(name) || (prefixOlder + "pw").equalsIgnoreCase(name)) {
			return new PasswordImpl(ORIGIN_HASHED, value, null, HASHED);
		}

		// fall back to encrypted password
		// preferred adminDefaultPassword adminPassword
		if ((prefix + "Password").equalsIgnoreCase(name) || (prefixOlder + "Password").equalsIgnoreCase(name)) {
			String rawPassword = new BlowfishEasy("tpwisgh").decryptString(value);
			return new PasswordImpl(ORIGIN_ENCRYPTED, rawPassword, salt);
		}

		if (env.equalsIgnoreCase(name)) {
			return new PasswordImpl(ORIGIN_ENCRYPTED, value, salt);
		}

		return null;
	}

	public static Password readFromStruct(Config config, Struct data, String salt, boolean isDefault, boolean getPasswordFromEnv) {
		String prefix = isDefault ? "adminDefault" : "admin";
		String prefixOlder = isDefault ? "default" : "";

		// first we look for the hashed and salted password
		// preferred adminDefaultHSPW adminHSPW
		String pw = ConfigFactoryImpl.getAttr(config, data, prefix + "hspw");
		if (StringUtil.isEmpty(pw, true)) pw = ConfigFactoryImpl.getAttr(config, data, prefixOlder + "hspw");
		if (!StringUtil.isEmpty(pw, true)) {
			// password is only of use when there is a salt as well
			if (salt == null) return null;
			return new PasswordImpl(ORIGIN_HASHED_SALTED, pw, salt, HASHED_SALTED);
		}

		// fall back to password that is hashed but not salted
		// preferred adminDefaultPW adminPW
		pw = ConfigFactoryImpl.getAttr(config, data, prefix + "pw");
		if (StringUtil.isEmpty(pw, true)) pw = ConfigFactoryImpl.getAttr(config, data, prefixOlder + "pw");
		if (!StringUtil.isEmpty(pw, true)) {
			return new PasswordImpl(ORIGIN_HASHED, pw, null, HASHED);
		}

		// fall back to encrypted password
		// preferred adminDefaultPassword adminPassword
		String pwEnc = ConfigFactoryImpl.getAttr(config, data, prefix + "Password");
		if (StringUtil.isEmpty(pwEnc, true)) pwEnc = ConfigFactoryImpl.getAttr(config, data, prefixOlder + "Password");
		if (isDefault && StringUtil.isEmpty(pwEnc, true)) pwEnc = ConfigFactoryImpl.getAttr(config, data, "adminPasswordDefault");
		if (!StringUtil.isEmpty(pwEnc, true)) {
			String rawPassword = new BlowfishEasy("tpwisgh").decryptString(pwEnc);
			return new PasswordImpl(ORIGIN_ENCRYPTED, rawPassword, salt);
		}

		if (getPasswordFromEnv) {
			pw = SystemUtil.getSystemPropOrEnvVar(isDefault ? "lucee.admin.default.password" : "lucee.admin.password", null);
			if (!StringUtil.isEmpty(pw, true)) {
				return new PasswordImpl(ORIGIN_ENCRYPTED, pw, salt);
			}
		}

		return null;
	}

	public static Password writeToStruct(Struct el, String passwordRaw) {
		// salt
		String salt = getSalt(el);

		Password pw = new PasswordImpl(ORIGIN_UNKNOW, passwordRaw, salt);
		writeToStruct(el, pw);
		return pw;
	}

	public static Password writeToStruct(Struct root, String salt, String passwordRaw) {
		Password pw = new PasswordImpl(ORIGIN_UNKNOW, passwordRaw, salt);
		writeToStruct(root, pw);
		return pw;
	}

	private static String getSalt(Struct data) {
		String salt = Caster.toString(data.get("salt", null), null);
		if (StringUtil.isEmpty(salt, true)) salt = Caster.toString(data.get("adminsalt", null), null);
		if (StringUtil.isEmpty(salt, true)) throw new RuntimeException("missing salt!");// this should never happen
		return salt.trim();
	}

	public static void writeToStruct(Struct data, Password pw) {
		if (pw == null) {
			if (data.containsKey("hspw")) data.remove("hspw");
			if (data.containsKey("pw")) data.remove("pw");
			if (data.containsKey("password")) data.remove("password");
		}
		else {
			// remove backward compatibility
			if (data.containsKey("pw")) data.remove("pw");
			if (data.containsKey("password")) data.remove("password");

			if (pw.getType() == HASHED_SALTED) data.setEL("hspw", pw.getPassword());
			// password is not hashed and salted
			else {
				PasswordImpl pwi;
				if (pw instanceof PasswordImpl && (pwi = ((PasswordImpl) pw)).rawPassword != null) {
					data.setEL("hspw", hash(pwi.rawPassword, getSalt(data)));
				}
				else {
					data.setEL("pw", pw.getPassword());// this should never happen
				}
			}
		}
	}

	public static void removeFromStruct(Struct root) {
		writeToStruct(root, (Password) null);
	}

	public static Password updatePasswordIfNecessary(ConfigPro config, Password passwordOld, String strPasswordNew) {

		try {
			int origin = config.getPasswordOrigin();

			// current is old style password and not a default password!
			if ((origin == Password.ORIGIN_HASHED || origin == Password.ORIGIN_ENCRYPTED)) {
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
	 * @throws PageException
	 * @throws BundleException
	 * @throws ConverterException
	 */
	public static void updatePassword(ConfigPro config, String strPasswordOld, String strPasswordNew) throws IOException, PageException, BundleException, ConverterException {

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

	public static void updatePassword(ConfigPro config, Password passwordOld, Password passwordNew) throws IOException, PageException, ConverterException {
		if (!config.hasPassword()) {
			config.setPassword(passwordNew);

			ConfigAdmin admin = ConfigAdmin.newInstance(config, passwordNew);
			admin.setPassword(passwordNew);

			admin.store(true);
			ConfigUtil.getConfigServerImpl(config).resetPassword().resetSalt();
			// validate
			ConfigUtil.checkPassword(config, "write", passwordNew);

		}
		else {
			ConfigUtil.checkPassword(config, "write", passwordOld);
			ConfigUtil.checkGeneralWriteAccess(config, passwordOld);
			ConfigAdmin admin = ConfigAdmin.newInstance(config, passwordOld);
			admin.setPassword(passwordNew);
			admin.store(true);
			ConfigUtil.getConfigServerImpl(config).resetPassword().resetSalt();
		}
	}

	public static Password passwordToCompare(ConfigWeb cw, boolean server, String rawPassword) {
		if (StringUtil.isEmpty(rawPassword, true)) return null;
		ConfigWebPro cwi = (ConfigWebPro) cw;
		int pwType;
		String pwSalt;
		if (server) {
			pwType = ConfigUtil.getConfigServerImpl(cwi).getPasswordType();
			pwSalt = ConfigUtil.getConfigServerImpl(cwi).getPasswordSalt();
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
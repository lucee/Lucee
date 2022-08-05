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
package lucee.runtime.security;

import java.io.IOException;
import java.util.Set;

import lucee.commons.digest.MD5;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.coder.Base64Coder;
import lucee.runtime.crypt.Cryptor;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ListUtil;

/**
 * User Password Information
 */
public final class CredentialImpl implements Credential {
	String username;
	String password;
	String[] roles;
	private Resource rolesDir;
	private String privateKey;
	private byte[] salt;
	private int iter;
	private static final byte[] staticSalt;
	private static final int staticIter;
	private static final String staticPrivateKey;
	private static final char ONE = (char) 1;
	private static final String ALGO = "Blowfish/CBC/PKCS5Padding";

	static {
		// salt
		String tmp = SystemUtil.getSystemPropOrEnvVar("lucee.loginstorage.salt", null);
		if (StringUtil.isEmpty(tmp, true)) tmp = "nkhuvghc";
		else tmp = tmp.trim();
		staticSalt = toSalt(tmp);

		// salt iteration
		int itmp = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.loginstorage.iterations", null), 0);
		if (itmp < 1) itmp = 10;
		staticIter = itmp;

		// private key
		tmp = SystemUtil.getSystemPropOrEnvVar("lucee.loginstorage.privatekey", null);
		if (!StringUtil.isEmpty(tmp, true)) {
			staticPrivateKey = tmp.trim();
		}
		else staticPrivateKey = null;
	}

	/**
	 * credential constructor
	 * 
	 * @param username
	 */
	public CredentialImpl(String username, Resource rolesDir) {
		this(username, null, new String[0], rolesDir, null, null, 0);
	}

	private static byte[] toSalt(String salt) {
		byte[] barr = salt.trim().getBytes(CharsetUtil.UTF8);
		if (barr.length == 8) return barr;
		// we only take the first 8 bytes
		if (barr.length > 8) {
			byte[] tmp = new byte[8];
			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = barr[i];
			}
			return tmp;
		}
		// we repeat the bytes until we reach 8
		byte[] tmp = new byte[8];
		int index = 0;
		outer: while (true) {
			for (int i = 0; i < barr.length; i++) {
				if (index >= 8) break outer;
				tmp[index++] = barr[i];
			}
		}
		return tmp;
	}

	/**
	 * credential constructor
	 * 
	 * @param username
	 * @param password
	 */
	public CredentialImpl(String username, String password, Resource rolesDir) {
		this(username, password, new String[0], rolesDir, null, null, 0);
	}

	/**
	 * credential constructor
	 * 
	 * @param username
	 * @param password
	 * @param roles
	 * @throws PageException
	 */
	public CredentialImpl(String username, String password, String roles, Resource rolesDir) throws PageException {
		this(username, password, toRole(roles), rolesDir, null, null, 0);
	}

	/**
	 * credential constructor
	 * 
	 * @param username
	 * @param password
	 * @param roles
	 * @throws PageException
	 */
	public CredentialImpl(String username, String password, Array roles, Resource rolesDir) throws PageException {
		this(username, password, toRole(roles), rolesDir, null, null, 0);
	}

	/**
	 * credential constructor
	 * 
	 * @param username
	 * @param password
	 * @param roles
	 */

	public CredentialImpl(String username, String password, String[] roles, Resource rolesDir) {
		this(username, password, roles, rolesDir, null, null, 0);
	}

	public CredentialImpl(String username, String password, String[] roles, Resource rolesDir, String privateKey, String salt, int iter) {
		this.username = username;
		this.password = password;
		this.roles = roles;
		this.rolesDir = rolesDir;
		this.privateKey = StringUtil.isEmpty(privateKey, true) ? staticPrivateKey : privateKey.trim();
		this.salt = StringUtil.isEmpty(salt, true) ? staticSalt : toSalt(salt);
		this.iter = iter < 1 ? staticIter : iter;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String[] getRoles() {
		return roles;
	}

	@Override
	public String getUsername() {
		return username;
	}

	/**
	 * convert an Object to a String Array of Roles
	 * 
	 * @param oRoles
	 * @return roles
	 * @throws PageException
	 */
	public static String[] toRole(Object oRoles) throws PageException {
		if (oRoles instanceof String) {
			oRoles = ListUtil.listToArrayRemoveEmpty(oRoles.toString(), ",");
		}

		if (oRoles instanceof Array) {
			Array arrRoles = (Array) oRoles;
			String[] roles = new String[arrRoles.size()];
			for (int i = 0; i < roles.length; i++) {
				roles[i] = Caster.toString(arrRoles.get(i + 1, ""));
			}
			return roles;
		}
		throw new ApplicationException("invalid roles definition for tag loginuser");
	}

	@Override
	public String serialize() {
		return serialize(null);
	}

	@Override
	public String serialize(Set<Object> done) {
		return "createObject('java','lucee.runtime.security.Credential').init('" + username + "','" + password + "','" + ListUtil.arrayToList(roles, ",") + "')";
	}

	@Override
	public String encode() throws PageException {
		String raw = ListUtil.arrayToList(roles, ",");
		if (raw.length() > 100) {
			try {
				if (!rolesDir.exists()) rolesDir.mkdirs();
				String md5 = MD5.getDigestAsString(raw);
				IOUtil.write(rolesDir.getRealResource(md5), raw, CharsetUtil.UTF8, false);
				return encrypt(username + ONE + password + ONE + "md5:" + md5, privateKey, salt, iter, true);
			}
			catch (IOException e) {
			}
		}
		try {
			return encrypt(username + ONE + password + ONE + raw, privateKey, salt, iter, true);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static String encrypt(String input, String privateKey, byte[] salt, int iter, boolean precise) throws PageException {
		if (StringUtil.isEmpty(privateKey, true)) return Caster.toB64(input.getBytes(CharsetUtil.UTF8));
		try {
			return Cryptor.encrypt(input, privateKey, ALGO, salt, iter, "Base64", Cryptor.DEFAULT_CHARSET, precise);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static String decrypt(Object input, String privateKey, byte[] salt, int iter, boolean precise) throws PageException {
		if (StringUtil.isEmpty(privateKey, true)) {
			try {
				return Base64Coder.decodeToString(Caster.toString(input), "UTF-8", true);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		try {
			return Cryptor.decrypt(Caster.toString(input), privateKey, ALGO, salt, iter, "Base64", Cryptor.DEFAULT_CHARSET, precise);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public static Credential decode(Object encoded, Resource rolesDir) {
		try {
			return decode(encoded, rolesDir, null, null, 0, true);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * decode the Credential form a Base64 String value
	 * 
	 * @param encoded
	 * @return Credential from decoded string
	 * @throws PageException
	 */
	public static Credential decode(Object encoded, Resource rolesDir, String privateKey, String salt, int iter, boolean precise) throws PageException {
		String _privateKey = StringUtil.isEmpty(privateKey, true) ? staticPrivateKey : privateKey.trim();
		byte[] _salt = StringUtil.isEmpty(salt, true) ? staticSalt : toSalt(salt);
		int _iter = iter < 1 ? staticIter : iter;
		String dec = decrypt(encoded, _privateKey, _salt, _iter, precise);

		Array arr = ListUtil.listToArray(dec, "" + ONE);
		int len = arr.size();
		if (len == 3) {
			String str = Caster.toString(arr.get(3, ""));
			if (str.startsWith("md5:")) {
				if (!rolesDir.exists()) rolesDir.mkdirs();
				str = str.substring(4);
				Resource md5 = rolesDir.getRealResource(str);
				try {
					str = IOUtil.toString(md5, CharsetUtil.UTF8);
				}
				catch (IOException e) {
					str = "";
				}
			}

			return new CredentialImpl(Caster.toString(arr.get(1, "")), Caster.toString(arr.get(2, "")), str, rolesDir);
		}
		if (len == 2) return new CredentialImpl(Caster.toString(arr.get(1, "")), Caster.toString(arr.get(2, "")), rolesDir);
		if (len == 1) return new CredentialImpl(Caster.toString(arr.get(1, "")), rolesDir);

		return null;
	}

	@Override
	public String toString() {
		return "username:" + username + ";password:" + password + ";roles:" + roles;
	}

	/*
	 * public static void main(String[] args) throws PageException { int i = 20; Resource rolesDir =
	 * ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Temp/"); String key =
	 * "vhvzglmjknkvug"; String salt = "dbjvzvhvnbubvuh"; CredentialImpl c = new CredentialImpl("susi",
	 * "sorglos", new String[] { "qqq" }, rolesDir, key, salt, i); String enc = c.encode(); Credential
	 * res = CredentialImpl.decode(enc, rolesDir, key, "df", i); print.e(enc); print.e(res.toString());
	 * }
	 */

}
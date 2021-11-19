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
package lucee.commons.io.res.type.smb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base32;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.Resources;
import lucee.commons.io.res.util.ResourceLockImpl;
import lucee.commons.lang.StringUtil;

public class SMBResourceProvider implements ResourceProvider {

	private String scheme = "smb";
	private Map<String, String> args;
	private final static String ENCRYPTED_PREFIX = "$smb-enc$";
	private final static Charset UTF8 = CharsetUtil.UTF8;
	private final ResourceLockImpl lock = new ResourceLockImpl(10000, false);
	private final static Base32 Base32DecEnc = new Base32();

	@Override
	public ResourceProvider init(String scheme, Map arguments) {
		_setProperties(arguments);

		if (!StringUtil.isEmpty(scheme)) this.scheme = scheme;
		this.args = arguments;
		return this;
	}

	private void _setProperties(Map arguments) {

		String resolveOrder = (String) arguments.get("resolveOrder");
		if (resolveOrder == null) resolveOrder = "DNS";

		String dfsDisabled = (String) arguments.get("smb.client.dfs.disabled");
		if (dfsDisabled == null) dfsDisabled = "true";
		System.setProperty("jcifs.resolveOrder", resolveOrder);
		System.setProperty("jcifs.smb.client.dfs.disabled", dfsDisabled);

	}

	public Resource getResource(String path, NtlmPasswordAuthentication auth) {
		return new SMBResource(this, path, auth);
	}

	@Override
	public Resource getResource(String path) {
		return new SMBResource(this, path);
	}

	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public Map<String, String> getArguments() {
		return args;
	}

	@Override
	public void setResources(Resources resources) {
		// TODO Not sure what this does
	}

	@Override
	public void unlock(Resource res) {
		lock.unlock(res);
	}

	@Override
	public void lock(Resource res) throws IOException {
		lock.lock(res);
	}

	@Override
	public void read(Resource res) throws IOException {
		lock.read(res);
	}

	@Override
	public boolean isCaseSensitive() {
		return false;
	}

	@Override
	public boolean isModeSupported() {
		return false;
	}

	@Override
	public boolean isAttributesSupported() {
		return false;
	}

	public SmbFile getFile(String path, NtlmPasswordAuthentication auth) {
		try {
			return new SmbFile(path, auth);
		}
		catch (MalformedURLException e) {
			return null; // null means it is a bad SMBFile
		}
	}

	public static boolean isEncryptedUserInfo(String userInfo) {
		return userInfo.startsWith(ENCRYPTED_PREFIX);
	}

	public static String unencryptUserInfo(String userInfo) {
		if (!isEncryptedUserInfo(userInfo)) return userInfo;
		String encrypted = userInfo.replaceAll(Pattern.quote(ENCRYPTED_PREFIX), "");
		byte[] unencryptedBytes = Base32DecEnc.decode(encrypted.toUpperCase());
		return new String(unencryptedBytes, UTF8);

	}

	public static String encryptUserInfo(String userInfo) {
		byte[] bytes = Base32DecEnc.encode(userInfo.getBytes(UTF8));
		return ENCRYPTED_PREFIX.concat(new String(bytes, UTF8));
	}
}
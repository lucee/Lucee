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
package lucee.runtime.thread;

import java.io.Serializable;

import javax.servlet.http.Cookie;

import lucee.runtime.type.scope.CookieImpl;

public class SerializableCookie implements Serializable {

	private static final long serialVersionUID = -7167614871212402517L;

	private String comment;
	private String domain;
	private int maxAge;
	private String name;
	private String path;
	private boolean secure;
	private String value;
	private int version;
	private boolean httpOnly;

	public SerializableCookie(String comment, String domain, int maxAge, String name, String path, boolean secure, String value, int version, boolean httpOnly) {
		this.comment = comment;
		this.domain = domain;
		this.maxAge = maxAge;
		this.name = name;
		this.path = path;
		this.secure = secure;
		this.value = value;
		this.version = version;
		this.httpOnly = httpOnly;
	}

	public SerializableCookie(Cookie cookie) {
		this.comment = cookie.getComment();
		this.domain = cookie.getDomain();
		this.maxAge = cookie.getMaxAge();
		this.name = cookie.getName();
		this.path = cookie.getPath();
		this.secure = cookie.getSecure();
		this.value = cookie.getValue();
		this.version = cookie.getVersion();
		this.httpOnly = CookieImpl.isHTTPOnly(cookie);
	}

	public String getComment() {
		return comment;
	}

	public String getDomain() {
		return domain;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public boolean getSecure() {
		return secure;
	}

	public String getValue() {
		return value;
	}

	public int getVersion() {
		return version;
	}

	public boolean isHttpOnly() {
		return httpOnly;
	}

	public void setComment(String purpose) {
		this.comment = purpose;
	}

	public void setDomain(String pattern) {
		this.domain = pattern;
	}

	public void setMaxAge(int expiry) {
		this.maxAge = expiry;
	}

	public void setPath(String uri) {
		this.path = uri;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}

	public Cookie toCookie() {
		Cookie c = new Cookie(name, value);
		if (comment != null) c.setComment(comment);
		if (domain != null) c.setDomain(domain);
		c.setMaxAge(maxAge);
		if (path != null) c.setPath(path);
		c.setSecure(secure);
		c.setVersion(version);
		if (httpOnly) CookieImpl.setHTTPOnly(c);
		return c;
	}

	public static Cookie[] toCookies(SerializableCookie[] src) {
		if (src == null) return new Cookie[0];
		Cookie[] dest = new Cookie[src.length];
		for (int i = 0; i < src.length; i++) {
			dest[i] = src[i].toCookie();
		}
		return dest;
	}

	public static SerializableCookie[] toSerializableCookie(Cookie[] src) {
		if (src == null) return new SerializableCookie[0];
		SerializableCookie[] dest = new SerializableCookie[src.length];
		for (int i = 0; i < src.length; i++) {
			dest[i] = new SerializableCookie(src[i]);
		}
		return dest;
	}
}
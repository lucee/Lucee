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
package lucee.runtime.net.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.lang.StringUtils;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class MultiPartResponseUtils {

	public static boolean isMultipart(String mimetype) {
		return !StringUtil.isEmpty(extractBoundary(mimetype, null)) && StringUtil.startsWithIgnoreCase(mimetype, "multipart/");
	}

	public static Array getParts(byte[] barr, String contentTypeHeader) throws IOException, PageException {
		String boundary = extractBoundary(contentTypeHeader, "");
		ByteArrayInputStream bis = new ByteArrayInputStream(barr);
		MultipartStream stream;
		Array result = new ArrayImpl();
		stream = new MultipartStream(bis, getBytes(boundary, "UTF-8"));//

		boolean hasNextPart = stream.skipPreamble();
		while (hasNextPart) {
			result.append(getPartData(stream));
			hasNextPart = stream.readBoundary();
		}
		return result;
	}

	private static String extractBoundary(String contentTypeHeader, String defaultValue) {
		if (contentTypeHeader == null) return defaultValue;
		String[] headerSections = ListUtil.listToStringArray(contentTypeHeader, ';');
		for (String section: headerSections) {
			String[] subHeaderSections = ListUtil.listToStringArray(section, '=');
			String headerName = subHeaderSections[0].trim();
			if (headerName.toLowerCase().equals("boundary")) {
				return subHeaderSections[1].replaceAll("^\"|\"$", "");
			}

		}
		return defaultValue;
	}

	private static Struct getPartData(MultipartStream stream) throws IOException, PageException {
		Struct headers = extractHeaders(stream.readHeaders());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		stream.readBodyData(baos);
		Struct fileStruct = new StructImpl();
		fileStruct.set(KeyConstants._content, baos.toByteArray());
		fileStruct.set(KeyConstants._headers, headers);
		IOUtil.close(baos);
		return fileStruct;
	}

	private static Struct extractHeaders(String rawHeaders) throws PageException {
		Struct result = new StructImpl();
		String[] headers = ListUtil.listToStringArray(rawHeaders, '\n');
		for (String rawHeader: headers) {
			String[] headerArray = ListUtil.listToStringArray(rawHeader, ':');
			String headerName = headerArray[0];
			if (!StringUtil.isEmpty(headerName, true)) {
				String value = StringUtils.join(Arrays.copyOfRange(headerArray, 1, headerArray.length), ":").trim();
				result.set(headerName, value);
			}
		}
		return result;
	}

	private static byte[] getBytes(String string, String charset) {
		byte[] bytes;
		try {
			bytes = string.getBytes(charset);
		}
		catch (UnsupportedEncodingException e) {
			bytes = string.getBytes();
		}
		return bytes;
	}

}
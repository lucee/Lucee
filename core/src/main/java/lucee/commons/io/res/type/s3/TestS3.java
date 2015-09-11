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
package lucee.commons.io.res.type.s3;



import lucee.aprint;
import lucee.commons.date.TimeZoneConstants;
import lucee.commons.net.http.HTTPResponse;

public class TestS3 {
	public static void main(String[] args) throws Throwable {

		String accessKeyId = "1DHC5C5FVD7YEPR4DBG2"; 
		String secretAccessKey = "R/sOy3hgimrI8D9c0lFHchoivecnOZ8LyVmJpRFQ";
		HTTPResponse m;
		
		S3 s3=new S3(secretAccessKey, accessKeyId, TimeZoneConstants.CET);
		//raw = s3.listBucketsRaw();
		//print.o(StringUtil.replace(IOUtil.toString(raw, null),"<","\n<",false));
		
		//meta = s3.getMetadata("j878", "sub/text.txt");
		//print.o(meta);
		//meta = s3.getMetadata("j878", "sub/xxxx");
		//print.o(meta);
		//raw = s3.aclRaw("j878", null);
		//print.o(StringUtil.replace(IOUtil.toString(raw, null),"<","\n<",false));
		
		
		m = s3.head("j878", "sub/text.txt");
		aprint.o(m.getContentAsString());
		aprint.e(m.getStatusCode());
		//aprint.o(StringUtil.replace(m.getResponseBodyAsString(),"<","\n<",false));
		
		//m = s3.head("j878", null);
		//print.o(m.getResponseHeaders());
		//print.o(StringUtil.replace(m.getResponseBodyAsString(),"<","\n<",false));
		
	}
}
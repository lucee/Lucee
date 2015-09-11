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
package lucee.runtime.functions.s3;

import java.io.IOException;
import java.util.List;

import lucee.commons.io.res.type.s3.AccessControl;
import lucee.commons.io.res.type.s3.AccessControlPolicy;
import lucee.commons.io.res.type.s3.S3Resource;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class StoreSetACL extends S3Function {
	
	public static String call(PageContext pc , String url, Object objACL) throws PageException {
		try {
			return _call(pc, url, objACL);
		} catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	public static String _call(PageContext pc , String url, Object objACL) throws PageException, IOException {
		S3Resource res=toS3Resource(pc,url,"StoreSetACL");
		invoke(res, objACL);
		return null;
	}
	
	public static void invoke(S3Resource res, Object objACL) throws PageException, IOException {
		AccessControlPolicy acp = res.getAccessControlPolicy();
		List<AccessControl> acl = AccessControl.toAccessControlList(objACL);
		AccessControlPolicy.removeDuplicates(acl);
		acp.setAccessControlList(acl);
		res.setAccessControlPolicy(acp);
	}
	
	
}
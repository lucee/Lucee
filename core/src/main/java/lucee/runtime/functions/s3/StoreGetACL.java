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
import java.util.Iterator;
import java.util.List;

import lucee.commons.io.res.type.s3.AccessControl;
import lucee.commons.io.res.type.s3.S3Exception;
import lucee.commons.io.res.type.s3.S3Resource;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class StoreGetACL extends S3Function {

	public static final Collection.Key DISPLAY_NAME = KeyImpl.intern("displayName");
	public static final Collection.Key PERMISSION = KeyImpl.intern("permission");
	
	
	public static Object call(PageContext pc , String url) throws PageException {
        
		S3Resource res=toS3Resource(pc,url,"StoreGetACL");
		try {
			return toArrayStruct(res.getAccessControlPolicy().getAccessControlList());
		} catch (IOException e) {
			throw Caster.toPageException(e);
		}
    }

	private static Object toArrayStruct(List<AccessControl> accessControlList) throws S3Exception {
		Array arr=new ArrayImpl();
		String type;
		Struct sct;
		AccessControl ac;
		Iterator<AccessControl> it = accessControlList.iterator();
		while(it.hasNext()){
			ac=it.next();
			arr.appendEL(sct=new StructImpl());
			sct.setEL(KeyConstants._id, ac.getId());
			sct.setEL(PERMISSION, ac.getPermission());
			
			type = AccessControl.toType(ac.getType());
			if("Group".equalsIgnoreCase(type)) 				
				setGroup(sct,ac);
			else if("CanonicalUser".equalsIgnoreCase(type)) 
				sct.setEL(DISPLAY_NAME, ac.getDisplayName());
			else 
				sct.setEL(KeyConstants._email, ac.getId());
		}
		return arr;
	}
	
	private static void setGroup(Struct sct, AccessControl ac) {
		String uri = ac.getUri();
		sct.setEL(KeyConstants._id, uri);
		if("http://acs.amazonaws.com/groups/global/AllUsers".equalsIgnoreCase(uri))
			sct.setEL(KeyConstants._group, "all");
		else if("http://acs.amazonaws.com/groups/global/AuthenticatedUsers".equalsIgnoreCase(uri))
			sct.setEL(KeyConstants._group, "authenticated");
		else if("http://acs.amazonaws.com/groups/s3/LogDelivery".equalsIgnoreCase(uri))
			sct.setEL(KeyConstants._group, "log_delivery");
	}

}
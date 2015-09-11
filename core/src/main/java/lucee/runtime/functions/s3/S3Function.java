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

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.type.s3.S3Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;

public class S3Function {


	public static S3Resource toS3Resource(PageContext pc, String url, String functionName) throws ExpressionException {
		Resource res=ResourceUtil.toResourceNotExisting(pc, url);
		ResourceProvider provider = res.getResourceProvider();
		if(!provider.getScheme().equalsIgnoreCase("s3") || !res.exists()) 
			throw new FunctionException(pc,functionName,1,"url","defined url must be a valid existing S3 Resource");
		
		return (S3Resource) res;
	}
}
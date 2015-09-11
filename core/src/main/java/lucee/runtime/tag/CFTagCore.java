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
package lucee.runtime.tag;

import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.customtag.InitFile;
import lucee.runtime.exp.PageException;

public class CFTagCore extends CFTag {

	private String name;
	private String filename;
	private boolean isweb;


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	
	public void set__name(String name){
		this.name=name;
	}
	public void set__filename(String filename){
		this.filename=filename;
	}
	public void set__isweb(boolean isweb){
		this.isweb=isweb;
	}
	@Override
	public InitFile initFile(PageContext pageContext) throws PageException {
    	return createInitFile(pageContext,isweb,filename);
     
    }
	
	public static InitFile createInitFile(PageContext pageContext,boolean isweb,String filename) {
    	ConfigWebImpl config = (ConfigWebImpl) pageContext.getConfig();
    	Mapping mapping=isweb?config.getTagMapping():config.getServerTagMapping();
    	
    	return new InitFile(pageContext,
    			mapping.getPageSource(filename),
    			filename);
     
    }
}
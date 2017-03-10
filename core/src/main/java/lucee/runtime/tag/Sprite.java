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

import java.io.IOException;

import lucee.commons.digest.MD5;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.functions.image.ImageNew;
import lucee.runtime.functions.image.ImageWrite;
import lucee.runtime.img.Image;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public final class Sprite extends TagImpl {
	
	private String _id;
	private String _ids;
	private String _srcs;
	
	String src;
		

	
	@Override
	public void release()	{
		this._id=null;
		this._ids=null;
		this.src=null;
		this._srcs=null;
		super.release();
	}
	
	
	
	public void set_ids(String _ids){
		this._ids=_ids;
	}

	public void set_id(String _id){
		this._id=_id;
	}

	public void set_srcs(String _srcs){
		this._srcs=_srcs;
	}

	public void setSrc(String src){
		this.src=src;
	}
	
	@Override

	public int doStartTag() throws PageException	{
		try {
			return _doStartTag();
		} catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
			throw Caster.toPageException(e);
		}
	}
	
	
	public int _doStartTag() throws Throwable	{
		
		// write out div for single item
		pageContext.write("<div id=\""+_id+"\"></div>");
		
		
		
		
		
		
		// handle all items
		if(!StringUtil.isEmpty(_ids)) {
			String[] ids=ListUtil.listToStringArray(_ids, ',');
			String[] strSrcs=ListUtil.listToStringArray(_srcs, ',');
			Resource[] srcs=new Resource[strSrcs.length];
			Image[] images=new Image[strSrcs.length];
			for(int i=0;i<srcs.length;i++){
				srcs[i]=ResourceUtil.toResourceExisting(pageContext, strSrcs[i]);
				images[i] = new Image(srcs[i]);
			}
			
			// TODO use the same resource as for cfimage
			PageSource ps = pageContext.getCurrentTemplatePageSource();
			 Resource dir;
			if(ps!=null) {
				Resource curr = ps.getResource();
				dir = curr.getParentResource();
			}
			else dir=SystemUtil.getTempDirectory();
			
			 
			Resource cssDir = dir.getRealResource("css");
			Resource pathdir = cssDir;
			cssDir.mkdirs();
			
			
			//the base name for the files we are going to create as a css and image
			String baseRenderedFileName = MD5.getDigestAsString(_ids);
			Resource cssFileName = cssDir.getRealResource(baseRenderedFileName+".css");
			Resource imgFileName = pathdir.getRealResource(baseRenderedFileName+"."+ResourceUtil.getExtension(src,""));
			
			//if the files don't exist, then we create them, otherwise
			boolean bCreate = !cssFileName.isFile() || !imgFileName.isFile();
			
			
			//Are we going to create it, let's say no
			String css = "";
			if(bCreate){
				int imgMaxHeight = 0;
				int imgMaxWidth = 0;
				Image img;
				int actualWidth,actualHeight;
				//Setup the max height and width of the new image. 
				for(int i=0;i<srcs.length;i++){
					img = images[i];
					
					//set the image original height and width 
					actualWidth = img.getWidth();;
					actualHeight = img.getHeight();
									
									
					
					//Check if there is a height, 
					imgMaxHeight += actualHeight;
					if(actualWidth  > imgMaxWidth) imgMaxWidth  =  actualWidth;
				}
				
				//Create the new image (hence we needed to do two of these items)
				Image spriteImage = (Image) ImageNew.call(pageContext,"", ""+imgMaxWidth,""+imgMaxHeight, "argb");
				
				int placedHeight = 0;
				//Loop again but this time, lets do the copy and paste
				for(int i=0;i<srcs.length;i++){
					img = images[i];
					spriteImage.paste(img,1,placedHeight);
					
						css += "#"+ids[i]+" {\n\tbackground: url("+baseRenderedFileName+"."+ResourceUtil.getExtension(strSrcs[i],"")+") 0px -"+placedHeight+"px no-repeat; width:"+img.getWidth()+"px; height:"+img.getHeight()+"px;\n} \n";
						placedHeight += img.getHeight();
				}
				
				//Now Write the CSS and the Sprite Image
				
				ImageWrite.call(pageContext, spriteImage, imgFileName.getAbsolutePath());
				IOUtil.write(cssFileName, css,"UTF-8",false);
				
			}

			
			//pageContext.write("<style>"+css+"</style>");

			try {
				((PageContextImpl)pageContext).getRootOut()
					.appendHTMLHead("<link rel=\"stylesheet\" href=\"css/"+baseRenderedFileName+".css\" type=\"text/css\" media=\"screen\" title=\"no title\" charset=\"utf-8\">");
			} catch (IOException e) {
				Caster.toPageException(e);
			} 
			
		}
		
		
		
		
		return SKIP_BODY;
	}



	@Override
	public int doEndTag()	{
		return EVAL_PAGE;
	}
}
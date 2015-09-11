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
package lucee.commons.pdf;

import java.awt.Dimension;
import java.awt.Insets;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;

import lucee.VersionInfo;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.util.FileWrapper;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.Md5;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class PDF {
	
	private final Object pd4ml;
	private static Class pd4mlClass;
	private static ClassLoader classLoader;
	private static Class pd4mlMarkClass;
	//private final boolean isEvaluation;

	public PDF(Config config) throws PageException {
		//this.isEvaluation=isEvaluation;
		try {
			//classLoader=new URLClassLoader(new URL[]{new File("/Users/mic/Downloads/java/pd4ml/fullversion/pd4ml.volume.310/lib/pd4ml.jar").toURL()},this.getClass().getClassLoader());
			if(classLoader==null) {
				Resource temp = config.getConfigDir().getRealResource("temp");
				lucee.Info info = ConfigWebUtil.getEngine(config).getInfo();
				Resource file=temp.getRealResource(Md5.getDigestAsString(info.getVersion().toString())+".lmdp");
		        
				if(!file.exists()){
					ResourceUtil.removeChildrenEL(temp, new ExtensionResourceFilter(".lmdp"));
					
		        	file.createFile(true);
		        	//print.out(new Info().getClass().getResource("/resource/lib/pd4ml.jar"));
		        	InputStream jar = InfoImpl.class.getResourceAsStream("/resource/lib/pd4ml.jar");
		    		IOUtil.copy(jar, file,true);
		        }
		        ClassLoader parent = VersionInfo.class.getClassLoader();
		        classLoader=new URLClassLoader(new URL[]{FileWrapper.toFile(file).toURL()},parent);
		    	
		    	//classLoader=new URLClassLoader(new URL[]{new Info().getClass().getResource("/resource/lib/pd4ml.jar")},this.getClass().getClassLoader());
			}
			if(pd4mlClass==null)pd4mlClass=ClassUtil.loadClass(classLoader,"org.zefer.pd4ml.PD4ML");
			pd4ml=ClassUtil.loadInstance(pd4mlClass);
			
		} catch (Exception e) {
			throw Caster.toPageException(e);
		}
		pd4mlClass=pd4ml.getClass();
	}
	
	public void enableTableBreaks(boolean b) throws PageException {
		invoke(pd4ml,"enableTableBreaks",b);
	}

	public void interpolateImages(boolean b) throws PageException {
		invoke(pd4ml,"interpolateImages",b);
	}

	public void adjustHtmlWidth() throws PageException {
		invoke(pd4ml,"adjustHtmlWidth");
	}

	public void setPageInsets(Insets insets) throws PageException {
		invoke(pd4ml,"setPageInsets",insets);
	}

	public void setPageSize(Dimension dimension) throws PageException {
		invoke(pd4ml,"setPageSize",dimension);
	}

	public void setPageHeader(PDFPageMark header) throws PageException {
		invoke(pd4ml,"setPageHeader",toPD4PageMark(header));
	}
	
	public void generateOutlines(boolean flag) throws PageException {
		invoke(pd4ml,"generateOutlines",
				new Object[]{Caster.toBoolean(flag)},new Class[]{boolean.class});
	}

	public void useTTF(String pathToFontDirs,boolean embed ) throws PageException {
		invoke(pd4ml,"useTTF",
				new Object[]{pathToFontDirs,Caster.toBoolean(embed)},
				new Class[]{String.class,boolean.class});
	}
	

	public void setDefaultTTFs(String string, String string2, String string3) throws PageException {
		invoke(pd4ml,"setDefaultTTFs",
				new Object[]{string,string2,string3},
				new Class[]{String.class,String.class,String.class});
	}

	public void setPageFooter(PDFPageMark footer) throws PageException {
		//if(isEvaluation) return;
		invoke(pd4ml,"setPageFooter",toPD4PageMark(footer));
	}
	
	public void render(InputStreamReader reader, OutputStream os) throws PageException {
		//setEvaluationFooter();
		
		
		invoke(pd4ml, "render", 
				new Object[]{reader,os}, 
				new Class[]{reader.getClass(),OutputStream.class});
		
		//invoke(pd4ml,"render",reader,os,OutputStream.class);
		
	}

	public void render(String str, OutputStream os,URL base) throws PageException {
		//setEvaluationFooter();
		
		StringReader sr = new StringReader(str);
		if(base==null) {
			invoke(pd4ml, "render", 
				new Object[]{sr,os}, 
				new Class[]{sr.getClass(),OutputStream.class});
		}
		else {
			invoke(pd4ml, "render", 
				new Object[]{sr,os,base}, 
				new Class[]{sr.getClass(),OutputStream.class,URL.class});
		}
		//invoke(pd4ml,"render",new StringReader(str),os,OutputStream.class);
	}
	
	/*private void setEvaluationFooterX() throws PageException {
		if(isEvaluation) invoke(pd4ml,"setPageFooter",toPD4PageMark(new PDFPageMark(-1,EVAL_TEXT)));
	}*/


	private Object toPD4PageMark(PDFPageMark mark) throws PageException {
		Object pd4mlMark=null;
		try {
			if(pd4mlMarkClass==null)pd4mlMarkClass=ClassUtil.loadClass(classLoader,"org.zefer.pd4ml.PD4PageMark");
			pd4mlMark=ClassUtil.loadInstance(pd4mlMarkClass);
			
		} catch (Exception e) {
		}

		invoke(pd4mlMark,"setAreaHeight",mark.getAreaHeight());
		invoke(pd4mlMark,"setHtmlTemplate",mark.getHtmlTemplate());
		return pd4mlMark;
	}
	
	private void invoke(Object o,String methodName, Object[] args, Class[] argClasses) throws PageException {
		try {
			o.getClass().getMethod(methodName, argClasses).invoke(o, args);
		} 
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
	/*private void invoke(Object o,String methodName, Object argument1, Object argument2,Class clazz) throws PageException {
		try {
			o.getClass().getMethod(methodName, new Class[]{argument1.getClass(),clazz}).invoke(o, new Object[]{argument1,argument2});
		} 
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}*/
	private void invoke(Object o,String methodName, Object argument) throws PageException {
		try {
			o.getClass().getMethod(methodName, new Class[]{argument.getClass()}).invoke(o, new Object[]{argument});
		} 
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private void invoke(Object o,String methodName, boolean argument) throws PageException {
		try {
			o.getClass().getMethod(methodName, new Class[]{boolean.class}).invoke(o, new Object[]{Caster.toRef(argument)});
		} 
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
	private void invoke(Object o,String methodName, int argument) throws PageException {
		try {
			o.getClass().getMethod(methodName, new Class[]{int.class}).invoke(o, new Object[]{Caster.toRef(argument)});
		} 
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
	
	private void invoke(Object o,String methodName) throws PageException {
		try {
			o.getClass().getMethod(methodName, new Class[]{}).invoke(o, new Object[]{});
		} 
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}


}
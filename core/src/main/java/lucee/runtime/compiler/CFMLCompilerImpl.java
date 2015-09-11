/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.compiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeFactory;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ClassRenamer;
import lucee.transformer.cfml.tag.CFMLTransformer;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.util.AlreadyClassException;
import lucee.transformer.util.PageSourceCode;
import lucee.transformer.util.SourceCode;



/**
 * CFML Compiler compiles CFML source templates
 */
public final class CFMLCompilerImpl implements CFMLCompiler {

	private CFMLTransformer cfmlTransformer;
	private Map<String,WatchEntry> watched=new ConcurrentHashMap<String,WatchEntry>(); 
	

	
	/**
	 * Constructor of the compiler
	 * @param config
	 */
	public CFMLCompilerImpl() {
		cfmlTransformer=new CFMLTransformer();
	}
	
	public Result compile(ConfigImpl config,PageSource ps, TagLib[] tld, FunctionLib[] fld, 
        Resource classRootDir, boolean returnValue, boolean ignoreScopes) throws TemplateException, IOException {
		return _compile(config, ps, null,null, tld, fld, classRootDir,returnValue,ignoreScopes);
	}
	
	public Result compile(ConfigImpl config,SourceCode sc, TagLib[] tld, FunctionLib[] fld, 
		Resource classRootDir, String className, boolean returnValue,boolean ignoreScopes) throws TemplateException, IOException {
		
		// just to be sure
		PageSource ps=(sc instanceof PageSourceCode)?((PageSourceCode)sc).getPageSource():null;
		
		return _compile(config, ps, sc,className, tld, fld, classRootDir,returnValue,ignoreScopes);
	}
	
	/*private byte[] _compiless(ConfigImpl config,PageSource ps,SourceCode sc,String className, TagLib[] tld, FunctionLib[] fld, 
			Resource classRootDir,TransfomerSettings settings) throws TemplateException {
		Factory factory = BytecodeFactory.getInstance(config);
		
		Page page=null;
		
		TagLib[][] _tlibs=new TagLib[][]{null,new TagLib[0]};
		_tlibs[CFMLTransformer.TAG_LIB_GLOBAL]=tld;
		// reset page tlds
		if(_tlibs[CFMLTransformer.TAG_LIB_PAGE].length>0) {
			_tlibs[CFMLTransformer.TAG_LIB_PAGE]=new TagLib[0];
		}
		
		CFMLScriptTransformer scriptTransformer = new CFMLScriptTransformer();
		scriptTransformer.transform(
				BytecodeFactory.getInstance(config)
				, page
				, new EvaluatorPool()
				, _tlibs, fld
				, null
				, config.getCoreTagLib(ps.getDialect()).getScriptTags()
				, sc
				, settings);
		
		//CFMLExprTransformer extr=new CFMLExprTransformer();
		//extr.transform(factory, page, ep, tld, fld, scriptTags, cfml, settings)
		
		return null;
	}*/
	
	private Result _compile(ConfigImpl config,PageSource ps,SourceCode sc,String className, TagLib[] tld, FunctionLib[] fld, 
        Resource classRootDir, boolean returnValue, boolean ignoreScopes) throws TemplateException, IOException {
		Result result=null;
		//byte[] barr = null;
			Page page = null;
			Factory factory = BytecodeFactory.getInstance(config);
	        try {
	        	page = sc==null? 
	        			cfmlTransformer.transform(factory,config,ps,tld,fld,returnValue,ignoreScopes):
	        			cfmlTransformer.transform(factory,config,sc,tld,fld,System.currentTimeMillis(),
	        					sc.getDialect()==CFMLEngine.DIALECT_CFML && config.getDotNotationUpperCase(),returnValue,ignoreScopes);
	        	page.setSplitIfNecessary(false);
	        	try {
	        		result=new Result(page,page.execute(className));
	        		//barr = page.execute(className);
	        	}
	        	catch(RuntimeException re) {
	        		String msg=StringUtil.emptyIfNull(re.getMessage());
	        		if(StringUtil.indexOfIgnoreCase(msg, "Method code too large!")!=-1) {
	        			page = sc==null? 
	    	        			cfmlTransformer.transform(factory,config,ps,tld,fld,returnValue,ignoreScopes):
	    	        			cfmlTransformer.transform(factory,config,sc,tld,fld,
	    	        					System.currentTimeMillis(),
	    	        					sc.getDialect()==CFMLEngine.DIALECT_CFML && config.getDotNotationUpperCase(),returnValue,ignoreScopes);

	        			page.setSplitIfNecessary(true);
	        			result = new Result(page,page.execute(className));
	        		}
	        		else throw re;
	        	}
		        catch(ClassFormatError cfe) {
		        	String msg=StringUtil.emptyIfNull(cfe.getMessage());
		        	if(StringUtil.indexOfIgnoreCase(msg, "Invalid method Code length")!=-1) {
		        		page = ps!=null? 
			        			cfmlTransformer.transform(factory,config,ps,tld,fld,returnValue,ignoreScopes):
			        			cfmlTransformer.transform(factory,config,sc,tld,fld,System.currentTimeMillis(),
			        					sc.getDialect()==CFMLEngine.DIALECT_CFML && config.getDotNotationUpperCase(),returnValue,ignoreScopes);
			        	
		        		
		        		
			        	page.setSplitIfNecessary(true);
		        		result = new Result(page,page.execute(className));
		        	}
		        	else throw cfe;
		        }
		        
	        	// store
	        	if(classRootDir!=null) {
		        	Resource classFile = classRootDir.getRealResource(page.getClassName()+".class");
					Resource classFileDirectory=classFile.getParentResource();
			        if(!classFileDirectory.exists()) classFileDirectory.mkdirs(); 
					IOUtil.copy(new ByteArrayInputStream(result.barr), classFile,true);
	        	}
				
		        return result;
			} 
	        catch (AlreadyClassException ace) {
	        	InputStream is=null;
	        	try{
	        		result = new Result(null,IOUtil.toBytes(is=ace.getInputStream()));
	        		
	        		String displayPath=ps!=null?"["+ps.getDisplayPath()+"] ":"";
	        		String srcName = ASMUtil.getClassName(result.barr);
	        		// source is cfm and target cfc
	        		if(sc.getDialect()==CFMLEngine.DIALECT_CFML &&
	        				endsWith(srcName,Constants.getCFMLTemplateExtensions(),sc.getDialect())
	        				&& 
	        				className.endsWith("_"+Constants.getCFMLComponentExtension()+
	        					(sc.getDialect()==CFMLEngine.DIALECT_CFML?Constants.CFML_CLASS_SUFFIX:Constants.LUCEE_CLASS_SUFFIX))) {
	        				throw new TemplateException("source file "+displayPath+"contains the bytecode for a regular cfm template not for a component");
	        		}
	        		// source is cfc and target cfm
	        		if(sc.getDialect()==CFMLEngine.DIALECT_CFML &&
	        				srcName.endsWith("_"+Constants.getCFMLComponentExtension()+(sc.getDialect()==CFMLEngine.DIALECT_CFML?Constants.CFML_CLASS_SUFFIX:Constants.LUCEE_CLASS_SUFFIX)) && 
	        				endsWith(className,Constants.getCFMLTemplateExtensions(),sc.getDialect())
	        				)
	        				throw new TemplateException("source file "+displayPath+"contains a component not a regular cfm template");
	        		
	        		// rename class name when needed
	        		if(!srcName.equals(className))result=new Result(result.page, ClassRenamer.rename(result.barr, className));
	        		// store
		        	if(classRootDir!=null) {
		        		Resource classFile=classRootDir.getRealResource(className+".class");
		    			Resource classFileDirectory=classFile.getParentResource();
		    			if(!classFileDirectory.exists()) classFileDirectory.mkdirs(); 
		    			result=new Result(result.page, Page.setSourceLastModified(result.barr,ps!=null?ps.getPhyscalFile().lastModified():System.currentTimeMillis()));
		        		IOUtil.copy(new ByteArrayInputStream(result.barr), classFile,true);
		        	}
	        		
	        	}
	        	finally {
	        		IOUtil.closeEL(is);
	        	}
	        	return result;
	        }
	        catch (TransformerException bce) {
	        	Position pos = bce.getPosition();
	        	int line=pos==null?-1:pos.line;
	        	int col=pos==null?-1:pos.column;
	        	if(ps!=null)bce.addContext(ps, line, col,null);
	        	throw bce;
			}
	}

	private boolean endsWith(String name, String[] extensions, int dialect) {
		for(int i=0;i<extensions.length;i++){
			if(name.endsWith("_"+extensions[i]+(dialect==CFMLEngine.DIALECT_CFML?Constants.CFML_CLASS_SUFFIX:Constants.LUCEE_CLASS_SUFFIX))) return true;
		}
		return false;
	}

	
	public Page transform(ConfigImpl config,PageSource source, TagLib[] tld, FunctionLib[] fld, boolean returnValue, boolean ignoreScopes) throws TemplateException, IOException {
		return cfmlTransformer.transform(BytecodeFactory.getInstance(config),config,source,tld,fld,returnValue,ignoreScopes);
	}
	

	public class Result {

		public Page page;
		public byte[] barr;

		public Result(Page page, byte[] barr) {
			this.page=page;
			this.barr=barr;
		}
	}
	
	public void watch(PageSource ps, long now) {
		watched.put(ps.getDisplayPath(),new WatchEntry(ps,now,ps.getPhyscalFile().length(),ps.getPhyscalFile().lastModified()));
	}

	public void checkWatched() {
		long now=System.currentTimeMillis();
		Iterator<Entry<String, WatchEntry>> it = watched.entrySet().iterator();
		Entry<String, WatchEntry> e;
		while(it.hasNext()){
			e=it.next();
			if(e.getValue().now+1000<=now) {// only check entries that are at least a second old
				if(e.getValue().length!=e.getValue().ps.getPhyscalFile().length()) { // file changed (size or time)
					((PageSourceImpl)e.getValue().ps).flush();
				}
				watched.remove(e.getKey());
			}
		}
		
	}
	
	private class WatchEntry {

		private final PageSource ps;
		private final long now;
		private final long length;
		private final long lastModified;

		public WatchEntry(PageSource ps, long now, long length, long lastModified) {
			this.ps=ps;
			this.now=now;
			this.length=length;
			this.lastModified=lastModified;
		}
	}
}
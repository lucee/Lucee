package lucee.runtime.template;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Mapping;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.compiler.CFMLCompilerImpl.Result;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.MissingIncludeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.op.Caster;

public class CorePageFactory implements TemplatePageFactory {
	
	private final CoreTemplateEngine engine;
	private final int dialect;
	
	public CorePageFactory(CoreTemplateEngine engine) {
		this.engine = engine;
		dialect = engine.getDialect();
	}

	
	
	public static final byte LOAD_ARCHIVE=2;
	public static final byte LOAD_PHYSICAL=3;
	
//		private LuceeCoreTemplateEngine cfTemplateEngine;
//	
//		public LuceeCorePageFactory(LuceeCoreTemplateEngine cfTemplateEngine) {
//			this.cfTemplateEngine = cfTemplateEngine;
//		}

	@Override
	public Page getPage(PageContext pc, PageSource ps, boolean forceReload, Page defaultValue) throws PageException {
		PageSourceImpl psi = (PageSourceImpl) ps;
		
		if (forceReload)
			psi.setPage(null);
		
		Page page = psi.getPage();
		Mapping mapping = ps.getMapping();
		
		if (mapping.isPhysicalFirst()) {
			page = loadPhysical(pc, ps, page);
			if (page==null) page = loadArchive(ps, page);
			if (page != null) return page;
		} else {
			page = loadArchive(ps, page);
			if (page==null) page=loadPhysical(pc, ps, page);
			if (page != null) return page;
		}
		
		if(defaultValue == null)
			throw new MissingIncludeException(ps);
		
		return defaultValue;
	}

	private Page loadArchive(PageSource ps, Page page) {
		PageSourceImpl psi = (PageSourceImpl) ps;
		Mapping mapping = ps.getMapping();
		if (!mapping.hasArchive()) return null;
		if (page != null && page.getLoadType() == LOAD_ARCHIVE) return page;
		try {
			synchronized(ps) {
				Class clazz = mapping.getArchiveClass(psi.getClassName());
				page = newInstance(clazz, ps);
				psi.setPage(page);
				page.setPageSource(psi);
				page.setLoadType(LOAD_ARCHIVE);
				return page;
			}
		} catch(Exception e) {
			return null;
		}
	}
	
	private Page loadPhysical(PageContext pc, PageSource ps, Page page) throws TemplateException {
		PageSourceImpl psi = (PageSourceImpl)ps;
		Mapping mapping = ps.getMapping();
		
		if (!mapping.hasPhysical()) return null;
		
		ConfigWeb config = pc.getConfig();
		PageContextImpl pci = (PageContextImpl)pc;
		if ((mapping.getInspectTemplate() == Config.INSPECT_NEVER || pci.isTrusted(page)) && isLoad(page, LOAD_PHYSICAL))
			return page;
		
		Resource srcFile = ps.getPhyscalFile();
		long srcLastModified = srcFile.lastModified();
		if (srcLastModified == 0L) return null;
		
		// page exists
		if (page != null) {
			if (srcLastModified != page.getSourceLastModified()) {
				page = compile(config, ps, page, false, false);
				psi.setPage(page);
				page.setPageSource(psi);
				page.setLoadType(LOAD_PHYSICAL);
			}
		
		// page doesn't exist
		} else {
			
			Resource crd = mapping.getClassRootDirectory();
			Resource classFile = crd.getRealResource(psi.getJavaName()+".class");
			boolean isNew = false;
			
			if (psi.isFlush() && !classFile.exists()) {
				page = compile(config, ps, null, false, false);
				psi.setPage(page);
				psi.resetFlush();
				isNew = true;
			} else {
				try {
					page = newInstance(mapping.getPhysicalClass(psi.getClassName()), ps);
				} catch(Throwable t) {
//					System.out.println("DANGER WILL");
//					System.out.println(psi.getClassName());
//					t.printStackTrace();
					page = null;
				}
				
				if (page == null) page = compile(config, ps, null, false, false);
				psi.setPage(page);
			}
			
			// check for newer version
			if (!isNew)
				if (   srcLastModified != page.getSourceLastModified()
					|| page.getVersion() != pc.getConfig().getFactory().getEngine().getInfo().getFullVersionInfo()) {
					isNew = true;
					page = compile(config, ps, page, false, false);
					psi.setPage(page);
				}
			
			page.setPageSource(ps);
			page.setLoadType(LOAD_PHYSICAL);
			
		}
		
		pci.setPageUsed(page);
		
		return page;
	}
	
	
	private synchronized Page compile(ConfigWeb config,PageSource ps, Page existing, boolean returnValue, boolean ignoreScopes) throws TemplateException {
		Resource classRootDir = ps.getMapping().getClassRootDirectory();
		try {
			return _compile(config,ps, classRootDir,existing,returnValue,ignoreScopes);
		}
			catch(RuntimeException re) {re.printStackTrace();
			String msg=StringUtil.emptyIfNull(re.getMessage());
			if(StringUtil.indexOfIgnoreCase(msg, "Method code too large!")!=-1) {
				throw new TemplateException("There is too much code inside the template ["+ps.getDisplayPath()+"], "+Constants.NAME+" was not able to break it into pieces, move parts of your code to an include or a external component/function",msg);
			}
			throw re;
		}
		catch(ClassFormatError e) {
			String msg=StringUtil.emptyIfNull(e.getMessage());
			if(StringUtil.indexOfIgnoreCase(msg, "Invalid method Code length")!=-1) {
				throw new TemplateException("There is too much code inside the template ["+ps.getDisplayPath()+"], "+Constants.NAME+" was not able to break it into pieces, move parts of your code to an include or a external component/function",msg);
			}
			throw new TemplateException("ClassFormatError:"+e.getMessage());
		}
		catch(Throwable t) {
			if(t instanceof TemplateException) throw (TemplateException)t;
			throw new TemplateException(t.getClass().getName()+":"+t.getMessage());
		}
	}

	private Page _compile(ConfigWeb config,PageSource ps, Resource classRootDir, Page existing,boolean returnValue, boolean ignoreScopes) throws IOException, SecurityException, IllegalArgumentException, PageException {
		ConfigWebImpl cwi=(ConfigWebImpl) config;
		
		long now;
		if((ps.getPhyscalFile().lastModified()+10000)>(now=System.currentTimeMillis()))
			cwi.getCompiler().watch(ps,now);
		
		Result result = cwi.getCompiler().
			compile(cwi,ps,cwi.getTLDs(dialect),cwi.getFLDs(dialect),classRootDir,returnValue,ignoreScopes);
		
		try {
			Class<?> clazz = ps.getMapping().getPhysicalClass(ps.getClassName(), result.barr);
			return newInstance(clazz, ps);
		} catch(Throwable t) {
			PageException pe = Caster.toPageException(t);
			pe.setExtendedInfo("failed to load template "+ps.getDisplayPath());
			throw pe;
		}
	}
	
	

	private boolean isLoad(Page page, byte load) {
		return page!=null && load==page.getLoadType();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Page newInstance(Class clazz, PageSource ps) throws SecurityException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Constructor<?> c = clazz.getConstructor(new Class[]{PageSource.class});
		return (Page) c.newInstance(new Object[]{ps});
	}

	
//	@Override
//	public Page getPage(PageContext pc, PageSource ps, boolean forceReload, Page defaultPage) {
//		return loadArchivePage(pc, ps, defaultPage);
//	}
//	
//	private Page loadArchivePage(PageContext pc, PageSource ps, Page defaultPage) {
//		Page page;
//		Mapping mapping = ps.getMapping();
//		if (!mapping.hasArchive()) return  null;
//		if (defaultPage != null && defaultPage.getLoadType() == PageSourceImpl.LOAD_ARCHIVE) return defaultPage;
//		
//		PageSourceImpl psi = (PageSourceImpl)ps;
//		
//		try {
//			
//			Class<?> clazz = mapping.getArchiveClass(ps.getClassName());
//			page = newInstance(clazz, ps);
//			psi.setPage(page);
//			page.setPageSource(ps);
//			page.setLoadType(PageSourceImpl.LOAD_ARCHIVE);
//		
//		} catch(Exception e) {
//			page = null;
//		}
//		
//		return page;
//	}
//	
//	private Page loadPhysicalPage(PageContext pc, PageSource ps, boolean forceReload, Page defaultPage) {
//		return null;
//	}
//	
//	
//	
//	private Page newInstance(Class<?> clazz, PageSource ps) throws SecurityException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//		Constructor<?> c = clazz.getConstructor(new Class[]{PageSource.class});
//		return (Page) c.newInstance(new Object[]{ps});
//	}
}

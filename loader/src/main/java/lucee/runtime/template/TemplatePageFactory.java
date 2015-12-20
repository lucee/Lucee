/**
 * 
 */
package lucee.runtime.template;

import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.exp.PageException;

/**
 * For a given page source, return a Page object.
 *  
 * @author dajester2013
 *
 */
public interface TemplatePageFactory {

	public Page getPage(PageContext pc, PageSource ps, boolean forceReload, Page defaultValue) throws PageException;
	
}

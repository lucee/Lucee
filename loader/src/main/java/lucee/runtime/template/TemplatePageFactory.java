/**
 * 
 */
package lucee.runtime.template;

import lucee.runtime.Page;
import lucee.runtime.PageSource;

/**
 * Template engines are a way for Lucee to incorporate 3rd party templating systems. 
 * @author dajester2013
 *
 */
public interface TemplatePageFactory {

	public Page getPage(PageSource ps);
	
}

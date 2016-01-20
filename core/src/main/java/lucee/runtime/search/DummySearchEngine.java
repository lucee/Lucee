/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.search;

import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.type.Query;

import org.w3c.dom.Element;

public class DummySearchEngine implements SearchEngine {
	
	private static final String LUCENE = "EFDEB172-F52E-4D84-9CD1A1F561B3DFC8";
	private static boolean tryToInstall=true;

	@Override
	public void init(Config config, Resource searchDir) {
		
	}

	@Override
	public SearchCollection getCollectionByName(String name) throws SearchException {
		throw notInstalled();
	}

	@Override
	public Query getCollectionsAsQuery() throws SearchException {
		throw notInstalled();
	}

	@Override
	public SearchCollection createCollection(String name, Resource path, String language, boolean allowOverwrite) throws SearchException {
		throw notInstalled();
	}

	@Override
	public Resource getDirectory() {
		throw notInstalledEL();
	}

	/*@Override
	public Element getIndexElement(Element collElement, String id) {
		throw notInstalledEL();
	}*/

	@Override
	public String getDisplayName() {
		throw notInstalledEL();
	}

	@Override
	public SearchData createSearchData(int suggestionMax) {
		throw notInstalledEL();
	}
	
	private SearchException notInstalled() {
		/*if(tryToInstall){
			try {
				ConfigWebImpl config = (ConfigWebImpl) ThreadLocalPageContext.getConfig();
				if(config.installServerExtension(LUCENE))
					return new SearchException("Lucene Search Engine installed, with the next request the extension should work.");
			}
			finally {
				tryToInstall=false;
			}
		}*/
		return new SearchException("No Search Engine installed! Check out the Extension Store in the Lucee Administrator for \"Search\".");
	}
	

	private PageRuntimeException notInstalledEL() {
		return new PageRuntimeException(notInstalled());
	}

}
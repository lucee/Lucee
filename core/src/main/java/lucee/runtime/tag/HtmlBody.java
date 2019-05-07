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
package lucee.runtime.tag;

import java.io.IOException;

import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;

public final class HtmlBody extends HtmlHeadBodyBase {

	@Override
	public String getTagName() {
		return "htmlbody";
	}

	@Override
	public boolean getDefaultForce() {
		return false;
	}

	@Override
	public void actionAppend() throws IOException, ApplicationException {

		((PageContextImpl) pageContext).getRootOut().appendHTMLBody(text);
	}

	@Override
	public void actionWrite() throws IOException, ApplicationException {

		((PageContextImpl) pageContext).getRootOut().writeHTMLBody(text);
	}

	@Override
	public void actionReset() throws IOException {

		((PageContextImpl) pageContext).getRootOut().resetHTMLBody();
	}

	@Override
	public void actionRead() throws PageException, IOException {

		String str = ((PageContextImpl) pageContext).getRootOut().getHTMLBody();
		pageContext.setVariable(variable != null ? variable : "cfhtmlbody", str);
	}

	@Override
	public void actionFlush() throws IOException {

		PageContextImpl pci = (PageContextImpl) pageContext;

		pci.write(pci.getRootOut().getHTMLBody());
		pci.getRootOut().resetHTMLBody();
	}

}
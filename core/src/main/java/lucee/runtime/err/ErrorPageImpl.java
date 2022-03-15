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
package lucee.runtime.err;

import lucee.runtime.PageSource;

public final class ErrorPageImpl implements ErrorPage {

	/** Type of exception. Required if type = "exception" or "monitor". */
	private String exception = "any";

	/** The relative path to the custom error page. */
	private PageSource template;

	/**
	 * The e-mail address of the administrator to notify of the error. The value is available to your
	 * custom error page in the MailTo property of the error object.
	 */
	private String mailto = "";

	private short type;

	@Override
	public void setMailto(String mailto) {
		this.mailto = mailto;
	}

	@Override
	public void setTemplate(PageSource template) {
		this.template = template;
	}

	@Override
	public void setTypeAsString(String exception) {
		setException(exception);
	}

	@Override
	public void setException(String exception) {
		this.exception = exception;
	}

	@Override
	public String getMailto() {
		return mailto;
	}

	@Override
	public PageSource getTemplate() {
		return template;
	}

	@Override
	public String getTypeAsString() {
		return getException();
	}

	@Override
	public String getException() {
		return exception;
	}

	@Override
	public void setType(short type) {
		this.type = type;
	}

	@Override
	public short getType() {
		return type;
	}
}
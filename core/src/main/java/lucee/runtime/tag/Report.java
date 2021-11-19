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

import lucee.runtime.exp.TagNotSupported;
import lucee.runtime.ext.tag.BodyTagImpl;

/**
 * Runs a predefined Crystal Reports report.
 *
 *
 *
 **/
public final class Report extends BodyTagImpl {

	private String template;
	private String format;
	private String name;
	private String filename;
	private String query;
	private boolean overwrite;
	private String encryption;
	private String ownerpassword;
	private String userpassword;
	private String permissions;
	private String datasource;
	private String type;
	private double timeout;
	private String password;
	private String orderby;
	private String report;
	private String username;
	private String formula;

	/**
	 * constructor for the tag class
	 * 
	 * @throws TagNotSupported
	 **/
	public Report() throws TagNotSupported {
		// TODO implement tag
		throw new TagNotSupported("report");
	}

	/**
	 * set the value password
	 * 
	 * @param password value to set
	 **/
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * set the value orderby Orders results according to your specifications.
	 * 
	 * @param orderby value to set
	 **/
	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

	/**
	 * set the value report
	 * 
	 * @param report value to set
	 **/
	public void setReport(String report) {
		this.report = report;
	}

	/**
	 * set the value username
	 * 
	 * @param username value to set
	 **/
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * set the value formula Specifies one or more named formulas. Terminate each formula specification
	 * with a semicolon.
	 * 
	 * @param formula value to set
	 **/
	public void setFormula(String formula) {
		this.formula = formula;
	}

	@Override
	public int doStartTag() {
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	@Override
	public void doInitBody() {

	}

	@Override
	public int doAfterBody() {
		return SKIP_BODY;
	}

	@Override
	public void release() {
		super.release();
		password = "";
		orderby = "";
		report = "";
		username = "";
		formula = "";

		template = "";
		format = "";
		name = "";
		filename = "";
		query = "";
		overwrite = false;
		encryption = "";
		ownerpassword = "";
		userpassword = "";
		permissions = "";
		datasource = "";
		type = "";
		timeout = 0;
	}

	public void addReportParam(ReportParamBean param) {
		// TODO Auto-generated method stub

	}
}
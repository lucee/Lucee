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

import java.sql.Connection;

import javax.servlet.jsp.JspException;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.db.DataSourceManager;
import lucee.runtime.db.DatasourceManagerImpl;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagTryCatchFinallyImpl;

/**
 * Transaction class
 */
public final class Transaction extends BodyTagTryCatchFinallyImpl {

	private static final int ACTION_NONE = 0;

	private static final int ACTION_BEGIN = 1;

	private static final int ACTION_COMMIT = 2;

	private static final int ACTION_ROLLBACK = 4;

	private static final int ACTION_SET_SAVEPOINT = 8;

	// private boolean hasBody;
	private int isolation = Connection.TRANSACTION_NONE;
	private int action = ACTION_NONE;
	private boolean innerTag = false;

	private boolean ignore = false;

	private String savepoint;

	@Override
	public void release() {
		// hasBody=false;
		isolation = Connection.TRANSACTION_NONE;
		action = ACTION_NONE;
		innerTag = false;
		ignore = false;
		savepoint = null;
		super.release();
	}

	/**
	 * @param action The action to set.
	 * @throws DatabaseException
	 */
	public void setAction(String strAction) throws DatabaseException {
		strAction = strAction.trim().toLowerCase();
		if (strAction.equals("begin")) action = ACTION_BEGIN;
		else if (strAction.equals("commit")) action = ACTION_COMMIT;
		else if (strAction.equals("rollback")) action = ACTION_ROLLBACK;
		else if (strAction.equals("setsavepoint")) action = ACTION_SET_SAVEPOINT;
		else {
			throw new DatabaseException("Attribute [action] has an invalid value, valid values are [begin,commit,setsavepoint and rollback]", null, null, null);
		}

	}

	/**
	 * @param isolation The isolation to set.
	 * @throws DatabaseException
	 */
	public void setIsolation(String isolation) throws DatabaseException {
		isolation = isolation.trim().toLowerCase();
		if (isolation.equals("read_uncommitted")) this.isolation = Connection.TRANSACTION_READ_UNCOMMITTED;
		else if (isolation.equals("read_committed")) this.isolation = Connection.TRANSACTION_READ_COMMITTED;
		else if (isolation.equals("repeatable_read")) this.isolation = Connection.TRANSACTION_REPEATABLE_READ;
		else if (isolation.equals("serializable")) this.isolation = Connection.TRANSACTION_SERIALIZABLE;
		else if (isolation.equals("none")) this.isolation = Connection.TRANSACTION_NONE;
		else throw new DatabaseException(
				"Transaction has an invalid isolation level (attribute [isolation], valid values are [read_uncommitted,read_committed,repeatable_read,serializable])", null, null,
				null);
	}

	/**
	 * @param isolation The isolation to set.
	 * @throws DatabaseException
	 */
	public void setSavepoint(String savepoint) throws DatabaseException {
		if (StringUtil.isEmpty(savepoint, true)) this.savepoint = null;
		else this.savepoint = savepoint.trim().toLowerCase();
	}

	@Override
	public int doStartTag() throws PageException {
		DataSourceManager manager = pageContext.getDataSourceManager();
		// first transaction
		if (manager.isAutoCommit()) {
			// if(!hasBody)throw new DatabaseException("transaction tag with no end Tag can only be used inside
			// a transaction tag",null,null,null);
			manager.begin(isolation);
			return EVAL_BODY_INCLUDE;
		}
		// inside transaction
		innerTag = true;
		switch (action) {
		/*
		 * nested transaction no longer throw an exception, they are simply ignored case ACTION_NONE: throw
		 * new
		 * DatabaseException("you can't have a nested transaction with no action defined",null,null,null);
		 * case ACTION_BEGIN: throw new
		 * DatabaseException("you can't start a transaction inside a transaction tag",null,null,null);
		 */
		case ACTION_NONE:
		case ACTION_BEGIN:
			ignore = true;
			break;

		case ACTION_COMMIT:
			manager.commit();
			break;
		case ACTION_ROLLBACK:
			((DatasourceManagerImpl) manager).rollback(savepoint);
			break;
		case ACTION_SET_SAVEPOINT:
			((DatasourceManagerImpl) manager).savepoint(savepoint);
			break;
		}

		return EVAL_BODY_INCLUDE;
	}

	@Override
	public void doCatch(Throwable t) throws Throwable {
		ExceptionUtil.rethrowIfNecessary(t);
		if (innerTag || ignore) throw t;

		DataSourceManager manager = pageContext.getDataSourceManager();
		try {
			manager.rollback();
		}
		catch (DatabaseException e) {
			// print.printST(e);
		}
		throw t;
	}

	/**
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {// print.out("hasBody"+hasBody);
		// this.hasBody=hasBody;
	}

	@Override
	public void doFinally() {
		if (!ignore && !innerTag) {
			pageContext.getDataSourceManager().end();
		}
		super.doFinally();
	}

	@Override
	public int doAfterBody() throws JspException {

		if (!ignore && !innerTag) {
			pageContext.getDataSourceManager().commit();
		}
		return super.doAfterBody();
	}
}

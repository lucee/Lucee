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
package lucee.runtime.type.scope.storage.clean;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.type.scope.storage.StorageScopeCleaner;
import lucee.runtime.type.scope.storage.StorageScopeEngine;
import lucee.runtime.type.scope.storage.StorageScopeListener;

public abstract class StorageScopeCleanerSupport implements StorageScopeCleaner {

	protected static final int INTERVALL_MINUTE = 60 * 1000;
	protected static final int INTERVALL_HOUR = 60 * 60 * 1000;
	protected static final int INTERVALL_DAY = 24 * 60 * 60 * 1000;

	protected StorageScopeEngine engine;
	protected int type;
	protected StorageScopeListener listener;
	private String application;
	protected String strType;
	private final int intervall;
	private long lastClean;

	public StorageScopeCleanerSupport(int type, StorageScopeListener listener, int intervall) {
		this.type = type;
		this.listener = listener;
		this.strType = VariableInterpreter.scopeInt2String(type);
		application = strType + " storage";
		this.intervall = intervall;

	}

	@Override
	public void init(StorageScopeEngine engine) {
		this.engine = engine;
	}

	@Override
	public final void clean() {
		if (lastClean + intervall < System.currentTimeMillis()) {
			// info("cleaning "+application);
			_clean();
			lastClean = System.currentTimeMillis();
			// info("next cleaning intervall in "+(intervall/1000)+" seconds");
		}
	}

	protected abstract void _clean();

	/**
	 * @return the log
	 */
	@Override
	public void info(String msg) {
		engine.getFactory().getScopeContext().info(msg);
	}

	@Override
	public void error(String msg) {
		engine.getFactory().getScopeContext().error(msg);
		engine._getLog().error(application, msg);
	}

	@Override
	public void error(Throwable t) {
		engine.getFactory().getScopeContext().error(t);
		engine._getLog().error(application, ExceptionUtil.getStacktrace(t, true));
	}
}
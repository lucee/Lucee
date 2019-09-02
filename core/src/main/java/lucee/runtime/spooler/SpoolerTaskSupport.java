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
package lucee.runtime.spooler;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public abstract class SpoolerTaskSupport implements SpoolerTaskPro {

	private static final long serialVersionUID = 2150341858025259745L;

	private long creation;
	private long lastExecution;
	private int tries = 0;
	private long nextExecution;
	private Array exceptions = new ArrayImpl();
	private boolean closed;
	private String id;
	private ExecutionPlan[] plans;

	/**
	 * Constructor of the class
	 * 
	 * @param plans
	 * @param timeOffset offset from the local time to the config time
	 */
	public SpoolerTaskSupport(ExecutionPlan[] plans, long nextExecution) {
		this.plans = plans;
		creation = System.currentTimeMillis();

		if (nextExecution > 0) this.nextExecution = nextExecution;
	}

	public SpoolerTaskSupport(ExecutionPlan[] plans) {

		this(plans, 0);
	}

	@Override
	public final String getId() {
		return id;
	}

	@Override
	public final void setId(String id) {
		this.id = id;
	}

	/**
	 * return last execution of this task
	 * 
	 * @return last execution
	 */
	@Override
	public final long lastExecution() {
		return lastExecution;
	}

	@Override
	public final void setNextExecution(long nextExecution) {
		this.nextExecution = nextExecution;
	}

	@Override
	public final long nextExecution() {
		return nextExecution;
	}

	/**
	 * returns how many tries to send are already done
	 * 
	 * @return tries
	 */
	@Override
	public final int tries() {
		return tries;
	}

	final void _execute(Config config) throws PageException {

		lastExecution = System.currentTimeMillis();
		tries++;
		try {
			execute(config);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			PageException pe = Caster.toPageException(t);
			String st = ExceptionUtil.getStacktrace(t, true);
			// config.getErrWriter().write(st+"\n");

			Struct sct = new StructImpl();
			sct.setEL(KeyConstants._message, pe.getMessage());
			sct.setEL(KeyConstants._detail, pe.getDetail());
			sct.setEL(KeyConstants._stacktrace, st);
			sct.setEL(KeyConstants._time, Caster.toLong(System.currentTimeMillis()));
			exceptions.appendEL(sct);

			throw pe;
		}
		finally {
			lastExecution = System.currentTimeMillis();
		}
	}

	/**
	 * @return the exceptions
	 */
	@Override
	public final Array getExceptions() {
		return exceptions;
	}

	@Override
	public final void setClosed(boolean closed) {
		this.closed = closed;
	}

	@Override
	public final boolean closed() {
		return closed;
	}

	/**
	 * @return the plans
	 */
	@Override
	public ExecutionPlan[] getPlans() {
		return plans;
	}

	/**
	 * @return the creation
	 */
	@Override
	public long getCreation() {
		return creation;
	}

	@Override
	public void setLastExecution(long lastExecution) {
		this.lastExecution = lastExecution;
	}

	@Override
	public SpoolerTaskListener getListener() {
		return null; // not supported
	}
}
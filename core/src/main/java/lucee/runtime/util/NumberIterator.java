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
package lucee.runtime.util;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.OpUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;

/**
 * A Number Iterator Implementation to iterate from to
 */
public final class NumberIterator {

	private int _from;
	private int _to;
	private int _current;
	private int recordcount;

	// private static int count=0;

	/**
	 * constructor of the number iterator
	 * 
	 * @param from iterate from
	 * @param to iterate to
	 * @param recordcount
	 */
	private NumberIterator(int from, int to, int recordcount) {
		// lucee.print.ln(count++);
		init(from, to, recordcount);
	}

	private NumberIterator init(int from, int to, int recordcount) {
		this._from = from;
		this._current = from;
		this._to = to;
		this.recordcount = recordcount;
		return this;
	}

	/**
	 * @return returns if there is a next value
	 */
	public boolean hasNext() {
		return _current < _to;
	}

	public boolean hasNext(boolean useRecordcount) {
		return _current < (useRecordcount ? recordcount : _to);
	}

	/**
	 * @return increment and return new value
	 */
	public int next() {
		return ++_current;
	}

	/**
	 * @return returns if there is a previous value
	 */
	public boolean hasPrevious() {
		return _current > _from;
	}

	/**
	 * @return decrement and return new value
	 */
	public int previous() {
		return --_current;
	}

	/**
	 * @return returns smallest possible value
	 */
	public int from() {
		return _from;
	}

	/**
	 * @return returns greatest value
	 */
	public int to() {
		return _to;
	}

	/**
	 * @return set value to first and return
	 */
	public int first() {
		return _current = _from;
	}

	/**
	 * @return set value to last and return thid value
	 */
	public int last() {
		return _current = _to;
	}

	/**
	 * @return returns current value
	 */
	public int current() {
		return _current;
	}

	/**
	 * sets the current position
	 * 
	 * @param current
	 */
	public void setCurrent(int current) {
		_current = current;
	}

	/**
	 * @return is after last
	 */
	public boolean isAfterLast() {
		return _current > _to;
	}

	/**
	 * @return is pointer on a valid position
	 */
	public boolean isValid() {
		return _current >= _from && _current <= _to;
	}

	public boolean isValid(int current) {
		_current = current;
		return _current >= _from && _current <= _to;
	}

	private static NumberIterator[] iterators = new NumberIterator[] { new NumberIterator(1, 1, 1), new NumberIterator(1, 1, 1), new NumberIterator(1, 1, 1),
			new NumberIterator(1, 1, 1), new NumberIterator(1, 1, 1), new NumberIterator(1, 1, 1), new NumberIterator(1, 1, 1), new NumberIterator(1, 1, 1),
			new NumberIterator(1, 1, 1), new NumberIterator(1, 1, 1) };
	private static int pointer = 0;

	/**
	 * load an iterator
	 * 
	 * @param from
	 * @param to iterate to
	 * @return NumberIterator
	 */
	private static NumberIterator _load(int from, int to) {
		return _load(from, to, to);
	}

	private static NumberIterator _load(int from, int to, int recordcount) {
		if (pointer >= iterators.length) return new NumberIterator(from, to, recordcount);
		return iterators[pointer++].init(from, to, recordcount);
	}

	/**
	 * create a Number Iterator with value from and to
	 * 
	 * @param from
	 * @param to
	 * @return NumberIterator
	 */
	public static synchronized NumberIterator load(double from, double to) {
		return _load((int) from, (int) to, (int) to);
	}

	public static synchronized NumberIterator load(int from, int to) {
		return _load(from, to, to);
	}

	/**
	 * create a Number Iterator with value from and to
	 * 
	 * @param from
	 * @param to
	 * @param max
	 * @return NumberIterator
	 */
	public static synchronized NumberIterator load(double from, double to, double max) {
		return loadMax((int) from, (int) to, (int) max);
	}

	public static synchronized NumberIterator loadMax(int from, int to, int max) {
		return _load(from, ((from + max - 1 < to) ? from + max - 1 : to), to);
	}

	public static synchronized NumberIterator loadEnd(int from, int to, int end) {
		return _load(from, ((end < to) ? end : to), to);
	}

	/**
	 * @param ni
	 * @param query
	 * @param groupName
	 * @param caseSensitive
	 * @return number iterator for group
	 * @throws PageException
	 */
	public static synchronized NumberIterator load(PageContext pc, NumberIterator ni, Query query, String groupName, boolean caseSensitive) throws PageException {
		int startIndex = query.getCurrentrow(pc.getId());

		Object startValue = query.get(KeyImpl.init(groupName));
		while (ni.hasNext(true)) {
			if (!OpUtil.equals(pc, startValue, query.getAt(groupName, ni.next()), caseSensitive)) {

				ni.previous();
				return _load(startIndex, ni.current());
			}
		}
		return _load(startIndex, ni.current());
	}

	/**
	 * @param ni Iterator to release
	 */
	public static synchronized void release(NumberIterator ni) {
		if (pointer > 0) {
			iterators[--pointer] = ni;
		}
	}

}
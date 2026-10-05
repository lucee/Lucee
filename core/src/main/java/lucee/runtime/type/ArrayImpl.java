/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
package lucee.runtime.type;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.concurrent.locks.ReentrantLock;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.op.ThreadLocalDuplication;
import lucee.runtime.type.wrap.ListAsArray;

/**
 * CFML array object implements Array,List,Objects
 *
 * A deque with lock-free validated reads and a lazy writer lock. The live elements are {@code data[ head, end )} of
 * the current {@link Holder}.
 *
 * Readers never lock: they load the holder, its {@code epoch}, its packed {@code bounds}, then the slot, and re-check
 * the epoch, retrying if it was odd or moved (a seqlock read, as StampedLock.validate). Walks are live, one validated
 * point read per step.
 *
 * Writers serialise on the lock, which unsynchronized arrays skip. Append and prepend write the slot, then
 * release-store the new bounds. Pop and shift release-store the new bounds, then tombstone the uncovered slot; a
 * reader that loaded the old bounds and lands on the tombstone retries. A write into a tombstoned slot, and every
 * {@code set}, bumps the epoch by 2 first. Middle deletes and inserts shift the shorter side in place inside an odd
 * epoch: opaque store of epoch + 1, storeStoreFence, the shift, release store of epoch + 2. Growth, clear and sort
 * publish a new holder.
 *
 * Serialization goes through the inherited {@code list} slot, which stays null in memory; see
 * {@link #streamList()} and {@link #readObject(ObjectInputStream)}.
 */
public class ArrayImpl extends ListAsArray {

	private static final long serialVersionUID = -6187994169003839005L;
	public static final int DEFAULT_CAP = 32;

	private static final Object TOMBSTONE = new Object();
	// read() result for a key outside the bounds
	private static final Object END = new Object();
	private static final Object[] EMPTY = {};
	// scan() result when it hit a tombstone
	private static final int DIRTY = -2;
	private static final int BULK_TRIES = 4;
	private static final int SPINS = 64;
	private static final int FIRST_GROWTH = 10;

	private static final class Holder {
		final Object[] data;
		// head in the high 32 bits, end in the low 32, so a reader gets a consistent pair from one load
		volatile long bounds;
		// even when stable, odd while a multi-slot shift is in progress; +2 before a write into a reused slot
		volatile int epoch;

		Holder(Object[] data, int head, int end) {
			this.data = data;
			this.bounds = pack(head, end);
		}
	}

	private static final VarHandle AA = MethodHandles.arrayElementVarHandle(Object[].class);
	private static final VarHandle BOUNDS;
	private static final VarHandle EPOCH;
	private static final VarHandle OWNER;
	private static final VarHandle LOCK;

	static {
		try {
			MethodHandles.Lookup l = MethodHandles.lookup();
			BOUNDS = l.findVarHandle(Holder.class, "bounds", long.class);
			EPOCH = l.findVarHandle(Holder.class, "epoch", int.class);
			OWNER = l.findVarHandle(ArrayImpl.class, "owner", Thread.class);
			LOCK = l.findVarHandle(ArrayImpl.class, "lock", ReentrantLock.class);
		}
		catch (ReflectiveOperationException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	// all transient: the stream carries the elements in the list slot, and the ArrayImpl slot stays field-free so an
	// older Lucee can read it
	private transient volatile Holder h;
	// lazy writer lock: a thin CAS lock on owner, inflated to a ReentrantLock the first time a second writer contends
	private transient volatile Thread owner;
	private transient int holds;
	private transient boolean viaLock;
	private transient volatile ReentrantLock lock;
	private transient boolean sync;

	public ArrayImpl() {
		this(DEFAULT_CAP, true);
	}

	public ArrayImpl(int initalCap) {
		this(initalCap, true);
	}

	public ArrayImpl(int initalCap, boolean sync) {
		super(null);
		if (initalCap < 0) throw new IllegalArgumentException("Illegal Capacity: " + initalCap);
		this.sync = sync;
		this.h = new Holder(initalCap == 0 ? EMPTY : new Object[initalCap], 0, 0);
	}

	// the builder: copies the elements straight into the holder with no lock ops, the callers may keep the array
	public ArrayImpl(Object[] objects) {
		super(null);
		this.sync = true;
		this.h = new Holder(objects.length == 0 ? new Object[DEFAULT_CAP] : Arrays.copyOf(objects, objects.length, Object[].class), 0, objects.length);
	}

	private static long pack(int head, int end) {
		return ((long) head << 32) | (end & 0xFFFFFFFFL);
	}

	private static int head(long b) {
		return (int) (b >>> 32);
	}

	private static int end(long b) {
		return (int) b;
	}

	// ---- readers ----

	// a validated point read: the element at key (negative counts from the end), or END when key is outside the
	// bounds. The out-of-range answer is validated too, so a reader mid-shift retries rather than reporting a size
	// that only existed inside the shift
	private Object read(int key) {
		while (true) {
			Holder x = h;
			int e = x.epoch;
			if ((e & 1) != 0) {
				Thread.onSpinWait();
				continue;
			}
			long b = x.bounds;
			int head = head(b), n = end(b) - head, idx;
			if (key > 0) idx = key > n ? -1 : head + key - 1;
			else idx = key == 0 || n + key < 0 ? -1 : head + n + key;
			// the slot load is an acquire, and so is the bounds load, so the epoch re-read can't float above either
			Object v = idx < 0 ? END : AA.getAcquire(x.data, idx);
			if (v != TOMBSTONE && x.epoch == e) return v;
		}
	}

	@Override
	public final Object get(PageContext pc, int key, Object defaultValue) {
		if (key <= 0) return defaultValue;
		Object v = read(key);
		if (v == END) return defaultValue;
		if (v == null) {
			if (NullSupportHelper.full(pc)) {
				return null;
			}
			return defaultValue;
		}
		return v;
	}

	@Override
	public final Object getE(PageContext pc, int key) throws PageException {
		Object v = read(key);
		if (v == END) throw new ExpressionException("Array index [" + key + "] out of range, array size is [" + size() + "]");
		if (v == null && key > 0) {
			if (NullSupportHelper.full(pc)) {
				return null;
			}
			throw new ExpressionException("Element at position [" + key + "] does not exist in list");
		}
		return v;
	}

	@Override
	public int size() {
		long b = h.bounds;
		return end(b) - head(b);
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	// the live elements [ from, to ) as a new array, both 0-based and clamped to the current size. Bulk reads copy
	// with plain loads and validate the epoch after, retrying a few times before copying under the writer lock, so
	// steady middle deletes can't starve them
	private Object[] range(int from, int to) {
		for (int tries = 0; tries < BULK_TRIES; tries++) {
			Holder x = h;
			int e = x.epoch;
			if ((e & 1) != 0) {
				Thread.onSpinWait();
				continue;
			}
			long b = x.bounds;
			int head = head(b), n = end(b) - head, lo = Math.min(from, n), len = Math.min(to, n) - lo;
			Object[] d = x.data, out = new Object[len];
			boolean clean = true;
			Object v;
			for (int i = 0; i < len; i++) {
				v = d[head + lo + i];
				// a tombstone inside a stable epoch is a concurrent pop / shift at an end; retry for a clean copy
				if (v == TOMBSTONE) {
					clean = false;
					break;
				}
				out[i] = v;
			}
			// the copy's loads are plain, so fence them before the re-read, as StampedLock.validate does
			VarHandle.acquireFence();
			if (clean && x.epoch == e) return out;
		}
		lock();
		try {
			Holder x = h;
			long b = x.bounds;
			int head = head(b), n = end(b) - head, lo = Math.min(from, n);
			return Arrays.copyOfRange(x.data, head + lo, head + Math.min(to, n));
		}
		finally {
			unlock();
		}
	}

	@Override
	public Object[] toArray() {
		return range(0, Integer.MAX_VALUE);
	}

	@Override
	public ArrayList toArrayList() {
		return new ArrayList(Arrays.asList(toArray()));
	}

	@Override
	public int[] intKeys() {
		for (int tries = 0; tries < BULK_TRIES; tries++) {
			Holder x = h;
			int e = x.epoch;
			if ((e & 1) != 0) {
				Thread.onSpinWait();
				continue;
			}
			long b = x.bounds;
			int[] keys = keys(x.data, head(b), end(b));
			VarHandle.acquireFence();
			if (keys != null && x.epoch == e) return keys;
		}
		lock();
		try {
			long b = h.bounds;
			return keys(h.data, head(b), end(b));
		}
		finally {
			unlock();
		}
	}

	// the keys of the non-null elements, or null on a tombstone
	private static int[] keys(Object[] d, int head, int end) {
		int n = end - head, c = 0;
		int[] keys = new int[n];
		Object v;
		for (int i = 0; i < n; i++) {
			v = d[head + i];
			if (v == TOMBSTONE) return null;
			if (v != null) keys[c++] = i + 1;
		}
		return c == n ? keys : Arrays.copyOf(keys, c);
	}

	@Override
	public int indexOf(Object o) {
		return find(o, false);
	}

	@Override
	public int lastIndexOf(Object o) {
		return find(o, true);
	}

	private int find(Object o, boolean last) {
		for (int tries = 0; tries < BULK_TRIES; tries++) {
			Holder x = h;
			int e = x.epoch;
			if ((e & 1) != 0) {
				Thread.onSpinWait();
				continue;
			}
			long b = x.bounds;
			int found = scan(x.data, head(b), end(b), o, last);
			if (found != DIRTY && x.epoch == e) return found;
		}
		lock();
		try {
			long b = h.bounds;
			return scan(h.data, head(b), end(b), o, last);
		}
		finally {
			unlock();
		}
	}

	// the 0-based index of o, -1 when absent, DIRTY on a tombstone
	private static int scan(Object[] d, int head, int end, Object o, boolean last) {
		Object v;
		for (int j = 0, n = end - head; j < n; j++) {
			int i = last ? end - 1 - j : head + j;
			v = AA.getAcquire(d, i);
			if (v == TOMBSTONE) return DIRTY;
			if (o == null ? v == null : o.equals(v)) return i - head;
		}
		return -1;
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}

	@Override
	public boolean containsAll(java.util.Collection c) {
		for (Object o: c) {
			if (!contains(o)) return false;
		}
		return true;
	}

	// ---- writers ----

	@Override
	public Object append(Object o) throws PageException {
		doAppend(o);
		return o;
	}

	@Override
	public Object appendEL(Object o) {
		doAppend(o);
		return o;
	}

	@Override
	public boolean add(Object o) {
		doAppend(o);
		return true;
	}

	@Override
	public Object prepend(Object o) throws PageException {
		doPrepend(o);
		return o;
	}

	@Override
	public boolean insert(int key, Object value) throws PageException {
		if (!doInsert(key, value)) throw new ExpressionException("can't insert value to array at position " + key + ", array goes from 1 to " + size());
		return true;
	}

	@Override
	public boolean addAll(int index, java.util.Collection c) {
		Object[] a = c.toArray();
		for (int i = 0; i < a.length; i++) {
			if (!doInsert(index + i + 1, a[i])) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
		}
		return a.length != 0;
	}

	@Override
	public Object removeE(int key) throws PageException {
		Object v = doRemove(key);
		if (v == END) throw new ExpressionException("can not remove Element at position [" + key + "]", "Index " + (key - 1) + " out of bounds for length " + size());
		return v;
	}

	@Override
	public Object pop() throws PageException {
		Object v = doRemove(-1);
		if (v == END) throw new ExpressionException("can not pop Element from array, array is empty");
		return v;
	}

	@Override
	public Object pop(Object defaultValue) {
		Object v = doRemove(-1);
		return v == END ? defaultValue : v;
	}

	@Override
	public Object shift() throws PageException {
		Object v = doRemove(1);
		if (v == END) throw new ExpressionException("can not pop Element from array, array is empty");
		return v;
	}

	@Override
	public Object shift(Object defaultValue) {
		Object v = doRemove(1);
		return v == END ? defaultValue : v;
	}

	@Override
	public Object setE(PageContext pc, int key, Object value) throws PageException {
		if (!doPut(key, value)) throw new ExpressionException("can not set Element at position [" + key + "]", "Index " + (key - 1) + " out of bounds for length " + size());
		return value;
	}

	@Override
	public Object setEL(int key, Object value) {
		doPut(key, value);
		return value;
	}

	@Override
	public void resize(int to) throws PageException {
		lock();
		try {
			Holder x = h;
			long b = x.bounds;
			int head = head(b), end = end(b);
			if (to > end - head) fill(x, head, end, to, null);
		}
		finally {
			unlock();
		}
	}

	@Override
	public void clear() {
		lock();
		try {
			Holder x = h;
			long b = x.bounds;
			if (end(b) == head(b)) return;
			h = new Holder(new Object[Math.min(x.data.length, DEFAULT_CAP)], 0, 0);
		}
		finally {
			unlock();
		}
	}

	private void doAppend(Object value) {
		lock();
		try {
			appendLocked(h, value);
		}
		finally {
			unlock();
		}
	}

	private void appendLocked(Holder x, Object value) {
		long b = x.bounds;
		int head = head(b), end = end(b);
		if (end == x.data.length) {
			int n = end - head;
			x = grow(x, head, end, 0, growth(x, n) - n);
			head = 0;
			end = n;
		}
		else if (x.data[end] == TOMBSTONE) EPOCH.setRelease(x, x.epoch + 2);
		AA.setRelease(x.data, end, value);
		BOUNDS.setRelease(x, pack(head, end + 1));
	}

	private void doPrepend(Object value) {
		lock();
		try {
			prependLocked(h, value);
		}
		finally {
			unlock();
		}
	}

	private void prependLocked(Holder x, Object value) {
		long b = x.bounds;
		int head = head(b), end = end(b);
		if (head == 0) {
			int n = end - head, front = slack(n);
			x = grow(x, head, end, front, x.data.length - end);
			head = front;
			end = front + n;
		}
		else if (x.data[head - 1] == TOMBSTONE) EPOCH.setRelease(x, x.epoch + 2);
		AA.setRelease(x.data, head - 1, value);
		BOUNDS.setRelease(x, pack(head - 1, end));
	}

	// false when key is outside 1 .. size + 1
	private boolean doInsert(int key, Object value) {
		lock();
		try {
			Holder x = h;
			long b = x.bounds;
			int head = head(b), end = end(b), n = end - head;
			if (key < 1 || key > n + 1) return false;
			if (key == n + 1) {
				appendLocked(x, value);
				return true;
			}
			if (key == 1) {
				prependLocked(x, value);
				return true;
			}
			// middle: shift the shorter side outward into the slack, inside an odd epoch
			Object[] d = x.data;
			int front = key - 1, back = n - front;
			boolean left = head > 0 && (front <= back || end == d.length);
			if (!left && end == d.length) {
				int cap = growth(x, n);
				Object[] nd = new Object[cap];
				System.arraycopy(d, head, nd, 0, front);
				nd[front] = value;
				System.arraycopy(d, head + front, nd, front + 1, back);
				h = new Holder(nd, 0, n + 1);
				return true;
			}
			int e = x.epoch;
			EPOCH.setOpaque(x, e + 1);
			// the odd epoch must be visible before any shifted slot, as StampedLock.tryAcquireWrite
			VarHandle.storeStoreFence();
			if (left) {
				System.arraycopy(d, head, d, head - 1, front);
				AA.setRelease(d, head - 1 + front, value);
				BOUNDS.setRelease(x, pack(head - 1, end));
			}
			else {
				int i = head + front;
				System.arraycopy(d, i, d, i + 1, back);
				AA.setRelease(d, i, value);
				BOUNDS.setRelease(x, pack(head, end + 1));
			}
			EPOCH.setRelease(x, e + 2);
			return true;
		}
		finally {
			unlock();
		}
	}

	// the removed element, or END when key is outside the bounds; -1 is the last element
	private Object doRemove(int key) {
		lock();
		try {
			Holder x = h;
			long b = x.bounds;
			int head = head(b), end = end(b), n = end - head;
			if (key == -1) key = n;
			if (key < 1 || key > n) return END;
			Object[] d = x.data;
			if (key == 1) {
				Object v = d[head];
				BOUNDS.setRelease(x, pack(head + 1, end));
				AA.setRelease(d, head, TOMBSTONE);
				return v;
			}
			if (key == n) {
				Object v = d[end - 1];
				BOUNDS.setRelease(x, pack(head, end - 1));
				AA.setRelease(d, end - 1, TOMBSTONE);
				return v;
			}
			// middle: shift the shorter side in place, as ArrayDeque.delete, inside an odd epoch
			int i = head + key - 1, e = x.epoch, front = key - 1, back = n - key;
			Object v = d[i];
			EPOCH.setOpaque(x, e + 1);
			// the odd epoch must be visible before any shifted slot, as StampedLock.tryAcquireWrite
			VarHandle.storeStoreFence();
			if (front < back) {
				System.arraycopy(d, head, d, head + 1, front);
				BOUNDS.setRelease(x, pack(head + 1, end));
				AA.setRelease(d, head, TOMBSTONE);
			}
			else {
				System.arraycopy(d, i + 1, d, i, back);
				BOUNDS.setRelease(x, pack(head, end - 1));
				AA.setRelease(d, end - 1, TOMBSTONE);
			}
			EPOCH.setRelease(x, e + 2);
			return v;
		}
		finally {
			unlock();
		}
	}

	// false when key < 1; past the end, pads with null and appends as one op
	private boolean doPut(int key, Object value) {
		if (key < 1) return false;
		lock();
		try {
			Holder x = h;
			long b = x.bounds;
			int head = head(b), end = end(b);
			if (key <= end - head) {
				EPOCH.setRelease(x, x.epoch + 2);
				AA.setRelease(x.data, head + key - 1, value);
			}
			else fill(x, head, end, key, value);
			return true;
		}
		finally {
			unlock();
		}
	}

	// grow the array to newN elements, null padded, with last in the final slot
	private void fill(Holder x, int head, int end, int newN, Object last) {
		int n = end - head;
		Object[] d = x.data;
		if (head + newN > d.length) {
			x = grow(x, head, end, 0, Math.max(growth(x, n), newN) - n);
			head = 0;
			end = n;
			d = x.data;
		}
		else {
			// a reader holding older, wider bounds would map a reused slot to a different key
			for (int i = end; i < head + newN; i++) {
				if (d[i] == TOMBSTONE) {
					EPOCH.setRelease(x, x.epoch + 2);
					break;
				}
			}
		}
		int newEnd = head + newN;
		for (int i = end; i < newEnd - 1; i++) {
			d[i] = null;
		}
		d[newEnd - 1] = last;
		BOUNDS.setRelease(x, pack(head, newEnd));
	}

	// ArrayList's 1.5x, of the capacity when the live elements fill it
	private static int growth(Holder x, int n) {
		return Math.max(x.data.length, n + slack(n));
	}

	private static int slack(int n) {
		return n == 0 ? FIRST_GROWTH : Math.max(n >> 1, 1);
	}

	// copy the live elements into a new array with the given slack at each end, publish it and return it
	private Holder grow(Holder x, int head, int end, int front, int back) {
		int n = end - head;
		Object[] nd = new Object[front + n + back];
		System.arraycopy(x.data, head, nd, front, n);
		Holder y = new Holder(nd, front, front + n);
		h = y;
		return y;
	}

	// the comparator is user code, so it runs unlocked on a private copy, published only if nothing changed meanwhile;
	// any change to the live contents moves the holder, the bounds or the epoch. On a conflict sort once more under the
	// inflated lock, so contending writers park rather than spin, instead of retrying unbounded or losing their writes
	@Override
	public void sortIt(Comparator comp) {
		if (getDimension() > 1) throw new PageRuntimeException("only 1 dimensional arrays can be sorted");
		Holder x;
		long b;
		int e, n;
		Object[] nd;
		lock();
		try {
			x = h;
			e = x.epoch;
			b = x.bounds;
			nd = liveCopy(x, b);
		}
		finally {
			unlock();
		}
		n = end(b) - head(b);
		Arrays.sort(nd, 0, n, comp);
		lock();
		try {
			if (h == x && x.bounds == b && x.epoch == e) {
				h = new Holder(nd, 0, n);
				return;
			}
		}
		finally {
			unlock();
		}
		inflate();
		lock();
		try {
			x = h;
			b = x.bounds;
			nd = liveCopy(x, b);
			n = end(b) - head(b);
			Arrays.sort(nd, 0, n, comp);
			h = new Holder(nd, 0, n);
		}
		finally {
			unlock();
		}
	}

	// the live elements at the front of a new array with the holder's capacity
	private static Object[] liveCopy(Holder x, long b) {
		int head = head(b), n = end(b) - head;
		Object[] nd = new Object[Math.max(x.data.length, n)];
		System.arraycopy(x.data, head, nd, 0, n);
		return nd;
	}

	// ---- lazy lock ----

	private void lock() {
		if (!sync) return;
		Thread t = Thread.currentThread();
		if (owner == t) {
			holds++;
			return;
		}
		if (lock == null && OWNER.compareAndSet(this, null, t)) {
			holds = 1;
			return;
		}
		slowLock(t);
	}

	private void slowLock(Thread t) {
		ReentrantLock l = lock;
		if (l == null) {
			for (int i = 0; i < SPINS; i++) {
				Thread.onSpinWait();
				if (owner == null && OWNER.compareAndSet(this, null, t)) {
					holds = 1;
					return;
				}
			}
			l = new ReentrantLock();
			if (!LOCK.compareAndSet(this, null, l)) l = lock;
		}
		// once inflated, contended writers park here instead of spinning; only fast-path writers already in flight
		// when the lock was installed can still hold the thin lock, so this spin is bounded
		l.lock();
		int spins = 0;
		while (!OWNER.compareAndSet(this, null, t)) {
			if (++spins < 1024) Thread.onSpinWait();
			else Thread.yield();
		}
		holds = 1;
		viaLock = true;
	}

	private void unlock() {
		if (!sync) return;
		if (--holds > 0) return;
		boolean rl = viaLock;
		viaLock = false;
		OWNER.setRelease(this, null);
		if (rl) lock.unlock();
	}

	private void inflate() {
		if (sync && lock == null) LOCK.compareAndSet(this, null, new ReentrantLock());
	}

	// ---- copies, iteration, serialization ----

	@Override
	public Collection duplicate(boolean deepCopy) {
		return duplicate(new ArrayImpl(), deepCopy);
	}

	// the refs are copied under the lock, the elements are duplicated outside it (LDEV-384). Null elements are
	// skipped, so trailing nulls are dropped (LDEV-1983)
	protected Collection duplicate(ArrayImpl arr, boolean deepCopy) {
		Object[] refs;
		lock();
		try {
			Holder x = h;
			long b = x.bounds;
			refs = Arrays.copyOfRange(x.data, head(b), end(b));
		}
		finally {
			unlock();
		}
		boolean inside = deepCopy ? ThreadLocalDuplication.set(this, arr) : true;
		try {
			Object v;
			for (int i = 0; i < refs.length; i++) {
				v = refs[i];
				if (v == null) continue;
				arr.setEL(i + 1, deepCopy ? Duplicator.duplicate(v, deepCopy) : v);
			}
		}
		finally {
			if (!inside) ThreadLocalDuplication.reset();
		}

		return arr;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable table = new DumpTable("array", "#52b788", "#b7e4c7", "#000000");
		table.setTitle("Array");

		int top = dp.getMaxlevel();

		if (size() > top) table.setComment("Rows: " + size() + " (showing top " + top + ")");
		else if (size() > 10 && dp.getMetainfo()) table.setComment("Rows: " + size());

		int length = size();

		for (int i = 1; i <= length; i++) {
			Object o = null;
			try {
				o = getE(i);
			}
			catch (Exception e) {
			}

			table.appendRow(1, new SimpleDumpData(i), DumpUtil.toDumpData(o, pageContext, maxlevel, dp));

			if (i == top) break;
		}

		return table;
	}

	@Override
	public Iterator iterator() {
		return new LiveIterator(0);
	}

	@Override
	public ListIterator listIterator() {
		return new LiveIterator(0);
	}

	@Override
	public ListIterator listIterator(int index) {
		if (index < 0 || index > size()) throw new IndexOutOfBoundsException("Index: " + index);
		return new LiveIterator(index);
	}

	@Override
	public Iterator<Object> valueIterator() {
		return new LiveIterator(0);
	}

	@Override
	public Iterator<Object> getIterator() {
		return new LiveIterator(0);
	}

	@Override
	public List subList(int fromIndex, int toIndex) {
		int n = size();
		if (fromIndex < 0) throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		if (toIndex > n) throw new IndexOutOfBoundsException("toIndex = " + toIndex);
		if (fromIndex > toIndex) throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		return new Slice(fromIndex, toIndex - fromIndex);
	}

	@Override
	protected List streamList() {
		ArrayList l = new ArrayList(Arrays.asList(toArray()));
		return sync ? Collections.synchronizedList(l) : l;
	}

	// the same code reads old streams and new ones: both carry the elements in the list slot
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		sync = list.getClass().getName().indexOf("Synchronized") != -1;
		Object[] d = list.toArray();
		h = new Holder(d, 0, d.length);
		list = null;
	}

	public boolean sync() {
		return sync;
	}

	/**
	 * live and index-based over the array, like AbstractList.Itr: each step is one validated point read, and a
	 * shrink reports the end rather than throwing. {@code remove()} is exact single-threaded, best effort under
	 * concurrent writers
	 */
	private final class LiveIterator implements ListIterator<Object> {

		// 0-based index of the next element
		private int cursor;
		private int lastRet = -1;
		// prefetched by hasNext
		private Object next = END;

		LiveIterator(int index) {
			this.cursor = index;
		}

		@Override
		public boolean hasNext() {
			if (next != END) return true;
			next = read(cursor + 1);
			return next != END;
		}

		@Override
		public Object next() {
			if (!hasNext()) throw new NoSuchElementException();
			Object v = next;
			next = END;
			lastRet = cursor++;
			return v;
		}

		@Override
		public boolean hasPrevious() {
			return cursor > 0;
		}

		@Override
		public Object previous() {
			Object v = cursor > 0 ? read(cursor) : END;
			if (v == END) throw new NoSuchElementException();
			next = END;
			lastRet = --cursor;
			return v;
		}

		@Override
		public int nextIndex() {
			return cursor;
		}

		@Override
		public int previousIndex() {
			return cursor - 1;
		}

		@Override
		public void remove() {
			if (lastRet < 0) throw new IllegalStateException();
			doRemove(lastRet + 1);
			if (lastRet < cursor) cursor--;
			lastRet = -1;
			next = END;
		}

		@Override
		public void set(Object o) {
			if (lastRet < 0) throw new IllegalStateException();
			doPut(lastRet + 1, o);
		}

		@Override
		public void add(Object o) {
			if (!doInsert(cursor + 1, o)) throw new IndexOutOfBoundsException("Index: " + cursor + ", Size: " + size());
			cursor++;
			lastRet = -1;
			next = END;
		}
	}

	// a live O(slice) view of [ from, from + size ), for ArraySlice (LDEV-3958); toArray copies
	private final class Slice extends AbstractList<Object> implements RandomAccess {

		private final int from;
		private final int size;

		Slice(int from, int size) {
			this.from = from;
			this.size = size;
		}

		@Override
		public Object get(int index) {
			java.util.Objects.checkIndex(index, size);
			Object v = read(from + index + 1);
			if (v == END) throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + size);
			return v;
		}

		@Override
		public Object set(int index, Object element) {
			Object old = get(index);
			doPut(from + index + 1, element);
			return old;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public Object[] toArray() {
			return range(from, from + size);
		}
	}
}
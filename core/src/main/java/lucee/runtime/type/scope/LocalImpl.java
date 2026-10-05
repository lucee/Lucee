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
package lucee.runtime.type.scope;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.commons.io.SystemUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

public final class LocalImpl extends ScopeSupport implements Scope, Local {

	private static final long serialVersionUID = -7155406303949924403L;
	private boolean bind;
	private static int localInitialCapacity;
	private static final boolean USE_ARRAY_OPTIMIZATION;

	// Lightweight storage - simple Object[] array
	// Format: [key1, value1, key2, value2, ...]
	private boolean useHashMap = false;
	private Object[] localArray;
	private int localCount = 0;

	static {
		localInitialCapacity = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.scope.local.capacity", "16"), 16);
		USE_ARRAY_OPTIMIZATION = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.scope.local.array", "true"), true);
	}

	public LocalImpl() {
		// super("local", Scope.SCOPE_LOCAL, Struct.TYPE_SYNC, 4);
		super("local", Scope.SCOPE_LOCAL, Struct.TYPE_REGULAR, localInitialCapacity);
		if ( USE_ARRAY_OPTIMIZATION ) {
			localArray = new Object[localInitialCapacity * 2];
		}
		else {
			useHashMap = true;
		}
	}

	/**
	 * Ensures array has capacity for at least minCapacity key-value pairs
	 */
	private void ensureCapacity( int minCapacity ) {
		if ( localArray == null ) return;
		int currentCapacity = localArray.length / 2;
		if ( minCapacity > currentCapacity ) {
			int newCapacity = Math.max( minCapacity, currentCapacity * 2 );
			Object[] newArray = new Object[newCapacity * 2];
			System.arraycopy( localArray, 0, newArray, 0, localCount * 2 );
			localArray = newArray;
		}
	}

	/**
	 * Converts from lightweight array storage to HashMap storage
	 */
	private void convertToHashMap() {
		if ( useHashMap ) return;

		// Migrate data from array to parent HashMap
		for ( int i = 0; i < localCount; i++ ) {
			super.setEL( (Collection.Key) localArray[i * 2], localArray[i * 2 + 1] );
		}

		// Clear array and switch mode
		localArray = null;
		localCount = 0;
		useHashMap = true;
	}

	@Override
	public void release(PageContext pc) {
		// Clear our array storage
		if ( !useHashMap ) {
			localCount = 0;
			// Null out array for GC
			if ( localArray != null ) {
				for ( int i = 0; i < localArray.length; i++ ) {
					localArray[i] = null;
				}
			}
		}
		// Reset to array mode for next use
		if ( USE_ARRAY_OPTIMIZATION ) {
			useHashMap = false;
			if ( localArray == null ) {
				localArray = new Object[localInitialCapacity * 2];
			}
		}
		// Let parent handle isInit and HashMap clearing
		super.release(pc);
	}

	@Override
	public boolean isBind() {
		return bind;
	}

	@Override
	public void setBind(boolean bind) {
		if (bind) {
			convertToHashMap();
			makeSynchronized();
		}
		this.bind = bind;
	}

	// Override key methods to use array storage
	@Override
	public Object g(Collection.Key key, Object defaultValue) {
		if ( !useHashMap ) {
			for ( int i = 0; i < localCount; i++ ) {
				if ( localArray[i * 2].equals( key ) ) {
					return localArray[i * 2 + 1];
				}
			}
			return defaultValue;
		}
		return super.g( key, defaultValue );
	}

	@Override
	public Object g(Collection.Key key) throws PageException {
		if ( !useHashMap ) {
			for ( int i = 0; i < localCount; i++ ) {
				if ( localArray[i * 2].equals( key ) ) {
					return localArray[i * 2 + 1];
				}
			}
			throw new ExpressionException("The key [" + key.getString() + "] doesn't exist in the local scope.");
		}
		return super.g( key );
	}

	@Override
	public Object get(Collection.Key key, Object defaultValue) {
		if ( !useHashMap ) {
			PageContext pc = ThreadLocalPageContext.get();
			for ( int i = 0; i < localCount; i++ ) {
				if ( localArray[i * 2].equals( key ) ) {
					Object val = localArray[i * 2 + 1];
					if ( val == null && !NullSupportHelper.full( pc ) ) return defaultValue;
					return val;
				}
			}
			return defaultValue;
		}
		return super.get( key, defaultValue );
	}

	@Override
	public Object get(Collection.Key key) throws PageException {
		if ( !useHashMap ) {
			PageContext pc = ThreadLocalPageContext.get();
			for ( int i = 0; i < localCount; i++ ) {
				if ( localArray[i * 2].equals( key ) ) {
					Object val = localArray[i * 2 + 1];
					if ( val == null && !NullSupportHelper.full( pc ) ) {
						throw new ExpressionException("The key [" + key.getString() + "] doesn't exist in the local scope.");
					}
					return val;
				}
			}
			throw new ExpressionException("The key [" + key.getString() + "] doesn't exist in the local scope.");
		}
		return super.get( key );
	}

	@Override
	public Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		if ( !useHashMap ) {
			for ( int i = 0; i < localCount; i++ ) {
				if ( localArray[i * 2].equals( key ) ) {
					Object val = localArray[i * 2 + 1];
					if ( val == null && !NullSupportHelper.full( pc ) ) return defaultValue;
					return val;
				}
			}
			return defaultValue;
		}
		return super.get( pc, key, defaultValue );
	}

	@Override
	public Object get(PageContext pc, Collection.Key key) throws PageException {
		if ( !useHashMap ) {
			for ( int i = 0; i < localCount; i++ ) {
				if ( localArray[i * 2].equals( key ) ) {
					Object val = localArray[i * 2 + 1];
					if ( val == null && !NullSupportHelper.full( pc ) ) {
						throw new ExpressionException("The key [" + key.getString() + "] doesn't exist in the local scope.");
					}
					return val;
				}
			}
			throw new ExpressionException("The key [" + key.getString() + "] doesn't exist in the local scope.");
		}
		return super.get( pc, key );
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		if ( !useHashMap ) {
			// Check if key exists, update it
			for ( int i = 0; i < localCount; i++ ) {
				if ( localArray[i * 2].equals( key ) ) {
					localArray[i * 2 + 1] = value;
					return value;
				}
			}
			// Key doesn't exist, add it
			ensureCapacity( localCount + 1 );
			localArray[localCount * 2] = key;
			localArray[localCount * 2 + 1] = value;
			localCount++;
			return value;
		}
		return super.set( key, value );
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		if ( !useHashMap ) {
			// Check if key exists, update it
			for ( int i = 0; i < localCount; i++ ) {
				if ( localArray[i * 2].equals( key ) ) {
					localArray[i * 2 + 1] = value;
					return value;
				}
			}
			// Key doesn't exist, add it
			ensureCapacity( localCount + 1 );
			localArray[localCount * 2] = key;
			localArray[localCount * 2 + 1] = value;
			localCount++;
			return value;
		}
		return super.setEL( key, value );
	}

	@Override
	public int size() {
		if ( !useHashMap ) {
			return localCount;
		}
		return super.size();
	}

	@Override
	public Collection.Key[] keys() {
		if ( !useHashMap ) {
			Collection.Key[] result = new Collection.Key[localCount];
			for ( int i = 0; i < localCount; i++ ) {
				result[i] = (Collection.Key) localArray[i * 2];
			}
			return result;
		}
		return super.keys();
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		if ( !useHashMap ) {
			List<Collection.Key> keyList = new ArrayList<Collection.Key>( localCount );
			for ( int i = 0; i < localCount; i++ ) {
				keyList.add( (Collection.Key) localArray[i * 2] );
			}
			return keyList.iterator();
		}
		return super.keyIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		if ( !useHashMap ) {
			List<Object> valueList = new ArrayList<Object>( localCount );
			for ( int i = 0; i < localCount; i++ ) {
				valueList.add( localArray[i * 2 + 1] );
			}
			return valueList.iterator();
		}
		return super.valueIterator();
	}

	@Override
	public Iterator<Map.Entry<Collection.Key, Object>> entryIterator() {
		if ( !useHashMap ) {
			List<Map.Entry<Collection.Key, Object>> entryList = new ArrayList<Map.Entry<Collection.Key, Object>>( localCount );
			for ( int i = 0; i < localCount; i++ ) {
				final Collection.Key k = (Collection.Key) localArray[i * 2];
				final Object v = localArray[i * 2 + 1];
				entryList.add( new Map.Entry<Collection.Key, Object>() {
					@Override
					public Collection.Key getKey() {
						return k;
					}

					@Override
					public Object getValue() {
						return v;
					}

					@Override
					public Object setValue( Object value ) {
						throw new UnsupportedOperationException();
					}
				} );
			}
			return entryList.iterator();
		}
		return super.entryIterator();
	}

	@Override
	public boolean containsKey(Collection.Key key) {
		if ( !useHashMap ) {
			for ( int i = 0; i < localCount; i++ ) {
				if ( localArray[i * 2].equals( key ) ) {
					return true;
				}
			}
			return false;
		}
		return super.containsKey( key );
	}

	@Override
	public boolean containsKey(PageContext pc, Collection.Key key) {
		if ( !useHashMap ) {
			for ( int i = 0; i < localCount; i++ ) {
				if ( localArray[i * 2].equals( key ) ) {
					return true;
				}
			}
			return false;
		}
		return super.containsKey( pc, key );
	}

	@Override
	public Object remove(Collection.Key key) throws PageException {
		if ( !useHashMap ) {
			for ( int i = 0; i < localCount; i++ ) {
				if ( localArray[i * 2].equals( key ) ) {
					Object removed = localArray[i * 2 + 1];
					// Shift remaining elements left
					System.arraycopy( localArray, (i + 1) * 2, localArray, i * 2, (localCount - i - 1) * 2 );
					localCount--;
					// Clear last elements
					localArray[localCount * 2] = null;
					localArray[localCount * 2 + 1] = null;
					return removed;
				}
			}
			throw new ExpressionException("can't remove key [" + key.getString() + "], key doesn't exist");
		}
		return super.remove( key );
	}

	@Override
	public Object remove(Collection.Key key, Object defaultValue) {
		if ( !useHashMap ) {
			for ( int i = 0; i < localCount; i++ ) {
				if ( localArray[i * 2].equals( key ) ) {
					Object removed = localArray[i * 2 + 1];
					// Shift remaining elements left
					System.arraycopy( localArray, (i + 1) * 2, localArray, i * 2, (localCount - i - 1) * 2 );
					localCount--;
					// Clear last elements
					localArray[localCount * 2] = null;
					localArray[localCount * 2 + 1] = null;
					return removed;
				}
			}
			return defaultValue;
		}
		return super.remove( key, defaultValue );
	}

	@Override
	public Object removeEL(Collection.Key key) {
		if ( !useHashMap ) {
			for ( int i = 0; i < localCount; i++ ) {
				if ( localArray[i * 2].equals( key ) ) {
					Object removed = localArray[i * 2 + 1];
					// Shift remaining elements left
					System.arraycopy( localArray, (i + 1) * 2, localArray, i * 2, (localCount - i - 1) * 2 );
					localCount--;
					// Clear last elements
					localArray[localCount * 2] = null;
					localArray[localCount * 2 + 1] = null;
					return removed;
				}
			}
			return null;
		}
		return super.removeEL( key );
	}

	@Override
	public void clear() {
		if ( !useHashMap ) {
			localCount = 0;
			// Help GC by nulling out references
			if ( localArray != null ) {
				for ( int i = 0; i < localArray.length; i++ ) {
					localArray[i] = null;
				}
			}
			return;
		}
		super.clear();
	}
}
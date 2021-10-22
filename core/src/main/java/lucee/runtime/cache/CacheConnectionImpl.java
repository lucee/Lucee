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
package lucee.runtime.cache;

import java.io.IOException;
import java.util.Arrays;

import org.osgi.framework.BundleException;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.exp.CacheException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.config.Config;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class CacheConnectionImpl implements CacheConnectionPlus {

	private String name;
	private ClassDefinition<Cache> classDefinition;
	private Struct custom;
	private Cache cache;
	private boolean readOnly;
	private boolean storage;
	// private Class<Cache> clazz;

	public CacheConnectionImpl(Config config, String name, ClassDefinition<Cache> cd, Struct custom, boolean readOnly, boolean storage) {
		this.name = name;
		this.classDefinition = cd;
		this.custom = custom == null ? new StructImpl() : custom;
		this.readOnly = readOnly;
		this.storage = storage;
	}

	@Override
	public Cache getInstance(Config config) throws IOException {
		if (cache == null) {
			try {
				Class<Cache> clazz = classDefinition.getClazz();
				if (!Reflector.isInstaneOf(clazz, Cache.class, false))
					throw new CacheException("class [" + clazz.getName() + "] does not implement interface [" + Cache.class.getName() + "]");
				Object obj = ClassUtil.loadInstance(clazz);
				if (obj instanceof Exception) {
					throw ExceptionUtil.toIOException((Exception) obj);
				}
				cache = (Cache) obj;

			}
			catch (BundleException be) {
				throw new PageRuntimeException(be);
			}
			try {
				cache.init(config, getName(), getCustom());
			}
			catch (IOException ioe) {
				cache = null;
				throw ioe;
			}
		}
		return cache;
	}

	@Override
	public Cache getLoadedInstance() {
		return cache;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ClassDefinition<Cache> getClassDefinition() {
		return classDefinition;
	}

	@Override
	public Struct getCustom() {
		return custom;
	}

	@Override
	public String toString() {
		return "name:" + this.name + ";" + getClassDefinition() + ";custom:" + custom + ";";
	}

	public String id() {
		StringBuilder sb = new StringBuilder().append(name.toLowerCase()).append(';').append(getClassDefinition()).append(';');
		Struct _custom = getCustom();
		Key[] keys = _custom.keys();
		Arrays.sort(keys);
		for (Key k: keys) {
			sb.append(k).append(':').append(_custom.get(k, null)).append(';');
		}
		return Caster.toString(HashUtil.create64BitHash(sb.toString()));
	}

	@Override
	public CacheConnection duplicate(Config config) throws IOException {
		return new CacheConnectionImpl(config, name, classDefinition, custom, readOnly, storage);
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public boolean isStorage() {
		return storage;
	}
}
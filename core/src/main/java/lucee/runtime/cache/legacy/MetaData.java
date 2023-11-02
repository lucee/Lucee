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
package lucee.runtime.cache.legacy;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.WildCardFilter;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.converter.JavaConverter;

public class MetaData implements Serializable {

	private static Map<String, MetaData> instances = new HashMap<String, MetaData>();

	private HashMap<String, String> data = new HashMap<String, String>();
	private Resource file;

	private MetaData(Resource file) {
		this.file = file;
		data = new HashMap<String, String>();
	}

	public MetaData(Resource file, HashMap<String, String> data) {
		this.file = file;
		this.data = data;
	}

	public static MetaData getInstance(Resource directory) {
		MetaData instance = instances.get(directory.getAbsolutePath());

		if (instance == null) {
			Resource file = directory.getRealResource("meta");
			if (file.exists()) {
				try {
					instance = new MetaData(file, (HashMap) JavaConverter.deserialize(file));
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
			if (instance == null) instance = new MetaData(file);
			instances.put(directory.getAbsolutePath(), instance);
		}
		return instance;
	}

	public void add(String name, String raw) throws IOException {
		synchronized (data) {
			data.put(name, raw);
			JavaConverter.serialize(data, file);
		}
	}

	public List<String> get(String wildcard) throws IOException {
		synchronized (data) {
			List<String> list = new ArrayList<String>();
			Iterator<Entry<String, String>> it = data.entrySet().iterator();
			WildCardFilter filter = new WildCardFilter(wildcard);
			Entry<String, String> entry;
			String value;
			while (it.hasNext()) {
				entry = it.next();
				value = entry.getValue();
				if (filter.accept(value)) {
					list.add(entry.getKey());
					it.remove();
				}
			}
			if (list.size() > 0) JavaConverter.serialize(data, file);
			return list;
		}
	}

}
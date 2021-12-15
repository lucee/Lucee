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
package lucee.runtime.tag;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.jsp.tagext.Tag;

import lucee.commons.lang.ClassUtil;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Identification;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.transformer.library.ClassDefinitionImpl;

// TODO kann man nicht auf context ebene

/**
 * Pool to Handle Tags
 */
public final class TagHandlerPool {
	private ConcurrentHashMap<String, Queue<Tag>> map = new ConcurrentHashMap<String, Queue<Tag>>();
	private ConfigWeb config;

	public TagHandlerPool(ConfigWeb config) {
		this.config = config;
	}

	/**
	 * return a tag to use from a class
	 * 
	 * @param tagClass
	 * @return Tag
	 * @throws PageException
	 */
	public Tag use(String className, String tagBundleName, String tagBundleVersion, Identification id) throws PageException {
		Queue<Tag> queue = getQueue(toId(className, tagBundleName, tagBundleVersion));
		Tag tag = queue.poll();
		if (tag != null) return tag;
		return loadTag(className, tagBundleName, tagBundleVersion, id);
	}

	private String toId(String className, String tagBundleName, String tagBundleVersion) {
		if (tagBundleName == null && tagBundleVersion == null) return className;
		if (tagBundleVersion == null) return className + ":" + tagBundleName;
		return className + ":" + tagBundleName + ":" + tagBundleVersion;
	}

	/**
	 * free a tag for reusing
	 * 
	 * @param tag
	 * @throws ExpressionException
	 */
	public void reuse(Tag tag) {
		tag.release();
		Queue<Tag> queue = getQueue(tag.getClass().getName());
		queue.add(tag);
	}

	public void reuse(Tag tag, String bundleName, String bundleVersion) {
		tag.release();
		Queue<Tag> queue = getQueue(toId(tag.getClass().getName(), bundleName, bundleVersion));
		queue.add(tag);
	}

	private Tag loadTag(String className, String tagBundleName, String tagBundleVersion, Identification id) throws PageException {
		try {
			return (Tag) ClassUtil.newInstance(new ClassDefinitionImpl(className, tagBundleName, tagBundleVersion, id).getClazz());
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private Queue<Tag> getQueue(String id) {
		Queue<Tag> queue = map.get(id);// doing get before, do avoid constructing ConcurrentLinkedQueue Object all the time
		if (queue != null) return queue;
		Queue<Tag> nq, oq;
		oq = map.putIfAbsent(id, nq = new ConcurrentLinkedQueue<Tag>());
		if (oq != null) return oq;
		return nq;

	}

	public void reset() {
		map.clear();
	}
}
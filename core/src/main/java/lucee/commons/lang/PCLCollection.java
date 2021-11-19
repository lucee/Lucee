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
package lucee.commons.lang;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.runtime.MappingImpl;
import lucee.runtime.type.util.StructUtil;

/**
 * Directory ClassLoader
 */
public final class PCLCollection {

	private final Resource directory;
	private final ClassLoader resourceCL;

	private final int maxBlockSize;
	private final MappingImpl mapping;
	private final LinkedList<PCLBlock> cfcs = new LinkedList<PCLBlock>();
	private LinkedList<PCLBlock> cfms = new LinkedList<PCLBlock>();
	private PCLBlock cfc;
	private PCLBlock cfm;
	private Map<String, PCLBlock> index = new HashMap<String, PCLBlock>();

	/**
	 * Constructor of the class
	 * 
	 * @param directory
	 * @param parent
	 * @throws IOException
	 */
	public PCLCollection(MappingImpl mapping, Resource directory, ClassLoader resourceCL, int maxBlockSize) throws IOException {
		// check directory
		if (!directory.exists()) directory.mkdirs();

		if (!directory.isDirectory()) throw new IOException("resource " + directory + " is not a directory");
		if (!directory.canRead()) throw new IOException("no access to " + directory + " directory");

		this.directory = directory;
		this.mapping = mapping;
		// this.pcl=systemCL;
		this.resourceCL = resourceCL;
		cfc = new PCLBlock(directory, resourceCL);
		cfcs.add(cfc);
		cfm = new PCLBlock(directory, resourceCL);
		cfms.add(cfm);
		this.maxBlockSize = maxBlockSize;
	}

	private PCLBlock current(boolean isCFC) {
		if ((isCFC ? cfc.count() : cfm.count()) >= maxBlockSize) {
			synchronized (isCFC ? cfcs : cfms) {
				if (isCFC) {
					cfc = new PCLBlock(directory, resourceCL);
					cfcs.add(cfc);
				}
				else {
					cfm = new PCLBlock(directory, resourceCL);
					cfms.add(cfm);
				}
			}
		}
		return isCFC ? cfc : cfm;
	}

	public synchronized Class<?> loadClass(String name, byte[] barr, boolean isCFC) {
		// if class is already loaded flush the classloader and do new classloader
		PCLBlock block = index.get(name);
		if (block != null) {

			// flush classloader when update is not possible
			mapping.clearPages(block);
			StructUtil.removeValue(index, block);
			if (isCFC) {
				cfcs.remove(block);
				if (block == cfc) cfc = new PCLBlock(directory, resourceCL);
			}
			else {
				cfms.remove(block);
				if (block == cfm) cfm = new PCLBlock(directory, resourceCL);
			}
		}

		// load class from byte array
		PCLBlock c = current(isCFC);
		index.put(name, c);
		return c.loadClass(name, barr);
	}

	/**
	 * load existing class
	 * 
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	public synchronized Class<?> loadClass(String className) throws ClassNotFoundException {
		// if class is already loaded flush the classloader and do new classloader
		PCLBlock cl = index.get(className);
		if (cl != null) {
			return cl.loadClass(className);
		}
		throw new ClassNotFoundException("class " + className + " not found");
	}

	public synchronized InputStream getResourceAsStream(String name) {
		return current(false).getResourceAsStream(name);
	}

	public long count() {
		return index.size();
	}

	/**
	 * shrink the classloader elements
	 * 
	 * @return how many page have removed from classloaders
	 */

	public synchronized int shrink(boolean force) {
		int before = index.size();

		// CFM
		int flushCFM = 0;
		while (cfms.size() > 1) {
			flush(cfms.poll());
			flushCFM++;
		}

		// CFC
		if (force && flushCFM < 2 && cfcs.size() > 1) {
			flush(oldest(cfcs));
			if (cfcs.size() > 1) flush(cfcs.poll());
		}
		// print.o("shrink("+mapping.getVirtual()+"):"+(before-index.size())+">"+force+";"+(flushCFM));
		return before - index.size();
	}

	private static PCLBlock oldest(LinkedList<PCLBlock> queue) {
		int index = NumberUtil.randomRange(0, queue.size() - 2);
		return queue.remove(index);
		// return queue.poll();
	}

	private void flush(PCLBlock cl) {
		mapping.clearPages(cl);
		StructUtil.removeValue(index, cl);
		// System.gc(); gc is in Controller call, to make sure gc is only called once
	}
}

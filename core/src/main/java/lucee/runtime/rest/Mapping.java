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
package lucee.runtime.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.commons.io.FileUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.AndResourceFilter;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.OrResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.mimetype.MimeType;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.component.ComponentLoader;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;

public class Mapping {

	private static final ResourceFilter _FILTER_CFML = new AndResourceFilter(
			new ResourceFilter[] { new ExtensionResourceFilter(Constants.getCFMLComponentExtension()), new ResourceFilter() {
				@Override
				public boolean accept(Resource res) {
					return !Constants.CFML_APPLICATION_EVENT_HANDLER.equalsIgnoreCase(res.getName());
				}
			} });

	private static final ResourceFilter _FILTER_LUCEE = new AndResourceFilter(
			new ResourceFilter[] { new ExtensionResourceFilter(Constants.getLuceeComponentExtension()), new ResourceFilter() {

				@Override
				public boolean accept(Resource res) {
					return !Constants.LUCEE_APPLICATION_EVENT_HANDLER.equalsIgnoreCase(res.getName());
				}
			} });

	private static final ResourceFilter FILTER = new OrResourceFilter(new ResourceFilter[] { _FILTER_CFML, _FILTER_LUCEE });

	private String virtual;
	private Resource physical;
	private String strPhysical;
	private boolean hidden;
	private boolean readonly;
	private boolean _default;

	private List<Source> baseSources;
	private Map<Resource, List<Source>> customSources = new HashMap<Resource, List<Source>>();

	public Mapping(Config config, String virtual, String physical, boolean hidden, boolean readonly, boolean _default) {
		if (!virtual.startsWith("/")) this.virtual = "/" + virtual;
		if (virtual.endsWith("/")) this.virtual = virtual.substring(0, virtual.length() - 1);
		else this.virtual = virtual;

		this.strPhysical = physical;
		this.hidden = hidden;
		this.readonly = readonly;
		this._default = _default;

		if (!(config instanceof ConfigWebImpl)) return;
		ConfigWebImpl cw = (ConfigWebImpl) config;

		this.physical = ConfigWebUtil.getExistingResource(cw.getServletContext(), physical, null, cw.getConfigDir(), FileUtil.TYPE_DIR, cw, true);

	}

	private List<Source> init(PageContext pc, boolean reset) throws PageException {
		if (reset) release();

		Resource[] locations = pc.getApplicationContext().getRestCFCLocations();

		// base source
		if (ArrayUtil.isEmpty(locations)) {
			if (baseSources == null && this.physical != null && this.physical.isDirectory()) {
				baseSources = _init(pc, this, this.physical);
			}
			return baseSources;
		}

		// custom sources
		List<Source> rtn = new ArrayList<Source>();
		List<Source> list;
		for (int i = 0; i < locations.length; i++) {
			list = customSources.get(locations[i]);
			if (list == null && locations[i].isDirectory()) {
				list = _init(pc, this, locations[i]);
				customSources.put(locations[i], list);
			}
			copy(list, rtn);
		}
		return rtn;
	}

	private void copy(List<Source> src, List<Source> trg) {
		if (src == null) return;
		Iterator<Source> it = src.iterator();
		while (it.hasNext()) {
			trg.add(it.next());
		}
	}

	private static ArrayList<Source> _init(PageContext pc, Mapping mapping, Resource dir) throws PageException {
		Resource[] children = dir.listResources(FILTER);
		RestSettings settings = pc.getApplicationContext().getRestSettings();
		ArrayList<Source> sources = new ArrayList<Source>();

		PageSource ps;
		Component cfc;
		Struct meta;
		String path;
		for (int i = 0; i < children.length; i++) {
			try {
				ps = pc.toPageSource(children[i], null);
				cfc = ComponentLoader.loadComponent(pc, ps, children[i].getName(), true, true);
				meta = cfc.getMetaData(pc);
				if (Caster.toBooleanValue(meta.get(KeyConstants._rest, null), false)) {
					path = Caster.toString(meta.get(KeyConstants._restPath, null), null);
					sources.add(new Source(mapping, cfc.getPageSource(), path));
				}
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if (!settings.getSkipCFCWithError()) throw Caster.toPageException(t);
			}
		}
		return sources;
	}

	public lucee.runtime.rest.Mapping duplicate(Config config, Boolean readOnly) {
		return new Mapping(config, virtual, strPhysical, hidden, readOnly == null ? this.readonly : readOnly.booleanValue(), _default);
	}

	/**
	 * @return the physical
	 */
	public Resource getPhysical() {
		return physical;
	}

	/**
	 * @return the virtual
	 */
	public String getVirtual() {
		return virtual;
	}

	public String getVirtualWithSlash() {
		return virtual + "/";
	}

	/**
	 * @return the strPhysical
	 */
	public String getStrPhysical() {
		return strPhysical;
	}

	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @return the readonly
	 */
	public boolean isReadonly() {
		return readonly;
	}

	public boolean isDefault() {
		return _default;
	}

	public Result getResult(PageContext pc, String path, Struct matrix, int format, boolean hasFormatExtension, List<MimeType> accept, MimeType contentType, Result defaultValue)
			throws PageException {
		List<Source> sources = init(pc, false);
		Iterator<Source> it = sources.iterator();
		Source src;
		String[] arrPath, subPath;
		int index;
		while (it.hasNext()) {
			src = it.next();
			Struct variables = new StructImpl();
			arrPath = RestUtil.splitPath(path);
			index = RestUtil.matchPath(variables, src.getPath(), arrPath);
			if (index != -1) {
				subPath = new String[(arrPath.length - 1) - index];
				System.arraycopy(arrPath, index + 1, subPath, 0, subPath.length);
				return new Result(src, variables, subPath, matrix, format, hasFormatExtension, accept, contentType);
			}
		}

		return defaultValue;
	}

	public void setDefault(boolean _default) {
		this._default = _default;
	}

	public void reset(PageContext pc) throws PageException {
		init(pc, true);
	}

	public synchronized void release() {
		if (baseSources != null) {
			baseSources.clear();
			baseSources = null;
		}
		customSources.clear();
	}
}
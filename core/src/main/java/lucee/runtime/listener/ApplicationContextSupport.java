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
package lucee.runtime.listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.db.DataSource;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ArrayUtil;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.library.tag.TagLibTagAttr;

public abstract class ApplicationContextSupport implements ApplicationContext {

	private static final long serialVersionUID = 1384678713928757744L;
	
	protected int idletimeout=1800;
	protected String cookiedomain;
	protected String applicationtoken;
	
	private Map<Collection.Key,Map<Collection.Key,Object>> tagDefaultAttributeValues=null;
	protected Map<Integer,Object> cachedWithins;


	protected ConfigWeb config;

	public ApplicationContextSupport(ConfigWeb config) {
		this.config=config;
		tagDefaultAttributeValues=((ConfigImpl)config).getTagDefaultAttributeValues();
		
		cachedWithins=new HashMap<Integer, Object>();
		cachedWithins.put(Config.CACHEDWITHIN_FUNCTION, config.getCachedWithin(Config.CACHEDWITHIN_FUNCTION));
		cachedWithins.put(Config.CACHEDWITHIN_INCLUDE, config.getCachedWithin(Config.CACHEDWITHIN_INCLUDE));
		cachedWithins.put(Config.CACHEDWITHIN_QUERY, config.getCachedWithin(Config.CACHEDWITHIN_QUERY));
		cachedWithins.put(Config.CACHEDWITHIN_RESOURCE, config.getCachedWithin(Config.CACHEDWITHIN_RESOURCE));
		cachedWithins.put(Config.CACHEDWITHIN_HTTP, config.getCachedWithin(Config.CACHEDWITHIN_HTTP));
		cachedWithins.put(Config.CACHEDWITHIN_FILE, config.getCachedWithin(Config.CACHEDWITHIN_FILE));
		cachedWithins.put(Config.CACHEDWITHIN_WEBSERVICE, config.getCachedWithin(Config.CACHEDWITHIN_WEBSERVICE));
		
	}
	

	protected void _duplicate(ApplicationContextSupport other) {
		idletimeout=other.idletimeout;
		cookiedomain=other.cookiedomain;
		applicationtoken=other.applicationtoken;
		
		if(other.tagDefaultAttributeValues!=null) {
			tagDefaultAttributeValues=new HashMap<Collection.Key, Map<Collection.Key,Object>>();
			Iterator<Entry<Collection.Key, Map<Collection.Key, Object>>> it = other.tagDefaultAttributeValues.entrySet().iterator();
			Entry<Collection.Key, Map<Collection.Key, Object>> e;
			Iterator<Entry<Collection.Key, Object>> iit;
			Entry<Collection.Key, Object> ee;
			Map<Collection.Key, Object> map;
			while(it.hasNext()){
				e = it.next();
				iit=e.getValue().entrySet().iterator();
				map=new HashMap<Collection.Key, Object>();
				while(iit.hasNext()){
					ee = iit.next();
					map.put(ee.getKey(), ee.getValue());
				}
				tagDefaultAttributeValues.put(e.getKey(), map);
			}
		}
		other.cachedWithins=Duplicator.duplicateMap(cachedWithins, true);
	}

	@Override
	public void setSecuritySettings(String applicationtoken, String cookiedomain, int idletimeout) {
		this.applicationtoken=applicationtoken;
		this.cookiedomain=cookiedomain;
		this.idletimeout=idletimeout;
		
	}
	
	@Override
	public String getSecurityApplicationToken() {
		if(StringUtil.isEmpty(applicationtoken,true)) return getName();
		return applicationtoken;
	}
	
	@Override
	public String getSecurityCookieDomain() {
		if(StringUtil.isEmpty(applicationtoken,true)) return null;
		return cookiedomain;
	}
	
	@Override
	public int getSecurityIdleTimeout() {
		if(idletimeout<1) return 1800;
		return idletimeout;
	}
	

	
	@Override
	public DataSource getDataSource(String dataSourceName, DataSource defaultValue) {
		if(dataSourceName==null) return defaultValue;
		
		dataSourceName=dataSourceName.trim();
		DataSource[] sources = getDataSources();
		if(!ArrayUtil.isEmpty(sources)) {
			for(int i=0;i<sources.length;i++){
				if(sources[i].getName().equalsIgnoreCase(dataSourceName))
					return sources[i];
			}
		}
		return defaultValue;
	}
	
	@Override
	public DataSource getDataSource(String dataSourceName) throws ApplicationException {
		DataSource source = getDataSource(dataSourceName,null);
		if(source==null)
			throw new ApplicationException("there is no datasource with name ["+dataSourceName+"]");
		return source;
	}
	
	@Override
	public Map<Collection.Key, Map<Collection.Key, Object>> getTagAttributeDefaultValues(PageContext pc) {
		return tagDefaultAttributeValues;
	}
	
	@Override
	public Map<Collection.Key, Object> getTagAttributeDefaultValues(PageContext pc,String fullname) {
		if(tagDefaultAttributeValues==null) return null;
		return tagDefaultAttributeValues.get(KeyImpl.init(fullname));
	}

	
	@Override
	public void setTagAttributeDefaultValues(PageContext pc,Struct sct) {
		if(tagDefaultAttributeValues==null) 
			tagDefaultAttributeValues=new HashMap<Collection.Key, Map<Collection.Key,Object>>();
		initTagDefaultAttributeValues(config, tagDefaultAttributeValues, sct, pc.getCurrentTemplateDialect());
	}
	

	public static void initTagDefaultAttributeValues(Config config,Map<Collection.Key, Map<Collection.Key, Object>> tagDefaultAttributeValues, Struct sct, int dialect) {
		if(sct.size()==0) return;
		ConfigImpl ci = ((ConfigImpl)config);
		
		// first check the core lib without namespace
		TagLib lib = ci.getCoreTagLib(dialect);
		_initTagDefaultAttributeValues(config, lib, tagDefaultAttributeValues, sct,false);
		if(sct.size()==0) return;
		
		// then all the other libs including the namespace
		TagLib[] tlds = ci.getTLDs(dialect);
		for(int i=0;i<tlds.length;i++){
			_initTagDefaultAttributeValues(config, tlds[i], tagDefaultAttributeValues, sct,true);
			if(sct.size()==0) return;
		}
	}
	
	private static void _initTagDefaultAttributeValues(Config config,TagLib lib,
			Map<Collection.Key, Map<Collection.Key, Object>> tagDefaultAttributeValues, Struct sct, boolean checkNameSpace) {
		if(sct==null) return;
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		// loop tags
		Struct attrs;
		TagLibTag tag;
		Iterator<Entry<Key, Object>> iit;
		Entry<Key, Object> e;
		Map<Collection.Key,Object> map;
		TagLibTagAttr attr;
		String name;
		while(it.hasNext()){
			e = it.next();
			attrs=Caster.toStruct(e.getValue(),null);
			if(attrs!=null){
				tag=null;
				if(checkNameSpace) {
					name=e.getKey().getLowerString();
					if(StringUtil.startsWithIgnoreCase(name, lib.getNameSpaceAndSeparator())) {
						name=name.substring(lib.getNameSpaceAndSeparator().length());
						tag = lib.getTag(name);
					}
				}
				else
					tag = lib.getTag(e.getKey().getLowerString());
				
				if(tag!=null) {
					sct.removeEL(e.getKey());
					map=new HashMap<Collection.Key, Object>();
					iit = attrs.entryIterator();
					while(iit.hasNext()){
						e = iit.next();
						map.put(KeyImpl.init(e.getKey().getLowerString()),e.getValue());
					}
					tagDefaultAttributeValues.put(KeyImpl.init(tag.getFullName()), map);
				}
			}	
		}
	}

	@Override
	public final void setCachedWithin(int type, Object value) {
		if(StringUtil.isEmpty(value)) return;
		if(cachedWithins==null)
			cachedWithins=new HashMap<Integer, Object>();
		
		if(value!=null)cachedWithins.put(type, value);
	}

	// FUTURE add to interface
	public abstract Resource getAntiSamyPolicyResource();
	public abstract void setAntiSamyPolicyResource(Resource res);
	public abstract CacheConnection getCacheConnection(String cacheName, CacheConnection defaultValue);
	public abstract void setCacheConnection(String cacheName, CacheConnection value);
}
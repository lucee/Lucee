/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.orm;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.db.DataSource;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;

public interface ORMSession {

	/**
	 * flush all elements in all sessions (for all datasources)
	 * 
	 * @param pc
	 * @throws PageException
	 */
	public void flushAll(PageContext pc) throws PageException;

	/**
	 * flush all elements in the default sessions
	 * 
	 * @param pc
	 * @throws PageException
	 */
	public void flush(PageContext pc) throws PageException;

	/**
	 * flush all elements in a specific sessions defined by datasource name
	 * 
	 * @param pc
	 * @throws PageException
	 */
	public void flush(PageContext pc, String datasource) throws PageException;

	/**
	 * delete elememt from datasource
	 * 
	 * @param pc
	 * @param obj
	 * @throws PageException
	 */
	public void delete(PageContext pc, Object obj) throws PageException;

	/**
	 * insert entity into datasource, even the entry already exist
	 * 
	 * @param pc
	 * @param obj
	 * @param forceInsert
	 * @throws PageException
	 */
	public void save(PageContext pc, Object obj, boolean forceInsert) throws PageException;

	/**
	 * Reloads data for an entity that is already loaded. This method refetches data from the database
	 * and repopulates the entity with the refreshed data.
	 * 
	 * @param obj
	 */
	public void reload(PageContext pc, Object obj) throws PageException;

	/**
	 * creates an entity matching the given name
	 * 
	 * @param entityName
	 * @return
	 */
	public Component create(PageContext pc, String entityName) throws PageException;

	/**
	 * Attaches the specified entity to the current ORM session. It copies the state of the given object
	 * onto the persistent object with the same identifier and returns the persistent object. If there
	 * is no persistent instance currently associated with the session, it is loaded. The given instance
	 * is not associated with the session. User have to use the returned object from this session.
	 * 
	 * @param pc
	 * @param obj
	 * @throws PageException
	 */
	public Component merge(PageContext pc, Object obj) throws PageException;

	/**
	 * clear all elements in the default sessions
	 * 
	 * @param pc
	 * @throws PageException
	 */
	public void clear(PageContext pc) throws PageException;

	/**
	 * clear all elements in a specific sessions defined by datasource name
	 * 
	 * @param pc
	 * @param dataSource
	 * @throws PageException
	 */
	public void clear(PageContext pc, String dataSource) throws PageException;

	/**
	 * load and return an Object that match given filter, if there is more than one Object matching the
	 * filter, only the first Object is returned
	 * 
	 * @param name
	 * @param filter
	 * @return
	 */
	public Component load(PageContext pc, String name, Struct filter) throws PageException;

	public Query toQuery(PageContext pc, Object obj, String name) throws PageException;

	/**
	 * load and return an Object that match given id, if there is more than one Object matching the id,
	 * only the first Object is returned
	 * 
	 * @param name
	 * @param id
	 */
	public Component load(PageContext pc, String name, String id) throws PageException; // FUTURE deprecate

	// public Component load(PageContext pc, String name, Object id) throws PageException; // FUTURE ADD

	/**
	 * load and return an Array of Objects matching given filter
	 * 
	 * @param name
	 * @param filter
	 * @return
	 */
	public Array loadAsArray(PageContext pc, String name, Struct filter) throws PageException;

	/**
	 * load and return an Array of Objects matching given filter
	 * 
	 * @param name
	 * @param filter
	 * @param options
	 * @return
	 */
	public Array loadAsArray(PageContext pc, String name, Struct filter, Struct options) throws PageException;

	/**
	 * @param pc
	 * @param name
	 * @param filter
	 * @param options
	 * @param order
	 * @return
	 * @throws PageException
	 */
	public Array loadAsArray(PageContext pc, String name, Struct filter, Struct options, String order) throws PageException;

	/**
	 * load and return an Array of Objects matching given id
	 * 
	 * @param name
	 * @param id
	 */
	public Array loadAsArray(PageContext pc, String name, String id) throws PageException;

	/**
	 * @param pc
	 * @param name
	 * @param id
	 * @param order
	 * @return
	 * @throws PageException
	 */
	public Array loadAsArray(PageContext pc, String name, String id, String order) throws PageException;

	/**
	 * load and return an Array of Objects matching given sampleEntity
	 * 
	 * @param pc
	 * @param obj
	 */
	public Array loadByExampleAsArray(PageContext pc, Object obj) throws PageException;

	/**
	 * load and return an Object that match given sampleEntity, if there is more than one Object matching
	 * the id, only the first Object is returned
	 * 
	 * @param pc
	 * @param obj
	 */
	public Component loadByExample(PageContext pc, Object obj) throws PageException;

	public void evictCollection(PageContext pc, String entity, String collection) throws PageException;

	public void evictCollection(PageContext pc, String entity, String collection, String id) throws PageException;

	public void evictEntity(PageContext pc, String entity) throws PageException;

	public void evictEntity(PageContext pc, String entity, String id) throws PageException;

	public void evictQueries(PageContext pc) throws PageException;

	public void evictQueries(PageContext pc, String cacheName) throws PageException;

	public void evictQueries(PageContext pc, String cacheName, String datasource) throws PageException;

	public Object executeQuery(PageContext pc, String dataSourceName, String hql, Array params, boolean unique, Struct queryOptions) throws PageException;

	public Object executeQuery(PageContext pc, String dataSourceName, String hql, Struct params, boolean unique, Struct queryOptions) throws PageException;

	/**
	 * close all elements in all sessions
	 * 
	 * @param pc
	 * @throws PageException
	 */
	public void closeAll(PageContext pc) throws PageException;

	/**
	 * close all elements in the default sessions
	 * 
	 * @param pc
	 * @throws PageException
	 */
	public void close(PageContext pc) throws PageException;

	/**
	 * close all elements in a specific sessions defined by datasource name
	 * 
	 * @param pc
	 * @param datasource
	 * @throws PageException
	 */
	public void close(PageContext pc, String datasource) throws PageException;

	/**
	 * is session valid or not
	 * 
	 * @return is session valid
	 */
	public boolean isValid(DataSource ds);

	public boolean isValid();

	/**
	 * engine from session
	 * 
	 * @return engine
	 */
	public ORMEngine getEngine();

	public Object getRawSession(String dataSourceName) throws PageException;

	public Object getRawSessionFactory(String dataSourceName) throws PageException;

	public ORMTransaction getTransaction(String dataSourceName, boolean autoManage) throws PageException;

	public String[] getEntityNames();

	public DataSource[] getDataSources();
}
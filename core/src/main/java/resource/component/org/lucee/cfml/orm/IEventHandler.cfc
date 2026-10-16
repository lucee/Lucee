/**
 * Event handler for ORM Events. This should be used as a global application wide handler that can be set in the application
 * using ormsettings.eventHandler=MyEventHandler. These events can be handled by the application to perform any pre or post
 * actions for all ORM operations.
 */
Interface
{
	/**
	 * Called before injecting property values into a newly loaded entity instance.
	 */
	public void function preLoad(any entity);

	/**
	 * Called after an entity is fully loaded.
	 */
	public void function postLoad(any entity);

	/**
	 * Called before inserting the enetity into the database.
	 */
	public void function preInsert(any entity);

	/**
	 * Called after the entity is inserted into the database.
	 */
	public void function postInsert(any entity);

	/**
	 * Called before the entity is updated in the database.
	 */
	public void function preUpdate(any entity, Struct oldData);

	/**
	 * Called after the entity is updated in the database.
	 */
	public void function postUpdate(any entity);

	/**
	 * Called before the entity is deleted from the database.
	 */
	public void function preDelete(any entity);

	/**
	 * Called after deleting an item from the datastore
	 */
	public void function postDelete(any entity);

	/*
	 * Additional ORM events supported by the Hibernate extension.
	 *
	 * These are NOT part of the interface because adding them would break every
	 * existing event handler CFC that uses "implements". If you want to handle
	 * these events, just add the methods to your CFC — the extension discovers
	 * them by name, no interface required.
	 *
	 * public void function onDelete(any entity);       - delete queued, fires before preDelete
	 * public void function onFlush(any entity);        - session flush
	 * public void function onAutoFlush(any entity);    - auto-flush
	 * public void function onClear(any entity);        - ormClearSession()
	 * public void function onDirtyCheck(any entity);   - dirty check
	 * public void function onEvict(any entity);        - ormEvictEntity()
	 */
}

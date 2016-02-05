<cfscript>	
	setting showdebugoutput="false";
	ormReload();
	entity = EntityNew("MixedComponent");
	entity.setUnitId("hello");
	entity.setEntityId("goodbye");
	entity.setEntityTypeId(7);
	EntitySave(entity);


	entity = EntityNew("MixedComponent");
	entity.setUnitId(1);
	entity.setEntityId(1);
	entity.setEntityTypeId("7");
	EntitySave(entity);

	entity = EntityNew("MixedComponent");
	entity.setUnitId(true);
	entity.setEntityId(1);
	entity.setEntityTypeId(false);
	EntitySave(entity);

	ormFlush();
</cfscript>

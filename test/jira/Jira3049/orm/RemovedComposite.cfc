<cfcomponent name="RemovedComposite"
			 entityName="RemovedComposite"
			 persistent="true"
			 table="removed_composite"
			 output="false"
			 accessors="true" >

	<cfproperty name="UnitId" column="unit_id" fieldtype="id" type="string" />
	<cfproperty name="EntityId" column="entity_id" fieldtype="id" type="string" />
	<cfproperty name="EntityTypeId" column="entity_type_id" type="numeric" />
</cfcomponent>
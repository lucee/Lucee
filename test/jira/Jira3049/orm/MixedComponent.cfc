<cfcomponent name="MixedComponent"
			 entityName="MixedComponent"
			 persistent="true"
			 table="mixed_component"
			 output="false"
			 accessors="true" >

	<cfproperty name="UnitId" column="unit_id" fieldtype="id" type="string" />
	<cfproperty name="EntityId" column="entity_id" fieldtype="id" type="string" />
	<cfproperty name="EntityTypeId" column="entity_type_id" fieldtype="id" type="numeric" />
</cfcomponent>
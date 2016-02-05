<cfcomponent name="NumericChangedToString"
			 entityName="NumericChangedToString"
			 persistent="true"
			 table="numeric_changed_to_string"
			 output="false"
			 accessors="true" >

	<cfproperty name="UnitId" column="unit_id" fieldtype="id" type="string" />
	<cfproperty name="EntityId" column="entity_id" fieldtype="id" type="string" />
	<cfproperty name="EntityTypeId" column="entity_type_id" fieldtype="id" type="string" />
</cfcomponent>
<cfcomponent persistent="true" table="module" entityname="module">

	<cfproperty name="id" hint="module id"
				fieldtype="id"
				type="numeric"
				ormtype="int"
				generator="increment">
	
	<!--- Missing fkcolumn, column gets generated 2-times in the DB --->
	<cfproperty name="modulelang" fieldtype="one-to-many" cfc="moduleLang">
	
	<!---
	 works if this property also has a matching fkcolumn - fkcoumn just 1-time in DB 
	<cfproperty name="modulelang" fieldtype="one-to-many" cfc="moduleLang" fkcolumn="moduleFK">--->

</cfcomponent>
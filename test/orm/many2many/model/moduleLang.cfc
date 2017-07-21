<cfcomponent persistent="true" table="modulelang" entityname="moduleLang">

	<cfproperty name="id" hint="moduleLang id"
				fieldtype="id"
				type="numeric"
				ormtype="int"
				generator="increment">				

	<cfproperty name="module" fieldtype="many-to-one" cfc="module" fkcolumn="moduleFK">
	<cfproperty name="pModuleLang">

	
</cfcomponent>
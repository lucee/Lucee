<cfparam name="#FORM.Scene#" default="1">
<cfset hasError = false>
<cfif FORM.Scene EQ 1>
	<cftry>
		<cfloop index="x" from="1" to="3" endRow="1"> 
			<cfset Content = "X" & #x#> 
		</cfloop> 
		<cfcatch type="any">
			<cfset hasError = true>
		</cfcatch>
	</cftry>
<cfelseif FORM.Scene EQ 2>
	<cftry>
		<cfset CountVar = 0> 
		<cfloop condition = "CountVar Greater THAN 5" delimiters=","> 
		</cfloop>
		<cfcatch type="any">
			<cfset hasError = true>
		</cfcatch>
	</cftry>
<cfelseif FORM.Scene EQ 3>
	<cftry>
		<cfset myArray = [3]>
		<cfloop array="#myArray#" index="i" startRow="1">
		</cfloop>
		<cfcatch type="any">
			<cfset hasError = true>
		</cfcatch>
	</cftry>
<cfelseif FORM.Scene EQ 4>
	<cftry>
	 	<cfset myStruct = {"name":"ggg","dept":"finance"}> 
		<cfloop collection="#myStruct#" item="k" step="2">
		</cfloop> 
		<cfcatch type="any">
			<cfset hasError = true>
		</cfcatch>
	</cftry>
<cfelseif FORM.Scene EQ 5>
	<cftry>
		<cfset myList = "lucee">
		<cfloop list="#myList#" index="j" from="1" to="3">
		</cfloop>
		<cfcatch type="any">
			<cfset hasError = true>
		</cfcatch>
	</cftry>
<cfelseif FORM.Scene EQ 6>
	<cftry>
		<cfset myQuery = queryNew("name,age,dept","varchar,integer,varchar", [['aaa',35,'CEO'],['bbb',20, 'Employee']])>
		<cfloop query="#myQuery#" item="i">
		</cfloop>
		<cfcatch type="any">
			<cfset hasError = true>
		</cfcatch>
	</cftry>
</cfif>
<cfoutput>#hasError#</cfoutput>

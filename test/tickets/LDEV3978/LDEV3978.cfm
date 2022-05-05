<cfset list="first,second">
<cfloop list=#list# item="idx">
	<cflog type="information" file="LDEV3978_1" text="testone_#idx#" />
	<cflog type="information" file="LDEV3978_2" text="testtwo_#idx#" />
	<cflog type="information" file="LDEV3978_3" text="testthree_#idx#" />
</cfloop>
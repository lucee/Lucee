<cfset res = "classic,modern">
<cfset result = "">
<cfloop list="#res#" index="x">
	<cfdocument format="PDF" fontDirectory="./myfonts/" type="#x#" filename="pdf/test_#x#.pdf" overwrite="true">
		<style>
			i.inr { font-family: ITF Rupee; }
		</style>
		<p>
			<i class="inr">T </i>
			<span>1000</span>
		</p>
	</cfdocument>
	<cfif fileexists('pdf/test_#x#.pdf')>
		<cfset result = listappend(result,'Created a pdf with type = "#x#"')>
	</cfif>
</cfloop>
<cfoutput>#result#</cfoutput>
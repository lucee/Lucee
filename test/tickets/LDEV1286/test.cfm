<cfparam name="form.scene" default="1">

<cfif form.scene EQ 1>
	<cfoutput>
		<cftry>
			<cfparam name="address1" required="true">
			#address1#
			<cfcatch type="any">
				#cfcatch.message#
			</cfcatch>
		</cftry>
	</cfoutput>
<cfelseif form.scene EQ 2>
	<cfscript>
		try{
			param name="address1";
			writeOutput(address1);
		}catch(any e){
			writeOutput(e.message);
		}
	</cfscript>
<cfelseif form.scene EQ 3>
	<cfscript>
		try{
			param name="address1" required=true;
			writeOutput(address1);
		}catch(any e){
			writeOutput(e.message);
		}
	</cfscript>
<cfelseif form.scene EQ 4>
	<cfscript>
		try{
			param name="address2" required="testStr";
			writeOutput(address2);
		}catch(any e){
			writeOutput(e.message);
		}
	</cfscript>
</cfif>
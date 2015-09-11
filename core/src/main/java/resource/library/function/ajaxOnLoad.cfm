<cffunction name="ajaxOnLoad" output="true" hint="Causes the specified JavaScript function to run when the page loads."><cfargument 
	name="functionname" required="no" hint="The name of the function to run when the page loads."/><!--- 
	
	---><cfif len(arguments.functionname)><!--- 
		
		load js lib if required 
		---><cfajaximport /><!--- 
		
		subscribe to the onload event
		 ---><cfoutput><script type="text/javascript">Lucee.Events.subscribe(#arguments.functionname#,'onLoad');</script></cfoutput></cfif><!--- 
---></cffunction>
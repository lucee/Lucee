<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cffunction name="testcfimage">
		<cfset currDir = expandPath("LDEV0392")>
		<cfif !directoryExists("#currDir#/conversions")>
			<cfdirectory action="create" directory="#currDir#/conversions">
		</cfif>
		<cfimage action="WRITE" source="#currDir#/test.png" destination="#currDir#/conversions/test.png" overwrite="yes" />
		<cfimage action="convert" source="#currDir#/conversions/test.png" destination="#currDir#/conversions/capture.jpg" overwrite="yes">
		<cfset assertEquals(true, isImageFile("#currDir#/conversions/capture.jpg"))>
	</cffunction>
</cfcomponent>
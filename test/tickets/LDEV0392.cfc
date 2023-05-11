<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="image" >
	<cffunction name="testcfimage">
		<cftry>
			<cfset currDir = getDIRECTORYFROMPATH(getCurrentTemplatePath()) & "LDEV0392">
			<cfif !directoryExists("#currDir#/conversions")>
				<cfdirectory action="create" directory="#currDir#/conversions">
			</cfif>
			<cfimage action="WRITE" source="#currDir#/test.png" destination="#currDir#/conversions/test.png" overwrite="yes" />
			<cfimage action="convert" source="#currDir#/conversions/test.png" destination="#currDir#/conversions/capture.jpg" overwrite="yes">
			<cfset assertEquals(true, isImageFile("#currDir#/conversions/capture.jpg"))>

			<cfcatch>
				<cfif directoryExists("#currDir#/conversions")>
					<cfset DirectoryDelete("#currDir#/conversions",true)>
				</cfif>
			</cfcatch>
		</cftry>
		
	</cffunction>
</cfcomponent>
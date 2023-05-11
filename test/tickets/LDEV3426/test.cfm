<cfparam name="FORM.scene" default="">
<cfparam name="FORM.onDel" default="">
<cfif FORM.scene == 1>
    <cfquery name = "createTable" datasource="LDEV_3426">
        CREATE TABLE LDEV3426_test ( id SERIAL NOT NULL PRIMARY KEY,  foreignId INT NOT NULL REFERENCES LDEV3426_Primary (id) <cfif FORM.onDel>ON DELETE CASCADE</cfif> )
    </cfquery>
    <cfoutput>Success</cfoutput>
</cfif>

<cfif FORM.scene == 2>
    <cftry>
        <cfquery name = "createTable" datasource="LDEV_3426" result="res">
            CREATE TABLE LDEV3426_test ( id SERIAL NOT NULL PRIMARY KEY,  foreignId INT NOT NULL REFERENCES LDEV3426_Primary (id) <cfif FORM.onDel>ON DELETE CASCADE</cfif> )
        </cfquery>
        <cfoutput>Success</cfoutput>
        <cfcatch>
            <cfoutput>#cfcatch.message#</cfoutput>
        </cfcatch>
    </cftry>
</cfif>
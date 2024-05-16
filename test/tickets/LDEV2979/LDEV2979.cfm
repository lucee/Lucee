<cftry>
    <cftransaction>
        <cfquery name="qqq" datasource="ldev2979_dsn">
            CREATE TABLE LDEV2979 (item_id int);
            INSERT INTO LDEV2979 (item_id) VALUES (5);
            INSERT INTO LDEV2979 (item_idtest) VALUES (5);
        </cfquery>
    </cftransaction>
    <cfoutput>"success"</cfoutput>
    <cfcatch type="any">
    <cftransaction action="rollback"/>
    <cfoutput>#cfcatch.message#</cfoutput>
    </cfcatch>
</cftry>
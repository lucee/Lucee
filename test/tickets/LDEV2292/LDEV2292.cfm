<cfoutput>
		<cfparam name="form.scene" default="1">

		<cfif form.scene eq 1>
			
			<cfscript>
				test = new Query();
				test.setDatasource("LDEV2292");
				test.setSQL("select * from LDEV2292 where id=2");                    
				test.addParam(name="id", value=2, SQLType="CF_SQL_INTEGER");
				test.addParam(name="name", value="Lucee", SQLType="CF_SQL_VARCHAR");

				res = test.execute();
				sql = test.getSQL();
				test.clearParams();
			</cfscript>
			#sql#
		</cfif>

		<cfif form.scene eq 2>
			
			<cfscript>
				test = new Query();
				test.setDatasource("LDEV2292");
				test.setSQL("select * from LDEV2292 where id=2");                    
				test.addParam(name="id", value=2, SQLType="CF_SQL_INTEGER");
				test.addParam(name="name", value="Lucee", SQLType="CF_SQL_VARCHAR");

				sql = test.getSQL();
				res = test.execute();
				test.clearParams();
			</cfscript>
			#sql#
		</cfif>
</cfoutput>
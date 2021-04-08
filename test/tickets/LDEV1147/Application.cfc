<cfcomponent>
	<cfscript>
		this.name = "testcase";
		// otherwise we get the following on travis ORA-00604: error occurred at recursive SQL level 1 / ORA-01882: timezone region not found
		tz=getTimeZone();
		//var d1=tz.getDefault();
		tz.setDefault(tz);
		//throw d1&":"&tz.getDefault();

	 	this.datasource = server.getDatasource("oracle");

		function onRequestStart(){
			setting showdebugOutput=false;
			//  create package
			query {
				echo("CREATE OR REPLACE package lucee_bug_test as PROCEDURE testproc;
						PROCEDURE testproc2(p1 varchar2);
					end;"
				);
			}
			//  create package body
			query {
		        echo("CREATE OR REPLACE package body lucee_bug_test as
					PROCEDURE testproc IS
					BEGIN
						NULL;
					END;
						procedure testproc2(p1 varchar2) is
					begin
						null;
					end;
					END;"
				);
			}
			//  create Synonym for package
			query {
					echo("create or replace synonym bu## for lucee_bug_test");
			}
		}
	</cfscript>
</cfcomponent>
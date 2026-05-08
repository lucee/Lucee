<cfcomponent>
	<cfscript>
		this.name = "testcase";
		// otherwise we get the following on travis ORA-00604: error occurred at recursive SQL level 1 / ORA-01882: timezone region not found
		tz=getTimeZone();
		//var d1=tz.getDefault();
		tz.setDefault(tz);
		//throw d1&":"&tz.getDefault();

	 	this.datasource = server.getDatasource("oracle");

		 public function onRequestStart() {
			setting requesttimeout=10 showdebugOutput=false;
			//  create package
			query {
				echo("CREATE OR REPLACE package ldev1147_pkg as PROCEDURE testproc;
						PROCEDURE testproc2(p1 varchar2);
					end;"
				);
			}
			//  create package body
			query {
				echo("CREATE OR REPLACE package body ldev1147_pkg as
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
					echo("create or replace synonym ldev1147_syn## for ldev1147_pkg");
			}
		}
	</cfscript>
</cfcomponent>
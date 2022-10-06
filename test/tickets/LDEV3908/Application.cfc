component {
	this.name = "test";
	this.datasource = server.getDatasource("mssql");

	function onRequestStart() {
		query {
			echo("drop procedure if exists spThrowException");
		}

		query {
			echo("create procedure spThrowException 
				@unit varchar(20)
				, @output varchar(max) out
				as
				begin
					if( @unit not in ('MINS', 'HRS', 'DAYS') )
					begin
						raiserror('The time type of `%s` is invalid. Must be either MINS, HRS or DAYS.', /*Severity*/ 11, /*State*/ 1, @unit);
						return -1;
					end

				set @output = 'Hello World!'

				return (0)
				end"
			);
		}
	}

	function onRequestEnd() {
		query {
			echo("drop procedure if exists spThrowException");
		}
	}

}
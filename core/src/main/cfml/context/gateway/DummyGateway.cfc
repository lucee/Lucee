<!---
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 --->
 component hint="Dummy Gateway" {
	variables.state="stopped";

	public void function init( struct config, component listener ) output=false {
	}

	public void function start() output=false {
		systemOutput( "start", true );
		try {
			variables.state = "starting";
			sleep( 1000 );
			// do something useful here
			writeOutput("...");
			variables.state="running";
		} catch (any cfcatch) {
			variables.state="failed";
			rethrow;
		}
	}

	public void function stop() output=false {
		systemOutput("stop",true);
		try {
			variables.state = "stopping";
			sleep( 1000 );
			writeOutput("...");
			variables.state = "stopped";
		} catch ( any cfcatch ) {
			variables.state = "failed";
			rethrow;
		}
	}

	public void function restart() output=false {
		systemOutput( "restart", true );
		if ( state == "running" ) {
			stop();
		}
		start();
	}

	public any function getHelper() output=false {
		systemOutput( "getHelper", true );
		return "HelperReturnData";
	}

	public string function getState() output=false {
		systemOutput( "getState", true );
		return state;
	}

	public string function sendMessage(struct data) output=false {
		systemOutput( "sendMessage:", true );
		systemOutput( "- data:"&serialize(data), true );
	}

}

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
 component hint="Mail Watcher" {
	variables.state = "stopped";
	variables.logFile = "MailWatcher";

	public void function init( string id, struct config, component listener ) output=false {
		variables.id = id;
		variables.config = config;
		variables.listener = listener;

		writeLog ( text="init", file=variables.logFile, type="information" );
	}

	public void function start() output=false localmode=true {

		while ( getState() EQ "stopping" ) {
			sleep(10);
		}
		variables.state="running";
		writeLog ( text="start", file=variables.logFile, type="information" );
		var last =now();
		var mail = "";

		while ( variables.state EQ "running" ){
			try {
				mails = getMailsNewerThan( config.server, config.port, config.username, config.password, config.attachmentpath, last);
				for ( el in mails ) {
					if ( len( trim( config.functionName ) ) ) {
						variables.listener[ config.functionName ]( el );
					}
				}
			} catch (any cfcatch) {
				writeLog ( text=cfcatch.message, file=variables.logFile, type="Error" );
			}
			last = now();
			if ( getState() != "running" ) {
				break;
			}
			sleep( variables.config.interval );
		}
	}

	public array function getMailsNewerThan( required string server, required numeric port, required string user, required string pass,
			required string attachmentpath, required date newerThan) output=true {
		var mails="";
		var arr=[];
		var sct="";
		var col = "";
		cfpop( attachmentpath=arguments.attachmentpath,
			server=arguments.server,
			generateuniquefilenames=true,
			password=arguments.pass,
			name="mails",
			port=arguments.port,
			action="getall",
			username=arguments.user );

		loop query="mails" {
			if (mails.date GTE newerThan){
				sct = {};
				loop index="col" list="#mails.columnlist#" {
					sct[col]=mails[col];
				}
				ArrayAppend( arr,sct );
			}
		}
		return arr;
	}

	public void function stop() output=false {
		writeLog ( text="stop", file="MailWatcher", type="information" );
		variables.state="stopping";
	}

	public void function restart() output=false {
		if ( variables.state == "running" ) {
			stop();
		}
		start();
	}

	public string function getState() output=false {
		return variables.state;
	}

	public string function sendMessage(struct data) output=false {
		return "sendGatewayMessage() has !been implemented for the event gateway [MailWatcher]. If you want to modify it, please edit the following CFC:"& expandpath("./") & "MailWatcher.cfc";
	}
}
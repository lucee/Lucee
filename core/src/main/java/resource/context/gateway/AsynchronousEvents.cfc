/** 
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
 */ 
component {

    this.logfile    = "AsynGateWay";

    variables.state = "stopped";


    public function init( string id, Struct component) {

        variables.id       = arguments.id;
     
        log text="Asyn CFC initialized path #arguments.config.component#" file=this.logfile;
    }


    public function start() {

        while ( variables.state == "stopping" )
            sleep( 10 );

        variables.state = "running";

        log text="Event Gateway #variables.id# started" file=this.logfile;

        while ( variables.state == "running" ) {

            try {


            }
            catch ( ex ) {

                log text="Event Gateway #variables.id# error: #ex.message#" file=this.logfile type="error";
            }

            // sleep( variables.interval );
            
        
        }

        variables.state = "stopped";
        log text="Event Gateway #variables.id# stopped" file=this.logfile;
    }


    public function stop() {

        log text="Event Gateway #variables.id# stopping" file=this.logfile;
        variables.state = "stopping";
    }


    public function restart() {

        if ( variables.state == "running" )
            this.stop();

        this.start();
    }


    public function getState() {

        return variables.state;
    }


    public function sendMessage( struct data={} ) {

        return "sendGatewayMessage() has not been implemented for the event gateway [TaskGateway]. If you want to modify it, please edit the following CFC:"& expandpath("./") & "TaskGateway.cfc";
    }


}
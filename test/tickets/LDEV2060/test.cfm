<cfscript>
    
    accessFileOne = expandpath('./testone/');
    accessFileTwo = expandpath('../../');
    
    adm = new Administrator('server', request.SERVERADMINPASSWORD?:server.SERVERADMINPASSWORD);
    try{
        adm.updatedefaultsecuritymanager(
           file: "local",
           file_access : ["#accessFileOne#","#accessFileTwo#"]
        );
        list = "#accessFileOne#,#accessFileTwo#";
        cfloop(list="#list#",index="i"){
            writeoutput(fileread(i&'1.txt'));
        }
    }
    catch(any e){
        writeoutput(e.message);
    }

</cfscript>
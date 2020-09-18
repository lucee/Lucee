<cfscript>
    adm = new Administrator('server', "password");  
    try{
        adm.updatemapping(
            virtual: '/testing',
            physical: '/test/',
            archive: '',
            primary: '',
            inspect: '',
            toplevel: ''
        );
        test = adm.getmapping(virtual:'/testing');
        writeoutput("success");
    }
    catch(any e){
        writeoutput(e.message);
    }
</cfscript>
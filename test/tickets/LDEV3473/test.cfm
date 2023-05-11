<cfparam name="FORM.scene" default="">
<cfscript>
    if (form.scene == 1) {
        function tmp() { return "from mixin method"}
        session.user = new User(); 
        session.user.mixin =  tmp;
        writeoutput(session.user.mixin());  
    }
    if (form.scene == 2) {
        try{
            writeoutput(session.user.mixin());
        }
        catch(any e) {
            writeoutput(e.message);
        }
    }
</cfscript>
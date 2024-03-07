<cfscript>
    param name="FORM.scene" default="";
    try {
        main = new testComp();
        sub = new testComp$testSub();
    
        if (form.scene == 1) {
            writeoutput("#sub.sub#,#sub.subFunc()#");
        }
        else if (form.scene == 2) {
            writeoutput(testComp$testSub::subStatic)
        }
        else if (form.scene == 3) {
            writeoutput(sub.addiFunc())
        }
    }
    catch(any e) {
        writeoutput(e.message&";main:"&structKeyList(main)&";sub:"&structKeyList(sub));
    }
</cfscript>
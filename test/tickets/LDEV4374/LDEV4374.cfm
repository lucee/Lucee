<cfscript>
    param name="FORM.scene" default="";
    try {
        mystring = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        if (form.scene == 1) {
            writeOutput(mystring[4:13]); // Returns DEFGHIJKLM
        }
        else if (form.scene == 2) {
            writeOutput(mystring[4:13:2]); // Returns DFHJL
        }
        else if (form.scene == 3) {
            writeOutput(mystring[-10:-4]); // Returns QRSTUVW
        }
        else if (form.scene == 4) {
            writeOutput(mystring[-10:-4:2]); // Returns QSUW
        }
    }
    catch(any e) {
        writeoutput(e..message);
    }
</cfscript>
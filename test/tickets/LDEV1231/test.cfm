<cfparam name="form.scene" default="1">

<cfscript>
	if(form.scene EQ 1){
		module template='./customtags/sometag.cfm' {
			writeoutput("displays before call static function/");
			a = testing::something();
			writeoutput(a);
		}
	}else if(form.scene EQ 2){
		module template='./customtags/sometag.cfm' {
			writeoutput("displays before call static function/");
			a = testing1::something();
			writeoutput(a);
		}
	}else if(form.scene EQ 3){
		writeoutput("displays before call static function/");
		a = testing::something();
		writeoutput(a);
	}else if(form.scene EQ 4){
		writeoutput("displays before call static function/");
		a = testing1::something();
		writeoutput(a);
	}

	else if(form.scene EQ 5){
		module template='./customtags/sometag2.cfc' {
			writeoutput("displays before call static function/");
			a = testing::something();
			writeoutput(a);
		}
	}else if(form.scene EQ 6){
		module template='./customtags/sometag2.cfc' {
			writeoutput("displays before call static function/");
			a = testing1::something();
			writeoutput(a);
		}
	}
</cfscript>
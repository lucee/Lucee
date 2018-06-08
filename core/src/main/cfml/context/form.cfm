<cfcontent type="text/javascript"><cfsetting showdebugoutput="no">
/**
* represent a cfform 
*/
function LuceeForms(form,onError) {
	var pub=this;
	var prv={};
	
    pub.TYPE_SELECT=-1;
    pub.TYPE_TEXT=0;
    pub.TYPE_RADIO=1;
    pub.TYPE_CHECKBOX=2;
    pub.TYPE_PASSWORD=3;
	
	pub.TYPE_BUTTON=4;
    pub.TYPE_FILE=5;
    pub.TYPE_HIDDEN=6;
    pub.TYPE_IMAGE=7;
    pub.TYPE_RESET=8;
    pub.TYPE_SUBMIT=9;
    
	
    pub.VALIDATE_DATE=4;
    pub.VALIDATE_EURODATE=5;
    pub.VALIDATE_TIME=6;
    pub.VALIDATE_FLOAT=7;
    pub.VALIDATE_INTEGER=8;
    pub.VALIDATE_TELEPHONE=9;
    pub.VALIDATE_ZIPCODE=10;
    pub.VALIDATE_CREDITCARD=11;
    pub.VALIDATE_SOCIAL_SECURITY_NUMBER=12;
    pub.VALIDATE_REGULAR_EXPRESSION=13;
    pub.VALIDATE_NONE=14;
	
	pub.VALIDATE_USDATE=15;
    pub.VALIDATE_BOOLEAN=17;
    pub.VALIDATE_EMAIL=18;
    pub.VALIDATE_URL=19;
    pub.VALIDATE_UUID=20;
    pub.VALIDATE_GUID=21;
    pub.VALIDATE_MAXLENGTH=22;
    pub.VALIDATE_NOBLANKS=23;
    pub.VALIDATE_CFC=24;
    pub.VALIDATE_RANGE=16;
	
	prv.form=form;
	prv.onError=onError;
	prv.elements={};
	prv.errors=[];
			
	/*
	* adds a input definition to the for Object
	*/
	pub.addInput=function(name,required,type,validate,pattern,message,onerror,onvalidate,rangeMin,rangeMax,maxLength,validateCFC) {
		if((rangeMin || rangeMax) && validate!=pub.VALIDATE_FLOAT && validate!=pub.VALIDATE_INTEGER)validate=pub.VALIDATE_FLOAT;
		prv.elements[name]={'maxlength':maxLength,'name':name,'required':required,'type':type,'validate':validate,'pattern':pattern,'message':message,'onerror':onerror,'onvalidate':onvalidate,'rangeMin':rangeMin,'rangeMax':rangeMax,'validateCFC':validateCFC};
	}
	
	/*
	* check data from the form
	*/
	pub.check=function() {
		for(var key in prv.elements) {
			var el=prv.elements[key];
			if(pub.TYPE_TEXT==el.type) prv.checkText(el,true);
			else if(pub.TYPE_BUTTON==el.type) prv.checkText(el,true);
			else if(pub.TYPE_FILE==el.type) prv.checkText(el,true);
			else if(pub.TYPE_HIDDEN==el.type) prv.checkText(el,true);
			else if(pub.TYPE_IMAGE==el.type) prv.checkText(el,true);
			else if(pub.TYPE_RESET==el.type) prv.checkText(el,true);
			else if(pub.TYPE_SUBMIT==el.type) prv.checkText(el,true);
			else if(pub.TYPE_PASSWORD==el.type) prv.checkText(el,false);
			else if(pub.TYPE_RADIO==el.type) prv.checkRadio(el);
			else if(pub.TYPE_CHECKBOX==el.type) prv.checkRadio(el);
			else if(pub.TYPE_SELECT==el.type) prv.checkSelect(el);		
		}
		if(prv.errors.length) {
			var _errors=[];
			var _form=document.forms[prv.form]
            for(var i=0;i<prv.errors.length;i++) {
				var err=prv.errors[i];
				var el=err.element;
				var _input=_form[el.name];				
                var v=_input.value;
                if(!v && err.value)v='';
                if(el.onerror && typeof(el.onerror) == "string" && typeof(eval(el.onerror)) == "function") {
                	var func=eval(el.onerror);
                    func(_form,_input.name,v,err.error);
				}		
                else {
                	_errors[_errors.length]={form:_form,name:_input.name,value:v,error:err.error};
                }
			}
            
            if(_errors.length>0) {
                // general on error
                if(prv.onError && typeof(prv.onError) == "string" && typeof(eval(prv.onError)) == "function") {
                	var func=eval(prv.onError);
                    func(_errors);
                }
                else {                
                    if(_errors.length==1)
                        alert(_errors[0].error);
                    else if(_errors.length>1) {
                        var msg="";
                        for(var x=0;x<_errors.length;x++) {
                            msg+="- "+_errors[x].error+"\n";
                        }
                        alert(msg);
                    }
                }
			}
			prv.errors=[];
			return false;
		}
		else return true;
	}
	
	/*
	* checks a select input field 
	* @param el Element with all data to the input field
	*/
	prv.checkSelect=function(el) {
		if(!el.required) return;
		var select=document.forms[prv.form][el.name];
		var hasSelection=false;
		for(var i=0;i<select.options.length;i++) {
			if(select.options[i].selected) {
				hasSelection=true;
				break;
			}
		}
		if(!hasSelection) {
			if(el.message && el.message.length>0) prv.addError(el,el.message);
			else prv.addError(el,"drop-down field \""+el.name+"\" is required, but no selction is made");
		}
	}
	
	/*
	* checks a text/password input field 
	* @param el Element with all data to the input field
	*/
	prv.checkText=function(el,checkValidation) {
		var hasError=false;
		var input=document.forms[prv.form][el.name];
		var value=prv.trim(input.value);
		// missing value
		if(el.required && value.length==0) {
			if(el.message && el.message.length>0) prv.addError(el,el.message);
			else prv.addError(el,"missing value for text input field \""+el.name+"\"");
			hasError=true;
		}
		if(checkValidation && !hasError)prv.validate(el,value);
	}
	
	/*
	* checks a radio input field 
	* @param el Element with all data to the input field
	*/
	prv.checkRadio=function(el) {
		var hasError=false;
		var input=document.forms[prv.form][el.name];
		if(!input.length)	{
			if(!input.checked && el.required) {
				if(el.message && el.message.length>0) prv.addError(el,el.message);
				else prv.addError(el,'radio button or checkbox ['+el.name+'] is not checked but required');
				hasError=true;
			}
			if(input.checked && !hasError) {
				prv.validate(el,input.value);
			}
		}
		else {
			var isChecked=false;
			for(var i=0;i<input.length;i++) {
				var opt=input[i];
				if(opt.checked) {
					isChecked=true;
					prv.validate(el,opt.value);
				}
			}
			if(!isChecked && el.required) {
				if(el.message && el.message.length>0) prv.addError(el,el.message);
				else prv.addError(el,'radio button or checkbox ['+el.name+'] is not checked but required');
				hasError=true;
			}
		}
	}
	
	/*
	* checks a checkbox input field 
	* @param el Element with all data to the input field
	*/
	prv.checkCheckbox=function(el) {
		
	}
	
    prv.validate=function(el,value) {
    	try{
        	prv._validate(el,value);
        }
        catch(e){
        	alert(e);
        }
    }
	
	prv._validate=function(el,value) {
		var v=el.validate;
		
        
        if(el.onvalidate) {
			if(typeof(el.onvalidate) == "string" && typeof(eval(el.onvalidate)) == "function") {
  				var func=eval(el.onvalidate);
				var f=document.forms[prv.form]
				var i=f[el.name];				
				if(func(f,i,value));
				else {
					if(el.message && el.message.length>0)prv.addError(el,el.message);
					else prv.addError(el,'value ('+value+') of field '+el.name+' has an invalid value');
					
				}
			}
			else prv.addError(el,'invalid definition of the validation function in argument onValidation, you must only define the name of the function, not a function call, example: "myValidation" not "myValidation(\'argument\')"');
		}		
        
        
		if(v==pub.VALIDATE_NONE || value.length==0)return;
		else if(v==pub.VALIDATE_DATE) 					prv.validateDate(el,value);
		else if(v==pub.VALIDATE_USDATE) 				prv.validateUSDate(el,value);
		else if(v==pub.VALIDATE_EURODATE) 				prv.validateEuroDate(el,value);
		else if(v==pub.VALIDATE_TIME) 					prv.validateTime(el,value);
		else if(v==pub.VALIDATE_BOOLEAN) 				prv.validateBoolean(el,value);
		else if(v==pub.VALIDATE_FLOAT) 					prv.validateFloat(el,value);
		else if(v==pub.VALIDATE_RANGE) 					prv.validateFloat(el,value);
		else if(v==pub.VALIDATE_INTEGER) 				prv.validateInteger(el,value);
		else if(v==pub.VALIDATE_EMAIL) 					prv.validateEmail(el,value);
		else if(v==pub.VALIDATE_URL) 					prv.validateURL(el,value);
		else if(v==pub.VALIDATE_TELEPHONE) 				prv.validateTelephone(el,value);
		else if(v==pub.VALIDATE_ZIPCODE) 				prv.validateZipCode(el,value);
		else if(v==pub.VALIDATE_GUID) 					prv.validateGUID(el,value);
		else if(v==pub.VALIDATE_UUID)	 				prv.validateUUID(el,value);
		else if(v==pub.VALIDATE_MAXLENGTH)	 			prv.validateMaxLength(el,value);
		else if(v==pub.VALIDATE_NOBLANKS)	 			prv.validateNoBlanks(el,value);
		else if(v==pub.VALIDATE_CREDITCARD) 			prv.validateCreditCard(el,value);
		else if(v==pub.VALIDATE_SOCIAL_SECURITY_NUMBER)	prv.validateSocialSecurityNumber(el,value);
		else if(v==pub.VALIDATE_REGULAR_EXPRESSION)		prv.validateRegularExpression(el,value);
		else if(v==pub.VALIDATE_CFC)					prv.validateCFC(el,value);
		
	}
	
	/*
	* check if value contains a time value or not (hh:mm:ss)
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateCFC=function(el,value) {
    	var id=el.validateCFC.id;
    	var funcName=el.validateCFC.funcName;
        var args=el.validateCFC.args;
        
        // do el lower case
        var ellc={};
        for(var key in el){
        	ellc[key.toLowerCase()]=el[key];
        }
        
        
        // populateArgs
        var _args=[];
        for(var i=0;i < args.length;i++){
        	if(args[i]=="value")
            	_args[i]=value;
            else if(ellc[args[i].toLowerCase()])
            	_args[i]=ellc[args[i].toLowerCase()];
            else {
            	try{
            		_args[i]=eval(args[i]);
                }
                catch(e){
                	_args[i]=args[i];
                }
            }
        }
        
        var clazz=eval(id);
    	var validator = new clazz(); 
    	try{
        	var answer=validator[funcName].apply(validator, Array.prototype.slice.call(_args, 0));   
            if(answer && answer!="") 
        		prv.addError(el,answer);
        }
        catch(e){
        	prv.addError(el,"error while calling remote functionn "+funcName+":"+e);
        }
        
        
        
	}
	
	/*
	* check if value contains a time value or not (hh:mm:ss)
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateTime=function(el,value) {
		var pattern=/^(\d{1,2}):(\d{1,2}):(\d{1,2})$/;
		var result=value.match(pattern);
		if(!result) {
  			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a time value');
		}
	}
	
	/*
	* check if value contains a date value or not (dd/mm/yyyy)
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateEuroDate=function(el,value)  { 
        var pattern=/^(\d{1,2})([\/\.-])(\d{1,2})([\/\.-])(\d{1,4})$/; 
		
        var result=value.match(pattern);
        if(result && result.length==6) { 
			var month=result[3]; 
			var day=result[1]; 
			var year=result[5];
			var d1=result[2];
			var d2=result[4];
			 
			var date=new Date(year,month-1,day); 
			if(d1==d2 && day==date.getDate() && month==date.getMonth()+1 && (year==date.getYear() || year==date.getFullYear())) {
				return;
			} 
        } 
		if(el.message && el.message.length>0)prv.addError(el,el.message);
		else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a euro date value'); 
	}

	/*
	* check if value contains a date value or not (mm/dd/yyyy)
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateDate=function(el,value)  { 
        var pattern=/^(\d{1,2})([\/\.-])(\d{1,2})([\/\.-])(\d{1,4})$/; 
		
        var result=value.match(pattern);
		if(result && result.length==6) { 
			var month=result[1]; 
			var day=result[3]; 
			var year=result[5];
			var d1=result[2];
			var d2=result[4];
			
			var date=new Date(year,month-1,day); 
			if(d1==d2 && day==date.getDate() && month==date.getMonth()+1 && (year==date.getYear() || year==date.getFullYear())) {
				return;
			} 
        } 
		if(el.message && el.message.length>0)prv.addError(el,el.message);
		else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a date value'); 
	} 


	/*
	* check if value contains a date value or not (mm/dd/yyyy)
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateUSDate=prv.validateDate;

	/*
	* check if value contains a boolean value or not
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateBoolean=function(el,value) {
		value=value.toLowerCase();
		var nbr=Number(value);
		if(isNaN(nbr) && value!='true' && value!='yes' && value!='false' && value!='no') {
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a boolean value');
		
		}
	}

	/*
	* check max length of a value
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateMaxLength=function(el,value) {
		if(el.maxlength!=-1 && el.maxlength<value.length) {
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' is to long');
		
		}
	}
	
	/*
	* check if only contains white spaces
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateNoBlanks=function(el,value) {
        var pattern=/^\s+$/; 
        if(pattern.test(value))	{
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' does contains only Spaces');
		} 
	}

	/*
	* check if value contains a float value or not
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateFloat=function(el,value) {
		var nbr=Number(value);
		if(isNaN(nbr)) {
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a number value');
		}
		else {
			if(el.rangeMin && el.rangeMin>nbr || el.rangeMax && el.rangeMax<nbr) {
				if(el.message && el.message.length>0)prv.addError(el,el.message);
				else prv.addError(el,'value ('+value+') of field '+el.name+' has an invalid range, valid range is from '+el.rangeMin+' to '+el.rangeMax);
			}
		}
	}
	
	/*
	* check if value contains a int value or not
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateInteger=function(el,value) {
		var nbr=Number(value);
		if(isNaN(nbr) || nbr!=parseInt(nbr)) {
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a integer');
		}
		else {
			if(el.rangeMin && el.rangeMin>nbr || el.rangeMax && el.rangeMax<nbr) {
				if(el.message && el.message.length>0)prv.addError(el,el.message);
				else prv.addError(el,'value ('+value+') of field '+el.name+' has an invalid range, valid range is from '+el.rangeMin+' to '+el.rangeMax);
			}
		}
	}
	
	/*
	* check if value match pattern
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateRegularExpression=function(el,value)  { 
		//if(!eval(el.pattern).test(value)) {
		if(!el.pattern.test(value)) {
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t match given pattern ('+el.pattern+')');
		} 
	} 
	
	
	/*
	* check if value is a valid UUID
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateUUID=function(el,value) { 
        var pattern=/^\d{8}[ -]\d{4}[ -]\d{4}[ -]\d{16}$/; 
        if(!pattern.test(value))	{
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a UUID');
		} 
	} 
	
	/*
	* check if value is a valid GUID
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateGUID=function(el,value) { 
        var pattern=/^\d{8}[ -]\d{4}[ -]\d{4}[ -]\d{4}[ -]\d{12}$/; 
        if(!pattern.test(value))	{
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a GUID');
		} 
	} 
	
	/*
	* check if value is a valid zip code (us style)
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateZipCode=function(el,value) { 
        var pattern=/^\d{5}([ -]\d{4}){0,1}$/; 
        if(!pattern.test(value))	{
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a zip code');
		} 
	}  
	
	/*
	* check if value is a valid Email address
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateEmail=function(el,value) { 
        var pattern=/^((([a-z]|[0-9]|!|#|$|%|&|'|\*|\+|\-|\/|=|\?|\^|_|`|\{|\||\}|~)+(\.([a-z]|[0-9]|!|#|$|%|&|'|\*|\+|\-|\/|=|\?|\^|_|`|\{|\||\}|~)+)*)@((((([a-z]|[0-9])([a-z]|[0-9]|\-){0,61}([a-z]|[0-9])\.))*([a-z]|[0-9])([a-z]|[0-9]|\-){0,61}([a-z]|[0-9])\.)[\w]{2,4}|(((([0-9]){1,3}\.){3}([0-9]){1,3}))|(\[((([0-9]){1,3}\.){3}([0-9]){1,3})\])))$/i;
        if(!pattern.test(value))	{
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a valid Email Address');
		} 
	}    
	
	/*
	* check if value is a valid URL 
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateURL=function(el,value) { 
        var pattern=/^(([\w]+:)?\/\/)?(([\d\w]|%[a-fA-f\d]{2,2})+(:([\d\w]|%[a-fA-f\d]{2,2})+)?@)?([\d\w][-\d\w]{0,253}[\d\w]\.)+[\w]{2,4}(:[\d]+)?(\/([-+_~.\d\w]|%[a-fA-f\d]{2,2})*)*(\?(&?([-+_~.\d\w]|%[a-fA-f\d]{2,2})=?)*)?(#([-+_~.\d\w]|%[a-fA-f\d]{2,2})*)?$/; 
        if(!pattern.test(value))	{
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a URL');
		} 
	}  
	
	/*
	* check if value is a valid phone number (us style)
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateTelephone=function(el,value) { 
        var pattern=/^(\+\d[ -\.])?\d{3}[ -\.]?\d{3}[ -\.]?\d{4}$/; 
        if(!pattern.test(value))	{
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a phone number');
		} 
	} 
	
	/*
	* check if value is a valid Social Security Number (us)
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateSocialSecurityNumber=function(el,value) { 
        var pattern=/\d{3}[- ]\d{2}[- ]\d{4}/; 
        if(!pattern.test(value))	{
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a (us) Social Security Number');
		} 
	}
	
	/*
	* check if value is a valid credit card number
	* @param el Element with all data to the input field
	* @param value value from input field
	*/
	prv.validateCreditCard=function(el,value) {
		if(!prv._validateCreditCard(value)) {
			if(el.message && el.message.length>0)prv.addError(el,el.message);
			else prv.addError(el,'value ('+value+') of field '+el.name+' doesn\'t contain a valid creditcard number');
		}
	}
	prv._validateCreditCard=function(s) {
		// remove non-numerics
		var v = "0123456789";
		var w = "";
		for (i=0; i < s.length; i++) {
			x = s.charAt(i);
			if (v.indexOf(x,0) != -1) w += x;
		}
		// validate number
		j = w.length / 2;
		if (j < 6.5 || j > 8 || j == 7) return false;
		k = Math.floor(j);
		m = Math.ceil(j) - k;
		c = 0;
		for (i=0; i<k; i++) {
			a = w.charAt(i*2+m) * 2;
			c += a > 9 ? Math.floor(a/10 + a%10) : a;
		}
		for (i=0; i<k+m; i++) c += w.charAt(i*2+1-m) * 1;
		return (c%10 == 0);
	}
	
	prv.hasError=function() {
		return prv.errors!=0;
	}
	
	prv.addError=function(el,error) {
		prv.errors[prv.errors.length]={'element':el,'error':error};
	}
	prv.trim=function(inputString) {
	   // Removes leading and trailing spaces from the passed string. Also removes
	   // consecutive spaces and replaces it with one space. If something besides
	   // a string is passed in (null, custom object, etc.) then return the input.
	   if (typeof inputString != "string") { return inputString; }
	   var retValue = inputString;
	   var ch = retValue.substring(0, 1);
	   while (ch == " ") { // Check for spaces at the beginning of the string
		  retValue = retValue.substring(1, retValue.length);
		  ch = retValue.substring(0, 1);
	   }
	   ch = retValue.substring(retValue.length-1, retValue.length);
	   while (ch == " ") { // Check for spaces at the end of the string
		  retValue = retValue.substring(0, retValue.length-1);
		  ch = retValue.substring(retValue.length-1, retValue.length);
	   }
	   while (retValue.indexOf("  ") != -1) { // Note that there are two spaces in the string - look for multiple spaces within the string
		  retValue = retValue.substring(0, retValue.indexOf("  ")) + retValue.substring(retValue.indexOf("  ")+1, retValue.length); // Again, there are two spaces in each of the strings
	   }
	   return retValue; // Return the trimmed string back to the user
	}
}
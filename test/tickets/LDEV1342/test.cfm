<cfparam name="form.scene" default="1">

<cfscript>
result = "";
try {
	if(form.scene EQ 1){
		uploadImage ="./assets/images/testImage1.jpg";
		img = imageRead(uploadImage);
		imgNew = "./assets/images/newTestImage1.jpg";
		try{
			imageWrite(img, imgNew);
			result = "success";
		}catch(any e){
			result = e.message;
		}
	}else if(form.scene EQ 2){
		uploadImage ="./assets/images/testImage2.jpg";
		img = imageRead(uploadImage);
		imgNew = "./assets/images/newTestImage2.jpg";
		try{
			imageWrite(img, imgNew);
			result = "success";
		}catch(any e){
			result = e.message;
		}
	}
	writeOutput(result);
}
finally {
	if(!isNull(imgNew) && fileExists(imgNew))fileDelete(imgNew);
}
</cfscript>
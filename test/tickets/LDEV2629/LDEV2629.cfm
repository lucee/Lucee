<cfscript>
	if(form.scene eq 1) {
		cfchart(format = "png", title = "Animal Strrngth" name="mychart") {
			cfchartseries(type = "pie") {
				cfchartdata(item = "Tiger", value = 35);
				cfchartdata(item = "Lion", value = 30);
				cfchartdata(item = "leopard", value = 20);
				cfchartdata(item = "Wolf", value = 15);
			}
		}
		cffile( output = mychart, file = expandpath("./LDEV2629.png"), action = "WRITE" );
	}
</cfscript>


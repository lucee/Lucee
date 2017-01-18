<cfscript>
	OrmReload();
	product = entityNew('entities3');
	product.setName("success");
	writeoutput(product.getName());
</cfscript>
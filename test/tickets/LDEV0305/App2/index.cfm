<cfscript>
	OrmReload();
	product = entityNew('entities2');
	product.setName("success");
	writeoutput(product.getName());
</cfscript>
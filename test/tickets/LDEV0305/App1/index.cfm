<cfscript>
	OrmReload();
	product = entityNew('entities');
	product.setName("Product");
	writeoutput(product.getName());
</cfscript>
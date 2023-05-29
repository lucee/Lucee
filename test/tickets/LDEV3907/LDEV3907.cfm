<cfset brand = new Brand() />
<cfset brand.setBrandID("") />
<cfset brand.setBrandName("LDEV3907") />
<cfset entitySave(brand) />
<cfset ormFlush() />
<cfoutput>#brand.getBrandName()#</cfoutput>
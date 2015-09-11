<cfset driver=createObject("component","dbdriver."&form.type)>
<cfset driver.setFormData(form)>

<cfset admin.updateDatasource(form.name,driver.getClass(),driver.getDSN(),driver.getUsername(),driver.getPassword())>
<cfset admin.store()>
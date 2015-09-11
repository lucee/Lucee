<cfoutput>
<form action="#action('update')#" method="post">
	<table border="0" cellpadding="0" cellspacing="0" bgcolor="##FFCC00"
		style="background-color:##FFCC00;border-style:solid;border-color:##000000;border-width:1px;padding:10px;">
	<tr>
		<td valign="top" >
			<textarea style="background-color:##FFCC00;border-style:solid;border-color:##000000;border-width:0px;" name="note" cols="40" rows="10">#req.note#</textarea>
		</td>
	</tr>
	</table>
	<br />
	<input class="button submit" type="submit" name="submit" value="#lang.btnSubmit#" />
</form>
	
</cfoutput>

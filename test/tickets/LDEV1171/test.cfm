<html>
  <body>
    <cfform  preservedata="Yes">
     <p>Please enter your name:
      <cfinput type="Text" name="UserName"><p>
       <p>
       <p>select something:
       <cfselect name="sequence">
            <option value=" "> </option>
            <option value="1">1</option>
            <option value="2">2</option>
            <option value="3">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
      </cfselect>
      <input type="Submit" name="mybutton">
    </cfform>
  </body>
</html>
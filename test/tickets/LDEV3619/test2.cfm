<cfscript>
    param numeric a = 1;
    param  numeric b = 2;
    param  numeric c.d = 3;
    param   /* comment */ e.f = 4;
    param  name="g" default=5;
    writeoutput(a & b & c.d & e.f & g);
</cfscript>

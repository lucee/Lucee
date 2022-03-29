component extends="org.lucee.cfml.test.LuceeTestCase"  labels="pdf"{

    function testPDFWaterMark (){
        cfdocument (format="PDF", name="local.test") {
            echo("<H1>I am a watermark test</H1>");
        }
       
        local.img =  ImageNew("", 100,100,"rgb","Red");

        pdf action = "addWaterMark"
            source ="test"
            image="#img#"
            pages="1"
            name="watermarkedPDF"
            overwrite="true"
            position="0,0" 
            rotation="45";
            
        expect( IsPDFObject( watermarkedPDF ) ).toBeTrue();
    }
}
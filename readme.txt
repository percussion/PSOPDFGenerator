Welcome to the PSO FOP Assembler. 

This package contains an Assembly Plugin that calls the Apache FOP processor.
This plugin can be used to generate Rhythmyx templates that output in various 
"print friendly" formats including PDF, RTF, Postscript and HP-PCL. 

To deploy this package, unzip it into a temporary directory and run the deploy.xml: 

ant -f deploy.xml 

You will then be able to start your Rhythmyx server and add templates to your existing
content types using the new "FopAssembler".  Setting the MIME Type of the template will 
set the output format.  For more details see the enclosed JavaDoc.

If you want the sample templates, they must be deployed explicitly with the deploySamples
target: 

ant -f deploy.xml deploySamples 

For more details on the operation of the FOP assembler see Instructions.pdf


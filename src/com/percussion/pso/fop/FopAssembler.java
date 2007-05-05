/*
 * com.percussion.pso.fop FopAssembler.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.fop;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.jexl.Expression;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.xml.sax.InputSource;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.assembly.IPSAssembler;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.assembly.impl.plugin.PSVelocityAssembler;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.util.PSDataTypeConverter;
import com.percussion.utils.jexl.PSJexlEvaluator;
import com.percussion.utils.string.PSStringUtils;

/**
 * A Rhythmyx Assembly plugin for FOP processing.  This plugin extends the
 * standard Velocity assembler.  The template produces XML in FO format, and then calls the 
 * FO renderer to produce a PDF. 
 * <p>
 * The Assembler finds the FOP Properties file in <code>&lt;RhythmyxRoot&gt;/rxconfig/Server/fop.config</code>.
 * This file contains the default settings for the FOP processor. 
 * <p>
 * Metadata can be added to the PDF file with mappings in the <code>$FOP</code> binding variable.  
 * Binding <code>$FOP.author</code> will set the author metadata, etc.  The valid bindings are:
 * <ul>
 *    <li>author</li>
 *    <li>creator</li>
 *    <li>producer</li>
 *    <li>keywords</li>
 *    <li>title</li>
 *    <li>creatinDate</li>
 *    <li>targetResolution</li>
 * </ul>
 * </p>
 * <p>
 * The FOP Assembler can used to generate any of the formats that Apache FOP supports. If the MIME type specified in
 * the template is one of those supported by FOP, it will be generated.  Otherwise, the output will be in PDF format. 
 * Note that by default the Rhythmyx Workbench does not support PDF and other such formats.  
 * These can be added to the 
 * <code>Rhythmyx/rxconfig/Workbench/mimemapwb.properties</code> file. 
 * Some additional configuration to the renderers found in <code>fop.config</code> may be 
 * necessary for formats other than PDF.   
 * <p>
 * For debugging purposes, users can preview the template adding the 
 * parameter "showxml" with any non-blank value.  This will cause the 
 * FOP Assembler to return the XML that would have otherwise been passed
 * to the Apache FO Processor.  
 * 
 * @author DavidBenua
 *
 */
public class FopAssembler extends PSVelocityAssembler implements IPSAssembler
{
 
   /** 
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(FopAssembler.class);

   private TransformerFactory xfactory;
   private FopFactory fopFactory = null;
   /**
    * 
    */
   public FopAssembler()
   {
      super();
      xfactory = TransformerFactory.newInstance(); 
      fopFactory = FopFactory.newInstance(); 
      
      try
      {
         String rxRootDir = (String)PSRhythmyxInfoLocator.getRhythmyxInfo()
            .getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY);
         String fopConfigFilename = rxRootDir + "/rxconfig/Server/fop.config";
         DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
         Configuration cfg = cfgBuilder.buildFromFile(new File(fopConfigFilename));
         fopFactory.setUserConfig(cfg);
      } catch (Exception ex)
      {
        log.error("Error Reading configuration file: " + ex.getMessage(), ex);
      } 
   }

   /**
    * @see com.percussion.services.assembly.impl.plugin.PSVelocityAssembler#init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   @Override
   public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException
   {
      super.init(def, codeRoot);
      String supportedTypes = def.getInitParameter(INIT_PARAMETER_MIME_TYPES);
      validMimeTypes = supportedTypes.split(",");
   }

   /**
    * @see com.percussion.services.assembly.impl.plugin.PSAssemblerBase#doAssembleSingle(com.percussion.services.assembly.IPSAssemblyItem)
    */
   @Override
   protected IPSAssemblyResult doAssembleSingle(IPSAssemblyItem item) throws Exception
   {
      
      IPSAssemblyResult resultItem = super.doAssembleSingle(item);
      PSAssemblyWorkItem work = (PSAssemblyWorkItem) resultItem;
      if(work.isDebug())
      {
         //debug mode is unchanged from superclass. 
         log.debug("Debug Assembly enabled"); 
         return work;
      }
      
      PSJexlEvaluator eval = new PSJexlEvaluator(work.getBindings());
      
      String mimeType = work.getMimeType(); 
      Charset charSet = PSStringUtils.getCharsetFromMimeType(mimeType); 
      String showXML = work.getParameterValue(SHOW_XML_PARAMETER, null); 
      if(StringUtils.isNotBlank(showXML))
      {
         //the user wants xml, give him xml.  
         StringBuilder xmlMime = new StringBuilder();
         xmlMime.append("text/xml;charset="); 
         xmlMime.append(charSet.displayName()); 
         work.setMimeType(xmlMime.toString()); 
         return work;
      }
      
      Reader irdr = new InputStreamReader(new ByteArrayInputStream(work.getResultData()),
            charSet);
      Source src = new SAXSource(new InputSource(irdr));
      Transformer xform = xfactory.newTransformer(); 
      
      String resMimeType = validateMimeType(mimeType);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      FOUserAgent foUserAgent = getFOUserAgent();
      configureUserAgent(foUserAgent, eval); 
      
      //Setup FOP
      Fop fop = fopFactory.newFop(resMimeType, foUserAgent, out);

      //Make sure the XSL transformation's result is piped through to FOP
      Result res = new SAXResult(fop.getDefaultHandler());

      //Start the transformation and rendering process
      xform.transform(src, res);
      
      work.setResultData(out.toByteArray()); 
      work.setMimeType(resMimeType); 
      
      return resultItem;
   }
   
   /**
    * Gets a new default User Agent. The values in the User Agent will be determined by the FOP Properties used
    * to initialize the FOP factory.   
    * @return the new User Agent object. 
    */
   protected FOUserAgent getFOUserAgent() {
       FOUserAgent userAgent = fopFactory.newFOUserAgent();
       //Configure foUserAgent as desired
       return userAgent;
   }
   
   /**
    * Sets up the FOUserAgent metadata parameters based on the values included in the bindings. 
    * The bindings in <code>$FOP</code> are evaluated and saved into the user agent
    * @param agent the user agent 
    * @param eval the JEXL Evaluator for the current content item. 
    * @return the modified user agent
    */
   protected FOUserAgent configureUserAgent(FOUserAgent agent, PSJexlEvaluator eval)
   {
      try
      {
         String author = (String) eval.evaluate(FOP_AUTHOR); 
         if(StringUtils.isNotBlank(author))
         {
            log.debug("Setting author " + author);
            agent.setAuthor(author); 
         }
         
         String creator = (String) eval.evaluate(FOP_CREATOR); 
         if(StringUtils.isNotBlank(author))
         {
            log.debug("Setting creator " + creator);
            agent.setCreator(creator);
         }
         
         String producer = (String) eval.evaluate(FOP_PRODUCER); 
         if(StringUtils.isNotBlank(producer))
         {
            log.debug("Setting producer " + producer);
            agent.setProducer(producer);
         }
         
         String keywords = (String) eval.evaluate(FOP_KEYWORDS); 
         if(StringUtils.isNotBlank(keywords))
         {
            log.debug("Setting keywords " + keywords);
            agent.setKeywords(keywords);
         }
         
         String title = (String) eval.evaluate(FOP_TITLE); 
         if(StringUtils.isNotBlank(title))
         {
            log.debug("Setting title " + title);
            agent.setTitle(title);
         }
         
         String creationDateStr = (String)eval.evaluate(FOP_CREATIONDATE); 
         if(StringUtils.isNotBlank(creationDateStr))
         {
            log.debug("Setting creation date " + creationDateStr);
            Date createDate = PSDataTypeConverter.parseStringToDate(creationDateStr);
            agent.setCreationDate(createDate);
         }
         
         String targetResolution = (String)eval.evaluate(FOP_TARGETRESOLUTION); 
         if(StringUtils.isNotBlank(targetResolution))
         {
            log.debug("Setting target resolution " + targetResolution);
            int resolution = Integer.parseInt(targetResolution);
            agent.setTargetResolution(resolution); 
         }
      } catch (Exception ex)
      {
         log.error("Error binding user configuration "  + ex.getMessage(), ex); 
      }
      
      return agent; 
   }
   
   /**
    * Validate the MIME type of the calling template.  If the type is one of the recognized list, it will be 
    * returned unchanged.  Otherwise, this function will return <code>application/pdf</code>
    * @param mimeType the MIME type to validate
    * @return the valid MIME Type or application/pdf
    */
   private static String validateMimeType(String mimeType)
   {     
      log.debug("Validating Mime Type " + mimeType);
      mimeType = StringUtils.substringBefore(mimeType, ";").trim();
      if(StringUtils.isEmpty(mimeType))
      {
         log.debug("No MIME Type, using PDF");
         return MimeConstants.MIME_PDF;
      }
      List vtl = Arrays.asList(validMimeTypes);
      if(vtl.contains(mimeType))
      {
         log.debug("returning type " + mimeType);
         return mimeType;
      }
      log.debug("returning default - PDF"); 
      return MimeConstants.MIME_PDF;
   }
   
   /**
    * Array of valid MIME types for the FOP processor.  
    */
   private static String[] validMimeTypes = null;  
//     {MimeConstants.MIME_PDF, 
//      MimeConstants.MIME_RTF, MimeConstants.MIME_FOP_PRINT, 
//      MimeConstants.MIME_EPS, MimeConstants.MIME_PCL, 
//      MimeConstants.MIME_PLAIN_TEXT}; 
   
   private static final Expression FOP_AUTHOR =  PSJexlEvaluator.createStaticExpression("$FOP.author"); 
   private static final Expression FOP_CREATOR =  PSJexlEvaluator.createStaticExpression("$FOP.creator"); 
   private static final Expression FOP_PRODUCER =  PSJexlEvaluator.createStaticExpression("$FOP.producer"); 
   private static final Expression FOP_KEYWORDS =  PSJexlEvaluator.createStaticExpression("$FOP.keywords"); 
   private static final Expression FOP_TITLE =  PSJexlEvaluator.createStaticExpression("$FOP.title"); 
   private static final Expression FOP_CREATIONDATE =  PSJexlEvaluator.createStaticExpression("$FOP.creationDate"); 
   private static final Expression FOP_TARGETRESOLUTION =  PSJexlEvaluator.createStaticExpression("$FOP.targetResolution");
   
   /**
    * Parameter value for showing XML.  Specify any non-empty value for this
    * HTML parameter, and the assembler will return the XML that would have been
    * passed to the FO Processor. 
    * Use this for debugging templates.
    */
   public static final String SHOW_XML_PARAMETER = "showxml";
   
   public static final String INIT_PARAMETER_MIME_TYPES = "com.percussion.pso.fop.SupportedMimeTypes"; 
}

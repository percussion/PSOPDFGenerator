package com.percussion.pso.pdf;
/*
 * Copyright 1999-2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: FopServlet.java 389086 2006-03-27 09:51:14Z jeremias $ */

/* TODO */
/* Tidy up other methods (would any of the other ones be useful? */
/* Make sure all error handling is in and correct */


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;

import java.util.Properties;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.commons.logging.impl.SimpleLog;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
//import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.params.HttpMethodParams;
//import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpStatus;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
/**
 * Example servlet to generate a PDF from a servlet.
 * <br/>
 * Servlet param is:
 * <ul>
 *   <li>fo: the path to a XSL-FO file to render
 * </ul>
 * or
 * <ul>
 *   <li>xml: the path to an XML file to render</li>
 *   <li>xslt: the path to an XSLT file that can transform the above XML to XSL-FO</li>
 * </ul>
 * <br/>
 * Example URL: http://servername/fop/servlet/FopServlet?fo=readme.fo
 * <br/>
 * Example URL: http://servername/fop/servlet/FopServlet?xml=data.xml&xslt=format.xsl
 * <br/>
 * For this to work with Internet Explorer, you might need to append "&ext=.pdf"
 * to the URL.
 *
 * @author <a href="mailto:fop-dev@xmlgraphics.apache.org">Apache FOP Development Team</a>
 * @version $Id: FopServlet.java 389086 2006-03-27 09:51:14Z jeremias $
 * (todo) Ev. add caching mechanism for Templates objects
 */
public class PDFServlet extends HttpServlet {

    
    /** Logger to give to FOP */
    protected Log log = LogFactory.getLog(PDFServlet.class);
    //protected SimpleLog log = null;
    /** The TransformerFactory used to create Transformer instances */
    protected TransformerFactory transFactory = null;
    /** The FopFactory used to create Fop instances */
    protected FopFactory fopFactory = null;
    /** URIResolver for use by this servlet */
    protected URIResolver uriResolver; 
    


    /**
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
//        this.log = new SimpleLog("FOP/Servlet");
//        log.setLevel(SimpleLog.LOG_LEVEL_WARN);
//        this.uriResolver = new ServletContextURIResolver(getServletContext());
        this.transFactory = TransformerFactory.newInstance();
//        this.transFactory.setURIResolver(this.uriResolver);
        
        m_conf = MyConfig.getInstance();
        
        MyAuthenticator.install();
        
        this.fopFactory = FopFactory.newInstance();
        this.fopFactory.setURIResolver(this.uriResolver);

        try {
            String fopConfigFilename = getServletContext().getRealPath("/WEB-INF/config/fop.config");        
            DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
            Configuration cfg = cfgBuilder.buildFromFile(new File(fopConfigFilename));
            this.fopFactory.setUserConfig(cfg);
        } catch (Exception e) {
        	System.err.println("There was an error loading the fop.properties file the error was: " + e.getMessage() ); 
        	throw new ServletException(e);
        }
        //Configure FopFactory as desired
        configureFopFactory();
    }
    
    /**
     * This method is called right after the FopFactory is instantiated and can be overridden
     * by subclasses to perform additional configuration.
     */
    protected void configureFopFactory() {
        //Subclass and override this method to perform additional configuration
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException {
        try {

            //Get parameters

        	String sysAssemblyLocation = request.getParameter(m_conf.getProperty("resourceparam"));

        	String sysOutput = "pdf";
        	
        	if (!(request.getParameter("sys_output") == null)) {
        		sysOutput = request.getParameter("sys_output");
        	}
        	
        	
        	String remoteHost = m_conf.getProperty("rhythmyxserver");
        	String remotePort = m_conf.getProperty("rhythmyxport");
        	log.debug("Rhythmyx Server " + remoteHost + ":" + remotePort);
            
        	String sysContentID = request.getParameter("sys_contentid");
        	String sysRevision = request.getParameter("sys_revision");
        	String sysContext = request.getParameter("sys_context");
        	String sysAuthtype = request.getParameter("sys_authtype");
        	String sysFolderID = request.getParameter("sys_folderid");
        	String sysSiteID = request.getParameter("sys_siteid");
        	String sysVariantID = request.getParameter("sys_variantid");
        	String sysTemplate = request.getParameter("sys_template");
        	String sysItemFilter = request.getParameter("sys_itemfilter");
            
            //Analyze parameters and decide with method to use
            if (sysAssemblyLocation != null && sysOutput.equalsIgnoreCase("pdf")) {
                renderPDFfromRxResource(sysAssemblyLocation, remoteHost, remotePort, sysContentID, sysRevision, sysVariantID, sysSiteID, sysAuthtype, sysContext, sysItemFilter, sysTemplate, response);
            } else if (sysAssemblyLocation != null && sysOutput.equalsIgnoreCase("rtf")) {
                renderRTFfromRxResource(sysAssemblyLocation, remoteHost, remotePort, sysContentID, sysRevision, sysVariantID, sysSiteID, sysAuthtype, sysContext, sysItemFilter, sysTemplate, response);
            }else {
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.println("<html><head><title>Error</title></head>\n"
                          + "<body><h1>FopServlet Error</h1><h3>No 'sys_assemblylocation' "
                          + sysOutput + " request param given.</body></html>");
            }
        } catch (Exception ex) {System.err.println("There was an error in the doGet method the error was " + ex.getMessage());
        	log.error("Servlet Exception " + ex.getMessage(), ex); 
            throw new ServletException(ex);
        }
    }

    /**
     * Converts a String parameter to a JAXP Source object.
     * @param param a String parameter
     * @return Source the generated Source object
     */
    protected Source convertString2Source(String param) {
        return new StreamSource(new File(param));
    }

    private void sendPDF(byte[] content, HttpServletResponse response) throws IOException {
        //Send the result back to the client
        response.setContentType("application/pdf");
        response.setContentLength(content.length);
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }

    private void sendRTF(byte[] content, HttpServletResponse response) throws IOException {
        //Send the result back to the client
        response.setContentType("application/word");
        response.setContentLength(content.length);
        response.setHeader("Content-Disposition", "inline; filename=generic.doc"); 
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }
    
    /**
     * Renders an XSL-FO file into a PDF file. The PDF is written to a byte
     * array that is returned as the method's result.
     * @param fo the XSL-FO file
     * @param response HTTP response object
     * @throws FOPException If an error occurs during the rendering of the
     * XSL-FO
     * @throws TransformerException If an error occurs while parsing the input
     * file
     * @throws IOException In case of an I/O problem
     */
    protected void renderFO(String fo, HttpServletResponse response)
                throws FOPException, TransformerException, IOException {

        //Setup source
        Source foSrc = convertString2Source(fo);

        //Setup the identity transformation
        Transformer transformer = this.transFactory.newTransformer();
        transformer.setURIResolver(this.uriResolver);

        //Start transformation and rendering process
        render(foSrc, transformer, response);
    }


    
    protected void renderPDFfromRxResource(String url, String remoteHost, String remotePort, String sysContentID, String sysRevision, String sysVariantID, String sysSiteID, String sysAuthtype, String sysContext,  String sysItemFilter, String sysTemplate, HttpServletResponse response)
    throws FOPException, TransformerException, IOException {
    	
    	System.err.println("Username from properties file: " + m_conf.getProperty("username"));
    	System.err.println("Password from properties file: " + m_conf.getProperty("password"));
    	
    	// Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Set up our authentication credentials
        client.getState().setCredentials(null,
        		null,
                new UsernamePasswordCredentials(m_conf.getProperty("username"), m_conf.getProperty("password"))
            );
        
        // Create a method instance.
        GetMethod method = new GetMethod("http://" + remoteHost + ":" + remotePort + url);
        //System.err.println("http://" + remoteHost + ":" + remotePort + url);
        // Tell the GET method to automatically handle authentication. The
        // method will use any appropriate credentials to handle basic
        // authentication requests.  Setting this value to false will cause
        // any request for authentication to return with a status of 401.
        // It will then be up to the client to handle the authentication.
        method.setDoAuthentication( true );
        
        // Provide custom retry handler if necessary
//        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
//        		new DefaultHttpMethodRetryHandler(3, false));
		
        method.addRequestHeader("RX_USEBASICAUTH", "yes");
        
		 NameValuePair[] parameters = new NameValuePair[8];
		 parameters[0] = new NameValuePair("sys_contentid", sysContentID);
		 parameters[1] = new NameValuePair("sys_variantid", sysVariantID);
		 parameters[2] = new NameValuePair("sys_authtype", sysAuthtype);
		 parameters[3] = new NameValuePair("sys_siteid", sysSiteID);
		 parameters[4] = new NameValuePair("sys_context", sysContext);
		 parameters[5] = new NameValuePair("sys_revision", sysRevision);
		 parameters[6] = new NameValuePair("sys_template", sysTemplate);
		 parameters[7] = new NameValuePair("sys_itemfilter", sysItemFilter);

		 method.setQueryString(parameters);
		 
 
       
        try {
          // Execute the method.
          int statusCode = client.executeMethod(method);

          if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + method.getStatusLine());
            System.err.println("This was the url: " + url);
          }

          // Read the response body.
          InputStream in = method.getResponseBodyAsStream();
          
          // Create a new source for JAXP based on the InputStream from our get call
          Source foSrc  = new StreamSource(in);

      	  // Default identity transform
      	  Transformer transformer = this.transFactory.newTransformer();
      	  transformer.setURIResolver(this.uriResolver);

      	  //Start transformation and rendering process
      	  render(foSrc, transformer, response);


        } catch (HttpException e) {
          System.err.println("Fatal protocol violation: " + e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          System.err.println("Fatal transport error: " + e.getMessage());
          e.printStackTrace();
        } finally {
          // Release the connection.
          method.releaseConnection();
          
        }

    }   

    protected void renderRTFfromRxResource(String url, String remoteHost, String remotePort, String sysContentID, String sysRevision, String sysVariantID, String sysSiteID, String sysAuthtype, String sysContext, String sysItemFilter, String sysTemplate, HttpServletResponse response)
    throws FOPException, TransformerException, IOException {
    	//System.err.println("Rhythmyx host: " + remoteHost);
    	//System.err.println("Rhythmyx port: " + remotePort);

    	System.err.println("Username from properties file: " + m_conf.getProperty("username"));
    	System.err.println("Password from properties file: " + m_conf.getProperty("password"));

    	
    	
    	// Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Set up our authentication credentials
        client.getState().setCredentials(null, null,
                new UsernamePasswordCredentials(m_conf.getProperty("username"), m_conf.getProperty("password"))
            );
        
        // Create a method instance.
        GetMethod method = new GetMethod("http://" + remoteHost + ":" + remotePort + url);
        
        // Tell the GET method to automatically handle authentication. The
        // method will use any appropriate credentials to handle basic
        // authentication requests.  Setting this value to false will cause
        // any request for authentication to return with a status of 401.
        // It will then be up to the client to handle the authentication.
        method.setDoAuthentication( true );
        
        // Provide custom retry handler if necessary
//        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
//        		new DefaultHttpMethodRetryHandler(3, false));
		
        
		 NameValuePair[] parameters = new NameValuePair[8];
		 parameters[0] = new NameValuePair("sys_contentid", sysContentID);
		 parameters[1] = new NameValuePair("sys_variantid", sysVariantID);
		 parameters[2] = new NameValuePair("sys_authtype", sysAuthtype);
		 parameters[3] = new NameValuePair("sys_siteid", sysSiteID);
		 parameters[4] = new NameValuePair("sys_context", sysContext);
		 parameters[5] = new NameValuePair("sys_revision", sysRevision);
		 parameters[6] = new NameValuePair("sys_template", sysTemplate);
		 parameters[7] = new NameValuePair("sys_itemfilter", sysItemFilter);
		 
		 
		 
		 method.setQueryString(parameters);
		 
 
       
        try {
          // Execute the method.
          int statusCode = client.executeMethod(method);

          if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + method.getStatusLine());
            System.err.println("This was the url: " + url);
          }

          // Read the response body.
          InputStream in = method.getResponseBodyAsStream();
          
          // Create a new source for JAXP based on the InputStream from our get call
          Source foSrc  = new StreamSource(in);

      	  // Default identity transform
      	  Transformer transformer = this.transFactory.newTransformer();
      	  transformer.setURIResolver(this.uriResolver);

      	  //Start transformation and rendering process
      	  renderRTF(foSrc, transformer, response);


        } catch (HttpException e) {
          System.err.println("Fatal protocol violation: " + e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          System.err.println("Fatal transport error: " + e.getMessage());
          e.printStackTrace();
        } finally {
          // Release the connection.
          method.releaseConnection();
          
        }

    }   
    /**
     * Renders an input file (XML or XSL-FO) into a PDF file. It uses the JAXP
     * transformer given to optionally transform the input document to XSL-FO.
     * The transformer may be an identity transformer in which case the input
     * must already be XSL-FO. The PDF is written to a byte array that is
     * returned as the method's result.
     * @param src Input XML or XSL-FO
     * @param transformer Transformer to use for optional transformation
     * @param response HTTP response object
     * @throws FOPException If an error occurs during the rendering of the
     * XSL-FO
     * @throws TransformerException If an error occurs during XSL
     * transformation
     * @throws IOException In case of an I/O problem
     */
    protected void render(Source src, Transformer transformer, HttpServletResponse response)
                throws FOPException, TransformerException, IOException {

        FOUserAgent foUserAgent = getFOUserAgent();

        //Setup output
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        //Setup FOP
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

        //Make sure the XSL transformation's result is piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        //Start the transformation and rendering process
        transformer.transform(src, res);

        //Return the result
        sendPDF(out.toByteArray(), response);
    }
    
    protected void renderRTF(Source src, Transformer transformer, HttpServletResponse response)
    throws FOPException, TransformerException, IOException {

    	FOUserAgent foUserAgent = getFOUserAgent();
    	
    	//Setup output
    	ByteArrayOutputStream out = new ByteArrayOutputStream();

    	//Setup FOP
    	Fop fop = fopFactory.newFop(MimeConstants.MIME_RTF, foUserAgent, out);

    	//Make sure the XSL transformation's result is piped through to FOP
    	Result res = new SAXResult(fop.getDefaultHandler());

    	//Start the transformation and rendering process
    	transformer.transform(src, res);

    	//Return the result
    	sendRTF(out.toByteArray(), response);
    }   
    
    /** @return a new FOUserAgent for FOP */
    protected FOUserAgent getFOUserAgent() {
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        //Configure foUserAgent as desired
        return userAgent;
    }
    private MyConfig m_conf;
 


}




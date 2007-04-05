package com.percussion.pso.pdf;


import java.util.ResourceBundle;


// TODO : Add logging
/*import org.apache.log4j.Logger;*/

public class MyConfig{
	   
	public synchronized static MyConfig getInstance()
	   {
	     if (ms_singleInstance == null)

	         ms_singleInstance = new MyConfig();

	      return ms_singleInstance;

	   }

	
	   
	   private MyConfig()

	   {
		   
		   // TODO add error checking around the loading of this resource bundle
		   // The .properties is assumed
		   try {
			   m_bundle = ResourceBundle.getBundle("fop");
		   } 
	       catch (Exception e)
	       {
	    	   System.err.println("There was an error loading the fop.properties file"); 
	    	   m_bundle = null;
	       }
	       
	   }	   
	   /**

	    * Gets the value of a property in the navigation properties.

	    * 

	    * @param name the name of the property.

	    * @return the object representing that property. Will be <code>null</code>

	    *         if the property does not exist.

	    */

	   public String getProperty(String name)

	   {
	      return m_bundle.getString(name);
	   }



	   
	   /**

	    * singleton instance of this class.

	    */

	   private static MyConfig ms_singleInstance = null;
	   

	   /**

	    * write the log.

	    */

	   /*private static Logger m_log = Logger.getLogger(MyConfig.class);*/	   

	   private ResourceBundle m_bundle = null;
}

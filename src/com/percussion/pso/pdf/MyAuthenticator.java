package com.percussion.pso.pdf;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class MyAuthenticator extends Authenticator {

    /**
     * @see java.net.Authenticator#getPasswordAuthentication()
     */
    protected PasswordAuthentication getPasswordAuthentication() {

    	m_conf = MyConfig.getInstance();
        return new PasswordAuthentication(m_conf.getProperty("username"), m_conf.getProperty("password").toCharArray());
    }

    public static final void install() {
        Authenticator.setDefault(new MyAuthenticator());
    }
    private MyConfig m_conf;
    
}
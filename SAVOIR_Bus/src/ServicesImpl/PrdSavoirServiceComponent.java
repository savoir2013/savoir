// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ServicesImpl;
import java.io.StringReader;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import ca.gc.nrc.iit.savoir.thresholdMgmt.ThresholdMgr;



import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import java.util.ResourceBundle;
import java.util.Locale;

import cxfSupport.InputStreamClosingInterceptor;

public class PrdSavoirServiceComponent {
	//static String[] CONFIG_FILES = new String[] { "classpath:cxf.xml" };

	//static ApplicationContext ac = new ClassPathXmlApplicationContext(
	//		CONFIG_FILES);
    private ThresholdMgr thrMgr = null;
    private ResourceBundle resource = ResourceBundle.getBundle("savoirbus", Locale.getDefault());
    private String mgmtUrl;
    public PrdSavoirServiceComponent()
    {
    	//thrMgr = (ThresholdMgr)ac.getBean("thresholdMgrClient");
    	JaxWsProxyFactoryBean pfb = new JaxWsProxyFactoryBean();
    	pfb.getInInterceptors().add(new InputStreamClosingInterceptor());
        pfb.setServiceClass(ThresholdMgr.class);
        mgmtUrl = resource.getString("savoir.prd.bus.mgmt.url");
        //test for local use 80
        pfb.setAddress(mgmtUrl);
        thrMgr = (ThresholdMgr)pfb.create();
    	
    }
	public void servReqByThrManager(String msg){
		if(thrMgr == null){
			//thrMgr = (ThresholdMgr)ac.getBean("thresholdMgrClient");
			JaxWsProxyFactoryBean pfb = new JaxWsProxyFactoryBean();
			pfb.getInInterceptors().add(new InputStreamClosingInterceptor());
	        pfb.setServiceClass(ThresholdMgr.class);
	        mgmtUrl = resource.getString("savoir.prd.bus.mgmt.url");
	        //test for local use 80
	        pfb.setAddress(mgmtUrl);
	        thrMgr = (ThresholdMgr)pfb.create();
		}
		thrMgr.handleIncoming(msg);
	}
	


}

// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ServicesImpl;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;
import Services.EdgeServicesPrototype;
 
@WebService(endpointInterface = "Services.EdgeServicesPrototype")
public class EdgeServicePrototypeImpl implements EdgeServicesPrototype{
	public String sendOutBoundMsg(String outBoundMsg){
		String firstMsg = null;
		
		firstMsg = outBoundMsg;
		return firstMsg;
	}
	
	

}

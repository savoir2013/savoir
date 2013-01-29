// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package Services;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
@WebService
public interface EdgeServicesPrototype {
	public String sendOutBoundMsg(@WebParam(name="outBoundMsg") String outBoundMsg);
}

// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.credMgmt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.gc.iit.nrc.savoir.domain.CredentialParameter;
import ca.gc.iit.nrc.savoir.domain.CredentialSchema;
import ca.gc.iit.nrc.savoir.domain.CredentialSchemaParameter;
import ca.gc.iit.nrc.savoir.domain.types.ParameterType;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;

/**
 * Validates credentials against their schema.
 * <p>
 * Credentials are represented as sets of name-value pairs, where each of the 
 * pairs is stored using a standard {@code ParameterType}. The schemas are 
 * similarly simple - a schema consists of a list of parameter names, which may 
 * be marked as optional or required as well as once-only or multiple-allowed. 
 * This class provides a utility method which, given a list of parameters 
 * comprising a credential, and the ID of a resource to check them against, 
 * verifies that all required parameters are present, no parameters that should 
 * only have one occurance are listed multiple times, and no parameters are 
 * present that are not specified in the schema.
 * 
 * @author Aaron Moss
 */
public class CredSchemaValidator {

	/**
	 * Checks if a credential is valid with regard to a resource's credential 
	 * schema.
	 * 
	 * @param creds		The parameters composing the credential to validate
	 * @param resource	The resource to validate against. If the resource has 
	 * 					no associated schema, will return valid.
	 * 
	 * @return are the credentials valid against the resource's schema?
	 */
	public static boolean isValidForResource(List<CredentialParameter> creds, 
			int resource) {
		//get schema
		CredentialSchema schema = 
			DAOFactory.getDAOFactoryInstance().getCredentialDAO()
				.getSchemaByResource(resource);
		
		//where there is no schema to validate against, the credential is 
		// considered valid
		if (null == schema) {
			return true;
		}
		
		//the set of required parameters yet to be found
		Set<ParameterType> unfoundRequired = 
			new HashSet<ParameterType>(schema.getRequiredParams());
		//the set of currently valid parameters
		Map<ParameterType, CredentialSchemaParameter> validParams =
			new HashMap<ParameterType, CredentialSchemaParameter>();
		
		for (CredentialSchemaParameter p : schema.getParams()) {
			validParams.put(p.getParameterType(),p);
		}
		
		for (CredentialParameter p : creds) {
			//get the schema parameter for this parameter
			CredentialSchemaParameter sp = validParams.get(p.getParameter());
			
			//if no such parameter, fail
			if (null == sp) {
				return false;
			}
			
			//mark parameter found, if required
			unfoundRequired.remove(p.getParameter());
			
			//if schema does not allow multiples of this parameter, remove from 
			// valid params
			if (!sp.isAllowMultiple()) {
				validParams.remove(p.getParameter());
			}
		}
		
		//return success if and only if there remain no unfound required 
		// parameters
		return unfoundRequired.isEmpty();
	}
}

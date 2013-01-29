/*
 * Copyright 2007 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.gc.nrc.iit.oauth.common.signature;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.gc.nrc.iit.oauth.common.OAuth;
import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.common.exception.OAuthException;
import ca.gc.nrc.iit.oauth.common.exception.OAuthProblemException;


/**
 * A pair of algorithms for computing and verifying an OAuth digital signature.
 * <p>
 * Static methods of this class implement a registry of signature methods. It's
 * pre-populated with the standard OAuth algorithms. Appliations can replace
 * them or add new ones.
 * 
 * @author John Kristian, Netflix
 * @author Aaron Moss, NRC-IIT
 */
public abstract class OAuthSignatureMethod {

    /** Add a signature to the message. 
     * @throws URISyntaxException 
     * @throws IOException */
    public void sign(OAuthParams params, OAuthToken token, OAuthConsumer consumer) 
    		throws OAuthException {
        params.setSignature(getSignature(params, token, consumer));
    }

    /**
     * Check whether the message has a valid signature.
     * @throws URISyntaxException 
     *
     * @throws OAuthProblemException
     *             the signature is invalid
     */
    public void validate(OAuthParams params, OAuthToken token, OAuthConsumer consumer)
    		throws OAuthException {
        if (params.getSignature() == null) {
        	OAuthProblemException e = 
        		new OAuthProblemException(OAuth.Problems.PARAMETER_ABSENT);
        	e.setParameter(OAuth.Problems.PARAMETER_ABSENT, 
        			OAuth.percentEncode(OAuthParams.OAUTH_SIGNATURE));
        	throw e;
        }
        String signature = params.getSignature();
        String baseString;
		try {
			baseString = getBaseString(params);
		} catch (IOException e) {
			throw new OAuthProblemException(OAuth.Problems.SIGNATURE_INVALID);
		}
        if (!isValid(signature, baseString, token, consumer)) {
            OAuthProblemException problem = 
            	new OAuthProblemException(OAuth.Problems.SIGNATURE_INVALID);
            problem.setParameter("oauth_signature", signature);
            problem.setParameter("oauth_signature_base_string", baseString);
            problem.setParameter("oauth_signature_method",
                    params.getSignatureMethod());
            throw problem;
        }
    }

    protected String getSignature(OAuthParams params, OAuthToken token, OAuthConsumer consumer)
    		throws OAuthException {
    	String baseString;
		try {
			baseString = getBaseString(params);
		} catch (IOException e) {
			throw new OAuthProblemException(OAuth.Problems.SIGNATURE_INVALID);
		}
        String signature = getSignature(baseString, token, consumer);
        return signature;
    }

    /** Compute the signature for the given base string. */
    protected abstract String getSignature(String baseString, OAuthToken token, 
    		OAuthConsumer consumer) throws OAuthException;

    /** Decide whether the signature is valid. */
    protected abstract boolean isValid(String signature, String baseString, 
    		OAuthToken token, OAuthConsumer consumer) throws OAuthException;
    
    /** @return the name of this signature method */
    public abstract String getMethodName();

    public static String getBaseString(OAuthParams params) throws IOException {
        return OAuth.percentEncode(params.getHttpMethod()) + '&'
                + OAuth.percentEncode(params.getUrl()) + '&'
                + OAuth.percentEncode(OAuth.formEncode(params.getSignatureParameters()));
    }

    /**
     * Determine whether the given strings contain the same sequence of
     * characters. The implementation discourages a <a
     * href="http://codahale.com/a-lesson-in-timing-attacks/">timing attack</a>.
     */
    public static boolean equals(String x, String y) {
        if (x == null)
            return y == null;
        else if (y == null)
            return false;
        else if (y.length() <= 0)
            return x.length() <= 0;
        char[] a = x.toCharArray();
        char[] b = y.toCharArray();
        char diff = (char) ((a.length == b.length) ? 0 : 1);
        int j = 0;
        for (int i = 0; i < a.length; ++i) {
            diff |= a[i] ^ b[j];
            j = (j + 1) % b.length;
        }
        return diff == 0;
    }

    /**
     * Determine whether the given arrays contain the same sequence of bytes.
     * The implementation discourages a <a
     * href="http://codahale.com/a-lesson-in-timing-attacks/">timing attack</a>.
     */
    public static boolean equals(byte[] a, byte[] b) {
        if (a == null)
            return b == null;
        else if (b == null)
            return false;
        else if (b.length <= 0)
            return a.length <= 0;
        byte diff = (byte) ((a.length == b.length) ? 0 : 1);
        int j = 0;
        for (int i = 0; i < a.length; ++i) {
            diff |= a[i] ^ b[j];
            j = (j + 1) % b.length;
        }
        return diff == 0;
    }

    public static byte[] decodeBase64(String s) {
        return BASE64.decode(s.getBytes());
    }

    public static String base64Encode(byte[] b) {
        return new String(BASE64.encode(b));
    }

    private static final Base64 BASE64 = new Base64();

    /** The factory for signature methods. */
    public static OAuthSignatureMethod newMethod(String name) throws OAuthException {
        try {
            Class<?> methodClass = NAME_TO_CLASS.get(name);
            if (methodClass != null) {
                OAuthSignatureMethod method = 
                	(OAuthSignatureMethod)methodClass.newInstance();
                return method;
            }
            OAuthProblemException problem = 
            	new OAuthProblemException(OAuth.Problems.SIGNATURE_METHOD_REJECTED);
            String acceptable = OAuth.percentEncode(NAME_TO_CLASS.keySet());
            if (acceptable.length() > 0) {
                problem.setParameter("oauth_acceptable_signature_methods",
                        acceptable.toString());
            }
            throw problem;
        } catch (InstantiationException e) {
            throw new OAuthException(e);
        } catch (IllegalAccessException e) {
            throw new OAuthException(e);
        }
    }
    
    public String toString() {
    	return getMethodName();
    }

    /**
     * Subsequently, newMethod(name) will attempt to instantiate the given
     * class, with no constructor parameters.
     */
    public static void registerMethodClass(String name, Class<?> clazz) {
        if (clazz == null)
            unregisterMethod(name);
        else
            NAME_TO_CLASS.put(name, clazz);
    }

    /**
     * Registers one of the default 3 methods by name
     * @param name one of "PLAINTEXT", "HMAC-SHA1", "RSA-SHA1"
     */
    public static void registerMethod(String name) {
    	if (PLAINTEXT.equals(name)) {
    		registerMethodClass(PLAINTEXT, PlaintextSignatureMethod.class);
    	} else if (HMAC_SHA1.equals(name)) {
    		registerMethodClass(HMAC_SHA1, HmacSha1SignatureMethod.class);
    	} else if (RSA_SHA1.equals(name)) {
    		registerMethodClass(RSA_SHA1, RsaSha1SignatureMethod.class);
    	}
    }
    
    /**
     * Subsequently, newMethod(name) will fail.
     */
    public static void unregisterMethod(String name) {
        NAME_TO_CLASS.remove(name);
    }
    
    public static final String HMAC_SHA1 = "HMAC-SHA1",
    	PLAINTEXT = "PLAINTEXT", RSA_SHA1 = "RSA-SHA1";

    private static final Map<String, Class<?>> NAME_TO_CLASS = 
    	new ConcurrentHashMap<String, Class<?>>();
    static {
        registerMethodClass(HMAC_SHA1, HmacSha1SignatureMethod.class);
        registerMethodClass(PLAINTEXT, PlaintextSignatureMethod.class);
        registerMethodClass(RSA_SHA1, RsaSha1SignatureMethod.class);
    }
}

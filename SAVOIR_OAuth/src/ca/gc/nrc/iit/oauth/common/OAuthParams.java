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
package ca.gc.nrc.iit.oauth.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.gc.nrc.iit.oauth.common.nonce.OAuthNonceFactory;
import ca.gc.nrc.iit.oauth.common.signature.OAuthSignatureMethod;


/**
 * A wrapper for parameters for an OAuth call
 * 
 * @author Aaron Moss, NRC-IIT
 * @author John Kristian, Netflix
 */
public class OAuthParams {
	/* Parameter names */
	public static final String OAUTH_PREFIX = "oauth_";
	
	public static final String OAUTH_CALLBACK = "oauth_callback";
	public static final String OAUTH_CALLBACK_CONFIRMED = "oauth_callback_confirmed";
	public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
	public static final String OAUTH_NONCE = "oauth_nonce";
	public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    public static final String OAUTH_TOKEN = "oauth_token";
    public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
    public static final String OAUTH_SIGNATURE = "oauth_signature";
    public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    public static final String OAUTH_VERIFIER = "oauth_verifier";
    public static final String OAUTH_VERSION = "oauth_version";
    
    /**
     * An easily sortable name-value parameter class 
     */
    public static class Parameter implements Map.Entry<String, String>, Comparable<Parameter> {
    	
    	private String name, value, key;
    	
    	public Parameter(){
    		this(null, null);    		
    	}
    	
    	public Parameter(Map.Entry<String, String> entry) {
    		this(entry.getKey(), entry.getValue());
    	}
    	
    	public Parameter(String name, String value) {
    		this.name = name;
    		this.value = value;
    		// ' ' is used because it comes before any character
            // that can appear in a percentEncoded string.
    		this.key = OAuth.percentEncode(this.name) + ' ' + 
    			OAuth.percentEncode(this.value);
    	}
    	
    	@Override
    	public int compareTo(Parameter o) {
    		return this.key.compareTo(o.key);
    	}

		public String getName() {
			return this.name;
		}

		public String getValue() {
			return this.value;
		}

		@Override
		public String getKey() {
			return this.name;
		}

		@Override
		public String setValue(String value) {
			String oldVal = this.value;
			this.value = value;
			return oldVal;
		}
    }
    
    /* The number of OAuth signature parameters */
    private static final int NUM_OAUTH_SIG_PARAMS = 6;
    /* The total number of OAuth parameters (includes signature) */
    private static final int NUM_OAUTH_PARAMS = NUM_OAUTH_SIG_PARAMS + 1;
	
	/** The HTTP method for this call - required for signing */
    private String httpMethod;
    /** The URL for this call - required for signing */
    private String url;
    
    /* OAuth parameters */
	/** callback URL or "oob" for out of band - only used on request token request */
    private String callback;
    /** The consumer to authenticate as - required parameter */
	private String consumerKey;
	/** The nonce used for this request - required parameter*/
	private String nonce;
	/** The signature method to use - required parameter */
	private String signatureMethod;
	/** The signature corresponding to these parameters */
	private String signature;
	/** The timestamp of this request - required parameter*/
	private long timestamp;
	/** The OAuth token */
	private String token;
	/** The version of the OAuth protocol used - optional parameter*/
	private double version;
	
	/** Non-OAuth parameters */
	private LinkedList<Parameter> otherParams;
	/** If true, otherParams needs sorted */
	private boolean otherParamsDirty;
	
	public OAuthParams() {
		this.otherParams = new LinkedList<Parameter>();
		this.otherParamsDirty = false;
	}
	
	public OAuthParams(OAuthConsumer consumer, String httpMethod, String url) 
			throws URISyntaxException {
		this(httpMethod, url, consumer.getConsumerKey(), null, null, null);
	}
	
	public OAuthParams(OAuthToken token, OAuthConsumer consumer, String httpMethod, String url) 
			throws URISyntaxException {
		this(httpMethod, url, consumer.getConsumerKey(), null, null, token.getToken());
	}
	
	public OAuthParams(String httpMethod, String url, String consumerKey, String nonce, 
			String signatureMethod, String token) 
			throws URISyntaxException {
		this.otherParams = new LinkedList<Parameter>();
		this.otherParamsDirty = false;
		this.setHttpMethod(httpMethod);
		this.setUrl(url);
		this.callback = null;
		this.consumerKey = consumerKey;
		this.setNonce(nonce);
		this.nonce = this.getNonce();
		this.signatureMethod = 
			signatureMethod == null ? OAuthSignatureMethod.HMAC_SHA1 : signatureMethod;
		this.timestamp = this.getTimestamp();
		this.token = token;
		this.version = OAuth.VERSION_1_0;
	}
	
	public String getHttpMethod() {
		return this.httpMethod;
	}
	
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod.toUpperCase();
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public void setUrl(String url) throws URISyntaxException {
		this.url = normalizeUrl(stripUrlParams(url));
	}
	
	public String getToken() {
		return this.token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getCallback() {
		return this.callback;
	}
	
	public void setCallback(String callback) {
		this.callback = callback;
	}
	
	public String getConsumerKey() {
		return this.consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getSignature() {
		return this.signature;
	}
	
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public String getSignatureMethod() {
		return this.signatureMethod;
	}

	public void setSignatureMethod(String signatureMethod) {
		this.signatureMethod = signatureMethod;
	}

	public long getTimestamp() {
		return this.timestamp == 0L ? System.currentTimeMillis() / 1000L : this.timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getNonce() {
		return this.nonce == null ? OAuthNonceFactory.newNonce() : this.nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public double getVersion() {
		return this.version;
	}
	
	public void setVersion(double version) {
		this.version = version;
	}
	
	public List<Parameter> getOtherParams() {
		if (this.otherParamsDirty) Collections.sort(this.otherParams);
		return this.otherParams;
	}
	
	public void setOtherParams(Collection<Parameter> otherParams) {
		this.otherParams = new LinkedList<Parameter>(otherParams);
		this.otherParamsDirty = true;
	}
	
	public void setOtherParams(SortedSet<Parameter> otherParams) {
		this.otherParams = new LinkedList<Parameter>(otherParams);
		this.otherParamsDirty = false;
	}
	
	public void addOtherParam(Parameter otherParam) {
		if (this.otherParams == null) 
			this.otherParams = new LinkedList<Parameter>();
		
		this.otherParams.add(otherParam);
		this.otherParamsDirty = true;
	}
	
	/**
	 * @return a list of parameters for the OAuth signature
	 */
	public List<Parameter> getSignatureParameters() {
		ArrayList<Parameter> l = new ArrayList<Parameter>(
				otherParams.size() + NUM_OAUTH_SIG_PARAMS);
		
		l.addAll(otherParams);
		
		//add oauth parameters
		if (callback != null) {
			//parameter not included on all messages
			l.add(new Parameter(OAUTH_CALLBACK, callback));
		}
		l.add(new Parameter(OAUTH_CONSUMER_KEY, consumerKey));
		l.add(new Parameter(OAUTH_NONCE, nonce));
		l.add(new Parameter(OAUTH_SIGNATURE_METHOD, signatureMethod));
		l.add(new Parameter(OAUTH_TIMESTAMP, Long.toString(timestamp)));
		if (token != null && !token.isEmpty()) {
			l.add(new Parameter(OAUTH_TOKEN, token));
		}
		if (version != 0) {
			//optional parameter
			l.add(new Parameter(OAUTH_VERSION, Double.toString(version)));
		}
		
		
		Collections.sort(l);
		return l;
	}
	
	/**
	 * Get the OAuth parameters, for message sending
	 * @return
	 */
	public List<Parameter> getOAuthParameters() {
		List<Parameter> l = new ArrayList<Parameter>(NUM_OAUTH_PARAMS);
		
		if (callback != null) {
			//parameter not included on all messages
			l.add(new Parameter(OAUTH_CALLBACK, callback));
		}
		l.add(new Parameter(OAUTH_CONSUMER_KEY, consumerKey));
		if (token != null) {
			if (!token.isEmpty()) {
				l.add(new Parameter(OAUTH_TOKEN, token));
			}
		}
		l.add(new Parameter(OAUTH_SIGNATURE_METHOD, signatureMethod));
		
		if (signature != null){ 
			l.add(new Parameter(OAUTH_SIGNATURE, signature));
		}
		l.add(new Parameter(OAUTH_TIMESTAMP, Long.toString(timestamp)));
		l.add(new Parameter(OAUTH_NONCE, nonce));
		l.add(new Parameter(OAUTH_VERSION, Double.toString(version)));
		
		return l;
	}
	
	/**
	 * Gets the combination of the OAuth parameters with the other parameters
	 * @return
	 */
	public List<Parameter> getParameters() {
		ArrayList<Parameter> l = new ArrayList<Parameter>(
				otherParams.size() + NUM_OAUTH_PARAMS);
		
		l.addAll(otherParams);
		l.addAll(getOAuthParameters());
		
		return l;
	}
	
	/**
	 * Sets parameters - will sort incoming OAuth parameters into their proper 
	 * variable. If given a parameter that already is set on this object, will 
	 * overwrite it.
	 * 
	 * @param parameters	The parameters to add
	 */
	public void setParameters(List<Parameter> parameters) {
		for (Parameter p : parameters) {
			//for OAuth parameters
			if (p.getName().substring(0,OAUTH_PREFIX.length()).equals(OAUTH_PREFIX)) {
				Object set = setOAuthParam(p.getName(), p.getValue());
				if (set == null) {
					//parameter not set, add to otherParams
					otherParams.add(p);
				}
				continue;
			}
			//all other parameters we add to otherParams
			otherParams.add(p);
		}
		otherParamsDirty = true;
	}

	
	public void dump(Map<String, Object> into) {
		if (callback != null) into.put(OAUTH_CALLBACK, callback);
		if (consumerKey != null) into.put(OAUTH_CONSUMER_KEY, consumerKey);
		if (nonce != null) into.put(OAUTH_NONCE, nonce);
		if (signatureMethod != null) into.put(OAUTH_SIGNATURE_METHOD, signatureMethod);
		if (signature != null) into.put(OAUTH_SIGNATURE, signature);
		if (timestamp != 0L) into.put(OAUTH_TIMESTAMP, timestamp);
		if (token != null) into.put(OAUTH_TOKEN, token);
		if (version != 0.0) into.put(OAUTH_VERSION, version);
		for (Parameter p : otherParams) {
			into.put(p.getName(), p.getValue());
		}
	}
	
	protected static String normalizeUrl(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String scheme = uri.getScheme().toLowerCase();
        String authority = uri.getAuthority().toLowerCase();
        boolean dropPort = (scheme.equals("http") && uri.getPort() == 80)
                           || (scheme.equals("https") && uri.getPort() == 443);
        if (dropPort) {
            // find the last : in the authority
            int index = authority.lastIndexOf(":");
            if (index >= 0) {
                authority = authority.substring(0, index);
            }
        }
        String path = uri.getRawPath();
        if (path == null || path.length() <= 0) {
            path = "/"; // conforms to RFC 2616 section 3.2.2
        }
        // we know that there is no query and no fragment here.
        return scheme + "://" + authority + path;
    }
	
	protected String stripUrlParams(String url) {
      int q = url.indexOf('?');
      if (q < 0) {
          return url;
      } else {
          // Combine the URL query string with the other parameters:
          setParameters(OAuth.decodeForm(url.substring(q + 1)));
          return url.substring(0, q);
      }
	}

	/**
     * Parse the parameters from an OAuth Authorization header. 
     * If the given header doesn't start with "OAuth ", do nothing.
     */
	public void decodeAuthorization(String authorization) {
		Matcher m = AUTHORIZATION.matcher(authorization);
		if (m.matches()) {
			if ("OAuth".equalsIgnoreCase(m.group(1))) {
				for (String nvp : m.group(2).split("\\s*,\\s*")) {
                    m = NVP.matcher(nvp);
                    if (m.matches()) {
                        String name = OAuth.decodePercent(m.group(1));
                        String value = OAuth.decodePercent(m.group(2));
                        setOAuthParam(name, value);
                    }
                }
			}
		}
	}
	
	private static final Pattern AUTHORIZATION = Pattern.compile("\\s*(\\w*)\\s+(.*)");
	private static final Pattern NVP = Pattern.compile("(\\S*)\\s*\\=\\s*\"([^\"]*)\"");
	
	/**
	 * Sets the appropriate OAuth parameter, given its name and value
	 * 
	 * @param name		The name of the parameter
	 * @param value		The value to set it to
	 * 
	 * @return the value if a valid parameter, null otherwise
	 */
	protected Object setOAuthParam(String name, String value) {
		if (name.equals(OAUTH_CALLBACK)) {
			return (this.callback = value);
		} else if (name.equals(OAUTH_CONSUMER_KEY)) {
			return (this.consumerKey = value);
		} else if (name.equals(OAUTH_NONCE)) {
			return (this.nonce = value);
		} else if (name.equals(OAUTH_SIGNATURE_METHOD)) {
			return (this.signatureMethod = value);
		} else if (name.equals(OAUTH_SIGNATURE)) {
			return (this.signature = value);
		} else if (name.equals(OAUTH_TIMESTAMP)) {
			return (this.timestamp = Long.parseLong(value));
		} else if (name.equals(OAUTH_TOKEN)) {
			return (this.token = value);
		} else if (name.equals(OAUTH_VERSION)) {
			return (this.version = Double.parseDouble(value));
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if (httpMethod != null) sb.append("HTTP Method:\t`").append(httpMethod).append("'\n");
		if (url != null) sb.append("URL:\t`").append(url).append("'\n");
		if (callback != null) sb.append("Callback:\t`").append(callback).append("'\n");
		if (consumerKey != null) sb.append("Consumer Key:\t`").append(consumerKey).append("'\n");
		if (nonce != null) sb.append("Nonce:\t`").append(nonce).append("'\n");
		if (signatureMethod != null) 
			sb.append("Signature Method:\t`").append(signatureMethod).append("'\n");
		if (signature != null) sb.append("Signature:\t`").append(signature).append("'\n");
		if (timestamp != 0) sb.append("Timestamp:\t`").append(timestamp).append("'\n");
		if (token != null) sb.append("Token:\t`").append(token).append("'\n");
		if (version != 0.0) sb.append("OAuth version:\t`").append(version).append("'\n");
		
		for (Parameter p : otherParams) {
			sb.append(p.getName()).append(":\t`").append(p.getValue()).append("'\n");
		}
		
		return sb.toString();
	}
}

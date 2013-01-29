// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.oAuthProvider;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.common.OAuthParams.Parameter;

/**
 * A serialization wrapper around the default OAuthParams.
 * (The default OAuthParams object does not serialize efficiently, so this 
 * version, with a simplified java beans interface, is used to wrap an 
 * OAuthParams object for more convenient serialization).
 * 
 * @author Aaron Moss
 */
public class WebServiceOAuthParams {
	
	/**
	 * Wraps a parameter entry, a key-value pair
	 * 
	 * @author Aaron Moss
	 */
	public static class KeyValuePair {
		
		/** The parameter key */
		private String k;
		/** The parameter value */
		private String v;
		
		public KeyValuePair() {}
		
		public KeyValuePair(String k, String v) {
			this.k = k;
			this.v = v;
		}

		public String getK() {
			return k;
		}

		public String getV() {
			return v;
		}

		public void setK(String k) {
			this.k = k;
		}

		public void setV(String v) {
			this.v = v;
		}
	}
	
	/**
	 * Due to some nastiness in how CXF/JAXB deserializes {@code List<?>} type 
	 * fields of objects (adding elements to the list returned by 
	 * {@code getXxx()} instead of calling {@code setXxx()}), this is a list 
	 * that can account for this (brain-dead) behaviour.
	 */
	private static class ParamUpdateList extends ArrayList<KeyValuePair> {
		private static final long serialVersionUID = 1L;
		
		private OAuthParams wrapped;
		
		public ParamUpdateList(OAuthParams wrapped) {
			super();
			
			this.wrapped = wrapped;
			
			//un-null wrapped OAuthParams, if needed
			if (this.wrapped == null) this.wrapped = new OAuthParams();

			//add existing other parameters to list
			List<Parameter> ps = this.wrapped.getOtherParams();
			super.ensureCapacity(ps.size());
			for (Parameter p : ps) 
				super.add(new KeyValuePair(p.getName(), p.getValue()));
		}
		
		@Override
		public void add(int index, KeyValuePair element) {
			super.add(index, element);
			addToWrapped(element);
		}

		@Override
		public boolean add(KeyValuePair e) {
			boolean ret = super.add(e);
			addToWrapped(e);
			return ret;
		}

		@Override
		public boolean addAll(Collection<? extends KeyValuePair> c) {
			boolean ret = super.addAll(c);
			if (c != null) for (KeyValuePair e : c) {
				addToWrapped(e);
			}
			return ret;
		}

		@Override
		public boolean addAll(int index, Collection<? extends KeyValuePair> c) {
			boolean ret = super.addAll(index, c);
			if (c != null) for (KeyValuePair e : c) {
				addToWrapped(e);
			}
			return ret;
		}

		@Override
		public KeyValuePair remove(int index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public KeyValuePair set(int index, KeyValuePair element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}
		
		
		private void addToWrapped(KeyValuePair e) {
			if (e == null) return;
			wrapped.addOtherParam(new Parameter(e.getK(), e.getV()));
		}
	}
	
	/** The wrapped OAuthParams object */
	private OAuthParams params;
	
	
	/**
	 * Primary constructor
	 * @param params	OAuthParams object to wrap
	 */
	public WebServiceOAuthParams(OAuthParams params) {
		this.params = params;
	}
	
	/*
	 * Convenience constructors to mirror OAuthParams interface.
	 * Constructs a new wrapped OAuthParams object using the equivalent constructor.
	 */
	public WebServiceOAuthParams() {
		this(new OAuthParams());
	}
	
	public WebServiceOAuthParams(OAuthConsumer consumer, String httpMethod, 
			String url) throws URISyntaxException {
		this(new OAuthParams(consumer, httpMethod, url));
	}
	
	public WebServiceOAuthParams(OAuthToken token, OAuthConsumer consumer, 
			String httpMethod, String url) throws URISyntaxException {
		this(new OAuthParams(token, consumer, httpMethod, url));
	}
	
	public WebServiceOAuthParams(String httpMethod, String url, 
			String consumerKey, String nonce, String signatureMethod, 
			String token) throws URISyntaxException {
		
		this(new OAuthParams(httpMethod, url, consumerKey, nonce, 
				signatureMethod, token));
	}
	
	
	public String getHttpMethod() {
		return params.getHttpMethod();
	}
	
	public void setHttpMethod(String httpMethod) {
		params.setHttpMethod(httpMethod);
	}
	
	public String getUrl() {
		return params.getUrl();
	}
	
	public void setUrl(String url) throws URISyntaxException {
		params.setUrl(url);
	}
	
	public String getToken() {
		return params.getToken();
	}
	
	public void setToken(String token) {
		params.setToken(token);
	}
	
	public String getCallback() {
		return params.getCallback();
	}
	
	public void setCallback(String callback) {
		params.setCallback(callback);
	}
	
	public String getConsumerKey() {
		return params.getConsumerKey();
	}

	public void setConsumerKey(String consumerKey) {
		params.setConsumerKey(consumerKey);
	}

	public String getSignature() {
		return params.getSignature();
	}
	
	public void setSignature(String signature) {
		params.setSignature(signature);
	}
	
	public String getSignatureMethod() {
		return params.getSignatureMethod();
	}

	public void setSignatureMethod(String signatureMethod) {
		params.setSignatureMethod(signatureMethod);
	}

	public long getTimestamp() {
		return params.getTimestamp();
	}

	public void setTimestamp(long timestamp) {
		params.setTimestamp(timestamp);
	}

	public String getNonce() {
		return params.getNonce();
	}

	public void setNonce(String nonce) {
		params.setNonce(nonce);
	}

	public double getVersion() {
		return params.getVersion();
	}
	
	public void setVersion(double version) {
		params.setVersion(version);
	}
	
	public List<KeyValuePair> getQueryParams() {
		return new ParamUpdateList(params);
	}
	
	public void setQueryParams(List<KeyValuePair> otherParams) {
		List<Parameter> toAdd = new ArrayList<Parameter>();
		
		if (otherParams != null) for (KeyValuePair e : otherParams) {
				Parameter p = new Parameter(e.getK(), e.getV());
				toAdd.add(p);
		}
		
		params.setOtherParams(toAdd);
	}
	
	/**
	 * Convert this to a standard OAuthParams object
	 */
	public OAuthParams toOAuthParams() {
		return this.params;
	}
}

/*
 * Copyright 2008 Netflix, Inc.
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

package ca.gc.nrc.iit.oauth.consumer.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.gc.nrc.iit.oauth.common.OAuth;
import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.common.OAuthParams.Parameter;
import ca.gc.nrc.iit.oauth.common.exception.OAuthException;
import ca.gc.nrc.iit.oauth.consumer.OAuthClient;
import ca.gc.nrc.iit.oauth.consumer.http.ExcerptInputStream;


import static ca.gc.nrc.iit.oauth.common.OAuth.*;

/**
 * An HTTP request or response.
 * 
 * @author John Kristian
 */
public class HttpMessage {

    public HttpMessage() {
        this(null, null);
    }

    public HttpMessage(String method, URL url) {
        this(method, url, null);
    }

    public HttpMessage(String method, URL url, InputStream body) {
        this.method = method;
        this.url = url;
        this.body = body;
    }

    public String method;
    public URL url;
    public final List<Map.Entry<String, String>> headers = new ArrayList<Map.Entry<String, String>>();
    protected InputStream body = null;
    private static String bodyEncoding = null;
    
    public static String getBodyEncoding() {
    	if (bodyEncoding == null) {
    		bodyEncoding = DEFAULT_CHARSET;
    	}
    	return bodyEncoding;
    }
    
    public static void setBodyEncoding(String bodyEncodingIn) {
    	bodyEncoding = bodyEncodingIn;
    }

    /**
     * Get the value of the last header of the given name. The name is
     * case-insensitive.
     */
    public final String getHeader(String name)
    {
        String value = null;
        for (Map.Entry<String, String> header : headers) {
            if (equalsIgnoreCase(name, header.getKey())) {
                value = header.getValue();
            }
        }
        return value;
    }

    /**
     * Remove all headers of the given name. The name is case insensitive.
     * 
     * @return the value of the last header with that name, or null to indicate
     *         there was no such header
     */
    public String removeHeaders(String name)
    {
        String value = null;
        for (Iterator<Map.Entry<String, String>> i = headers.iterator(); i.hasNext();) {
            Map.Entry<String, String> header = i.next();
            if (equalsIgnoreCase(name, header.getKey())) {
                value = header.getValue();
                i.remove();
            }
        }
        return value;
    }

    public final String getContentCharset()
    {
        return getCharset(getHeader(CONTENT_TYPE));
    }

    public final InputStream getBody() throws IOException
    {
        if (body == null) {
            InputStream raw = openBody();
            if (raw != null) {
                body = new ExcerptInputStream(raw);
            }
        }
        return body;
    }

    protected InputStream openBody() throws IOException {
        return null;
    }

    /** Put a description of this message and its origins into the given Map. */
    public void dump(Map<String, Object> into) throws IOException {
    }

    /**
     * Construct an HTTP request from this OAuth message.
     * 
     * @param style
     *            where to put the OAuth parameters, within the HTTP request
     */
    public static HttpMessage newRequest(OAuthParams from, OAuthToken token, OAuthConsumer consumer,
    		ParameterStyle style) throws IOException {
        final boolean isPost = HttpClient.POST.equalsIgnoreCase(from.getHttpMethod());
        InputStream body = null;
        if (style == ParameterStyle.BODY && !isPost) {
            style = ParameterStyle.QUERY_STRING;
        }
        String url = from.getUrl();
        final List<Map.Entry<String, String>> headers = 
        	new ArrayList<Map.Entry<String, String>>();
        
        //ensure signature set on parameters
        try {
			OAuthClient.ensureSignature(from, token, consumer);
		} catch (OAuthException ignored) {
		} catch (URISyntaxException ignored) {
		}
        
        switch (style) {
        case QUERY_STRING:
            url = OAuth.addParameters(url, from.getParameters());
            break;
        case BODY: {
            byte[] form = OAuth.formEncode(from.getParameters()).
            	getBytes(getBodyEncoding());
            headers.add(new Parameter(CONTENT_TYPE, OAuth.FORM_ENCODED));
            headers.add(new Parameter(CONTENT_LENGTH, form.length + ""));
            body = new ByteArrayInputStream(form);
            break;
        }
        case AUTHORIZATION_HEADER:
			headers.add(new Parameter("Authorization", OAuthClient.getAuthorizationHeader(from)));
            // Find the non-OAuth parameters:
            List<Parameter> others = from.getOtherParams();
            if (others != null && !others.isEmpty()) {
                // Place the non-OAuth parameters elsewhere in the request:
                if (isPost && body == null) {
                    byte[] form = OAuth.formEncode(others).getBytes(getBodyEncoding());
                    headers.add(new Parameter(CONTENT_TYPE, OAuth.FORM_ENCODED));
                    headers.add(new Parameter(CONTENT_LENGTH, form.length + ""));
                    body = new ByteArrayInputStream(form);
                } else {
                    url = OAuth.addParameters(url, others);
                }
            }
            break;
        }
        HttpMessage httpRequest = new HttpMessage(from.getHttpMethod(), new URL(url), body);
        httpRequest.headers.addAll(headers);
        
        return httpRequest;
    }

    private static boolean equalsIgnoreCase(String x, String y)
    {
        if (x == null)
            return y == null;
        else
            return x.equalsIgnoreCase(y);
    }

    private static final String getCharset(String mimeType)
    {
        if (mimeType != null) {
            Matcher m = CHARSET.matcher(mimeType);
            if (m.find()) {
                String charset = m.group(1);
                if (charset.length() >= 2 && charset.charAt(0) == '"'
                        && charset.charAt(charset.length() - 1) == '"') {
                    charset = charset.substring(1, charset.length() - 1);
                    charset = charset.replace("\\\"", "\"");
                }
                return charset;
            }
        }
        return DEFAULT_CHARSET;
    }

    /** The name of a dump entry whose value is the HTTP request. */
    protected static final String REQUEST = OAuth.HTTP_REQUEST;

    /** The name of a dump entry whose value is the HTTP response. */
    protected static final String RESPONSE = OAuth.HTTP_RESPONSE;

    /** The name of a dump entry whose value is the HTTP status code. */
    protected static final String STATUS_CODE = OAuth.HTTP_STATUS_CODE;

    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String DEFAULT_CHARSET = "ISO-8859-1";

    private static final Pattern CHARSET = Pattern
            .compile("; *charset *= *([^;\"]*|\"([^\"]|\\\\\")*\")(;|$)");

}

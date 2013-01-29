// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package transformer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.mule.transformer.AbstractTransformer;
import org.mule.util.IOUtils;

public class HttpRequestToSavoirString extends AbstractTransformer {

	public HttpRequestToSavoirString() {
        super();
        this.registerSourceType(String.class);
        this.registerSourceType(byte[].class);
        this.registerSourceType(InputStream.class);
        this.setReturnClass(String.class);
    }
	
	@Override
	protected Object doTransform(Object src, String encoding) {
		return asString(src, encoding);
	}
	
	private String asString(Object src, String encoding) {
		String result = null;
		
		if (src instanceof byte[]) {
			if (encoding != null) {
                try
                {
                    result = new String((byte[])src, encoding);
                }
                catch (UnsupportedEncodingException ex)
                {
                    result = new String((byte[])src);
                }
            } else {
                result = new String((byte[])src);
            }
        } else if (src instanceof InputStream) {
        	InputStream input = (InputStream) src;
        	
        	try {
        		result = IOUtils.toString(input);
        	} finally {
        		IOUtils.closeQuietly(input);
        	}
//            BufferedReader in = new BufferedReader(new InputStreamReader(input));
//            StringBuilder sb = new StringBuilder();
//            try {
//            	String s = in.readLine();
//	            while (s != null) {
//	            	sb.append(s).append('\n');
//	            	s = in.readLine();
//	            }
//            } catch (IOException ignored) {
//            } finally {
//            	try {
//					input.close();
//				} catch (IOException ignored) {}
//            }
//            result = sb.toString();
        } else {
            result = src.toString();
        }
		
		return result;
	}
}

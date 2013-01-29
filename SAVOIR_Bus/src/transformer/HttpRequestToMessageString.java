// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package transformer;


import org.mule.api.transformer.TransformerException;

import org.mule.transformer.AbstractTransformer;
import org.mule.util.IOUtils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class HttpRequestToMessageString extends AbstractTransformer {
	private static final String REQUEST_PARAMETER = "savoirmsg=";
		
	public HttpRequestToMessageString()
    {
        super();
        this.registerSourceType(String.class);
        this.registerSourceType(byte[].class);
        this.registerSourceType(InputStream.class);
        this.setReturnClass(String.class);
    }


	protected Object doTransform(Object src, String encoding)
			throws TransformerException {
		String msgStr = extractMessageValue(extractRequestQuery(convertRequestToString(src, encoding)));
		logger.info(msgStr + "\n");
		return msgStr;
		//return new MessageString(extractMessageValue(extractRequestQuery(convertRequestToString(src, encoding))));
	}
	
	private String convertRequestToString(Object src, String encoding)
    {
		String srcAsString = null;
        
        if (src instanceof byte[])
        {
            if (encoding != null)
            {
                try
                {
                    srcAsString = new String((byte[])src, encoding);
                }
                catch (UnsupportedEncodingException ex)
                {
                	logger.error(srcAsString);
                    srcAsString = new String((byte[])src);
                }
            }
            else
            {
                srcAsString = new String((byte[])src);
            }
        }
        else if (src instanceof InputStream)
        {
            InputStream input = (InputStream) src;
            try
            {
                srcAsString = IOUtils.toString(input);
            }
            finally
            {
                logger.info(input.toString());
                IOUtils.closeQuietly(input);
            }
        }
        else
        {
            srcAsString = src.toString();
        }        
        logger.info("srcAsString" + srcAsString);
        return srcAsString;
    }
	
	private String extractRequestQuery(String request)
    {
        String requestQuery = null;
        
        if (request != null && request.length() > 0 && request.indexOf('?') != -1)
        {
            requestQuery = request.substring(request.indexOf('?') + 1).trim();
        } else if (request != null && request.length() > 0 ){
        	requestQuery = request;
        }
        logger.info("requestQuery " + requestQuery);
        return requestQuery;
    }
    
    private String extractMessageValue(String requestQuery) throws TransformerException
    {
        String msgValue = null;
        
        if (requestQuery != null && requestQuery.length() > 0)
        {
            int nameParameterPos = requestQuery.indexOf(REQUEST_PARAMETER);
            if (nameParameterPos != -1)
            {
//                int nextParameterValuePos = requestQuery.indexOf('&'); 
//                if (nextParameterValuePos == -1 || nextParameterValuePos < nameParameterPos)
//                {
//                    nextParameterValuePos = requestQuery.length();
//                }

//                nameValue = requestQuery.substring(nameParameterPos + REQUEST_PARAMETER.length(), nextParameterValuePos);
            	msgValue = requestQuery.substring(nameParameterPos + REQUEST_PARAMETER.length());
            }
            
            if (msgValue != null && msgValue.length() > 0)
            {
                try
                {
                    msgValue = URLDecoder.decode(msgValue, "UTF-8");
                }
                catch (UnsupportedEncodingException uee)
                {
                    logger.error(uee.getMessage());
                }
            } else {
            	msgValue = requestQuery;
            }
        }

        if (msgValue == null)
        {
            msgValue = "";
        }
        logger.info("msgValue " + msgValue); 
        return msgValue;
    }

}

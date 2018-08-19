package com.amazonaws.lambda.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;

import java.util.HashMap;
import java.util.Map;


import io.proximax.xpx.facade.connection.RemotePeerConnection;

import io.proximax.xpx.facade.upload.Upload;
import io.proximax.xpx.facade.upload.UploadException;

import io.proximax.xpx.facade.upload.UploadResult;
import io.proximax.xpx.facade.upload.UploadTextDataParameter;


import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.LambdaLogger;


import org.json.simple.JSONObject;

import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

public class LambdaFunctionHandler implements RequestStreamHandler {
    JSONParser parser = new JSONParser();
    
    private static  String PRIVATE_KEY = "";
    private static  String PUBLIC_KEY = "";

    private static RemotePeerConnection remotePeerConnection = new RemotePeerConnection(
            "https://testnet.gateway.proximax.io");

    private static final Map<String, String> METADATA = fileToPlainMessageNemHashMap();

    private static Map<String, String> fileToPlainMessageNemHashMap() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("Author Name", "Faz");
        map.put("Year Published", "2018");
        return map;
    }

    
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

        LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of ProxyWithStream");
        JSONObject responseJson = new JSONObject();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject reqEvent = null;
        try {
			reqEvent = (JSONObject)parser.parse(reader);
		} catch(ParseException pex) {
			logger.log("1");
			pex.printStackTrace();
            responseJson.put("statusCode", "400");
            responseJson.put("exception", pex);
        }

        JSONObject reqQps = (JSONObject)reqEvent.get("queryStringParameters");
        JSONObject body = new JSONObject();
		try {
			body = (JSONObject)parser.parse((String)reqEvent.get("body"));
		} catch (ParseException pex) {
			logger.log("3");
			pex.printStackTrace();
            responseJson.put("statusCode", "400");
            responseJson.put("exception", pex);

		}
        JSONObject pps = (JSONObject)reqEvent.get("pathParameters");
        logger.log(body.toJSONString());

        JSONObject reqObj = new JSONObject();
        JSONObject proximaxJson = new JSONObject();
        
        
        PRIVATE_KEY = (String) body.get("privateKey");
        PUBLIC_KEY = (String) body.get("publicKey");

		switch ((String)pps.get("proxy")) {
		case "text":

			reqObj.put("jsontext", (String) body.get("jsontext"));


			logger.log(reqObj.toJSONString());

			proximaxJson = uploadText(reqObj);
			break;
		case "base64": 
			reqObj.put("base64", (String) body.get("base64"));


			logger.log(reqObj.toJSONString());

			proximaxJson = uploadText(reqObj);			
			break;
		}
        
        logger.log(proximaxJson.toJSONString());
        
        String responseCode = "200";

        JSONObject responseBody = new JSONObject();

      JSONObject headerJson = new JSONObject();
      headerJson.put("x-custom-header", "my custom header value");
      headerJson.put("Access-Control-Allow-Origin", "*");

      responseJson.put("isBase64Encoded", false);
      responseJson.put("statusCode", responseCode);
      responseJson.put("headers", headerJson);
//      responseJson.put("proximax", proximaxJson);
      responseJson.put("body", proximaxJson.toString());  
        
        logger.log(responseJson.toJSONString());
        logger.log(proximaxJson.toJSONString());
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toJSONString());  
        writer.close();
    }
    
    private JSONObject uploadText(JSONObject reqObj) {
        final UploadTextDataParameter parameter = UploadTextDataParameter.create().senderPrivateKey(PRIVATE_KEY)
                .receiverPublicKey(PUBLIC_KEY).data(reqObj.toString()).name("test-text")
                .contentType("text/plain").encoding("UTF-8").keywords("keywords").metadata(METADATA).build();        
        
        final Upload upload = new Upload(remotePeerConnection);
        UploadResult uploadResult;
        JSONObject proximaxJson = new JSONObject();
        try {
			uploadResult = upload.uploadTextData(parameter);
			proximaxJson.put("nemHash",uploadResult.getNemHash());
			proximaxJson.put("ipfsHash",uploadResult.getDataMessage().hash());
			proximaxJson.put("digest",uploadResult.getDataMessage().digest()); 
			proximaxJson.put("contentType",uploadResult.getDataMessage().type());
			proximaxJson.put("keywords",uploadResult.getDataMessage().keywords());
			proximaxJson.put("metadata",uploadResult.getDataMessage().metaData());		
		} catch (UploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return proximaxJson;
    }
    
    private JSONObject uploadBinary(JSONObject reqObj) {
        final UploadTextDataParameter parameter = UploadTextDataParameter.create().senderPrivateKey(PRIVATE_KEY)
                .receiverPublicKey(PUBLIC_KEY).data(reqObj.toString()).name("test-text")
                .contentType("text/plain").encoding("UTF-8").keywords("keywords").metadata(METADATA).build();        
        
        final Upload upload = new Upload(remotePeerConnection);
        UploadResult uploadResult;
        JSONObject proximaxJson = new JSONObject();
        try {
			uploadResult = upload.uploadTextData(parameter);
			proximaxJson.put("nemHash",uploadResult.getNemHash());
			proximaxJson.put("ipfsHash",uploadResult.getDataMessage().hash());
			proximaxJson.put("digest",uploadResult.getDataMessage().digest()); 
			proximaxJson.put("contentType",uploadResult.getDataMessage().type());
			proximaxJson.put("keywords",uploadResult.getDataMessage().keywords());
			proximaxJson.put("metadata",uploadResult.getDataMessage().metaData());		
		} catch (UploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return proximaxJson;    	
    }
}
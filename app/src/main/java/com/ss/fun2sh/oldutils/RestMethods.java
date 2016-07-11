package com.ss.fun2sh.oldutils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;


public class RestMethods {

	public static String WSPost(JSONObject jsonParam, String postMethod) {
		String result = postWebserviceMethod(jsonParam, postMethod);
		return result;
	}
	
	private static String getFormatedDate(String format) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Date date = new Date();
		return dateFormat.format(date);
	}

	public static String getCurrentDate() {
		String requiredDate = getFormatedDate("MMddyy");
		String addedDate = getFormatedDate("yyyy");
		return addedDate.substring(0, 2) + requiredDate;
	}

	private static JSONObject encryptJsonObjectResponse(
			String jsonObjectResponse) {

		JSONObject jsonObject = null;
		try {
			AESEncrypt aesEncrypt = new AESEncrypt();
			// getCurrentDate() Date format is yy(first two digits of the
			// current year)+MMddyy
		
			
//			String key = getCurrentDate() + Constants.ENCRIPTION_KEY;
			
			String key =  Constants.ENCRIPTION_KEY;
			
//			Logger.log("encryptionkey:::"+key+":::::"+getCurrentDate());
			
//			String key = Constants.ENCRIPTION_KEY;
			
			String encryptedText = aesEncrypt.encrypt(jsonObjectResponse, key);
			jsonObject = new JSONObject();
			jsonObject.put("Info", encryptedText);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	

	public static String postWebserviceMethod(JSONObject jsonParam, String postMethod) {

		HttpURLConnection httpURLConnection = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		String postResult = null;
		try {
			
		JSONObject encryptJsonObj=	encryptJsonObjectResponse(jsonParam.toString());
		
		Logger.log("responce:::::::encrypt:"+encryptJsonObj.toString());
		Logger.log("responce:::::::normal:"+jsonParam);
			
			URL url = new URL(Constants.SERVER_ADDRESS + postMethod);
			httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setUseCaches(false);
			/* optional request header */
			httpURLConnection.setRequestProperty(Constants.ACCEPT,Constants.CONTENT);
			httpURLConnection.setRequestProperty(Constants.CONTENT_TYPE,Constants.CONTENT);
			httpURLConnection.setRequestMethod("POST");
			// optional
			httpURLConnection.connect();

			// Send POST output.
			outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
			outputStream.write(encryptJsonObj.toString().getBytes());
			outputStream.flush();

			// getting response
			inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
			postResult = convertStreamToString(inputStream);

			Logger.log("output::::::::"+postResult);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			 Logger.log("response code is::catch1" + e);
		} catch (IOException e) {
			e.printStackTrace();
			 Logger.log("response code is::catch2" + e);
		} catch (Exception e) {
			e.printStackTrace();
			 Logger.log("response code is::catch3" + e);
		} finally {
			try {
				/* Close Stream */
				outputStream.close();
				inputStream.close();
				httpURLConnection.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return postResult;

	}

	private static String convertStreamToString(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return stringBuilder.toString();
	}
}

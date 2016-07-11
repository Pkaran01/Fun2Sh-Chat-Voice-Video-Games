package com.ss.fun2sh.encrypt;

import org.json.JSONObject;

/**
 * Created by ajaybabup on 6/8/2016.
 */
public class Encrypt {

    public static JSONObject encryptJsonObjectResponse(
            String jsonObjectResponse) {

        JSONObject jsonObject = null;
        try {
            AESEncrypt aesEncrypt = new AESEncrypt();
            // getCurrentDate() Date format is yy(first two digits of the
            // current year)+MMddyy


//			String key = getCurrentDate() + Constants.ENCRIPTION_KEY;

//            String key =  Const.ENCRIPTION_KEY;

            String key="8!c9r3ORIfeWr%^tt]nXQ";
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
}

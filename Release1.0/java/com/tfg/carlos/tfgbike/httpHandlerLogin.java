package com.tfg.carlos.tfgbike;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Carlos on 01/04/2015.
 * Envía al servidor una petición para pedir los datos de usuario en la base de datos del servidor y espera la respuesta.
 * {} = JSONObject
 * [] = JSONArray
 */
public class httpHandlerLogin {

    String posturl = "http://vidalbodeloncarlos.xyz/login.php";
    public JSONObject post(List<NameValuePair> params){

        JSONObject json = null;
        String result ="";

        try {
        //Creamos el objeto de HttpClient que nos permitira conectarnos mediante peticiones http
            HttpClient httpclient = new DefaultHttpClient();
        //El objeto HttpPost permite que enviemos una peticion de tipo POST a una URL especificada
            HttpPost httppost = new HttpPost(posturl);

		//Una vez añadidos los parametros actualizamos la entidad de httppost,
            httppost.setEntity(new UrlEncodedFormEntity(params));

         //Finalmente ejecutamos enviando la info al server*/
            HttpResponse resp = httpclient.execute(httppost);

            //Pasar de httpresponse a json
            BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            //para leer varias lineas, si solo fuese una linea no hace falta el bucle ni el builder
            for (String line = null; (line = reader.readLine()) != null;) {
                builder.append(line).append("\n");
            }

            reader.close();
            result = builder.toString();
            //result.concat("}");

        }
        catch(Exception e) { Log.d("HANDLER LOGIN EXCEPTION", e.toString(), e);}

        try{
            if(result.contains("email")) {
                json = new JSONObject(result);
            }
            else{//resultado no valido, retornar jarray null
                return json;
            }
        }catch(JSONException e){Log.d("HANDLER LOGIN EXCEPTION", "Error string to jsonobject" + e.toString(), e);}

        return json;
    }


}

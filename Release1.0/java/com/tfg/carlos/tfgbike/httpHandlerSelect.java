package com.tfg.carlos.tfgbike;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carlos on 18/05/2015.
 * Envía al servidor una petición Select para la base de datos del servidor y espera la respuesta.
 */
public class httpHandlerSelect {
    String posturl = "http://vidalbodeloncarlos.xyz/select.php";
    public JSONArray post(String query){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("query", query));

        JSONArray json = null;
        String result ="";

        try {
        /*Creamos el objeto de HttpClient que nos permitira conectarnos mediante peticiones http*/
            HttpClient httpclient = new DefaultHttpClient();
        /*El objeto HttpPost permite que enviemos una peticion de tipo POST a una URL especificada*/
            HttpPost httppost = new HttpPost(posturl);

		/*Una vez añadidos los parametros actualizamos la entidad de httppost, esto quiere decir en pocas palabras anexamos los parametros al objeto para que al enviarse al servidor envien los datos que hemos añadido*/
            httppost.setEntity(new UrlEncodedFormEntity(params));

         /*Finalmente ejecutamos enviando la info al server*/
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
            Log.d("HANDLER SELECT Result", result.concat("\n"));

        }
        catch(Exception e) { Log.d("HANDLERSELECT EXCEPTION", e.toString(), e);}

        try{//todas las tablas contienen el campo _id
            if(result.contains("_id")) {
                json = new JSONArray(result);
            }
            else{//resultado no valido

                return json;
            }
        }catch(JSONException e){Log.d("HttpHandler select", " result string to jsonarray error: " + e.toString(), e);}

        return json;
    }
}

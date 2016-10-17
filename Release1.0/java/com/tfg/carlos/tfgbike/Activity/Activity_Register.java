package com.tfg.carlos.tfgbike.Activity;


import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

//spinner
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.tfg.carlos.tfgbike.BD.BaseDeDatosLocalOperaciones;
import com.tfg.carlos.tfgbike.R;
import com.tfg.carlos.tfgbike.httpHandlerInsert;
import com.tfg.carlos.tfgbike.httpHandlerLogin;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Activity_Register extends ActionBarActivity {

    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    private Context context;

    private Spinner spinnersex;
    private ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        context = this;

        //INICIALIZAR EL SPINNER
        spinnersex = (Spinner) findViewById(R.id.spinner_register_sex);
        // Create an ArrayAdapter using the string array (string_choice_sex) and a default spinner layout
        adapter = ArrayAdapter.createFromResource(context,
                R.array.choice_sex, android.R.layout.simple_spinner_item);
        // Apply the adapter to the spinner
        spinnersex.setAdapter(adapter);

        //INICIALIZAR webview con EULA
        WebView eula = (WebView) findViewById(R.id.webView_registro);
        eula.loadUrl("http://vidalbodeloncarlos.xyz/condiciones.html");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void onClickButtonRegisterInRegister(View view){
        if(validarCampos()) {
            RegistroTask rt = new RegistroTask();
            rt.execute();
        }
    }

    /**
     * Hilo asíncrono que efectua el registro del usuario en el sistema.
     * Muestra un mensaje con el resultado de la operación.
     */
    private class RegistroTask extends AsyncTask<Void, Void, Boolean> {
        String message = "Error inesperado o desconocido";

        @Override
        protected Boolean doInBackground(Void... parameters) {
            try {
                //Comprobar que el usuario no existe
                //llamada al handler que conecta con la base de datos y devuelve un objeto json
                httpHandlerLogin handler = new httpHandlerLogin();
                JSONObject jo = handler.post(params);

                if (jo!=null) {
                    //el usuario existe...?
                    JSONArray ja = jo.getJSONArray("result").getJSONObject(0).getJSONArray("usuario");
                    String emailUser = params.get(0).getValue();
                    if ((ja.getJSONObject(0).getString("email")).equalsIgnoreCase(emailUser)) {
                        message = getString(R.string.error_email_existe);
                    } else {
                        message = getString(R.string.error_datos_inesperados);
                    }
                } else {
                    //Usuario nuevo, procedemos a dar el alta
                    altaUsuarios(params);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                //mostrar mensaje de exito
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                //volver al fragment bicis cuando pasen 2 segundos
                final Handler mHandler = new Handler();
                Runnable mUpdateTimeTask = new Runnable() {
                    public void run() {
                        //volver al login
                        onBackPressed();
                    }
                };
                mHandler.postDelayed(mUpdateTimeTask, 2000);
            }
            else{
                //mostrar mensaje de fracaso
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }

        //Construye la query y da el alta del usuario en las BD del servidor y local
        private void altaUsuarios(final List<NameValuePair> params) throws JSONException{

            //_id : autogenerado por la base de datos del servidor
            String query = "INSERT INTO usuarios (email, contrasena, nombre, apellido, sexo, edad, peso) " +
                    "VALUES ('" + params.get(0).getValue() + "','" + //email
                    params.get(1).getValue() + "','" +    //pass
                    params.get(2).getValue() + "','" +    //name
                    params.get(3).getValue() + "','" +    //surname
                    params.get(4).getValue() + "'," +    //sex
                    params.get(5).getValue() + "," +    //age
                    params.get(6).getValue() + ")";        //weight

            Log.d("SERVICE_REGISTER ", query);

            httpHandlerInsert handlerAlta = new httpHandlerInsert();
            //Alta en BD servidor
            String resultadoRegistro = handlerAlta.post(query);
            ContentValues values=new ContentValues();
            if(isInteger(resultadoRegistro)) {
                //usuario dado de alta en el servidor correctamente
                //Alta en BD local
                //el _id, sera el resultado del insert
                values.put("_id", resultadoRegistro);

                values.put("email", params.get(0).getValue());
                values.put("contrasena",  params.get(1).getValue());
                values.put("nombre", params.get(2).getValue());
                values.put("apellido",  params.get(3).getValue());
                values.put("sexo", params.get(4).getValue());
                values.put("edad",  params.get(5).getValue());
                values.put("peso", params.get(6).getValue());

                //insertar los values en la tabla local "usuarios"
                BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(context);
                db1.insertBDLocal(values, "usuarios");
                message = getString(R.string.msg_registro_ok);
            }
            else {
                //No alta en el servidor = no se da de alta en local al usuario, indispensable que se registre en el servidor correctamente
                message = getString(R.string.msg_registro_bad);
            }
        }
    }//fin asyncTask


    /**
     * Comprueba si un String es un numero, util para ver si los inserts se han realizado correctamente en el servidor
     * @param str numero en formato string, lo normal es que sea el identificador de un nuevo registro dado de alta en el servidor.
     * @return true si str es un número entero, false en caso contrario.
     */
    private static boolean isInteger(String str)
    {
        try{
            Integer.parseInt(str);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            Log.d("Resultado", "NO NUMERICO");
            return false;
        }
    }

    /**
     * Validacion de los campos del formulario de registro.
     * Obtiene los values del formulario de registro y los valida y empaqueta de manera nombre-valor
     * @return true si los valores del formulario se validan correctamente, falso en caso contrario.
     */
    private boolean validarCampos(){
        View focusView = null;
        View vvalue;
        String svalue;
        //cada vez que validamos tenemos que vaciar los parametros para su correcta introducción
        params.clear();

        //USER-EMAIL
        vvalue = findViewById(R.id.register_Email);
        svalue=((EditText)vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            //bRegister = true;
            return false;
        }
        else if(!isEmailValid(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_invalid_email));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else {
            //valido
            params.add(new BasicNameValuePair("user", svalue));
        }

        //USER-PASSWORD
        vvalue = findViewById(R.id.register_Password);
        svalue = ((EditText)vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else if(!isPasswordValid(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_invalid_password));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else {
            params.add(new BasicNameValuePair("pass", svalue));
        }

        //NAME
        vvalue = findViewById(R.id.register_Name);
        svalue = ((EditText)vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else if(!isNameOrSurnameValid(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_nombre_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else {
            params.add(new BasicNameValuePair("name", svalue));
        }

        //SURNAME
        vvalue = findViewById(R.id.register_Surname);
        svalue = ((EditText)vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else if(!isNameOrSurnameValid(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_nombre_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else {
            params.add(new BasicNameValuePair("surname", svalue));
        }

        //SEX
        params.add(new BasicNameValuePair("sex", ((Spinner)findViewById(R.id.spinner_register_sex)).getSelectedItem().toString()));

        //AGE
        vvalue = findViewById(R.id.register_Age);
        svalue = ((EditText) vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else if(!isAgeValid(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_edad_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else {
            params.add(new BasicNameValuePair("age", svalue));
        }

        //WEIGHT
        vvalue = findViewById(R.id.register_Weight);
        svalue = ((EditText) vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else if(!isWeightValid(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_peso_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else {
            params.add(new BasicNameValuePair("weight", svalue));
        }
        return true;

    }

    private boolean isEmailValid(String email) {
        return (email.contains("@") && email.length()<=40);
    }

    private boolean isPasswordValid(String password) {
        return (password.length() > 4 );
    }

    private boolean isNameOrSurnameValid(String NameSurname){
        if(NameSurname.length() > 40)
            return false;
        return true;
    }

    private boolean isAgeValid(String Age){
        int a;
        try{
            a = Integer.parseInt(Age);
        }
        catch (NumberFormatException e){
            e.printStackTrace();
            return false;
        }
        if(a<1 || a>100)
            return false;
        return true;
    }

    private boolean isWeightValid(String Weight){
        int w;
        try{
            w = Integer.parseInt(Weight);
        }
        catch (NumberFormatException e){
            e.printStackTrace();
            return false;
        }
        if(w<1 || w>300)
            return false;
        return true;
    }


}

package com.tfg.carlos.tfgbike.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.content.Intent;

import com.tfg.carlos.tfgbike.BD.BaseDeDatosLocalOperaciones;
import com.tfg.carlos.tfgbike.R;
import com.tfg.carlos.tfgbike.httpHandlerLogin;
import com.tfg.carlos.tfgbike.httpHandlerQuery;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class Activity_Login extends Activity implements LoaderCallbacks<Cursor> {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private Activity a = this;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    final List<NameValuePair> params1 = new ArrayList<NameValuePair>();
    JSONObject jObjectDatos = null;
    JSONArray jusuario = null;
    JSONArray jbicis = null;
    JSONArray jcomponentes = null;
    JSONArray jactividades = null;
    JSONArray jvideos = null;

    private WebView loginWebView;
    private String error = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        loginWebView = (WebView) findViewById(R.id.Login_webView);
        if(loginWebView !=null){
            portada(null);
        }
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)){
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {

        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {

        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(Activity_Login.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            params1.add(new BasicNameValuePair("user", mEmail));
            params1.add(new BasicNameValuePair("pass", mPassword));

            //attempt authentication against a network service.
            try {
                //llamada al handler que conecta con la base de datos y devuelve un objeto json
                httpHandlerLogin handler = new httpHandlerLogin();
                jObjectDatos = handler.post(params1);
                try{
                    jObjectDatos.getJSONArray("result").getJSONObject(0).getJSONArray("usuario").getJSONObject(0);
                }catch(Exception e){
                    error = "usuarioinexistente";
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if(obtenerUsuario()){
                        //si usuario y contraseña correctas...
                        //Sincronizamos bases de datos
                        sincronizacion(mEmail);
                        // guardamos el resto de datos de usuario
                        if(!guardarDatosUsuario()){
                            return false;
                        }
                }
                else{return false;}
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                //iniciar actividad home
                enterApp();
            } else {
                String mensajeWeb = getString(R.string.error_login_inesperado);
                if(error == null) {
                    mensajeWeb = "<html><head><title>Service Temporarily Unavailable</title></head><body>" +
                            "<h1>Service Temporarily Unavailable</h1><p>The server closed the connection without sending any data.<p>The server is temporarily unable to service your request due to maintenance downtime or capacity problems." +
                            "<p>Due to heavy load on the server, connections may be temporarily blocked from locations that fetch an unusually high number of pages. <p>We apologize for the inconvenience.<hr></body></html>";
                }
                else if(error.equals("pass")) {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }
                else if(error.equals("usuarioinexistente")){
                    mensajeWeb = "<html><head><title>Credenciales incorrectas</title></head><body>Comprueba los datos introducidos o registrate como usuario nuevo.</body></html>";
                }

                portada(mensajeWeb);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * Comprueba que los datos obtenidos en el login son correctos para la autenticacion.
     * @return true si autenticacion correcta, fale en caso contrario.
     * @throws JSONException
     */
    private boolean obtenerUsuario() throws JSONException{

            String emailUser = mEmailView.getText().toString();
            String passUser = mPasswordView.getText().toString();
            //obtener el array de datos de usuario
            jusuario = jObjectDatos.getJSONArray("result").getJSONObject(0).getJSONArray("usuario");
            try {
                if ((jusuario.getJSONObject(0).getString("email")).equalsIgnoreCase(emailUser)) {
                    if ((jusuario.getJSONObject(0).getString("contrasena")).equals(passUser)) {
                        Log.d("SERVICE_LOGIN ", "Credenciales correctos");
                        return true;
                    }
                    else{
                        Log.d("SERVICE_LOGIN", "Contrasena incorrecta, passUser= " + passUser + "passServer= " + jusuario.getJSONObject(0).getString("contrasena"));
                        error = "pass";
                    }
                } else {
                    //Datos recibidos inesperados
                    Log.d("SERVICE_LOGIN ", "Datos recibidos inesperados: " + jusuario.toString());
                }
            }
            catch(Exception e){
                e.printStackTrace();
                return false;
            }

        return false;
    }

    /**
     * Guarda los datos de usuario obtenidos del servidor en la base de datos local.
     * @return true si operación correcta, false en caso contrario.
     * @throws JSONException
     */
    private boolean guardarDatosUsuario() throws JSONException {

        ContentValues values = new ContentValues();
        BaseDeDatosLocalOperaciones bdl = new BaseDeDatosLocalOperaciones(this);
        int i=0;

        //datos de usuario
        values.put("_id", jusuario.getJSONObject(i).getString("_id"));
        values.put("email", jusuario.getJSONObject(i).getString("email"));
        values.put("contrasena", jusuario.getJSONObject(i).getString("contrasena"));
        values.put("nombre", jusuario.getJSONObject(i).getString("nombre"));
        values.put("apellido", jusuario.getJSONObject(i).getString("apellido"));
        values.put("sexo", jusuario.getJSONObject(i).getString("sexo"));
        values.put("edad", jusuario.getJSONObject(i).getString("edad"));
        values.put("peso", jusuario.getJSONObject(i).getString("peso"));

        if(!bdl.replaceBDLocal(values, "usuarios"))
            return false;
        values.clear();

        //cargar el array con los datos de las bicis de usuario
        jbicis = jObjectDatos.getJSONArray("result").getJSONObject(1).getJSONArray("bicis");
        if(jbicis != null) {
            for (i = 0; jbicis.length()> i; i++) {
                values.put("_id", jbicis.getJSONObject(i).getString("_id"));
                values.put("marca", jbicis.getJSONObject(i).getString("marca"));
                values.put("modelo", jbicis.getJSONObject(i).getString("modelo"));
                values.put("tipo", jbicis.getJSONObject(i).getString("tipo"));
                values.put("peso", jbicis.getJSONObject(i).getString("peso"));
                values.put("km", jbicis.getJSONObject(i).getString("km"));
                values.put("precio", jbicis.getJSONObject(i).getString("precio"));
                values.put("gasto", jbicis.getJSONObject(i).getString("gasto"));
                values.put("usuario", jbicis.getJSONObject(i).getString("usuario"));

                //todo registro 1 a 1, mejorarlo a todo un replace a la vez
                if (!bdl.replaceBDLocal(values, "bicis"))
                    Log.i("LOGIN guardar bicis", "error en replace, el indice vale: " + String.valueOf(i));
                values.clear();
            }
        }

        jcomponentes = jObjectDatos.getJSONArray("result").getJSONObject(2).getJSONArray("componentes");
        if(jcomponentes != null) {
            for(i=0; jcomponentes.length()>i; i++) {
                values.put("_id", jcomponentes.getJSONObject(i).getString("_id"));
                values.put("marca", jcomponentes.getJSONObject(i).getString("marca"));
                values.put("modelo", jcomponentes.getJSONObject(i).getString("modelo"));
                values.put("peso", jcomponentes.getJSONObject(i).getString("peso"));
                values.put("tipo", jcomponentes.getJSONObject(i).getString("tipo"));
                values.put("fecha", jcomponentes.getJSONObject(i).getString("fecha"));
                values.put("estado", jcomponentes.getJSONObject(i).getString("estado"));
                values.put("precio", jcomponentes.getJSONObject(i).getString("precio"));
                values.put("notas", jcomponentes.getJSONObject(i).getString("notas"));
                values.put("gasto", jcomponentes.getJSONObject(i).getString("gasto"));
                values.put("idbici", jcomponentes.getJSONObject(i).getString("idbici"));
                values.put("usuario", jcomponentes.getJSONObject(i).getString("usuario"));

                if(!bdl.replaceBDLocal(values, "componentes"))
                    Log.i("LOGIN ","guardar componentes error en replace, el indice vale:" + String.valueOf(i));
                values.clear();
            }
        }

        jactividades = jObjectDatos.getJSONArray("result").getJSONObject(3).getJSONArray("actividades");
        if(jactividades != null) {
            for(i=0; jactividades.length()>i; i++) {
                values.put("_id", jactividades.getJSONObject(i).getString("_id"));
                values.put("usuario", jactividades.getJSONObject(i).getString("usuario"));
                values.put("idbici", jactividades.getJSONObject(i).getString("idbici"));
                values.put("date", jactividades.getJSONObject(i).getString("date"));
                values.put("datosRuta", jactividades.getJSONObject(i).getString("datosRuta"));

                if(!bdl.replaceBDLocal(values, "actividades"))
                    Log.i("LOGIN ","guardar actividades error en replace, el indice vale:" + String.valueOf(i));
                values.clear();
            }
        }

        jvideos = jObjectDatos.getJSONArray("result").getJSONObject(4).getJSONArray("videos");
        if(jvideos != null) {
            for(i=0; jvideos.length()>i; i++) {
                values.put("_id", jvideos.getJSONObject(i).getString("_id"));
                values.put("tipocomponente", jvideos.getJSONObject(i).getString("tipocomponente"));
                values.put("Instalar", jvideos.getJSONObject(i).getString("Instalar"));
                values.put("Eliminar", jvideos.getJSONObject(i).getString("Eliminar"));
                values.put("Mantenimiento", jvideos.getJSONObject(i).getString("Mantenimiento"));

                if(!bdl.replaceBDLocal(values, "videos"))
                    Log.i("LOGIN ","guardar videos error en replace, el indice vale:" + String.valueOf(i));
                values.clear();
            }
        }

        return true;
    }

    /**
     * Entra en la zona de la aplicación de usuarios registrados.
     */
    private void enterApp(){
        Intent intent = new Intent(this, Activity_Navigation.class);
        //paso de parametros
        intent.putExtra("loginUser", ((EditText)findViewById(R.id.email)).getText().toString());
        startActivity(intent);
    }

    /**
     * Cierra la App
     */
    public void onClickToExitApp(View view){
        //matar pid de aplicacion
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

    /**
     * Entra en la aplicación de manera local siempre y cuando
     * se haya hecho un login en el servidor alguna vez y en el dispositivo haya ya datos descargados del usuario.
     *
     */
    public void onClickEnterAppNoServer(View view){
        //comprobar que el campo email no esté vacio y si hay información del usuario dado en la bd local
        String email = mEmailView.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));

        } else {
            BaseDeDatosLocalOperaciones bd = new BaseDeDatosLocalOperaciones(this);
            ArrayList<ContentValues> result = bd.selectBDLocal("SELECT _id FROM usuarios WHERE email='"+email+"'");
            if(result.size()!=0){
                enterApp();
            }
            else{
                portada("<html><head><title>No hay informacion local del usuario</title></head><body>Comprueba los datos introducidos o haz login.</body></html>");
            }
        }
    }

    //boton de registro
    public void onClickButtonRegister(View view){
        Intent intent = new Intent(this, Activity_Register.class);
        startActivity(intent);
    }

    /**
     * Inicializa el webView de la pantalla login.
     * @param URL String a cargar en el webView
     */
    private void portada(String URL){
        if(URL == null) {
            loginWebView.setBackgroundColor(Color.TRANSPARENT);
            loginWebView.setBackgroundResource(R.drawable.urbike_portada);
        }else {
            loginWebView.setBackgroundColor(Color.WHITE);
            loginWebView.loadData(URL, null, null);
            loginWebView.setWebChromeClient(new WebChromeClient());
        }
    }

    /**
     * Efectua la sincronización entre la base de datos local y la base de datos del servidor.
     * Actualiza en el servidor, los posibles cambios que se hayan efectuadon en la base de datos local si estaba sin conexión.
     * @param email String con el identificador de usuario cuyos datos queremos sincronizar.
     */
    private void sincronizacion(String email){
        String loginUser = email;
        String query = "";
        ArrayList<ContentValues> registrosSincroUsuario;
        ArrayList<ContentValues> registrosSincroBicis;
        ArrayList<ContentValues> registrosSincroComponentes;
        ArrayList<ContentValues> registrosSincroActividades;
        BaseDeDatosLocalOperaciones bdsincro = new BaseDeDatosLocalOperaciones(this);

        //busqueda para insertar nuevos registros
        registrosSincroComponentes = bdsincro.selectBDLocal("SELECT * FROM componentes WHERE usuario='" + loginUser +"' AND nuevo=" + 1);
        registrosSincroActividades = bdsincro.selectBDLocal("SELECT * FROM actividades WHERE usuario='" + loginUser +"' AND nuevo=" + 1);
        if(!(registrosSincroComponentes.isEmpty() && registrosSincroActividades.isEmpty())){
            if (!registrosSincroComponentes.isEmpty()) {
                for (int i = 0; i < registrosSincroComponentes.size(); i++) {
                    query += "INSERT INTO componentes (marca, modelo, peso, tipo, fecha, estado, usuario, idbici, precio, gasto, notas) VALUES ";
                    query += "('" + registrosSincroComponentes.get(i).getAsString("marca") +
                            "','" + registrosSincroComponentes.get(i).getAsString("modelo") +
                            "'," + registrosSincroComponentes.get(i).getAsString("peso") +
                            ",'" + registrosSincroComponentes.get(i).getAsString("tipo") +
                            "','" + registrosSincroComponentes.get(i).getAsString("fecha") +
                            "','" + registrosSincroComponentes.get(i).getAsString("estado") +
                            "','" + registrosSincroComponentes.get(i).getAsString("usuario") +
                            "'," + registrosSincroComponentes.get(i).getAsString("idbici") +
                            "," + registrosSincroComponentes.get(i).getAsString("precio") +
                            "," + registrosSincroComponentes.get(i).getAsString("gasto") +
                            ",'" + registrosSincroComponentes.get(i).getAsString("notas") + "');\n";
                }
            }
            if (!registrosSincroActividades.isEmpty()) {
                for (int i = 0; i < registrosSincroActividades.size(); i++) {
                    query += "INSERT INTO actividades (usuario, idbici, date, datosRuta) VALUES ";
                    query += "('" + registrosSincroActividades.get(i).getAsString("usuario") +
                            "'," + registrosSincroActividades.get(i).getAsString("idbici") +
                            "," + registrosSincroActividades.get(i).getAsString("date") +
                            ",\"" + registrosSincroActividades.get(i).getAsString("datosRuta") + "\");\n";
                }
            }
            Log.d("LOGIN SINCRO INSERT",query);
            httpHandlerQuery sincroInsert = new httpHandlerQuery();
            String resultado = sincroInsert.post(query);
            if(resultado.equals("done")){
                //en bd local poner los flags de nuevo a 0
                if (bdsincro.queryBDLocal("UPDATE componentes SET nuevo=0, needUpdate=0 WHERE nuevo=1") &&
                    bdsincro.queryBDLocal("UPDATE actividades SET nuevo=0, needUpdate=0 WHERE nuevo=1")){
                    Log.d("Login SINCRO", "Sincronizado nuevos registros");
                }else {
                    Log.d("Login SINCRO", "Error reiniciando flags en local al sincronizar nuevos registros");
                }
            }
        }
        registrosSincroComponentes.clear();
        registrosSincroActividades.clear();
        query="";

        //busqueda para actualizar registros
        registrosSincroUsuario = bdsincro.selectBDLocal("SELECT _id FROM usuarios WHERE email='" + loginUser +"' AND needUpdate=" + 1);
        registrosSincroBicis = bdsincro.selectBDLocal("SELECT _id FROM bicis WHERE usuario='" + loginUser +"' AND needUpdate=" + 1);
        registrosSincroComponentes = bdsincro.selectBDLocal("SELECT _id FROM componentes WHERE usuario='" + loginUser +"' AND needUpdate=" + 1);
        registrosSincroActividades = bdsincro.selectBDLocal("SELECT _id FROM actividades WHERE usuario='" + loginUser +"' AND needUpdate=" + 1);
        if(!(registrosSincroComponentes.isEmpty() && registrosSincroActividades.isEmpty() && registrosSincroBicis.isEmpty() && registrosSincroUsuario.isEmpty())) {
            if (!registrosSincroUsuario.isEmpty()) {
                for (int i = 0; i < registrosSincroBicis.size(); i++) {
                    query += "UPDATE usuarios SET ";
                    query += "email='" + registrosSincroUsuario.get(i).getAsString("email") +
                            "',password='" + registrosSincroUsuario.get(i).getAsString("password") +
                            "',nombre='" + registrosSincroUsuario.get(i).getAsString("nombre") +
                            "',apellido='" + registrosSincroUsuario.get(i).getAsString("apellido") +
                            "',sexo='" + registrosSincroUsuario.get(i).getAsString("sexo") +
                            "',edad=" + registrosSincroUsuario.get(i).getAsString("edad") +
                            ",peso=" + registrosSincroUsuario.get(i).getAsString("peso") +
                            "WHERE _id=" + registrosSincroUsuario.get(i).getAsString("_id") + ";\n";
                }
            }
            if (!registrosSincroBicis.isEmpty()) {
                for (int i = 0; i < registrosSincroBicis.size(); i++) {
                    query += "UPDATE bicis SET ";
                    query += "marca='" + registrosSincroBicis.get(i).getAsString("marca") +
                            "',modelo='" + registrosSincroBicis.get(i).getAsString("modelo") +
                            "',peso=" + registrosSincroBicis.get(i).getAsString("peso") +
                            ",tipo='" + registrosSincroBicis.get(i).getAsString("tipo") +
                            "',usuario='" + registrosSincroBicis.get(i).getAsString("usuario") +
                            "',km=" + registrosSincroBicis.get(i).getAsString("km") +
                            ",precio=" + registrosSincroBicis.get(i).getAsString("precio") +
                            ",gasto=" + registrosSincroBicis.get(i).getAsString("gasto") +
                            "WHERE _id=" + registrosSincroBicis.get(i).getAsString("_id") + ";\n";
                }
            }
            if (!registrosSincroComponentes.isEmpty()) {
                for (int i = 0; i < registrosSincroComponentes.size(); i++) {
                    query += "UPDATE componentes SET ";
                    query += "marca='" + registrosSincroComponentes.get(i).getAsString("marca") +
                            "',modelo='" + registrosSincroComponentes.get(i).getAsString("modelo") +
                            "',peso=" + registrosSincroComponentes.get(i).getAsString("peso") +
                            ",tipo='" + registrosSincroComponentes.get(i).getAsString("tipo") +
                            "',fecha='" + registrosSincroComponentes.get(i).getAsString("fecha") +
                            "',estado='" + registrosSincroComponentes.get(i).getAsString("estado") +
                            "',usuario='" + registrosSincroComponentes.get(i).getAsString("usuario") +
                            "',idbici=" + registrosSincroComponentes.get(i).getAsString("idbici") +
                            ",precio=" + registrosSincroComponentes.get(i).getAsString("precio") +
                            ",gasto=" + registrosSincroComponentes.get(i).getAsString("gasto") +
                            ",notas='" + registrosSincroComponentes.get(i).getAsString("notas") +
                            "WHERE _id=" + registrosSincroComponentes.get(i).getAsString("_id") + ";\n";
                }
            }
            if (!registrosSincroActividades.isEmpty()) {
                for (int i = 0; i < registrosSincroActividades.size(); i++) {
                    query += "UPDATE actividades SET ";
                    query += "usuario='" + registrosSincroActividades.get(i).getAsString("usuario") +
                            "',idbici=" + registrosSincroActividades.get(i).getAsString("idbici") +
                            ",date=" + registrosSincroActividades.get(i).getAsString("date") +
                            ",datosRuta=\"" + registrosSincroActividades.get(i).getAsString("datosRuta") + "\"" +
                            "WHERE _id=" + registrosSincroActividades.get(i).getAsString("_id") + ";\n";
                }
            }
            Log.d("LOGIN SINCRO UPDATE",query);
            httpHandlerQuery sincroUpdate = new httpHandlerQuery();
            String resultado = sincroUpdate.post(query);
            if(resultado.equals("done")){
                //en local poner los flags de nuevo a 0
                if (bdsincro.queryBDLocal("UPDATE usuarios SET needUpdate=0 WHERE nuevo=1") &&
                        bdsincro.queryBDLocal("UPDATE bicis SET needUpdate=0 WHERE nuevo=1") &&
                        bdsincro.queryBDLocal("UPDATE componentes SET needUpdate=0 WHERE nuevo=1") &&
                        bdsincro.queryBDLocal("UPDATE actividades SET needUpdate=0 WHERE nuevo=1")){
                    Log.d("Login SINCRO", "Sincronizado nuevos registros");
                }else {
                    Log.d("Login SINCRO", "Error reiniciando flags en local al sincronizar nuevos registros");
                }
            }
        }

    }
}




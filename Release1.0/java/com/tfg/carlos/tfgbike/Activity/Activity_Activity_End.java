package com.tfg.carlos.tfgbike.Activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tfg.carlos.tfgbike.BD.BaseDeDatosLocalOperaciones;
import com.tfg.carlos.tfgbike.R;
import com.tfg.carlos.tfgbike.httpHandlerInsert;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.facebook.FacebookSdk;

public class Activity_Activity_End extends ActionBarActivity {

    List<HashMap<String, String>> trip = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> tripStartHash = new HashMap<>();
    HashMap<String, String> tripEndHash =  new HashMap<>();
    private static String loginUser;
    private static String biciUser;
    String idb;
    File file;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    PolylineOptions path;
    boolean mapExpandido = false;

    TextView txtBici;
    TextView txtDistancia;
    TextView txtVelocidad;
    TextView txtTiempo;
    TextView txtTiempoIni;
    TextView txtTiempoFin;
    TextView txtAltitudRelativa;
    TextView txtCalorias;

    Context context;

    CallbackManager callbackManager;
    ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_end);
        context = this;

        //recogemos los parametros pasados
        Bundle b = getIntent().getExtras();
        if(b!=null) {
            loginUser = b.getString("loginUser");
            biciUser = b.getString("biciUser");
        }
        //asignamos los textviews
        txtBici = (TextView) findViewById(R.id.text_activity_end_bici);
        txtDistancia = (TextView) findViewById(R.id.text_activity_end_distancia);
        txtVelocidad = (TextView) findViewById(R.id.text_activity_end_velociad);
        txtTiempoIni = (TextView) findViewById(R.id.text_activity_end_ini);
        txtTiempoFin = (TextView) findViewById(R.id.text_activity_end_fin);
        txtTiempo = (TextView) findViewById(R.id.text_activity_end_tiempo);
        txtAltitudRelativa = (TextView) findViewById(R.id.text_activity_end_altitud_relativa);
        txtCalorias = (TextView) findViewById(R.id.text_activity_end_calorias);

        //inicializar mapa
        setUpMapIfNeeded();

        //cargar resultados de la actividad o trip
        try {
            parseXML();
        }catch (Exception e){}

        //dibujar en el mapa la ruta
        dibujarRuta();
        //mostrar los datos de la actividad al usuario
        mostrarDatos();

        //boton guardar actividad
        Button bGuardar = (Button) findViewById(R.id.b_activity_end_guardar);
        bGuardar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GuardarActividadTask gat = new GuardarActividadTask();
                gat.execute();
            }
        });
        //boton descartar actividad si no se desea guardar
        Button bDescartar = (Button) findViewById(R.id.b_activity_end_descartar);
        bDescartar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //dialogo confirmacion
                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //mostrar mensaje de actividad descartada
                                Toast.makeText(context, "Actividad descartada", Toast.LENGTH_LONG).show();
                                //volver a home destruyendo esta actividad
                                finish();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.dialog_confirm).setPositiveButton(R.string.dialog_yes, dialogClickListener)
                        .setNegativeButton(R.string.dialog_no, dialogClickListener).show();
            }
        });

        //boton compartir actividad
        Button bCompartir = (Button) findViewById(R.id.b_activity_end_share);
        bCompartir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //dialogo confirmacion
                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //todo permitir compartir en redes sociales el resumen de la actividad
                                compartirActividadFB();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.dialog_confirm).setPositiveButton(R.string.dialog_yes, dialogClickListener)
                        .setNegativeButton(R.string.dialog_no, dialogClickListener).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Inicializa el mapa Google
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the MapFragment.
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
    private void setUpMap() {
        //inicializar polyline para el dibujado del path
        path = new PolylineOptions();
        path.color(Color.parseColor("#CC0000FF"));
        path.width(5);
        path.visible(true);
        path.geodesic(true);
    }

    //PARSER XML
    // no usar namespaces (espacio de nombres)
    private static final String ns = null;
    public void parseXML(){
        try{
            file = new File(this.getFilesDir(),"Resultados_Actividad");
            if(file.exists()) {
                InputStream in = new FileInputStream(file);
                parse(in);
            }
            else{Log.d("ACTIVITY END","archivo no encontrado");}
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void parse(InputStream in) throws XmlPullParserException, IOException {
        Log.d("Activity END", "PARSE");
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readTrip(parser);
        } finally {
            in.close();
        }
    }

    /**
     * parsea y empaqueta todos los waypoints o puntos de ruta y su informacion asociada
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private List<HashMap<String, String>> readTrip(XmlPullParser parser) throws XmlPullParserException, IOException {


        parser.require(XmlPullParser.START_TAG, ns, "trip");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("waypoint")) {
                //anadir la informacion del waypoint a la lista
                trip.add(readWaypoint(parser));
            } else if (name.equals("tripstart")) {
                readTripStart(parser);
            } else if(name.equals("tripend")) {
                readTripEnd(parser);
            } else{
                //skip parser
                int depth = 1;
                while (depth != 0) {
                    switch (parser.next()) {
                        case XmlPullParser.END_TAG:
                            depth--;
                            break;
                        case XmlPullParser.START_TAG:
                            depth++;
                            break;
                    }
                }
            }
        }
        return trip;
    }

    /**
     * Parsea y empaqueta la información del inicio de la actividad.
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readTripStart(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d("Activity END", "READTripStart");

        tripStartHash.put("idbici", biciUser);
        tripStartHash.put("usuario", loginUser);

        parser.require(XmlPullParser.START_TAG, ns, "tripstart");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            //segun que etiqueta sea, metemos el valor en la variable adecuada
            if (name.equals("dateini")) {
                if (parser.next() == XmlPullParser.TEXT) {
                    tripStartHash.put("dateini", parser.getText());
                    parser.nextTag();
                }
            } else {
                //skip parser
                int depth = 1;
                while (depth != 0) {
                    switch (parser.next()) {
                        case XmlPullParser.END_TAG:
                            depth--;
                            break;
                        case XmlPullParser.START_TAG:
                            depth++;
                            break;
                    }
                }
            }
        }
    }

    /**
     * Parsea y empaqueta la información del fin de la actividad.
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readTripEnd(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d("Activity END", "READTripEnd");

        parser.require(XmlPullParser.START_TAG, ns, "tripend");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            //segun que etiqueta sea, metemos el valor en la variable adecuada
            if (name.equals("datefin")) {
                if (parser.next() == XmlPullParser.TEXT) {
                    tripEndHash.put("datefin", parser.getText());
                    parser.nextTag();
                }
            } else if (name.equals("tutilizado")) {
                if (parser.next() == XmlPullParser.TEXT) {
                    tripEndHash.put("tutilizado", parser.getText());
                    parser.nextTag();
                }
            } else if (name.equals("velmedia")) {
                if (parser.next() == XmlPullParser.TEXT) {
                    tripEndHash.put("velmedia", parser.getText());
                    parser.nextTag();
                }
            } else if (name.equals("distanciatotal")) {
                if (parser.next() == XmlPullParser.TEXT) {
                    tripEndHash.put("distanciatotal", parser.getText());
                    parser.nextTag();
                }
            } else if (name.equals("calorias")) {
                if (parser.next() == XmlPullParser.TEXT) {
                    tripEndHash.put("calorias", parser.getText());
                    parser.nextTag();
                }
            } else if (name.equals("desnivel")) {
                if (parser.next() == XmlPullParser.TEXT) {
                    tripEndHash.put("desnivel", parser.getText());
                    parser.nextTag();
                }
            } else{
                //skip parser
                int depth = 1;
                while (depth != 0) {
                    switch (parser.next()) {
                        case XmlPullParser.END_TAG:
                            depth--;
                            break;
                        case XmlPullParser.START_TAG:
                            depth++;
                            break;
                    }
                }
            }
        }
    }

    /**
     * Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
     * to their respective "read" methods for processing. Otherwise, skips the tag.
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private HashMap<String, String> readWaypoint(XmlPullParser parser) throws XmlPullParserException, IOException {
        HashMap<String, String> waypointHash =  new HashMap<>();

        parser.require(XmlPullParser.START_TAG, ns, "waypoint");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            //segun que etiqueta sea, metemos el valor en la variable adecuada
            if (name.equals("lat")) {
                if (parser.next() == XmlPullParser.TEXT) {
                    waypointHash.put("lat",parser.getText());
                    parser.nextTag();
                }
            }
            else if (name.equals("lng")){
                if (parser.next() == XmlPullParser.TEXT) {
                    waypointHash.put("lng",parser.getText());
                    parser.nextTag();
                }
            }
            else if (name.equals("velocidad")){
                if (parser.next() == XmlPullParser.TEXT) {
                    waypointHash.put("velocidad",parser.getText());
                    parser.nextTag();
                }
            }
            else if (name.equals("altitud")){
                if (parser.next() == XmlPullParser.TEXT) {
                    waypointHash.put("altitud",parser.getText());
                    parser.nextTag();
                }
            }
            else if (name.equals("altitudrelativa")){
                if (parser.next() == XmlPullParser.TEXT) {
                    waypointHash.put("altitudrelativa",parser.getText());
                    parser.nextTag();
                }
            }
            else if (name.equals("distancia")){
                if (parser.next() == XmlPullParser.TEXT) {
                    waypointHash.put("distancia",parser.getText());
                    parser.nextTag();
                }
            }
            else if (name.equals("distanciatotal")){
                if (parser.next() == XmlPullParser.TEXT) {
                    waypointHash.put("distanciatotal",parser.getText());
                    parser.nextTag();
                }
            }
            else if (name.equals("tiempo")){
                if (parser.next() == XmlPullParser.TEXT) {
                    waypointHash.put("tiempo",parser.getText());
                    parser.nextTag();
                }
            }
            else if (name.equals("pendiente")){
                if (parser.next() == XmlPullParser.TEXT) {
                    waypointHash.put("pendiente",parser.getText());
                    parser.nextTag();
                }
            }
            else{
                //skip parser
                int depth = 1;
                while (depth != 0) {
                    switch (parser.next()) {
                        case XmlPullParser.END_TAG:
                            depth--;
                            break;
                        case XmlPullParser.START_TAG:
                            depth++;
                            break;
                    }
                }
            }
        }
        return waypointHash;
    }

    /**
     * Dibuja la ruta en el mapa
     */
    private void dibujarRuta(){
        Log.d("Activity END", "CargaRuta");
        LatLng nodo = null;
        int i=0;
        if ( mMap == null )
            return;

        //inicio
        nodo = new LatLng(Double.parseDouble(trip.get(i).get("lat")),Double.parseDouble(trip.get(i).get("lng")));
        mMap.addMarker(new MarkerOptions().position(nodo).title("Inicio"));

        for (i=1; trip.size()>i; i++){
            nodo = new LatLng(Double.parseDouble(trip.get(i).get("lat")),Double.parseDouble(trip.get(i).get("lng")));
            path.add(nodo);
        }
        //fin
        mMap.addMarker(new MarkerOptions().position(nodo).title("Fin"));

        mMap.addPolyline(path);
        //zoom a la ruta
        if(nodo!=null)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nodo, 13));

    }

    /**
     * Rellena la UI con datos resumen de la actividad
     */
    private void mostrarDatos(){
        txtBici.setText(biciUser);
        txtDistancia.setText(tripEndHash.get("distanciatotal"));
        txtVelocidad.setText(tripEndHash.get("velmedia"));
        txtTiempoIni.setText(tripStartHash.get("dateini"));
        txtTiempoFin.setText(tripEndHash.get("datefin"));
        txtTiempo.setText(tripEndHash.get("tutilizado"));
        txtAltitudRelativa.setText(tripEndHash.get("desnivel"));
        txtCalorias.setText(tripEndHash.get("calorias"));
    }

    /**
     * Hilo asíncrono que guarda en bd y server los datos de la actividad.
     * Muestra un mensaje con el resultado de la operación.
     */
    public class GuardarActividadTask extends AsyncTask<Void, Void, Boolean> {
        String message = "Error";
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                //leer el archivo de los datos de la actividad o trip y volcarlo a un string para guararlo en la base de datos
                String tripData = archivoToString();
                //id de bici utilizada
                BaseDeDatosLocalOperaciones bd = new BaseDeDatosLocalOperaciones(context);
                String query = "SELECT _id FROM bicis WHERE modelo='" + biciUser + "' AND usuario='" + loginUser + "'";
                ArrayList<ContentValues> idbici = bd.selectBDLocal(query);
                idb = idbici.get(0).getAsString("_id");

                //quitar la hora y dejar solo fecha de calendario
                String date=tripStartHash.get("dateini").substring(0, tripStartHash.get("dateini").lastIndexOf(" "));

                //preparamos la query para guardar la actividad en las bd
                query = "INSERT INTO actividades (usuario, idbici, date, datosRuta) " +
                        "VALUES ('" + loginUser + "'," + idb + ",'" + date + "',\"" + tripData + "\")";

                //alta en bd servidor
                httpHandlerInsert insert = new httpHandlerInsert();
                String resultado = insert.post(query);
                if (isInteger(resultado)) {
                    query = "INSERT INTO actividades (_id, usuario, idbici, date, datosRuta) " +
                            "VALUES (" + resultado + ",'" + loginUser + "'," + idb + ",'" + date + "',\"" + tripData + "\")";
                    message = getString(R.string.msg_actividad_agregada_ok);
                }
                else { //query para bd local con el flag nuevo a 1 para sincro
                    query = "INSERT INTO actividades (usuario, idbici, date, datosRuta, nuevo) " +
                            "VALUES ('" + loginUser + "'," + idb + ",'" + date + "',\"" + tripData + "\"," + 1 + ")";
                    message = getString(R.string.msg_datos_agregados_badServer);
                }
                //alta en bd local
                BaseDeDatosLocalOperaciones bdlocal = new BaseDeDatosLocalOperaciones(context);
                bdlocal.queryBDLocal(query);
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success){
            if (success) {
                //renombre de archivo a fecha en la que se inicio la actividad o trip
                archivoRenombre(tripStartHash.get("dateini"));

                //actualizar km de bici (solo en local, ya se sincronizara con el servidor cuando se haga login)
                BaseDeDatosLocalOperaciones bd = new BaseDeDatosLocalOperaciones(context);
                //Obtener los km que tiene la bici
                ArrayList<ContentValues> result = bd.selectBDLocal("SELECT km FROM bicis WHERE _id=" + idb);
                Double km = result.get(0).getAsDouble("km");
                //sumar los km de esta actividad
                km += Double.parseDouble(tripEndHash.get("distanciatotal"));
                //actualizar km
                ContentValues valuesb = new ContentValues();
                valuesb.put("km", km);
                Log.d("VALUES", valuesb.get("km").toString());
                valuesb.put("needUpdate", 1);
                bd.updateBDLocal("bicis", valuesb, "_id=" + idb, null);

                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                finish();
            }
            else
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Renombra el archivo de la actividad a la fecha de inicio de esta.
     * Lo guarda en el directorio de la aplicación y en la subcarpeta /Actividades/loginUser
     * @param nombre String que corresponde con la fecha del inicio de la actividad.
     */
    private void archivoRenombre(String nombre){
        try {
            File destino = new File(this.getFilesDir() + "/Actividades/" + loginUser, nombre);
            file.renameTo(destino);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String archivoToString(){
        String resultado="";
        try {
            File file = new File(this.getFilesDir(),"Resultados_Actividad");
            if(file.exists()) {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                resultado = sb.toString();
                br.close();
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return resultado;
    }

    /**
     * Comparte datos de la actividad en la red social Facebook
     */
    private void compartirActividadFB(){
        // Initialize the SDK before executing any other operations, especially, if you're using Facebook UI elements.
        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Urbike Actividad")
                    .setContentDescription("Has recorrido "+ txtDistancia.getText() +
                            " km en " + txtTiempo.getText() + " y con un desnivel de " +
                            txtAltitudRelativa.getText() +" metros")
                    .setContentUrl(Uri.parse("http://vidalbodeloncarlos.xyz/"))
                    .build();

            shareDialog.show(linkContent);
        }
    }
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * boton expandir mapa
     * @param view
     */
    public void OnClickButtonExpandMap_ActivityEnd(View view){
        LinearLayout maplayout = (LinearLayout) findViewById(R.id.layoutMap);
        PopupWindow popUp = new PopupWindow(this);
        if(mapExpandido == false){
            popUp.showAtLocation(maplayout, Gravity.BOTTOM, 10, 10);
            popUp.update(50, 50, 300, 80);
            mapExpandido = true;
        }
        else if(mapExpandido == true){
            popUp.dismiss();
            mapExpandido = false;
        }
    }

    /**
     * boton cambiar estilo de mapa
     * @param view
     */
    public void OnClickButtonCambiarMap_ActivityEnd(View view){
        if(mMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID)
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

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
}

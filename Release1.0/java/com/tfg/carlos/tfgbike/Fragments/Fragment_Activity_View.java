package com.tfg.carlos.tfgbike.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tfg.carlos.tfgbike.Activity.Activity_Navigation;
import com.tfg.carlos.tfgbike.BD.BaseDeDatosLocalOperaciones;
import com.tfg.carlos.tfgbike.R;
import com.tfg.carlos.tfgbike.httpHandlerQuery;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Activity_View.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Activity_View#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Activity_View extends Fragment {

    List<HashMap<String, String>> trip = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> tripStartHash = new HashMap<>();
    HashMap<String, String> tripEndHash =  new HashMap<>();
    ArrayList<ContentValues> actividad;
    private static String loginUser;
    private static String biciUser;
    private static String idActividad;
    Context context;

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

    //facebook share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    private View fragmentview;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Activity_View.
     */
    public static Fragment_Activity_View newInstance(String param1, String param2) {
        Fragment_Activity_View fragment = new Fragment_Activity_View();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_Activity_View() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentview = inflater.inflate(R.layout.fragment_activity_view, container, false);

        context = getActivity();

        //asignamos los textviews
        txtBici = (TextView) fragmentview.findViewById(R.id.text_activity_view_bici);
        txtDistancia = (TextView) fragmentview.findViewById(R.id.text_activity_view_distancia);
        txtVelocidad = (TextView) fragmentview.findViewById(R.id.text_activity_view_velociad);
        txtTiempoIni = (TextView) fragmentview.findViewById(R.id.text_activity_view_ini);
        txtTiempoFin = (TextView) fragmentview.findViewById(R.id.text_activity_view_fin);
        txtTiempo = (TextView) fragmentview.findViewById(R.id.text_activity_view_tiempo);
        txtAltitudRelativa = (TextView) fragmentview.findViewById(R.id.text_activity_view_altitud_relativa);
        txtCalorias = (TextView) fragmentview.findViewById(R.id.text_activity_view_calorias);

        //inicializar mapa
        setUpMapIfNeeded();

        //cargar resultados de la actividad o trip
        cargaActividad();

        //boton descartar actividad si no se desea guardar
        Button bDescartar = (Button) fragmentview.findViewById(R.id.b_activity_view_eliminar);
        bDescartar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //dialogo confirmacion
                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //eliminar actividad y volver al fragment activity
                                EliminarActividadTask eat = new EliminarActividadTask();
                                eat.execute();
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

        //boton expandir mapa
        Button bexpandMap = (Button) fragmentview.findViewById(R.id.b_activity_view_fullScreen);
        bexpandMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });

        //boton cambiar estilo de mapa
        Button bcambiarMap = (Button) fragmentview.findViewById(R.id.b_activity_view_chage_map);
        bcambiarMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID)
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                else
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });

        //boton compartir actividad
        Button bShare = (Button) fragmentview.findViewById(R.id.b_activity_view_share);
        bShare.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                compartirActividadFB();
            }
        });

        return fragmentview;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            ((Activity_Navigation) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    //PARA NAVIGATION
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static Fragment_Activity_View newInstance(int sectionNumber, String id){

        Fragment_Activity_View fragment = new Fragment_Activity_View();
        //para pasarle mas argumentos al drawer
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        idActividad=id;

        return fragment;
    }

    /**
     * Inicializa el mapa de google.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the MapFragment.
            mMap = ((MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if(mMap!=null)
                setUpMap();
        }
    }

    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //inicializar polyline para el dibujado del path
        path = new PolylineOptions();
        path.color(Color.parseColor("#CC0000FF"));
        path.width(5);
        path.visible(true);
        path.geodesic(true);
    }

    /**
     * Carga en el mapa la ruta realizada.
     * Para ello lee el fichero con los datos de la actividad.
     */
    private void cargaActividad(){
        BaseDeDatosLocalOperaciones bd = new BaseDeDatosLocalOperaciones(context);
        actividad = bd.selectBDLocal("SELECT * FROM actividades WHERE _id=" + idActividad);
        if(actividad!=null) {
            loginUser = actividad.get(0).getAsString("usuario");
            biciUser = actividad.get(0).getAsString("idbici");
            parseXML(comprobarFichero(actividad.get(0).getAsString("usuario"), actividad.get(0).getAsString("date")));
            //dibujar en el mapa la ruta
            dibujarRuta();
            //mostrar los datos de la actividad al usuario
            mostrarDatos();
        }
    }

    /**
     * Comprueba que el fichero de la actividad existe.
     * @param usuario String del identificador de usuario que realizó la actividad.
     * @param dateActividad String con la fecha de inicio de la actividad
     * que sirve en este caso como nombre del fichero en el que se guardo los datos de la actividad.
     * @return File del fichero donde se guardaron los datos de la actividad.
     */
    private File comprobarFichero(String usuario, String dateActividad){
        File path= new File(context.getFilesDir() + "/Actividades/" + usuario);
        File file = new File(path, dateActividad);
        //si por lo que sea no existe crear el fichero a partir de datosRuta de la actividad
        if (!file.exists()) {
            String content = actividad.get(0).getAsString("datosRuta");
            try {
                Log.d("AcitivtyVIew", "fichero No existe");
                path.mkdirs();
                file.createNewFile();
                //cargar en fichero el datosRuta de la actividad
                FileWriter writer = new FileWriter(file);
                writer.append(content);
                writer.flush();
                writer.close();
            }catch (Exception e){e.printStackTrace();}
        }
        else{
            Log.d("AcitivtyVIew", "fichero existente");
        }
        return file;
    }

//PARSER XML
    // no usar namespaces (espacio de nombres)
    private static final String ns = null;
    public void parseXML(File file){
        try{
            if(file.exists()) {
                InputStream in = new FileInputStream(file);
                parse(in);
            }
            else{
                Log.d("ACTIVITY END", "archivo no encontrado");}
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
     * Parsea y empaqueta todos los waypoints o puntos de ruta y su informacion asociada.
     * @param parser
     * @return Lista con los datos de todos los waypoints de la ruta.
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
        //Log.d("Activity END", "READTripStart");

        tripStartHash.put("idbici", actividad.get(0).getAsString("idbici"));

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
     *   to their respective "read" methods for processing. Otherwise, skips the tag.
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
     * Dibuja la ruta en el mapa Google.
     *
     */
    private void dibujarRuta(){
        Log.d("Activity END", "dibujarRuta");
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
     * Rellena la UI con datos de la actividad.
     *
     */
    private void mostrarDatos(){
        txtBici.setText(idToModeloBici(biciUser));
        //distancia en km
        Double distancia = Math.rint(Double.parseDouble(tripEndHash.get("distanciatotal"))*100)/100;
        txtDistancia.setText(distancia.toString());
        //vel media
        Double velmedia = Math.rint(Double.parseDouble(tripEndHash.get("velmedia"))*100)/100;
        txtVelocidad.setText(velmedia.toString());
        txtTiempoIni.setText(tripStartHash.get("dateini"));
        txtTiempoFin.setText(tripEndHash.get("datefin"));
        //tiempo utilizado en seg
        Double tiempo = Double.parseDouble(tripEndHash.get("tutilizado"));
        tiempo = Math.rint(tiempo)/1000;
        txtTiempo.setText(tiempo.toString());
        txtAltitudRelativa.setText(tripEndHash.get("desnivel"));
        txtCalorias.setText(tripEndHash.get("calorias"));
    }


    /**
     * Hilo asíncrono que elimina la actividad del sistema.
     * Muestra un mensaje con el resultado de la operación.
     */
    private class EliminarActividadTask extends AsyncTask<Void, Void, Boolean> {

        String message = "Error";

        @Override
        protected Boolean doInBackground(Void... parameters) {

            String query = "DELETE FROM actividades WHERE _id=" + idActividad + " AND usuario='" + loginUser + "'";
            Log.d("Actividad View ELIMINAR", query);

            try {
                httpHandlerQuery delete = new httpHandlerQuery();
                String resultado = delete.post(query);
                if (resultado.equals("done\n")) {
                    //delete from local BD
                    BaseDeDatosLocalOperaciones bd = new BaseDeDatosLocalOperaciones(getActivity());
                    try {
                        bd.queryBDLocal(query);
                        //todo borrar el archivo fisico del trip
                        message = getString(R.string.msg_datos_eliminados_ok);
                        return true;
                    }catch (Exception e){
                        message = getString(R.string.msg_datos_eliminar_local_bad);
                    }
                }
                else{
                    message = getString(R.string.msg_datos_eliminados_badServer);
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
                Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
                //volver al fragment bicis cuando pasen 2 segundos
                final Handler mHandler = new Handler();
                Runnable mUpdateTimeTask = new Runnable() {
                    public void run() {
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, Fragment_Activity.newInstance(4 + 1, loginUser));
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                };
                mHandler.postDelayed(mUpdateTimeTask, 2000);
            }
            else{
                //mostrar mensaje de fracaso
                Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }
    }//fin async task

    /**
     * Comparte datos de la actividad en la red social Facebook
     */
    private void compartirActividadFB(){
        // Initialize the SDK before executing any other operations, especially, if you're using Facebook UI elements.
        FacebookSdk.sdkInitialize(context);

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
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Obtener modelo de bici a partir de su _id.
     * @param idbici String que se corresponde al campo _id de la bicicleta.
     * @return String que contiene el modelo de la bicicleta.
     */
    private String idToModeloBici(String idbici){
        if(idbici.equals("0"))
            return "Ninguna";

        BaseDeDatosLocalOperaciones db2 = new BaseDeDatosLocalOperaciones(getActivity());
        ArrayList<ContentValues> modelo= db2.selectBDLocal("SELECT modelo FROM bicis WHERE _id=" + idbici + "");

        if (modelo!=null)
            return modelo.get(0).getAsString("modelo");
        else
            return "-";
    }
}

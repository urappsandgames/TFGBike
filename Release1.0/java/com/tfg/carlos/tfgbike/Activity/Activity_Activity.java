package com.tfg.carlos.tfgbike.Activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.IBinder;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tfg.carlos.tfgbike.R;
import com.tfg.carlos.tfgbike.Services_or_Threads.Service_Activity_Recording;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Activity_Activity extends ActionBarActivity {

    private static String loginUser;
    private static String biciUser;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    LatLng latlng;

    TextView txtDistancia;
    TextView txtVelocidad;
    TextView txtTiempo;
    TextView txtAltitud;
    TextView txtAltitudRelativa;
    TextView txtPendiente;
    Button pauseButton;
    Button buttonComenzar;

    boolean isRecording = false;
    int locationChangeCounter;

    boolean  mIsBound = false;
    MyReceiver myReceiver;

    PolylineOptions path;
    CircleOptions circle;
    boolean mapExpandido = false;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_);
        Log.d("Activity activity", "Oncreate");
        //recogemos los parametros pasados
        Bundle b = getIntent().getExtras();
        if(b!=null) {
            loginUser = b.getString("loginUser");
            biciUser = b.getString("biciUser");
        }
        context = this;

        //asignamos los textviews
        txtDistancia = (TextView) findViewById(R.id.text_activity_distancia);
        txtVelocidad = (TextView) findViewById(R.id.text_activity_velocidad);
        txtTiempo = (TextView) findViewById(R.id.text_activity_tiempo);
        txtAltitud = (TextView) findViewById(R.id.text_activity_altitud);
        txtAltitudRelativa = (TextView) findViewById(R.id.text_activity_altitud_relativa);
        txtPendiente = (TextView) findViewById(R.id.text_activity_pendiente);

        pauseButton = (Button) findViewById(R.id.b_activity_pause_resume);

        //TODO Si el GPS esta desactivado, peticion para activarlo
        locationChangeCounter = 0;
        //mapa google
        setUpMapIfNeeded();

        //bind service
        doBindService();

        //boton pausa reanudar listener
        pauseButton.setEnabled(false);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isRecording) {
                    pauseButton.setText("Pausar");
                    mBoundService.pauseRecording();
                    Toast.makeText(context, "Actividad pausada", Toast.LENGTH_LONG).show();
                } else {
                    pauseButton.setText("Continuar");
                    mBoundService.resumeRecording();
                    Toast.makeText(context, "Actividad reanudada", Toast.LENGTH_LONG).show();
                }
            }
        });

    } //fin onCreate

    private Service_Activity_Recording mBoundService;

    /**
     * Conecta o desconecta el servicio que registra la actividad del usuario.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            Log.d("Activity_activity", "Servicio local conectado");
            mBoundService = ((Service_Activity_Recording.LocalBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Log.d("Activity_activity","Servicio local desconectado");
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        mIsBound = bindService(new Intent(this,Service_Activity_Recording.class), mConnection, Context.BIND_AUTO_CREATE);

        if(mIsBound)
            Log.d("ACTIVITY ACTIVITY", "doBind exitoso");
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            Log.d("ACTIVITY ACTIVITY", "doUnBind");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ACTIVITY ACTIVITY","OnDestroy");
        if(myReceiver!=null)
            unregisterReceiver(myReceiver);
        doUnbindService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ACTIVITY ACTIVITY", "OnResume");
        setUpMapIfNeeded();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /*/cuando se destruye se guarda aqui los datos necesarios para cuando vuelva "hacia delante" se cargue la informacion correctamente, asi da la sensacion de nunca destruirse
    @Override
    protected void onSaveInstanceState(Bundle bundle){

    }*/

    //MENU del action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_back, menu);

        //Cambiar color al action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF8E4EFF")));

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

    /**
     * boton empezar/acabar actividad
     * @param view
     */
    public void OnClickButtonActivityEnd(View view){
        buttonComenzar = (Button) view.findViewById(R.id.b_activity_start_end);

        if(buttonComenzar.getText().toString().equals("Comenzar")) {
            //Register BroadcastReceiver
            //to receive event from our service
            myReceiver = new MyReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(mBoundService.MY_ACTION);
            registerReceiver(myReceiver, intentFilter);
            //iniciar service_activity_recording que nos calcula en background los datos en tiempo real
            mBoundService.startRecording();

            isRecording=true;
            //cambiar el texto del boton y deshabilitarlo hasta recibir datos de posicionamiento
            buttonComenzar.setText("Finalizar");
            buttonComenzar.setEnabled(false);
            pauseButton.setEnabled(true);
        }
        else if(buttonComenzar.getText().toString().equals("Finalizar")){
            //dialogo confirmacion
            final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            mBoundService.finishRecording();
                            isRecording=false;
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_confirm).setPositiveButton(R.string.dialog_yes, dialogClickListener)
                    .setNegativeButton(R.string.dialog_no, dialogClickListener).show();
        }
    }


    /**
     * Boton expandir mapa.
     * @param view
     */
    public void OnClickButtonExpandMap(View view){
        /*LinearLayout maplayout = (LinearLayout) findViewById(R.id.layoutMap);
        ScrollView scrolllayout = (ScrollView) findViewById(R.id.ActivityScrollView);
        if(mapExpandido == false){
            maplayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.9f));
            scrolllayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.001f));
            mapExpandido = true;
        }
        else if(mapExpandido == true){
            maplayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,0.4f));
            scrolllayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,0.5f));
            mapExpandido = false;
        }*/
        View maplayout = findViewById(R.id.layoutMap);
        View main = findViewById(R.id.view_main_activity);

        PopupWindow popUp = new PopupWindow(maplayout,LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        if(mapExpandido == false){
            //popUp.showAtLocation(main,Gravity.BOTTOM,100,100);
            //popUp.showAsDropDown(maplayout, 50, -30);
            mapExpandido = true;

        }
        else if(mapExpandido == true){
            popUp.dismiss();
            mapExpandido = false;
        }
    }

    /**
     * Botón cambiar estilo del mapa.
     * @param view
     */
    public void OnClickButtonCambiarMap(View view){
        if(mMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID)
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
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

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        //boton localizador de posicion
        mMap.setMyLocationEnabled(true);
        //TODO posicionarte

        //inicializar polyline para el dibujado del path
        path = new PolylineOptions();
        path.color( Color.parseColor("#CC0000FF") );
        path.width(5);
        path.visible(true);
        path.geodesic(true);

        //inicializar circle que marca posicion del usuario
        circle = new CircleOptions();
        circle.center(new LatLng(0, 0));
        circle.radius(3);
        circle.strokeColor(Color.parseColor("#CC0000FF"));
        circle.fillColor(Color.BLUE);
        mMap.addCircle(circle);

    }

    /**
     * Recibir mensajes broadcast del servicio que registra la actividad.
     * Formatea estos datos y los muestra en la interfaz.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {

            //si flag de finalizar activo, realizamos el unbind del servicio
            if(arg1.getBooleanExtra("finalizar",false))
                finalizarActividad();
            else {
                //si boton finalizar deshabilitado, habilitarlo
                if(!buttonComenzar.isEnabled())
                    buttonComenzar.setEnabled(true);
                //extraer datos del mensaje
                latlng = new LatLng(arg1.getDoubleExtra("latitud", 0), arg1.getDoubleExtra("longitud", 0));
                double velocidad = arg1.getDoubleExtra("velocidad", 0);
                String distancia = new DecimalFormat("#.##").format(arg1.getFloatExtra("distancia", 0));
                int altitud = arg1.getIntExtra("altitud", 0);
                int altitudPos = arg1.getIntExtra("altitudPos", 0);
                int altitudNeg = arg1.getIntExtra("altitudNeg", 0);
                double pendiente = arg1.getDoubleExtra("pendiente", 0);
                pendiente = Math.rint(pendiente*100)/100;

                //tiempo empleado //todo las horas empiezan por 1 en vez de por 0 y no debería ya que el formato HH empieza la hora por 0
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                String tiempo = formatter.format(new Date(arg1.getLongExtra("tiempo", 0)));

                //actualizar UI con datos recibidos
                txtAltitud.setText(String.valueOf(altitud));
                txtAltitudRelativa.setText(String.valueOf(altitudPos) + " \\ " + String.valueOf(altitudNeg));
                txtVelocidad.setText(String.valueOf(velocidad));
                txtDistancia.setText(distancia);
                txtTiempo.setText(tiempo);
                txtPendiente.setText(String.valueOf(pendiente) + "%");

                //Seguimiento de camara y zoom
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16));
                //Draw path
                path.add(latlng);
                mMap.addPolyline(path);
            }
        }
    }

    /**
     * Finaliza el registro de la actividad.
     * Desconecta el servicio y cambia la pantalla a resumen de actividad.
     */
    private void finalizarActividad(){
        doUnbindService();
        //iniciar otra actividad post activity donde se da opcion a guardar o rechazar la actividad y a compartirla, esta actividad se encarga de formatear y guardar los datos recogidos
        Intent intent = new Intent(getApplicationContext(), Activity_Activity_End.class);
        Bundle b = new Bundle();
        b.putString("loginUser", loginUser);
        b.putString("biciUser", biciUser);
        intent.putExtras(b);
        startActivity(intent);
        //destruir esta actividad
        finish();
    }

    /*
        void Sleep(int ms) {
            Log.d("ACTIVITY ", "Sleep");
            try {
                Thread.sleep(ms);
            } catch (Exception e) {
            }
        }
    */

}




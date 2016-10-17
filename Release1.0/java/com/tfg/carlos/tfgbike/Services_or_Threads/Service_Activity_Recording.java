package com.tfg.carlos.tfgbike.Services_or_Threads;


import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.tfg.carlos.tfgbike.BD.BaseDeDatosLocalOperaciones;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

/**
 * Created by Carlos on 24/08/2015.
 * Servicio aun en el hilo principal, hay que crear otro thread para mantenerlo en background
 */
public class Service_Activity_Recording extends Service{

    LatLng latlngNew;
    LatLng latlngOld;
    Location locationOld=null;
    Location locationNew=null;
    int locationChangeCounter;

    private int altitud;
    private int altitudPos;
    private int altitudNeg;
    private int altitudRelativa;
    private int velocidad;
    private double velocidadMedia=0;
    private double velocidadKm;
    private Vector<Double> velocidadM= new Vector<Double>();
    private float distancia=0;
    private float distanciaKmTotal=0;
    private double pendiente=0;
    private long tiempoIni=0;
    private long tiempo=0;
    private long tiempoActual=0;
    private double calorias = 0;
    private String dateIni;
    private String dateFin;

    LocationManager lm = null;

    FileOutputStream outputStream;
    String filename;
    String resultado;
    public final static String MY_ACTION = "MY_ACTION";

    public final static int STATE_IDLE = 0;
    public final static int STATE_RECORDING = 1;
    public final static int STATE_PAUSED = 2;
    public final static int STATE_FULL = 3;
    int state = STATE_IDLE;


    //binder
    public class LocalBinder extends Binder {
       public Service_Activity_Recording getService() {
           Log.d("Service ACTIVITY","LocalBinder");
            return Service_Activity_Recording.this;
        }
    }

    // ---SERVICE methods - required! -----------------
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Service ACTIVITY", "onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service ACTIVITY", "onCreate");
        locationChangeCounter=0;
        altitud = 0;
        velocidad = 0;
        velocidadKm = 0;
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //nombre de fichero
        filename= "Resultados_Actividad";
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Service ACTIVITY", "onDestroy");
        //parar de escuchar los cambios de gps
        lm.removeUpdates(locationListenerGPS);
        lm.removeUpdates(locationListenerNET);

        //cerrar fichero
        try {
            outputStream.close();
        }catch (Exception e){e.printStackTrace();};
    }

    // ---end SERVICE methods -------------------------

    public static String getCurrentTimeStamp(){
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTimeStamp = dateFormat.format(new Date());
            return currentTimeStamp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Comienza a registrar la actividad.
     *
     */
    public void startRecording() {
        Log.d("Service ACTIVITY", "start Recording");
        this.state = STATE_RECORDING;

        //coger tiempo actual
        dateIni=getCurrentTimeStamp();
        tiempoIni=System.currentTimeMillis();

        //encabezado del documento xml que conformara la actividad
        resultado = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<trip>\n<tripstart>\n<dateini>" + dateIni + "</dateini>\n</tripstart>\n";
        try {
            outputStream.write(resultado.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start listening for GPS updates! en thread diferente
        //provider,min time, min distance, listener
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListenerGPS);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNET);
    }

    /**
     * Pausa el registro de la actividad.
     */
    public void pauseRecording() {
        Log.d("Service ACTIVITY","Pause Recording");
        this.state = STATE_PAUSED;
        lm.removeUpdates(locationListenerGPS);
        lm.removeUpdates(locationListenerNET);
        //location = null;
    }

    /**
     * Reaunda el registro de una actividad pausada.
     */
    public void resumeRecording() {
        Log.d("Service ACTIVITY", "Resume Recording");
        this.state = STATE_RECORDING;
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListenerGPS);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNET);
    }

    /**
     * Finaliza el registro de una actividad.
     */
    public void finishRecording() {
        Log.d("Service ACTIVITY","Finish Recording");
        this.state = STATE_FULL;
        lm.removeUpdates(locationListenerGPS);
        lm.removeUpdates(locationListenerNET);
        //clearNotifications();
        //calcular tiempo total
        dateFin = getCurrentTimeStamp();
        //obtener velocidad media del recorrido
        for(int i=0; i<velocidadM.size(); i++) {
            velocidadMedia += velocidadM.get(i);
        }
        velocidadMedia = velocidadMedia/velocidadM.size();
        //calorias
        calCarorias();

        resultado ="<tripend>"+
                "\n<datefin>"+dateFin+"</datefin>"+
                "\n<tutilizado>"+tiempo+"</tutilizado>"+
                "\n<velmedia>"+velocidadMedia+"</velmedia>"+
                "\n<distanciatotal>"+distanciaKmTotal+"</distanciatotal>"+
                "\n<calorias>"+calorias+"</calorias>"+
                "\n<desnivel>"+ (altitudPos-altitudNeg) + "</desnivel>"+
                "\n</tripend>\n</trip>";

        try {
            outputStream.write(resultado.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //mandar el flag de terminacion a la actividad que maneja la UI
        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("finalizar", true);
        sendBroadcast(intent);
    }

    /**
     * Calculo de calorias segun el esfuerzo realizado en la actividad
     */
    private void calCarorias(){
        //calorias = (MET x 0,0175 x peso (kg)) * minutos
        //METS Bicicleta: paseo (<16 km/h)	4,0 Bicicleta (esfuerzo ligero: 16-19 km/h)	6,0 Bicicleta (esfuerzo medio: 19-22,5 km/h) 8,0 Bicicleta (esfuerzo vigoroso: 22,5-24 km/h) 10,0
        int esfuerzo = (int)(Math.round(velocidadMedia));
        double MET = 0;
        if(esfuerzo<16)
            MET=4;
        else if(esfuerzo==17)
            MET=5;
        else if(esfuerzo==18)
            MET=6;
        else if(esfuerzo==19)
            MET=7;
        else if(esfuerzo==20)
            MET=8;
        else if(esfuerzo==21)
            MET=9;
        else if(esfuerzo==22)
            MET=10;
        else if(esfuerzo==23)
            MET=11;
        else if(esfuerzo>=24)
            MET=12;

        //sacar peso de usuario
        BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(this);
        //todo coger el peso del usuario logueado, hay que pasarle al service la variable
        ArrayList<ContentValues> result= db1.selectBDLocal("SELECT peso FROM usuarios");
        int peso = result.get(0).getAsInteger("peso");
        //                              tiempo a minutos
        calorias = MET * 0.0175 * peso * (tiempo/1000/60);
    }


    //calcular datos y publicar los cambios
    void recordingUpdate(Location loc){
        //primera localizacion recibida, inicializamos location con ella
        if(locationOld == null) {
            locationOld = loc;
            locationNew = loc;
        }
        else{
            Log.d("Service recording", "precision: " + String.valueOf(loc.getAccuracy()) + "timeProvider: " + String.valueOf(loc.getTime()));
            //manejar la precision y tiempo (milisegundos) del location para saber si actualizar posicion
            if(loc.getAccuracy()<=5 || (loc.getTime() > locationNew.getTime() + 1900 && loc.getAccuracy()<=30)) {
                //incrementar contador de cambio de posicion
                locationChangeCounter++;
                //establecer posiciones nueva y vieja
                locationOld = locationNew;
                locationNew = loc;

                //Obtener latitud y longitud
                latlngNew = new LatLng(locationNew.getLatitude(), locationNew.getLongitude());

                //tiempo actual
                tiempoActual = System.currentTimeMillis();
                //tiempo que llevamos en la actividad
                tiempo = tiempoActual - tiempoIni;

                //obtener altitud(m), precision(m) y velocidad(m/s)
                altitud = (int) locationNew.getAltitude();
                velocidad = (int) locationNew.getSpeed();

                if (locationOld != null) {
                    //Velocidad en km/h y velocidad media
                    velocidadKm = (((double) velocidad * 60) * 60) / 1000;
                    velocidadM.add(velocidadKm);
                    //Distancia
                    distancia = locationOld.distanceTo(locationNew); //metros
                    distanciaKmTotal += distancia / 1000;
                    //Altitud
                    altitudRelativa = (int) (locationNew.getAltitude() - locationOld.getAltitude());
                    if (altitudRelativa > 0)
                        altitudPos = altitudPos + altitudRelativa;
                    else
                        altitudNeg = altitudNeg + altitudRelativa;
                    //Pendiente en planos rectos: Distancia en vertical * 100/Distancia en horizontal = %pendiente (45grados inclinacion es = 100% de pendiente)
                    pendiente = altitudRelativa * 100 / distancia;
                }

                //enviar informacion a la actividad para que actualice el UI
                try {
                    Intent intent = new Intent();
                    intent.setAction(MY_ACTION);
                    intent.putExtra("latitud", latlngNew.latitude);
                    intent.putExtra("longitud", latlngNew.longitude);
                    intent.putExtra("velociad", velocidadKm);
                    intent.putExtra("altitud", altitud);
                    intent.putExtra("altitudPos", altitudPos);
                    intent.putExtra("altitudNeg", altitudNeg);
                    intent.putExtra("distancia", distanciaKmTotal);
                    intent.putExtra("tiempo", tiempo);
                    intent.putExtra("pendiente", pendiente);
                    sendBroadcast(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //meter info de nodo o waypoint al trip
                resultado = "<waypoint>\n<lat>"+latlngNew.latitude+"</lat>\n<lng>"+latlngNew.longitude+"</lng>"+
                        "\n<velocidad>"+velocidadKm+"</velocidad>"+
                        "\n<altitud>"+altitud+"</altitud>"+
                        "\n<altitudrelativa>"+altitudRelativa+"</altitudrelativa>"+
                        "\n<distancia>"+distancia+"</distancia>"+
                        "\n<distanciatotal>"+distanciaKmTotal+"</distanciatotal>"+
                        "\n<tiempo>"+tiempo+"</tiempo>"+
                        "\n<pendiente>"+pendiente+"</pendiente>"+
                        "\n</waypoint>\n";
                try {
                    outputStream.write(resultado.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //LocationListener implementation
    /**La mayoria de los dispositivos Android traen incorporado un dispositivo GPS
     * con el que podemos obtener las coordenadas donde se encuentra. Para acceder a estos datos necesitamos dos cosas:
     * un manejador o controlador y un lisener que escuche los cambios de estado del gps (cuando nos movemos, encendemos o apagamos el GPS).
     * Para lo primero debemos usar un servicio de sistema que obtendremos haciendo una llamada a la funcion getSystemService().
     * En concreto tendremos que usar un objeto LocationManager.
     *
     * Una vez obtenido el manejador o controlador, debemos definir un Listener que escuche los cambios de posicion y realice las operaciones correspondientes,
     * para ello deberemos crear una clase que extienda de la clase LocationListener, esta clase nos obligara a sobrescribir e implementar cuatro metodos:
     ? onLocationChanged(). Este metodo se llama cuando la posicion GPS cambia, para mi es el mas util, ya que nos pasara el nuevo punto y podremos calcular distancias.. etc.
     ? onProviderDisabled(). Este metodo se llama cuando el usuario desactive el GPS.
     ? onProviderEnabled(). Este metodo se llama cuando el usuario ha activado el GPS.
     ? onStatusChanged(). Este metodo se llama cuando el estado del manejador ha cambiado.
     */
    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("Service ACTIVITY", "onLocationChangedGPS");
            //obtener, calcular datos y publicar los cambios
            recordingUpdate(location);
        }

        @Override
        public void onProviderDisabled(String arg0) {
            Log.d("Service ACTIVITY", "GPS desactivado");
            //GPS desactivado
        }

        @Override
        public void onProviderEnabled(String arg0) {
            Log.d("Service ACTIVITY", "GPS activado");
            //GPS activado
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            Log.d("Service ACTIVITY", "onStatusChanged");
        }
    };

    LocationListener locationListenerNET = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //netSingal = true;
            Log.d("Service ACTIVITY", "onLocationChangedNET");
            //obtener, calcular datos y publicar los cambios
            recordingUpdate(location);

        }

        // @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("Service_Activity", "Provedor net status changed");
        }

        // @Override
        public void onProviderEnabled(String provider) {
            Log.i("Service_Activity", "PROVEDOR " + provider + " HABILITADO!");
        }

        // @Override
        public void onProviderDisabled(String provider) {
            Log.i("Service_Activity", "PROVEDOR " + provider + " DESABILITADO!");
        }
    };
    // END LocationListener implementation:

}

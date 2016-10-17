package com.tfg.carlos.tfgbike.Activity;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.tfg.carlos.tfgbike.R;
import com.tfg.carlos.tfgbike.httpHandlerSelect;

import org.json.JSONArray;
import org.json.JSONException;

public class Activity_Consulta extends ActionBarActivity {

    private String queryconsulta;
    private boolean local;
    JSONArray jresultado;

    private realizarConsulta mConsultaTask = null;
    Context context;

    private double precioT;
    private double pesoT;
    private double kmT;
    private double gastoT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        queryconsulta = b.getString("query");
        local = b.getBoolean("local");
        context = this;

        precioT = 0;
        pesoT = 0;
        kmT = 0;
        gastoT = 0;

        // hacer el intento de consulta del usuario
        mConsultaTask = new realizarConsulta();
        mConsultaTask.execute((Void) null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_back, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_back) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Crea una vista o interfaz para mostrar los resultados de la consulta.
     * @throws JSONException
     */
    private void cargaView() throws  JSONException{

        Context context = this;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view =inflater.inflate(R.layout.activity_consulta, null);

        //Encontrar el scrollView de la vista
        ScrollView sv = (ScrollView) view.findViewById(R.id.consulta_sv);

        // Create a LinearLayout element
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);


        TableLayout t = new TableLayout(context);
        t.setShrinkAllColumns(true);         //ajustar al ancho de pantalla las columnas
        t.setBackgroundColor(Color.parseColor("#3C3C3C"));

        TableRow rowcabecera = new TableRow(context);
        rowcabecera.setBackgroundColor(Color.parseColor("#E2A9F3"));
        rowcabecera.setPadding(0, 0, 0, 2);

        //todo cabeceras clicables para ordenar por
        if(jresultado!= null) {
            //cabeceras: marca modelo tipo precio peso (estado) (km) gasto
            TextView text = new TextView(context);
            text.setText("Marca");
            text.setPadding(1, 0, 1, 0);
            rowcabecera.addView(text);

            text = new TextView(context);
            text.setText("Modelo");
            text.setPadding(1, 0, 1, 0);
            rowcabecera.addView(text);

            text = new TextView(context);
            text.setText("Precio(e)");
            text.setPadding(1, 0, 1, 0);
            rowcabecera.addView(text);

            text = new TextView(context);
            text.setText("Peso(Kg)");
            text.setPadding(1, 0, 1, 0);
            rowcabecera.addView(text);

            if(jresultado.getJSONObject(0).has("estado")) {
                text = new TextView(context);
                text.setText("Estado");
                text.setPadding(1, 0, 1, 0);
                rowcabecera.addView(text);
            }

            if(jresultado.getJSONObject(0).has("km")) {
                text = new TextView(context);
                text.setText("Km recorridos");
                text.setPadding(1, 0, 1, 0);
                rowcabecera.addView(text);
            }

            text = new TextView(context);
            text.setText("Gasto(e)");
            text.setPadding(1, 0, 1, 0);
            rowcabecera.addView(text);

            t.addView(rowcabecera);

            //rellenar tabla
            int z=0;
            TableRow tr = new TableRow(context);
            for(int i=0; i<jresultado.length(); i++){

                text = new TextView(context);
                text.setText(jresultado.getJSONObject(i).getString("marca"));
                tr.addView(text);

                text = new TextView(context);
                text.setText(jresultado.getJSONObject(i).getString("modelo"));
                tr.addView(text);

                text = new TextView(context);
                text.setText(jresultado.getJSONObject(i).getString("precio"));
                tr.addView(text);
                precioT += jresultado.getJSONObject(i).getDouble("precio");

                text = new TextView(context);
                text.setText(jresultado.getJSONObject(i).getString("peso"));
                tr.addView(text);
                pesoT += jresultado.getJSONObject(i).getDouble("peso");

                if(jresultado.getJSONObject(0).has("estado")){
                    text = new TextView(context);
                    text.setText(jresultado.getJSONObject(i).getString("estado"));
                    tr.addView(text);
                }

                if(jresultado.getJSONObject(0).has("km")){
                    text = new TextView(context);
                    text.setText(jresultado.getJSONObject(i).getString("km"));
                    tr.addView(text);
                    kmT += jresultado.getJSONObject(i).getDouble("km");
                }

                text = new TextView(context);
                text.setText(jresultado.getJSONObject(i).getString("gasto"));
                tr.addView(text);
                gastoT += jresultado.getJSONObject(i).getDouble("gasto");

                //colores de las filas de la tabla
                if(z%2 == 0) {
                    tr.setBackgroundColor(Color.parseColor("#F5F5F5"));
                }
                else{
                    tr.setBackgroundColor(Color.parseColor("#FFFFFF"));
                }

                t.addView(tr);
                z++;
                //inicializar nueva fila
                tr = new TableRow(context);
            }
            //si consulta local cargar otra fila mas al final con el precio, gasto, etc totales
            if(local){
                tr = new TableRow(context);

                text = new TextView(context);
                text.setText("TOTALES:");
                text.setPadding(1, 0, 1, 0);
                tr.addView(text);

                text = new TextView(context);
                text.setText("-");
                text.setPadding(1, 0, 1, 0);
                tr.addView(text);

                text = new TextView(context);
                text.setText(String.valueOf(precioT));
                text.setPadding(1, 0, 1, 0);
                tr.addView(text);

                text = new TextView(context);
                text.setText(String.valueOf(pesoT));
                text.setPadding(1, 0, 1, 0);
                tr.addView(text);

                text = new TextView(context);
                if(kmT > 0)
                    text.setText(String.valueOf(kmT));
                else
                    text.setText("-");
                text.setPadding(1, 0, 1, 0);
                tr.addView(text);

                text = new TextView(context);
                text.setText(String.valueOf(gastoT));
                text.setPadding(1, 0, 1, 0);
                tr.addView(text);

                tr.setBackgroundColor(Color.parseColor("#E2A9F3"));
                t.addView(tr);
            }
            ll.addView(t, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
        }
        else{
            //no devolvio ningun resultado, mostrar mensaje y boton atras
            TextView text = new TextView(context);
            text.setText(getString(R.string.consulta_msg_vacia));
            text.setPadding(1, 0, 1, 0);
            ll.addView(text);
            //boton atras
            Button batras = new Button(context);
            batras.setText(getString(R.string.consulta_msg_atras));
            batras.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            ll.addView(batras);
        }

        sv.addView(ll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        setContentView(view);
    }//fin cargaView

    /**
     * Hilo asíncrono que ejecuta la busqueda de la consulta en el servidor.
     *
     */
    public class realizarConsulta extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                httpHandlerSelect query = new httpHandlerSelect();
                jresultado = query.post(queryconsulta);
                /*
                //todo si el usuario es uno mismo, realizar el select de manera local (ahorramos conexiones al servidor ineccesarias)
                if(local){
                    //Preparamos la BD local y cargamos los datos perteneciente al usuario que se logueo
                    BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(context);
                    ArrayList<ContentValues> values = db1.selectBDLocal(queryconsulta);
                    if(values!=null)
                        jresultado = new JSONArray(values);
                    //al convertir jresultado = [null,null,null], hacer a mano la conversion
                    Log.d("ActivityConsultasLOCAL ", jresultado.toString());
                }
                else {
                    httpHandlerSelect query = new httpHandlerSelect();
                    jresultado = query.post(queryconsulta);
                    Log.d("ActivityConsultaServer ",jresultado.toString());
                }
                */

            } catch(Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mConsultaTask = null;
            //showProgress(false);
            if (success) {
                try {
                    //todo si jresultado es null mostrar mensaje error y posible boton actualizar
                    cargaView();

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            else{
                Log.d("Activity COnsulta", "fallo en consulta");
            }
        }

        @Override
        protected void onCancelled() {
            mConsultaTask = null;
        }
    }
}

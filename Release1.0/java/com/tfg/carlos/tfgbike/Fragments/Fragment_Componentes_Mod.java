package com.tfg.carlos.tfgbike.Fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tfg.carlos.tfgbike.Activity.Activity_Navigation;
import com.tfg.carlos.tfgbike.BD.BaseDeDatosLocalOperaciones;
import com.tfg.carlos.tfgbike.R;
import com.tfg.carlos.tfgbike.httpHandlerQuery;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Componentes_Mod.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Componentes_Mod#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Componentes_Mod extends Fragment {
    View fragmentview;
    ArrayList<ContentValues> values;
    static String loginUser;
    static int indexComp;

    final List<NameValuePair> params = new ArrayList<NameValuePair>();
    //spinner
    private Spinner spinner;
    private ArrayAdapter<CharSequence> adapter;
    private Spinner spinnerbici;
    private ArrayAdapter<CharSequence> adapterbici;
    private Spinner spinnerestado;
    private ArrayAdapter<CharSequence> adapterestado;

    private Runnable runnable;

    //calendario para elegir dia
    static Calendar c = Calendar.getInstance();
    static int startYear = c.get(Calendar.YEAR);
    static int startMonth = c.get(Calendar.MONTH);
    static int startDay = c.get(Calendar.DAY_OF_MONTH);
    static String date;
    static TextView textdate;

    // Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Componentes_Mod.
     */
    //Rename and change types and number of parameters
    public static Fragment_Componentes_Mod newInstance(String param1, String param2) {
        Fragment_Componentes_Mod fragment = new Fragment_Componentes_Mod();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_Componentes_Mod() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1); //loginuser
            mParam2 = getArguments().getString(ARG_PARAM2); //id componente
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentview = inflater.inflate(R.layout.fragment_componentes_mod, container, false);
        //Cambiar color al action bar
        ((ActionBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF90C9FF")));

        componenteCargaDatos();

        //listener de botones
        Button button = (Button) fragmentview.findViewById(R.id.button_componente_mod_guardar);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validarCampos()) {
                    ComponenteGuardarModTask cgmt = new ComponenteGuardarModTask();
                    cgmt.execute();
                }
            }
        });

        ImageButton button2 = (ImageButton) fragmentview.findViewById(R.id.Button_componente_mod_calendar);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new StartDatePicker();
                dialogFragment.show(getActivity().getFragmentManager(), "Elije fecha del componente");
                textdate = (TextView) (fragmentview.findViewById(R.id.componente_mod_fecha));
            }
        });


        return fragmentview;
    }

    //Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (OnFragmentInteractionListener) activity;
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

    //PARA NAVIGATION
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static Fragment_Componentes_Mod newInstance(int sectionNumber, String indexComponent, String user){

        Fragment_Componentes_Mod fragment = new Fragment_Componentes_Mod();
        //para pasarle mas argumentos al drawer
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        loginUser = user;
        indexComp = Integer.parseInt(indexComponent);

        return fragment;
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
        // update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


    /**
     * Obtiene la información del componente a modificar con el indexComp que hemos obtenido al cargar el fragmento.
     * Inicializa los spinners modelo de bici, tipo de componente y estado del componente.
     * Carga la información obtenida en el formulario de modificación del componente.
     */
    private void componenteCargaDatos(){

        //obtener info de componente de la bd
        BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(getActivity());
        values= db1.selectBDLocal("SELECT * FROM componentes WHERE _id='" + indexComp + "'");
        //Obtener las bicis del usuario para poder asignar el componente a otra bici si se desea
        ArrayList<ContentValues> result= db1.selectBDLocal("SELECT modelo, _id FROM bicis WHERE usuario='" + loginUser + "'");

        //spinner bici
        ArrayList<CharSequence> modelosSpinner = new ArrayList<>();
        String modeloBiciComponente = "Ninguna";
        spinnerbici = (Spinner) fragmentview.findViewById(R.id.spinner_componentes_bici);
        modelosSpinner.add(modeloBiciComponente);
        if(result!=null) {
            for (int i = 0; result.size() > i; i++) {
                modelosSpinner.add(result.get(i).getAsString("modelo"));
                //asignar al modeloBiciComponente el modelo de la bici a la que pertenece el componente
                if((values.get(0).getAsString("idbici").equals(result.get(i).getAsString("_id")))){
                    modeloBiciComponente = result.get(i).getAsString("modelo");
                }
            }
        }
        // Create an ArrayAdapter using models of the user bikes
        adapterbici = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_dropdown_item, modelosSpinner);
        // Apply the adapter to the spinner
        spinnerbici.setAdapter(adapterbici);
        //dejar seleccionado la bici a la que pertenece el componente
        spinnerbici.setSelection(adapterbici.getPosition(modeloBiciComponente));

        //spinner tipo componente
        spinner = (Spinner) fragmentview.findViewById(R.id.spinner_componentes_tipo);
        // Create an ArrayAdapter using the string array (string_choice_sex) and a default spinner layout
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.choice_componentType, android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(values.get(0).getAsString("tipo")));

        //spinner estado componente
        spinnerestado = (Spinner) fragmentview.findViewById(R.id.spinner_componente_estado);
        // Create an ArrayAdapter using the string array (string_choice_sex) and a default spinner layout
        adapterestado = ArrayAdapter.createFromResource(getActivity(),
                R.array.choice_componentStatus, android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapterestado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerestado.setAdapter(adapterestado);
        spinnerestado.setSelection(adapterestado.getPosition(values.get(0).getAsString("estado")));

        //Marca
        TextView ttext = (TextView)(fragmentview.findViewById(R.id.componente_mod_marca));
        if(ttext!= null)
            ttext.setText(values.get(0).getAsString("marca"));
        //Modelo
        ttext = (TextView)(fragmentview.findViewById(R.id.componente_mod_modelo));
        if(ttext!= null)
            ttext.setText(values.get(0).getAsString("modelo"));
        //Peso
        ttext = (TextView)(fragmentview.findViewById(R.id.componente_mod_peso));
        if(ttext!= null)
            ttext.setText(values.get(0).getAsString("peso"));
        //Fecha
        ttext = (TextView)(fragmentview.findViewById(R.id.componente_mod_fecha));
        if(ttext!= null)
            ttext.setText(values.get(0).getAsString("fecha"));
        //Precio
        ttext = (TextView)(fragmentview.findViewById(R.id.componente_mod_precio));
        if(ttext!= null)
            ttext.setText(values.get(0).getAsString("precio"));
        //Notas
        ttext = (TextView)(fragmentview.findViewById(R.id.componente_mod_notas));
        if(ttext!= null)
            ttext.setText(values.get(0).getAsString("notas"));
        //Gasto  //precio + gasto
        ttext = (TextView)(fragmentview.findViewById(R.id.componente_mod_gasto));
        if(ttext!= null)
            ttext.setText(values.get(0).getAsString("gasto"));
        //Modificar Gasto  //disminuir o aumentar el gasto
        //listener para boton aumentar gasto
        ImageButton buttonGastoMas = (ImageButton) fragmentview.findViewById(R.id.Button_componente_gasto_mas);
        buttonGastoMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiarGasto(true);
            }}
        );
        //listener para boton aumentar gasto
        ImageButton buttonGastoMenos = (ImageButton) fragmentview.findViewById(R.id.Button_componente_gasto_menos);
        buttonGastoMenos.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                cambiarGasto(false);
             }}
        );
    }

    /**
     * Valida el gasto cuando queremos modificarlo usando la interfaz de la pantalla modificar componente.
     * Muestra a través de la interfaz si hay algún fallo.
     * @param positivo boolean para saber si restar o sumar el cambio de gasto al antiguo.
     */
    private void cambiarGasto(boolean positivo){

        TextView gasto = (TextView)(fragmentview.findViewById(R.id.componente_mod_gasto));
        TextView gastoCambio = (TextView)(fragmentview.findViewById(R.id.componente_mod_mod_gasto));
        double gastoOld = (Double.parseDouble(gasto.getText().toString()));
        double gastoMod = (Double.parseDouble(gastoCambio.getText().toString()));
        double gastoNew;

        if(positivo)
            gastoNew = gastoOld + gastoMod;
        else
            gastoNew = gastoOld - gastoMod;
        if(gastoNew<0)
            gastoCambio.setText("op negativa");
        else {
            gastoCambio.setText("");
            gasto.setText(String.valueOf(gastoNew));
        }
    }

    /**
     * Hilo asíncrono para guardar en las bases de datos la modificación del componente.
     * Muestra un mensaje con el resultado de la operación.
     */
    private class ComponenteGuardarModTask extends AsyncTask<Void, Void, Boolean> {
        String message = "";

        @Override
        protected Boolean doInBackground(Void... parameters) {
            try {
                //asignar idbici segun la bici del componente
                String idbici= getIdBici();
                String _id = values.get(0).getAsString("_id");
                String query = "UPDATE componentes SET marca='" + params.get(0).getValue() +
                        "', modelo='" + params.get(1).getValue() +
                        "', peso='" + params.get(2).getValue() +
                        "', tipo='" + params.get(3).getValue() +
                        "', fecha='" + params.get(4).getValue() +
                        "', estado='" + params.get(5).getValue() +
                        "', precio='" + params.get(6).getValue() +
                        "', notas='" + params.get(7).getValue() +
                        "', gasto='" + params.get(8).getValue() +
                        "', idbici='" + idbici +
                        "' WHERE usuario='" + loginUser + "' AND _id='" + _id + "'";

                httpHandlerQuery httpmodbici = new httpHandlerQuery();
                String resultado = httpmodbici.post(query);
                //si no devuelve done, se guarda en local igualmente con needToUpdate 1
                ContentValues values = new ContentValues();
                if (!resultado.equals("done\n")) {
                    values.put("needUpdate",1);
                    message = getString(R.string.msg_datos_modificados_badServer);
                }
                //Update en BD local
                BaseDeDatosLocalOperaciones bd = new BaseDeDatosLocalOperaciones(getActivity());
                values.put("marca", params.get(0).getValue());
                values.put("modelo", params.get(1).getValue());
                values.put("peso", params.get(2).getValue());
                values.put("tipo", params.get(3).getValue());
                values.put("fecha", params.get(4).getValue());
                values.put("estado", params.get(5).getValue());
                values.put("precio", params.get(6).getValue());
                values.put("notas", params.get(7).getValue());
                values.put("gasto", params.get(8).getValue());
                values.put("idbici", idbici);
                try {
                    bd.updateBDLocal("componentes", values, "_id=" + _id, null);
                    message = getString(R.string.msg_datos_modificados_ok);
                    return true;
                }catch(Exception e){
                    message = getString(R.string.msg_datos_modificar_local_bad);
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
                //volver al fragment componentes cuando pasen 2 segundos
                final Handler mHandler = new Handler();
                Runnable mUpdateTimeTask = new Runnable() {
                    public void run() {
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, Fragment_Componentes.newInstance(2 + 1, loginUser));
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
     * Devuelve el _id de la bici a la que se le quiere asignar el componente, si no seleccionamos ninguna bici, devuelve 0.
     * @return el campo "_id" de la bicicleta que está seleccionada en el spinner modelobici.
     */
    private String getIdBici(){
        String modelobici = ((Spinner) fragmentview.findViewById(R.id.spinner_componentes_bici)).getSelectedItem().toString();
        if(!modelobici.equals("Ninguna")) {
            BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(getActivity());
            ArrayList<ContentValues> result = db1.selectBDLocal("SELECT _id FROM bicis WHERE usuario='" + loginUser + "' AND modelo='" + modelobici + "'");
            return result.get(0).getAsString("_id");
        }
        return "0";
    }

    /**
     * Diálogo para elegir la fecha del componente.
     */
    public static class StartDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, startYear, startMonth, startDay);
            return dialog;

        }
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // Do something with the date chosen by the user
            startYear = year;
            startMonth = monthOfYear;
            startDay = dayOfMonth;
            date = startYear+"-"+startMonth+"-"+startDay;
            textdate.setText(date);
        }
    }

    /**
     * Valida los valores del componente a modificar del formulario y los empaqueta de manera clave-valor.
     * @return true si los valores del componente se validan correctamente, falso en caso contrario.
     */
    private boolean validarCampos(){
        View focusView = null;
        View vvalue;
        String svalue;
        params.clear();

        //Marca 0
        vvalue = fragmentview.findViewById(R.id.componente_mod_marca);
        svalue = ((EditText) vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        } else if (!isMarcaValid(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_nombre_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        } else {
            params.add(new BasicNameValuePair("marca", svalue));
        }

        //Modelo 1
        vvalue = fragmentview.findViewById(R.id.componente_mod_modelo);
        svalue = ((EditText) vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        } else if (!isModeloValid(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_nombre_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        } else {
            params.add(new BasicNameValuePair("modelo", svalue));
        }

        //peso 2
        vvalue = fragmentview.findViewById(R.id.componente_mod_peso);
        svalue = ((EditText) vvalue).getText().toString();
        if (!isPesoAgeValid(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_peso_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        } else {
            params.add(new BasicNameValuePair("peso", svalue));
        }

        //tipo 3
        params.add(new BasicNameValuePair("tipo", ((Spinner) fragmentview.findViewById(R.id.spinner_componentes_tipo)).getSelectedItem().toString()));

        //fecha (4)
        vvalue = fragmentview.findViewById(R.id.componente_mod_fecha);
        svalue = ((TextView) vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else{
            params.add(new BasicNameValuePair("fecha", svalue));
        }

        //estado 5
        params.add(new BasicNameValuePair("estado", ((Spinner) fragmentview.findViewById(R.id.spinner_componente_estado)).getSelectedItem().toString()));

        //precio 6
        vvalue = fragmentview.findViewById(R.id.componente_mod_precio);
        svalue = ((EditText) vvalue).getText().toString();
        if (!isPesoAgeValid(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_precio_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        } else {
            params.add(new BasicNameValuePair("precio", svalue));
        }

        //notas 7
        vvalue = fragmentview.findViewById(R.id.componente_mod_notas);
        svalue = ((EditText) vvalue).getText().toString();
        params.add(new BasicNameValuePair("notas", svalue));

        //gasto 8
        vvalue = fragmentview.findViewById(R.id.componente_mod_gasto);
        svalue = ((TextView) vvalue).getText().toString();
        if (!isGastoValid(svalue)){
            ((TextView) vvalue).setError(getString(R.string.error_gasto_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else{
            params.add(new BasicNameValuePair("gasto", svalue));
        }

        return true;
    }

    private boolean isMarcaValid(String marca) {
        return (marca.length() < 40 && marca.length() > 1);
    }

    private boolean isModeloValid(String modelo) {
        return (modelo.length() > 1 && modelo.length() < 40);
    }

    private boolean isPesoAgeValid(String value){
        double a;
        try{
            a = Double.parseDouble(value);
        }
        catch (NumberFormatException e){
            e.printStackTrace();
            return false;
        }
        if(a<0)
            return false;
        return true;
    }

    //gasto debe ser siempre igual o mayor que el precio
    private boolean isGastoValid(String value){
        String precio = params.get(6).getValue();
        if((Double.parseDouble(precio)) > (Double.parseDouble(value)))
            return false;
        return true;
    }

}

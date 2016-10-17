package com.tfg.carlos.tfgbike.Fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.tfg.carlos.tfgbike.httpHandlerInsert;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Componentes_New.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Componentes_New#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Componentes_New extends Fragment {

    static String loginUser;
    private View fragmentview;
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

    //Rename parameter arguments, choose names that match
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
     * @return A new instance of fragment Fragment_Componentes_New.
     */
    //Rename and change types and number of parameters
    public static Fragment_Componentes_New newInstance(String param1, String param2) {
        Fragment_Componentes_New fragment = new Fragment_Componentes_New();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_Componentes_New() {
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
        fragmentview = inflater.inflate(R.layout.fragment_componentes_new, container, false);

        //INICIALIZAR SPINNERs
        setSpinners();

        //listener para los onclicks de los botones en fragments
        Button button = (Button) fragmentview.findViewById(R.id.button_componente_guardar);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validarCampos()) {
                    NuevoComponenteTask nct = new NuevoComponenteTask();
                    nct.execute();
                }
            }
        });

        ImageButton button2 = (ImageButton) fragmentview.findViewById(R.id.Button_componente_calendar);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new StartDatePicker();
                dialogFragment.show(getActivity().getFragmentManager(), "Elije fecha del componente");
                textdate = (TextView)(fragmentview.findViewById(R.id.componente_new_fecha));
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

    public static Fragment_Componentes_New newInstance(int sectionNumber, String user){

        Fragment_Componentes_New fragment = new Fragment_Componentes_New();
        //para pasarle mas argumentos al drawer
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        loginUser = user;

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
        //Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


    //carga el modelo de las bicis de usuario en el spinner bici y deja seleccionada la predeterminada

    /**
     * Inicializa los spinners de la pantalla para añadir un nuevo componente.
     * Estos spinners son:
     * modelo de bicicleta a la que se asociará el componente.
     * tipo de componente.
     * estado de componente
     *
     */
    private void setSpinners(){
        //spinner bici
        BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(getActivity());
        ArrayList<ContentValues> result= db1.selectBDLocal("SELECT modelo, predeterminada FROM bicis WHERE usuario='" + loginUser + "'");
        ArrayList<CharSequence> modelosSpinner = new ArrayList<>();

        spinnerbici = (Spinner) fragmentview.findViewById(R.id.spinner_componentes_bici);
        modelosSpinner.add("Ninguna");
        String predeterminada =  null;
        if(result!=null) {
            predeterminada = result.get(0).getAsString("modelo");
            for (int i = 0; result.size() > i; i++) {
                modelosSpinner.add(result.get(i).getAsString("modelo"));
            }
        }
        // Create an ArrayAdapter using models of the user bikes
        adapterbici = new ArrayAdapter<CharSequence> (getActivity(), android.R.layout.simple_spinner_dropdown_item, modelosSpinner);
        // Apply the adapter to the spinner
        spinnerbici.setAdapter(adapterbici);
        //dejar seleccionado la bici predeterminada
        if(predeterminada!=null)
            spinnerbici.setSelection(adapterbici.getPosition(predeterminada));

        //spinner tipo componente
        spinner = (Spinner) fragmentview.findViewById(R.id.spinner_componentes_tipo);
        // Create an ArrayAdapter using the string array (string_choice_sex) and a default spinner layout
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.choice_componentType, android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        //spinner estado componente
        spinnerestado = (Spinner) fragmentview.findViewById(R.id.spinner_componente_estado);
        // Create an ArrayAdapter using the string array (string_choice_sex) and a default spinner layout
        adapterestado = ArrayAdapter.createFromResource(getActivity(),
                R.array.choice_componentStatus, android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerestado.setAdapter(adapterestado);
    }


    /**
     * Hilo asíncrono para dar de alta un nuevo componente.
     * Muestra un mensaje con el resultado de la operación.
     */
    private class NuevoComponenteTask extends AsyncTask<Void, Void, Boolean>{
        String message = "Error";

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                    httpHandlerInsert query = new httpHandlerInsert();
                    String resultado = query.post(completarPreparativosComponente());
                    if(isInteger(resultado)) {
                        try {
                            if(altaEnBDLocal(0, resultado)){
                                message = getString(R.string.msg_componente_agregado_ok);
                                return true;
                            }
                            else{
                                message = getString(R.string.msg_datos_agregar_local_bad);
                            }
                        }catch (Exception e){e.printStackTrace();}

                    }
                    else{
                        if(altaEnBDLocal(1, null))
                            message = getString(R.string.msg_datos_agregados_badServer);
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
                        transaction.replace(R.id.container, Fragment_Componentes.newInstance(3 + 1, loginUser));
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
     * Construye para el envio al servidor la query para añadir el nuevo componente.
     * @return String con la query para enviar al servidor.
     */
    private String completarPreparativosComponente(){

        //usuario (8)
        params.add(new BasicNameValuePair("usuario", loginUser));

        //gasto (9) = precio al dar de alta el componente
        params.add(new BasicNameValuePair("gasto", params.get(6).getValue()));

        //idbici obtenido con el modelo del spinner y el usuario (10)
        params.add(new BasicNameValuePair("idbici", getIdBici()));

        String query = "INSERT INTO componentes (marca, modelo, peso, tipo, fecha, estado, precio, notas, gasto, idbici, usuario) " +
                "VALUES ('" + params.get(0).getValue() + "','" + //marca
                params.get(1).getValue() + "'," +    //modelo
                params.get(2).getValue() + ",'" +    //peso
                params.get(3).getValue() + "','" +    //tipo
                params.get(4).getValue() + "','" +    //fecha
                params.get(5).getValue() + "'," +    //estado
                params.get(6).getValue() + ",'" +    //precio
                params.get(7).getValue() + "'," +    //notas
                params.get(9).getValue() + "," +     //gasto
                params.get(10).getValue() + ",'" +    //idbici
                loginUser + "')";                    //usuario
        //Log.d("COmpoNew query server", query);

        return query;
    }

    /**
     * Da de alta el componente en la base de datos local.
     * Segun el parametro pasado se dara de alta con el flag needUpdate activado (para subirlo posteriormente al servidor)
     * o desactivado (ya subido al servidor).
     *
     * @param nuevo integer que puede tomar el valor 0 o 1 para funcionar a modo de flag para el campo "nuevo"
     * @param id string con el identificador del componente.
     * @return true si la operación se efectua correctamente, false en caso contrario.
     * @throws JSONException
     */
    private boolean altaEnBDLocal(int nuevo, String id) throws JSONException{

        BaseDeDatosLocalOperaciones bd = new BaseDeDatosLocalOperaciones(getActivity());
        ContentValues values= new ContentValues();
        values.put("_id", id);
        values.put("marca", params.get(0).getValue());
        values.put("modelo", params.get(1).getValue());
        values.put("peso", params.get(2).getValue());
        values.put("tipo", params.get(3).getValue());
        values.put("fecha", params.get(4).getValue());
        values.put("estado", params.get(5).getValue());
        values.put("precio", params.get(6).getValue());
        values.put("notas", params.get(7).getValue());
        values.put("gasto", params.get(9).getValue());
        values.put("idbici", params.get(10).getValue());
        values.put("usuario", loginUser);
        values.put("nuevo", nuevo);
        try {
            bd.insertBDLocal(values, "componentes");
            return true;
        }catch (Exception e){e.printStackTrace();}

        return false;
    }

    /**
     * Consigue el campo "_id" de la bici a la que se le quiere asignar el componente, si no seleccionamos ninguna bici, devuelve 0.
     * @return _id de la bicicleta a la que se le quiere asignar el componente.
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
     * Obtiene los valores del componente del formulario, los valida y empaqueta de manera nombre-valor.
     * @return true si los valores del componente se validan conrrectamente, false en caso contrario.
     */
    private boolean validarCampos(){
        View focusView = null;
        View vvalue;
        String svalue;
        params.clear();

        //Marca 0
        vvalue = fragmentview.findViewById(R.id.componente_new_marca);
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
        vvalue = fragmentview.findViewById(R.id.componente_new_modelo);
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
        vvalue = fragmentview.findViewById(R.id.componente_new_peso);
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
        vvalue = fragmentview.findViewById(R.id.componente_new_fecha);
        svalue = ((TextView) vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((TextView) vvalue).setError(getString(R.string.error_field_required));
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
        vvalue = fragmentview.findViewById(R.id.componente_new_precio);
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
        vvalue = fragmentview.findViewById(R.id.componente_new_notas);
        svalue = ((EditText) vvalue).getText().toString();
        params.add(new BasicNameValuePair("notas", svalue));

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

    //dialogo con recuadro para elegir fecha
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
            //Log.i("Componente NeW ", "DATE PICKER" + date);
            textdate.setText(date);
        }
    }

}





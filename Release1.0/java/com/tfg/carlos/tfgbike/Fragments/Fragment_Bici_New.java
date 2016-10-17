package com.tfg.carlos.tfgbike.Fragments;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.tfg.carlos.tfgbike.Activity.Activity_Navigation;
import com.tfg.carlos.tfgbike.BD.BaseDeDatosLocalOperaciones;
import com.tfg.carlos.tfgbike.R;
import com.tfg.carlos.tfgbike.httpHandlerInsert;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Bici_New.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Bici_New#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Bici_New extends Fragment {
    static String loginUser;
    public View fragmentview;
    final List<NameValuePair> params = new ArrayList<NameValuePair>();
    boolean predeterminada = false;

    ArrayAdapter<CharSequence> adapter;
    Spinner spinner;

    private Runnable runnable;

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
     * @return A new instance of fragment Fragment_Bici_New.
     */
    // Rename and change types and number of parameters
    public static Fragment_Bici_New newInstance(String param1, String param2) {
        Fragment_Bici_New fragment = new Fragment_Bici_New();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_Bici_New() {
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
        fragmentview = inflater.inflate(R.layout.fragment_bici_new, container, false);
        //Cambiar color al action bar
        ((ActionBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF91DB90")));

        //INICIALIZAR SPINNER tipo bici
        spinner = (Spinner) fragmentview.findViewById(R.id.spinner_bikeType);
        // Create an ArrayAdapter using the string array (string_choice_sex) and a default spinner layout
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.choice_bikeType, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        //listener para los onclicks de los botones en fragments
        Button button = (Button) fragmentview.findViewById(R.id.button_bici_new_guardar);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validarCampos()) {
                    GuardarNuevaBiciTask nbt = new GuardarNuevaBiciTask();
                    nbt.execute();
                }
            }
        });

        return fragmentview;
    }

    //  Rename method, update argument and hook method into UI event
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

    public static Fragment_Bici_New newInstance(int sectionNumber, String user){

        Fragment_Bici_New fragment = new Fragment_Bici_New();
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
        // Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    /**
     *Hilo asíncrono que da de alta una nueva bici en el sistema.
     * Muestra un mensaje con el resultado de la operación.
     */
    public class GuardarNuevaBiciTask extends AsyncTask<Void, Void, Boolean> {
        String message = "";

        GuardarNuevaBiciTask() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try{

                    httpHandlerInsert query = new httpHandlerInsert();
                    String resultado = query.post(completarPreparativosBici());
                    Log.d("BICI NUEVA ", resultado);
                    if(isInteger(resultado)) {
                        try {
                            if(altaEnBDLocal(resultado)) {
                                message = getString(R.string.msg_bici_agregada_ok);
                                return true;
                            }
                            else{message=getString(R.string.msg_registro_bad);}
                        }catch (Exception e){e.printStackTrace(); message=getString(R.string.msg_datos_agregar_local_bad);}

                    }
                    else{
                        message=getString(R.string.msg_registro_bad);
                    }

            } catch(Exception e){e.printStackTrace();}
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
                        transaction.replace(R.id.container, Fragment_Bici.newInstance(2 + 1, loginUser));
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
    }// fin async task

    /**
     * Crea la query necesaria para añadir la nueva bicicleta en el servidor.
     * @return query que se enviará al servidor para que se ejecute.
     */
    private String completarPreparativosBici(){
        //como es nueva bici, gasto = precio
        String query = "INSERT INTO bicis (marca, modelo, peso, tipo, precio, gasto, usuario) " +
                "VALUES ('" + params.get(0).getValue() + "','" + //marca
                params.get(1).getValue() + "'," +    //modelo
                params.get(2).getValue() + ",'" +    //peso
                params.get(3).getValue() + "'," +    //tipo
                params.get(4).getValue() + "," +     //precio
                params.get(4).getValue() + ",'" +    //gasto
                loginUser + "')";             //usuario

        //Log.d("BICI NUEVA ", query);
        return query;
    }

    /**
     * Añade la bicicleta en la base de datos local empaquetando el registro en ContentValues clave valor.
     * Solo se ejecuta si también se añadió en el servidor.
     * @param id número en formato string que se corresponde con el campo "_id" que generó la base de datos en el servidor al añadir la bicicleta.
     * @return true si se añade correctamente, false en caso contrario.
     */
    private boolean altaEnBDLocal(String id){

        BaseDeDatosLocalOperaciones bd = new BaseDeDatosLocalOperaciones(getActivity());
        ContentValues values= new ContentValues();

        values.put("_id", id);
        values.put("marca", params.get(0).getValue());
        values.put("modelo", params.get(1).getValue());
        values.put("peso", params.get(2).getValue());
        values.put("tipo", params.get(3).getValue());
        values.put("km",0);
        values.put("precio", params.get(4).getValue());
        values.put("gasto", params.get(4).getValue());
        values.put("usuario", loginUser);
        try{
            bd.insertBDLocal(values ,"bicis");
            return true;
        }catch (Exception e){e.printStackTrace();}

        return false;
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
     * Obtiene los valores del formulario de bici nueva, los valida y empaqueta de manera nombre-valor.
     * @return true si los campos se validan correctamente o false en caso contrario.
     */
    private boolean validarCampos() {
        View focusView = null;
        View vvalue;
        String svalue;
        //cada vez que validamos tenemos que vaciar los parametros para su correcta introducción
        params.clear();

        //Marca
        vvalue = fragmentview.findViewById(R.id.bici_new_marca);
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
            //valido
            params.add(new BasicNameValuePair("marca", svalue));
        }

        //Modelo
        vvalue = fragmentview.findViewById(R.id.bici_new_modelo);
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

        //peso
        vvalue = fragmentview.findViewById(R.id.bici_new_peso);
        svalue = ((EditText) vvalue).getText().toString();

        if (!isPesoAgeValid(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_peso_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        } else {
            params.add(new BasicNameValuePair("peso", svalue));
        }

        //tipo
        params.add(new BasicNameValuePair("tipo", ((Spinner) fragmentview.findViewById(R.id.spinner_bikeType)).getSelectedItem().toString()));

        //precio
        vvalue = fragmentview.findViewById(R.id.bici_new_precio);
        svalue = ((EditText) vvalue).getText().toString();
        if (!isPesoAgeValid(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_precio_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        } else {
            params.add(new BasicNameValuePair("precio", svalue));
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
        if(a<0 || a>300)
            return false;
        return true;
    }

}

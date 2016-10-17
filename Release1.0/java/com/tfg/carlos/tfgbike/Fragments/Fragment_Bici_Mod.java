package com.tfg.carlos.tfgbike.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.widget.EditText;
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
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Bici_Mod.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Bici_Mod#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Bici_Mod extends Fragment {

    public View fragmentview;
    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;

    static ArrayList<ContentValues> values;
    static String loginUser;

    private Runnable runnable;
    final List<NameValuePair> params = new ArrayList<NameValuePair>();

    //Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Bici_Mod.
     */
    // Rename and change types and number of parameters
    public static Fragment_Bici_Mod newInstance(String param1, String param2) {
        Fragment_Bici_Mod fragment = new Fragment_Bici_Mod();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_Bici_Mod() {
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
        fragmentview = inflater.inflate(R.layout.fragment_bici_mod, container, false);

        //INICIALIZAR SPINNER tipo bici
        spinner = (Spinner) fragmentview.findViewById(R.id.spinner_bikeType);
        // Create an ArrayAdapter using the string array (string_choice_sex) and a default spinner layout
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.choice_bikeType, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        biciCargarDatos();

        //listener para los onclicks de los botones en fragments
        Button buttoneliminar = (Button) fragmentview.findViewById(R.id.button_bici_eliminar);
        buttoneliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //dialogo confirmacion
                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                BiciEliminarTask bet= new BiciEliminarTask();
                                bet.execute();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.dialog_confirm).setPositiveButton(R.string.dialog_yes, dialogClickListener)
                        .setNegativeButton(R.string.dialog_no, dialogClickListener).show();
            }
        });
        //boton guardar modificacion
        Button buttoneguardarmod = (Button) fragmentview.findViewById(R.id.button_bici_guardar);
        buttoneguardarmod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validarCampos()) {
                    BiciGuardarModTask bgmt = new BiciGuardarModTask();
                    bgmt.execute();
                }
            }
        });
        //boton nueva
        Button buttonnueva = (Button) fragmentview.findViewById(R.id.button_bici_nueva);
        buttonnueva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validarCampos()) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, Fragment_Bici_New.newInstance(10 + 1, loginUser));
                    transaction.addToBackStack("");
                    transaction.commit();
                }
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

    public static Fragment_Bici_Mod newInstance(int sectionNumber, ArrayList<ContentValues> bici, String user){

        Fragment_Bici_Mod fragment = new Fragment_Bici_Mod();
        //para pasarle mas argumentos al drawer
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        values=bici;
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
     * Rellena el formulario de modificacíon de bicicletas con los datos de la bicicleta seleccionada.
     */
    private void biciCargarDatos(){

        TextView ttext = (TextView)(fragmentview.findViewById(R.id.bici_mod_marca));
        if(ttext!= null)
            ttext.setText(values.get(0).getAsString("marca"));

        ttext = (TextView)(fragmentview.findViewById(R.id.bici_mod_modelo));
        if(ttext!= null)
            ttext.setText(values.get(0).getAsString("modelo"));

        ttext = (TextView)(fragmentview.findViewById(R.id.bici_mod_peso));
        if(ttext!= null)
            ttext.setText(values.get(0).getAsString("peso"));

        spinner.setSelection(adapter.getPosition(values.get(0).getAsString("tipo")));

        ttext = (TextView)(fragmentview.findViewById(R.id.bici_mod_precio));
        if(ttext!= null)
            ttext.setText(values.get(0).getAsString("precio"));

        ttext = (TextView)(fragmentview.findViewById(R.id.bici_mod_km));
        if(ttext!= null)
            ttext.setText(values.get(0).getAsString("km"));

        ttext = (TextView)(fragmentview.findViewById(R.id.bici_mod_gasto));
        if(ttext!= null)
            ttext.setText(values.get(0).getAsString("gasto"));

    }

    /**
     * Hilo asíncrono que elimina la bicicleta seleccionada.
     * Devuelve un mensaje con el resultado de la operación.
     */
    private class BiciEliminarTask extends AsyncTask<Void, Void, Boolean> {

        String message = "";

        @Override
        protected Boolean doInBackground(Void... parameters) {
            View vvalue;
            String marca, modelo, usuario;

            vvalue = fragmentview.findViewById(R.id.bici_mod_marca);
            marca = ((EditText) vvalue).getText().toString();
            vvalue = fragmentview.findViewById(R.id.bici_mod_modelo);
            modelo = ((EditText) vvalue).getText().toString();

            String query = "DELETE FROM bicis WHERE marca='" + marca +
                    "' AND modelo='" + modelo + "' AND usuario='" + loginUser + "'";
            //Log.d("BICI MOD ELIMINAR", query);

            try {
                httpHandlerQuery delete = new httpHandlerQuery();
                String resultado = delete.post(query);
                if (resultado.equals("done\n")) {
                    //delete from local BD
                    BaseDeDatosLocalOperaciones bd = new BaseDeDatosLocalOperaciones(getActivity());
                    try {
                        bd.queryBDLocal(query);
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
    }//fin async task

    /**
     * Hilo asíncrono que guarda los datos modificados de la bici previamente seleccionada.
     * Crea la query para enviar al servidor y realiza los cambios en la base de datos local.
     * Muestra un mensaje con el resultado de la operación.
     */
    private class BiciGuardarModTask extends AsyncTask<Void, Void, Boolean> {
        String message = "Error";

        BiciGuardarModTask() {
        }

        @Override
        protected Boolean doInBackground(Void... parameters) {
            try {
                boolean needUpdate=false;
                String idBici = values.get(0).getAsString("_id");
                String query = "UPDATE bicis SET marca='" + params.get(0).getValue() +
                        "', modelo='" + params.get(1).getValue() +
                        "', peso='" + params.get(2).getValue() +
                        "', tipo='" + params.get(3).getValue() +
                        "', precio='" + params.get(4).getValue() +
                        "', gasto='" + params.get(4).getValue() +
                        "' WHERE usuario='" + loginUser + "' AND _id='" + idBici + "'";

                httpHandlerQuery httpmodbici = new httpHandlerQuery();
                String resultado = httpmodbici.post(query);
                Log.d("BICI Mod ", query);
                if (!resultado.equals("done\n")) {
                    //si no ha sido correcto debemos modificar en local con el flag needUpdate encendido
                    message = getString(R.string.msg_datos_modificados_badServer);
                    needUpdate=true;
                    }
                //BD local
                try {
                    BaseDeDatosLocalOperaciones bd = new BaseDeDatosLocalOperaciones(getActivity());
                    ContentValues values = new ContentValues();
                    values.put("marca", params.get(0).getValue());
                    values.put("modelo", params.get(1).getValue());
                    values.put("peso", params.get(2).getValue());
                    values.put("tipo", params.get(3).getValue());
                    values.put("precio", params.get(4).getValue());
                    values.put("gasto", params.get(5).getValue());
                    if(needUpdate)
                        values.put("needUpdate", 1);
                    //update local
                    bd.updateBDLocal("bicis", values, "_id=" +idBici, null);

                    message = getString(R.string.msg_datos_modificados_ok);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
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
    }//fin async task

    /**
     * Validaciones de los campos para modificar la bicicleta.
     * @return true si las modificaciones se validan correctamente, false en caso contrario.
     */
    //
    private boolean validarCampos() {
        View focusView = null;
        View vvalue;
        String svalue;
        //cada vez que validamos tenemos que vaciar los parametros para su correcta introduccion
        params.clear();

        //Marca
        vvalue = fragmentview.findViewById(R.id.bici_mod_marca);
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
        vvalue = fragmentview.findViewById(R.id.bici_mod_modelo);
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
        vvalue = fragmentview.findViewById(R.id.bici_mod_peso);
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
        vvalue = fragmentview.findViewById(R.id.bici_mod_precio);
        svalue = ((EditText) vvalue).getText().toString();
        if (!isPesoAgeValid(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_precio_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        } else {
            params.add(new BasicNameValuePair("precio", svalue));
        }

        //todo futuro, permitir modificar gasto directamente en una bicicleta y no solo en sus componentes
        //gasto
        vvalue = fragmentview.findViewById(R.id.bici_mod_gasto);
        svalue = ((TextView) vvalue).getText().toString();
        params.add(new BasicNameValuePair("gasto", svalue));


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
            return false;
        }
        if(a<0)
            return false;
        return true;
    }

}

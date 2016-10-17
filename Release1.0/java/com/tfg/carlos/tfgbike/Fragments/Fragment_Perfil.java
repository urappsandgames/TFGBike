package com.tfg.carlos.tfgbike.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
 * {@link Fragment_Perfil.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Perfil#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Perfil extends Fragment {

    public View fragmentview;
    final List<NameValuePair> params = new ArrayList<NameValuePair>();
    static String loginUser;

    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;

    // Rename parameter arguments, choose names that match
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
     * @return A new instance of fragment Fragment_Perfil.
     */
    // Rename and change types and number of parameters
    public static Fragment_Perfil newInstance(String param1, String param2) {
        Fragment_Perfil fragment = new Fragment_Perfil();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_Perfil() {
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
        fragmentview = inflater.inflate(R.layout.fragment_perfil, container, false);
        //Cambiar color al action bar
        ((ActionBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFF289")));

        //INICIALIZAR EL SPINNER
        spinner = (Spinner) fragmentview.findViewById(R.id.spinner_perfil_sex);
        // Create an ArrayAdapter using the string array (string_choice_sex) and a default spinner layout
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.choice_sex, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        //carga de valores de usuario a los campos
        PerfilCargarDatos();

        //listener para los onclicks del boton guardar
        Button buttonGuardar = (Button) fragmentview.findViewById(R.id.button_perfil_guardar);
        buttonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dialogo confirmacion
                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                if(validarCampos()) {
                                    PerfilGuardarPerfilTask pgpt = new PerfilGuardarPerfilTask();
                                    pgpt.execute();
                                }
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

        //listener para los onclicks del boton guardar
        Button buttonEliminar = (Button) fragmentview.findViewById(R.id.button_perfil_eliminar);
        buttonEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dialogo confirmacion
                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                EliminarUsuarioTask eut = new EliminarUsuarioTask();
                                eut.execute();
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

        //permitir solo portrait
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        return fragmentview;
    }

    // Rename method, update argument and hook method into UI event
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

    public static Fragment_Perfil newInstance(int sectionNumber, String user){

        Fragment_Perfil fragment = new Fragment_Perfil();
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
     * Carga los datos del usuario logueado en el formulario.
     */
    private void PerfilCargarDatos(){
        //Cargar datos de usuario de BD local
        //Preparamos la BD local y cargamos los datos perteneciente al usuario que se logueo
        BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(getActivity());
        ContentValues values = db1.selectBDLocal("SELECT * FROM usuarios WHERE email='" + loginUser + "'").get(0); //solo un unico usuario siempre, nunca debería devolver más de 1

        TextView ttext = (TextView)(fragmentview.findViewById(R.id.perfil_Email));
        if(ttext!= null)
            ttext.setText(values.getAsString("email"));

        ttext = (TextView)(fragmentview.findViewById(R.id.perfil_Password));
        if(ttext!= null)
            ttext.setText(values.getAsString("contrasena"));

        EditText etext = (EditText)(fragmentview.findViewById(R.id.perfil_Name));
        if(etext!= null)
            etext.setText(values.getAsString("nombre"));

        etext = (EditText)(fragmentview.findViewById(R.id.perfil_Surname));
        if(etext!= null)
            etext.setText(values.getAsString("apellido"));

        spinner.setSelection(adapter.getPosition(values.getAsString("sexo")));

        etext = (EditText)(fragmentview.findViewById(R.id.perfil_Age));
        if(etext!= null)
            etext.setText(values.getAsString("edad"));

        etext = (EditText)(fragmentview.findViewById(R.id.perfil_Weight));
        if(etext!= null)
            etext.setText(values.getAsString("peso"));

    }

    /**
     * Hilo asíncrono que guarda los datos modificados del perfil de usuario en el servidor y en la base de datos local.
     * Muestra un mensaje con el resultado de la operación.
     */
    private class PerfilGuardarPerfilTask extends AsyncTask<Void, Void, Boolean>{

        String message = "Error";

        @Override
        protected Boolean doInBackground (Void...parameters){

            //_id : autogenerado por la base de datos del servidor
            String query = "UPDATE usuarios SET nombre='" + params.get(2).getValue() +
                    "', apellido='" + params.get(3).getValue() +
                    "', sexo='" + params.get(4).getValue() +
                    "', edad='" + params.get(5).getValue() +
                    "', peso=" + params.get(6).getValue() +
                    " WHERE email='" + params.get(0).getValue() + "'";

            //Log.d("PerfilQueryToModificar ", query);
            try {
                //Update en BD servidor
                httpHandlerQuery handlerAlta = new httpHandlerQuery();
                if(handlerAlta.post(query).equals("done\n")) {
                    //Update en BD local
                    BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(getActivity());
                    db1.queryBDLocal(query);
                    message = getString(R.string.msg_datos_modificados_ok);
                    return true;
                }
                else{
                    //Como fallo al modificarse en el servidor, needupdate encendido en bdlocal
                    BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(getActivity());
                    query = "UPDATE usuarios SET nombre='" + params.get(2).getValue() +
                            "', apellido='" + params.get(3).getValue() +
                            "', sexo='" + params.get(4).getValue() +
                            "', edad='" + params.get(5).getValue() +
                            "', peso='" + params.get(6).getValue() +
                            "', needUpdate=" + 1 +
                            " WHERE email='" + params.get(0).getValue() + "'";
                    db1.queryBDLocal(query);
                    message = getString(R.string.msg_datos_modificados_badServer);
                }
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                //mostrar mensaje de exito
                Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
            else{
                //mostrar mensaje de fracaso
                Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }
    }//fin async task


    /**
     * Hilo asíncrono que elimina el usuario del sistema.
     * Muestra un mensaje con el resultado de la operación.
     */
    private class EliminarUsuarioTask extends AsyncTask<Void, Void, Boolean>{

        String message = "Error";

        @Override
        protected Boolean doInBackground (Void...parameters){
            String query = "DELETE FROM usuarios WHERE email='" + loginUser + "';\n DELETE FROM bicis WHERE usuario='" + loginUser +
                    "';\n DELETE FROM componentes WHERE usuario='" + loginUser + "';\n DELETE FROM actividades WHERE usuario='" + loginUser +"'";
            Log.d("PerfilDELETE ", query);
            try {
                //en BD servidor
                httpHandlerQuery handlerBaja = new httpHandlerQuery();
                if(handlerBaja.post(query).equals("done\n")) {
                    //Baja en BD local
                    BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(getActivity());
                    db1.queryBDLocal("DELETE FROM usuarios WHERE email='" + loginUser + "'");
                    db1.queryBDLocal("DELETE FROM bicis WHERE usuario='" + loginUser + "'");
                    db1.queryBDLocal("DELETE FROM componentes WHERE usuario='" + loginUser + "'");
                    db1.queryBDLocal("DELETE FROM actividades WHERE usuario='" + loginUser + "'");
                    message = getString(R.string.msg_datos_eliminados_ok);

                    return true;
                }
                else{
                    message = getString(R.string.msg_datos_eliminados_badServer);;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                //mostrar mensaje de exito
                Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
                //salir de la aplicacion
                final Handler mHandler = new Handler();
                Runnable mUpdateTimeTask = new Runnable() {
                    public void run() {
                        //hacer finish
                        getActivity().finish();
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
     * Valida los valores modificados del formulario de perfil de usuario y empaqueta de manera nombre-valor.
     * @return true si los valores del usuario se validan correctamente, false en caso contrario.
     */
    private boolean validarCampos(){
        View focusView = null;
        View vvalue;
        String svalue;
        //cada vez que validamos tenemos que vaciar los parametros para su correcta introducción
        params.clear();

        //Email
        vvalue = fragmentview.findViewById(R.id.perfil_Email);
        svalue = ((TextView)vvalue).getText().toString();
        params.add(new BasicNameValuePair("user", svalue));

        //pass
        vvalue = fragmentview.findViewById(R.id.perfil_Password);
        svalue = ((TextView)vvalue).getText().toString();
        params.add(new BasicNameValuePair("pass", svalue));

        //NAME
        vvalue = fragmentview.findViewById(R.id.perfil_Name);
        svalue = ((EditText)vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else if(!isNameOrSurnameValid(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_nombre_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else {
            params.add(new BasicNameValuePair("name", svalue));
        }

        //SURNAME
        vvalue = fragmentview.findViewById(R.id.perfil_Surname);
        svalue = ((EditText)vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else if(!isNameOrSurnameValid(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_nombre_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else {
            params.add(new BasicNameValuePair("surname", svalue));
        }

        //SEX
        params.add(new BasicNameValuePair("sex", ((Spinner)fragmentview.findViewById(R.id.spinner_perfil_sex)).getSelectedItem().toString()));

        //AGE
        vvalue = fragmentview.findViewById(R.id.perfil_Age);
        svalue = ((EditText) vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else if(!isAgeValid(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_edad_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else {
            params.add(new BasicNameValuePair("age", svalue));
        }

        //WEIGHT
        vvalue = fragmentview.findViewById(R.id.perfil_Weight);
        svalue = ((EditText) vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else if(!isWeightValid(svalue)) {
            ((EditText)vvalue).setError(getString(R.string.error_peso_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }
        else {
            params.add(new BasicNameValuePair("weight", svalue));
        }

        return true;
    }

    private boolean isNameOrSurnameValid(String NameSurname){
        if(NameSurname.length() > 40)
            return false;
        return true;
    }

    private boolean isAgeValid(String Age){
        int a;
        try{
            a = Integer.parseInt(Age);
        }
        catch (NumberFormatException e){
            e.printStackTrace();
            return false;
        }
        if(a<1 || a>100)
            return false;
        return true;
    }

    private boolean isWeightValid(String Weight){
        int w;
        try{
            w = Integer.parseInt(Weight);
        }
        catch (NumberFormatException e){
            e.printStackTrace();
            return false;
        }
        if(w<1 || w>300)
            return false;
        return true;
    }

}

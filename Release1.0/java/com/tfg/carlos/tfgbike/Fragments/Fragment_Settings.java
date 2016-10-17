package com.tfg.carlos.tfgbike.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.tfg.carlos.tfgbike.Activity.Activity_Navigation;
import com.tfg.carlos.tfgbike.BD.BaseDeDatosLocalOperaciones;
import com.tfg.carlos.tfgbike.R;

import java.io.File;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Settings.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Settings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Settings extends Fragment {

    private View fragmentview;
    private static String loginUser;

    Spinner spinnerLenguaje;
    SpinnerAdapter adapter;

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
     * @return A new instance of fragment Fragment_Settings.
     */
    public static Fragment_Settings newInstance(String param1, String param2) {
        Fragment_Settings fragment = new Fragment_Settings();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_Settings() {
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
        fragmentview = inflater.inflate(R.layout.fragment_settings, container, false);

        //Spinner Lenguaje inicializacion
        spinnerLenguaje = (Spinner) fragmentview.findViewById(R.id.spinner_Lenguaje);
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.choice_languaje, android.R.layout.simple_spinner_item);
        // Apply the adapter to the spinner
        spinnerLenguaje.setAdapter(adapter);

        //listeners de los botones
        buttonListeners();

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

    //PARA NAVIGATION
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static Fragment_Settings newInstance(int sectionNumber, String user){

        Fragment_Settings fragment = new Fragment_Settings();
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
        public void onFragmentInteraction(Uri uri);
    }

    /**
     * Inicializa los listeners de los diferentes botones.
     */
    private void buttonListeners(){
        //listener boton borrar base de datos
        Button button = (Button) fragmentview.findViewById(R.id.button_settings_deleteSql);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteBDLocal();
            }
        });
        //listener boton borrar base de datos info
        ImageButton imageButton1 = (ImageButton) fragmentview.findViewById(R.id.button_settings_deleteSql_info);
        imageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarInfo(getString(R.string.setting_borrar_base));
            }
        });

        //listener boton borrar datos de usuario
        Button button2 = (Button) fragmentview.findViewById(R.id.button_settings_deleteUser);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUserFromBDLocal(loginUser);
            }
        });
        //listener boton borrar datos de usuario info
        ImageButton imageButton2 = (ImageButton) fragmentview.findViewById(R.id.button_settings_deleteUser_info);
        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarInfo(getString(R.string.setting_borrar_base_usuario));
            }
        });

        //Listener Spinner Lenguaje
        spinnerLenguaje.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int i, long l) {
                if (i == 2)
                    setLocale("en");

                if (i == 1)
                    setLocale("es");
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        //listener boton lenguaje info
        ImageButton imageButton3 = (ImageButton) fragmentview.findViewById(R.id.button_settings_lenguaje_info);
        imageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarInfo(getString(R.string.setting_lenguaje));
            }
        });


        //listener boton Acerca de
        Button button4 = (Button) fragmentview.findViewById(R.id.button_settings_acercaDe);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarInfo("Aplicacion creada por:\nCarlos Vidal Bodelon\n\nProyecto de fin de grado\nIngenieria Informatica en Tecnologias de la Informacion\nEPI Gijon, Universidad de Oviedo\n\n2015 version: 1.0");
            }
        });
        //listener boton Acerca de info
        ImageButton imageButton4 = (ImageButton) fragmentview.findViewById(R.id.button_settings_acercaDe_info);
        imageButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarInfo(getString(R.string.setting_acerca));
            }
        });
    }

    /**
     * Borrado de la base de datos local.
     * Muestra mensaje con el resultado de la operación.
     * @return true si correcto, false en cualquier otro caso.
     */
    private boolean deleteBDLocal(){
        File file = new File("/data/data/com.tfg.carlos.tfgbike/databases/db_TFGBike");
        TextView ttext = (TextView) (fragmentview.findViewById(R.id.settings_info));

        if(file.exists()) {
            if(file.delete()) {
                if (ttext != null)
                    ttext.setText(getString(R.string.setting_msg_base_borrada_ok));

                //esperar para hacer finish
                final Handler mHandler = new Handler();
                Runnable mUpdateTimeTask = new Runnable() {
                    public void run() {
                        getActivity().finish();
                    }
                };
                mHandler.postDelayed(mUpdateTimeTask, 2000);

                return true;
            }
        }
        ttext.setText(getString(R.string.setting_msg_base_borrada_bad));
        return false;
    }

    /**
     * Borrado de datos de solo el usuario logueado
     * Muestra mensaje con el resultado de la operación.
     * @param user String con el identificador de usuario logueado.
     * @return true si correcto, false en cualquier otro caso.
     */
    private boolean deleteUserFromBDLocal(String user){
        BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(getActivity());

        try{
            db1.queryBDLocal("DELETE FROM usuarios WHERE email='" + user + "'");
            db1.queryBDLocal("DELETE FROM bicis WHERE usuario='" + user + "'");
            db1.queryBDLocal("DELETE FROM componentes WHERE usuario='" + user + "'");
            db1.queryBDLocal("DELETE FROM actividades WHERE usuario='" + user + "'");

            TextView ttext = (TextView) (fragmentview.findViewById(R.id.settings_info));
            if (ttext != null)
                ttext.setText(getString(R.string.setting_msg_busuario_borrada_ok));

            //esperar para hacer finish
            final Handler mHandler = new Handler();
            Runnable mUpdateTimeTask = new Runnable() {
                public void run() {
                    getActivity().finish();
                }
            };
            mHandler.postDelayed(mUpdateTimeTask, 2000);

            return true;
        }catch (Exception e){
            e.printStackTrace();
            TextView ttext = (TextView) (fragmentview.findViewById(R.id.settings_info));
            if (ttext != null)
                ttext.setText(getString(R.string.setting_msg_busuario_borrada_bad));
        }
        return false;
    }


    /**
     * Muestra un diálogo con información.
     * @param message String que contiene la información a mostrar.
     */
    private void mostrarInfo(String message){
        //dialogo confirmacion
        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_NEUTRAL:
                        break;
                }
            }
        };
        ProgressDialog.Builder builder = new ProgressDialog.Builder(getActivity());
        builder.setMessage(message).setNeutralButton(R.string.dialog_ok, dialogClickListener).show();
    }

    /**
     * Cambia el idioma de la aplicación.
     * Carga los strings.xml de las diferentes carpetas de idiomas.
     * @param lang String memotécnico del lenguaje al que se cambiará la aplicación.
     */
    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(getActivity(), Activity_Navigation.class);
        //paso de parametros
        refresh.putExtra("loginUser", loginUser);
        startActivity(refresh);
        getActivity().finish();
    }

}

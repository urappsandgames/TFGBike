package com.tfg.carlos.tfgbike.Fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.tfg.carlos.tfgbike.Activity.Activity_Activity;
import com.tfg.carlos.tfgbike.Activity.Activity_Navigation;
import com.tfg.carlos.tfgbike.BD.BaseDeDatosLocalOperaciones;
import com.tfg.carlos.tfgbike.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Activity.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Activity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Activity extends Fragment {

    static String loginUser;
    private View fragmentview;

    private Spinner spinnerbici;
    private SpinnerAdapter adapterbici;

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
     * @return A new instance of fragment Fragment_Activity.
     */
    // Rename and change types and number of parameters
    public static Fragment_Activity newInstance(String param1, String param2) {
        Fragment_Activity fragment = new Fragment_Activity();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_Activity() {
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
        fragmentview = inflater.inflate(R.layout.fragment_activity, container, false);

        //inicializar spinner bici
        setSpinnerbici();

        //lista de actividades realizadas
        cargaListaActividades();

        //todo mostrar progresion hasta que se cargue la actividad
        //listener boton nueva actividad
        Button button = (Button) fragmentview.findViewById(R.id.b_nueva_actividad);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view){
                //si el spinner esta vacio no permitir nueva actividad y mostrar mensaje
                if(spinnerbici.getSelectedItem() ==null){
                    //mostrar mensaje
                    Toast.makeText(getActivity().getApplicationContext(), "Debes agregar una bicicleta primero", Toast.LENGTH_LONG).show();
                }
                else {
                    //lanzar nueva actividad enviando parametro
                    Intent intent = new Intent(getActivity(), Activity_Activity.class);
                    Bundle b = new Bundle();
                    b.putString("loginUser", loginUser);
                    b.putString("biciUser", spinnerbici.getSelectedItem().toString());
                    intent.putExtras(b);
                    startActivity(intent);
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

    public static Fragment_Activity newInstance(int sectionNumber, String user){

        Fragment_Activity fragment = new Fragment_Activity();
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
     * Inicializa el spinner con los modelos de las bicis del usuario.
     * Carga el modelo de las bicis de usuario en el spinner bici.
     */
    private void setSpinnerbici(){
        BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(getActivity());
        ArrayList<ContentValues> result= db1.selectBDLocal("SELECT modelo, predeterminada FROM bicis WHERE usuario='" + loginUser + "'");
        ArrayList<CharSequence> modelosSpinner = new ArrayList<>();

        for(int i=0; result.size()>i; i++) {
            modelosSpinner.add(result.get(i).getAsString("modelo"));
        }
        spinnerbici = (Spinner) fragmentview.findViewById(R.id.spinner_activity_bici);
        // Create an ArrayAdapter using models of the user bikes
        adapterbici = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, modelosSpinner);
        // Apply the adapter to the spinner
        spinnerbici.setAdapter(adapterbici);

    }


    /**
     * Crea una Vista o interfaz.
     * Carga en ella la lista de actividades de usuario ordenadas por fecha (nombre de fichero)
     */
    private void cargaListaActividades(){
        Context context = getActivity();
        LinearLayout llActividad;
        TextView text;
        ImageButton bver;

        // buscar ScrollView
        ScrollView sv = (ScrollView) fragmentview.findViewById(R.id.Activity_List_Scrollview);
        // Crear a LinearLayout element
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        //tabla de actividades
        TableLayout ta = new TableLayout(context);
        ta.setStretchAllColumns(false);
        ta.setBackgroundColor(Color.BLACK);

        //busqueda de actividades del usuario
        BaseDeDatosLocalOperaciones bd = new BaseDeDatosLocalOperaciones(context);
        ArrayList<ContentValues> actividades;
        actividades = bd.selectBDLocal("SELECT _id, idbici, date FROM actividades WHERE usuario='" + loginUser +"'");
        //todo ordenar por date actividad
        //Lista actividades
        int z=0;
        if(actividades!=null){
            for(int i=0; i<actividades.size(); i++) {
                TableRow tr = new TableRow(context);
                tr = new TableRow(context);
                //id actividad
                final String idActividad = actividades.get(i).getAsString("_id");

                //idbici actividad
                text = new TextView(context);
                text.setText(idToModeloBici(actividades.get(i).getAsString("idbici")));
                text.setPadding(0,0,20,0);
                tr.addView(text);
                //date actividad
                text = new TextView(context);
                text.setText(actividades.get(i).getAsString("date"));
                text.setPadding(0,0,20,0);
                tr.addView(text);

                //boton ver actividad
                bver = new ImageButton(context);
                bver.setImageResource(android.R.drawable.ic_menu_view);
                bver.setMaxHeight(5);
                bver.setMaxWidth(5);
                bver.setId(400 + i);
                //listener para boton
                bver.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack if needed
                        transaction.replace(R.id.container, Fragment_Activity_View.newInstance(13 + 1, idActividad))    //param1 = login user, param2 = id del componente a modificar
                            .addToBackStack("");
                        // Commit the transaction
                        transaction.commit();
                    }
                });
                tr.addView(bver);

                //colores de las filas de la tabla
                if(z%2 == 0) {
                    tr.setBackgroundColor(Color.parseColor("#F5F5F5"));
                }
                else{
                    tr.setBackgroundColor(Color.parseColor("#FFFFFF"));
                }
                z++;

                ta.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
            }
            // Add la tabla al LinearLayout, y este al ScrollView
            ll.addView(ta, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
            sv.addView(ll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }
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

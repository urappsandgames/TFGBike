package com.tfg.carlos.tfgbike.Fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.tfg.carlos.tfgbike.Activity.Activity_Navigation;
import com.tfg.carlos.tfgbike.BD.BaseDeDatosLocalOperaciones;
import com.tfg.carlos.tfgbike.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Bici.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Bici#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Bici extends Fragment {

    static String loginUser;
    private View fragmentview;

    Spinner spinnerbici;
    ArrayAdapter<CharSequence> adapterbici;
    ArrayList<ContentValues> values;

    ImageButton bmod;
    ImageButton beliminar;
    String idbici = null;

    Double gastoBici;

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
     * @return A new instance of fragment Fragment_Bici.
     */
    public static Fragment_Bici newInstance(String param1, String param2) {
        Fragment_Bici fragment = new Fragment_Bici();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_Bici() {
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
        fragmentview = inflater.inflate(R.layout.fragment_bici, container, false);
        //Cambiar color al action bar
        ((ActionBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF91DB90")));

        //INICIALIZAR SPINNER bicis y carga de datos
        spinnerbici = (Spinner) fragmentview.findViewById(R.id.spinner_bici);
        setSpinnerbici();

        //Carga datos cuando OnClick spinner, la vista se refresca para cargar los datos de la nueva bici seleccionada
        spinnerbici.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                biciCargarDatos(spinnerbici.getSelectedItem().toString());
                cargaComponentesBici();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        //listener para los onclicks del boton modificar bici
        Button buttonMod = (Button) fragmentview.findViewById(R.id.button_bici_mod);
        buttonMod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction()
                .replace(R.id.container, Fragment_Bici_Mod.newInstance(10 + 1, values, loginUser))
                        .addToBackStack("");
                // Commit the transaction
                transaction.commit();
            }
        });

        //listener para los onclicks del boton nueva bici
        Button buttonNew = (Button) fragmentview.findViewById(R.id.button_bici_new);
        buttonNew.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view){
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, Fragment_Bici_New.newInstance(10 + 1, loginUser))
                        .addToBackStack("");
                // Commit the transaction
                transaction.commit();
            }
        });

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

    public static Fragment_Bici newInstance(int sectionNumber, String user){

        Fragment_Bici fragment = new Fragment_Bici();
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
     * Carga el modelo de las bicis de usuario en el spinner bici y deja seleccionada la primera.
     * todo en vez de la primera hacer que deje seleccionada la predeterminada.
     */
    private void setSpinnerbici(){
        BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(getActivity());
        ArrayList<ContentValues> result= db1.selectBDLocal("SELECT modelo FROM bicis WHERE usuario='" + loginUser + "'");
        //si el usuario no tiene bicis, cargar fragemento bici nueva
        if(result.isEmpty()){
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.container, Fragment_Bici_New.newInstance(10 + 1, loginUser));
            transaction.commit();
            return;
        }

        ArrayList<CharSequence> modelosSpinner = new ArrayList<>();
        String predeterminada = result.get(0).getAsString("modelo");

        for(int i=0; result.size()>i; i++) {
            modelosSpinner.add(result.get(i).getAsString("modelo"));
        }

        // Create an ArrayAdapter using models of the user bikes
        adapterbici = new ArrayAdapter<CharSequence> (getActivity(), android.R.layout.simple_spinner_dropdown_item, modelosSpinner);
        // Apply the adapter to the spinner
        spinnerbici.setAdapter(adapterbici);
        //dejar seleccionado la bici predeterminada
        spinnerbici.setSelection(adapterbici.getPosition(predeterminada));

        biciCargarDatos(predeterminada);

    }


    /**
     * Al crear el fragmento lee la base de datos local para cargar los datos de la bici del usuario dado el modelo en los formularios
     * @param modelo modelo de la bicicleta de la que se cargarán los datos en el formulario.
     */
    private void biciCargarDatos(String modelo){
        //Preparamos la BD local y cargamos los datos de la bici perteneciente al usuario que se logueo
        BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(getActivity());
        values = db1.selectBDLocal("SELECT * FROM bicis WHERE usuario='" + loginUser + "' AND modelo='" + modelo + "'");

        //para calculo de gasto = precio bici + suma de gasto de componentes
        gastoBici = values.get(0).getAsDouble("precio");

        if(!values.isEmpty()) {
            //guardar la idbici para cargar los componentes posteriormente
            idbici = values.get(0).getAsString("_id");

            TextView ttext = (TextView) (fragmentview.findViewById(R.id.bici_marca));
            if (ttext != null)
                ttext.setText(values.get(0).getAsString("marca"));

            ttext = (TextView) (fragmentview.findViewById(R.id.bici_modelo));
            if (ttext != null)
                ttext.setText(values.get(0).getAsString("modelo"));

            ttext = (TextView) (fragmentview.findViewById(R.id.bici_talla));
            if (ttext != null)
                ttext.setText(values.get(0).getAsString("peso"));

            ttext = (TextView) (fragmentview.findViewById(R.id.bici_tipo));
            if (ttext != null)
                ttext.setText(values.get(0).getAsString("tipo"));

            ttext = (TextView) (fragmentview.findViewById(R.id.bici_precio));
            if (ttext != null)
                ttext.setText(values.get(0).getAsString("precio") + " €");

            ttext = (TextView) (fragmentview.findViewById(R.id.bici_km));
            if (ttext != null)
                ttext.setText(values.get(0).getAsString("km"));

            ttext = (TextView) (fragmentview.findViewById(R.id.bici_gasto));
            if (ttext != null)
                ttext.setText(values.get(0).getAsString("gasto") + " €");

        }
        else{
            //Algun fallo en la lectura de la BD local
            //getFragmentManager().beginTransaction().replace(R.id.container, Fragment_Bici.newInstance(11, loginUser)).commit();
        }
    }

    /**
     * Crea una vista de tabla con los datos de los componentes asociados a la bicicleta.
     * todo, opcional cargar otra fila mas al final con el precio, gasto total de componentes
     */
    private void cargaComponentesBici(){

        Context context = getActivity();
        // Find the LinearLayout element
        LinearLayout ll = (LinearLayout) fragmentview.findViewById(R.id.ll_bici);
        //borramos si habia algo en el ll componentes
        ll.removeAllViews();

        /*/añadir boton nuevo componente
        Button bNewComponent = new Button(context);
        bNewComponent.setText("*Nuevo componente");
        bNewComponent.setId(101);  //error fantasma, se queja de posibles errores futuros al referirnos al id con string en vez de con int, con View.getId() evitamos fallos de referencia
        ll.addView(bNewComponent);*/

        //añadir tabla de componentes
        TableLayout tcomponentes = new TableLayout(context);
        tcomponentes.setShrinkAllColumns(true);         //ajustar al ancho de pantalla las columnas
        tcomponentes.setBackgroundColor(Color.parseColor("#3C3C3C"));

        TableRow rowcabecera = new TableRow(context);
        rowcabecera.setBackgroundColor(Color.parseColor("#E2A9F3"));
        rowcabecera.setPadding(0,0,0,2);

        TextView tipo = new TextView(context);
        tipo.setText("Tipo");
        tipo.setPadding(0,0,1,0);
        rowcabecera.addView(tipo);

        TextView marca = new TextView(context);
        marca.setText("Marca");
        marca.setPadding(1,0,1,0);
        rowcabecera.addView(marca);

        TextView modelo = new TextView(context);
        modelo.setText("Modelo");
        modelo.setPadding(1,0,1,0);
        rowcabecera.addView(modelo);

        TextView precio = new TextView(context);
        precio.setText("Precio(€)");
        precio.setPadding(1,0,1,0);
        rowcabecera.addView(precio);

        TextView estado = new TextView(context);
        estado.setText("Estado");
        estado.setPadding(1,0,1,0);
        rowcabecera.addView(estado);

        TextView modificar = new TextView(context);
        modificar.setText("Modificar");
        modificar.setPadding(1,0,1,0);
        rowcabecera.addView(modificar);

        TextView eliminar = new TextView(context);
        eliminar.setText("Eliminar");
        eliminar.setPadding(1,0,0,0);
        rowcabecera.addView(eliminar);

        tcomponentes.addView(rowcabecera);

        //obtener componentes de la bd
        BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(getActivity());
        ArrayList<ContentValues> result= db1.selectBDLocal("SELECT * FROM componentes WHERE idbici='" + idbici + "'");

        //bucle introduccion de datos de los componentes en filas de la tabla
        int z=0;
        TableRow tr = new TableRow(context);
        for(int i=0; i<result.size(); i++){
            final String indexComponent = result.get(i).getAsString("_id");

            tipo = new TextView(context);
            tipo.setText(result.get(i).getAsString("tipo"));
            tr.addView(tipo);

            marca = new TextView(context);
            marca.setText(result.get(i).getAsString("marca"));
            tr.addView(marca);

            modelo = new TextView(context);
            modelo.setText(result.get(i).getAsString("modelo"));
            tr.addView(modelo);

            precio = new TextView(context);
            precio.setText(result.get(i).getAsString("precio"));
            tr.addView(precio);

            //gasto calculo
            gastoBici += result.get(i).getAsDouble("gasto");

            estado = new TextView(context);
            estado.setText(result.get(i).getAsString("estado"));
            tr.addView(estado);

            //boton modificar componente
            bmod = new ImageButton(context);
            bmod.setImageResource(R.drawable.ic_action_mod);
            bmod.setMaxHeight(5);
            bmod.setMaxWidth(5);
            bmod.setId(400 + i);

            //listener para boton modificar componente
            bmod.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack if needed
                    transaction.replace(R.id.container, Fragment_Componentes_Mod.newInstance(10 + 1, indexComponent, loginUser)); //param1 = login user, param2 = id del componente a modificar
                    transaction.addToBackStack(null);
                    // Commit the transaction
                    transaction.commit();
                }
            });
            tr.addView(bmod);

            //boton eliminar componente
            beliminar = new ImageButton(context);
            beliminar.setImageResource(R.drawable.ic_action_eliminar);
            beliminar.setMaxHeight(5);
            beliminar.setMaxWidth(5);
            beliminar.setId(800 + i);
            //listener para boton eliminar componente
            beliminar.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //eliminarComponente(indexComponent);
                }
            });
            tr.addView(beliminar);

            //añadir vista de la fila a la tabla
            tcomponentes.addView(tr);

            //colores de las filas de la tabla
            if(z%2 == 0) {
                tr.setBackgroundColor(Color.parseColor("#F5F5F5"));
            }
            else{
                tr.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            z++;
            //inicializar nueva fila
            tr = new TableRow(context);
        }

        ll.addView(tcomponentes, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        //actualizar gasto en la info de la bici
        TextView ttext = (TextView) (fragmentview.findViewById(R.id.bici_gasto));
        if (ttext != null)
            ttext.setText(gastoBici + " €");

    }//fin cargaCustomView


    /**
     * @deprecated
     * Refresca la vista del fragmento.
     * Usado cuando necesitamos que se reflejen los cambios en la lista de componentes.
     * Como invalidate o postInvalidate no funcionaba correctamente,
     * desacoplamos el fragmento, y lo volvemos a cargar. No es elegante pero funciona.
     */
    public void refreshView() {
        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.container);
        if ((currentFragment instanceof Fragment_Bici)) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.detach(currentFragment);
            transaction.attach(currentFragment);
            transaction.replace(R.id.container, currentFragment);
            transaction.commit();
        }
    }
}

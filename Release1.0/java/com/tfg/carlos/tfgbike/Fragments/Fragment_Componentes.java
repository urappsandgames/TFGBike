package com.tfg.carlos.tfgbike.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.tfg.carlos.tfgbike.Activity.Activity_Navigation;
import com.tfg.carlos.tfgbike.BD.BaseDeDatosLocalOperaciones;
import com.tfg.carlos.tfgbike.R;
import com.tfg.carlos.tfgbike.httpHandlerQuery;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Componentes.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Componentes#newInstance} factory method to
 * create an instance of this fragment.
 */

public class Fragment_Componentes extends Fragment {
    static String loginUser;
    View fragmentview;

    ScrollView sv;

    ImageButton bmod;
    ImageButton beliminar;

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
     * @return A new instance of fragment Fragment_Componentes.
     */
    public static Fragment_Componentes newInstance(String param1, String param2) {
        Fragment_Componentes fragment = new Fragment_Componentes();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_Componentes() {
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
        fragmentview = inflater.inflate(R.layout.fragment_componentes, container, false);
        //Cambiar color al action bar
        ((ActionBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF90C9FF")));

        cargaCustomView();

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

    public static Fragment_Componentes newInstance(int sectionNumber, String user){

        Fragment_Componentes fragment = new Fragment_Componentes();
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
     * Refresca la vista del fragmento.
     * Usado cuando necesitamos que se reflejen los cambios en la lista de componentes.
     * Como invalidate o postInvalidate no funcionaba correctamente,
     * desacoplamos el fragmento, y lo volvemos a cargar. No es elegante pero funciona.
     */
    public void refreshView() {
        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.container);
        if ((currentFragment instanceof Fragment_Componentes)) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.detach(currentFragment);
            transaction.attach(currentFragment);
            transaction.replace(R.id.container, currentFragment);
            transaction.commit();
        }
    }

    @SuppressWarnings("ResourceType")
    /**
     * Crea la vista de la interfaz y la lista de componentes del usuario.
     * También crea los botones para eliminar o modificar cada componente.
     * //todo (opcional) Las cabeceras de la tabla tienen que ser clicables para ordenar por
     */
    private void cargaCustomView(){

        Context context = getActivity();

        // Find the ScrollView
        sv = (ScrollView) fragmentview.findViewById(R.id.scrollView1);

        // Create a LinearLayout element
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        //añadir boton nuevo componente
        Button bNewComponent = new Button(context);
        bNewComponent.setText("*Nuevo componente");
        bNewComponent.setId(101);  //error fantasma, se queja de posibles errores futuros al referirnos al id con string en vez de con int, con View.getId() evitamos fallos de referencia
        ll.addView(bNewComponent);

        //añadir tabla de componentes
        TableLayout tcomponentes = new TableLayout(context);
        tcomponentes.setShrinkAllColumns(true);         //ajustar al ancho de pantalla las columnas
        tcomponentes.setBackgroundColor(Color.parseColor("#3C3C3C"));

        TableRow rowcabecera = new TableRow(context);
        rowcabecera.setBackgroundColor(Color.parseColor("#E2A9F3"));
        rowcabecera.setPadding(0, 0, 0, 2);

        TextView tipo = new TextView(context);
        tipo.setText("Tipo");
        tipo.setPadding(0, 0, 1, 0);
        rowcabecera.addView(tipo);

        //modelo de bici obtenido de idbici de componente
        TextView bici = new TextView(context);
        bici.setText("Bici");
        bici.setPadding(1,0,1,0);
        rowcabecera.addView(bici);

        TextView marca = new TextView(context);
        marca.setText("Marca");
        marca.setPadding(1,0,1,0);
        rowcabecera.addView(marca);

        TextView modelo = new TextView(context);
        modelo.setText("Modelo");
        modelo.setPadding(1,0,1,0);
        rowcabecera.addView(modelo);

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
        ArrayList<ContentValues> result= db1.selectBDLocal("SELECT * FROM componentes WHERE usuario='" + loginUser + "'");

        //bucle introduccion de datos de los componentes en filas de la tabla
        int z=0;
        TableRow tr = new TableRow(context);
        for(int i=0; i<result.size(); i++){

            final String indexComponent = result.get(i).getAsString("_id");

            tipo = new TextView(context);
            tipo.setText(result.get(i).getAsString("tipo"));
            tr.addView(tipo);

            bici = new TextView(context);
            bici.setText(idToModeloBici(result.get(i).getAsString("idbici")));
            tr.addView(bici);

            marca = new TextView(context);
            marca.setText(result.get(i).getAsString("marca"));
            tr.addView(marca);

            modelo = new TextView(context);
            modelo.setText(result.get(i).getAsString("modelo"));
            tr.addView(modelo);

            estado = new TextView(context);
            estado.setText(result.get(i).getAsString("estado"));
            tr.addView(estado);

            //boton modificar componente
            bmod = new ImageButton(context);
            bmod.setImageResource(R.drawable.ic_action_mod);
            bmod.setMaxHeight(5);
            bmod.setMaxWidth(5);
            bmod.setId(400 + i);

            //listener para boton modificar
            bmod.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, Fragment_Componentes_Mod.newInstance(10 + 1, indexComponent, loginUser)) //param1 = login user, param2 = id del componente a modificar
                    .addToBackStack("");
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
            //listener para boton eliminar
            beliminar.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //dialogo confirmacion
                    final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    ComponenteEliminarTask cet = new ComponenteEliminarTask();
                                    cet.execute(indexComponent);
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
        // Add the LinearLayout element to the ScrollView
        sv.addView(ll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        //listener para boton nuevo componente
        Button button = (Button) fragmentview.findViewById(bNewComponent.getId());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, Fragment_Componentes_New.newInstance(11 + 1, loginUser))
                    .addToBackStack(null);
                // Commit the transaction
                transaction.commit();
            }
        });

    }//fin cargaCustomView

    /**
     * Obtiene el modelo de la bici a partir de su identificador
     * @param idbici String que se corresponde con el campo "_id" de la bicicleta que queremos saber el modelo.
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


    /**
     * Hilo asíncrono que elimina el componente especificado por el id del componente.
     * El id nos lo proporciona el botón de eliminar componente cuando lo pulsamos.
     * Muestra un mensaje con el resultado de la operación.
     */
    private class ComponenteEliminarTask extends AsyncTask<String, Void, Boolean> {

        String message = "Error";

        @Override
        protected Boolean doInBackground(String... parameters) {
            String idComponente = parameters[0];
            String query = "DELETE FROM componentes WHERE _id='" + idComponente + "'";
            try{
                //eliminar del servidor
                httpHandlerQuery delete = new httpHandlerQuery();
                String resultado = delete.post(query);
                //Log.d("FragmentComponentes","eliminar componente de servidor: " + query + "\nresultado= " + resultado);
                if(resultado.equals("done\n")) {
                    //eliminar de la base local
                    BaseDeDatosLocalOperaciones bd = new BaseDeDatosLocalOperaciones(getActivity());
                    bd.queryBDLocal(query);
                    message=getString(R.string.msg_datos_eliminados_ok);
                    //refrescar la vista del fragment
                    refreshView();
                    return true;
                }
                else {
                    message=getString(R.string.msg_datos_eliminados_badServer);
                    return false;
                }
            } catch(Exception e){
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                //mostrar mensaje del resultado de la operacion
                Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
            else{
                //mostrar mensaje de fracaso
                Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }
    }// fin async task



}

package com.tfg.carlos.tfgbike.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.tfg.carlos.tfgbike.Activity.Activity_Consulta;
import com.tfg.carlos.tfgbike.Activity.Activity_Navigation;
import com.tfg.carlos.tfgbike.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Consultas.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Consultas#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Consultas extends Fragment {

    private View fragmentview;
    static String loginUser;

    private Context context;
    private ScrollView sv;
    private LinearLayout ll;
    private TextView text;
    private Spinner spinnerConsulta;
    private ArrayAdapter<CharSequence> adapterConsulta;
    String spinnerValue;
    Spinner spinner_consultaTipo;
    ArrayAdapter<CharSequence> adapter_consultaTipo;
    Spinner spinner_consultaUser;
    Spinner spinner_consultaPrecio;
    Spinner spinner_consultaPeso;

    EditText etext_precio;
    EditText etext_peso;

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
     * @return A new instance of fragment Fragment_Consultas.
     */
    public static Fragment_Consultas newInstance(String param1, String param2) {
        Fragment_Consultas fragment = new Fragment_Consultas();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_Consultas() {
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
        fragmentview = inflater.inflate(R.layout.fragment_consultas, container, false);

        context = getActivity();

        //SPINNER
        spinnerConsulta = new Spinner(context);
        // Create an ArrayAdapter using the string array (string_choice_sex) and a default spinner layout
        adapterConsulta = ArrayAdapter.createFromResource(getActivity(),
                R.array.choice_consulta, android.R.layout.simple_spinner_item);
        // Apply the adapter to the spinner
        spinnerConsulta.setAdapter(adapterConsulta);
        //listener spinner tipo de consulta
        spinnerConsulta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                spinnerValue = spinnerConsulta.getSelectedItem().toString();
                String[] myResArray;
                if(spinnerValue.equals("Bicis")) {
                    myResArray = getResources().getStringArray(R.array.choice_bikeType);
                }
                else {
                    myResArray = getResources().getStringArray(R.array.choice_componentType);
                }
                List<String> myResArrayList = Arrays.asList(myResArray);
                List<String> myResMutableList = new ArrayList<String>(myResArrayList);
                //anadir Cualquiera al principio del spinner
                myResMutableList.add(0,"Cualquiera");
                adapter_consultaTipo = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, myResMutableList);
                spinner_consultaTipo.setAdapter(adapter_consultaTipo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        spinnerValue = spinnerConsulta.getSelectedItem().toString();

        customView();

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

    public static Fragment_Consultas newInstance(int sectionNumber, String user){

        Fragment_Consultas fragment = new Fragment_Consultas();
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
     * Construye la interfaz del formulario para realizar una consulta.
     */
    private void customView(){

        // Find the ScrollView
        sv = (ScrollView) fragmentview.findViewById(R.id.scrollView_consultas);
        // Create a LinearLayout element
        ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(10,20,10,20);

        text = new TextView(context);
        text.setText(getString(R.string.consulta_tipo_consulta));
        ll.addView(text);
        spinnerConsulta.setBackgroundColor(Color.WHITE);
        ll.addView(spinnerConsulta);


        //anadir tabla
        TableLayout tconsulta = new TableLayout(context);
        tconsulta.setShrinkAllColumns(true);         //ajustar al ancho de pantalla las columnas
        tconsulta.setStretchAllColumns(true);

        //parametros para las filas de la tabla
        TableLayout.LayoutParams tableRowParams =
                new TableLayout.LayoutParams
                        (TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
        tableRowParams.setMargins(20, 20, 20, 0);

        //columna user
        TableRow tr = new TableRow(context);
        tr.setLayoutParams(tableRowParams);
        text = new TextView(context);
        text.setText(getString(R.string.consulta_propietario));

        spinner_consultaUser = new Spinner(context);
        spinner_consultaUser.setBackgroundColor(Color.WHITE);
        ArrayAdapter<CharSequence> adapter_consultaUser;
        adapter_consultaUser = ArrayAdapter.createFromResource(getActivity(), R.array.choice_consultaUser, android.R.layout.simple_spinner_item);
        spinner_consultaUser.setAdapter(adapter_consultaUser);

        tr.addView(text);
        tr.addView(spinner_consultaUser);
        tconsulta.addView(tr);

        //columna tipo
        tr = new TableRow(context);
        tr.setLayoutParams(tableRowParams);
        text = new TextView(context);
        text.setText(getString(R.string.consulta_tipo));

        //spinner de tipo de componente o tipo de bici
        spinner_consultaTipo = new Spinner(context);
        spinner_consultaTipo.setBackgroundColor(Color.WHITE);
        if(spinnerValue.equals("Bicis"))
            adapter_consultaTipo = ArrayAdapter.createFromResource(getActivity(), R.array.choice_bikeType, android.R.layout.simple_spinner_item);
        else
            adapter_consultaTipo = ArrayAdapter.createFromResource(getActivity(), R.array.choice_componentType, android.R.layout.simple_spinner_item);
        spinner_consultaTipo.setAdapter(adapter_consultaTipo);

        tr.addView(text);
        tr.addView(spinner_consultaTipo);
        tconsulta.addView(tr);

        //Columna precio
        tr = new TableRow(context);
        tr.setLayoutParams(tableRowParams);
        text = new TextView(context);
        text.setText(getString(R.string.consulta_precio));

        spinner_consultaPrecio = new Spinner(context);
        spinner_consultaPrecio.setBackgroundColor(Color.WHITE);
        ArrayAdapter<CharSequence> adapter_consultaPrecio;
        adapter_consultaPrecio = ArrayAdapter.createFromResource(getActivity(), R.array.choice_consulta_Mayormenor, android.R.layout.simple_spinner_item);
        spinner_consultaPrecio.setAdapter(adapter_consultaPrecio);

        etext_precio = new EditText(context);
        etext_precio.setBackgroundColor(Color.WHITE);
        etext_precio.setText("0");
        etext_precio.setId(901);
        //manera de establecer limite de caracteres de un edit text usando codigo
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(10);
        etext_precio.setFilters(FilterArray);

        tr.addView(text);
        tr.addView(spinner_consultaPrecio);
        tr.addView(etext_precio);
        tconsulta.addView(tr);

        //Columna peso
        tr = new TableRow(context);
        tr.setLayoutParams(tableRowParams);
        text = new TextView(context);
        text.setText(getString(R.string.consulta_peso));

        spinner_consultaPeso = new Spinner(context);
        spinner_consultaPeso.setBackgroundColor(Color.WHITE);
        ArrayAdapter<CharSequence> adapter_consultaPeso;
        adapter_consultaPeso = ArrayAdapter.createFromResource(getActivity(), R.array.choice_consulta_Mayormenor, android.R.layout.simple_spinner_item);
        spinner_consultaPeso.setAdapter(adapter_consultaPeso);

        etext_peso = new EditText(context);
        etext_peso.setBackgroundColor(Color.WHITE);
        etext_peso.setText("0");
        etext_peso.setId(902);
        //manera de establecer limite de caracteres de un edit text usando codigo
        InputFilter[] FilterArray2 = new InputFilter[1];
        FilterArray2[0] = new InputFilter.LengthFilter(10);
        etext_peso.setFilters(FilterArray2);

        tr.addView(text);
        tr.addView(spinner_consultaPeso);
        tr.addView(etext_peso);
        tconsulta.addView(tr);

        ll.addView(tconsulta, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        //boton busqueda consulta
        Button bbuscar = new Button(context);
        bbuscar.setText(getString(R.string.consulta_buscar));
        //listener para boton
        bbuscar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                realizar_consulta();
            }
        });
        ll.addView(bbuscar);


        sv.addView(ll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

    }


    /**
     * Construye la query para realizar la consulta.
     * Inicia una nueva actividad donde se ejecutara la query y mostraran los resultados de esta.
     */
    private void realizar_consulta(){
        if(validarDatos()){

            boolean local=false;
            String operadorPrecio=">=";
            String operadorPeso=">=";
            String elementoTipo =""; //cuando se elige Cualquiera

            if(spinner_consultaPrecio.getSelectedItem().toString().equals("Menor que"))
                operadorPrecio = "<=";

            if(spinner_consultaPeso.getSelectedItem().toString().equals("Menor que"))
                operadorPeso = "<=";

            if(!(spinner_consultaTipo.getSelectedItem().toString().equals("Cualquiera")))
                elementoTipo = "tipo='" + spinner_consultaTipo.getSelectedItem().toString() + "' AND ";

            String query = "SELECT * FROM " + spinnerValue.toLowerCase() +" WHERE " + elementoTipo
                    + "precio" + operadorPrecio + etext_precio.getText().toString() +" AND peso" + operadorPeso + etext_peso.getText().toString();

            //si solo queremos consultar nuestros objetos, realizamos una consulta local ahorrando conexiones al servidor
            if((spinner_consultaUser.getSelectedItem().toString()).equals("Yo")) {
                query = query + " AND usuario='" + loginUser + "'";
                local = true;
            }

            //lanzar nueva actividad enviando como parametro la query
            Intent intent = new Intent(context, Activity_Consulta.class);
            Bundle b = new Bundle();
            b.putString("query", query);
            b.putBoolean("local", local);
            intent.putExtras(b);
            startActivity(intent);

        }

    }

    /**
     * Valida los datos del formulario de consultas.
     * @return true si los datos son validados correctamente, false en caso contrario.
     */
    private boolean validarDatos(){

        View focusView = null;
        View vvalue;
        String svalue;

        //Precio
        vvalue = fragmentview.findViewById(901);
        svalue = ((EditText) vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        } else if (!numeroValido(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_precio_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }

        //Peso
        vvalue = fragmentview.findViewById(902);
        svalue = ((EditText) vvalue).getText().toString();
        if (TextUtils.isEmpty(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_field_required));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        } else if (!numeroValido(svalue)) {
            ((EditText) vvalue).setError(getString(R.string.error_precio_no_valido));
            focusView = vvalue;
            focusView.requestFocus();
            return false;
        }

        return true;
    }

    private boolean numeroValido(String value) throws NumberFormatException{
        try{
            //cuando da excepcion realiza el catch
            Float number = Float.parseFloat(value);
            if(number<0 || number > 999999999)
                return false;
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

}

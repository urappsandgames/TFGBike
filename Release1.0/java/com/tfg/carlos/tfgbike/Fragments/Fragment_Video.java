package com.tfg.carlos.tfgbike.Fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.tfg.carlos.tfgbike.Activity.Activity_Navigation;
import com.tfg.carlos.tfgbike.BD.BaseDeDatosLocalOperaciones;
import com.tfg.carlos.tfgbike.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Video.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Video#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Video extends Fragment {

    private View fragmentview;

    private Spinner spinnerComponente;
    private SpinnerAdapter adapterComponente;
    private Spinner spinnerAccion;
    private SpinnerAdapter adapterAccion;

    private WebView mWebView = null;
    private FrameLayout mContentView;
    private FrameLayout mCustomViewContainer;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    public MyChromeClient mClient;

    public Activity_Navigation an;


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
     * @return A new instance of fragment Fragment_Video.
     */
    public static Fragment_Video newInstance(String param1, String param2) {
        Fragment_Video fragment = new Fragment_Video();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_Video() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Log.d("FragmentVideo", "ONCReate");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentview = inflater.inflate(R.layout.fragment_video, container, false);

        //inicializar spinners
        //spinner tipo componente
        spinnerComponente = (Spinner) fragmentview.findViewById(R.id.spinner_video_componente);
        // Create an ArrayAdapter using the string array (string_choice_sex) and a default spinner layout
        adapterComponente = ArrayAdapter.createFromResource(getActivity(),
                R.array.choice_componentType, android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerComponente.setAdapter(adapterComponente);

        //spinner accion
        spinnerAccion = (Spinner) fragmentview.findViewById(R.id.spinner_video_accion);
        // Create an ArrayAdapter using the string array (string_choice_sex) and a default spinner layout
        adapterAccion = ArrayAdapter.createFromResource(getActivity(),
                R.array.choice_videoAction, android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerAccion.setAdapter(adapterAccion);

        //listener del boton VER
        Button button = (Button) fragmentview.findViewById(R.id.button_video);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ver();
            }
        });

        an = new Activity_Navigation();
        //vista de web
        mWebView = (WebView) fragmentview.findViewById(R.id.webView_video);
        if(mWebView !=null){
            mClient = new MyChromeClient();
            mWebView.setWebChromeClient(mClient);
        }

        //permitir cambio de vista para este fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

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

    public static Fragment_Video newInstance(int sectionNumber){

        Fragment_Video fragment = new Fragment_Video();
        //para pasarle mas argumentos al drawer
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

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

    //proteje, junto a android:configChanges="orientation" en manifest de la llamada a onCreate() cuando se cambia la orientacion de pantalla
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Ejecuta la busqueda en la base de datos local de la url del video según las opciones elegidas en los spinners.
     * Reproduce el video encontrado. @see Reproduce(String url)
     *
     */
    private void ver(){
        //obtener valores seleccionados de los spinners
        String vctipo = ((Spinner) fragmentview.findViewById(R.id.spinner_video_componente)).getSelectedItem().toString();
        String vaccion = ((Spinner) fragmentview.findViewById(R.id.spinner_video_accion)).getSelectedItem().toString();

        //obtener text field para poner la URL del video por si el usuario desea verlo en la aplicacion de youtube o en su navegador
        TextView textURL = (TextView) fragmentview.findViewById(R.id.text_video_url);

        //Preparamos la BD local y cargamos los datos perteneciente al usuario que se logueo
        BaseDeDatosLocalOperaciones db1 = new BaseDeDatosLocalOperaciones(getActivity());
        String query = "SELECT " + vaccion + " FROM videos WHERE tipocomponente='"+ vctipo + "'";
        ArrayList<ContentValues> result = db1.selectBDLocal(query);
        if(result.size()>0){
            String url = null;
            if(vaccion.equals("Instalar"))
                url=result.get(0).getAsString("Instalar");
            else if (vaccion.equals("Eliminar"))
                url=result.get(0).getAsString("Eliminar");
            else if (vaccion.equals("Mantenimiento"))
                url=result.get(0).getAsString("Mantenimiento");
            if(url!=null) {
                Reproduce(url);
                //link url
                textURL.setText(Html.fromHtml("<a href=\"https://www.youtube.com/embed/" + url + "\">link</a>"));
                textURL.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
        else{
            Log.d("Video", "Error, select no devolvio nada");
        }
    }

    /**
     * Reproduce un video de Youtube en la webView integrada en la interfaz
     * @param url Dirección del video a reproducir.
     */
    private void Reproduce(String url){
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setSaveFormData(true);

        mWebView.loadUrl("https://www.youtube.com/embed/" + url);
    }


/*/En la actividad
    @Override
    public void onBackPressed() {
        if (mCustomViewContainer != null)
            mClient.onHideCustomView();
        else if (mWebView.canGoBack())
            mWebView.goBack();
        else
            super.onBackPressed();
    }
*/
    public class MyChromeClient extends WebChromeClient {

        FrameLayout.LayoutParams LayoutParameters = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            Log.d("MyChromeCLient", "onShowCustomView");
            /* if a view already exists then immediately terminate the new one
            if (fragmentview != null) {
                Log.d("MyChromeCLient", "fragmentview null: callback onCustomviewHidden");
                callback.onCustomViewHidden();
                return;
            }*/
            mContentView = (FrameLayout) fragmentview;
            mContentView.setVisibility(View.GONE);
            mCustomViewContainer = new FrameLayout(getActivity());
            mCustomViewContainer.setLayoutParams(LayoutParameters);
            mCustomViewContainer.setBackgroundResource(android.R.color.black);
            view.setLayoutParams(LayoutParameters);
            mCustomViewContainer.addView(view);
            fragmentview = view;
            mCustomViewCallback = callback;
            mCustomViewContainer.setVisibility(View.VISIBLE);

            an.mClient = mClient;
            an.setInFullScreen(true);

            getActivity().setContentView(mCustomViewContainer);
        }

        //todo falla al volver del fullscrren el setContentView fallo de removeView Child
        @Override
        public void onHideCustomView() {
            Log.d("MyChromeCLient", "onHideCustomView");
            if (fragmentview == null) {
                Log.d("MyChromeCLient", "onHideCustomView fragmentview null");
                return;
            } else {
                // Hide the custom view.
                fragmentview.setVisibility(View.GONE);
                // Remove the custom view from its container.
                mCustomViewContainer.removeView(fragmentview);
                fragmentview = null;
                mCustomViewContainer.setVisibility(View.GONE);
                mCustomViewCallback.onCustomViewHidden();
                // Show the content view.
                mContentView.setVisibility(View.VISIBLE);

                an.setInFullScreen(false);

                getActivity().setContentView(mContentView);
            }
        }
    }

}

package com.tfg.carlos.tfgbike.Fragments;

import android.app.Activity;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;

import com.tfg.carlos.tfgbike.Activity.Activity_Navigation;
import com.tfg.carlos.tfgbike.R;


public class Fragment_Home extends Fragment{

    View fragmentview;
    LocationManager lm = null;
    static String loginUser;

    private static final String ARG_SECTION_NUMBER = "section_number";

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
     * @return A new instance of fragment FragmentHome.
     */
    // Rename and change types and number of parameters
    public static Fragment_Home newInstance(String param1, String param2) {
        Fragment_Home fragment = new Fragment_Home();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_Home() {
        // Required empty public constructor
    }

    public static Fragment_Home newInstance(int sectionNumber, String user){

        Fragment_Home fragment = new Fragment_Home();
        //para pasarle mas argumentos al drawer
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        loginUser = user;
        return fragment;
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
        fragmentview = inflater.inflate(R.layout.fragment_home, container, false);

        //boton actividades
        ImageButton bactividad = (ImageButton) fragmentview.findViewById(R.id.b_home_actividad);
        bactividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction()
                        .replace(R.id.container, Fragment_Activity.newInstance(4 + 1,  loginUser))
                        .addToBackStack("");
                // Commit the transaction
                transaction.commit();
            }
        });


        //para localizar al usuario y poder ofrecerle el tiempo que va a hacer
        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNET);

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
        ((Activity_Navigation) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
     * A partir de una localización geográfica construye una URL que mostrará el parte meteorológico de dicha localización.
     * @param loc Location obtenido de nuestra posición red.
     */
    private void weatherURLMaker(Location loc){
        lm.removeUpdates(locationListenerNET);
        String web = "<html><iframe id='forecast_embed' type='text/html' frameborder='0' height='200' width='100%' src='http://forecast.io/embed/#lat="
                + loc.getLatitude() + "&lon=" + loc.getLongitude() + "&units=ca'> </iframe></html>";
        setWebView(web);
    }

    /**
     * Inicializa el webView de home y carga una url.
     * @param url String que contine la dirección web que proporciona el parte meteorológico de nuestra posicón.
     */
    private void setWebView(String url){
        WebView homeWebView = (WebView) fragmentview.findViewById(R.id.home_webView);
        if(homeWebView !=null){
            homeWebView.setBackgroundColor(Color.parseColor("#ffeeeeee"));
            homeWebView.getSettings().setJavaScriptEnabled(true);
            homeWebView.loadDataWithBaseURL("",url,"text/html", "UTF-8","");
            homeWebView.setWebChromeClient(new WebChromeClient());
        }
    }

    /**
     * Listener que detecta un cambio en la posición geográfica que proporciona la red.
     */
    LocationListener locationListenerNET = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //obtener localizacion
            weatherURLMaker(location);
        }
        // @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        // @Override
        public void onProviderEnabled(String provider) {}
        // @Override
        public void onProviderDisabled(String provider) {}
    };

}

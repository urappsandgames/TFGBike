package com.tfg.carlos.tfgbike.Activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;

import com.tfg.carlos.tfgbike.Dialogs;
import com.tfg.carlos.tfgbike.Fragments.Fragment_Activity;
import com.tfg.carlos.tfgbike.Fragments.Fragment_Bici_New;
import com.tfg.carlos.tfgbike.Fragments.Fragment_Componentes;
import com.tfg.carlos.tfgbike.Fragments.Fragment_Consultas;
import com.tfg.carlos.tfgbike.Fragments.Fragment_Home;
import com.tfg.carlos.tfgbike.Fragments.Fragment_Perfil;
import com.tfg.carlos.tfgbike.Fragments.Fragment_Bici;
import com.tfg.carlos.tfgbike.Fragments.Fragment_Settings;
import com.tfg.carlos.tfgbike.Fragments.Fragment_Video;
import com.tfg.carlos.tfgbike.NavigationDrawerFragment;
import com.tfg.carlos.tfgbike.R;

public class Activity_Navigation extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    String loginUser;
    public static boolean inFullScreen;
    public Fragment_Video.MyChromeClient mClient;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        loginUser = getIntent().getExtras().getString("loginUser");
        inFullScreen = false;
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        CambiaFragmento(position);
    }

    /**
     * Sustituye el fragmento que esta cargado en la activity por otro.
     * Basicamente se encarga de la navegación entre las pantallas de la aplicación.
     * @param position integer que representa el numero de sección para el fragmentManager.
     */
    public void CambiaFragmento(int position){
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {
            case 0: fragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment_Home.newInstance(position + 1, loginUser))
                    .commit();
                break;
            case 1: fragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment_Perfil.newInstance(position + 1, loginUser)).addToBackStack("")
                    .commit();
                break;
            case 2: fragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment_Bici.newInstance(position + 1, loginUser)).addToBackStack("")
                    .commit();
                break;
            case 3: fragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment_Componentes.newInstance(position + 1, loginUser)).addToBackStack("")
                    .commit();
                break;
            case 4: fragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment_Activity.newInstance(position + 1, loginUser)).addToBackStack("")
                    .commit();
                break;
            case 5: fragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment_Consultas.newInstance(position + 1, loginUser)).addToBackStack("")
                    .commit();
                break;
            case 6: fragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment_Video.newInstance(position + 1)).addToBackStack("")
                    .commit();
                break;
            case 7: fragmentManager.beginTransaction()
                        .replace(R.id.container, Fragment_Settings.newInstance(position + 1, loginUser)).addToBackStack("")
                        .commit();
                break;
            case 8:
                DialogFragment newFragment = new Dialogs.DialogExitApp();
                newFragment.show(getFragmentManager(), "exit");
                break;

            //otros fragmentos fuera del menu
            case 10: fragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment_Bici_New.newInstance(position + 1, loginUser))
                    .commit();
                break;
        }
    }

    /**
     * Cambia el titulo de la activity según que fragmento tenga cargado.
     * @param number
     */
    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
            case 6:
                mTitle = getString(R.string.title_section6);
                break;
            case 7:
                mTitle = getString(R.string.title_section7);
                break;

            //En adelante no son fragmentos que se muestren directamente desde el menu del navigation
            case 11:
                mTitle = getString(R.string.title_section11);
                break;
            default:
                mTitle = getString(R.string.title_section_default);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.navigation, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_navigation, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((Activity_Navigation) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }


    /**
     * Controla el comportamiento del botón fisico "atras" del dispositivo móvil.
     */
    @Override
    public void onBackPressed() {
        if (inFullScreen){
            //salir de pantalla completa en reproduccion de videos
           // mClient = Fragment_Video.this.
            mClient.onHideCustomView();
        } else {
            if(getSupportFragmentManager().getBackStackEntryCount()==0) {
                DialogFragment newFragment = new Dialogs.DialogExitApp();
                newFragment.show(getFragmentManager(), "exit");
            }
            else{
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    /**
     * Enciende o apaga el flag de fullscreen.
     * Actualmente se utiliza para cambiar el comportamiento del boton "atras"
     * del dispositivo movil cuando se esta reproduciendo un video en pantalla completa.
     * @param fullscreen
     */
    public void setInFullScreen(boolean fullscreen){
        inFullScreen = fullscreen;
    }

}

package com.tfg.carlos.tfgbike.Examples;

import android.os.AsyncTask;

/**
 * Created by Carlos on 30/04/2015.
 */
public class AsyncTask_Esquleto_ForDebug {

    class PositionAsync extends AsyncTask<Void, Void, Void>
    {
        boolean condicion;

        void Sleep(int ms)
        {
            try
            {
                Thread.sleep(ms);
            }
            catch (Exception e)
            {
            }
        }

        @Override
        protected void onPreExecute()
        {
            // Prepare everything for doInBackground thread

        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            while (condicion)
            {

                this.publishProgress((Void) null);
                Sleep(1000);

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... progress)
        {
            // once all points are read & drawn refresh the imageview

        }

        @Override
        protected void onPostExecute(Void result)
        {

        }
    }

}

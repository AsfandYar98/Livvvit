package com.app.livit.utils;

import android.os.AsyncTask;


import android.util.Log;





/**


 * Created by GsolC on 2/24/2017.


 */





public class LongOperation extends AsyncTask<Void, Void, String> {

    String recipient;
    String price;
    String to;
    String from;

    public LongOperation(String recipient, String price, String to, String from) {
        this.recipient = recipient;
        this.price = price;
        this.to = to;
        this.from = from;
    }


    @Override
    protected String doInBackground(Void... params) {


        try {

            GMailSender sender = new GMailSender("crickethowzat7@gmail.com", "howzathowzat");


            sender.sendMail("Livraison Terminée",from+ ",Votre colis a été livré à"+ to + ". Le total des frais pour cette livraison est de "+price,"crickethowzat7@gmail.com", recipient);





        } catch (Exception e) {


            Log.e("error", e.getMessage(), e);


            return "Email Not Sent";


        }


        return "Email Sent";


    }





    @Override


    protected void onPostExecute(String result) {





        Log.e("LongOperation",result+"");


    }





    @Override


    protected void onPreExecute() {


    }





    @Override


    protected void onProgressUpdate(Void... values) {


    }


}


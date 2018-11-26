package com.retdevelopment.valuebetsprotipster;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * It is SINGLETON!
 */
public class DatabaseManager
{
    private FirebaseFirestore database;
    private static DatabaseManager INSTANCE;

    List<Prediction> predictions = Collections.synchronizedList(new ArrayList<Prediction>());
    private boolean isPredictionsUpdated;
    private boolean isPercentageUpdated;

    private volatile String percentage = "";
    private Context context;
    private MainActivity mainActivity;

    private DatabaseManager()
    {
    }


    public static DatabaseManager getInstance()
    {
        if (INSTANCE == null)
            INSTANCE = new DatabaseManager();

        return INSTANCE;
    }


    public void setContext(Context context, MainActivity mainActivity)
    {
        this.context = context;
        this.mainActivity = mainActivity;
    }


    public void update()
    {
        ConnectivityManager cm = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected)
        {
            mainActivity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(mainActivity);
                    dlgAlert.setMessage("Please check your internet connection and try again.");
                    dlgAlert.setCancelable(true);
                    dlgAlert.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    //dismiss the dialog
                                }
                            });


                    dlgAlert.create().show();
                }
            });


            isPredictionsUpdated = true;
            isPercentageUpdated = true;
            return;
        }

        if (database == null)
            database = FirebaseFirestore.getInstance();


        isPredictionsUpdated = false;
        isPercentageUpdated = false;
        predictions.clear();

        database.collection("Predictions").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            int wonCount = 0;
                            int lostCount = 0;
                            List<Prediction> localPredictions = new ArrayList<Prediction>();

                            for (DocumentSnapshot document : task.getResult())
                            {
                                Prediction currentPrediction = document.toObject(Prediction.class);
                                localPredictions.add(currentPrediction);

                                if (currentPrediction.getStatus().equalsIgnoreCase("lose"))
                                    lostCount++;
                                else if (currentPrediction.getStatus().equalsIgnoreCase("won"))
                                    wonCount++;

                            }

                            Collections.sort(localPredictions, new Comparator<Prediction>()
                            {

                                @Override
                                public int compare(Prediction prediction1, Prediction prediction2)
                                {
                                    Date matchDateTime1 = null;
                                    Date matchDateTime2 = null;

                                    SimpleDateFormat formatBet = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                    formatBet.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

                                    try
                                    {
                                        matchDateTime1 = formatBet.parse(prediction1.getDate());
                                        matchDateTime2 = formatBet.parse(prediction2.getDate());

                                        return matchDateTime2.compareTo(matchDateTime1);
                                    }
                                    catch (ParseException e)
                                    {
                                        e.printStackTrace();
                                    }


                                    return 0;
                                }
                            });

                            if (localPredictions.size() > 0)
                            {
                                int endIndex = localPredictions.size() > 130 ? 129 : localPredictions.size() - 1;
                                localPredictions = localPredictions.subList(0, endIndex);
                            }

                            synchronized (predictions)
                            {
                                predictions.addAll(localPredictions);
                            }

                            int total = wonCount + lostCount;

                            synchronized (percentage)
                            {
                                double perc = ((double) wonCount / (double) total) * 100.0;
                                DatabaseManager.this.percentage = String.valueOf((int) perc) + "%";
                            }
                        } else
                        {
                            Log.d("DatabaseManager Fail", "Error getting predictions: ", task.getException());
                        }

                        isPredictionsUpdated = true;
                        isPercentageUpdated = true;
                    }
                });


//        database.collection("percentage").get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
//                {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task)
//                    {
//                        if (task.isSuccessful())
//                        {
//                            for (DocumentSnapshot document : task.getResult())
//                            {
//                                String percentage = document.getString("value");
//
//                                synchronized (percentage)
//                                {
//                                    //   DatabaseManager.this.percentage = percentage;
//                                }
//                            }
//                        } else
//                        {
//                            Log.d("DatabaseManager Fail", "Error getting percentage: ", task.getException());
//                        }
//
//                        isPercentageUpdated = true;
//                    }
//                });

    }

    public boolean isUpdateFinished()
    {
        return isPredictionsUpdated && isPercentageUpdated;
    }

    public List<Prediction> getFuturesBets()
    {
        List<Prediction> futureBets = new ArrayList<>();
        synchronized (predictions)
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            for (Prediction prediction : predictions)
            {
                Date timeNow = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();

                Date datePred = null;

                SimpleDateFormat formatBet = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                formatBet.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

                try
                {
                    datePred = formatBet.parse(prediction.getDate());
                    Calendar calNow = Calendar.getInstance();
                    calNow.setTime(timeNow);
                    calNow.add(Calendar.HOUR, -2);
                    timeNow = calNow.getTime();
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }

                if (datePred.after(timeNow))
                {
                    futureBets.add(prediction);
                }

            }
        }

        return futureBets;
    }


    public List<Prediction> getHistoryBets()
    {
        List<Prediction> historyBets = new ArrayList<>();
        synchronized (predictions)
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            for (Prediction prediction : predictions)
            {
                Date timeNow = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();

                Date datePred = null;

                SimpleDateFormat formatBet = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                formatBet.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

                try
                {
                    datePred = formatBet.parse(prediction.getDate());

                    Calendar calNow = Calendar.getInstance();
                    calNow.setTime(timeNow);
                    calNow.add(Calendar.HOUR, -2);
                    timeNow = calNow.getTime();
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }

                if (timeNow.after(datePred))
                {
                    historyBets.add(prediction);
                }
            }
        }

        return historyBets;
    }


    public String getPercentage()
    {
        return percentage;
    }
}

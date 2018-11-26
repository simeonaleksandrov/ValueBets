package com.retdevelopment.valuebetsprotipster;


import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class FutureBetsFragment extends Fragment implements AdapterView.OnItemClickListener
{
    ListView lst;
    LinkedList<String> betsTitles = new LinkedList<String>();
    LinkedList<Prediction> betsFull = new LinkedList<Prediction>();

    ArrayAdapter<String> arrayAdapter;
    MainActivity.IBetsListener betsListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        betsTitles.clear();
        betsFull.clear();

        View rootView = inflater.inflate(R.layout.future_bets, container, false);
        ListView lst = (ListView) rootView.findViewById(R.id.listviewFuture);

        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, betsTitles)
        {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
            {
                TwoLineListItem tv = (TwoLineListItem) super.getView(position, convertView, parent);

            //    tv.setTextColor(Color.rgb(255, 255, 255));


                Prediction pred = betsFull.get(position);

                TextView text1 = (TextView) tv.findViewById(android.R.id.text1);
                TextView text2 = (TextView) tv.findViewById(android.R.id.text2);



                if(pred.getStatus().equalsIgnoreCase("date"))
                {
                    text1.setText(pred.getShortTitle());
                    text1.setBackgroundColor(Color.rgb(70, 70, 70));
                    text1.setTypeface(text1.getTypeface(), Typeface.BOLD);
                    text1.setGravity(Gravity.CENTER);
                    text1.setTextColor(Color.rgb(255, 255, 255));
                    text2.setVisibility(View.INVISIBLE);
                    return tv;
                }

                text1.setBackgroundResource(android.R.color.transparent);
                text1.setTypeface(text1.getTypeface(), Typeface.NORMAL);
                text1.setGravity(Gravity.LEFT);
                text2.setVisibility(View.VISIBLE);

                Date matchDateTime = null;

                SimpleDateFormat formatBet = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                formatBet.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

                try {
                    matchDateTime = formatBet.parse(pred.getDate());

                } catch (ParseException e)
                {
                    e.printStackTrace();
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(matchDateTime);
                calendar.setTimeZone(TimeZone.getDefault());

                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                String timeStr = format.format(calendar.getTime());

                String firstLine = String.format("%s %s v %s", timeStr, pred.getTeam1(), pred.getTeam2());

                String secondLine = String.format("%s @ %s", pred.getPrediction(), pred.getOdd());

                text1.setText(firstLine);

                text1.setTextColor(Color.rgb(255, 255, 255));

                text2.setText(secondLine);

                text2.setTextColor(Color.rgb(200, 200, 230));


                return tv;
            }
        };

        lst.setAdapter(arrayAdapter);
        lst.setOnItemClickListener(this);

        betsListener = new MainActivity.IBetsListener()
        {
            @Override
            public void updatedPredictions(List<Prediction> predictions)
            {
                betsTitles.clear();
                betsFull.clear();

                Collections.sort(predictions, new Comparator<Prediction>()
                {

                    @Override
                    public int compare(Prediction prediction1, Prediction prediction2)
                    {
                        Date matchDateTime1 = null;
                        Date matchDateTime2 = null;

                        SimpleDateFormat formatBet = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        formatBet.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

                        try {
                            matchDateTime1 = formatBet.parse(prediction1.getDate());
                            matchDateTime2 = formatBet.parse(prediction2.getDate());

                            return matchDateTime1.compareTo(matchDateTime2);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                        return 0;
                    }
                });

                String lastDate = "";

                for (Prediction pred: predictions)
                {
                    Date matchDateTime = null;

                    SimpleDateFormat formatBet = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    formatBet.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

                    try {
                        matchDateTime = formatBet.parse(pred.getDate());

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(matchDateTime);
                    calendar.setTimeZone(TimeZone.getDefault());

                    SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy");
                    String dateStr = format.format(calendar.getTime());

                    if(!lastDate.equalsIgnoreCase(dateStr))
                    {
                        lastDate = dateStr;

                        Prediction datepred = new Prediction();
                        datepred.setShortTitle(dateStr);
                        datepred.setStatus("date");
                        betsFull.add(datepred);
                        betsTitles.add(dateStr);
                    }


                    betsFull.add(pred);
                    betsTitles.add(pred.getShortTitle());

                }
                arrayAdapter.notifyDataSetChanged();
            }
        };

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.addFutureBetsListener(betsListener);

        List<Prediction> predictions = DatabaseManager.getInstance().getFuturesBets();
        Collections.sort(predictions, new Comparator<Prediction>()
        {

            @Override
            public int compare(Prediction prediction1, Prediction prediction2)
            {
                Date matchDateTime1 = null;
                Date matchDateTime2 = null;

                SimpleDateFormat formatBet = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                formatBet.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

                try {
                    matchDateTime1 = formatBet.parse(prediction1.getDate());
                    matchDateTime2 = formatBet.parse(prediction2.getDate());

                    return matchDateTime1.compareTo(matchDateTime2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                return 0;
            }
        });

        String lastDate = "";

        for (Prediction pred: predictions)
        {
            Date matchDateTime = null;

            SimpleDateFormat formatBet = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            formatBet.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

            try {
                matchDateTime = formatBet.parse(pred.getDate());

            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(matchDateTime);
            calendar.setTimeZone(TimeZone.getDefault());

            SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy");
            String dateStr = format.format(calendar.getTime());

            if(!lastDate.equalsIgnoreCase(dateStr))
            {
                lastDate = dateStr;

                Prediction datepred = new Prediction();
                datepred.setShortTitle(dateStr);
                datepred.setStatus("date");
                betsFull.add(datepred);
                betsFull.add(pred);
                betsTitles.add(dateStr);
                betsTitles.add(pred.getShortTitle());
            }
        }
        arrayAdapter.notifyDataSetChanged();


        return rootView;
    }

    public void addItem(String newMatch)
    {
        betsTitles.add(newMatch);
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else
        {
            builder = new AlertDialog.Builder(getContext());
        }
        Prediction clickedPrediction = betsFull.get(position);
        if(clickedPrediction.getStatus().equalsIgnoreCase("date"))
            return;

        Date matchDateTime = null;

        SimpleDateFormat formatBet = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        formatBet.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

        try {
            matchDateTime = formatBet.parse(clickedPrediction.getDate());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(matchDateTime);
        calendar.setTimeZone(TimeZone.getDefault());

        SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy - HH:mm");
        String dateStr = format.format(calendar.getTime());
        String message = String.format("%s\n%s:\n%s - %s\nBet: %s\nOdds: %s\n", dateStr, clickedPrediction.getLeague(), clickedPrediction.getTeam1(),
                clickedPrediction.getTeam2(), clickedPrediction.getPrediction(), clickedPrediction.getOdd());

        builder.setMessage(message).show();
    }

    @Override
    public void onDestroy()
    {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null)
            mainActivity.removeFutureBetsListener(betsListener);

        super.onDestroy();
    }
}

package com.retdevelopment.valuebetsprotipster;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by User on 12-Apr-18.
 */

public class ForMaxProfitFragment extends Fragment
{
    @Nullable

    TextView tv;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.maxprofit, container, false);

        tv = (TextView) rootView.findViewById(R.id.text_view);

        loadText();

        return rootView;
    }

    private void loadText() {
        String t = "To make max profit with our tips, just follow severall simple rules:\n" +
                "\n" +
                "1. Make sure you have funds for 30 bets with the same face value\n" +
                "-To make a bet with 10 euros, you should have bank with 300 euros. Example: 30 bets * 10 euros = 300€.\n" +
                "\n" +
                "2. Always bet the same amount, and NEVER increase it in an attempt to regain lost.\n" +
                "-Every time you increase your bet, and you violate the 30-bet rule, you reduce your win percent.\n" +
                "\n" +
                "3. Make only single match bets\n" +
                "-This will reduce your loses to minimum, and will raise your percent winning bets. ";


        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setText(t);
    }
}

package com.retdevelopment.valuebetsprotipster;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by User on 12-Apr-18.
 */

public class HomeFragment extends Fragment
{
    @Nullable

    TextView tv;
    private MainActivity.IPercentageListener percentageListener;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.home, container, false);

        rootView.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                (((MainActivity) getActivity())).switchToHistoryBets();
            }
        });

        rootView.findViewById(R.id.imageView2).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                (((MainActivity) getActivity())).switchToMaxProfit();
            }
        });


        percentageListener = new MainActivity.IPercentageListener()
        {
            @Override
            public void updatePercentage(String percentage)
            {
                TextView percentageText = (TextView) HomeFragment.this.getActivity().findViewById(R.id.textViewPercentage);
                if(percentageText == null)
                    return;

                percentageText.setText(percentage);
            }
        };

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.addPercentageListener(percentageListener);

        String percentage = DatabaseManager.getInstance().getPercentage();

        TextView percentageText = (TextView) rootView.findViewById(R.id.textViewPercentage);
        percentageText.setText(percentage);

        return rootView;
    }

    @Override
    public void onDestroy()
    {
        MainActivity mainActivity = (MainActivity)getActivity();
        if(mainActivity != null)
            mainActivity.removePercentageListener(percentageListener);

        super.onDestroy();
    }

}

package com.retdevelopment.valuebetsprotipster;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private MySwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private DatabaseManager databaseManager;
    private static int selectedTabPosition = 0;

    public interface IBetsListener
    {
        public void updatedPredictions(List<Prediction> predictions);
    }

    public interface IPercentageListener
    {
        public void updatePercentage(String percentage);
    }

    private List<IBetsListener> futureBetsListeners = new ArrayList<IBetsListener>();
    private List<IBetsListener> historyBetsListeners = new ArrayList<IBetsListener>();
    private List<IPercentageListener> percentageListeners = new ArrayList<IPercentageListener>();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        DatabaseManager.getInstance().setContext(getApplicationContext(), this);

        setContentView(R.layout.activity_main);

        swipeRefreshLayout = (MySwipeRefreshLayout) findViewById(R.id.swipelayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.refresh, R.color.refresh1, R.color.refresh2);
        swipeRefreshLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback()
        {
            @Override
            public boolean canChildScrollUp(SwipeRefreshLayout parent, @Nullable View child)
            {
                ListView listView = null;
                if(selectedTabPosition == 0)
                    listView = (ListView) findViewById(R.id.listviewFuture);
                else if(selectedTabPosition == 1)
                    listView = (ListView) findViewById(R.id.listViewHistory);
                else if(selectedTabPosition == 2)
                {
                    return true;
                }

                if (listView != null && listView.getChildCount() != 0)
                {
                    return listView.getChildAt(0).getTop() != 0;
                }

                return false;
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {

            @Override
            public void onRefresh()
            {
                onClickUpdateDatabase(null);
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFragment(new FutureBetsFragment(), "Future Bets");
        mSectionsPagerAdapter.addFragment(new HistoryBetsFragment(), "History Bets");

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout)
                                           {
                                               @Override
                                               public void onPageScrollStateChanged(int state)
                                               {
                                                   swipeRefreshLayout.setPaused(state != ViewPager.SCROLL_STATE_IDLE);
                                               }
                                           }
        );
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager){

            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                selectedTabPosition = tab.getPosition();
                super.onTabSelected(tab);
            }
        });

        findViewById(R.id.imageView3).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onClickUpdateDatabase(null);
            }
        });


        databaseManager = DatabaseManager.getInstance();

        onClickUpdateDatabase(null);

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
                dlgAlert.setMessage("Hello, we've decided to close our services, you will get FULL REFUND up to 10 days!\nHave a great day!");
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

        String percentage = DatabaseManager.getInstance().getPercentage();

        TextView percentageText = (TextView)findViewById(R.id.textViewPercentage);
        percentageText.setText(percentage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void onClickUpdateDatabase(View v)
    {
        new AsyncTask<Integer, Integer, Integer>()
        {
            @Override
            protected void onPreExecute()
            {
                tabLayout.setEnabled(false);
                swipeRefreshLayout.setRefreshing(true);

                super.onPreExecute();
            }

            protected Integer doInBackground(Integer... params)
            {
                databaseManager.update();

                while (!databaseManager.isUpdateFinished())
                {
                    try
                    {
                        Thread.sleep(50);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                return 0;
            }

            // This is called when doInBackground() is finished
            protected void onPostExecute(Integer result)
            {
                List futureBets = databaseManager.getFuturesBets();

                for (IBetsListener listener : futureBetsListeners)
                {
                    listener.updatedPredictions(futureBets);
                }

                List historyBets = databaseManager.getHistoryBets();
                for (IBetsListener listener : historyBetsListeners)
                {
                    listener.updatedPredictions(historyBets);
                }


                String percentage2 = DatabaseManager.getInstance().getPercentage();

                TextView percentageText = (TextView)findViewById(R.id.textViewPercentage);
                percentageText.setText(percentage2);
                
                String percentage = databaseManager.getPercentage();
                for (IPercentageListener listener : percentageListeners)
                {
                    listener.updatePercentage(percentage);


                }

                swipeRefreshLayout.setRefreshing(false);

                tabLayout.setEnabled(true);

                super.onPostExecute(result);
            }
        }.execute(0);
    }


    public void addFutureBetsListener(IBetsListener listener)
    {
        futureBetsListeners.add(listener);
    }

    public void removeFutureBetsListener(IBetsListener listener)
    {
        futureBetsListeners.remove(listener);
    }

    public void addHistoryBetsListener(IBetsListener listener)
    {
        historyBetsListeners.add(listener);
    }

    public void removeHistoryBetsListener(IBetsListener listener)
    {
        historyBetsListeners.remove(listener);
    }

    public void addPercentageListener(IPercentageListener listener)
    {
        percentageListeners.add(listener);
    }

    public void removePercentageListener(IPercentageListener listener)
    {
        percentageListeners.remove(listener);
    }

    public void switchToHistoryBets()
    {
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.getTabAt(2).select();
    }

    public void switchToMaxProfit()
    {
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.getTabAt(3).select();
    }
}
package com.waisblut.soccerscoreboardlite.views;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.waisblut.soccerscoreboardlite.Logger;
import com.waisblut.soccerscoreboardlite.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class FragmentMain
        extends Fragment
        implements View.OnClickListener
{
    protected enum TimerState
    {
        STOPPED(0),
        PLAYING(1),
        PAUSED(2);

        protected int code;

        TimerState(int i)
        {
            this.code = i;
        }

        protected static TimerState fromValue(int value)
        {
            for (TimerState my : TimerState.values())
            {
                if (my.code == value)
                {
                    return my;
                }
            }

            return null;
        }

        protected int getCode()
        {
            return code;
        }
    }

    //region Variables...
    private TimerState mTimerState = null;
    private long mDuration = 15000l;
    private Timer mTimer = new Timer();
    private TextView mTxtTimer;

    private RelativeLayout mRlA, mRlB, mRlA_Back, mRlB_Back;
    private Dialog mDlgHelp;
    private Button mBtnUndoA;
    private Button mBtnUndoB;
    private TextView mTxtNameA, mTxtNameB;
    private TextView mTxtScoreA, mTxtScoreB;
    private int mCounterA, mCounterB;
    private SharedPreferences mSp;
    //endregion

    public FragmentMain() {}

    //region Fragment events
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        getActivity().getWindow().setBackgroundDrawable(null);

        //region Init View...
        ImageButton imgHelp = (ImageButton) view.findViewById(R.id.imgBtnHelp);

        Button btnReset = (Button) view.findViewById(R.id.btnReset);
        mBtnUndoA = (Button) view.findViewById(R.id.btnUndo_A);
        mBtnUndoB = (Button) view.findViewById(R.id.btnUndo_B);

        mTxtNameA = (TextView) view.findViewById(R.id.txtTeam_A);
        mTxtNameB = (TextView) view.findViewById(R.id.txtTeam_B);

        mTxtScoreA = (TextView) view.findViewById(R.id.txtScore_A);
        mTxtScoreB = (TextView) view.findViewById(R.id.txtScore_B);

        mTxtTimer = (TextView) view.findViewById(R.id.txtTimer);

        mRlA = (RelativeLayout) view.findViewById(R.id.lay_A);
        mRlB = (RelativeLayout) view.findViewById(R.id.lay_B);
        mRlA_Back = (RelativeLayout) view.findViewById(R.id.lay_Back_A);
        mRlB_Back = (RelativeLayout) view.findViewById(R.id.lay_Back_B);
        //endregion

        //region Init Score....
        setPreferences();

        setInitialSettings();
        //endregion

        //region LongClick....
        View.OnLongClickListener myLongClick = new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                switch (v.getId())
                {
                case R.id.btnUndo_A:
                    mCounterA--;
                    mCounterA = changeScore(mCounterA, 'A');
                    break;

                case R.id.btnUndo_B:
                    mCounterB--;
                    mCounterB = changeScore(mCounterB, 'B');
                    break;

                case R.id.btnReset:
                    if (mCounterA == 0 && mCounterB == 0)
                    {
                        resetAll();
                    }
                    else
                    {
                        resetCounters();
                    }
                    stop();
                    break;

                case R.id.txtTimer:
                    //                    switch (mTimerState)
                    //                    {
                    //                    case PAUSED:
                    //                        play();
                    //                        break;
                    //
                    //                    case PLAYING:
                    //                        pause();
                    //                        break;
                    //
                    //                    case STOPPED:
                    //                        play();
                    //                        break;
                    //
                    //                    }
                    //                    break;

                    callTimePicker();
                    play();
                }

                return true;
            }
        };
        //endregion

        //region ClickListeners...
        imgHelp.setOnClickListener(this);

        btnReset.setOnClickListener(this);
        mBtnUndoA.setOnClickListener(this);
        mBtnUndoB.setOnClickListener(this);
        mRlA.setOnClickListener(this);
        mRlB.setOnClickListener(this);

        mBtnUndoA.setOnLongClickListener(myLongClick);
        mBtnUndoB.setOnLongClickListener(myLongClick);
        btnReset.setOnLongClickListener(myLongClick);
        mTxtTimer.setOnLongClickListener(myLongClick);
        //endregion

        return view;
    }

    private void callTimePicker() {}

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (mDlgHelp != null)
        {
            mDlgHelp.dismiss();
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v.getTag() == null)
        {
            switch (v.getId())
            {
            case R.id.imgBtnHelp:
                create_A_dialogHelp();
                break;

            case R.id.btnReset:
                Toast.makeText(getActivity(),
                               getResources().getString(R.string.long_press_reset),
                               Toast.LENGTH_SHORT).show();
                showPicker();
                break;
            }
        }
        else
        {
            char tag = v.getTag().toString().charAt(0);

            switch (tag)
            {
            case 'A':
                if (v.getId() != mBtnUndoA.getId())
                {
                    mCounterA++;
                    mCounterA = changeScore(mCounterA, 'A');
                }
                else
                {
                    Toast.makeText(getActivity(),
                                   getResources().getString(R.string.long_press_undo),
                                   Toast.LENGTH_SHORT).show();
                }
                break;

            case 'B':
                if (v.getId() != mBtnUndoB.getId())
                {
                    mCounterB++;
                    mCounterB = changeScore(mCounterB, 'B');
                }
                else
                {
                    Toast.makeText(getActivity(),
                                   getResources().getString(R.string.long_press_undo),
                                   Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
    //endregion

    //region Private Methods...
    //region Timer Methods
    private void play()
    {
        //TODO To be implemented
        //TODO Toogle between PLAY/PAUSE on Button
        Logger.log('d', "PLAY");
        this.mTimerState = TimerState.PLAYING;
        setCounter(mDuration);
    }

    private void pause()
    {
        this.mTimerState = TimerState.PAUSED;
        Logger.log('d', "PAUSE");
        //TODO To be implemented
    }

    private void stop()
    {
        this.mTimerState = TimerState.STOPPED;
        Logger.log('d', "STOP");
        //TODO To be implemented
    }
    //endregion

    private void setInitialSettings()
    {
        mCounterA = changeScore(mSp.getInt(Logger.TEAM_A_SCORE, 0), 'A');
        mCounterB = changeScore(mSp.getInt(Logger.TEAM_B_SCORE, 0), 'B');


        mTxtNameA.setText(mSp.getString(Logger.TEAM_A_NAME,
                                        getActivity().getResources().getString(R.string.team_A)));
        mTxtNameB.setText(mSp.getString(Logger.TEAM_B_NAME,
                                        getActivity().getResources().getString(R.string.team_B)));

        setBackground(mRlA, mSp.getInt(Logger.TEAM_A_COLOR,
                                       R.drawable.background_team_divider_red));
        setBackground(mRlB, mSp.getInt(Logger.TEAM_B_COLOR,
                                       R.drawable.background_team_divider_blue));

        this.mTimerState = TimerState.STOPPED;

        //TODO DISABLE BUTTON STOP
    }

    private int changeScore(int value, char team)
    {
        value = checkValue(value);

        if (team == 'A')
        {
            mTxtScoreA.setText(String.valueOf(value));
            mSp.edit().putInt(Logger.TEAM_A_SCORE, value).apply();
        }
        else if (team == 'B')
        {
            mTxtScoreB.setText(String.valueOf(value));
            mSp.edit().putInt(Logger.TEAM_B_SCORE, value).apply();
        }


        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(50);

        return value;
    }

    private int checkValue(int value)
    {
        if (value < 0)
        {
            value = 0;
        }

        if (value > Logger.SCORE_MAX)
        {
            value = Logger.SCORE_MAX;
        }

        return value;
    }

    private void resetAll()
    {
        mSp.edit().putInt(Logger.TEAM_A_SCORE, 0).apply();
        mSp.edit().putString(Logger.TEAM_A_NAME, getResources().getString(R.string.team_A)).apply();
        mTxtNameA.setText(getResources().getString(R.string.team_A));
        setBackground(mRlA, R.drawable.background_team_divider_red);

        mSp.edit().putInt(Logger.TEAM_B_SCORE, 0).apply();
        mSp.edit().putString(Logger.TEAM_B_NAME, getResources().getString(R.string.team_B)).apply();
        mTxtNameB.setText(getResources().getString(R.string.team_B));
        setBackground(mRlB, R.drawable.background_team_divider_blue);

    }

    private void resetCounters()
    {
        mCounterA = 0;
        mCounterB = 0;

        changeScore(mCounterA, 'A');
        changeScore(mCounterB, 'B');


    }

    private void setPreferences()
    {
        mSp = getActivity().getPreferences(Context.MODE_PRIVATE);
    }
    //endregion

    //region Dialog...
    @SuppressWarnings("ResultOfMethodCallIgnored")

    private void create_A_dialogHelp()
    {
        mDlgHelp = new Dialog(getActivity());

        //mDlgHelp.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlgHelp.setContentView(R.layout.dialog_help);
        mDlgHelp.setTitle(getString(R.string.app_name) + " " + getVersionName());

        TextView txtFullVersion = (TextView) mDlgHelp.findViewById(R.id.txtFullApp);

        txtFullVersion.setText(Html.fromHtml(getString(R.string.full_version)));
        txtFullVersion.setMovementMethod(LinkMovementMethod.getInstance());
        mDlgHelp.show();
    }

    private String getVersionName()
    {
        String versionName = "";
        try
        {
            versionName = getActivity().getPackageManager()
                                       .getPackageInfo(getActivity().getPackageName(),
                                                       0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        return versionName;
    }

    //region Dialog Methods....
    @SuppressWarnings("deprecation")
    private void setBackground(View v, int color)
    {
        if (Build.VERSION.SDK_INT >= 16)
        {
            try
            {
                v.setBackground(getResources().getDrawable(color));
            }
            catch (Exception e)
            {
                switch (v.getTag().toString().charAt(0))
                {
                case 'A':
                    v.setBackground(getResources().getDrawable(R.drawable.background_team_divider_red));
                    break;
                case 'B':
                    v.setBackground(getResources().getDrawable(R.drawable.background_team_divider_blue));
                    break;
                }
            }
        }
        else
        {
            try
            {
                v.setBackgroundDrawable(getResources().getDrawable(color));
            }
            catch (Exception e)
            {
                switch (v.getTag().toString().charAt(0))
                {
                case 'A':
                    v.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_team_divider_red));
                    break;
                case 'B':
                    v.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_team_divider_blue));
                    break;
                }
            }
        }

        if (v.getId() == mRlA.getId())
        {
            mSp.edit().putInt(Logger.TEAM_A_COLOR, color).apply();
        }
        else if (v.getId() == mRlB.getId())
        {
            mSp.edit().putInt(Logger.TEAM_B_COLOR, color).apply();
        }


    }
    //endregion
    //endregion

    //region Timer
    private void setCounter(final long duration)
    {
        mTimer = new Timer();
        final long startTime = duration + System.currentTimeMillis();

        mTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                final long timeLeft = (startTime - System.currentTimeMillis());

                if (timeLeft > 0)
                {
                    Logger.log('d', "Ticking...." + (timeLeft));

                    getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            setMillisOnTextView(mTxtTimer, timeLeft);
                            //mTxtTimer.setText(String.valueOf(timeLeft));
                        }
                    });
                }
                else
                {
                    Logger.log('d', "DONE");

                    //playSound();

                    mTimer.cancel();
                }
            }

        }, 0, 450);
    }

    protected void setMillisOnTextView(TextView txtView, long millisUntilFinished)
    {
        String strFormat = "%02d:%02d";
        txtView.setText("" + String.format(strFormat, TimeUnit.MILLISECONDS.toMinutes(
                                                   millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                                   TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),

                                           TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                           TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(
                                                   millisUntilFinished))));
    }
    //endregion

    public void showPicker()
    {
        Dialog_MyTimePicker.OnTimeSetListener listener = new Dialog_MyTimePicker.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(MyTimePicker view, int minute, int seconds)
            {
                mDuration = (minute * 60 + seconds) * 1000;

                setMillisOnTextView(mTxtTimer, mDuration);
            }
        };

        //TODO PASS A DEFAULT MINUTE/SECOND
        Dialog_MyTimePicker mTimePicker = new Dialog_MyTimePicker(this.getActivity(),
                                                                  listener,
                                                                  0,12);

        mTimePicker.show();
    }
}
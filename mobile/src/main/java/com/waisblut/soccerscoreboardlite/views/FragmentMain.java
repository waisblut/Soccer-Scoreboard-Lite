package com.waisblut.soccerscoreboardlite.views;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
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

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
        protected int getCode()
        {
            return code;
        }
    }

    //region Variables...
    private TimerState mTimerState = null;
    private long mDuration = 8000l;//TODO CREATE DEFAULT TIMER TO START
    private long mCurrentTimeLeft = 0l;
    private Timer mTimer = new Timer();
    private TextView mTxtTimer;
    private ImageButton mBtnPlay, mBtnStop;
    private ImageButton mBtnSettings;

    private RelativeLayout mRlA, mRlB;
    private Dialog mDlgHelp, mDlgPicker, mDlgSettings;
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
        //rlA_Back = (RelativeLayout) view.findViewById(R.id.lay_Back_A);
        //rlB_Back = (RelativeLayout) view.findViewById(R.id.lay_Back_B);

        mBtnPlay = (ImageButton) view.findViewById(R.id.imgBtnPlayPause);
        mBtnStop = (ImageButton) view.findViewById(R.id.imgBtnStop);
        mBtnSettings = (ImageButton) view.findViewById(R.id.imgBtnSettings);
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
                    create_dialogPicker(0, 11);
                    break;
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

        mBtnPlay.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
        mBtnSettings.setOnClickListener(this);

        mBtnUndoA.setOnLongClickListener(myLongClick);
        mBtnUndoB.setOnLongClickListener(myLongClick);
        btnReset.setOnLongClickListener(myLongClick);
        mTxtTimer.setOnLongClickListener(myLongClick);
        //endregion

        return view;
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (mDlgHelp != null)
        {
            mDlgHelp.dismiss();
        }

        if (mDlgPicker != null)
        {
            mDlgPicker.dismiss();
        }

        if (mDlgSettings != null)
        {
            mDlgSettings.dismiss();
        }
    }

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
                create_dialogHelp();
                break;

            case R.id.btnReset:
                Toast.makeText(getActivity(),
                               getResources().getString(R.string.long_press_reset),
                               Toast.LENGTH_SHORT).show();
                break;

            case R.id.imgBtnPlayPause:
                if (mTimerState == TimerState.PLAYING)
                {
                    pause();
                }
                else
                {
                    play();
                }
                break;

            case R.id.imgBtnStop:
                stop();
                break;

            case R.id.imgBtnSettings:
                create_dialogSettings();
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
        Logger.log('d', "PLAY");
        if (mTimerState != TimerState.PLAYING)
        {
            mBtnStop.setEnabled(true);
            mBtnPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));

            this.mTimerState = TimerState.PLAYING;
            setCounter(mDuration);
        }
    }

    private void pause()
    {
        Logger.log('d', "PAUSE");
        mBtnStop.setEnabled(true);
        mBtnPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        mDuration = mCurrentTimeLeft;
        setMillisOnTextView(mTxtTimer, mDuration);
        mTimer.cancel();
        this.mTimerState = TimerState.PAUSED;
    }

    private void stop()
    {
        Logger.log('d', "STOP");

        mBtnStop.setEnabled(false);
        mBtnPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        this.mTimerState = TimerState.STOPPED;
        mTimer.cancel();
        mDuration = mSp.getLong(Logger.CONST_DEFAULT_TIME, Logger.DEFAULT_TIME);
        setMillisOnTextView(mTxtTimer, mDuration);
    }
    //endregion

    private void animateTimer()
    {
        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(5);
        animation.setRepeatMode(Animation.REVERSE);
        mTxtTimer.startAnimation(animation);
    }

    private void setInitialSettings()
    {
        mCounterA = changeScore(mSp.getInt(Logger.CONST_TEAM_A_SCORE, 0), 'A');
        mCounterB = changeScore(mSp.getInt(Logger.CONST_TEAM_B_SCORE, 0), 'B');


        mTxtNameA.setText(mSp.getString(Logger.CONST_TEAM_A_NAME,
                                        getActivity().getResources().getString(R.string.team_A)));
        mTxtNameB.setText(mSp.getString(Logger.CONST_TEAM_B_NAME,
                                        getActivity().getResources().getString(R.string.team_B)));

        setBackground(mRlA, mSp.getInt(Logger.CONST_TEAM_A_COLOR,
                                       R.drawable.background_team_divider_red));
        setBackground(mRlB, mSp.getInt(Logger.CONST_TEAM_B_COLOR,
                                       R.drawable.background_team_divider_blue));

        stop();

        //TODO DISABLE BUTTON STOP
    }

    private int changeScore(int value, char team)
    {
        value = checkValue(value);

        if (team == 'A')
        {
            mTxtScoreA.setText(String.valueOf(value));
            mSp.edit().putInt(Logger.CONST_TEAM_A_SCORE, value).apply();
        }
        else if (team == 'B')
        {
            mTxtScoreB.setText(String.valueOf(value));
            mSp.edit().putInt(Logger.CONST_TEAM_B_SCORE, value).apply();
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
        mSp.edit().putInt(Logger.CONST_TEAM_A_SCORE, 0).apply();
        mSp.edit()
           .putString(Logger.CONST_TEAM_A_NAME, getResources().getString(R.string.team_A))
           .apply();
        mTxtNameA.setText(getResources().getString(R.string.team_A));
        setBackground(mRlA, R.drawable.background_team_divider_red);

        mSp.edit().putInt(Logger.CONST_TEAM_B_SCORE, 0).apply();
        mSp.edit()
           .putString(Logger.CONST_TEAM_B_NAME, getResources().getString(R.string.team_B))
           .apply();
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
    private void create_dialogSettings()
    {
//        mDlgSettings = new Dialog(getActivity());
//
//        mDlgSettings.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        mDlgSettings.setContentView(R.layout.dialog_settings);
//
//        mSp.edit().putLong(Logger.CONST_DEFAULT_TIME, (9999)).apply();//TODO FIX HERER
//        //TODO CREATE SETTINGS DIALOG XML
//
//        mDlgSettings.show();
    }

    private void create_dialogHelp()
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

    private void create_dialogPicker(int defMin, int defSec)
    {
        mDlgPicker = new Dialog(getActivity());
        //WindowManager.LayoutParams wmlp;
        final MyTimePicker timePicker;
        Button btnSetTime, btnCancel;

        setUpWindow(mDlgPicker);

        //        wmlp = dlgPicker.getWindow().getAttributes();
        //        wmlp.horizontalMargin = 0.1f;
        //        wmlp.verticalMargin = 0.1f;

        mDlgPicker.setContentView(R.layout.dialog_timepicker);

        timePicker = (MyTimePicker) mDlgPicker.findViewById(R.id.timePicker);
        btnSetTime = (Button) mDlgPicker.findViewById(R.id.btnSetTime);
        btnCancel = (Button) mDlgPicker.findViewById(R.id.btnCancel);

        View.OnClickListener onClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (v.getId())
                {
                case R.id.btnSetTime:
                    mDuration =
                            (timePicker.getCurrentMinute() * 60 + timePicker.getCurrentSeconds()) *
                            1000;

                    setMillisOnTextView(mTxtTimer, mDuration);
                    break;

                case R.id.btnCancel:
                    break;
                }
                mDlgPicker.dismiss();
            }
        };

        mDlgPicker.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {

            }
        });

        btnSetTime.setOnClickListener(onClick);
        btnCancel.setOnClickListener(onClick);

        timePicker.setCurrentMinute(defMin);
        timePicker.setCurrentSecond(defSec);

        mDlgPicker.show();
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
            mSp.edit().putInt(Logger.CONST_TEAM_A_COLOR, color).apply();
        }
        else if (v.getId() == mRlB.getId())
        {
            mSp.edit().putInt(Logger.CONST_TEAM_B_COLOR, color).apply();
        }


    }

    private void setUpWindow(Dialog d)
    {
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);

        d.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.getWindow().setDimAmount(0.50f);
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
                mCurrentTimeLeft = timeLeft;

                if (timeLeft > 0)
                {
                    Logger.log('d', "Ticking...." + (timeLeft));

                    getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            setMillisOnTextView(mTxtTimer, timeLeft);
                        }
                    });

                    if (timeLeft < 3000)
                    {
                        getActivity().runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                animateTimer();
                            }
                        });
                    }
                }
                else
                {
                    Logger.log('d', "DONE");

                    playSound();
                    mTimer.cancel();

                    getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            stop();
                        }
                    });
                }
            }

        }, 0, 450);
    }

    private void playSound()
    {
        MediaPlayer mp = MediaPlayer.create(getActivity(), R.raw.whistle);
        mp.start();
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
}
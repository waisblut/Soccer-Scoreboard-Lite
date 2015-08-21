package com.waisblut.soccerscoreboardlite.views;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
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
        implements View.OnClickListener {
    protected enum TimerState {
        STOPPED(0),
        PLAYING(1),
        PAUSED(2);

        protected int code;

        TimerState(int i) {
            this.code = i;
        }

        @SuppressWarnings("unused")
        protected static TimerState fromValue(int value) {
            for (TimerState my : TimerState.values()) {
                if (my.code == value) {
                    return my;
                }
            }

            return null;
        }

        @SuppressWarnings("unused")
        protected int getCode() {
            return code;
        }
    }

    //region Variables...
    private boolean mSwapped = false;
    private TimerState mTimerState = null;
    private long mDuration = 8000l;//TODO CREATE DEFAULT TIMER TO START
    private long mCurrentTimeLeft = 0l;
    private Timer mTimer = new Timer();
    private TextView mTxtTimer;
    private ImageButton mBtnPlay, mBtnStop, mBtnSwap;

    private RelativeLayout mRlA, mRlB, mRlA_Back, mRlB_Back;
    private Dialog mDlgEditTeamName, mDlgHelp, mDlgPicker;
    private Button mBtnReset, mBtnUndoA, mBtnUndoB;
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
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        getActivity().getWindow()
                     .setBackgroundDrawable(null);

        //region Init View...
        ImageButton imgHelp = (ImageButton) view.findViewById(R.id.imgBtnHelp);

        mBtnReset = (Button) view.findViewById(R.id.btnReset);
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

        mBtnPlay = (ImageButton) view.findViewById(R.id.imgBtnPlayPause);
        mBtnStop = (ImageButton) view.findViewById(R.id.imgBtnStop);
        mBtnSwap = (ImageButton) view.findViewById(R.id.imgBtnSwap);
        //endregion

        //region Init Score....
        setPreferences();

        setInitialSettings();
        //endregion

        //region LongClick....
        View.OnLongClickListener myLongClick = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switch (v.getId()) {
                case R.id.btnUndo_A:
                    mCounterA--;
                    mCounterA = changeScore(mCounterA, 'A');
                    break;

                case R.id.btnUndo_B:
                    mCounterB--;
                    mCounterB = changeScore(mCounterB, 'B');
                    break;

                case R.id.btnReset:
                    if (mCounterA == 0 && mCounterB == 0) {
                        resetAll();
                    }
                    else {
                        resetCounters();
                        Toast.makeText(getActivity(),
                                       getResources().getString(R.string.long_press_reset_all),
                                       Toast.LENGTH_SHORT)
                             .show();
                    }
                    stop();
                    break;

                case R.id.txtTimer:
                    create_dialogPicker();
                    break;

                case R.id.txtTeam_A:
                    create_dialogEditTeamName('A');
                    break;

                case R.id.txtTeam_B:
                    create_dialogEditTeamName('B');
                    break;
                }

                return true;
            }
        };
        //endregion

        //region ClickListeners...
        imgHelp.setOnClickListener(this);

        mBtnReset.setOnClickListener(this);
        mBtnUndoA.setOnClickListener(this);
        mBtnUndoB.setOnClickListener(this);
        mTxtScoreA.setOnClickListener(this);
        mTxtScoreB.setOnClickListener(this);
        mTxtNameA.setOnClickListener(this);
        mTxtNameB.setOnClickListener(this);

        mBtnPlay.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
        mBtnSwap.setOnClickListener(this);

        mTxtTimer.setOnClickListener(this);

        mBtnUndoA.setOnLongClickListener(myLongClick);
        mBtnUndoB.setOnLongClickListener(myLongClick);
        mBtnReset.setOnLongClickListener(myLongClick);
        mTxtTimer.setOnLongClickListener(myLongClick);

        mTxtNameA.setOnLongClickListener(myLongClick);
        mTxtNameB.setOnLongClickListener(myLongClick);
        //endregion

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mDlgEditTeamName != null) {
            mDlgEditTeamName.dismiss();
        }

        if (mDlgHelp != null) {
            mDlgHelp.dismiss();
        }

        if (mDlgPicker != null) {
            mDlgPicker.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.txtTeam_A:
        case R.id.txtTeam_B:
            Toast.makeText(getActivity(),
                           getResources().getString(R.string.long_press_editName),
                           Toast.LENGTH_SHORT)
                 .show();
            break;

        case R.id.imgBtnHelp:
            create_dialogHelp();
            break;

        case R.id.btnReset:
            Toast.makeText(getActivity(),
                           getResources().getString(R.string.long_press_reset),
                           Toast.LENGTH_SHORT)
                 .show();
            break;

        case R.id.imgBtnPlayPause:
            if (mTimerState == TimerState.PLAYING) {
                pause();
            }
            else {
                play();
            }
            break;

        case R.id.imgBtnStop:
            stop();
            break;

        case R.id.txtTimer:
            Toast.makeText(getActivity(),
                           getResources().getString(R.string.long_press_setTime),
                           Toast.LENGTH_SHORT)
                 .show();
            break;

        case R.id.txtScore_A:
            mCounterA++;
            mCounterA = changeScore(mCounterA, 'A');

            break;

        case R.id.txtScore_B:
            mCounterB++;
            mCounterB = changeScore(mCounterB, 'B');

            break;

        case R.id.btnUndo_A:
        case R.id.btnUndo_B:
            Toast.makeText(getActivity(),
                           getResources().getString(R.string.long_press_undo),
                           Toast.LENGTH_SHORT)
                 .show();

            break;

        case R.id.imgBtnSwap:
            swapSides();
        }
    }
    //endregion

    //region Private Methods...
    //region Timer Methods

    private void play() {
        Logger.log('d', "PLAY");
        if (mTimerState != TimerState.PLAYING) {
            mBtnStop.setEnabled(true);
            mBtnPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));

            this.mTimerState = TimerState.PLAYING;
            setCounter(mDuration);

            mTxtTimer.setTextColor(Color.GREEN);
        }
    }

    private void pause() {
        Logger.log('d', "PAUSE");
        mBtnStop.setEnabled(true);
        mBtnPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        mDuration = mCurrentTimeLeft;
        setMillisOnTextView(mTxtTimer, mDuration);
        mTimer.cancel();
        this.mTimerState = TimerState.PAUSED;

        mTxtTimer.setTextColor(Color.YELLOW);
    }

    private void stop() {
        Logger.log('d', "STOP");

        mBtnStop.setEnabled(false);
        mBtnPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        this.mTimerState = TimerState.STOPPED;
        mTimer.cancel();
        mDuration = mSp.getLong(Logger.CONST_DEFAULT_TIME, Logger.DEFAULT_TIME);
        setMillisOnTextView(mTxtTimer, mDuration);

        mTxtTimer.setTextColor(Color.RED);
    }

    private void animateTimer() {
        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(5);
        animation.setRepeatMode(Animation.REVERSE);
        mTxtTimer.startAnimation(animation);
    }
    //endregion

    @SuppressWarnings("deprecation")
    private void swapSides() {
        String nameTemp;
        int counterTemp;
        int colorTemp;

        nameTemp = mTxtNameB.getText()
                            .toString();
        counterTemp = mCounterB;
        colorTemp = mSp.getInt(Logger.CONST_TEAM_B_COLOR, R.drawable.background_team_divider_red);


        //        mTxtNameB.setText(mTxtNameA.getText()
        //                                   .toString());
        changeName(mTxtNameA.getText()
                            .toString(), Logger.B);
        mSp.edit()
           .putString(Logger.CONST_TEAM_B_NAME,
                      mTxtNameA.getText()
                               .toString())
           .apply();
        mCounterB = mCounterA;
        setBackground(mRlB, mSp.getInt(Logger.CONST_TEAM_A_COLOR,
                                       R.drawable.background_team_divider_red));


        //mTxtNameA.setText(nameTemp);
        changeName(nameTemp, Logger.A);
        mCounterA = counterTemp;
        setBackground(mRlA, colorTemp);


        changeScore(mCounterA, 'A');
        changeScore(mCounterB, 'B');

        mSwapped = !mSwapped;

        //        setBackground(mRlA,
        //                      mSwapped ? R.drawable.background_team_divider_blue : R.drawable.background_team_divider_red);
        //        setBackground(mRlB,
        //                      mSwapped ? R.drawable.background_team_divider_red : R.drawable.background_team_divider_blue);

    }

    private void setInitialSettings() {
        mCounterA = changeScore(mSp.getInt(Logger.CONST_TEAM_A_SCORE, 0), 'A');
        mCounterB = changeScore(mSp.getInt(Logger.CONST_TEAM_B_SCORE, 0), 'B');

        //mCounterA = changeScore(0, 'A');
        //mCounterB = changeScore(0, 'B');

        mTxtNameA.setText(mSp.getString(Logger.CONST_TEAM_A_NAME,
                                        getActivity().getResources()
                                                     .getString(R.string.team_A)));
        mTxtNameB.setText(mSp.getString(Logger.CONST_TEAM_B_NAME,
                                        getActivity().getResources()
                                                     .getString(R.string.team_B)));

        setBackground(mRlA, mSp.getInt(Logger.CONST_TEAM_A_COLOR,
                                       R.drawable.background_team_divider_red));
        setBackground(mRlB, mSp.getInt(Logger.CONST_TEAM_B_COLOR,
                                       R.drawable.background_team_divider_blue));

        toogleResetButtonName();
        toogleResetButtonEnabled();

        stop();

        //TODO DISABLE BUTTON STOP
    }

    private void changeName(String name, char team) {
        if (team == 'A') {
            mTxtNameA.setText(name);
            mSp.edit()
               .putString(Logger.CONST_TEAM_A_NAME, name)
               .apply();
        }
        else if (team == 'B') {
            mTxtNameB.setText(name);
            mSp.edit()
               .putString(Logger.CONST_TEAM_B_NAME, name)
               .apply();
        }

    }

    private int changeScore(int value, char team) {
        value = checkValue(value);

        if (team == 'A') {
            mTxtScoreA.setText(String.valueOf(value));
            mSp.edit()
               .putInt(Logger.CONST_TEAM_A_SCORE, value)
               .apply();
        }
        else if (team == 'B') {
            mTxtScoreB.setText(String.valueOf(value));
            mSp.edit()
               .putInt(Logger.CONST_TEAM_B_SCORE, value)
               .apply();
        }


        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(50);

        return value;
    }

    private int checkValue(int value) {
        if (value < 0) {
            value = 0;
        }

        if (value > Logger.SCORE_MAX) {
            value = Logger.SCORE_MAX;
        }

        return value;
    }

    private void resetAll() {
        mSwapped = false;

        mSp.edit()
           .putInt(Logger.CONST_TEAM_A_SCORE, 0)
           .apply();
        mSp.edit()
           .putString(Logger.CONST_TEAM_A_NAME, getResources().getString(R.string.team_A))
           .apply();
        mTxtNameA.setText(getResources().getString(R.string.team_A));
        setBackground(mRlA, R.drawable.background_team_divider_red);


        mSp.edit()
           .putInt(Logger.CONST_TEAM_B_SCORE, 0)
           .apply();
        mSp.edit()
           .putString(Logger.CONST_TEAM_B_NAME, getResources().getString(R.string.team_B))
           .apply();
        mTxtNameB.setText(getResources().getString(R.string.team_B));
        setBackground(mRlB, R.drawable.background_team_divider_blue);
    }

    private void resetCounters() {
        mCounterA = 0;
        mCounterB = 0;

        changeScore(mCounterA, 'A');
        changeScore(mCounterB, 'B');


    }

    private void setPreferences() {
        mSp = getActivity().getPreferences(Context.MODE_PRIVATE);
    }
    //endregion

    //region Dialog...
    private void create_dialogEditTeamName(final char tag) {
        mDlgEditTeamName = new Dialog(getActivity());
        WindowManager.LayoutParams wmlp;
        final EditText edtTeamName;

        setUpWindow(mDlgEditTeamName);

        wmlp = mDlgEditTeamName.getWindow()
                               .getAttributes();
        wmlp.horizontalMargin = 0.1f;
        wmlp.verticalMargin = 0.1f;

        mDlgEditTeamName.setContentView(R.layout.dialog_editteamname);

        edtTeamName = (EditText) mDlgEditTeamName.findViewById(R.id.dlgEditTeamName_editTeamName);
        edtTeamName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString()
                              .trim();

                if (tag == Logger.A) {
                    mTxtNameA.setText(str);
                }
                else if (tag == Logger.B) {
                    mTxtNameB.setText(str);
                }
            }
        });

        View.OnClickListener myClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout rl = new RelativeLayout(getActivity());

                if (tag == Logger.A) {
                    rl = mRlA;
                }
                else if (tag == Logger.B) {
                    rl = mRlB;
                }

                switch (v.getId()) {
                case R.id.imgButton_Yellow:
                    setBackground(rl, R.drawable.background_team_divider_yellow);
                    break;

                case R.id.imgButton_Blue:
                    setBackground(rl, R.drawable.background_team_divider_blue);
                    break;

                case R.id.imgButton_Red:
                    setBackground(rl, R.drawable.background_team_divider_red);
                    break;

                case R.id.imgButton_Green:
                    setBackground(rl, R.drawable.background_team_divider_green);
                    break;

                case R.id.imgButton_White:
                    setBackground(rl, R.drawable.background_team_divider_white);
                    break;

                case R.id.imgButton_Black:
                    setBackground(rl, R.drawable.background_team_divider_black);
                    break;
                }
            }
        };

        setImgButton(R.id.imgButton_Blue, myClick, tag);
        setImgButton(R.id.imgButton_Green, myClick, tag);
        setImgButton(R.id.imgButton_Red, myClick, tag);
        setImgButton(R.id.imgButton_Yellow, myClick, tag);
        setImgButton(R.id.imgButton_White, myClick, tag);
        setImgButton(R.id.imgButton_Black, myClick, tag);

        mDlgEditTeamName.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                switch (tag) {
                case Logger.B:
                    setBackground(mRlA_Back, R.drawable.soccer_field_left);
                    break;

                case Logger.A:
                    setBackground(mRlB_Back, R.drawable.soccer_field_right);
                    break;
                }

                mRlA.setVisibility(View.VISIBLE);
                mRlB.setVisibility(View.VISIBLE);

                changeName(mTxtNameA.getText()
                                    .toString(), Logger.A);
                changeName(mTxtNameB.getText()
                                    .toString(), Logger.B);

                //                mSp.edit()
                //                   .putString(Logger.CONST_TEAM_A_NAME,
                //                              mTxtNameA.getText()
                //                                       .toString())
                //                   .apply();
                //                mSp.edit()
                //                   .putString(Logger.CONST_TEAM_B_NAME,
                //                              mTxtNameB.getText()
                //                                       .toString())
                //                   .apply();

                toogleResetButtonEnabled();
            }
        });

        switch (tag) {
        case Logger.A:
            wmlp.gravity = Gravity.TOP | Gravity.END;
            mRlB.setVisibility(View.INVISIBLE);
            setBackground(mRlB_Back, android.R.color.black);
            break;

        case Logger.B:
            wmlp.gravity = Gravity.TOP | Gravity.START;
            mRlA.setVisibility(View.INVISIBLE);
            setBackground(mRlA_Back, android.R.color.black);
            break;
        }

        mDlgEditTeamName.show();
    }

    private void toogleResetButtonName() {
        if (mCounterA == mCounterB && mCounterA == 0) {
            mBtnReset.setText(getResources().getString(R.string.reset_all));
        }
        else {
            mBtnReset.setText(getResources().getString(R.string.reset_score));
        }
    }

    private boolean toogleResetButtonEnabled() {
        if ((mCounterA == mCounterB && mCounterA == 0) && (mTxtNameA.getText()
                                                                    .toString()
                                                                    .equals(getResources().getString(
                                                                            R.string.team_A)) &&
                                                           mTxtNameB.getText()
                                                                    .toString()
                                                                    .equals(getResources().getString(
                                                                            R.string.team_B))) &&
            isDefaultBackground()) {
            mBtnReset.setEnabled(false);
            return false;
        }
        else {
            mBtnReset.setEnabled(true);
            return true;
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isDefaultBackground() {
        Drawable.ConstantState myDdrawableA = mRlA.getBackground()
                                                  .getConstantState();
        Drawable.ConstantState myDdrawableB = mRlB.getBackground()
                                                  .getConstantState();

        Drawable.ConstantState red = getResources().getDrawable(R.drawable.background_team_divider_red)
                                                   .getConstantState();
        Drawable.ConstantState blue = getResources().getDrawable(R.drawable.background_team_divider_blue)
                                                    .getConstantState();

        return (myDdrawableA == red && myDdrawableB == blue);
    }

    private ImageButton setImgButton(int id, View.OnClickListener myClick, char tag) {
        ImageButton imgButton;

        imgButton = (ImageButton) mDlgEditTeamName.findViewById(id);
        imgButton.setTag(tag);
        imgButton.setOnClickListener(myClick);

        return imgButton;
    }

    private void create_dialogHelp() {
        mDlgHelp = new Dialog(getActivity());

        mDlgHelp.setContentView(R.layout.dialog_help);
        mDlgHelp.setTitle(getString(R.string.app_name) + " " + getVersionName());

        TextView txtFullVersion = (TextView) mDlgHelp.findViewById(R.id.txtFullApp);

        txtFullVersion.setText(Html.fromHtml(getString(R.string.full_version)));
        txtFullVersion.setMovementMethod(LinkMovementMethod.getInstance());
        mDlgHelp.show();
    }

    private void create_dialogPicker() {
        mDlgPicker = new Dialog(getActivity());
        final MyTimePicker timePicker;
        Button btnSetTime, btnCancel;

        setUpWindow(mDlgPicker);

        mDlgPicker.setContentView(R.layout.dialog_timepicker);

        timePicker = (MyTimePicker) mDlgPicker.findViewById(R.id.timePicker);
        btnSetTime = (Button) mDlgPicker.findViewById(R.id.btnSetTime);
        btnCancel = (Button) mDlgPicker.findViewById(R.id.btnCancel);

        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                case R.id.btnSetTime:
                    mDuration =
                            (timePicker.getCurrentMinute() * 60 + timePicker.getCurrentSeconds()) *
                            1000;

                    setMillisOnTextView(mTxtTimer, mDuration);

                    mSp.edit()
                       .putLong(Logger.CONST_DEFAULT_TIME, mDuration)
                       .apply();
                    break;

                case R.id.btnCancel:
                    break;
                }
                mDlgPicker.dismiss();
            }
        };
        btnSetTime.setOnClickListener(onClick);
        btnCancel.setOnClickListener(onClick);

        timePicker.setCurrentTime(mSp.getLong(Logger.CONST_DEFAULT_TIME, Logger.DEFAULT_TIME));

        mDlgPicker.show();
    }

    //region Dialog Methods....
    @SuppressWarnings("deprecation")
    private void setBackground(View v, int color) {
        if (Build.VERSION.SDK_INT >= 16) {
            try {
                v.setBackground(getResources().getDrawable(color));
            }
            catch (Exception e) {
                switch (v.getTag()
                         .toString()
                         .charAt(0)) {
                case 'A':
                    v.setBackground(getResources().getDrawable(mSwapped ? R.drawable.background_team_divider_blue : R.drawable.background_team_divider_red));
                    break;
                case 'B':
                    v.setBackground(getResources().getDrawable(mSwapped ? R.drawable.background_team_divider_red : R.drawable.background_team_divider_blue));
                    break;
                }
            }
        }
        else {
            try {
                v.setBackgroundDrawable(getResources().getDrawable(color));
            }
            catch (Exception e) {
                switch (v.getTag()
                         .toString()
                         .charAt(0)) {
                case 'A':
                    v.setBackgroundDrawable(getResources().getDrawable(mSwapped ? R.drawable.background_team_divider_blue : R.drawable.background_team_divider_red));
                    break;
                case 'B':
                    v.setBackgroundDrawable(getResources().getDrawable(mSwapped ? R.drawable.background_team_divider_red : R.drawable.background_team_divider_blue));
                    break;
                }
            }
        }

        if (v.getId() == mRlA.getId()) {
            mSp.edit()
               .putInt(Logger.CONST_TEAM_A_COLOR, color)
               .apply();
        }
        else if (v.getId() == mRlB.getId()) {
            mSp.edit()
               .putInt(Logger.CONST_TEAM_B_COLOR, color)
               .apply();
        }
    }

    private void setUpWindow(Dialog d) {
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);

        d.getWindow()
         .setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        d.getWindow()
         .setBackgroundDrawableResource(android.R.color.transparent);
        d.getWindow()
         .setDimAmount(0.50f);
    }

    private String getVersionName() {
        String versionName = "";
        try {
            versionName = getActivity().getPackageManager()
                                       .getPackageInfo(getActivity().getPackageName(),
                                                       0).versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }
    //endregion
    //endregion

    //region Timer
    private void setCounter(final long duration) {
        mTimer = new Timer();
        final long startTime = duration + System.currentTimeMillis();

        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final long timeLeft = (startTime - System.currentTimeMillis());
                mCurrentTimeLeft = timeLeft;

                if (timeLeft > 0) {
                    Logger.log('d', "Ticking...." + (timeLeft));

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                setMillisOnTextView(mTxtTimer, timeLeft);
                            }
                        });

                        if (timeLeft < 3000) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    animateTimer();
                                }
                            });
                        }
                    }
                    else {
                        mTimer.cancel();
                    }
                }
                else {
                    Logger.log('d', "DONE");

                    playSound();
                    mTimer.cancel();

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            stop();
                        }
                    });
                }
            }

        }, 0, 450);
    }

    private void playSound() {
        MediaPlayer mp = MediaPlayer.create(getActivity(), R.raw.whistle);
        mp.start();
    }

    protected void setMillisOnTextView(TextView txtView, long millisUntilFinished) {
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
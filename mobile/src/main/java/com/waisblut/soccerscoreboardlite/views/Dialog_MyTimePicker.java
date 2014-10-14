package com.waisblut.soccerscoreboardlite.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.waisblut.soccerscoreboardlite.R;

public class Dialog_MyTimePicker
        extends AlertDialog
        implements DialogInterface.OnClickListener,
                   MyTimePicker.OnTimeChangedListener
{

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    public interface OnTimeSetListener
    {

        /**
         * @param view   The view associated with this listener.
         * @param minute The minute that was set.
         */
        void onTimeSet(MyTimePicker view, int minute, int seconds);
    }

    private static final String MINUTE = "minute";
    private static final String SECONDS = "seconds";

    private final MyTimePicker mTimePicker;
    private final OnTimeSetListener mCallback;

    int mInitialMinute;
    int mInitialSeconds;

    /**
     * @param context  Parent.
     * @param callBack How parent is notified.
     * @param minute   The initial minute.
     */
    public Dialog_MyTimePicker(Context context, OnTimeSetListener callBack, int minute, int seconds)
    {

        this(context, 0, callBack, minute, seconds);
    }

    /**
     * @param context  Parent.
     * @param theme    the theme to apply to this dialog
     * @param callBack How parent is notified.
     * @param minute   The initial minute.
     */
    @SuppressWarnings("deprecation")
    public Dialog_MyTimePicker(Context context,
                               int theme,
                               OnTimeSetListener callBack,
                               int minute,
                               int seconds)
    {
        super(context, theme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mCallback = callBack;
        mInitialMinute = minute;
        mInitialSeconds = seconds;

        setButton(context.getText(R.string.time_set), this);
        setButton2(context.getText(R.string.cancel), (OnClickListener) null);
        //setIcon(android.R.drawable.ic_dialog_time);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_timepicker, null);
        setView(view);
        mTimePicker = (MyTimePicker) view.findViewById(R.id.timePicker);

        // initialize state
        mTimePicker.setCurrentMinute(mInitialMinute);
        mTimePicker.setCurrentSecond(mInitialSeconds);
        mTimePicker.setOnTimeChangedListener(this);
    }

    public void onClick(DialogInterface dialog, int which)
    {
        if (mCallback != null)
        {
            mTimePicker.clearFocus();
            mCallback.onTimeSet(mTimePicker,
                                mTimePicker.getCurrentMinute(),
                                mTimePicker.getCurrentSeconds());
        }
    }

    public void onTimeChanged(MyTimePicker view, int minute, int seconds)
    {
        //updateTitle(minute, seconds);
    }

    public void updateTime(int minutOfHour, int seconds)
    {
        mTimePicker.setCurrentMinute(minutOfHour);
        mTimePicker.setCurrentSecond(seconds);
    }

    //    private void updateTitle(int minute, int seconds)
    //    {
    //        mCalendar.set(Calendar.MINUTE, minute);
    //        mCalendar.set(Calendar.SECOND, seconds);
    //        setTitle(mDateFormat.format(mCalendar.getTime()) + ":" + String.format("%02d", seconds));
    //    }

    @Override
    public Bundle onSaveInstanceState()
    {
        Bundle state = super.onSaveInstanceState();
        state.putInt(MINUTE, mTimePicker.getCurrentMinute());
        state.putInt(SECONDS, mTimePicker.getCurrentSeconds());

        return state;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        int minute = savedInstanceState.getInt(MINUTE);
        int seconds = savedInstanceState.getInt(SECONDS);
        mTimePicker.setCurrentMinute(minute);
        mTimePicker.setCurrentSecond(seconds);
        mTimePicker.setOnTimeChangedListener(this);
    }


}

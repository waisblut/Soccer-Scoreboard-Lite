package com.waisblut.soccerscoreboardlite.views;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import com.waisblut.soccerscoreboardlite.R;

import java.text.DecimalFormat;

public class MyTimePicker
        extends FrameLayout
{

    /**
     * A no-op callback used in the constructor to avoid null checks
     * later in the code.
     */
    @SuppressWarnings("UnusedDeclaration")
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = new OnTimeChangedListener()
    {
        @Override
        public void onTimeChanged(MyTimePicker view, int minute, int seconds)
        {

        }
    };

    public static final NumberPicker.Formatter TWO_DIGIT_FORMATTER = new NumberPicker.Formatter()
    {

        @Override
        public String format(int value)
        {
            return String.format("%02d", value);
        }
    };

    //region state
    private int mCurrentMinute = 0; // 0-59
    private int mCurrentSeconds = 0; // 0-59
    //endregion

    //region ui components
    private final NumberPicker mMinutePicker;
    private final NumberPicker mSecondPicker;
    //endregion

    // callbacks
    private OnTimeChangedListener mOnTimeChangedListener;

    public interface OnTimeChangedListener
    {
        /**
         * @param view    The view associated with this listener.
         * @param minute  The current minute.
         * @param seconds The current second.
         */
        void onTimeChanged(MyTimePicker view, int minute, int seconds);
    }

    @SuppressWarnings("UnusedDeclaration")
    public MyTimePicker(Context context)
    {
        this(context, null);
    }

    public MyTimePicker(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public MyTimePicker(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_timepicker, this, // we are the parent
                         true);

        // digits of minute
        mMinutePicker = (NumberPicker) findViewById(R.id.picker_minute);
        mMinutePicker.setMinValue(0);
        mMinutePicker.setMaxValue(59);
        mMinutePicker.setFormatter(TWO_DIGIT_FORMATTER);
        mMinutePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal)
            {
                mCurrentMinute = newVal;
                onTimeChanged();
            }
        });

        // digits of seconds
        mSecondPicker = (NumberPicker) findViewById(R.id.picker_seconds);
        mSecondPicker.setMinValue(0);
        mSecondPicker.setMaxValue(59);
        mSecondPicker.setFormatter(TWO_DIGIT_FORMATTER);
        mSecondPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                mCurrentSeconds = newVal;
                onTimeChanged();

            }
        });

        // initialize to current time
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);

        setCurrentMinute(12);
        setCurrentSecond(15);

        if (!isEnabled())
        {
            setEnabled(false);
        }
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        mMinutePicker.setEnabled(enabled);
    }

    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState
            extends BaseSavedState
    {

        private final int mSecond;
        private final int mMinute;

        private SavedState(Parcelable superState, int minute, int second)
        {
            super(superState);
            mSecond = second;
            mMinute = minute;
        }

        private SavedState(Parcel in)
        {
            super(in);
            mSecond = in.readInt();
            mMinute = in.readInt();
        }

        public int getSecond()
        {
            return mSecond;
        }

        public int getMinute()
        {
            return mMinute;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);
            dest.writeInt(mSecond);
            dest.writeInt(mMinute);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>()
        {
            public SavedState createFromParcel(Parcel in)
            {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size)
            {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mCurrentMinute, mCurrentSeconds);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentSecond(ss.getSecond());
        setCurrentMinute(ss.getMinute());
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onTimeChangedListener the callback, should not be null.
     */
    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener)
    {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentMinute()
    {
        return mCurrentMinute;
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentMinute(Integer currentMinute)
    {
        this.mCurrentMinute = currentMinute;
        updateMinuteDisplay();
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentSeconds()
    {
        return mCurrentSeconds;
    }

    /**
     * Set the current second (0-59).
     */
    public void setCurrentSecond(Integer currentSecond)
    {
        this.mCurrentSeconds = currentSecond;
        updateSecondsDisplay();
    }

    public void setCurrentTime(long millis)
    {

        this.mCurrentSeconds = Integer.parseInt(new DecimalFormat().format(
                ((millis / 60000f) % 1) * 60));
        this.mCurrentMinute = (int) (millis / 60000);

        updateSecondsDisplay();
        updateMinuteDisplay();
    }

    private void onTimeChanged()
    {
        mOnTimeChangedListener.onTimeChanged(this, getCurrentMinute(), getCurrentSeconds());
    }

    /**
     * Set the state of the spinners appropriate to the current minute.
     */
    private void updateMinuteDisplay()
    {
        mMinutePicker.setValue(mCurrentMinute);
        mOnTimeChangedListener.onTimeChanged(this, getCurrentMinute(), getCurrentSeconds());
    }

    /**
     * Set the state of the spinners appropriate to the current second.
     */
    private void updateSecondsDisplay()
    {
        mSecondPicker.setValue(mCurrentSeconds);
        mOnTimeChangedListener.onTimeChanged(this, getCurrentMinute(), getCurrentSeconds());
    }
}
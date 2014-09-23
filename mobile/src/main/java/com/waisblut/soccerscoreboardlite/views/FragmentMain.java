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
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.waisblut.soccerscoreboardlite.Logger;
import com.waisblut.soccerscoreboardlite.R;

import java.util.regex.Pattern;

public class FragmentMain
        extends Fragment
        implements View.OnClickListener
{

    //region Variables...
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
                        //                        Toast.makeText(getActivity(),
                        //                                       getResources().getString(R.string.long_press_reset_all),
                        //                                       Toast.LENGTH_SHORT).show();
                    }

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

        mBtnUndoA.setOnLongClickListener(myLongClick);
        mBtnUndoB.setOnLongClickListener(myLongClick);
        btnReset.setOnLongClickListener(myLongClick);
        //endregion

        return view;
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
    }

    @Override
    public void onClick(View v)
    {
        if (v.getTag() == null)
        {
            switch (v.getId())
            {
            case R.id.imgBtn_Config_A:
                //create_A_dialog('A');
                break;

            case R.id.imgBtn_Config_B:
                //create_A_dialog('B');
                break;

            case R.id.imgBtnHelp:
                create_A_dialogHelp();
                break;

            case R.id.btnReset:
                Toast.makeText(getActivity(),
                               getResources().getString(R.string.long_press_reset),
                               Toast.LENGTH_SHORT).show();
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

    //region Private Methods...
    private int changeScore(int value, char team)
    {
        value = (value < 0) ? 0 : value;

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

    //    private void create_A_dialog(final char tag)
    //    {
    //        mDlgConfig = new Dialog(getActivity());
    //        WindowManager.LayoutParams wmlp;
    //        //final ImageButton imgBtnRed, imgBtnGreen, imgBtnYellow;
    //        final EditText edtTeamName;
    //
    //        setUpWindow();
    //
    //        wmlp = mDlgConfig.getWindow().getAttributes();
    //        wmlp.horizontalMargin = 0.1f;
    //        wmlp.verticalMargin = 0.1f;
    //
    //        mDlgConfig.setContentView(R.layout.dialog_config);
    //
    //        edtTeamName = (EditText) mDlgConfig.findViewById(R.id.edtTeamName);
    //        edtTeamName.addTextChangedListener(new TextWatcher()
    //        {
    //            @Override
    //            public void beforeTextChanged(CharSequence s, int start, int count, int after)
    //            {
    //
    //            }
    //
    //            @Override
    //            public void onTextChanged(CharSequence s, int start, int before, int count)
    //            {
    //
    //            }
    //
    //            @Override
    //            public void afterTextChanged(Editable s)
    //            {
    //                String str = s.toString().trim();
    //
    //                if (tag == 'A')
    //                {
    //                    mTxtNameA.setText(str);
    //                }
    //                else if (tag == 'B')
    //                {
    //                    mTxtNameB.setText(str);
    //                }
    //            }
    //        });
    //
    //        View.OnClickListener myClick = new View.OnClickListener()
    //        {
    //            @Override
    //            public void onClick(View v)
    //            {
    //                RelativeLayout rl = new RelativeLayout(getActivity());
    //
    //                if (tag == 'A')
    //                {
    //                    rl = mRlA;
    //                }
    //                else if (tag == 'B')
    //                {
    //                    rl = mRlB;
    //                }
    //
    //                switch (v.getId())
    //                {
    //                case R.id.imgButton_Yellow:
    //                    setBackground(rl, R.drawable.background_team_divider_yellow);
    //                    break;
    //
    //                case R.id.imgButton_Blue:
    //                    setBackground(rl, R.drawable.background_team_divider_blue);
    //                    break;
    //
    //                case R.id.imgButton_Red:
    //                    setBackground(rl, R.drawable.background_team_divider_red);
    //                    break;
    //
    //                case R.id.imgButton_Green:
    //                    setBackground(rl, R.drawable.background_team_divider_green);
    //                    break;
    //                }
    //            }
    //        };
    //
    //        setImgButton(R.id.imgButton_Blue, myClick, tag);
    //        setImgButton(R.id.imgButton_Green, myClick, tag);
    //        setImgButton(R.id.imgButton_Red, myClick, tag);
    //        setImgButton(R.id.imgButton_Yellow, myClick, tag);
    //
    //        mDlgConfig.setOnDismissListener(new DialogInterface.OnDismissListener()
    //        {
    //            @Override
    //            public void onDismiss(DialogInterface dialog)
    //            {
    //                switch (tag)
    //                {
    //                case 'B':
    //                    setBackground(mRlA_Back, R.drawable.soccer_field_left);
    //                    break;
    //
    //                case 'A':
    //                    setBackground(mRlB_Back, R.drawable.soccer_field_right);
    //                    break;
    //                }
    //
    //                mRlA.setVisibility(View.VISIBLE);
    //                mRlB.setVisibility(View.VISIBLE);
    //
    //                mSp.edit().putString(Logger.TEAM_A_NAME, mTxtNameA.getText().toString()).apply();
    //                mSp.edit().putString(Logger.TEAM_B_NAME, mTxtNameB.getText().toString()).apply();
    //            }
    //        });
    //
    //        switch (tag)
    //        {
    //        case 'A':
    //            wmlp.gravity = Gravity.TOP | Gravity.END;
    //            mRlB.setVisibility(View.INVISIBLE);
    //            setBackground(mRlB_Back, android.R.color.black);
    //            break;
    //
    //        case 'B':
    //            wmlp.gravity = Gravity.TOP | Gravity.START;
    //            mRlA.setVisibility(View.INVISIBLE);
    //            setBackground(mRlA_Back, android.R.color.black);
    //            break;
    //        }
    //
    //        mDlgConfig.show();
    //    }

    //region Dialog Methods....
    @SuppressWarnings("deprecation")
    private void setBackground(View v, int color)
    {
        if (Build.VERSION.SDK_INT >= 16)
        {
            v.setBackground(getResources().getDrawable(color));
        }
        else
        {
            v.setBackgroundDrawable(getResources().getDrawable(color));
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

    //    private void setUpWindow()
    //    {
    //        mDlgConfig.requestWindowFeature(Window.FEATURE_NO_TITLE);
    //
    //        mDlgConfig.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    //        mDlgConfig.getWindow().setDimAmount(0.05f);
    //    }
    //
    //    private ImageButton setImgButton(int id, View.OnClickListener myClick, char tag)
    //    {
    //        ImageButton imgButton;
    //
    //        imgButton = (ImageButton) mDlgConfig.findViewById(id);
    //        imgButton.setTag(tag);
    //        imgButton.setOnClickListener(myClick);
    //
    //        return imgButton;
    //    }
    //endregion
    //endregion
}
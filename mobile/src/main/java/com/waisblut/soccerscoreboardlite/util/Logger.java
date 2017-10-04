package com.waisblut.soccerscoreboardlite.util;

import android.util.Log;

import com.waisblut.soccerscoreboardlite.BuildConfig;

public final class Logger
{
    //Project Constants
    private final static String TAG = "waisblut";

    public final static char A = 'A';
    public final static char B = 'B';
    public final static String CONST_TEAM_A_SCORE = "Team A Score";
    public final static String CONST_TEAM_B_SCORE = "Team B Score";
    public final static String CONST_TEAM_A_NAME = "Team A Name";
    public final static String CONST_TEAM_B_NAME = "Team B Name";
    public final static String CONST_TEAM_A_COLOR = "Team A Color";
    public final static String CONST_TEAM_B_COLOR = "Team B Color";
    public final static int SCORE_MAX = 99;
    public final static String CONST_DEFAULT_TIME = "Default Time";
    public final static long DEFAULT_TIME = (60 * 7) * 1000;//7Min

    private final static boolean IS_DEBUG = BuildConfig.DEBUG;

    public static void log(char type, String s)
    {
        if (IS_DEBUG)
        {
            switch (type)
            {
            case 'd':
                Log.d(TAG, s);
                break;

            case 'e':
                Log.e(TAG, s);
                break;

            case 'i':
                Log.i(TAG, s);
                break;

            case 'v':
                Log.v(TAG, s);
                break;

            case 'w':
                Log.w(TAG, s);
                break;

            default:
                break;
            }
        }
    }
}

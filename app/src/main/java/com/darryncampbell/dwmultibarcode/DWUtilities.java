package com.darryncampbell.dwmultibarcode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class DWUtilities {

    public static final String PROFILE_INTENT_ACTION = "com.zebra.dwmultibarcode";
    public static final String PROFILE_NAME = "DataWedge MultiBarcode";

    private static final String EXTRA_GET_VERSION_INFO = "com.symbol.datawedge.api.GET_VERSION_INFO";
    private static final String EXTRA_KEY_NOTIFICATION_TYPE = "com.symbol.datawedge.api.NOTIFICATION_TYPE";
    private static final String EXTRA_KEY_APPLICATION_NAME = "com.symbol.datawedge.api.APPLICATION_NAME";
    private static final String EXTRA_REGISTER_NOTIFICATION = "com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION";
    private static final String EXTRA_UNREGISTER_NOTIFICATION = "com.symbol.datawedge.api.UNREGISTER_FOR_NOTIFICATION";
    private static final String EXTRA_GET_CONFIG = "com.symbol.datawedge.api.GET_CONFIG";
    private static final String EXTRA_GET_ACTIVE_PROFILE = "com.symbol.datawedge.api.GET_ACTIVE_PROFILE";
    private static final String EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE";
    private static final String EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";

    private static final String ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION";
    private static final String EXTRA_EMPTY = "";

    //  Receiving
    public static final String ACTION_RESULT_DATAWEDGE = "com.symbol.datawedge.api.RESULT_ACTION";
    public static final String EXTRA_RESULT_GET_ACTIVE_PROFILE = "com.symbol.datawedge.api.RESULT_GET_ACTIVE_PROFILE";
    public static final String EXTRA_RESULT_GET_VERSION_INFO = "com.symbol.datawedge.api.RESULT_GET_VERSION_INFO";
    public static final String EXTRA_RESULT_GET_CONFIG = "com.symbol.datawedge.api.RESULT_GET_CONFIG";
    public static final String ACTION_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION_ACTION";
    public static final String EXTRA_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION";
    public static final String EXTRA_RESULT_NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    public static final String EXTRA_KEY_VALUE_SCANNER_STATUS = "SCANNER_STATUS";
    public static final String EXTRA_KEY_VALUE_NOTIFICATION_STATUS = "STATUS";
    public static final String EXTRA_KEY_VALUE_PROFILE_SWITCH = "PROFILE_SWITCH";

    public static void getDWVersion(Context context) {
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_GET_VERSION_INFO, EXTRA_EMPTY);
    }

    public static void registerForProfileSwitch(Context context) {
        Bundle extras = new Bundle();
        extras.putString(EXTRA_KEY_APPLICATION_NAME, context.getPackageName());
        extras.putString(EXTRA_KEY_NOTIFICATION_TYPE, EXTRA_KEY_VALUE_SCANNER_STATUS);
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_REGISTER_NOTIFICATION, extras);
    }

    public static void deregisterProfileSwitch(Context context)
    {
        Bundle extras = new Bundle();
        extras.putString(EXTRA_KEY_APPLICATION_NAME, context.getPackageName());
        extras.putString(EXTRA_KEY_NOTIFICATION_TYPE, EXTRA_KEY_VALUE_SCANNER_STATUS);
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_UNREGISTER_NOTIFICATION, extras);
    }

    public static void getActiveProfile(Context context) {
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_GET_ACTIVE_PROFILE, EXTRA_EMPTY);
    }

    public static void getConfig(Context context)
    {
        Bundle bMain = new Bundle();
        bMain.putString("PROFILE_NAME", PROFILE_NAME);
        Bundle bConfig = new Bundle();
        ArrayList<String> pluginName = new ArrayList<>();
        pluginName.add("BARCODE");
        bConfig.putStringArrayList("PLUGIN_NAME", pluginName);
        bMain.putBundle("PLUGIN_CONFIG", bConfig);
        //  This is one example of a config that can be obtained.  The documentation details how
        //  to obtain the associated applications with a profile or the current scanner status
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_GET_CONFIG, bMain);
    }

    public static void createProfile(Context context) {
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_CREATE_PROFILE, PROFILE_NAME);

        //  Now configure that created profile to apply to our application
        Bundle profileConfig = new Bundle();
        profileConfig.putString("PROFILE_NAME", PROFILE_NAME);
        profileConfig.putString("PROFILE_ENABLED", "true"); //  Seems these are all strings
        profileConfig.putString("CONFIG_MODE", "UPDATE");

        Bundle barcodeConfig = new Bundle();
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
        Bundle barcodeProps = new Bundle();
        //  Note: configure_all_scanners does not work here, I guess because not all DW scanners (Camera?) support multi barcode
        //barcodeProps.putString("configure_all_scanners", "true");
        barcodeProps.putString("scanner_selection_by_identifier", "INTERNAL_IMAGER");
        barcodeProps.putString("scanner_input_enabled", "true");
        barcodeProps.putString("scanning_mode", "3");
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps);
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);
        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME", context.getPackageName());      //  Associate the profile with this app
        appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});
        profileConfig.putParcelableArray("APP_LIST", new Bundle[]{appConfig});
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);

        //  You can only configure one plugin at a time, we have done the barcode input, now do the intent output
        profileConfig.remove("PLUGIN_CONFIG");
        Bundle intentConfig = new Bundle();
        intentConfig.putString("PLUGIN_NAME", "INTENT");
        intentConfig.putString("RESET_CONFIG", "true");
        Bundle intentProps = new Bundle();
        intentProps.putString("intent_output_enabled", "true");
        intentProps.putString("intent_action", PROFILE_INTENT_ACTION);
        intentProps.putString("intent_delivery", "2");
        intentConfig.putBundle("PARAM_LIST", intentProps);
        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig);
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);

        //  Disable keyboard output
        profileConfig.remove("PLUGIN_CONFIG");
        Bundle keystrokeConfig = new Bundle();
        keystrokeConfig.putString("PLUGIN_NAME", "KEYSTROKE");
        keystrokeConfig.putString("RESET_CONFIG", "true");
        Bundle keystrokeProps = new Bundle();
        keystrokeProps.putString("keystroke_output_enabled", "false");
        keystrokeConfig.putBundle("PARAM_LIST", keystrokeProps);
        profileConfig.putBundle("PLUGIN_CONFIG", keystrokeConfig);
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);
    }

    public static void setConfig(Context context, int numberOfBarcodesPerScan, Boolean bReportInstantly)
    {
        Bundle profileConfig = new Bundle();
        profileConfig.putString("PROFILE_NAME", PROFILE_NAME);
        profileConfig.putString("PROFILE_ENABLED", "true"); //  Seems these are all strings
        profileConfig.putString("CONFIG_MODE", "UPDATE");

        Bundle barcodeConfig = new Bundle();
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
        barcodeConfig.putString("RESET_CONFIG", "true");
        Bundle barcodeProps = new Bundle();
        //  Note: configure_all_scanners does not work here, I guess because not all DW scanners (Camera?) support multi barcode
        barcodeProps.putString("scanner_selection_by_identifier", "INTERNAL_IMAGER");
        barcodeProps.putString("scanning_mode", "3");
        if (bReportInstantly)
            barcodeProps.putString("instant_reporting_enable", "true");
        else
            barcodeProps.putString("instant_reporting_enable", "false");
        barcodeProps.putString("multi_barcode_count", "" + numberOfBarcodesPerScan);
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps);
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);
    }


    private static void sendDataWedgeIntentWithExtra(Context context, String action, String extraKey, String extraValue)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extraValue);
        context.sendBroadcast(dwIntent);
    }

    private static void sendDataWedgeIntentWithExtra(Context context, String action, String extraKey, Bundle extras)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extras);
        context.sendBroadcast(dwIntent);
    }

}

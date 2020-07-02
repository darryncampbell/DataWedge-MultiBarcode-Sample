package com.darryncampbell.dwmultibarcode;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.darryncampbell.dwmultibarcode.DWUtilities.EXTRA_RESULT_GET_ACTIVE_PROFILE;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final String LOG_TAG = "Datawedge MultiBarcode";
    boolean m_bUiInitialised;
    boolean m_bReportInstantly = false;
    int m_iBarcodeCount = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_bUiInitialised = false;

        IntentFilter filter = new IntentFilter();
        filter.addAction(DWUtilities.ACTION_RESULT_DATAWEDGE);//  DW 6.2
        filter.addCategory(Intent.CATEGORY_DEFAULT);    //  NOTE: this IS REQUIRED for DW6.2 and up!
        filter.addAction(DWUtilities.ACTION_RESULT_NOTIFICATION);      //  DW 6.3 for notifications
        filter.addAction(DWUtilities.PROFILE_INTENT_ACTION);
        registerReceiver(myBroadcastReceiver, filter);

        DWUtilities.getDWVersion(getApplicationContext());
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
        DWUtilities.deregisterProfileSwitch(getApplicationContext());
    }

    private void profileIsApplied()
    {
        if (!m_bUiInitialised)
        {
            m_bUiInitialised = true;
            TextView lblHeading = findViewById(R.id.lblHeading);
            lblHeading.setText("Please wait... retrieving configuration");
            DWUtilities.getConfig(getApplicationContext());
        }
    }

    private void populateUI(String instantReportingEnabled, String multiBarcodeCount) {
        CheckBox chkReportInstantly = findViewById(R.id.chkReportInstantly);
        EditText txtMultiBarcodeCount = findViewById(R.id.txtBarcodeCount);
        TextView lblHeading = findViewById(R.id.lblHeading);
        Button btnMinus = findViewById(R.id.btnDec);
        Button btnPlus = findViewById(R.id.btnInc);
        Button btnClear = findViewById(R.id.btnClear);
        chkReportInstantly.setEnabled(true);
        if (instantReportingEnabled.equalsIgnoreCase("true"))
        {
            chkReportInstantly.setChecked(true);
            m_bReportInstantly = true;
            lblHeading.setText(getResources().getString(R.string.maxBarcodes));
        }
        else
        {
            chkReportInstantly.setChecked(false);
            m_bReportInstantly = false;
            lblHeading.setText(getResources().getString(R.string.numBarcodes));
        }
        txtMultiBarcodeCount.setText(multiBarcodeCount);
        m_iBarcodeCount = Integer.parseInt(multiBarcodeCount);
        btnMinus.setEnabled(true);
        btnPlus.setEnabled(true);
        btnClear.setEnabled(true);

        chkReportInstantly.setOnCheckedChangeListener(this);
        btnMinus.setOnClickListener(this);
        btnPlus.setOnClickListener(this);
        btnClear.setOnClickListener(this);
    }

    private void displayScanResult(Intent intent)
    {
        TextView txtOutput = findViewById(R.id.txtOutput);
        String output = txtOutput.getText().toString();
        String decoded_mode = intent.getStringExtra("com.symbol.datawedge.decoded_mode");
        if (decoded_mode.equalsIgnoreCase("multiple_decode"))
        {
            String barcodeBlock = "";
            List<Bundle> multiple_barcodes = (List<Bundle>) intent.getSerializableExtra("com.symbol.datawedge.barcodes");
            if (multiple_barcodes != null)
            {
                //output += "Multi Barcode count: " + multiple_barcodes.size() + '\n';
                for (int i = 0; i < multiple_barcodes.size(); i++)
                {
                    Bundle thisBarcode = multiple_barcodes.get(i);
                    String barcodeData = thisBarcode.getString("com.symbol.datawedge.data_string");
                    String symbology = thisBarcode.getString("com.symbol.datawedge.label_type");
                    barcodeBlock += "Barcode: " + barcodeData + " [" + symbology + "]";
                    if (multiple_barcodes.size() != 1)
                        barcodeBlock += "\n";
                }
            }
            txtOutput.setText(barcodeBlock + "\n" + output);
        }
    }

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            //  This is useful for debugging to verify the format of received intents from DataWedge
            //for (String key : b.keySet())
            //{
            //    Log.v(LOG_TAG, key);
            //}
            if (action.equals(DWUtilities.PROFILE_INTENT_ACTION))
            {
                //  Received a barcode scan
                displayScanResult(intent);
            }
            else if (action.equals(DWUtilities.ACTION_RESULT_DATAWEDGE))
            {
                if (intent.hasExtra(EXTRA_RESULT_GET_ACTIVE_PROFILE))
                {
                    String activeProfile = intent.getStringExtra(EXTRA_RESULT_GET_ACTIVE_PROFILE);
                    if (DWUtilities.PROFILE_NAME.equalsIgnoreCase(activeProfile))
                    {
                        //  The correct DataWedge profile is now in effect
                        profileIsApplied();
                    }
                }
                else if (intent.hasExtra(DWUtilities.EXTRA_RESULT_GET_VERSION_INFO))
                {
                    //  6.3 API for GetVersionInfo
                    Bundle versionInformation = intent.getBundleExtra(DWUtilities.EXTRA_RESULT_GET_VERSION_INFO);
                    String DWVersion = versionInformation.getString("DATAWEDGE");
                    Log.i(LOG_TAG, "DataWedge Version info: " + DWVersion);
                    if (DWVersion.compareTo("8.0.0") >= 1)
                    {
                        //  Register for profile change - want to update the UI based on the profile
                        DWUtilities.createProfile(getApplicationContext());
                        DWUtilities.registerForProfileSwitch(getApplicationContext());
                        DWUtilities.getActiveProfile(getApplicationContext());
                    }
                }
                else if (intent.hasExtra(DWUtilities.EXTRA_RESULT_GET_CONFIG))
                {
                    Bundle result = intent.getBundleExtra(DWUtilities.EXTRA_RESULT_GET_CONFIG);
                    ArrayList<Bundle> pluginConfig = result.getParcelableArrayList("PLUGIN_CONFIG");
                    //  In the call to Get_Config we only requested the barcode plugin config (which will be index 0)
                    Bundle barcodeProps = pluginConfig.get(0).getBundle("PARAM_LIST");
                    String instantReportingEnabled = barcodeProps.getString("instant_reporting_enable");
                    String multiBarcodeCount = barcodeProps.getString("multi_barcode_count");

                    populateUI(instantReportingEnabled, multiBarcodeCount);
                }
            }
            else if (action.equals(DWUtilities.ACTION_RESULT_NOTIFICATION))
            {
                //  6.3 API for RegisterForNotification
                if (intent.hasExtra(DWUtilities.EXTRA_RESULT_NOTIFICATION))
                {
                    Bundle extras = intent.getBundleExtra(DWUtilities.EXTRA_RESULT_NOTIFICATION);
                    String notificationType = extras.getString(DWUtilities.EXTRA_RESULT_NOTIFICATION_TYPE);
                    if (notificationType != null && notificationType.equals(DWUtilities.EXTRA_KEY_VALUE_PROFILE_SWITCH))
                    {
                        //  The profile has changed
                        if (DWUtilities.PROFILE_NAME.equalsIgnoreCase(extras.getString("PROFILE_NAME")))
                        {
                            //  The correct DataWedge profile is now in effect
                            profileIsApplied();
                        }
                    }
                    if (notificationType != null && notificationType.equals(DWUtilities.EXTRA_KEY_VALUE_SCANNER_STATUS))
                    {
                        if (extras.getString(DWUtilities.EXTRA_KEY_VALUE_NOTIFICATION_STATUS).equalsIgnoreCase("SCANNING"))
                        {
                            //  Bit of a hack but most reliable way to ensure there is always a LF between scan sessions
                            TextView txtOutput = findViewById(R.id.txtOutput);
                            txtOutput.setText("\n" + txtOutput.getText().toString());
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
        //  InstantReporting check has changed
        m_bReportInstantly = bChecked;
        TextView lblHeading = findViewById(R.id.lblHeading);
        if (bChecked)
            lblHeading.setText(getResources().getString(R.string.maxBarcodes));
        else
            lblHeading.setText(getResources().getString(R.string.numBarcodes));
        DWUtilities.setConfig(getApplicationContext(), m_iBarcodeCount, bChecked);
    }

    @Override
    public void onClick(View view) {
        //  Button has been clicked
        TextView numBarcodes = findViewById(R.id.txtBarcodeCount);
        String barcodesAsString = numBarcodes.getText().toString();
        Integer barcodesAsInt = Integer.parseInt(barcodesAsString);
        if (view.getId() == R.id.btnDec)
        {
            if (barcodesAsInt > 2)
            {
                barcodesAsInt--;
                m_iBarcodeCount = barcodesAsInt;
                numBarcodes.setText("" + barcodesAsInt);
                DWUtilities.setConfig(getApplicationContext(), barcodesAsInt.intValue(), m_bReportInstantly);
            }
        }
        else if (view.getId() == R.id.btnInc)
        {
            if (barcodesAsInt < 100)
            {
                barcodesAsInt++;
                m_iBarcodeCount = barcodesAsInt;
                numBarcodes.setText("" + barcodesAsInt);
                DWUtilities.setConfig(getApplicationContext(), barcodesAsInt.intValue(), m_bReportInstantly);
            }
        }
        else if (view.getId() == R.id.btnClear)
        {
            TextView txtOutput = findViewById(R.id.txtOutput);
            txtOutput.setText("");
        }
    }
}
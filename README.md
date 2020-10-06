*Please be aware that this application / sample is provided as-is for demonstration purposes without any guarantee of support*
=========================================================

# DataWedge MultiBarcode Sample

This application demonstrates how to capture multiple barcodes with a single trigger pull on a Zebra Android mobile computer using the "Basic Multibarcode" feature of DataWedge.

![Application](https://github.com/darryncampbell/DataWedge-MultiBarcode-Sample/raw/master/screenshots/app.jpg)

This application requires DataWedge version 7.3 or higher to run.

Over the past few years there have been a number of ways to scan multiple barcodes simultaneously which has led to some confusion over what is available and in which version of DataWedge:

## MultiBarcode History in DataWedge

- Very early: [AimType Continuous Read](https://techdocs.zebra.com/datawedge/5-0/guide/decoders/#readerparams) allowing multiple barcodes to be scanned with a single trigger pull. 
- v5.0: [SimulScan Input plugin](https://techdocs.zebra.com/datawedge/6-3/guide/input/simulscan/) introduced that supported capturing multiple barcodes.
- v6.7: [Multibarcode support first introduced](https://techdocs.zebra.com/datawedge/6-7/guide/input/barcode/#multibarcodedecoding) as a property of the barcode plugin, _but you needed to specify the exact number of barcodes being scanned_.
- v7.3: A new [Instant Reporting](https://techdocs.zebra.com/datawedge/7-3/guide/input/barcode/#multibarcodedecoding) parameter was added to send each unique barcode _immediately_ as it is scanned.  It was no longer necessary to specify the exact number of barcodes being scanned. 
- v7.6: A new [Report Decoded Barcodes](https://techdocs.zebra.com/datawedge/7-6/guide/input/barcode/#multibarcodeparams) parameter was added so you did not have to specify the exact number of barcodes being scanned but you only receive the scanned barcodes at the end of the scan session (i.e. not _immediately_)
- v8.0: [NextGen SimulScan](https://techdocs.zebra.com/datawedge/8-0/guide/input/barcode/#nextgensimulscanconfiguration) was introduced to replace the SimulScan Input plugin, which was deprecated.  NextGen SimulScan is an amalgamation of the multibarcode features introduced in DataWedge v6.7, v7.3 and v7.6.

There are a number of additional caveats when working with multibarcode e.g. interaction between `Instant Reporting` and `AimType:Continuous Read`.  For more information please see the [latest multibarcode documentation](https://techdocs.zebra.com/datawedge/latest/guide/input/barcode/#basicmultibarcodeparams)

## The Sample Application

This sample application makes use of [Basic Multibarcode capture](https://techdocs.zebra.com/datawedge/latest/guide/input/barcode/#multibarcodedecoding) as well as allowing the user to turn on the  [Instant Reporting](https://techdocs.zebra.com/datawedge/7-3/guide/input/barcode/#multibarcodedecoding) feature.

To use the app, either:

- Enable Instant Reporting and scan a sheet of barcodes

Or:

- Disable Instant Reporting and specify the number of barcodes being scanned prior to scanning a sheet of barcodes.

A [sample set of barcodes](https://github.com/darryncampbell/DataWedge-MultiBarcode-Sample/raw/master/screenshots/barcodes.jpg) is included in this repository if needed.  I recommend you print it out rather than trying to scan it from a computer screen for best performance.

You can see the sample app in action in the below video, scanning the [sample barcodes](https://github.com/darryncampbell/DataWedge-MultiBarcode-Sample/raw/master/screenshots/barcodes.jpg).

[![Sample app](https://img.youtube.com/vi/bN7KJ16s6Gg/0.jpg)](https://www.youtube.com/watch?v=bN7KJ16s6Gg)

### Sample app code

The sample application will modify the DataWedge profile associated with it using the [SET_CONFIG API](https://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/), as below:

```java
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
    //  Note: configure_all_scanners does not work here.
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
```

A number of other APIs are also used to ensure the app UI accurately reflects the profile configuration and to verify that the DataWedge version is high enough.

## DataWedge Configuration

This article assumes familiarity with Zebra's DataWedge tool as well as the DataWedge profile mechanism.  For an overview of DataWedge, please refer to the [DataWedge Techdocs page](https://techdocs.zebra.com/datawedge/latest/guide/overview/)

When you first run this application, it will create a profile associated with itself called `DataWedge MultiBarcode` and set the following properties in that profile:

- Scanning Mode is set to `Basic MultiBarcode`
- Output plugin set to send a Broadcast intent to `com.zebra.dwmultibarcode`

The following profile properties are then modified as you interact with the application:

- `Instant Reporting` enabled / disabled
- `Multiple Barcode Count` updated

If for whatever reason DataWedge does not get configured correctly, please refer to the screenshots below:

![DataWedge 1](https://github.com/darryncampbell/DataWedge-MultiBarcode-Sample/raw/master/screenshots/dw1.jpg)
![DataWedge 2](https://github.com/darryncampbell/DataWedge-MultiBarcode-Sample/raw/master/screenshots/dw2.jpg)
![DataWedge 3](https://github.com/darryncampbell/DataWedge-MultiBarcode-Sample/raw/master/screenshots/dw3.jpg)



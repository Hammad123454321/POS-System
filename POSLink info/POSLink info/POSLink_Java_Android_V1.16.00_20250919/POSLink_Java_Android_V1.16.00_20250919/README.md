# Overview

## Documents for Java/Android developers

This guide is a quick start guide for adding **POSLink** to Java/Android applications. 

## Get started

[Integrate your App with POSLink](#Integrate POSLink)

[Sample code](#Samples)

[API reference](#reference)

[Libraries](#libraries)

## Guides

This section illustrate how to build your App with POSLink SDK

### Integration Mode

You can find more details under directory [Guide][1][^1]

#### Semi-Integration

If you are building a semi-integration App, you can read **POSLink_Java_Android_API_Guide.pdf** and [API reference](#reference)

### Integrate POSLink

#### Enable log

This is a global log setting, 

```java
LogSetting.setLogMode(true);  // Open or close log
LogSetting.setLevel(LogSetting.LOGLEVEL.DEBUG);  // Set log level
LogSetting.setLogFileName("POSLinkLog");  // Set the file name of output log
LogSetting.setOutputPath("sdcard/log");  // Set path for output log
LogSetting.setLogDays("30");  // Keep log for 30 days
```

#### Configure communication parameters

We supports 8 kinds of communication type(**UART, USB, Bluetooth, TCP, SSL, HTTP, HTTPS, AIDL**), AIDL is only for Android, you can learn more in [Reference](#reference).

 Here is an example for TCP connection:

```java
CommSetting commSetting = new CommSetting();
commSetting.setType(CommSetting.TCP);   
commSetting.setDestIP("192.168.0.1");
commSetting.setDestPort("10009");
commSetting.setTimeOut("60000");  // 60s connection timeout 
```

You can hold the **commSetting** as a global reference in your code to reuse it when App restarts or for different transactions. 

To learn more about TCP/AIDL settings, you can check **TCP_Guide/AIDL_Guide** pdf under [Guide][1][^1]

#### Initialize SDK

##### Java

```java
PosLink poslink = new PosLink();  // Create POSLink instance
```

##### Android

1. Add permission to AndroidManifest

   ```xml
   <!-- For usb communication -->
   <uses-feature android:name="android.hardware.usb.host" />
   
   <!-- For network communication -->
   <uses-permission android:name="android.permission.INTERNET"/>
   <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
   
   <!-- For bluetooth communication -->
   <uses-permission android:name="android.permission.BLUETOOTH" />
   <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
   <!-- TargetSdkVersion greater than or equal to 31 -->
   <uses-permission android:name="android.permission.BLUETOOTH_SCAN"/>
   
   <!-- For using scanner -->
   <uses-permission android:name="android.permission.CAMERA"/>
   <uses-permission android:name="android.permission.WAKE_LOCK" />
   <uses-permission android:name="android.permission.FLASHLIGHT"/>
   
   <!-- For using ICC/PICC/Printer peripheries -->
   <uses-permission android:name="com.pax.permission.PICC"/>
   <uses-permission android:name="com.pax.permission.ICC"/>
   <uses-permission android:name="com.pax.permission.PED"/>
   <uses-permission android:name="com.pax.permission.PRINTER"/>
   <uses-permission android:name="com.pax.permission.USB_SECURITY"/>
   
   <!-- AIDL, targetSdkVersion greater than or equal to 30 -->
   <queries>
       <intent>
           <action android:name="com.pax.us.std.poslink.aidl" />
       </intent>
   </queries>
   ```
   <table><tr><td bgcolor=#e1f5fe><b>Note:</b> The Android SDK 29 or higher changed the package visibility rule (<a href="https://developer.android.com/training/package-visibility">https://developer.android.com/training/package-visibility</a>). If the &lt;queries&gt; tag is not added to the AndroidManifest, you need to set the targetSdkVersion to 29 and below, otherwise POSLink will not be able to communicate with BroadPOS via AIDL.</td></tr></table>
   
1. Initialize Android SDK

   ```java
   POSLinkAndroid.init(application); // Pass application context as parameter
   ```
   There are several init methods in class `POSLinkAndroid` in [Android API][Android API], you could init the SDK according to your requirements. 
   
   <table><tr><td bgcolor=#e1f5fe><b>Note:</b> Different with Java SDK, Android SDK need to be initialized in Application at first when App startup.</td></tr></table>
   
2. Create the instance before starting the transaction.

   ```java
   PosLink poslink = new PosLink(context);  // Create POSLink instance
   ```

#### Prepare transaction parameters

```java
// Set the communication type to POSLink instance
poslink.SetCommSetting(commSetting);

 /**
  * Setup the Request. It can be PaymentRequest, ManageRequest, BatchRequest, ReportRequest
  *
  * For management request, just replace the code below with
  * ManageRequest request = new ManageRequest()"
  */
PaymentRequest request = new PaymentRequest();

// Set the TenderType and TransType for the request first so that POS knows which part of data of request should be used.
request.TenderType = request.ParseTenderType("CREDIT");
request.TransType = request.ParseTransType("SALE");

// Your unique ID for this transaction
request.ECRRefNum = "1";

//Optional fields.
request.Amount = "1100";

// Assign the request instance to poslink instance's PaymentRequest field
poslink.PaymentRequest = request;
```

#### Start transaction

```java
/**
 * Start transaction 
 * 
 * Note: because this execution is time consuming, 
 *       please call it on work thread, otherwise it may block UI rendering
 */
ProcessTransResult ptr = poslink.ProcessTrans();
```

#### Get transaction result

```java
// Check transaction result
if(ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {
    /**
     * When the transResult.Code is OK, then the response has already been 
     * assigned to poslink instance's PaymentResponse field automatically, 
     * what you only need to do is get the response from the field 
     */
    PaymentResponse response = poslink.PaymentResponse; 
    // Todo assign the response's value to your UI
} else if (ptr.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
    String errorMsg = ptr.Msg
    // Todo show error msg on your UI
} else {
    String errorMsg = ptr.Msg
    // Todo show error msg on your UI
}
```

 For more details, you can see **API Guide** in [Reference](#reference), or check in [Samples Code](#samples)

#### POSLink2 Compatibility

We strongly recommend using the latest version of POSLink SDK (POSLink  V2 SDK). The newest version of the POSLink V2 SDK provides a more  concise API and more detailed notes for interface and parameters.  However, if you have to use the POSLink SDK with POSLink V2 SDK, you can update the POSLink SDK to V1.13.00 to make it compatible with the  POSLink V2 SDK. Likewise, you can keep the POSLink SDK before completely switching to the POSLink V2 SDK.

## Reference

This section provides the guides and API reference you need.

Start building your app with  POSLink APIs. They are available in [Java][Java API] and [Android][Android API].

<table><tr><td bgcolor=#e1f5fe><b>Note:</b> Many Android reference topics are derived from Java-based source code. This means that almost all Android reference are the same with Java's, except for some Android specific functions</td></tr></table>

## Samples

The samples are wrote in [Java][2][^2] and [Android][3][^3], including pre-build binary file, source code and guide.

### Java

You can build your App with POSLink Java SDK for both [Linux](#linux) or [Windows](#windows) platform

To run Java demo, you need pre-setup the environment. [Learn more](./Sample/Java/POSLinkJavaDemo/README.md)

### Android

The android demo provides a comprehensive demo for POSLink abilities in [Android](#android) platform

To run Android Demo, just install the APK, or build the APK by yourself. [Learn more](./Sample/android/How to build the POSLink Android demo.docx)

## Libraries

It contains three platform(Android, Linux, Windows) libraries. 
<table><tr><td bgcolor=#e1f5fe><b>Note:</b> All libraries are wrote in Java with almost the same API, except for the different dependency, specific functions</td></tr></table>

### Android

Start integrate your App with [Android library][4][^4]

see [Android setup](./libs/android/README.md)

### Linux


Start integrate your App with [Linux library][5][^5]

see [Linux setup](./libs/linux/README.docx)

### Windows

Start integrate your App with [Windows library][6][^6]

see [Windows setup](./libs/windows/README.md)

## Release notes

This [doc](./POSLink_Java_Android_Release Notes.pdf) links to announcements and release notes for releases to the stable version.



[^1]: ./Guide

[1]: ./Guide	"Guide Directory"

[^2]: ./Sample/Java

[2]: ./Sample/Java	"Java Sample"

[^3]: ./Sample/android

[3]: ./Sample/android "Android Sample"

[^4]: ./libs/android

[4]: ./libs/android	"Android Library"

[^5]: ./libs/linux

[5]: ./libs/linux	"Linux Library"

[^6]: ./libs/windows

[6]: ./libs/windows	"Windows Library"
[Android API]: ./Reference/doc_android/index.html
[Java API]: ./Reference/doc_java/index.html

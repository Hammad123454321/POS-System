We offer several ways to use the libraries in Android. Choose the way you like.

### 1 AAR
You can just add the aar file to a directory such as /libAars. This needs gradle support.
And add the following content to your build.gradle file :

```
  repositories {
    flatDir {
        dirs 'libAars'
    }
  }

  dependencies {
	compile(name: 'PAX_POSLinkAndroid_20XXXXXX', ext: 'aar')
  }
```

If you use this aar in your library project, you need to add
```
	flatDir {
		project(':your_lib_project_name').file('libs')
	}
```
to your app project, too.

Note: The AAR integrates GLComm and GLExtPrinter. In addition, other libraries can be added to your application as required according to the situation described in the JAR below.

### 2 JAR
Traditional. Just use all the jars as libraries. And the so file under armeabi should be imported too to support custom scan codec.
After v1.01.03_20180409, the PaxCommomLib_V20170524.jar was removed and GLComm_V1.02.00_20180402.jar is used.

libDCL.so and libIGLBarDecoder.so：
Used for PAX device scan code and result decoding. If it is not a Pax device or the function of scanning code is not needed, these so files may not be added.

Also, the following libraries can be added to the application according to their needs.
1. GLExtPrinter_V1.01.01_20191225.jar (Since POSLINK_V1.02.00)
  If you want to use a Bluetooth printer, need to add it.

  Note: After POSLINK_V1.09.00 version, GLComm needs to be updated to GLComm_V1.08.00_20210514.

2. Zxing-core-3.3.3.jar (Since POSLINK_V1.02.00)
  If you want to support printing barcodes, you need to add it.
  Copy zxing-core-x.x.x.jar to your project, or use the following method to load Zxing dependencies.
  Apache Maven:
```
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.3.3</version>
</dependency>
```
Gradle Groovy DSL:
```
implementation 'com.google.zxing:core:3.3.3'
```
3. Okio(Since POSLINK_V1.06.00):
  
  The tool library that okHttp depends on. If you use Gradle or Maven to import okHttp, you do not need to import this library. If you are copying okhttp.jar to the project, you need to add okio.jar to the project.

4. OkHttp (Since POSLINK_V1.06.00):
  This library is used to enhance the speed of UpdateResource. If you want to increase the speed of UpdateResource, you need to add it.
  Copy okhttp-x.x.x.jar and okio-x.x.x.jar to your project, or use the following method to load OkHttp dependencies.

  Note: If you choose to import the jar package directly into the project, you need to add okio.jar. Another library may also depend on the okio package, causing package conflicts. It is recommended to use Gradle or Maven to add okHttp.

  Apache Maven:
```
<dependency>
  <groupId>com.squareup.okhttp3</groupId>
  <artifactId>okhttp</artifactId>
  <version>x.x.x</version>
</dependency>
```
Gradle Groovy DSL:
```
implementation 'com.squareup.okhttp3:okhttp:x.x.x'
```

5. Gson (Since POSLINK_V1.06.00):
This library is used to enhance the speed of UpdateResource. If you want to increase the speed of UpdateResource, you need to add it.
Copy gson-x.x.x.jar to your project, or use the following method to load Gson dependencies.
Apache Maven:
```
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.8.6</version>
</dependency>
```
Gradle Groovy DSL:
```
implementation 'com.google.code.gson:gson:2.8.6'
```
6. JSch (Since POSLINK_V1.07.00):
This library is used by POSLink itself to upload logs to SFTP, namely POSLink.uploadLog().
Copy jsch-x.x.x.jar to your project, or use the following method to load JSch dependencies.
Apache Maven:
```
<dependency>
    <groupId>com.jcraft</groupId>
    <artifactId>jsch</artifactId>
    <version>0.1.55</version>
</dependency>
```
Gradle Groovy DSL:
```
implementation 'com.jcraft:jsch:0.1.55'
```

7. GLComm [V1.10.02_T_20220707] (Since POSLink_V1.01.03):
  The GLCOMM Library is only necessary if POSLink is using either Bluetooth, or USB as it’s primary communication mode. If the POS application will not use these features, then there is no need to import the GLComm library
### 3 Parts of The JAR Files. (Deprecated after 2017/11/23)

In the new version, the logback-android-classic-1.1.1-6.jar, logback-android-core-1.1.1-6.jar, and slf4j-api-1.7.21.jar dependencies are removed.
Please remove them from your dependencies if you don't need them.

------------------
### External Lib

1. The external lib Commonlib_20150420_dex has been removed!
You do not need to add it to your application anymore, just remove it. --2017/05/25
2. casio_vr7000_addon_1_0_3/casioregdevicelibrary_dex.jar
This is for Casio device vr7000. Only when you use this device that copies it to your asset dir of Android project.
3. casio_vx100_addon_3_8/caiosdevice_dex.jar
This is for Casio device vx100. Only when you use this device that copies it to your asset dir of Android project.

------------------
Now in Android, some other libraries are used, the lib initialization must be called at the start of the Android application.

The directory will be created automatically.

Call LogSetting.setOutputPath() and LogSetting.setLogFileName() first.
And then call the 
```
POSLinkAndroid.init(getApplicationContext());
```
For example:
```
public class MainApplication extends Application{
	@Override
	public void onCreate() {
		super.onCreate();
		POSLinkAndroid.init(getApplicationContext());
	}
}
```
------------------
###To use USB 

1. Because the USB implementation is in Android, the initialization way of POSLink changes.

Just call 
```
POSLink poslink = POSLinkCreator.createPoslink(context);
// Or POSLink poslink = new POSLink(context);
```
to replace 
```
POSLink poslink = new POSLink();
```

2.Add
```
<uses-feature android:name="android.hardware.usb.host"/>
```
to your AndroidManifest.xml
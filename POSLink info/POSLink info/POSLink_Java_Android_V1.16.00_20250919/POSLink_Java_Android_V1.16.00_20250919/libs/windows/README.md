
# Windows 
----------------------------------------------------

Choose your binary build - x64 or x86 (based on which version of
the JVM you are installing to)

NOTE: You MUST match your architecture.  You can't install the i386
version on a 64-bit version of the JDK and vice-versa.

For a JDK installation:

~~Copy RXTXcomm.jar ---> <JAVA_HOME>\jre\lib\ext or use it to your project lib~~
~~Copy rxtxSerial.dll ---> <JAVA_HOME>\jre\bin~~
~~Copy rxtxParallel.dll ---> <JAVA_HOME>\jre\bin~~

Because the RXTX library has a serious bug in the high version of the JDK, it will cause the JVM to crash. Therefore, it is not recommended to use RXTX, use JserialComm instead of RXTX. (Since POSLINK_V1.08.00)

Copy JSerialComm.jar ---> <JAVA_HOME>\jre\lib\ext or use it to your project lib

These libs are used to implement UART communication.

------------------
### External Lib

The following libraries can be added to the application according to their needs.

#### Okio: (Since POSLINK_V1.06.00)

The tool library that okHttp depends on. If you use Gradle or Maven to import okHttp, you do not need to import this library. If you are copying okhttp.jar to the project, you need to add okio.jar to the project.

#### OkHttp: (Since POSLINK_V1.06.00)
This library is used to enhance the speed of UpdateResource. If you want to increase the speed of UpdateResource, you need to add it.
Copy okhttp-x.x.x.jar and okio-x.x.x.jar to your project, or use the following method to load OkHttp dependencies.

Note: If you choose to import the jar package directly into the project, you need to add okio.jar. Another library may also depend on the okio package, causing package conflicts. It is recommended to use Gradle or Maven to add okHttp.

Apache Maven:

```
<dependency>
  <groupId>com.squareup.okhttp3</groupId>
  <artifactId>okhttp</artifactId>
  <version>4.9.0</version>
</dependency>
```
Gradle Groovy DSL:
```
implementation 'com.squareup.okhttp3:okhttp:4.9.0'
```

#### Gson: (Since POSLINK_V1.06.00)
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
#### JSch: (Since POSLINK_V1.07.00)
This library is used by POSLink itself to upload logs to SFTP, namely POSLink.uploadLog().
Copy jsch-x.x.x.jar to your project, or use the following method to load Gson dependencies.
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
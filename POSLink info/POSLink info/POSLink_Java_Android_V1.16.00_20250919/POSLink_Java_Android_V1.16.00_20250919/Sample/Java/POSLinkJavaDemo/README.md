###How to run the program:

1. Make sure you have installed JRE and add it to your system environment.
2. Double click start.bat under ./POSLinkJavaDemo
3. This program is a Java Program. You can ignore the start.bat and use javaw to run in shell:
     javaw -jar POSLinkJavaDemo.jar
4. If you want to use UART, remember to copy these two dll to your jre lib.
     Win:
       Copy rxtxSerial.dll ---> <JAVA_HOME>\jre\bin
       Copy rxtxParallel.dll ---> <JAVA_HOME>\jre\bin
       Linux:
       Copy librxtxSerial.so ---> <JAVA_HOME>\jre\lib\amd64
       Copy librxtxParallel.so ---> <JAVA_HOME>\jre\lib\amd64 (Different operating system names are different, such as aarch64)
       Note: The RXTX library only searches through /dev/ttySxx, so you need to make symlinks if your distro does the same, so for example ln -s /dev/ttyPos0 /dev/ttyS33.
       Besides that, you need to close the serial port after starting, to prevent Linux from making new devices, like /dev/ttyPos0. Do not forget to remove the lock file from /var/lock if you forgot to close the port.  

### Runtime Requirements

POSLinkJavaDemo.jar requires a Java Runtime Environment (JRE) **with JavaFX support**.

Please note that recent versions of Java no longer include JavaFX by default.  
To ensure compatibility, we recommend using **Java 8 JRE**, which already bundles JavaFX.

You can run the demo with:
```bash
java -jar POSLinkJavaDemo.jar
```

### External Lib

The following libraries can be added to the application according to their needs.

#### OkHttp: (Since POSLINK_V1.06.00)
This library is used to enhance the speed of UpdateResource. Use UpdateResource for large files. If you want to increase the speed of UpdateResource, you need to add it.
Copy okhttp-x.x.x.jar and okio-x.x.x.jar to your project, or use the following method to load OkHttp dependencies.

Note: If you choose to import the jar package directly into the project, you need to add okio.jar. Another library may also depend on okio package, causing package conflicts. It is recommended to use Gradle or Maven to add okHttp.

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
This library is used to enhance the speed of UpdateResource. Use UpdateResource for large files. If you want to increase the speed of UpdateResource, you need to add it.

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



###How to build the Code:

Just run
  gradlew jfxJar
under POSLinkJavaDemoCode
Then you will see output files in /POSLinkJavaDemoCode/build/jfx/POSLinkJavaDemo


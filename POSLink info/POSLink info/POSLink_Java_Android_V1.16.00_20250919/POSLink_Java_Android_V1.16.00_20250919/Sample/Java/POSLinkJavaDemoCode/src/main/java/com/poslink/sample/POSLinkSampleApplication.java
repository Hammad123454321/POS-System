package com.poslink.sample;

import com.pax.poslink.util.LogStaticWrapper;
import com.poslink.sample.common.LogUtil;
import com.poslink.sample.jsbridge.ConsoleLog;
import com.poslink.sample.jsbridge.JSBridge;
import com.poslink.sample.jsbridge.Java2JavascriptUtils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class POSLinkSampleApplication extends Application {

    private final String PAGE = "/static/home.html";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        createWebView(primaryStage, PAGE);
    }

    private void createWebView(Stage primaryStage, String page) {
        System.out.println("init");

        final WebView webView = new WebView();

        // show "alert" Javascript messages in stdout (useful to debug)
        WebEngine engine = webView.getEngine();

        if (LogUtil.DEBUG) {
            engine.documentProperty().addListener((observable, oldValue, newValue) -> enableFireBug(engine));
        }
        Java2JavascriptUtils.connectBackendObject(webView.getEngine(), "jsBridge", new JSBridge());
        Java2JavascriptUtils.connectBackendObject(webView.getEngine(), "console", new ConsoleLog());
        webView.getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent<String> arg0) {
                System.err.println("alert: " + arg0.getData());
            }
        });

        engine.load(getClass().getResource(page).toExternalForm());

        primaryStage.setMaximized(true);
        primaryStage.setScene(new Scene(webView));
        primaryStage.setTitle("POSLinkDemo");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/res/icon_window.ico")));
        primaryStage.show();
    }

    private static void enableFireBug(WebEngine engine) {
        engine.executeScript("if (!document.getElementById('FirebugLite')){" +
                "E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;" +
                "E = E ? document['createElement' + 'NS'](E, 'script') " +
                ": document['createElement']('script');E['setAttribute']('id', 'FirebugLite');" +
                "E['setAttribute']('src', 'js/libs/' + 'firebug-lite.js' + '#startOpened');" +
                "E['setAttribute']('FirebugLite', '4');" +
                "(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);" +
                "E = new Image;E['setAttribute']('src', 'js/libs/' + '#startOpened');" +
                "}");
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false"); // enhance fonts
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osVer = System.getProperty("os.version");
        String vmVer = System.getProperty("java.vm.version");
        LogStaticWrapper.getLog().v(String.format("env version: %s, %s, %s, %s", javaVersion, osName, osVer, vmVer));
        launch(args);
    }
}


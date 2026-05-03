package com.pax.poslink.fullIntegration.mock;

import com.pax.poslink.util.LogStaticWrapper;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Iterator;

/**
 * @author Justin.Z
 */
public class XmlObjectDom4j {

    private Document document;
    private Element root;

    public XmlObjectDom4j(String rootElement) {
        document = null;
        try {
            document = DocumentHelper.createDocument();
            root = null;
            root = document.addElement(rootElement);
        } catch (Exception e) {
            LogStaticWrapper.getLog().exceptionLog(e);
        }
    }


    public void put(String name, String value) {
        Element ele = root.addElement(name);
        ele.setText(value);
    }

    public void put(String name, XmlObjectDom4j value) {
        Element ele = root.addElement(name);
        Iterator it = value.root.elementIterator();
        while (it.hasNext()) {
            Element element = (Element) it.next();
            Element temp = ele.addElement(element.getName());
            temp.setText(element.getText());
        }
    }

    @Override
    public String toString() {
        return root.asXML();
    }
}

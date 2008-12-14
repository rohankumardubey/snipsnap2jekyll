/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anzix.snipsnap2xwiki.migator;

import net.anzix.snipsnap2xwiki.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author elek
 */
public abstract class AbstractMigrator {

    private MigrationContext context;

    public AbstractMigrator(MigrationContext context) {
        this.context = context;
    }

    public abstract void migrate(Element root) throws Exception;

    protected Element findFirstObject(Element root, String className) {
        List<Element> objects2 = root.getChildren("object");
        for (Element object : objects2) {
            if (object.getChild("className").getText().equals(className)) {
                return object;
            }
        }
        throw new IllegalArgumentException("No such object: " + className);
    }

    protected Element findLastObject(Element root, String className) {
        Element result = null;
        List<Element> objects2 = root.getChildren("object");
        for (Element object : objects2) {
            if (object.getChild("className").getText().equals("XWiki.XWikiRights")) {
                result = object;
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("No such object: " + className);
        } else {
            return result;
        }
    }

    protected void modifyProperty(Element object, String propertyName, String value) {
        for (Element e : (List<Element>) object.getChildren("property")) {
            if (e.getChild(propertyName) != null) {
                e.getChild(propertyName).setText(value);
                return;
            }
        }
        throw new IllegalArgumentException("No such proeprty: " + object + "+" + propertyName);
    }

    protected void writeFile(String space, String name, Document content) throws IOException {
        //write output
        XMLOutputter outputter = new XMLOutputter();
        outputter.output(content, new FileWriter(new File(new File(context.getOutputDir(), space), name + ".xml")));
    //write to package descriptor

    }

    public MigrationContext getContext() {
        return context;
    }
}

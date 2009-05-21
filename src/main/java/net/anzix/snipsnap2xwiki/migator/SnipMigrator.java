/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anzix.snipsnap2xwiki.migator;

import net.anzix.snipsnap2xwiki.*;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.anzix.snipsnap2xwiki.transformation.AddPrefix;
import net.anzix.snipsnap2xwiki.transformation.Transformation;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author elek
 */
public class SnipMigrator extends AbstractObjectMigrator {

    private boolean migrateAttachments = true;

    private Set<String> ignorePages;

    public SnipMigrator(MigrationContext context) {
        super(context);
        ignorePages = new HashSet();
        ignorePages.add("start");
        ignorePages.add("start2");
        ignorePages.add("sandbox");
        ignorePages.add("snips-by-user");
    }

    @Override
    protected boolean includeInMigration(Element e) {

        if (e.getName().equals("user")) {
            return false;
        }
        String name = e.getChildText("name");
        if (name.equals("admin")) {
            return false;
        } else if (name.startsWith("comment") || name.startsWith("start/") || name.startsWith("topics/")) {
            return false;
        } else if (name.startsWith("SnipSnap") || name.startsWith("snipsnap")) {
            return false;
        } else if (ignorePages.contains(name)) {
            return false;
        } else if (getContext().getUserCache().containsKey(name)) {
            return false;
        }
        return true;
    }

    @Override
    public void migrateObject(Element oldRoot) throws Exception {

        String name = oldRoot.getChildText("name");


        Document d = builder.build(new File("src/main/template/Page.xml"));

        //open template
        Element newRoot = d.getRootElement();

        Transformation xwikiPrefix = new AddPrefix("XWiki.");

        if (name.contains("/")) {

            String parentName = name.substring(0, name.lastIndexOf("/"));
            name = name.replaceAll("/", "");
            parentName = parentName.replaceAll("/", "");
            newRoot.getChild("parent").setText("Main." + parentName);
        }


        //copy properties
        DomCopier copier = new DomCopier(oldRoot, newRoot);
        copier.copyText("name", "name", new Transformation() {

            @Override
            public String transform(String source) {
                return source.replaceAll("/", "");
            }
        });

        copier.copyText("name", "title");
        copier.copyText("mUser", "contentAuthor", xwikiPrefix);
        copier.copyText("mUser", "author", xwikiPrefix);
        copier.copyText("cUser", "creator", xwikiPrefix);

        copier.copyText("cTime", "creationDate");
        copier.copyText("mTime", "date");
        copier.copyText("mTime", "contentUpdateDate");

        copier.copyText("content", "content", getContext().getSyntaxTransformation());
        if (migrateAttachments) {
            for (Element attachemnt : (List<Element>) oldRoot.getChild("attachments").getChildren("attachment")) {
                Element newAttachemnt = new Element("attachment");
                newRoot.addContent(newAttachemnt);
                DomCopier attahcmentCopier = new DomCopier(attachemnt, newAttachemnt);
                attahcmentCopier.copyText("name", "filename");
                attahcmentCopier.copyText("size", "filesize");
                attahcmentCopier.copyText("date", "date");
                attahcmentCopier.copyText("data", "content");
                addTag(newAttachemnt, "version", "1.0");
                addTag(newAttachemnt, "author", "XWiki.Admin");
                addTag(newAttachemnt, "comment", "migrated from snipsnap");
            }
        }

        copyComments(name, newRoot);
        fixObjextNames(name, newRoot);

        //write output
        writeFile("Main", name, d);
        getContext().pageMigratedSuccesfully("Main." + name);

    }

    public void setMigrateAttachments(boolean migrateAttachments) {
        this.migrateAttachments = migrateAttachments;
    }
}

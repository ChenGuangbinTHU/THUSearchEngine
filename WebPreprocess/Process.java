import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
//import org.pdfbox.pdfparser.PDFParser;
//import org.pdfbox.pdmodel.PDDocument;
//import org.pdfbox.util.PDFTextStripper;
import org.apache.commons.lang3.StringEscapeUtils;


import java.io.*;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by chen on 17-5-23.
 */
public class Process {
    MySql ms = null;
    String rootUrl = null;//use for Jsoup to convert relative path to absolute path


    Process() {
        ms = new MySql();
        ms.getConnection();
        ms.getStatement();
        try{
            File file =new File("debug.txt");

            //if file doesnt exists, then create it
            if(!file.exists()){
                file.createNewFile();
            }

            //true = append file
            FileWriter fileWritter = new FileWriter(file.getName(),true);
            BufferedWriter writer = new BufferedWriter(fileWritter);
            System.out.println("Done");

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public String getCurrentUrl() {
        return rootUrl;
    }

    public String wrapContent(String content) {
        return "\""+StringEscapeUtils.escapeHtml3(content)+"\"";
    }

    public void saveHtmlToDB(String filename) throws IOException{
        WebAnalyse wb = new WebAnalyse(filename,rootUrl);
        if(wb.doc == null)
            return;

        Map<String,String> links = wb.getLinks();
        String text = wb.getAllText();
        String url = wb.getOwnUrl();
        String title = wb.getTitle();
        String h1 = wb.getH("h1");
        String h2 = wb.getH("h2");
        if(!h1.equals(""))
            title = h1;
        else
            title = "";
        String href = "";
        String a = "";
        for (HashMap.Entry<String, String> entry: links.entrySet()) {
            href += entry.getKey();
            href += '\n';
            a += entry.getValue();
            a += '\n';
        }

        String sql = "insert into "+ms.getTableName()+"(url,out_links,text,h1,h2,title,a) values(" +wrapContent(url)
                +","+wrapContent(href)+","+wrapContent(text)+","+wrapContent(h1)+
                ","+wrapContent(h2)+","+wrapContent(title)+","+wrapContent(a)+")";
        ms.create(ms.stmt,sql);
        return;
    }

    public void savePDFToDB(String filename){
        String s = "";
        try {
            FileInputStream is = new FileInputStream(filename);

            PDFTextStripper stripper = new PDFTextStripper();
            PDDocument pdfDocument = PDDocument.load(is);

            StringWriter writer = new StringWriter();
            stripper.writeText(pdfDocument, writer);

            s = writer.getBuffer().toString();
            //System.out.println(s);
            pdfDocument.close();
            writer.close();
        } catch (Exception e) {

        }

        String sql = "insert into "+ms.getTableName()+"(url,text) values(" +wrapContent(Tools.getStringAfterMirror(filename))
                +","+wrapContent(s)+")";
        ms.create(ms.stmt,sql);
    }

    public void saveWord2003ToDB(String filename){
        File file = null;
        WordExtractor extractor = null;
        String doc = "";
        try
        {

            file = new File(filename);
            FileInputStream fis = new FileInputStream(file.getAbsolutePath());
            HWPFDocument document = new HWPFDocument(fis);
            extractor = new WordExtractor(document);
            String[] fileData = extractor.getParagraphText();

            for (int i = 0; i < fileData.length; i++)
            {
                if (fileData[i] != null)
                    doc+=fileData[i];
            }
        }
        catch (Exception exep)
        {

        }

        String sql = "insert into "+ms.getTableName()+"(url,text) values(" +wrapContent(Tools.getStringAfterMirror(filename))
                +","+wrapContent(doc)+")";
        ms.create(ms.stmt,sql);
    }

    public void saveWord2007ToDB(String filename){
        String text = "";
        try {
            InputStream is = new FileInputStream(filename);
            XWPFDocument doc = new XWPFDocument(is);
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            text = extractor.getText();
            is.close();
        } catch (Exception e) {

        }

        String sql = "insert into test(url,text) values(" +wrapContent(Tools.getStringAfterMirror(filename))
                +","+wrapContent(text)+")";
        ms.create(ms.stmt,sql);

    }


    public static void main(String[] args) throws IOException {

        String rootDir = "";//Web Page
        Process p = new Process();
        Set<String> s = new HashSet<>();
        Files.walkFileTree(Paths.get(rootDir), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // TODO Auto-generated method stub
                String dirname = dir.toString();
                String parentDir = dir.getParent().toString();
                if(parentDir.substring(parentDir.length()-6).equals("mirror")) {
                    System.out.println(dirname);
                    p.setRootUrl("http://"+dir.getFileName().toString());
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // TODO Auto-generated method stub
                String filename = file.toString();
                int pos = filename.lastIndexOf('/');
                pos = filename.substring(0,pos).lastIndexOf('/');
                String lastFileName = filename.substring(pos);
                if(s.contains(lastFileName)) {
                    return FileVisitResult.SKIP_SUBTREE;
                } else {
                    s.add(lastFileName);
                }
                if (file.getFileName().toString().endsWith(".html") ||
                        file.getFileName().toString().endsWith(".htm")
                        ) {
                    p.saveHtmlToDB(filename);
                } else if(file.getFileName().toString().endsWith(".doc")) {
                    p.saveWord2003ToDB(filename);
                } else if(file.getFileName().toString().endsWith(".docx")) {
                    p.saveWord2007ToDB(filename);
                } else if(file.getFileName().toString().endsWith(".pdf")) {
                    p.savePDFToDB(filename);
                }
                return FileVisitResult.CONTINUE;
            }

        });
    }
}

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by chen on 17-5-23.
 */
public class WebAnalyse {

    String filepath = null;
    String baseurl = null;
    Document doc = null;
    String filename = null;

    public WebAnalyse(String filepath,String baseurl) throws IOException{
        this.filepath = filepath;
        this.baseurl = baseurl.endsWith("/")?baseurl:baseurl+"/";
        File input = new File(filepath);
        filename = input.getName();
        try {
            doc = Jsoup.parse(input,"UTF-8",baseurl);
        } catch (IllegalArgumentException e) {
            doc = null;
        }

    }

    /*
    get out links
     */
    public Map<String,String> getLinks() {
        Elements links = doc.getElementsByTag("a");
        Map<String,String> map = new HashMap<>();
        if(links == null)
            return null;
        int cnt = 0;
        for (Element i : links) {
            String url = i.absUrl("href").toString();
            String text = i.text();
            url = String.valueOf(cnt) + ":" + url;
            text = String.valueOf(cnt) + ":" + text;
            map.put(url,text);
            cnt++;
        }
        return map;
    }

    public String getA() {
        Elements links = doc.getElementsByTag("a");
        String ret = "";
        if(links == null)
            return ret;
        for (Element i : links) {
            String url_text = i.text();
            ret += url_text;
            ret += "\n";

        }
        return ret;
    }

    public String getAllText() {
        return doc.text();
    }

    public String getH(String h) {
        Elements tags = doc.getElementsByTag(h);
        String ret = "";
        if(tags == null)
            return ret;
        for (Element tag : tags) {
            ret += tag.text();
            ret += '\n';
        }
        return ret;
    }



    public String getOwnUrl() {
        return Tools.getStringAfterMirror(filepath);
    }

    public String getTitle() {
        if(doc.title() == null)
            return "";
        return doc.title();
    }


    public static void main(String[] args) throws IOException{
        WebAnalyse wb = new WebAnalyse("src/test.html","http://news.tsinghua.edu.cn/");
    }
}

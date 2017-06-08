/**
 * Created by chen on 17-5-22.
 */

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MySql {

    // JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/SchoolSearch?characterEncoding=utf-8";

    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "";
    static final String PASS = "";


    public ResultSet rst = null;
    Connection conn = null;
    Statement stmt = null;

    BufferedWriter writer = null;
    String tableName = "";

    MySql() {
        try{
            File file =new File("debug_html.txt");

            if(!file.exists()){
                file.createNewFile();
            }

            FileWriter fileWritter = new FileWriter(file.getName(),true);
            writer = new BufferedWriter(fileWritter);
            System.out.println("Done");

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void getConnection() {

        try {
            // 注册 JDBC 驱动
            Class.forName("com.mysql.jdbc.Driver");

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch(ClassNotFoundException e){
            System.out.println("加载数据库驱动失败:\n"+e.toString());
            return;
        }
        catch (SQLException e) {
            System.out.println("获取连接失败" + e.toString());
            return;
        }
    }

    public Statement getStatement() {
        try {
            stmt = conn.createStatement();
        }catch (SQLException e) {
            System.out.println("获取连接失败" + e.toString());
            return null;
        }
        return stmt;
    }

    public int create(Statement stmt,String sqlCreate){
        int nRecord=0;
        try{
            nRecord =stmt.executeUpdate(sqlCreate);
        }
        catch(SQLException e){
            try {
                writer.write(sqlCreate + '\n'+e.toString()+'\n');
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return nRecord;
    }

    public ResultSet read(Statement stmt,String sqlSelect){
        ResultSet rst =null;
        //System.out.println(sqlSelect);
        try{
            rst =stmt.executeQuery(sqlSelect);
            //System.out.println("查询成功");
        }
        catch(SQLException e){
            System.out.println(e.toString());
        }
        return rst;
    }

    public int update(Statement stmt,String sqlUpdate){
        int nRecord=0;
        try{
            nRecord =stmt.executeUpdate(sqlUpdate);
            System.out.println("更新成功");
        }
        catch(SQLException e){
            System.out.println(e.toString());
        }
        return nRecord;
    }

    //删除记录[delete from table1 where 范围]
    public int delete(Statement stmt,String sqlDelete){
        int nRecord=0;
        try{
            nRecord =stmt.executeUpdate(sqlDelete);
            System.out.println("删除成功");
        }
        catch(SQLException e){
            System.out.println(e.toString());
        }
        return nRecord;
    }

    //关闭结果集
    public void closeResultSet(ResultSet rst){
        try{
            rst.close();
        }
        catch(SQLException e){
            System.out.println(e.toString());
            return;
        }
    }

    //关闭SQL语句执行对象
    public void closeStatement(){
        try{
            stmt.close();
        }
        catch(SQLException e){
            System.out.println(e.toString());
            return;
        }
    }

    //断开与数据库的连接
    public void closeConnection(){
        try{
            conn.close();
        }
        catch(SQLException e){
            System.out.println(e.toString());
            return;
        }
    }

    public void getInLinks() {
        rst = read(stmt,"select url,out_links from"+ tableName+";");
        try {
            int cnt = 0;
            Map<String,String> map =new HashMap();
            while(rst.next()) {
                String s = rst.getString("out_links");
                if(s == null)
                    continue;
                map.put(rst.getString("url"),s);
            }

            for (HashMap.Entry<String, String> entry: map.entrySet()) {
                String[] out_links = entry.getValue().split("\n");
                for(String out : out_links) {
                    System.out.println(out);
                    String[] link = out.split(":",3);
                    if(link.length == 3) {
                        if(link[2].replace(" ","") != "") {
                            if(link[2].length() < 2)
                                continue;
                            link[2] = link[2].substring(2);
                            rst = read(stmt,"select id,in_links from "+tableName+" where url=\'" + link[2] + "\';");
                            if(rst == null)
                                continue;
                            String in_links = null;
                            while(rst.next()) {
                                in_links = rst.getString("in_links");

                            }
                            if(in_links == null)
                                in_links = "";
                            if(in_links.length() > 400)
                                continue;
                            in_links += entry.getKey();
                            in_links += '\n';
                            String sql = "UPDATE "+tableName+" SET "+tableName+".in_links = \'"+in_links+"\' where "+tableName+".url = \'"+link[2] + "\';";
                            System.out.println(sql);
                            update(stmt,sql);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void completeTitle() {
        completeTitle1();
        completeTitle2();
        completeTitle3();
        completeTitle4();
        completeTitle5();
    }

    /*
    complete title according to in links;
     */
    public void completeTitle1() {
        Map<String,String> needToComplete = new HashMap<>();
        rst = read(stmt,"select url,in_links from "+tableName+" where title = \'\'");
        try {
            while(rst.next()) {
                needToComplete.put(rst.getString("url"),rst.getString("in_links"));
            }
            System.out.println(needToComplete.size());
            int cnt = 0;
            int t = 0;
            for (HashMap.Entry<String, String> entry: needToComplete.entrySet()) {
                String url = entry.getKey();
                System.out.println(url);
                String inLinksString = entry.getValue();
                boolean finalGet = false;
                if(inLinksString == null || inLinksString.replace(" ","").equals(""))
                    continue;
                String[] inLinks = inLinksString.split("\n");
                if(inLinks.length == 0 || inLinks == null)
                    continue;
                for(String inLink : inLinks) {
                    rst = read(stmt,"select out_links,a from "+tableName+" where url = \'" + inLink + "\'");
                    if(rst == null)
                        continue;
                    String outLinksString = null;
                    String aString = null;
                    while(rst.next()) {
                        outLinksString = rst.getString("out_links");
                        aString = rst.getString("a");
                    }
                    if(outLinksString == null || outLinksString.replace(" ","") == "")
                        continue;
                    if(aString == null || aString.replace(" ","") == null)
                        continue;
                    String[] outLinks = outLinksString.split("\n");
                    String[] aLinks = aString.split("\n");
                    if(outLinks.length == 0 || aLinks.length == 0)
                        continue;
                    String pos = "-1";
                    for(String outLink : outLinks) {
                        String[] outLinkSplit = outLink.split(":",3);
                        if(outLinkSplit.length < 3)
                            continue;
                        if(outLinkSplit[2].length() < 2)
                            continue;
                        outLink = outLinkSplit[2].substring(2);
                        if(outLink.toLowerCase().equals(url.toLowerCase())) {
                            pos = outLinkSplit[0];
                            break;
                        }

                    }
                    if(pos.equals("-1"))
                        continue;
                    boolean findA = false;
                    for(String aLink : aLinks) {
                        String[] aLinkSplit = aLink.split(":",2);
                        if(aLinkSplit.length < 2)
                            continue;
                        if(aLinkSplit[0].equals(pos)) {
                            System.out.println(++cnt +" : "+ url + " : " + aLinkSplit[1]);
                            String sql = "UPDATE "+tableName+" SET "+tableName+".title = \'"+aLinkSplit[1]+"\' where "+tableName+".url = \'"+url + "\';";
                            update(stmt,sql);
                            findA = true;
                            break;
                        }
                    }
                    if(findA) {
                        finalGet = true;
                        break;
                    }

                }
                if(!finalGet) {
                    System.out.println(url);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    complete title according to h2
     */
    public void completeTitle2() {
        Map<String,String> needToComplete = new HashMap<>();
        rst = read(stmt,"select url,h2 from "+tableName+" where title = \'\' and h2 <> \'\'");
        try {
            while(rst.next()) {
                needToComplete.put(rst.getString("url"),rst.getString("h2"));
            }
            System.out.println(needToComplete.size());
            int cnt = 0;
            int t = 0;
            for (HashMap.Entry<String, String> entry: needToComplete.entrySet()) {
                String url = entry.getKey();
                System.out.println(url);
                if(url.endsWith(".htm") || url.endsWith(".html")) {
                    String title = entry.getValue();
                    //System.out.println(title);
                    title = StringEscapeUtils.unescapeHtml3(title).replace("\n"," ").replace(" +"," ");
                    //System.out.println(url + " : " + title);
                    String sql = "UPDATE "+tableName+" SET "+tableName+".title = \'"+title+"\' where "+tableName+".url = \'"+url + "\';";
                    update(stmt,sql);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    complete title according to text
     */
    public void completeTitle3() {
        Map<String,String> needToComplete = new HashMap<>();
        rst = read(stmt,"select url,text from "+tableName+" where title = \'\'");
        try {
            while(rst.next()) {
                needToComplete.put(rst.getString("url"),rst.getString("text"));
            }
            System.out.println(needToComplete.size());
            int cnt = 0;
            int t = 0;
            for (HashMap.Entry<String, String> entry: needToComplete.entrySet()) {
                String url = entry.getKey();
                if (url.equals("www.env.tsinghua.edu.cn"))
                    continue;
                String title = "";
                if(url.endsWith(".htm") || url.endsWith(".html")) {
                    title = Jsoup.connect("http://"+url).ignoreHttpErrors(true).get().title();
                    if(title == null || title.replace(" ","") == "") {
                        title = entry.getValue().replace("\n"," ").replace(" +"," ");
                    }

                } else {
                    title = entry.getValue().split(" ")[0];

                }

                System.out.println(url + " : " + title);
                String sql = "UPDATE "+tableName+" SET "+tableName+".title = \'"+title+"\' where "+tableName+".url = \'"+url + "\';";
                update(stmt,sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    complete doc's title accroding to text
     */
    public void completeTitle4() {
        Map<String,String> needToComplete = new HashMap<>();
        rst = read(stmt,"select url,text from "+tableName+" where title = \'\'");
        try {
            while(rst.next()) {
                needToComplete.put(rst.getString("url"),rst.getString("text"));
            }
            System.out.println(needToComplete.size());
            int cnt = 0;
            int t = 0;
            for (HashMap.Entry<String, String> entry: needToComplete.entrySet()) {
                System.out.println(t++);
                String url = entry.getKey();

                String[] titleSplit = entry.getValue().split(" ");
                String title = "";
                if(titleSplit.length > 2) {
                    title = StringEscapeUtils.escapeHtml3(titleSplit[0] + " "+ titleSplit[1] + " " + titleSplit[2]);
                }else
                    title = StringEscapeUtils.escapeHtml3(titleSplit[0]);



                //System.out.println(url + " : " + title);
                String sql = "UPDATE "+tableName+" SET "+tableName+".title = \'"+title+"\' where "+tableName+".url = \'"+url + "\';";
                update(stmt,sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    complete title according to url
     */
    public void completeTitle5() {
        Map<String,String> needToComplete = new HashMap<>();
        rst = read(stmt,"select url,text from "+tableName+" where isnull(title)");
        try {
            while(rst.next()) {
                needToComplete.put(rst.getString("url"),rst.getString("text"));
            }
            System.out.println(needToComplete.size());
            int cnt = 0;
            int t = 0;
            for (HashMap.Entry<String, String> entry: needToComplete.entrySet()) {
                System.out.println(t++);
                String url = entry.getKey();

                String title = url.substring(url.lastIndexOf("/")).replace("\'"," ");

                System.out.println(title);


                //System.out.println(url + " : " + title);
                String sql = "UPDATE "+tableName+" SET "+tableName+".title = \'"+title+"\' where "+tableName+".url = \'"+url + "\';";
                update(stmt,sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getTableName() {
        return tableName;
    }


    public static void main(String[] args) throws SQLException {
        MySql ms = new MySql();
        ms.getConnection();
        ms.getStatement();

        ms.rst = ms.read(ms.stmt,"select id,url,text,h1,h2,title,PR from "+ms.getTableName()+"");



        File file = new File("SchoolSearch.xml");

        FileWriter fw = null;
        try {
            fw = new FileWriter(file.getAbsoluteFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(fw);

        try {
            bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            bw.write("<pics>\n" +
                    "\t<category name=\"SchoolSearch\">\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(ms.rst.next()) {
            String id = Tools.processString(ms.rst.getString("id"));
            String url = Tools.processString(ms.rst.getString("url"));
            String text = Tools.processString(ms.rst.getString("text"));
            String h1 = Tools.processString(ms.rst.getString("h1"));
            String h2 = Tools.processString(ms.rst.getString("h2"));
            String title = Tools.processString(ms.rst.getString("title"));
            double PR = ms.rst.getDouble("PR");


            try {
                bw.write("<pic id=\""+id+"\" url=\""+url+"\" title=\""+title+"\" text=\""+text+"\" h1=\""+h1+"\" h2=\""+h2+"\""+" PR=\""+ PR + "\" />\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            bw.write("</category>\n" +
                    "</pics>");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
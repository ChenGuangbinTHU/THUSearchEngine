package META;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.wltea.analyzer.lucene.IKAnalyzer;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.StringReader;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class InvertedIndex {

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String MYSQL_DB_URL = "jdbc:mysql://localhost:3306/SchoolSearch?characterEncoding=utf-8";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    private static final String MYSQL_TABLE = "SchoolWeb";

    private static final String REDIS_HOST = "127.0.0.1";
    private static final int REDIS_PORT = 6379;

    private IKAnalyzer analyzer;
    private ImageSearcher searcher;
    private Jedis jedis;

    private HashSet<String> termSet;

    String[] fields = {"h1", "h2", "text", "title"};

    InvertedIndex() {
        analyzer = new IKAnalyzer();
        searcher = new ImageSearcher("forIndex/index_school");
        searcher.loadGlobals("forIndex/global_school.txt");
        jedis = new Jedis(REDIS_HOST, REDIS_PORT);

        termSet = new HashSet<>();
    }

    private void loadMySQL() {
        try {
            Class.forName(JDBC_DRIVER);
            java.sql.Connection conn = DriverManager.getConnection(MYSQL_DB_URL, USER, PASSWORD);
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id,text FROM " + MYSQL_TABLE);

//            int count = 0;
            while (resultSet.next()) {
//                if (++count % 1000 == 0) {
//                    System.out.println(count + "   " + termSet.size());
//                }

                String text = resultSet.getString("text");
                TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(text));
                tokenStream.addAttribute(CharTermAttribute.class);
                while (tokenStream.incrementToken()) {
                    String term = tokenStream.getAttribute(CharTermAttribute.class).toString();
                    termSet.add(term);
                }
            }
//            System.out.println(termSet.size());  // 864185

            for (String term: termSet) {
                jedis.sadd("TermSet", term);
            }

            statement.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void calculateBM25() {
        Set<String> termSet = jedis.smembers("TermSet");
        System.out.println(termSet.size());

        int count = 0;
        for (String term: termSet) {
            if (++count % 100 == 0) {
                System.out.println(count);
            }

            Map<String, String> map = new HashMap<>();
            for (String field: fields) {
                HashMap<Long, Double> scoreMap = new HashMap<>();
                TopDocs results = searcher.searchQuery(term, field, 150000);
                for (ScoreDoc sDoc : results.scoreDocs) {
                    scoreMap.put(Long.valueOf(searcher.getDoc(sDoc.doc).get("id")), (double) sDoc.score);
                }
                map.put(field, new Gson().toJson(scoreMap));
//                System.out.println(new Gson().toJson(scoreMap));
            }

            jedis.hmset("TERM_" + term, map);
        }

    }

    public List getTerm(String term,String field,long doc) {
        List list =  jedis.hmget("TERM_" + term,field);
        //System.out.println(list.get(0));
        Gson gson = new Gson();
        Map<Long,Double> map = gson.fromJson((String)list.get(0),new TypeToken<Map<Long,Double>>() {}.getType());
        System.out.println(map);
        System.out.println(map.get(doc));
        return list;
    }


    private void transformMySQL2Redis() {
        try {
            Class.forName(JDBC_DRIVER);
            java.sql.Connection conn = DriverManager.getConnection(MYSQL_DB_URL, USER, PASSWORD);
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id, url, text, title, PR FROM " + MYSQL_TABLE);

            int count = 0;
            while (resultSet.next()) {
                if (++count % 1000 == 0) {
                    System.out.println(count);
                }

                Long id = resultSet.getLong("id");
                Map<String, String> map = new HashMap<>();
                map.put("url", resultSet.getString("url"));
                map.put("text", resultSet.getString("text"));
                map.put("title", resultSet.getString("title"));
                map.put("PR", resultSet.getString("PR"));

                jedis.hmset("ITEM_" + id, map);

            }
            System.out.println(count);

//            System.out.println(termSet.size());  // 864185

            statement.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }



    }


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        {
            InvertedIndex invertedIndex = new InvertedIndex();
//            invertedIndex.loadMySQL();
            invertedIndex.calculateBM25();
//            invertedIndex.getTerm("刘","title",37888);
//            invertedIndex.getTerm("刘","h1",107520);
//            invertedIndex.getTerm("刘","h2",37888);
//            invertedIndex.getTerm("刘","text",37888);

//            invertedIndex.transformMySQL2Redis();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("\nRun Time: " + ((double) (endTime - startTime)) / 1000 + "s");
    }


}

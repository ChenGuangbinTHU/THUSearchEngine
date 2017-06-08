import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class ImageSearcher {
    private IndexReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    Map avgLength;


    public ImageSearcher(String indexdir) {
        analyzer = new IKAnalyzer();
        try {

            reader = IndexReader.open(FSDirectory.open(new File(indexdir)));
            searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new SimpleSimilarity());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TopDocs searchQuery(String queryString, String field, int maxnum) {
        try {
            Term term = new Term(field, queryString);
            Query query = new SimpleQuery(term, (float)avgLength.get(field));
            query.setBoost(1.0f);
            //Weight w=searcher.createNormalizedWeight(query);
            //System.out.println(w.getClass());

            System.out.println(query);

            return searcher.search(query, maxnum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Document getDoc(int docID) {
        try {
            return searcher.doc(docID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadGlobals(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            avgLength = new HashMap();
            avgLength.put("h1", Float.valueOf(reader.readLine()));
            avgLength.put("h2", Float.valueOf(reader.readLine()));
            avgLength.put("text", Float.valueOf(reader.readLine()));
            avgLength.put("title", Float.valueOf(reader.readLine()));
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ImageSearcher search = new ImageSearcher("forIndex/index_school");
        search.loadGlobals("forIndex/global_school.txt");

        TopDocs results = search.searchQuery("刘奕群", "text", 100);
        ScoreDoc[] hits = results.scoreDocs;
        for (int i = 0; i < hits.length; i++) { // output raw format
            Document doc = search.getDoc(hits[i].doc);
            System.out.println(" score=" + hits[i].score + " picPath= " + doc.get("url") + "PR:" + doc.get("PR"));
        }
    }
}

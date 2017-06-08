import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.wltea.analyzer.lucene.IKAnalyzer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Objects;


public class ImageServer extends HttpServlet {
    private Analyzer analyzer;
    public static final int PAGE_RESULT = 10;
    public static final String indexDir = "forIndex";
    public static final String picDir = "ImageSearch/";
    private String[] fields = {"h1", "text", "title"};
//    private float[] boosts = {0.5f,1.0f};
    private ImageSearcher search = null;

    public ImageServer() {
        super();
        analyzer = new IKAnalyzer();
        search = new ImageSearcher(indexDir + "/index_school");
        search.loadGlobals(indexDir + "/global_school.txt");
    }

    public ScoreDoc[] showList(ScoreDoc[] results, int page) {
        if (results == null || results.length < (page - 1) * PAGE_RESULT) {
            return null;
        }
        int start = Math.max((page - 1) * PAGE_RESULT, 0);
        int docnum = Math.min(results.length - start, PAGE_RESULT);
        ScoreDoc[] ret = new ScoreDoc[docnum];
        System.arraycopy(results, start, ret, 0, docnum);
        return ret;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        String queryString = request.getParameter("query");
        String pageString = request.getParameter("page");
        int page = 1;
        if (pageString != null) {
            page = Integer.parseInt(pageString);
        }
        ScoreDoc[] scoredDocs = new ScoreDoc[0];
        Integer[] combinedPageNum = new Integer[0];
        String[] urls = null;
        String[] titles = null;
        String[] texts = null;
        if (queryString == null) {
            System.out.println("null query");
//            request.getRequestDispatcher("/Image.jsp").forward(request, response);
        } else {
            for (String field : fields) {
                TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(queryString));
                tokenStream.addAttribute(CharTermAttribute.class);
                String[] terms = new String[queryString.length()];
                int index = 0;
                while (tokenStream.incrementToken()) {
                    String query = tokenStream.getAttribute(CharTermAttribute.class).toString();
                    System.out.println(query);
                    boolean existed = false;
                    for (int i = 0; i < index; ++i) {
                        if (terms[i].equals(query)) {
                            existed = true;
                            break;
                        }
                    }
                    if (existed) {
                        continue;
                    }
                    terms[index++] = query;
                    TopDocs results = search.searchQuery(query, field, 150);
                    System.out.println("totalHit"+results.scoreDocs.length);
                    for (ScoreDoc doc: results.scoreDocs) {
                        boolean newDoc = true;
                        for (int sdId = 0; sdId < scoredDocs.length; ++sdId) {
                            if (doc.doc == scoredDocs[sdId].doc) {

                                if (Objects.equals(field, "title")) {
                                    doc.score *= 100;
                                }

                                scoredDocs[sdId].score += doc.score;
                                combinedPageNum[sdId] += 1;
                                newDoc = false;
                                break;
                            }
                        }
                        if (newDoc) {

                            if (Objects.equals(field, "title")) {
//                                doc.score *= 100;
                            }

                            ScoreDoc[] temp = new ScoreDoc[1];
                            temp[0] = doc;
                            scoredDocs = concat(scoredDocs, temp);
                            Integer[] tempInt = new Integer[1];
                            tempInt[0] = 1;
                            combinedPageNum = concat(combinedPageNum, tempInt);
                        }
                    }
                }
            }

            for (int sdId = 0; sdId < scoredDocs.length; ++sdId) {
                scoredDocs[sdId].score *= combinedPageNum[sdId];
//                while (combinedPageNum[sdId] > 0) {
//                    scoredDocs[sdId].score *= 2;
//                    combinedPageNum[sdId] -= 1;
//                }
            }

            int pageNum = scoredDocs.length;
            if (scoredDocs.length > 0) {
                for (int i = 0; i < scoredDocs.length - 1; ++i) {
                    for (int j = i + 1; j < scoredDocs.length; ++j) {
                        if (scoredDocs[i].score < scoredDocs[j].score) {
                            ScoreDoc temp = scoredDocs[i];
                            scoredDocs[i] = scoredDocs[j];
                            scoredDocs[j] = temp;
                        }
                    }
                }
                scoredDocs = showList(scoredDocs, page);
                if (scoredDocs != null) {
                    urls = new String[scoredDocs.length];
                    texts = new String[scoredDocs.length];
                    titles = new String[scoredDocs.length];
                    for (int i = 0; i < scoredDocs.length && i < PAGE_RESULT; ++i) {
                        Document doc = search.getDoc(scoredDocs[i].doc);
                        urls[i] = doc.get("url");
                        texts[i] = doc.get("text");
                        titles[i] = doc.get("title");
                        System.out.println("doc=" + scoredDocs[i].doc + " score=" + scoredDocs[i].score + " title=" + titles[i] + " urls=" + urls[i]);
                    }
                } else {
                    System.out.println("page null");
                }
            } else {
                System.out.println("result null");
            }

            request.setAttribute("currentQuery", queryString);
            request.setAttribute("currentPage", page);
            request.setAttribute("urls", urls);
            request.setAttribute("texts", texts);
            request.setAttribute("titles",titles);
            request.setAttribute("pageNum", (pageNum + 9) / 10);
            request.getRequestDispatcher("/imageshow.jsp").forward(request, response);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.doGet(request, response);
    }

    private static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /*
    public void test()
            throws ServletException, IOException {
        String queryString = "刘奕群";
        String pageString ="1";
        int page = 1;
        page = Integer.parseInt(pageString);
        ScoreDoc[] scoredDocs = new ScoreDoc[0];
        Integer[] combindPageNum = new Integer[0];
        String[] urls = null;
        String[] texts = null;
        String[] tags = null;
        for (String field : fields) {
//                System.out.println(queryString);
//                System.out.println(URLDecoder.decode(queryString, "utf-8"));
//                System.out.println(URLDecoder.decode(queryString, "gb2312"));

            TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(queryString));
            tokenStream.addAttribute(CharTermAttribute.class);
            String[] terms = new String[queryString.length()];
            int index = 0;
            while (tokenStream.incrementToken()) {
                String query = tokenStream.getAttribute(CharTermAttribute.class).toString();
                System.out.println(query);
                boolean existed = false;
                for (int i = 0; i < index; ++i) {
                    if (terms[i].equals(query)) {
                        existed = true;
                        break;
                    }
                }
                if (existed) {
                    continue;
                }
                terms[index++] = query;
                TopDocs results = search.searchQuery(query, field, 100000);
                System.out.println("totalHit"+results.scoreDocs.length);
                for (ScoreDoc doc: results.scoreDocs) {
                    boolean newDoc = true;
                    for (int sdId = 0; sdId < scoredDocs.length; ++sdId) {
                        if (doc.doc == scoredDocs[sdId].doc) {
                            scoredDocs[sdId].score += doc.score;
                            combindPageNum[sdId] += 1;
                            newDoc = false;
                            break;
                        }
                    }

//                    for (ScoreDoc sDoc: scoredDocs) {
//                        if (doc.doc == sDoc.doc) {
//                            sDoc.score += doc.score;
//                            System.out.println("Merging................." + sDoc.score + "    " + doc.doc + "    " + search.getDoc(doc.doc).get("url"));
                            //System.out.println(search.getDoc(sDoc.doc).get("url") + " " + sDoc.score);
//                            newDoc = false;
//                            break;
//                        }
//                    }
                    if (newDoc) {
                        ScoreDoc[] temp = new ScoreDoc[1];
                        temp[0] = doc;
                        scoredDocs = concat(scoredDocs, temp);
                        Integer[] tempInt = new Integer[1];
                        tempInt[0] = 1;
                        combindPageNum = concat(combindPageNum, tempInt);
                    }
                }
            }
        }

        for (int sdId = 0; sdId < scoredDocs.length; ++sdId) {
            combindPageNum[sdId] = combindPageNum[sdId] * combindPageNum[sdId] * combindPageNum[sdId];
            while (combindPageNum[sdId] > 0) {
                scoredDocs[sdId].score *= 10;
                combindPageNum[sdId] -= 1;
            }
        }

        if (scoredDocs.length > 0) {
            for (int i = 0; i < scoredDocs.length - 1; ++i) {
                for (int j = i + 1; j < scoredDocs.length; ++j) {
                    if (scoredDocs[i].score < scoredDocs[j].score) {
                        ScoreDoc temp = scoredDocs[i];
                        scoredDocs[i] = scoredDocs[j];
                        scoredDocs[j] = temp;
                    }
                }
            }
            scoredDocs = showList(scoredDocs, page);
            if (scoredDocs != null) {
                tags = new String[scoredDocs.length];
                urls = new String[scoredDocs.length];
                texts = new String[scoredDocs.length];
                for (int i = 0; i < scoredDocs.length && i < PAGE_RESULT; ++i) {
                    Document doc = search.getDoc(scoredDocs[i].doc);
                    System.out.println("doc=" + scoredDocs[i].doc + " score=" + scoredDocs[i].score + " picPath= " + doc.get("picPath") + " tag= " + doc.get("abstract"));
                    tags[i] = doc.get("tags");
                    texts[i] = doc.get("text");
                    urls[i] = doc.get("url");
                    System.out.println(urls[i]+ "  PR:" + doc.get("PR")) ;
                    System.out.println();
                }
            } else {
                System.out.println("page null");
            }
        } else {
            System.out.println("result null");
        }
    }
    */

    public static void main(String[] args) throws ServletException, IOException {
//        ImageServer i = new ImageServer();
//        i.test();
    }

}

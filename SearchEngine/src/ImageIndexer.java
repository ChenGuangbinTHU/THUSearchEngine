import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wltea.analyzer.lucene.IKAnalyzer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


public class ImageIndexer {
	private Analyzer analyzer; 
    private IndexWriter indexWriter;
    private float aveURLen = 1.0f;
    private float aveTextLen = 1.0f;
    private float aveH1Len = 1.0f;
    private float aveH2Len = 1.0f;
    private float aveTitleLen = 1.0f;

    public ImageIndexer(String indexDir){
    	analyzer = new IKAnalyzer();
    	try{
    		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, analyzer);
    		Directory dir = FSDirectory.open(new File(indexDir));
    		indexWriter = new IndexWriter(dir,iwc);
    		indexWriter.setSimilarity(new SimpleSimilarity());
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }


    public void saveGlobals(String filename){
    	try{
    		PrintWriter pw=new PrintWriter(new File(filename));
			pw.println(aveH1Len);
			pw.println(aveH2Len);
			pw.println(aveTextLen);
			pw.println(aveTitleLen);
			pw.println(aveURLen);
    		pw.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
	
	/** 
	 * <p>
	 * index sogou.xml 
	 * 
	 */
	public void indexSpecialFile(String filename){
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();   
			DocumentBuilder db = dbf.newDocumentBuilder();    
			org.w3c.dom.Document doc = db.parse(new File(filename));
			NodeList nodeList = doc.getElementsByTagName("pic");
			for(int i=0;i<nodeList.getLength();i++){
				Node node=nodeList.item(i);
				NamedNodeMap map=node.getAttributes();
				Node id=map.getNamedItem("id");
				Node url=map.getNamedItem("url");
				Node text=map.getNamedItem("text");
				Node h1=map.getNamedItem("h1");
				Node h2  = map.getNamedItem("h2");
				Node title = map.getNamedItem("title");
				Node PRs = map.getNamedItem("PR");
				aveURLen += url.getNodeValue().length();
				aveTextLen += text.getNodeValue().length();
				aveH1Len += h1.getNodeValue().length();
				aveH2Len += h2.getNodeValue().length();
				aveTitleLen += title.getNodeValue().length();
				//String absString=bigClass.getNodeValue()+" "+smallClass.getNodeValue()+" "+query.getNodeValue();
				Document document  =   new  Document();
				document.setBoost(Float.valueOf(PRs.getNodeValue()));
				Field idField  =   new  Field( "id" ,id.getNodeValue(),Field.Store.YES, Field.Index.NO);
				Field urlField  =   new  Field( "url" ,url.getNodeValue(),Field.Store.YES, Field.Index.ANALYZED);
				Field textField = new Field( "text" ,text.getNodeValue(),Field.Store.YES, Field.Index.ANALYZED);
				Field h1Field = new Field( "h1" ,h1.getNodeValue(),Field.Store.YES, Field.Index.ANALYZED);
				Field h2Field = new Field( "h2" ,h2.getNodeValue(),Field.Store.YES, Field.Index.ANALYZED);
				Field PRField = new Field( "PR" ,PRs.getNodeValue(),Field.Store.YES, Field.Index.ANALYZED);
				Field titleField = new Field( "title" ,title.getNodeValue(),Field.Store.YES, Field.Index.ANALYZED);
				titleField.setBoost(1000000);
				h1Field.setBoost(1000000);
				h2Field.setBoost(100);
				textField.setBoost(1);
				//averageLength += absString.length();
				//tagsField.setBoost(10.0f);
//				if(text.getNodeValue().contains("刘奕群")) {
//					System.out.println("yes!");
//					document.setBoost(1000000000f);
//				} else {
//					//System.out.println("no");
//					document.setBoost(0.000000001f);
//				}
//				urlField.setBoost(5.0f);
//				textField.setBoost(2.0f);
				document.add(idField);
				document.add(urlField);
				document.add(textField);
				document.add(h1Field);
				document.add(h2Field);
				document.add(PRField);
				document.add(titleField);
				indexWriter.addDocument(document);
				if (i % 10000 == 0) {
					System.out.println("process " + i);
				}
				//TODO: add other fields such as html title or html content 

			}
			//averageLength /= indexWriter.numDocs();
			aveH1Len /= indexWriter.numDocs();
			aveTextLen /= indexWriter.numDocs();
			aveURLen /= indexWriter.numDocs();
			aveH2Len /= indexWriter.numDocs();
			aveTitleLen /= indexWriter.numDocs();
			System.out.println("h1:" + aveH1Len);
            System.out.println("h2:" + aveH2Len);
			System.out.println("text:" + aveTextLen);
			System.out.println("title:" + aveTitleLen);
            System.out.println("urls:" + aveURLen);
			//System.out.println("average length = "+averageLength);
			System.out.println("total "+indexWriter.numDocs()+" documents");
			indexWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ImageIndexer indexer=new ImageIndexer("forIndex/index_school");
		indexer.indexSpecialFile("input/SchoolSearch.xml");
		indexer.saveGlobals("forIndex/global_school.txt");
	}
}

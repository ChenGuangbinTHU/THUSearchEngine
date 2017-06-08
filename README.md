# THUSearchEngine
A search engine for news.tsinghua.edu.cn

## lib module
java 1.8.0
lucene 3.5
gson 2.8.1
pdfbox-2.0.6
jsoup 1.10.2
mysql-connector-java-5.1.39
poi-3.9

## direction for user
run WebPreprocess/process.java to preprocess html,pdf and docx and save them to mysql database;
config server(e.g. Tomcat) with SchoolSearch/ and run server.

# THUSearchEngine
A search engine for news.tsinghua.edu.cn

## lib module
java 1.8.0\n
lucene 3.5\n
gson 2.8.1\n
pdfbox-2.0.6\n
jsoup 1.10.2\n
mysql-connector-java-5.1.39\n
poi-3.9\n

## direction for user
run WebPreprocess/process.java to preprocess html,pdf and docx and save them to mysql database\n
config server(e.g. Tomcat) with SchoolSearch/ and run server.

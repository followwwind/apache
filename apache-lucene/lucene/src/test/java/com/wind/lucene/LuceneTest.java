package com.wind.lucene;

import com.wind.entity.Article;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * @author wind
 */
public class LuceneTest {

    private String datadir = "data";

    private Directory dir = null;

    private Analyzer analyzer = new StandardAnalyzer();

    private List<Article> articleList = new ArrayList<>();

    @Before
    public void init(){

        if(dir == null){
            try {
                dir = FSDirectory.open(Paths.get(datadir));

                for(int i = 0; i < 10; i++){
                    Article article = new Article();
                    long createTime = System.currentTimeMillis();
                    article.setId(i);
                    article.setTitle("titile" + i);
                    article.setAuthor("wind" + i);
                    article.setContent("content" + i);
                    article.setCreateTime(createTime);
                    article.setUpdateTime(createTime);
                    articleList.add(article);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * 创建索引
     *  1.创建Directory 索引存放目录
     *  2.创建分词器
     *  3.创建IndexWriterConfig
     *  4.创建IndexWriter
     */
    @Test
    public void testIndex(){

        IndexWriter iw = null;
        try {
            // To store an index on disk, use this instead:
            //Directory dir = FSDirectory.open(Paths.get(datadir));
            // Store the index in memory:
            //Directory  directory = new RAMDirectory();

            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iw = new IndexWriter(dir, iwc);

            List<Document> documents = new ArrayList<>();

            articleList.forEach(article -> {
                Document doc = new Document();
                doc.add(new IntPoint("id", article.getId()));
                doc.add(new StringField("title", article.getTitle(), Field.Store.YES));
                doc.add(new LongPoint("createTime", article.getCreateTime()));
                // 对于内容只索引不存储
                doc.add(new TextField("author", article.getAuthor(), Field.Store.NO));
                doc.add(new Field("content", article.getContent(), TextField.TYPE_STORED));
                documents.add(doc);
            });

            iw.addDocuments(documents);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(iw != null){
                try {
                    iw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testIndexReader(){

        try {
            IndexReader ir = DirectoryReader.open(dir);

            System.out.println("max num:" + ir.maxDoc());
            System.out.println("index num:" + ir.numDocs());
            // 删除了的索引数 4.X版本后取消了恢复删除
            System.out.println("delete index num:" + ir.numDeletedDocs());

            ir.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 若要查询所有的文档，则新增索引的时候就给每一个文档加一个特殊的标记(如:"★"，或者tableName="user")，
     * 查询"★/user"就可以查询到所有的信息
     * 查询"★/user"就可以查询到所有的信息
     * 根据条件查找索引
     */
    @Test
    public void queryIndex() {
        try {
            IndexReader ir = DirectoryReader.open(dir);
            // Now search the index:
            DirectoryReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);
            // Parse a simple query that searches for "text":
            QueryParser parser = new QueryParser("content", analyzer);
            Query query = parser.parse("content0");
            int count = searcher.count(query);
            System.out.println(count);
            ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;

            for (int i = 0; i < hits.length; i++) {
                ScoreDoc hit = hits[i];
                Document hitDoc = searcher.doc(hit.doc);
                // 结果按照得分来排序。主要由 关键字的个数和权值来决定
                System.out.println("(" + hit.doc + "-" + hit.score + ")" +
                        "id:" + hitDoc.get("id") +
                        " title:" + hitDoc.get("title") +
                        " createTime:" + hitDoc.get("createTime") +
                        " content:" + hitDoc.get("content"));
            }
            ir.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    /**
     * 索引更新(Update)
     * 原理是删除查询到的document 再增加一个新创建的document
     */
    @Test
    public void updateIndex() {
        try {
            IndexWriterConfig conf = new IndexWriterConfig(analyzer);
            IndexWriter iw = new IndexWriter(dir, conf);

            Term term = new Term("id", "10");
            Document doc = new Document();
            doc.add(new IntPoint("id", 10));
            StringField title = new StringField("title", "title", Field.Store.YES);
            doc.add(title);
            doc.add(new LongPoint("createTime", System.currentTimeMillis()));
            // 对于内容只索引不存储
            doc.add(new TextField("content", "内容", Field.Store.NO));

            title.setStringValue("标题");
            // 更新的时候，会把原来那个索引删掉，重新生成一个索引
            iw.updateDocument(term, doc);

            iw.commit();
            iw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 索引删除(Delete)
     */
    @Test
    public void deleteIndex() {
        try {
            IndexWriterConfig conf = new IndexWriterConfig(analyzer);
            IndexWriter iw = new IndexWriter(dir, conf);

            /*Term term = new Term("id", "3");
            iw.deleteDocuments(term);*/


            /*QueryParser parse = new QueryParser("id", analyzer);
            Query query = parse.parse("1");
            iw.deleteDocuments(query);*/

            long count = iw.deleteAll();
            System.out.println(count);
            // deleteDocuments
            iw.commit();
            iw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

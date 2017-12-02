package com.wind.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lionsoul.jcseg.analyzer.JcsegAnalyzer;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;

import java.io.IOException;

/**
 * smartcn中文分词
 */
public class AnalyzerTest {

    /**
     * 要处理的文本
     */
    private String text = "2008年8月8日晚，举世瞩目的北京第二十九届奥林匹克运动会开幕式在国家体育场隆重举行。";

    private Analyzer analyzer;

    @Before
    public void before(){

    }

    @After
    public void after(){

        if(analyzer != null){
            try {
                TokenStream ts = analyzer.tokenStream("field", text);
                CharTermAttribute ch = ts.addAttribute(CharTermAttribute.class);
                ts.reset();
                int i = 0;
                while (ts.incrementToken()) {
                    i++;
                    System.out.print(ch.toString() + "\t");
                    if(i % 7 == 0){
                        System.out.println();
                    }
                }
                ts.end();
                ts.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    public void testStandard(){
        analyzer = new StandardAnalyzer();
    }


    @Test
    public void testSmartCn(){
        analyzer = new SmartChineseAnalyzer();
    }

    /**
     * Jcseg中文分词工具
     */
    @Test
    public void testJcseg(){
        analyzer = new JcsegAnalyzer(JcsegTaskConfig.COMPLEX_MODE);
    }

}

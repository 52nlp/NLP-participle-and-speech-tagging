package com.nlp1.rmwang;

public class Mainclass {
	
	public static void main(String [] args) {
		/*main部分：*/
		long startTime;
		long endTime;
		startTime= System.currentTimeMillis();    //获取开始时间
		MyDictionary dictionary=new MyDictionary("E:\\corpus.txt","E:\\dic.txt", "GBK");
		endTime= System.currentTimeMillis();    //获取结束时间
		System.out.println("建立词典用时：" + (endTime - startTime) + "ms");
		if(!myIO.isFileExist("E:\\HMMtrain.txt"))
		{
			startTime= System.currentTimeMillis();    //获取开始时间
			dictionary.buildCorpusForHMM("E:\\HMMtrain.txt");
			endTime= System.currentTimeMillis();    //获取结束时间
			System.out.println("建立HMM语料库用时：" + (endTime - startTime) + "ms");
		}
		startTime= System.currentTimeMillis();    //获取开始时间
		FMMandBMM myFmmBmm=new FMMandBMM("E:\\question.txt", "E:\\answer.txt", "Unicode", dictionary, "E:\\result",".txt");
		myFmmBmm.showfunctions();
		endTime= System.currentTimeMillis();    //获取结束时间
		System.out.println("FMMBMM用时：" + (endTime - startTime) + "ms");
		startTime= System.currentTimeMillis();    //获取开始时间
		HMMdictionary hmmdictionary=new HMMdictionary("E:\\HMMtrain.txt", "E:\\HMMdic.txt", "Unicode");
		endTime= System.currentTimeMillis();    //获取结束时间
		System.out.println("建立HMM词典用时：" + (endTime - startTime) + "ms");
		startTime= System.currentTimeMillis();    //获取开始时间
		HMM myhmm=new HMM("E:\\question.txt", "E:\\answer.txt", "Unicode", hmmdictionary, "E:\\result",".txt");
		myhmm.showfunctions();
		endTime= System.currentTimeMillis();    //获取结束时间
		System.out.println("HMM用时：" + (endTime - startTime) + "ms");
    }
}

package com.nlp2.rmwang;

public class Mainclass {
	final static String QHMM="E:\\QHMMCX.txt";
	final static String AHMM="E:\\AHMMCX.txt";
	public static void main(String [] args)
	{
		long startTime;
		long endTime;
		startTime= System.currentTimeMillis();    //获取开始时间
		HMMdictionaryforCX myHMMdictionary=new HMMdictionaryforCX("E:\\corpus.txt", "E:\\HMMCiXingDIC.txt", "GBK", "E:\\HMMcxINI.txt");
		endTime= System.currentTimeMillis();    //获取结束时间
		System.out.println("建立词典用时：" + (endTime - startTime) + "ms");
		if(!(IOforHMM.isFileExist(QHMM)&&IOforHMM.isFileExist(AHMM)))
		{
			startTime= System.currentTimeMillis();    //获取开始时间
			myHMMdictionary.corpusYCL("E:\\corpusfortest.txt",QHMM,AHMM,"GBK");
			endTime= System.currentTimeMillis();    //获取结束时间
			System.out.println("生成评测语料用时：" + (endTime - startTime) + "ms");
		}
		startTime= System.currentTimeMillis();    //获取开始时间
		HMMforCX myHmm=new HMMforCX(QHMM, AHMM, "GBK", myHMMdictionary, "E:\\CXResult", ".txt");
		myHmm.showfunctions();
		endTime= System.currentTimeMillis();    //获取结束时间
		System.out.println("词性标注用时：" + (endTime - startTime) + "ms");
	}
}

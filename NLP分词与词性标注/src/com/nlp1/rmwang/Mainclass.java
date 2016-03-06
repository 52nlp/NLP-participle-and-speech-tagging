package com.nlp1.rmwang;

public class Mainclass {
	
	public static void main(String [] args) {
		/*main���֣�*/
		long startTime;
		long endTime;
		startTime= System.currentTimeMillis();    //��ȡ��ʼʱ��
		MyDictionary dictionary=new MyDictionary("E:\\corpus.txt","E:\\dic.txt", "GBK");
		endTime= System.currentTimeMillis();    //��ȡ����ʱ��
		System.out.println("�����ʵ���ʱ��" + (endTime - startTime) + "ms");
		if(!myIO.isFileExist("E:\\HMMtrain.txt"))
		{
			startTime= System.currentTimeMillis();    //��ȡ��ʼʱ��
			dictionary.buildCorpusForHMM("E:\\HMMtrain.txt");
			endTime= System.currentTimeMillis();    //��ȡ����ʱ��
			System.out.println("����HMM���Ͽ���ʱ��" + (endTime - startTime) + "ms");
		}
		startTime= System.currentTimeMillis();    //��ȡ��ʼʱ��
		FMMandBMM myFmmBmm=new FMMandBMM("E:\\question.txt", "E:\\answer.txt", "Unicode", dictionary, "E:\\result",".txt");
		myFmmBmm.showfunctions();
		endTime= System.currentTimeMillis();    //��ȡ����ʱ��
		System.out.println("FMMBMM��ʱ��" + (endTime - startTime) + "ms");
		startTime= System.currentTimeMillis();    //��ȡ��ʼʱ��
		HMMdictionary hmmdictionary=new HMMdictionary("E:\\HMMtrain.txt", "E:\\HMMdic.txt", "Unicode");
		endTime= System.currentTimeMillis();    //��ȡ����ʱ��
		System.out.println("����HMM�ʵ���ʱ��" + (endTime - startTime) + "ms");
		startTime= System.currentTimeMillis();    //��ȡ��ʼʱ��
		HMM myhmm=new HMM("E:\\question.txt", "E:\\answer.txt", "Unicode", hmmdictionary, "E:\\result",".txt");
		myhmm.showfunctions();
		endTime= System.currentTimeMillis();    //��ȡ����ʱ��
		System.out.println("HMM��ʱ��" + (endTime - startTime) + "ms");
    }
}

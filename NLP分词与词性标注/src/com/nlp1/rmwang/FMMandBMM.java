package com.nlp1.rmwang;

public class FMMandBMM {
	int sentencenum;//句子数量
	final MyDictionary dictionary;
	String testQTxtSrcString;//测试文件路径
	String testATxtSrcString;//测试答案路径
	String encodingString;//文件编码格式
	String resultsavepathString;//结果存储路径
	String filetype;//结果文件扩展名
	AlgorithmEvaluation myAlgorithmEvaluation;
	myIO myIOforfmmbmm;
	public void showfunctions()//展示BMMFMM功能
	{
		Calculate(0);
		Calculate(1);
	}
	public FMMandBMM(String questionSrc,String answerSrc,String encoding,MyDictionary dic,String rsltsavepath,String filetp)//构造
	{
		testQTxtSrcString=questionSrc;
		testATxtSrcString=answerSrc;
		encodingString=encoding;
		dictionary=dic;
		resultsavepathString=rsltsavepath;
		filetype=filetp;
		myAlgorithmEvaluation=new AlgorithmEvaluation();
		myIOforfmmbmm=new myIO(2);
	}
	public void Calculate(int choose)//choose=0 FMM;choose=1 BMM 算法总体框架
	{
		myIOforfmmbmm.startRead(testQTxtSrcString, encodingString, 0);//for q
		myIOforfmmbmm.startRead(testATxtSrcString, encodingString, 1);//for a
		StringBuffer saveBuffer=new StringBuffer("");
		String tmpstoreString;
		String qString="",aString="";
		if(choose==0)
		{
			while((qString=myIOforfmmbmm.readOneSentence(0))!=null)
			{
				aString=myIOforfmmbmm.readOneSentence(1);
				tmpstoreString=oneSentencedivideByFMM(qString);
				myAlgorithmEvaluation.oneSentenceMatch(tmpstoreString, aString);
				saveBuffer.append(tmpstoreString);
				saveBuffer.append("\r\n");
			}
			myIOforfmmbmm.startWrite(resultsavepathString+"byFMM"+filetype, encodingString, 0);
			myIOforfmmbmm.writeStringBufferIntoTXT(saveBuffer, 0);
			System.out.println("FMM");
		}
		else {
			while((qString=myIOforfmmbmm.readOneSentence(0))!=null)
			{
				aString=myIOforfmmbmm.readOneSentence(1);
				tmpstoreString=oneSentencedivideByBMM(qString);
				myAlgorithmEvaluation.oneSentenceMatch(tmpstoreString, aString);
				saveBuffer.append(tmpstoreString);
				saveBuffer.append("\r\n");
			}
			myIOforfmmbmm.startWrite(resultsavepathString+"byBMM"+filetype, encodingString, 0);
			myIOforfmmbmm.writeStringBufferIntoTXT(saveBuffer, 0);
			System.out.println("BMM");
		}
		myAlgorithmEvaluation.cal_Evaluation();
		myAlgorithmEvaluation.printEvaluation();
		myIOforfmmbmm.endRead(0);
		myIOforfmmbmm.endRead(1);
		myIOforfmmbmm.endWrite(0);
	}
	public String oneSentencedivideByFMM(String q)//一个句子的切分,依据FMM
	{
		String resultString="";
		int maxlen=dictionary.maxwordlenght;
		int len=q.length();
		int i=0;
		int possiblesize;
		int j;
		while(i<len)
		{
			possiblesize=Math.min(len-i,maxlen );
			for(j=i+possiblesize;j>i;j--)
			{
				String tmptest=q.substring(i,j);
				if(dictionary.find(tmptest)||j-i==1)//如果该词存在于字典中
				{
					resultString+=tmptest;
					if(j<len) resultString+="  ";
					break;
				}
			}
			i=j;
		}
		return resultString;
	}
	public String oneSentencedivideByBMM(String q)//一个句子的切分，依据FMM
	{
		String resultString="";
		int maxlen=dictionary.maxwordlenght;
		int len=q.length();
		int i=len;
		int possiblesize;
		int j;
		while(i>0)
		{
			possiblesize=Math.min(i,maxlen );
			for(j=i-possiblesize;i>j;j++)
			{
				String tmptest=q.substring(j,i);
				if(dictionary.find(tmptest)||i-j==1)//如果该词存在于字典中
				{
					resultString=tmptest+resultString;
					if(j>0) resultString="  "+resultString;
					break;
				}
			}
			i=j;
		}
		return resultString;
	}
}

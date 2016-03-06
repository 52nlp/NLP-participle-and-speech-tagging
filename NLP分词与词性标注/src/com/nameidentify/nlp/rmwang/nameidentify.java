package com.nameidentify.nlp.rmwang;
import com.nlp1.rmwang.*;
import com.nlp1.rmwang.AlgorithmEvaluation;
import com.nlp2.rmwang.*;
public class nameidentify {
	final static String QHMM="E:\\resultbyFMM.txt";//分词结果路径
	final static String answer="E:\\answer.txt";//答案路径
	final static String cxfile="E:\\CXResultcalrstbyHMM.txt";//分词结果词性标注路径
	final static String fmmorgfile="E:\\resultbyFMM.txt";//分词结果路径
	final static String RESULTPATH_STRING="E:\\resultofnameidentify.txt";//人名识别结果存储路径
	static int sentencenum=0;
	public static void main(String [] args){
		long startTime;
		long endTime;
		startTime= System.currentTimeMillis();    //获取开始时间
		nameidentifybaseCX();
		endTime= System.currentTimeMillis();    //获取结束时间
		System.out.println("句子数："+sentencenum+"人名识别用时：" + (endTime - startTime) + "ms");
	}
	public static void nameidentifybaseCX()//基于词性标注的人名识别
	{
		myIO myiofornameidentifyIo=new myIO(3);
		if(!myIO.isFileExist(cxfile))
		{
			HMMdictionaryforCX myHmMdictionaryforCX=new HMMdictionaryforCX("E:\\corpus.txt", "E:\\HMMCiXingDIC.txt", "GBK", "E:\\HMMcxINI.txt");
			HMMforCX myHmm=new HMMforCX(QHMM, answer, "Unicode", myHmMdictionaryforCX, "E:\\CXResult", ".txt");
			myHmm.HMMcal();
		}
		myiofornameidentifyIo.startRead(fmmorgfile, "Unicode", 0);
		myiofornameidentifyIo.startRead(cxfile, "Unicode", 1);
		myiofornameidentifyIo.startRead(answer, "Unicode", 2);
		myiofornameidentifyIo.startWrite(RESULTPATH_STRING, "Unicode", 0);
		String fmmString="",cxString="",aString;
		AlgorithmEvaluation myAlgorithmEvaluation=new AlgorithmEvaluation();
		StringBuffer mybuffer=new StringBuffer(10000);
		while((fmmString=myiofornameidentifyIo.readOneSentence(0))!=null)
		{
			sentencenum++;
			int wordcounter=0;
			cxString=myiofornameidentifyIo.readOneSentence(1);
			aString=myiofornameidentifyIo.readOneSentence(2);
			int i=0;
			int len;
			len=fmmString.length();
			String newresult="";
			int start=i;
			char lastcx='J';
			while(i<len)
			{
				start=i;
				while(i<len&&fmmString.charAt(i)!=' ')
				{
					i++;
				}
				try {
					if(!((wordcounter==0||cxString.charAt(wordcounter)=='J')&&lastcx=='J'))
					{
						newresult+="  ";
					}
				} catch (java.lang.StringIndexOutOfBoundsException e) {
					// TODO: handle exception
				}
				lastcx=cxString.charAt(wordcounter);
				newresult+=fmmString.substring(start,i);
				wordcounter++;
				while(i<len&&fmmString.charAt(i)==' ')
				{
					i++;
				}
			}
			myAlgorithmEvaluation.oneSentenceMatch(newresult, aString);
			mybuffer.append(newresult+"\r\n");
		}
		myiofornameidentifyIo.endRead(0);
		myiofornameidentifyIo.endRead(1);
		myiofornameidentifyIo.endRead(2);
		myiofornameidentifyIo.writeStringBufferIntoTXT(mybuffer, 0);
		myiofornameidentifyIo.endWrite(0);
		myAlgorithmEvaluation.cal_Evaluation();
		myAlgorithmEvaluation.printEvaluation();
	}
}

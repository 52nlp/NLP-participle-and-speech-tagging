package com.nlp2.rmwang;

public class AlgorithmEvaluation {
	float rightRate;//正确率
	float callbackRate;//召回率，弃用
	int rightnum;//正确数
	int answernum;//答案切分数，弃用
	int resultnum;//总词数
	public AlgorithmEvaluation()
	{
		rightRate=0;
		callbackRate=0;
		rightnum=0;
		answernum=0;
		resultnum=0;
	}
	public void cal_Evaluation()//计算正确率
	{
		rightRate=(float)rightnum/(float)resultnum;
	}
	public void printEvaluation()//输出正确率
	{
		System.out.println("正确率："+rightRate);
	}
 	public void oneSentenceMatch(int a[],int expa[])//一个句子的正误验证，句子存在int[]中，本项目中已不用，但保留该接口
	{
		int len=a.length;
		int i;
		if(a.length!=expa.length||a.length==0)
		{
			System.out.println("长度错误");
		}
		for(i=0;i<len;i++)
		{
			if(a[i]==expa[i])
			{
				rightnum++;
			}
			resultnum++;
		}
	}
 	public void oneSentenceMatch(String a,String expa)//单句匹配，a为结果，expa为答案
 	{
 		int len=a.length();
 		int i;
 		for(i=0;i<len;i++)
 		{
 			if(a.substring(i,i+1).equals(expa.substring(i,i+1))==true)
 				rightnum++;
 			resultnum++;
 		}
 	}

}

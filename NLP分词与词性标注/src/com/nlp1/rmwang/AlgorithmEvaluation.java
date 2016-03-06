package com.nlp1.rmwang;

public class AlgorithmEvaluation {
	float rightRate;//正确率
	float callbackRate;//召回率
	int rightnum;//正确数
	int answernum;//答案切分数
	int resultnum;//总词数
	public AlgorithmEvaluation()
	{
		rightRate=0;
		callbackRate=0;
		rightnum=0;
		answernum=0;
		resultnum=0;
	}
	public void cal_Evaluation()
	{
		rightRate=(float)rightnum/(float)resultnum;
		callbackRate=(float)rightnum/(float)answernum;
	}
	public void printEvaluation()
	{
		System.out.println("正确率："+rightRate);
		System.out.println("召回率："+callbackRate);
		System.out.println(rightnum+"  "+resultnum+"  "+answernum);
		clear();
	}
 	public void oneSentenceMatch(int a[],int expa[])//一个句子的正误验证
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
 	public void oneSentenceMatch(String a,String exp_a)//单句分词评价计数
	{
		int alen=a.length(),exp_alen=exp_a.length();
		int i=0,j=0;
		boolean lastjisspace=true;
		boolean resultnumcounterswitch=true;
		while(i<exp_alen)//外循环一次，完成答案中一个词的匹配
		{
			int start=i;
			for(;i<exp_alen;i++)
			{
				if(exp_a.charAt(i)==' ')
					break;
			}
			answernum++;
			String s=exp_a.substring(start,i);
			int len=s.length();
			int counter=0;
			int startj=0;
			while(j<alen&&counter<len)
			{
				if(a.charAt(j)!=' ')
				{	
					counter++;	
					if(counter==1)
						startj=j;
					if(resultnumcounterswitch)
					{
						resultnum++;
						resultnumcounterswitch=false;
					}
				}
				else {
					resultnumcounterswitch=true;
					//System.out.println(s);
				}
				j++;
			}
			if(lastjisspace==true&&(j>=alen||a.charAt(j)==' ')&&j-startj==len)
			{
				rightnum++;
			}
			if(j>=alen) continue;
			if(a.charAt(j)==' ')
			{
				lastjisspace=true;
			}
			else {
				lastjisspace=false;
			}
			while(i<exp_alen&&exp_a.charAt(i)==' ')
			{
				i++;
			}
		}
	}
 	public void resultAnalyze(String apath,String expapath,String encodingString)
 	{
 		myIO myIOs=new myIO(2);
 		myIOs.startRead(apath, "GBK", 0);
 		myIOs.startRead(expapath, encodingString, 1);
 		String a,expa;
 		while ((a=myIOs.readOneSentence(0))!=null) {
			expa=myIOs.readOneSentence(1);
			oneSentenceMatch(a, expa);
			System.out.println(a);
			System.out.println(expa);
		}
 		cal_Evaluation();
 		System.out.println(rightnum+"  "+resultnum+"  "+answernum);
 		printEvaluation();
 	}
 	public void clear()
 	{
 		rightnum=0;
 		resultnum=0;
 		answernum=0;
 	}
}

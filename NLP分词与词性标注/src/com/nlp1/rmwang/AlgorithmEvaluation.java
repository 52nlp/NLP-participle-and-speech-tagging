package com.nlp1.rmwang;

public class AlgorithmEvaluation {
	float rightRate;//��ȷ��
	float callbackRate;//�ٻ���
	int rightnum;//��ȷ��
	int answernum;//���з���
	int resultnum;//�ܴ���
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
		System.out.println("��ȷ�ʣ�"+rightRate);
		System.out.println("�ٻ��ʣ�"+callbackRate);
		System.out.println(rightnum+"  "+resultnum+"  "+answernum);
		clear();
	}
 	public void oneSentenceMatch(int a[],int expa[])//һ�����ӵ�������֤
	{
		int len=a.length;
		int i;
		if(a.length!=expa.length||a.length==0)
		{
			System.out.println("���ȴ���");
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
 	public void oneSentenceMatch(String a,String exp_a)//����ִ����ۼ���
	{
		int alen=a.length(),exp_alen=exp_a.length();
		int i=0,j=0;
		boolean lastjisspace=true;
		boolean resultnumcounterswitch=true;
		while(i<exp_alen)//��ѭ��һ�Σ���ɴ���һ���ʵ�ƥ��
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

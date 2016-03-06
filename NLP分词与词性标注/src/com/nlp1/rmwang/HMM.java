package com.nlp1.rmwang;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class HMM {
	float rightRate;//��ȷ��
	float callbackRate;//�ٻ���
	int sentencenum;//��������
	int rightnum;//��ȷ�з���
	int answernum;//���з���
	int resultnum;//����з���
	int compromisednum;//��Ϊ���Ͽⲻ��������µĺ���EmitRobEmitRobMatrixֵ���зִʵľ��������������ľ������ǳ�Ϊcompromised sentence
	final HMMdictionary dictionary;
	InputStreamReader qread,aread;
	BufferedReader qbufread,abufread;
	String testQTxtSrcString;//�����ļ�·��
	String testATxtSrcString;//���Դ�·��
	String encodingString;//�����ʽ
	String resultsavepathString;//����洢·��
	String filetype;//����ļ���չ��
	public HMM(String questionSrc,String answerSrc,String encoding,HMMdictionary dic,String rsltsavepath,String filetp)//����
	{
		testQTxtSrcString=questionSrc;
		testATxtSrcString=answerSrc;
		encodingString=encoding;
		dictionary=dic;
		resultsavepathString=rsltsavepath;
		rightnum=0;
		answernum=0;
		resultnum=0;
		filetype=filetp;
		compromisednum=0;
		sentencenum=0;
	}
	public void oneSentenceMatch(String a,String exp_a)//һ�����ӵ�������֤
	{
		int alen=a.length(),exp_alen=exp_a.length();
		int i=0,j=0;
		boolean lastjisspace=true;
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
	public void writeintoTXT(StringBuffer buf,String path)//�����д��txt
	{
		File save=new File(path);
		try {
			if(save.exists()) save.delete();
			save.createNewFile();
			FileOutputStream outputStream=new FileOutputStream(save);
			OutputStreamWriter writer=new OutputStreamWriter(outputStream,encodingString);
			BufferedWriter bufwriter=new BufferedWriter(writer);
			bufwriter.write(buf.toString());
			bufwriter.close();
			writer.close();
			outputStream.close();
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
	}
	public String onSentencedivideByHMM(String sen)//����HMM�з�
	{
		int len=sen.length();
		double weight[][];
		int path[][];
		weight=new double[4][len];
		path=new int[4][len];
		String s=sen.substring(0,1);
		int i,j,k;
		weight[0][0]=dictionary.getInitStatusValue(0)+dictionary.getEmitRobMatrixValue(0, s);
		weight[1][0]=dictionary.getInitStatusValue(1)+dictionary.getEmitRobMatrixValue(1, s);
		weight[2][0]=dictionary.getInitStatusValue(2)+dictionary.getEmitRobMatrixValue(2, s);
		weight[3][0]=dictionary.getInitStatusValue(3)+dictionary.getEmitRobMatrixValue(3, s);
		if(!(weight[0][0]>Math.log(0)||weight[3][0]>Math.log(0)))
		{
			weight[0][0]=0;
			weight[3][0]=0;
		}
		path[0][0]=-1;
		path[1][0]=-1;
		path[2][0]=-1;
		path[3][0]=-1;
		boolean compromisecounterswitch=true;
		//�������ӣ��±�i��1��ʼ����Ϊ�ղų�ʼ����ʱ���Ѿ���0��ʼ��������
		for(i = 1; i < len; i++)
		{
		    // �������ܵ�״̬
			boolean ischanged=false;
		    for(j = 0; j < 4; j++) 
		    {
		        weight[j][i] = Math.log(0);
		        path[j][i] = -1;
		        
		        //����ǰһ���ֿ��ܵ�״̬
		        for(k = 0; k < 4; k++)
		        {
		            double tmp = weight[k][i-1] + dictionary.getTransProbMatrixValue(k, j)+ dictionary.getEmitRobMatrixValue(j, sen.substring(i,i+1));
		            if(tmp > weight[j][i]) // �ҳ�����weight[j][i]ֵ
		            {
		                weight[j][i] = tmp;
		                path[j][i] = k;
		                ischanged=true;
		            }
		        }
		        
		    }
		    if(ischanged==false)//HMM��δ������ֵ������compromise����
	        {
		    	for(j = 0; j < 4; j++)
		    	{
		    		for(k = 0; k < 4; k++)
			        {
			            double tmp = weight[k][i-1] + dictionary.getTransProbMatrixValue(k, j);
			            if(tmp > weight[j][i]) // �ҳ�����weight[j][i]ֵ
			            {
			                weight[j][i] = tmp;
			                path[j][i] = k;
			                ischanged=true;
			            }
			        }
		    	}
	        	if(compromisecounterswitch)//ͳ��compromised sentence������
	        	{
	        		compromisednum++;
	        		compromisecounterswitch=false;
	        	}
	        }
		    if(ischanged==false)
		    {
		    	System.out.println("HMMת�ƾ�������ȱʧ��������������Ͽ�ѵ��");
		    }
		}
		//���ݣ�
		String resultString="";
		int thiswordtype;
		if(weight[1][len-1]>weight[3][len-1])//E��β
		{
			thiswordtype=1;
		}
		else{//S��β
			thiswordtype=3;
		}
		int x;
		for(x=len-1;x>=0;x--)//�Ծ��ӻ��֣��������ͷ��β�Ŀո�
		{
			switch (thiswordtype) {
			case 0://B
				resultString="  "+sen.substring(x,x+1)+resultString;
				resultnum++;
				break;
			case 1://E
				resultString=sen.substring(x,x+1)+"  "+resultString;
				break;
			case 2://M
				resultString=sen.substring(x,x+1)+resultString;
				break;
			case 3://S
				resultString="  "+sen.substring(x,x+1)+"  "+resultString;
				resultnum++;
				break;
			default:System.out.println("�����쳣����"+thiswordtype+"  "+x);
				break;
			}
			thiswordtype=path[thiswordtype][x];
		}
		resultString=resultString.substring(2,resultString.length()-2);
		return resultString;
	}
	public void showfunctions()//����չʾ
	{
		HMManalyze();
	}
	public void HMManalyze()//HMM�ִ��ܿ��
	{
		File qfile=new File(testQTxtSrcString);
		File afile=new File(testATxtSrcString);
		StringBuffer saveBuffer=new StringBuffer("");
		String tmpstoreString;
		try {
			qread=new InputStreamReader(new FileInputStream(qfile),encodingString);
			aread=new InputStreamReader(new FileInputStream(afile),encodingString);
			qbufread=new BufferedReader(qread);
			abufread=new BufferedReader(aread);
			String qString="",aString="";
			while((qString=qbufread.readLine())!=null)
			{
				sentencenum++;
				aString=abufread.readLine();
				tmpstoreString=onSentencedivideByHMM(qString);
				oneSentenceMatch(tmpstoreString, aString);
				saveBuffer.append(tmpstoreString);
				saveBuffer.append("\r\n");
			}
			writeintoTXT(saveBuffer, resultsavepathString+"byHMM"+filetype);
			System.out.println("HMM");
			rightRate=(float)rightnum/(float)resultnum;
			callbackRate=(float)rightnum/(float)answernum;
			System.out.println("��ȷ�ʣ�"+rightRate);
			System.out.println("�ٻ��ʣ�"+callbackRate);
			System.out.println(compromisednum+" sentences  compromised duo to corpus which is too simple");
			System.out.println("total sentences:"+sentencenum);
			abufread.close();
			qbufread.close();
			qread.close();
			aread.close();
		} catch (UnsupportedEncodingException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
	}
}

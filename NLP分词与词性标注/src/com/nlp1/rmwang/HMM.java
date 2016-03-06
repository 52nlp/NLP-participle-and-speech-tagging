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
	float rightRate;//正确率
	float callbackRate;//召回率
	int sentencenum;//句子数量
	int rightnum;//正确切分数
	int answernum;//答案切分数
	int resultnum;//结果切分数
	int compromisednum;//因为语料库不够大而导致的忽略EmitRobEmitRobMatrix值进行分词的句子数量，这样的句子我们称为compromised sentence
	final HMMdictionary dictionary;
	InputStreamReader qread,aread;
	BufferedReader qbufread,abufread;
	String testQTxtSrcString;//测试文件路径
	String testATxtSrcString;//测试答案路径
	String encodingString;//编码格式
	String resultsavepathString;//结果存储路径
	String filetype;//结果文件扩展名
	public HMM(String questionSrc,String answerSrc,String encoding,HMMdictionary dic,String rsltsavepath,String filetp)//构造
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
	public void oneSentenceMatch(String a,String exp_a)//一个句子的正误验证
	{
		int alen=a.length(),exp_alen=exp_a.length();
		int i=0,j=0;
		boolean lastjisspace=true;
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
	public void writeintoTXT(StringBuffer buf,String path)//将结果写入txt
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
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	public String onSentencedivideByHMM(String sen)//单句HMM切分
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
		//遍历句子，下标i从1开始是因为刚才初始化的时候已经对0初始化结束了
		for(i = 1; i < len; i++)
		{
		    // 遍历可能的状态
			boolean ischanged=false;
		    for(j = 0; j < 4; j++) 
		    {
		        weight[j][i] = Math.log(0);
		        path[j][i] = -1;
		        
		        //遍历前一个字可能的状态
		        for(k = 0; k < 4; k++)
		        {
		            double tmp = weight[k][i-1] + dictionary.getTransProbMatrixValue(k, j)+ dictionary.getEmitRobMatrixValue(j, sen.substring(i,i+1));
		            if(tmp > weight[j][i]) // 找出最大的weight[j][i]值
		            {
		                weight[j][i] = tmp;
		                path[j][i] = k;
		                ischanged=true;
		            }
		        }
		        
		    }
		    if(ischanged==false)//HMM后未曾更新值，故作compromise处理
	        {
		    	for(j = 0; j < 4; j++)
		    	{
		    		for(k = 0; k < 4; k++)
			        {
			            double tmp = weight[k][i-1] + dictionary.getTransProbMatrixValue(k, j);
			            if(tmp > weight[j][i]) // 找出最大的weight[j][i]值
			            {
			                weight[j][i] = tmp;
			                path[j][i] = k;
			                ischanged=true;
			            }
			        }
		    	}
	        	if(compromisecounterswitch)//统计compromised sentence的数量
	        	{
	        		compromisednum++;
	        		compromisecounterswitch=false;
	        	}
	        }
		    if(ischanged==false)
		    {
		    	System.out.println("HMM转移矩阵数据缺失，请更换更大语料库训练");
		    }
		}
		//回溯：
		String resultString="";
		int thiswordtype;
		if(weight[1][len-1]>weight[3][len-1])//E结尾
		{
			thiswordtype=1;
		}
		else{//S结尾
			thiswordtype=3;
		}
		int x;
		for(x=len-1;x>=0;x--)//对句子划分，多余出开头结尾的空格
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
			default:System.out.println("回溯异常错误："+thiswordtype+"  "+x);
				break;
			}
			thiswordtype=path[thiswordtype][x];
		}
		resultString=resultString.substring(2,resultString.length()-2);
		return resultString;
	}
	public void showfunctions()//功能展示
	{
		HMManalyze();
	}
	public void HMManalyze()//HMM分词总框架
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
			System.out.println("正确率："+rightRate);
			System.out.println("召回率："+callbackRate);
			System.out.println(compromisednum+" sentences  compromised duo to corpus which is too simple");
			System.out.println("total sentences:"+sentencenum);
			abufread.close();
			qbufread.close();
			qread.close();
			aread.close();
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
}

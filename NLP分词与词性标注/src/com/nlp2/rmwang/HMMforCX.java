package com.nlp2.rmwang;

import java.util.Vector;


public class HMMforCX {
		int CiXing_type;//词性种类，暂定10
		int sentencenum;//句子数量
		int compromisednum;//因为语料库不够大而导致的忽略EmitRobEmitRobMatrix值进行分词的句子数量，这样的句子我们称为compromised sentence
		final HMMdictionaryforCX dictionary;
		String testQTxtSrcString;//测试文件路径
		String testATxtSrcString;//测试答案路径
		String encodingString;//编码格式
		String resultsavepathString;//结果存储路径
		String filetype;//结果文件扩展名
		IOforHMM myIOforHMM;
		AlgorithmEvaluation myEvaluation;
		public HMMforCX(String questionSrc,String answerSrc,String encoding,HMMdictionaryforCX dic,String rsltsavepath,String filetp)//构造
		{
			testQTxtSrcString=questionSrc;
			testATxtSrcString=answerSrc;
			encodingString=encoding;
			dictionary=dic;
			resultsavepathString=rsltsavepath;
			filetype=filetp;
			compromisednum=0;
			sentencenum=0;
			myIOforHMM=new IOforHMM(2);
			myEvaluation=new AlgorithmEvaluation();
			CiXing_type=dic.getCXtypeNum();
		}
		private String onSentenceAnalyzeByHMM(String sen)//单句HMM切分
		{
			int i,j,k;
			int len=sen.length();
			final Vector wordVector=new Vector(200);
			//long a1,a2,a3,a4;
			
			//a1= System.currentTimeMillis(); 
			
			for(i=0;i<len;)
			{
				int x;
				for(x=i;x<len;x++)
				{
					if(sen.charAt(x)==' ')
						break;
				}
				wordVector.add(sen.substring(i,x));
				while(x<len&&sen.charAt(x)==' ')
				{
					x++;
				}
				i=x;
			}
			int size=wordVector.size();
			double weight[][];
			int path[][];
			weight=new double[CiXing_type][size];
			path=new int[CiXing_type][size];
			
			//a2= System.currentTimeMillis(); 
			
			for(i=0;i<CiXing_type;i++)
			{
				weight[i][0]=dictionary.getInitStatusValue(i)+dictionary.getEmitRobMatrixValue(i, (String)wordVector.get(0));
				path[i][0]=-1;
			}
			boolean compromisecounterswitch=true;
			//遍历句子，下标i从1开始是因为刚才初始化的时候已经对0初始化结束了
			for(i = 1; i < size; i++)
			{
			    // 遍历可能的状态
				boolean ischanged=false;
			    for(j = 0; j < CiXing_type; j++) 
			    {
			        weight[j][i] = Math.log(0);
			        path[j][i] = -1;
			        
			        //遍历前一个字可能的状态
			        for(k = 0; k < CiXing_type; k++)
			        {
			            double tmp = weight[k][i-1] + dictionary.getTransProbMatrixValue(k, j)+ dictionary.getEmitRobMatrixValue(j, (String)wordVector.get(i));
			            if(tmp > weight[j][i]) // 找出最大的weight[j][i]值
			            {
			                weight[j][i] = tmp;
			                path[j][i] = k;
			                ischanged=true;
			            }
			        }
			    }
			    if(ischanged==false)//HMM后未曾更新值，故作compromise处理,根据词性标注未知词性特点，标注为n/nr
		        {
			    	weight[dictionary.translateCX("n")][i]=0;//此词标注为n
			    	weight[dictionary.translateCX("nr")][i]=0;
			    	int x;
			    	double max=Math.log(0);
			    	int recx=-1;
			    	for(x=0;x<CiXing_type;x++)
			    	{
			    		if(weight[x][i-1]>max)
			    		{
			    			max=weight[x][i-1];
			    			recx=x;
			    			ischanged=true;
			    		}
			    	}
			    	path[dictionary.translateCX("n")][i]=recx;//这个词上一词选择pro最大的
			    	for(x=0;x<CiXing_type;x++)
			    	{
			    		if(weight[x][i-1]>max)
			    		{
			    			max=weight[x][i-1];
			    			ischanged=true;
			    			recx=x;
			    		}
			    	}
			    	path[dictionary.translateCX("nr")][i]=recx;//这个词上一词选择pro最大的
		        	if(compromisecounterswitch)//统计compromised sentence的数量
		        	{
		        		compromisednum++;
		        		compromisecounterswitch=false;
		        	}
		        }
			    if(ischanged==false)
			    {
			    	System.out.println("HMM转移矩阵数据缺失，请更换更大语料库训练");
			    	System.out.println(sen);
			    	System.out.println(i);
			    }
			}
			//回溯：
			//a3= System.currentTimeMillis(); 
			StringBuilder resultStringbuf=new StringBuilder(1000);
			int thiswordtype=-1;
			double max=Math.log(0);
			for(i=0;i<CiXing_type;i++)
			{
				if(weight[i][size-1]>max)
				{
					max=weight[i][size-1];
					thiswordtype=i;
				}
			}
			for(int x=size-1;x>=0;x--)//thiswordtype代表词性
			{
				String tString=resultStringbuf.toString();
				resultStringbuf.setLength(0);
				resultStringbuf.append((char)(thiswordtype+65)+tString);
				thiswordtype=path[thiswordtype][x];
			}
			//a4= System.currentTimeMillis(); 
			//System.out.println("建立vector用时：" + (a2 - a1));
			//System.out.println("计算用时："+(a3-a2));
			//System.out.println("回溯用时:" + (a4-a3));
			return resultStringbuf.toString();
		}
		public void showfunctions()//功能展示
		{
			HMManalyze();
		}
		private void HMManalyze()//HMM总框架
		{
			myIOforHMM.startRead(testQTxtSrcString, encodingString, 0);
			myIOforHMM.startRead(testATxtSrcString, encodingString, 1);
			StringBuilder saveBuffer=new StringBuilder(1000);
			String tmpstoreString;
			String qString="",aString="";
			while((qString=myIOforHMM.readOneSentence(0))!=null)
			{
				sentencenum++;
				if(qString.length()==0) continue;
				//a1= System.currentTimeMillis(); 
				tmpstoreString=onSentenceAnalyzeByHMM(qString);
				//a2= System.currentTimeMillis();
				//System.out.println("osa用时："+(a2-a1));
				aString=myIOforHMM.readOneSentence(1);
				saveBuffer.append(tmpstoreString+"\r\n");
				myEvaluation.oneSentenceMatch(tmpstoreString, aString);
				//a3= System.currentTimeMillis(); 
				//System.out.println("osm用时："+(a3-a2));
			}
			myIOforHMM.startWrite(resultsavepathString+"byHMM"+filetype,encodingString,0);
			myIOforHMM.writechars(saveBuffer.toString().toCharArray(),0);
			myIOforHMM.endWrite(0);
			myIOforHMM.endRead(0);
			myIOforHMM.endRead(1);
			System.out.println("HMM词性标注");
			myEvaluation.cal_Evaluation();
			myEvaluation.printEvaluation();
			System.out.println(compromisednum+" sentences  compromised duo to corpus which is too simple");
			System.out.println("total sentences:"+sentencenum);
		}
		public void match(String apath,String expapath)//比对两个结果文件，第一个参数是结果路径，第二个参数是答案路径
		{
			myIOforHMM.startRead(apath, encodingString, 0);
			myIOforHMM.startRead(expapath, encodingString, 1);
			String a,expa;
			while((a=myIOforHMM.readOneSentence(0))!=null)
			{
				expa=myIOforHMM.readOneSentence(1);
				myEvaluation.oneSentenceMatch(a, expa);
			}
			myEvaluation.cal_Evaluation();
			myEvaluation.printEvaluation();
			System.out.println(myEvaluation.resultnum);
			System.out.println(myEvaluation.rightnum);
		}
		public void HMMcal()
		{
			myIOforHMM.startRead(testQTxtSrcString, encodingString, 0);
			StringBuilder saveBuffer=new StringBuilder("");
			String tmpstoreString;
			String qString="";
			while((qString=myIOforHMM.readOneSentence(0))!=null)
			{
				sentencenum++;
				if(qString.length()==0) continue;
				tmpstoreString=onSentenceAnalyzeByHMM(qString);
				saveBuffer.append(tmpstoreString+"\r\n");
			}
			System.out.println("HMM词性标注完成");
			System.out.println(compromisednum+" sentences  compromised duo to corpus which is too simple");
			System.out.println("total sentences:"+sentencenum);
			myIOforHMM.startWrite(resultsavepathString+"calrstbyHMM"+filetype,encodingString,0);
			myIOforHMM.writechars(saveBuffer.toString().toCharArray(),0);
			myIOforHMM.endWrite(0);
			myIOforHMM.endRead(0);
			
		}
}

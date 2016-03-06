package com.nlp1.rmwang;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
class probabitity{
	double counter;
	double pro;
	public probabitity(double cnt,double p)
	{
		counter=cnt;
		pro=p;
	}
	public probabitity()
	{
		counter=1;
		pro=0;
	}
	public void inccounter()
	{
		counter+=1;
	}
	public void calpro(double total)
	{
		pro=Math.log(counter/total);
	}
}
public class HMMdictionary {
	double TransProbMatrix[][],InitStatus[];
	String OriginalfilePath;
	String dictionaryPath;
	String encodingString;
	int allsentencenum;//总句子数
	int sstartnum;//s开头总数
	int bstartnum;//b开头总数
	int alltransnum;//转移总数
	int allwordnum;//总字数，备用，目前计算不使用
	HashMap Bmap,Emap,Mmap,Smap;
	myIO myIOforHMMdic;
	static final int B=0;
	static final int E=1;
	static final int M=2;
	static final int S=3;
	public boolean isdicExist()//判断词典文件是否存在
	{
		File dicfile=new File(dictionaryPath);
		return dicfile.exists();
	}
	public HMMdictionary(String orgpath,String dicpath,String encoding)//构造
	{
		myIOforHMMdic=new myIO(2);
		OriginalfilePath=orgpath;
		dictionaryPath=dicpath;
		encodingString=encoding;
		allsentencenum=0;
		alltransnum=0;
		allwordnum=0;
		sstartnum=0;
		bstartnum=0;
		TransProbMatrix=new double[4][4];
		InitStatus=new double[4];
		int i,j;
		for(i=0;i<4;i++)
		{
			for(j=0;j<4;j++)
			{
				TransProbMatrix[i][j]=0;
			}
			InitStatus[i]=0;
		}
		if(isdicExist())
		{
			buildbyDicfile();
		}
		else {
			buildbyOrgfile();
		}
	}
	public String standardtheLineforBuildDIC(String a)//标准化读入句子，经此函数转化为本程序标准化格式，不同预料只需修改此接口
	{
		int i=0,len=a.length();
		while(i<len&&a.charAt(i)==' ')
		{
			i++;
		}
		return a.substring(i);
	}
	public void buildbyOrgfile()//依据语料库生成字典算法框架
	{
		myIOforHMMdic.startRead(OriginalfilePath, encodingString, 0);
		Bmap=new HashMap();
		Emap=new HashMap();
		Mmap=new HashMap();
		Smap=new HashMap();
		String tmpLineValString=null;
		while ((tmpLineValString=myIOforHMMdic.readOneSentence(0))!=null) {
			if(tmpLineValString.length()==0)
			{
				//System.out.println("空行");
				continue;
			}
			tmpLineValString=standardtheLineforBuildDIC(tmpLineValString);
			allsentencenum++;
			onesentenceAnalyze(tmpLineValString);
		}
		InitStatus[S]=Math.log((double)sstartnum/(double)allsentencenum);
		InitStatus[B]=Math.log((double)bstartnum/(double)allsentencenum);
		InitStatus[E]=Math.log(InitStatus[E]);
		InitStatus[M]=Math.log(InitStatus[M]);
		int i,j;
		for(i=0;i<4;i++)
		{
			for(j=0;j<4;j++)
			{
				TransProbMatrix[i][j]=Math.log(TransProbMatrix[i][j]/alltransnum);
			}
		}
		calproofEmitRobMatrix(Bmap);
		calproofEmitRobMatrix(Emap);
		calproofEmitRobMatrix(Mmap);
		calproofEmitRobMatrix(Smap);
		myIOforHMMdic.endRead(0);
		writeDicIntoTXT(dictionaryPath);
		System.out.println("词典建立完成");
	}
	public void calproofEmitRobMatrix(HashMap map)//计算EmitRobMatrix的值（概率取ln）
	{
		int size=map.size();
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			HashMap.Entry entry = (HashMap.Entry) iter.next();
			probabitity tmpp = (probabitity)entry.getValue();
			tmpp.calpro(size);//计算对数型概率
		}
	}
	public void onesentenceAnalyze(String sen)//依据语料库建立字典，单句分析
	{
		int i=0;
		int len=sen.length();
		int start=i;
		String s;
		boolean lastisS=false;
		while(i<len)
		{
			start=i;
			while(i<len&&sen.charAt(i)!=' ')
			{
				i++;
			}
			s=sen.substring(start,i);
			if(start==0)
			{
				try {
					lastisS=onewordAnalyze(s, true,lastisS);
				} catch (java.lang.StringIndexOutOfBoundsException e) {
					// TODO: handle exception
					System.out.println(sen);
					System.exit(0);
				}
				
			}
			else {
				lastisS=onewordAnalyze(s, false,lastisS);
			}
			while(i<len&&sen.charAt(i)==' ')
			{
				i++;
			}
		}
	}
	public boolean onewordAnalyze(String word,boolean isstart,boolean lastwordisS)//依据语料库建立字典，单词分析
	{
		int len=word.length();
		if(isstart==true)
		{
			if(len==1)
			{
				sstartnum++;
			}
			else {
				bstartnum++;
			}
		}
		allwordnum+=len;//更新总字数
		int i=0;
		String s=null;
		if(len>1)//非s
		{
			for(i=0;i<len;i++)
			{
				if(i==0)
				{
					//EmitRobMatrix:
					s=word.substring(i,i+1);
					if(Bmap.containsKey(s))
					{
						probabitity tmpp=(probabitity)Bmap.get(s);
						tmpp.inccounter();
						Bmap.put(s, tmpp);
					}
					else {
						Bmap.put(s, new probabitity());
					}
					//TransProbMatrix:
					if(isstart==false)
					{
						if(lastwordisS)
						{
							TransProbMatrix[S][B]++;
						}
						else {
							TransProbMatrix[E][B]++;
						}
						alltransnum++;
					}
					
				}
				else if(i==len-1){
					//EmitRobMatrix:
					s=word.substring(i,i+1);
					if(Emap.containsKey(s))
					{
						probabitity tmpp=(probabitity)Emap.get(s);
						tmpp.inccounter();
						Emap.put(s, tmpp);
					}
					else {
						Emap.put(s, new probabitity());
					}
					//TransProbMatrix:
					if(i-1==0)
					{
						TransProbMatrix[B][E]++;
					}
					else {
						TransProbMatrix[M][E]++;
					}
					alltransnum++;
				}
				else {
					//EmitRobMatrix:
					s=word.substring(i,i+1);
					if(Mmap.containsKey(s))
					{
						probabitity tmpp=(probabitity)Mmap.get(s);
						tmpp.inccounter();
						Mmap.put(s, tmpp);
					}
					else {
						Mmap.put(s, new probabitity());
					}
					//TransProbMatrix:
					if(i-1==0)
					{
						TransProbMatrix[B][M]++;
					}
					else {
						TransProbMatrix[M][M]++;
					}
					alltransnum++;
				}
			}
			return false;
		}
		else {//s
			//EmitRobMatrix:
			s=word.substring(0,1);
			if(Smap.containsKey(s))
			{
				probabitity tmpp=(probabitity)Smap.get(s);
				tmpp.inccounter();
				Smap.put(s, tmpp);
			}
			else {
				Smap.put(s, new probabitity());
			}
			//TransProbMatrix:
			if(isstart==false)
			{
				if(lastwordisS)
				{
					TransProbMatrix[S][S]++;
				}
				else {
					TransProbMatrix[E][S]++;
				}
				alltransnum++;
			}
			return true;
		}
	}
	public void buildbyDicfile()//依据字典文件建立字典
	{
		myIOforHMMdic.startRead(dictionaryPath, encodingString, 0);
		Bmap=new HashMap();
		Emap=new HashMap();
		Mmap=new HashMap();
		Smap=new HashMap();
		String tmpLineValString=null;
		String tmpproString=null;
		int i,j;
		for(i=0;i<4;i++)
		{
			tmpLineValString=myIOforHMMdic.readOneSentence(0);
			InitStatus[i]=Double.valueOf(tmpLineValString).doubleValue();
		}
		for(i=0;i<4;i++)
		{
			for(j=0;j<4;j++)
			{
				tmpLineValString=myIOforHMMdic.readOneSentence(0);
				TransProbMatrix[i][j]=Double.valueOf(tmpLineValString).doubleValue();
			}
		}
		for(i=0;i<4;i++)
		{
			tmpLineValString=myIOforHMMdic.readOneSentence(0);
			int size=Integer.valueOf(tmpLineValString).intValue();
			for(j=0;j<size;j++)
			{
				tmpLineValString=myIOforHMMdic.readOneSentence(0);
				tmpproString=myIOforHMMdic.readOneSentence(0);
				probabitity tmpp=new probabitity(0,Double.valueOf(tmpproString).doubleValue());
				switch (i) {
				case 0:Bmap.put(tmpLineValString, tmpp);break;
				case 1:Emap.put(tmpLineValString, tmpp);break;
				case 2:Mmap.put(tmpLineValString, tmpp);break;
				case 3:Smap.put(tmpLineValString, tmpp);break;
				default:
					break;
				}
			}
		}
		myIOforHMMdic.endRead(0);
		//writeDicIntoTXT("E:\\HMMdic2.txt");
		System.out.println("词典建立完成");
	}
	public void writeHashmapIntoTXT(HashMap hashmap,BufferedWriter bufwriter)//写入hashmap（写入字典子函数）
	{
		Iterator iter = hashmap.entrySet().iterator();
		try {
			bufwriter.write(hashmap.size()+"\r\n");//写入总数
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		while (iter.hasNext()) {
			HashMap.Entry entry = (HashMap.Entry) iter.next();
			String key = (String)entry.getKey();
			probabitity val = (probabitity)entry.getValue();
			try {//写入单个数据项
				bufwriter.write(key+"\r\n");
				bufwriter.write(val.pro+"\r\n");
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}
	public void writeDicIntoTXT(String path)//把字典写入txt
	{
		File save=new File(path);
		try {
			if(save.exists()) save.delete();
			save.createNewFile();
			FileOutputStream outputStream=new FileOutputStream(save);
			OutputStreamWriter writer=new OutputStreamWriter(outputStream,encodingString);
			BufferedWriter bufwriter=new BufferedWriter(writer);
			bufwriter.write(InitStatus[B]+"\r\n"+InitStatus[E]+"\r\n"+InitStatus[M]+"\r\n"+InitStatus[S]+"\r\n");
			int i,j;
			for(i=0;i<4;i++)
			{
				for(j=0;j<4;j++)
				{
					bufwriter.write(TransProbMatrix[i][j]+"\r\n");
				}
			}
			writeHashmapIntoTXT(Bmap, bufwriter);
			writeHashmapIntoTXT(Emap, bufwriter);
			writeHashmapIntoTXT(Mmap, bufwriter);
			writeHashmapIntoTXT(Smap, bufwriter);
			bufwriter.close();
			writer.close();
			outputStream.close();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	public double getEmitRobMatrixValue(int type,String word)//获取EmitRobMatrix值
	{
		if(iswordExist(word)==false) return 0;//如果是未登录的字，返回等可能的概率值
		try {
			switch (type) {
			case 0:probabitity bProbabitity=(probabitity)Bmap.get(word);if(bProbabitity==null) {return Math.log(0);}else {return bProbabitity.pro;}
			case 1:probabitity eProbabitity=(probabitity)Emap.get(word);if(eProbabitity==null) {return Math.log(0);}else {return eProbabitity.pro;}
			case 2:probabitity mProbabitity=(probabitity)Mmap.get(word);if(mProbabitity==null) {return Math.log(0);}else {return mProbabitity.pro;}
			case 3:probabitity sProbabitity=(probabitity)Smap.get(word);if(sProbabitity==null) {return Math.log(0);}else {return sProbabitity.pro;}
			default:
				break;
			}
		} catch (NullPointerException e) {
			//System.out.println(word+"  "+type);
			// TODO: handle exception
		}
		
		return 0;
	}
	public double getInitStatusValue(int type)//获取InitStatusValue
	{
		return InitStatus[type];
	}
	public double getTransProbMatrixValue(int i,int j)//获取TransProbMatrixValue
	{
		return TransProbMatrix[i][j];
	}
	public boolean iswordExist(String word)//判断某个字是否是未登录的
	{
		return (Bmap.containsKey(word)||Emap.containsKey(word)||Mmap.containsKey(word)||Smap.containsKey(word));
	}
}

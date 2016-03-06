package com.nlp2.rmwang;

import java.util.HashMap;
import java.util.Iterator;




class rtvalofComplexWordHandler{//用于complexwordHandler返回复合结果
	int end;
	int lastwordCX;
	public rtvalofComplexWordHandler(int e,int lst)
	{
		end=e;
		lastwordCX=lst;
	}
}
class probabitity{//频率，用于存储EmitProbMatrix
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

public class HMMdictionaryforCX {
	//substring(23);
	private int CiXing_type=100;//词性种类，初始化为100
	private double TransProbMatrix[][],InitStatus[];
	private String OriginalfilePath;
	private String dictionaryPath;
	private String encodingString;
	private String cxiniPath;//词性配置文件路径
	private int allsentencenum;//总句子数
	private int alltransnum;//转移总数
	private HashMap hashmaps[];
	private IOforHMM myIOforHMMdic;
	private HashMap cxmap,allwordMap;
	private int cxnum;
	private int idforN;//n的id
	private int idforNR;//nr的id
	boolean isCXiniExist;
	
	public HMMdictionaryforCX(String orgpath,String dicpath,String encoding,String cxpath)//构造
	{
		myIOforHMMdic=new IOforHMM(2);
		OriginalfilePath=orgpath;
		dictionaryPath=dicpath;
		cxiniPath=cxpath;
		encodingString=encoding;
		allsentencenum=0;
		alltransnum=0;
		cxnum=0;
		cxmap=new HashMap();
		allwordMap=new HashMap();
		if(IOforHMM.isFileExist(cxiniPath))//载入配置文件
		{
			buildCXini();
			isCXiniExist=true;
			//saveCXini("E:\\HMMcxINITEST.txt");测试代码
		}
		else {
			isCXiniExist=false;
		}
		TransProbMatrix=new double[CiXing_type][CiXing_type];
		InitStatus=new double[CiXing_type];
		int i,j;
		hashmaps=new HashMap[CiXing_type];
		for(i=0;i<CiXing_type;i++)
		{
			for(j=0;j<CiXing_type;j++)
			{
				TransProbMatrix[i][j]=0;
			}
			InitStatus[i]=0;
			hashmaps[i]=new HashMap();
		}
		if(isdicExist()&&isCXiniExist)
		{
			buildbyDicfile();
			//saveDic("E:\\HMMdicTEST.txt");测试代码
		}
		else {
			buildbyOrgfile();
		}
		if(isCXiniExist==false)
		{
			saveCXini(cxiniPath);
			isCXiniExist=true;
		}
		idforN=translateCX("n");
		idforNR=translateCX("nr");
	}
	private void buildCXini()//建立词性配置信息
	{
		myIOforHMMdic.startRead(cxiniPath, encodingString, 0);
		String s;
		CiXing_type=Integer.valueOf(myIOforHMMdic.readOneSentence(0)).intValue();
		while((s=myIOforHMMdic.readOneSentence(0))!=null)
		{
			cxmap.put(s, new Integer(myIOforHMMdic.readOneSentence(0)));
		}
		myIOforHMMdic.endRead(0);
	}
	private void saveCXini(String path)//保存词性配置信息
	{
		myIOforHMMdic.startWrite(path, encodingString,1);
		myIOforHMMdic.writeOneString(CiXing_type+"\r\n", 1);//写入总数
		Iterator iter = cxmap.entrySet().iterator();
		while (iter.hasNext()) {
			HashMap.Entry entry = (HashMap.Entry) iter.next();
			String key = (String)entry.getKey();
			Integer val = (Integer)entry.getValue();
			myIOforHMMdic.writeOneString(key+"\r\n",1);
			myIOforHMMdic.writeOneString(val.intValue()+"\r\n",1);
		}
		myIOforHMMdic.endWrite(1);
	}
	private void buildbyDicfile()//通过词典文件建立词典
	{
		myIOforHMMdic.startRead(dictionaryPath, encodingString, 0);
		int i,j;
		String s;
		for(i=0;i<CiXing_type;i++)
		{
			s=myIOforHMMdic.readOneSentence(0);
			InitStatus[i]=Double.valueOf(s).doubleValue();
			for(j=0;j<CiXing_type;j++)
			{
				s=myIOforHMMdic.readOneSentence(0);
				TransProbMatrix[i][j]=Double.valueOf(s).doubleValue();
			}
		}
		for(i=0;i<CiXing_type;i++)
		{
			int size=Integer.valueOf(myIOforHMMdic.readOneSentence(0)).intValue();
			for(j=0;j<size;j++)
			{
				s=myIOforHMMdic.readOneSentence(0);
				hashmaps[i].put(s, new probabitity(0, Double.valueOf(myIOforHMMdic.readOneSentence(0)).doubleValue()));
				allwordMap.put(s, null);
			}
		}
		myIOforHMMdic.endRead(0);
		System.out.println("词典建立完毕");
	}
	public String oneLineProcessForStandard(String orgString)//读进来的原始语料的一行处理成为标准格式，不同原始语料只需重写此函数
	//标准格式如下：[中国/ns  政府/n]nt  将/d  继续/v  坚持/v  奉行/v  独立自主/i  的/u  和平/n  外交/n  政策/n  ，/w  在/p  和平共处/l  五/m  项/q  原则/n  的/u  基础/n  上/f  努力/ad  发展/v  同/p  世界/n  各国/r  的/u  友好/a  关系/n  。/w  中国/ns  愿意/v  加强/v  同/p  联合国/nt  和/c  其他/r  国际/n  组织/n  的/u  协调/vn  ，/w  促进/v  在/p  扩大/v  经贸/j  科技/n  交流/vn  、/w  保护/v  环境/n  、/w  消除/v  贫困/an  、/w  打击/v  国际/n  犯罪/vn  等/u  方面/n  的/u  国际/n  合作/vn  。/w  中国/ns  永远/d  是/v  维护/v  世界/n  和平/n  与/c  稳定/an  的/u  重要/a  力量/n  。/w  中国/ns  人民/n  愿/v  与/p  世界/n  各国/r  人民/n  一道/d  ，/w  为/p  开创/v  持久/a  和平/n  、/w  共同/d  发展/v  的/u  新/a  世纪/n  而/c  不懈努力/l  ！/w 
	{
		return orgString.substring(23);
	}
	private void buildbyOrgfile()//依据语料库生成字典（框架）
	{
		myIOforHMMdic.startRead(OriginalfilePath, encodingString, 0);
		String tmpLineValString=null;
		while ((tmpLineValString=myIOforHMMdic.readOneSentence(0))!=null) {
			allsentencenum++;
			if(tmpLineValString.length()==0) //剔除空行
			{
				continue;
			}
			onesentenceAnalyze(oneLineProcessForStandard(tmpLineValString));//此处为剔除19980101-01-001-001/m  所用，更换语料库需重写
		}
		int i,j;
		for(i=0;i<CiXing_type;i++)
		{
			InitStatus[i]=Math.log((double)InitStatus[i]/(double)allsentencenum);//计算InitStatus
			calproofEmitRobMatrix(hashmaps[i]);//计算EmitRobMatrix
		}
		for(i=0;i<CiXing_type;i++)
		{
			for(j=0;j<CiXing_type;j++)
			{
				alltransnum+=TransProbMatrix[i][j];//计算TransProbMatrix
			}
		}
		for(i=0;i<CiXing_type;i++)
		{
			for(j=0;j<CiXing_type;j++)
			{
				TransProbMatrix[i][j]=Math.log(TransProbMatrix[i][j]/alltransnum);//计算TransProbMatrix
			}
		}
		myIOforHMMdic.endRead(0);
		CiXing_type=cxmap.size();//更新词性数量
		saveDic(dictionaryPath);
		System.out.println("词典建立完成");
	}
	public int translateCX(String a)//翻译词性,返回对应编号
	{
		if(isCXiniExist)
		{
			Integer tmpInteger;
			tmpInteger=(Integer)cxmap.get(a);
			return tmpInteger.intValue();
		}
		else {
			if(cxmap.containsKey(a))
			{
				Integer tmpInteger;
				tmpInteger=(Integer)cxmap.get(a);
				return tmpInteger.intValue();
			}
			else {
				Integer tmpInteger=new Integer(cxnum);
				cxmap.put(a, tmpInteger);
				cxnum++;
				return cxnum-1;
			}
		}
	}
	private rtvalofComplexWordHandler complexWordHandler(String tmpLineVal,int i,int len,int lastwordCX)//对复合词的处理，用于建立词典。例如：[中央/n  人民/n  广播/vn  电台/n]nt
	{
		rtvalofComplexWordHandler rt;
		int start=i+1;
		for(;i<len;i++)
		{
			if(tmpLineVal.substring(i,i+1).equals("]"))
				break;
		}
		int reci=i;//记录后括号位置
		for(;i<len;i++)
		{
			if(tmpLineVal.substring(i, i+1).equalsIgnoreCase(" "))
				break;
		}
		int bigwordcixing=translateCX(tmpLineVal.substring(reci+1,i));//复合词的词性
		String complexwordString=tmpLineVal.substring(start,reci);//完整的复合词，包含词性后缀
		int end=i;
		while(end<len&&tmpLineVal.charAt(end)==' ')//使end指向下一个词的开头
		{
			end++;
		}
		String bigwordString="";
		int complexwordlength=complexwordString.length();
		String s="";
		int lastcx=-1;//记录复合词中各成分词上一个成分词的词性
		for(i=0;i<complexwordlength;)
		{
			while(i<complexwordlength&&complexwordString.charAt(i)!=' ')//提取单个词
			{
				s+=complexwordString.charAt(i);
				i++;
			}
			int rec=0;
			while(rec<s.length()&&!s.substring(rec,rec+1).equalsIgnoreCase("/"))
			{
				rec++;
			}
			int cx=translateCX(s.substring(rec+1));//复合词中某单个词词性
			s=s.substring(0,rec);//削去词性后缀
			if(lastcx!=-1)
			{
				TransProbMatrix[lastcx][cx]++;
			}
			lastcx=cx;
			if(hashmaps[cx].containsKey(s))//已经包含，词频+1
			{
				probabitity tmppro;
				tmppro=(probabitity) hashmaps[cx].get(s);
				tmppro.inccounter();
			}
			else {//首次出现，词频置1
				hashmaps[cx].put(s, new probabitity(1,0));
				allwordMap.put(s, null);
			}
			bigwordString+=s;
			if(s.length()==0) System.out.println("Wrong in:"+tmpLineVal);//调试信息
			s="";
			while(i<complexwordlength&&complexwordString.charAt(i)==' ')
			{
				i++;
			}
		}
		//将复合词加入
		if(bigwordString.length()==0) System.out.println("Wrong in:"+tmpLineVal);//调试信息
		//System.out.println("XXX"+bigwordString);
		if(hashmaps[bigwordcixing].containsKey(bigwordString))//已经包含，词频+1
		{
			probabitity tmppro;
			tmppro=(probabitity) hashmaps[bigwordcixing].get(bigwordString);
			tmppro.inccounter();
		}
		else {//首次出现，词频置1
			hashmaps[bigwordcixing].put(bigwordString, new probabitity(1,0));
			allwordMap.put(bigwordString, null);
		}
		//更新词性转移矩阵
		if(lastwordCX!=-1)
		{
			TransProbMatrix[lastwordCX][bigwordcixing]++;
		}
		else {//=-1说明是首词
			InitStatus[bigwordcixing]++;
		}
		rt=new rtvalofComplexWordHandler(end, bigwordcixing);
		if(end<len&&tmpLineVal.charAt(end)=='[')
		{
			rt=complexWordHandler(tmpLineVal, end, len,bigwordcixing);
		}
		return rt;
	}
	private void onesentenceAnalyze(String tmpLineVal)//单句分析，用于建立词典
	{
		int len=tmpLineVal.length();
		int i;
		String s="";
		int lastCX=-1;
		rtvalofComplexWordHandler rt;
		for(i=0;i<len;)
		{
			if(tmpLineVal.substring(i,i+1).equals("["))//复合词处理
			{
				rt=complexWordHandler(tmpLineVal, i, len,lastCX);
				i=rt.end;
				lastCX=rt.lastwordCX;
				if(i>=len) break;
			}
			while(i<len&&tmpLineVal.charAt(i)!=' ')
			{
				s+=tmpLineVal.charAt(i);
				i++;
			}
			int rec=0;
			while(rec<s.length()&&!s.substring(rec,rec+1).equalsIgnoreCase("/"))
			{
				rec++;
			}
			int cx=translateCX(s.substring(rec+1));//获取词性
			s=s.substring(0,rec);//削去词性后缀
			
			//更新词性转移矩阵，及lastcx：
			if(lastCX!=-1)
			{
				TransProbMatrix[lastCX][cx]++;
			}
			else {
				InitStatus[cx]++;
			}
			lastCX=cx;
			//将词插入EmitProbMatrix
			if(hashmaps[cx].containsKey(s))//已经包含，词频+1
			{
				probabitity tmppro;
				tmppro=(probabitity) hashmaps[cx].get(s);
				tmppro.inccounter();
			}
			else {//首次出现，词频置1
				hashmaps[cx].put(s, new probabitity(1, 0));
				allwordMap.put(s, null);
			}
			if(s.length()==0) System.out.println(tmpLineVal);//调试信息
			//将词置空
			s="";
			//指针移动找到下一个词的起始坐标
			while(i<len&&tmpLineVal.charAt(i)==' ')
			{
				i++;
			}
		}
				
	}
	private void calproofEmitRobMatrix(HashMap map)//计算EmitRobMatrix的值（概率取ln）
	{
		int size=map.size();
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			HashMap.Entry entry = (HashMap.Entry) iter.next();
			probabitity tmpp = (probabitity)entry.getValue();
			tmpp.calpro(size);
		}
	}
	public double getEmitRobMatrixValue(int type,String word)//获取EmitRobMatrix值
	{
		/*if(iswordExist(word)==false&&(type==translateCX("n")||type==translateCX("nr"))) 
		{
			//System.out.println("未登录词："+word);
			return 0;//如果是未登录的词，猜测它是名词
		}*/
		if(!iswordExist(word)&&(type==idforN||type==idforNR))
		{
			//System.out.println("未登录词："+word);
			return 0;//如果是未登录的词，猜测它是名词
		}
		probabitity pro=(probabitity)hashmaps[type].get(word);
		if(pro==null) {return Math.log(0);}
		else {return pro.pro;}
	}
	public double getInitStatusValue(int type)//获取InitStatusValue
	{
		return InitStatus[type];
	}
	public double getTransProbMatrixValue(int i,int j)//获取TransProbMatrixValue
	{
		return TransProbMatrix[i][j];
	}
	public boolean isdicExist()//判断词典文件是否存在
	{
		return IOforHMM.isFileExist(dictionaryPath);
	}
	public boolean iswordExist(String word)//判断word是否是未登录词
	{
		/*boolean rstVal=false;
		int i;
		for(i=0;i<CiXing_type;i++)
		{
			rstVal=rstVal||hashmaps[i].containsKey(word);
		}
		return rstVal;*/
		return allwordMap.containsKey(word);
	}
	private void saveDic(String path)//存储词典
	{
		int i,j;
		myIOforHMMdic.startWrite(path, encodingString, 0);
		for(i=0;i<CiXing_type;i++)
		{
			myIOforHMMdic.writeOneString(InitStatus[i]+"\r\n", 0);
			for(j=0;j<CiXing_type;j++)
			{
				myIOforHMMdic.writeOneString(TransProbMatrix[i][j]+"\r\n", 0);
			}
		}
		for(i=0;i<CiXing_type;i++)
		{
			Iterator iter = hashmaps[i].entrySet().iterator();
			myIOforHMMdic.writeOneString(hashmaps[i].size()+"\r\n", 0);//写入总数
			while (iter.hasNext()) {
				HashMap.Entry entry = (HashMap.Entry) iter.next();
				String key = (String)entry.getKey();
				probabitity val = (probabitity)entry.getValue();
				myIOforHMMdic.writeOneString(key+"\r\n",0);
				myIOforHMMdic.writeOneString(val.pro+"\r\n",0);
			}
		}
		myIOforHMMdic.endWrite(0);
	}
	public void corpusYCL(String path,String QHMMpath,String AHMMpath,String enString)//语料预处理，依据corpus生成评测语料
	{
		myIOforHMMdic.startWrite(QHMMpath, encodingString, 0);
		myIOforHMMdic.startWrite(AHMMpath, enString, 1);
		myIOforHMMdic.startRead(path, encodingString, 0);
		String sen;
		StringBuffer bufQ,bufA;
		bufQ=new StringBuffer();
		bufA=new StringBuffer();
		while((sen=myIOforHMMdic.readOneSentence(0))!=null)
		{
			if(sen.length()==0) continue;
			try {
				oneSentenceYCL(oneLineProcessForStandard(sen),bufQ,bufA);
			} catch (java.lang.StringIndexOutOfBoundsException e) {
				// TODO: handle exception
				System.out.println(sen);
				System.exit(1);
			}
			
			myIOforHMMdic.writeStringBufferIntoTXT(bufQ, 0);
			myIOforHMMdic.writechars(bufA.toString().toCharArray(), 1);
			bufQ.setLength(0);
			bufA.setLength(0);
		}
		myIOforHMMdic.endRead(0);
		myIOforHMMdic.endWrite(0);
		myIOforHMMdic.endWrite(1);
	}
	private int complexWordYCL(String tmpLineVal, int i, int len,StringBuffer bufQ,StringBuffer bufA)//复合词的预处理
	{
		int start=i+1;
		for(;i<len;i++)
		{
			if(tmpLineVal.substring(i,i+1).equals("]"))
				break;
		}
		int reci=i;//记录后括号位置
		for(;i<len;i++)
		{
			if(tmpLineVal.substring(i, i+1).equalsIgnoreCase(" "))
				break;
		}
		int bigwordcixing=translateCX(tmpLineVal.substring(reci+1,i));//复合词的词性
		String complexwordString=tmpLineVal.substring(start,reci);//[]之内的内容
		int end=i;
		while(end<len&&tmpLineVal.charAt(end)==' ')//使end指向下一个词的开头
		{
			end++;
		}
		String bigwordString="";
		int complexwordlength=complexwordString.length();
		String s="";
		for(i=0;i<complexwordlength;)
		{
			while(i<complexwordlength&&complexwordString.charAt(i)!=' ')//提取单个词
			{
				s+=complexwordString.charAt(i);
				i++;
			}
			int rec=0;
			while(rec<s.length()&&!s.substring(rec,rec+1).equalsIgnoreCase("/"))
			{
				rec++;
			}
			s=s.substring(0,rec);//削去词性后缀
			bigwordString+=s;//拼接成复合词
			if(s.length()==0) System.out.println("Wrong in:"+tmpLineVal);//调试信息
			s="";
			while(i<complexwordlength&&complexwordString.charAt(i)==' ')
			{
				i++;
			}
		}
		if(bigwordString.length()==0) System.out.println("Wrong in:"+tmpLineVal);//调试信息
		bufQ.append(bigwordString+"  ");
		bufA.append((char)(bigwordcixing+65));
		if(end<len&&tmpLineVal.charAt(end)=='[')
		{
			end=complexWordYCL(tmpLineVal, end, len,bufQ,bufA);
		}
		return end;
	}
	private void oneSentenceYCL(String tmpLineVal,StringBuffer bufQ,StringBuffer bufA)//单句预处理
	{
		int len=tmpLineVal.length();
		int i;
		String s="";
		for(i=0;i<len;)
		{
			if(tmpLineVal.substring(i,i+1).equals("["))//复合词处理
			{
				i=complexWordYCL(tmpLineVal, i, len,bufQ,bufA);
				if(i>=len) break;
			}
			while(i<len&&tmpLineVal.charAt(i)!=' ')
			{
				s+=tmpLineVal.charAt(i);
				i++;
			}
			int rec=0;
			while(rec<s.length()&&!s.substring(rec,rec+1).equalsIgnoreCase("/"))
			{
				rec++;
			}
			int cx=translateCX(s.substring(rec+1));//获取词性
			s=s.substring(0,rec);//削去词性后缀
			bufA.append((char)(cx+65));
			bufQ.append(s+"  ");
			//将词置空
			s="";
			//指针移动找到下一个词的起始坐标
			while(i<len&&tmpLineVal.charAt(i)==' ')
			{
				i++;
			}
		}
		String tmp=bufQ.substring(0,bufQ.length()-2);//出去最后两个空格
		bufQ.setLength(0);
		bufQ.append(tmp);
		bufQ.append("\r\n");
		bufA.append("\r\n");
	}
	public int getCXtypeNum()//获取词性数量
	{
		return CiXing_type;
	}
}

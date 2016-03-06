package com.nlp1.rmwang;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
class counter{
	int val;
	public counter()
	{
		val=0;
	}
	public int get()
	{
		return val;
	}
	public void add(int r)
	{
		val+=r;
	}
	public counter(int r)
	{
		val=r;
	}
	public counter(String s)
	{
		val=new Integer(s).intValue();
	}
	public String toString()
	{
		return ""+val;
	}
}
public class MyDictionary {
	String corpusPathString;
	String dictionaryTXTPathString;//词典存储路径
	String encodingString;//编码格式
	HashMap hashmap;//词典数据结构
	myIO myIOforfmmbmmdic;
	StringBuffer originfileforHMM;//利用原始语料库为HMM生成所需语料库，文字缓存在此stringbuffer中
	public int maxwordlenght;//最大词长
	public int complexWordHandler(String tmpLineVal,int i,int len)//对复合词的处理，例如：[中央/n  人民/n  广播/vn  电台/n]nt
	{
		int start=i+1;
		for(;i<len;i++)
		{
			if(tmpLineVal.substring(i,i+1).equals("]"))
				break;
		}
		for(;i<len;i++)
		{
			if(tmpLineVal.substring(i, i+1).equalsIgnoreCase(" "))
				break;
		}
		String complexwordString=tmpLineVal.substring(start,i);
		int end=i;
		while(end<len&&tmpLineVal.charAt(end)==' ')
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
			if(hashmap.containsKey(s))//已经包含，词频+1
			{
				counter count;
				count=(counter) hashmap.get(s);
				count.add(1);
				hashmap.put(s,count);
			}
			else {//首次出现，词频置1
				hashmap.put(s, new counter(1));
				if(s.length()>maxwordlenght)//更新最大词长
				{
					maxwordlenght=s.length();
				}
			}
			bigwordString+=s;
			s="";
			while(i<complexwordlength&&complexwordString.charAt(i)==' ')
			{
				i++;
			}
		}
		if(hashmap.containsKey(bigwordString))//已经包含，词频+1
		{
			counter count;
			count=(counter) hashmap.get(bigwordString);
			count.add(1);
			hashmap.put(bigwordString,count);
		}
		else {//首次出现，词频置1
			hashmap.put(bigwordString, new counter(1));
			if(bigwordString.length()>maxwordlenght)//更新最大词长
			{
				maxwordlenght=bigwordString.length();
			}
		}
		if(end<len&&tmpLineVal.charAt(end)=='[')
		{
			end=complexWordHandler(tmpLineVal, end, len);
		}
		return end;
	}
	private void readfromOriginfile()//利用原始语料库建立FMMBMM词典，并导出HMM训练语料
	{
		myIOforfmmbmmdic.startRead(corpusPathString, encodingString, 0);
		//建立FMMBMM词典
		String tmpLineVal;
		hashmap=new HashMap();
		while((tmpLineVal=myIOforfmmbmmdic.readOneSentence(0))!=null)
		{
			if(tmpLineVal.length()==0) //原语料中有空行，除去
			{
				//System.out.println("空行");
				continue;
			}
			int len=tmpLineVal.length();
			int i;
			String s="";
			for(i=0;i<len;)
			{
				if(tmpLineVal.substring(i,i+1).equals("["))//复合词处理
				{
					i=complexWordHandler(tmpLineVal, i, len);
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
				s=s.substring(0,rec);//削去词性后缀
				if(hashmap.containsKey(s))//已经包含，词频+1
				{
					counter count;
					count=(counter) hashmap.get(s);
					count.add(1);
					hashmap.put(s,count);
				}
				else {//首次出现，词频置1
					hashmap.put(s, new counter(1));
					if(s.length()>maxwordlenght)//更新最大词长
					{
						maxwordlenght=s.length();
					}
				}
				s="";
				while(i<len&&tmpLineVal.charAt(i)==' ')
				{
					i++;
				}
			}
		}
		myIOforfmmbmmdic.endRead(0);
		System.out.println("词典建立完毕");
		
		
	}
	private void readfromdic(String path)//通过字典文件建立FMMBMM字典
	{
		String tmpLineVal="";
		String oValString="";
		hashmap=new HashMap();
		myIOforfmmbmmdic.startRead(path, encodingString, 0);
		tmpLineVal=myIOforfmmbmmdic.readOneSentence(0);//读取最大词长度
		maxwordlenght=new Integer(tmpLineVal).intValue();//读取最大词长度
		while((tmpLineVal=myIOforfmmbmmdic.readOneSentence(0))!=null)
		{
			oValString=myIOforfmmbmmdic.readOneSentence(0);
			counter tmpCounter=new counter(oValString);
			hashmap.put(tmpLineVal, tmpCounter);
		}
		myIOforfmmbmmdic.endRead(0);
		System.out.println("词典建立完毕");
	}
	public MyDictionary(String corpuspath,String dicString,String encoding)//构造
	{
		maxwordlenght=0;
		corpusPathString=corpuspath;
		dictionaryTXTPathString=dicString;
		encodingString=encoding;
		originfileforHMM=new StringBuffer();
		myIOforfmmbmmdic=new myIO(2);
		if(isdicExist(dictionaryTXTPathString))
		{
			readfromdic(dictionaryTXTPathString);
			//writeintoTXT("E:\\dic2.txt");
		}
		else {
			readfromOriginfile();
			writeintoTXT("E:\\dic.txt");
		}
	}
	public boolean find(String aim)//查找
	{
		return hashmap.containsKey(aim);
	}
	public int size()//返回词典大小
	{
		return hashmap.size();
	}
	public void writeintoTXT(String path)//将词典写入txt文件
	{
		myIOforfmmbmmdic.startWrite(path, encodingString, 0);
		Iterator iter = hashmap.entrySet().iterator();
		myIOforfmmbmmdic.writeOneString(new Integer(maxwordlenght).toString()+"\r\n",0);
		while (iter.hasNext()) {
			HashMap.Entry entry = (HashMap.Entry) iter.next();
			String key = (String)entry.getKey();
			counter val = (counter)entry.getValue();
			myIOforfmmbmmdic.writeOneString(key+"\r\n",0);
			myIOforfmmbmmdic.writeOneString(val.toString()+"\r\n",0);
		}
		myIOforfmmbmmdic.endWrite(0);
	}
	public boolean isdicExist(String path)//判断词典文件是否存在
	{
		File save=new File(path);
		return save.exists();
	}
	public void buildCorpusForHMM(String Path)//生成HMMcorpus
	{
		myIOforfmmbmmdic.startWrite(Path, "Unicode", 0);
		myIOforfmmbmmdic.startRead(corpusPathString, encodingString, 0);
		String tmpLineVal;
		while((tmpLineVal=myIOforfmmbmmdic.readOneSentence(0))!=null)
		{
			if(tmpLineVal.length()==0) //原语料中有空行，除去
			{
				//System.out.println("空行");
				continue;
			}
			int len=tmpLineVal.length();
			int i;
			String s="";
			for(i=0;i<len;)
			{
				if(tmpLineVal.substring(i,i+1).equals("["))//复合词处理
				{
					i=complexWordHandlerForHMMCorpus(tmpLineVal, i, len);
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
				s=s.substring(0,rec);//削去词性后缀
				originfileforHMM.append("  "+s);
				s="";
				while(i<len&&tmpLineVal.charAt(i)==' ')
				{
					i++;
				}
			}
			String tmpstoreString="";
			try {
				tmpstoreString=originfileforHMM.substring(23);/////////////////////////////////////////
			} catch (java.lang.StringIndexOutOfBoundsException e) {
				// TODO: handle exception
				System.out.println(originfileforHMM.toString());
				System.out.println(tmpLineVal);
				System.exit(0);
			}
			originfileforHMM.setLength(0);
			originfileforHMM.append(tmpstoreString);
			originfileforHMM.append("\r\n");
			myIOforfmmbmmdic.writeOneString(originfileforHMM.toString(),0);
			originfileforHMM.setLength(0);
		}
		myIOforfmmbmmdic.endRead(0);
		myIOforfmmbmmdic.endWrite(0);
		System.out.println("HMMcorpus建立完毕");
	}
	public int complexWordHandlerForHMMCorpus(String tmpLineVal,int i,int len)
	{
		int start=i+1;
		for(;i<len;i++)
		{
			if(tmpLineVal.substring(i,i+1).equals("]"))
				break;
		}
		for(;i<len;i++)
		{
			if(tmpLineVal.substring(i, i+1).equalsIgnoreCase(" "))
				break;
		}
		String complexwordString=tmpLineVal.substring(start,i);
		int end=i;
		while(end<len&&tmpLineVal.charAt(end)==' ')
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
			bigwordString+=s;
			s="";
			while(i<complexwordlength&&complexwordString.charAt(i)==' ')
			{
				i++;
			}
		}
		originfileforHMM.append("  "+bigwordString);
		if(end<len&&tmpLineVal.charAt(end)=='[')
		{
			end=complexWordHandler(tmpLineVal, end, len);
		}
		return end;
	}
}

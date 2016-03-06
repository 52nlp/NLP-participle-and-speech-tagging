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
	String dictionaryTXTPathString;//�ʵ�洢·��
	String encodingString;//�����ʽ
	HashMap hashmap;//�ʵ����ݽṹ
	myIO myIOforfmmbmmdic;
	StringBuffer originfileforHMM;//����ԭʼ���Ͽ�ΪHMM�����������Ͽ⣬���ֻ����ڴ�stringbuffer��
	public int maxwordlenght;//���ʳ�
	public int complexWordHandler(String tmpLineVal,int i,int len)//�Ը��ϴʵĴ������磺[����/n  ����/n  �㲥/vn  ��̨/n]nt
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
			while(i<complexwordlength&&complexwordString.charAt(i)!=' ')//��ȡ������
			{
				s+=complexwordString.charAt(i);
				i++;
			}
			int rec=0;
			while(rec<s.length()&&!s.substring(rec,rec+1).equalsIgnoreCase("/"))
			{
				rec++;
			}
			s=s.substring(0,rec);//��ȥ���Ժ�׺
			if(hashmap.containsKey(s))//�Ѿ���������Ƶ+1
			{
				counter count;
				count=(counter) hashmap.get(s);
				count.add(1);
				hashmap.put(s,count);
			}
			else {//�״γ��֣���Ƶ��1
				hashmap.put(s, new counter(1));
				if(s.length()>maxwordlenght)//�������ʳ�
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
		if(hashmap.containsKey(bigwordString))//�Ѿ���������Ƶ+1
		{
			counter count;
			count=(counter) hashmap.get(bigwordString);
			count.add(1);
			hashmap.put(bigwordString,count);
		}
		else {//�״γ��֣���Ƶ��1
			hashmap.put(bigwordString, new counter(1));
			if(bigwordString.length()>maxwordlenght)//�������ʳ�
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
	private void readfromOriginfile()//����ԭʼ���Ͽ⽨��FMMBMM�ʵ䣬������HMMѵ������
	{
		myIOforfmmbmmdic.startRead(corpusPathString, encodingString, 0);
		//����FMMBMM�ʵ�
		String tmpLineVal;
		hashmap=new HashMap();
		while((tmpLineVal=myIOforfmmbmmdic.readOneSentence(0))!=null)
		{
			if(tmpLineVal.length()==0) //ԭ�������п��У���ȥ
			{
				//System.out.println("����");
				continue;
			}
			int len=tmpLineVal.length();
			int i;
			String s="";
			for(i=0;i<len;)
			{
				if(tmpLineVal.substring(i,i+1).equals("["))//���ϴʴ���
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
				s=s.substring(0,rec);//��ȥ���Ժ�׺
				if(hashmap.containsKey(s))//�Ѿ���������Ƶ+1
				{
					counter count;
					count=(counter) hashmap.get(s);
					count.add(1);
					hashmap.put(s,count);
				}
				else {//�״γ��֣���Ƶ��1
					hashmap.put(s, new counter(1));
					if(s.length()>maxwordlenght)//�������ʳ�
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
		System.out.println("�ʵ佨�����");
		
		
	}
	private void readfromdic(String path)//ͨ���ֵ��ļ�����FMMBMM�ֵ�
	{
		String tmpLineVal="";
		String oValString="";
		hashmap=new HashMap();
		myIOforfmmbmmdic.startRead(path, encodingString, 0);
		tmpLineVal=myIOforfmmbmmdic.readOneSentence(0);//��ȡ���ʳ���
		maxwordlenght=new Integer(tmpLineVal).intValue();//��ȡ���ʳ���
		while((tmpLineVal=myIOforfmmbmmdic.readOneSentence(0))!=null)
		{
			oValString=myIOforfmmbmmdic.readOneSentence(0);
			counter tmpCounter=new counter(oValString);
			hashmap.put(tmpLineVal, tmpCounter);
		}
		myIOforfmmbmmdic.endRead(0);
		System.out.println("�ʵ佨�����");
	}
	public MyDictionary(String corpuspath,String dicString,String encoding)//����
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
	public boolean find(String aim)//����
	{
		return hashmap.containsKey(aim);
	}
	public int size()//���شʵ��С
	{
		return hashmap.size();
	}
	public void writeintoTXT(String path)//���ʵ�д��txt�ļ�
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
	public boolean isdicExist(String path)//�жϴʵ��ļ��Ƿ����
	{
		File save=new File(path);
		return save.exists();
	}
	public void buildCorpusForHMM(String Path)//����HMMcorpus
	{
		myIOforfmmbmmdic.startWrite(Path, "Unicode", 0);
		myIOforfmmbmmdic.startRead(corpusPathString, encodingString, 0);
		String tmpLineVal;
		while((tmpLineVal=myIOforfmmbmmdic.readOneSentence(0))!=null)
		{
			if(tmpLineVal.length()==0) //ԭ�������п��У���ȥ
			{
				//System.out.println("����");
				continue;
			}
			int len=tmpLineVal.length();
			int i;
			String s="";
			for(i=0;i<len;)
			{
				if(tmpLineVal.substring(i,i+1).equals("["))//���ϴʴ���
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
				s=s.substring(0,rec);//��ȥ���Ժ�׺
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
		System.out.println("HMMcorpus�������");
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
			while(i<complexwordlength&&complexwordString.charAt(i)!=' ')//��ȡ������
			{
				s+=complexwordString.charAt(i);
				i++;
			}
			int rec=0;
			while(rec<s.length()&&!s.substring(rec,rec+1).equalsIgnoreCase("/"))
			{
				rec++;
			}
			s=s.substring(0,rec);//��ȥ���Ժ�׺
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

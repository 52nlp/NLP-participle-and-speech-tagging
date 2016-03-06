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
	int allsentencenum;//�ܾ�����
	int sstartnum;//s��ͷ����
	int bstartnum;//b��ͷ����
	int alltransnum;//ת������
	int allwordnum;//�����������ã�Ŀǰ���㲻ʹ��
	HashMap Bmap,Emap,Mmap,Smap;
	myIO myIOforHMMdic;
	static final int B=0;
	static final int E=1;
	static final int M=2;
	static final int S=3;
	public boolean isdicExist()//�жϴʵ��ļ��Ƿ����
	{
		File dicfile=new File(dictionaryPath);
		return dicfile.exists();
	}
	public HMMdictionary(String orgpath,String dicpath,String encoding)//����
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
	public String standardtheLineforBuildDIC(String a)//��׼��������ӣ����˺���ת��Ϊ�������׼����ʽ����ͬԤ��ֻ���޸Ĵ˽ӿ�
	{
		int i=0,len=a.length();
		while(i<len&&a.charAt(i)==' ')
		{
			i++;
		}
		return a.substring(i);
	}
	public void buildbyOrgfile()//�������Ͽ������ֵ��㷨���
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
				//System.out.println("����");
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
		System.out.println("�ʵ佨�����");
	}
	public void calproofEmitRobMatrix(HashMap map)//����EmitRobMatrix��ֵ������ȡln��
	{
		int size=map.size();
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			HashMap.Entry entry = (HashMap.Entry) iter.next();
			probabitity tmpp = (probabitity)entry.getValue();
			tmpp.calpro(size);//��������͸���
		}
	}
	public void onesentenceAnalyze(String sen)//�������Ͽ⽨���ֵ䣬�������
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
	public boolean onewordAnalyze(String word,boolean isstart,boolean lastwordisS)//�������Ͽ⽨���ֵ䣬���ʷ���
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
		allwordnum+=len;//����������
		int i=0;
		String s=null;
		if(len>1)//��s
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
	public void buildbyDicfile()//�����ֵ��ļ������ֵ�
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
		System.out.println("�ʵ佨�����");
	}
	public void writeHashmapIntoTXT(HashMap hashmap,BufferedWriter bufwriter)//д��hashmap��д���ֵ��Ӻ�����
	{
		Iterator iter = hashmap.entrySet().iterator();
		try {
			bufwriter.write(hashmap.size()+"\r\n");//д������
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		while (iter.hasNext()) {
			HashMap.Entry entry = (HashMap.Entry) iter.next();
			String key = (String)entry.getKey();
			probabitity val = (probabitity)entry.getValue();
			try {//д�뵥��������
				bufwriter.write(key+"\r\n");
				bufwriter.write(val.pro+"\r\n");
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
	}
	public void writeDicIntoTXT(String path)//���ֵ�д��txt
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
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
	}
	public double getEmitRobMatrixValue(int type,String word)//��ȡEmitRobMatrixֵ
	{
		if(iswordExist(word)==false) return 0;//�����δ��¼���֣����صȿ��ܵĸ���ֵ
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
	public double getInitStatusValue(int type)//��ȡInitStatusValue
	{
		return InitStatus[type];
	}
	public double getTransProbMatrixValue(int i,int j)//��ȡTransProbMatrixValue
	{
		return TransProbMatrix[i][j];
	}
	public boolean iswordExist(String word)//�ж�ĳ�����Ƿ���δ��¼��
	{
		return (Bmap.containsKey(word)||Emap.containsKey(word)||Mmap.containsKey(word)||Smap.containsKey(word));
	}
}

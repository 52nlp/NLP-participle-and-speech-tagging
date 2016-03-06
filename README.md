# NLP-participle-and-speech-tagging
## 项目简要介绍
所有txt文件默认路径为E盘根目录下，所有txt文件在其当前目录下都有"说明.txt"文件进行说明，请将用到的文件放在E:\
com.nlp1.rmwang包含FMM，BMM，HMM分词
com.nlp2.rmwang包含词性标注
com.nameidentify.nlp.rmwang包含一个简单的人名识别
## 训练集说明：
AHMMCX是词性标注答案，corpus.txt的部分内容（corpusfortest.txt）经处理得到
QHMMCX是词性标注测试语料，corpus.txt的部分内容（corpusfortest.txt）经处理得到
question是分词测试语料中文分词评测测试语料（山西大学提供）
answer是分词答案，取自中文分词评测测试语料（山西大学提供）
## 词典说明 
dic是FMM，BMM所用词典，根据corpus.txt建立
HMMdic是HMM分词所用词典，根据（HMMtrain.txt）中文分词评测训练语料（山西大学提供）建立
HMMCiXingDIC是HMM词性标注所用词典，根据corpus.txt建立
HMMcxINI是HMM词性标注的词性信息配置文件，根据corpus.txt建立
## 结果集说明
CXResultbyHMM是HMM词性标注结果
CXResultcalrstbyHMM是resultbyFMM词性标注的结果，用于未登录词识别—人名识别
resultbyBMM是BMM的分词结果
resultbyFMM是FMM的分词结果
resultbyHMM是HMM的分词结果
resultofnameidentify是基于词性标注的人名识别结果

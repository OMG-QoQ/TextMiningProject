package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

public class Main {
	static ArrayList<DFitem> dflist=new <DFitem>ArrayList();
	static int tfid=0;
	static PriorItem[][]sim=new PriorItem[1095][1095];
	static ArrayList<IDFitem[]>idfs=new ArrayList<IDFitem[]>();
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//createSim();
	clustering();
	}
    public static void createSim() throws IOException{
    	//abs是用來存放doc之間的similarity
    	double[][]abs=new double[1095][1095];
    	//先計算所有文件的idf，這樣等會計算similarity時直接查詢就好，以空間換時間
    	for (int a=1;a<=1095;a++){
    		idfs.add(createIDF(String.valueOf(a)));
    	}
    	//接著將doc依序算出彼此的similarity
		for (int i=1;i<=1095;i++){
			for (int j=i+1;j<=1095;j++){
				abs[i-1][j-1]=CountSim(String.valueOf(i),String.valueOf(j));
			}
		}
		//做成文件以供取用
		FileWriter file = new FileWriter("similarity.txt");
	       BufferedWriter bwr =  new BufferedWriter(file);
	       DecimalFormat df = new DecimalFormat("0.000"); 
	     for (int i=0;i<1095;i++){  
	       for (int j=0;j<1095;j++){
	    	   if (abs[i][j]!=0.0){
	    	   bwr.write(df.format(abs[i][j])+" ");
	    	   }else {
	    		   bwr.write(df.format(abs[j][i])+" ");	   
	    	   }
	       }
	       bwr.newLine();
	     }
		   bwr.close();
		   file.close();
    }
	public static void clustering() throws IOException{
		//讀檔案進來，每個黨建立一個cluster->用陣列紀錄normalize tf-idf 計算各自的vector space
		
			int[]I=new int[1095];//I[n]表示點n還存在
			Cluster[]clusters=new Cluster[1095];//用來紀錄各cluster中的點有哪些
			//初始化分群List,一個doc是一群
			for (int i=0;i<1095;i++){
				Cluster clu=new Cluster();
				ArrayList<Integer>n=new ArrayList<Integer>();
				n.add(i);
				clu.SetN(n);
				clusters[i]=clu;
				
				//System.out.println(clusters.get(i).size());
			}
			
			 File similarity=new File("similarity.txt");
		      //以FileReader搭配BufferReader 進行stop word讀取(大量資料時比直接用FileReader快)
		      FileReader fr=new FileReader(similarity);
		      BufferedReader br = new BufferedReader(fr);
		     
		     
		      int row=0;
		      while (br.ready()) {
		     String input=br.readLine();
		      String[]abs=input.split(" ");
		      for (int i=0;i<1095;i++){
		    	  PriorItem pr=new PriorItem(row,Double.parseDouble(abs[i]),i);
		    	  sim[row][i]=pr;
		      }
		      row++;
		      }
		      
		      fr.close();
		      br.close();
		      int a=1095;
		      //讀檔完成後開始進行計算
		      //找到各個CLUSTER現在最接近的組合(抓其中各個DOC PAIR中SIM最大的)
		      
		      while(a>8){//a=K 當a的值為N時，代表剩下N個Cluster，因為每次merge都會減少一個群
		    	  PriorityQueue<PriorItem> pq = new PriorityQueue<PriorItem>();//使用PriorityQueue進行選擇要被merge的Cluster
		    	  
			//記錄各個cluster最近的點的編號
			for (int i=0;i<1095;i++){
				for (int j=0;j<1095;j++){
					if (I[j]!=1&&i!=j){//如果此編號的點還存在（為了節省設定值的時間，利用陣列預設為0，被merge才改為1),並過濾自己跟自己的similarity
						pq.add(sim[i][j]);
					}
				}
			}
			//再找出所有組合中最接近的進行merge
			PriorItem pr=new PriorItem();
			for (int i=0;i<pq.size();i++){
				pr=pq.poll();//pq中最近的一組
				if (I[pr.getN()]!=1){
					break;
				}
			}
			int num=pr.getN();
			int tar=pr.getTarget();
			
			//開始進行合併與更改I[n]
			if (clusters[tar].getN().size()==1){
				int ads=clusters[tar].getN().get(0);
				clusters[num].getN().add(ads);
				//System.out.print(ads);
			}else if (clusters[tar].getN().size()>1){
			for (int k=0;k<clusters[tar].getN().size();k++){
				clusters[num].getN().add(clusters[tar].getN().get(k));
			}}
			
			I[tar]=1;
			//更改新cluster對各群的similarity
			for (int i=0;i<1095;i++){
				sim[i][num].setSim(Math.min(sim[i][num].getSim(), sim[i][tar].getSim()));
				if (i==num){
					for (int j=0;j<1095;j++){
						sim[num][j].setSim(Math.min(sim[num][j].getSim(), sim[tar][j].getSim()));
					}
				}
			}
			a--; //每輪做完，cluster數都會減少
		      }
		     //將各cluster進行排序
		      ArrayList<int[]>ing=new ArrayList<int[]>();
		      for (int i=0;i<clusters.length;i++){
		    	  int[]num=new int[clusters[i].getN().size()];
		    	  for (int j=0;j<num.length;j++){
		    		  num[j]=Integer.valueOf(clusters[i].getN().get(j))+1;
		    	  }
		    	  Arrays.sort(num);
		    	  ing.add(num);
		      }
		      
		      
		      
		      
		     //將分群結果印出 
		      FileWriter file = new FileWriter(a+".txt");
		       BufferedWriter bwr =  new BufferedWriter(file);
		       //要按照排序
			  
		       for (int i=0;i<ing.size();i++){ //對每個群
			    	 if (I[i]!=1){
			    	 if (ing.get(i).length==1){  //裡面的每個數字做判斷是否為自己一個數一群，若是就不用跑下面的loop
			    		 bwr.write(String.valueOf(ing.get(i)[0]));	//印出該數
			    		 bwr.newLine();
			    	 }else{
			    	 for (int j=0;j<ing.get(i).length;j++){ //若不是就把整個群中的數已迴圈印出
			    		 bwr.write(String.valueOf(ing.get(i)[j]));
			    		 bwr.newLine();
			    	 }}
			    	 bwr.newLine(); //換行印下一群
			    	 }
			    	
			     }
			     bwr.close();
				 file.close();
				 
				
				 
		     
	}

	public static void createDic() throws IOException{
        //讀檔案迴圈，將資料逐筆stem後加入list
	       for (int i=1;i<=1095;i++){
	    	   String filename="IRTM/"+i;       
	    	   ArrayList<String>input=doStem(filename);
	    	   loadList(input,dflist); //加入list後會做轉成df格式的處理
	       }
	       //輸出成檔案
	       FileWriter file = new FileWriter("Dictionary.txt");
	       BufferedWriter bwr =  new BufferedWriter(file);
	       DFitem[]arr=new DFitem[dflist.size()];
	       for (int j=0;j<dflist.size();j++){
	    	   arr[j]=dflist.get(j);
	       }
	       //要按照字母排序
	       Arrays.sort(arr);
	       for(int u=0;u<dflist.size();u++){
	       bwr.write(tfid+" "+arr[u].getTerm()+" "+arr[u].getFreq());
	       bwr.newLine();
	       tfid++;
	       }
		   bwr.close();
		   file.close();
	}
	//輸入stem完的文件後將其加入dflist
private static void loadList(ArrayList<String>input,ArrayList<DFitem>output) throws IOException{
		
		back1:
		for (int i=0;i<input.size();i++){
		    	  String term=input.get(i);
		    	  int num=1;
		    	  for (int j=0;j<output.size();j++){
		    		  //如果這個字已經在list中有出現，則將frequency+1即可
		    		  if (term.equals(output.get(j).getTerm())){	    			  
		    			  output.get(j).addFreq();
		    			  continue back1;
		    		  }
		    	  }
		    	  //未出現過的字就新增
		    	  DFitem tf=new DFitem(term,num);
		    	  output.add(tf);
		      }
		}

	//修改上次寫過的Stemming algo. 讓他變成輸出ArrayList給其他方法使用，並把同一文件內重複出現的字第二次後刪去讓次數計算正確
	public static  ArrayList<String> doStem(String filename) throws IOException
	   {  ArrayList<String> terms=new ArrayList<String>();
	      char[] w = new char[501];
	     //建立新的Stemmer物件
	      Stemmer s = new Stemmer();
	      //輸入的文件名稱，以陣列方式可處理多筆文件
	      String[]context={"IRTM/"+filename};
	      //Stop word list的文件名稱
	      File stop=new File("stopword.txt");
	      //以FileReader搭配BufferReader 進行stop word讀取(大量資料時比直接用FileReader快)
	      FileReader fr=new FileReader(stop);
	      BufferedReader br = new BufferedReader(fr);
	      //由於處理出來的字數不確定，因此先以ArrayList存放再以其size設定陣列大小
	      ArrayList stoplist=new <String>ArrayList();
	      //將stopword放入ArrayList
	      while (br.ready()) {
	      stoplist.add(br.readLine());
	      }
	      
	      fr.close();
	      br.close();
	      //逐字進行(1)是否為文字（英文）(2)轉為小寫(3)是否為Stop word檢驗(4)寫入output
	      
	      for (int i = 0; i < context.length; i++)
	      try
	      {  
	    	 //將文章以InputStream方式輸入
	         FileInputStream in = new FileInputStream(context[i]);

	         try
	         {   back1:
	        	 while(true)

	           {  int ch = in.read();
	           //(1)
	              if (Character.isLetter((char) ch))
	              {
	                 int j = 0;
	                 while(true)
	                 {  
	                       //(2)     	 
	                	 ch = Character.toLowerCase((char) ch);
	                    w[j] = (char) ch;
	                    if (j < 500) j++;
	                    ch = in.read();
	                    if (!Character.isLetter((char) ch))
	                    {
	                       /* to test add(char ch) */
	                       for (int c = 0; c < j; c++) s.add(w[c]);

	                       s.stem();
	                       {  String u;

	                          /*  to test toString() : */
	                          u = s.toString();

	                          /* to test getResultBuffer(), getResultLength() : */
	                          /* u = new String(s.getResultBuffer(), 0, s.getResultLength()); */
	                          //(3)以一個boolean值檢驗這個文字，若其存在於stop word list中則不加入結果
	                          boolean isstopword=false;
	                          for (int x=0;x<stoplist.size();x++){
	                        	  if (u.equals(stoplist.get(x))){
	                             isstopword=true;
	                             break;
	                        	  }
	                          }
	                          //(4)
	                          if (!isstopword){
	                        	  
	                        	  if (!terms.isEmpty()){
	                        		  
	                        	  for (int k=0;k<terms.size();k++){
	                        		 //若此文件已出現過相同的字
	                        		  if (u.equals(terms.get(k))){
	                        			 continue back1; 
	                        		  }
	                        	  }
	                        	  }
	                        	 
	                        	  terms.add(u); 
	                          }
	                       }
	                       break;
	                    }
	                 }
	              }
	              if (ch < 0) break;
	           }
	         in.close(); 
	        
	         }
	         catch (IOException e)
	         {  System.out.println("error reading " + context[i]);
	            break;
	         }
	        
	      }
	      catch (FileNotFoundException e)
	      {  System.out.println("file " + context[i] + " not found");
	         break;
	      }
	      return terms;
	   }
	
	public static double CountSim(String file1,String file2) throws IOException{
		//先產出兩個檔案的tf-idf list
		IDFitem[]doc1tfidf=idfs.get(Integer.valueOf(file1)-1);
		IDFitem[]doc2tfidf=idfs.get(Integer.valueOf(file2)-1);
		//產生計算用的三個變數，var1,2是分母，3是分子
		double var1=0;
		double var2=0;
		double var3=0;
		//產出doc1的值（每個term的tfidf平方的總和）
		for (int i=0;i<doc1tfidf.length;i++){
			var1+=Math.pow(doc1tfidf[i].getscore(),2);
		}
		//產出doc2的值（每個term的tfidf平方的總和）
		for (int i=0;i<doc2tfidf.length;i++){
			var2+=Math.pow(doc2tfidf[i].getscore(),2);
		}
	    //兩個值取根號
		var1=Math.pow(var1, 0.5);
		var2=Math.pow(var2, 0.5);
		//計算分子
		for (int i=0;i<doc1tfidf.length;i++){
			for (int j=0;j<doc2tfidf.length;j++){
				//依照doc1的字找到doc2中相同的字，並取出他們的tfidf進行分子計算（向量內積）
				if (doc1tfidf[i].getId()==doc2tfidf[j].getId()){
					var3+=doc1tfidf[i].getscore()*doc2tfidf[j].getscore();
				}
				
			}
		}
	    //印出結果
		return (var3/(var1*var2));
		
		
	}
	public static IDFitem[] createIDF(String filename) throws IOException{
	    //第一個list用來存stem完的文件
		ArrayList<String>termlist=doStem(filename+".txt");
		//第二個list用來存計算出來的tf，由於都是具有id+frequency的格式，因直接採用df的class
		ArrayList<DFitem>tlist=new ArrayList<DFitem>();
	    //使用array存idf是為了排序
		IDFitem[]idfarray=new IDFitem[termlist.size()];
		//讀取dictionary用來存取剛剛計算好的df
		FileReader fr=new FileReader("Dictionary.txt");
	    BufferedReader br = new BufferedReader(fr);
	    ArrayList<DFitem>dictionary=new <DFitem>ArrayList();
	    while (br.ready()) {
	     String[]str=br.readLine().split(" ");
	     DFitem tfi=new DFitem(Integer.valueOf(str[0]),str[1],Integer.valueOf(str[2]));
	     dictionary.add(tfi);
	    }
	    fr.close();
	    br.close();
		//依照每個輸入進來的文字，計算它的tf後放進tlist
	    back1:
			for (int i=0;i<termlist.size();i++){
			    	  String term=termlist.get(i);
			    	  int num=1;//預設的tf值，有出現就是1
			    	  for (int j=0;j<tlist.size();j++){
			    		  //針對每一個字進行是否重複的處理
			    		  if (term.equals(tlist.get(j).getTerm())){	 
			    			  //如果重複就在frequency+1
			    			  tlist.get(j).addFreq();
			    			  continue back1;
			    		  }
			    	  }
			    	  
			    	  DFitem tf=new DFitem(term,num);
			    	  tlist.add(tf);
			      }
      //依照輸入的每個字進行tf-idf計算
		back1:
		for (int i=0;i<termlist.size();i++){
			//比對dictionary中的df值計算idf
			for (int j=0;j<dictionary.size();j++){
				//找到相符的term id 就取用他的id,frequency並計算tf-idf
				if (termlist.get(i).equals(dictionary.get(j).getTerm())){
					//if match : (1)add to writelist (2)count
					int id=dictionary.get(j).getID();
					int df=dictionary.get(j).getFreq();
					double idf=Math.log10(1095.0/df);
					double tf=1;
				    //透過剛剛計算好的tflist抓取這個字在此文件的tf
					for (int k=0;k<tlist.size();k++){
						//找到同一個term
						if(termlist.get(i).equals(tlist.get(k).getTerm()))
							//tf為tlist中的freq值
							tf=tlist.get(k).getFreq();
					}
					IDFitem idfitem=new IDFitem(id,tf*idf);
					idfarray[i]=idfitem;
					continue back1;
				}
			}
		}
	    //呼叫此方法會回傳Array並按照term id 排序好
	    Arrays.sort(idfarray);
		return idfarray;
	   
	}
	public static void rebuild() throws NumberFormatException, IOException{
		
	}
	
	
}

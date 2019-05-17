package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;




public class CloseByOne {
	static final int BIT_MAX = 2147483647*2+1 ;
	static final int ARCHBIT = 31 ;
	static final int BIT = 1 ;
	
	static AtomicInteger acounter = new AtomicInteger();//round robin
	ArrayList<KrajcaWorker> KrajcaWorkers = null;
	ArrayList<FairWorker> FairWorkers = null;
	ArrayList<FairWorker2> FairWorkers2 = null;
	ArrayList<FairWorker0308> FairWorkers0308 = null;
	ArrayList<Task> conceptStack = new ArrayList<Task>();
	ExecutorService executor = null;
	
	int para_mode = 0;// 0:serial/ 1:node/ 2:krajca(r-r)/ 3:node(para-for)/ 6:node(do myself)/
	int para_level = 2;
	boolean para_fair = true;//Krajca: fair(true) / round-robin(false)
	int threads = 4;
	static AtomicInteger c_counter = new AtomicInteger();
	String file_name = null;
	
	int int_count_a = 0;
	int int_count_o = 0;
	int attributes = 0;
	int objects = 0;
	public ArrayList<int[]> buff = new ArrayList<int[]>();
	int[][] context2 = null;
	int[] context = null;
	int[] upto_bit = new int[32];
	int[][][] cols = null;
	int[][] cols2 = null;
	int[][] supps = null;
	float min_sup_rate = 0.00f;
	int min_support = 1;
	
	public CloseByOne(String str, int mode, int level, int threads){
		this.file_name = str;
		this.para_mode = mode;
		this.para_level = level;
		this.threads = threads;
	}
	public void setMinSupp(int v){
		min_sup_rate = 0.01f * v;
	}
	public void main(){
		long StartTime = System.currentTimeMillis();
		//System.out.println("#read_context");
		read_context();
		//System.out.println("#initialize_algorithm");
		initialize_algorithm();
		//System.out.println(" initialize time = " + (System.currentTimeMillis() - StartTime) + "\n");
		//System.out.println("#find_all_intents");
		
		find_all_intents();
		
		System.out.println("#concept = " + c_counter.toString());

	}
	private void read_context(){
		int max = 0;
		try{
			File file = new File(file_name);    			   
			FileReader fl = new FileReader(file);
			BufferedReader br = new BufferedReader(fl);
			String str = "";
			while((str = br.readLine())!=null){
				String[] line = str.split(" ");  
				int[] line2 = new int[line.length];
				for(int i = 0; i < line.length; i++){
					line2[i]=Integer.parseInt(line[i]);
					if(max < line2[i]){
						max = line2[i];
					}
				}
				buff.add(line2);
			}
			fl.close();
		}catch(Exception e){
            System.out.println("error");
        }
		attributes = max+1;
		objects = buff.size();
		min_support = (int)(min_sup_rate * objects);
		if(min_support == 0){min_support = 1;}
		System.out.println("atr:" + attributes + "\n" + "obj:" + objects);
		int_count_a = (attributes+1)/(ARCHBIT+1) + 1 ;
		int_count_o = (objects+1)/(ARCHBIT+1) + 1 ;
		context = new int[objects * int_count_a];
		
		for(int i = 0; i < objects; i++){
			int[] obj =  buff.get(i);
			for(int j=0  ; j< obj.length ; j++){
				context[i * int_count_a + obj[j]/(ARCHBIT+1)] |= BIT << (ARCHBIT-(obj[j]%(ARCHBIT+1)));
			}
		}
		
	}
	private void initialize_algorithm(){
		for(int i = 0; i <= ARCHBIT; i++){
			for(int j = ARCHBIT; j > i; j--){
				upto_bit[i] |= (BIT << j);
			}
		}
		
		cols = new int[int_count_a][ARCHBIT+1][int_count_o];
		cols2 = new int[attributes][int_count_o];
		supps = new int[int_count_a][ARCHBIT+1];
		
		for(int i=0 ; i < objects; i++ ){
			int[] obj =  buff.get(i);
			for(int j = 0; j < obj.length; j++){
				cols2[obj[j]][i/(ARCHBIT+1)] |= BIT << (i % (ARCHBIT+1)) ;
				
			}
		}
		float items = 0;
		for (int j = 0; j < int_count_a; j++){
			for (int i = ARCHBIT; i >= 0; i--) {
				int mask = (BIT << i);
				for (int x = 0, y = j; x < objects; x++, y += int_count_a){
					if ((context[y] & mask) != 0) {
						cols[j][i][x / (ARCHBIT + 1)] |= BIT << (x % (ARCHBIT + 1));
						supps[j][i]++;
						++items;
					}
				}
			}
		}
		System.out.println("Density = " + (items / (objects * attributes)));
	}
	
	private void find_all_intents(){
		int[] intent = new int[int_count_a];
		int[] extent = new int[int_count_o];
		compute_closure (intent, extent, extent, null);
		if ((intent[int_count_a - 1] & 1) != 0){return;}

		if(para_mode == 0){
			System.out.println("$serial");
			long StartTime = System.currentTimeMillis();
			Task task = new Task(intent, extent, 0, ARCHBIT);
			task.setID(1);
			generate_from_node_s(task, 0, ARCHBIT, 0);
			//generate_from_node(intent, extent, 0, ARCHBIT, 0);
			System.out.println(" mining time = " + (System.currentTimeMillis() - StartTime));
			
		}
		else if(para_mode == 1){
			System.out.println("#node");

			FairWorkers = new ArrayList<FairWorker>();
			executor = Executors.newFixedThreadPool(threads);
			for(int i=0;i<threads;++i){
				FairWorker worker = new FairWorker(i);
				FairWorkers.add(worker);
			}
			long StartTime = System.currentTimeMillis();
			Task task = new Task(intent, extent, 0, ARCHBIT);
			fairTasks.offer(task);
			try{
				executor.invokeAll(FairWorkers);
			}
			catch(InterruptedException e){
				System.out.println("executor error : " + e);
			}
			finally{
				executor.shutdown();
			}
			System.out.println(" mining time = " + (System.currentTimeMillis() - StartTime));
			float sum = 0;
			long sum_forTime = 0;
			for(FairWorker worker : FairWorkers){
				worker.postProcess();
				sum += worker.getTime();
				sum_forTime += worker.getForTime();
				c_counter.addAndGet(worker.getConceptCount());
			}
			float average = sum / threads;
			float var = 0;
			for(FairWorker worker : FairWorkers){
				var += (worker.getTime() - average) * (worker.getTime() - average);
			}
			var /= threads;
			float sd = (float) Math.sqrt(var);
			System.out.println(" var = " + var + ", sd(procTime) = " + sd);
			
			System.out.println("sum(Pi.totalTime) = " + sum_forTime);
		}
		else if(para_mode == 3){
			System.out.println("#para-for");

			FairWorkers2 = new ArrayList<FairWorker2>();
			executor = Executors.newFixedThreadPool(threads);
			for(int i=0;i<threads;++i){
				FairWorker2 worker = new FairWorker2(i);
				FairWorkers2.add(worker);
			}
			long StartTime = System.currentTimeMillis();
			Task task = new Task(intent, extent, 0, ARCHBIT);
			fairTasks.offer(task);
			try{
				executor.invokeAll(FairWorkers2);
			}
			catch(InterruptedException e){
				System.out.println("executor error : " + e);
			}
			finally{
				executor.shutdown();
			}
			System.out.println(" mining time = " + (System.currentTimeMillis() - StartTime));
			float sum = 0;
			for(FairWorker2 worker : FairWorkers2){
				worker.postProcess();
				sum += worker.getTime();
				c_counter.addAndGet(worker.getConceptCount());
			}
			float average = sum / threads;
			float var = 0;
			for(FairWorker2 worker : FairWorkers2){
				var += (worker.getTime() - average) * (worker.getTime() - average);
			}
			var /= threads;
			float sd = (float) Math.sqrt(var);
			System.out.println(" var = " + var + ", sd = " + sd);
		}
		else if(para_mode == 2 || para_mode == 5){
			System.out.println("#krajca");
			if(para_mode == 5){
				para_fair = false;
				System.out.println("#round-robin");
			}
			KrajcaWorkers = new ArrayList<KrajcaWorker>();
			executor = Executors.newFixedThreadPool(threads);
			for(int i=0;i<threads;++i){
				KrajcaWorker worker = new KrajcaWorker(i);
				KrajcaWorkers.add(worker);
			}
			System.out.println("#parallel_generate_from_node");
			long StartTime = System.currentTimeMillis();
			parallel_generate_from_node(intent, extent, 0, ARCHBIT, 0);
			long miningTime = (System.currentTimeMillis() - StartTime);
			System.out.println(" mining time(seq + para) = " + miningTime);
			long maxTime = 0;
			long sum = 0;
			for(KrajcaWorker worker : KrajcaWorkers){
				long temp_time = worker.getTime();
				sum += temp_time;
				if(maxTime < temp_time){maxTime = temp_time;}
			}
			float average = (float)sum / threads;
			float var = 0;
			System.out.println(" mining time(seq) = " + (miningTime - maxTime));
			System.out.println("each Thread procTime");
			for(KrajcaWorker worker : KrajcaWorkers){
				worker.postProcess();
				var += (worker.getTime() - average) * (worker.getTime() - average);
			}
			var /= threads;
			float sd = (float) Math.sqrt(var);
			System.out.println(" var = " + var + ", sd = " + sd);
			System.out.println("sum(Pi.totalTime) = " + sum);
		}
		else if(para_mode == 6){
			System.out.println("#node(do myself)");//doMyselfの条件はmasterとslaveで2箇所書き換えが必要

			FairWorkers0308 = new ArrayList<FairWorker0308>();
			executor = Executors.newFixedThreadPool(threads);
			for(int i=0;i<threads;++i){
				FairWorker0308 worker = new FairWorker0308(i);
				FairWorkers0308.add(worker);
			}
			long StartTime = System.currentTimeMillis();
			Task task = new Task(intent, extent, 0, ARCHBIT);
			fairTasks.offer(task);
			try{
				executor.invokeAll(FairWorkers0308);
			}
			catch(InterruptedException e){
				System.out.println("executor error : " + e);
			}
			finally{
				executor.shutdown();
			}
			System.out.println(" mining time = " + (System.currentTimeMillis() - StartTime));
			float sum = 0;
			long sum_forTime = 0;
			for(FairWorker0308 worker : FairWorkers0308){
				worker.postProcess();
				sum += worker.getTime();
				sum_forTime += worker.getForTime();
				c_counter.addAndGet(worker.getConceptCount());
			}
			float average = sum / threads;
			float var = 0;
			for(FairWorker0308 worker : FairWorkers0308){
				var += (worker.getTime() - average) * (worker.getTime() - average);
			}
			var /= threads;
			float sd = (float) Math.sqrt(var);
			System.out.println(" var = " + var + ", sd(procTime) = " + sd);
			
			System.out.println("sum(Pi.totalTime) = " + sum_forTime);
		}
	}
	int seqConceptNum = 0;
	private void parallel_generate_from_node(int[] intent, int[] extent, int start_int, int start_bit, int rec_level){
		if (rec_level == para_level) {
			Task task = new Task(intent, extent, start_int, start_bit);
			if(para_fair){
				fairTasks.offer(task);
			}else{
				KrajcaWorkers.get(acounter.incrementAndGet() % threads).add(task);
			}
			
			return;
		}
		int total = start_int * (ARCHBIT + 1) + (ARCHBIT - start_bit);
		I_C_A: for (; start_int < int_count_a; start_int++) {
			ATTR: for (; start_bit >= 0; start_bit--) {
				if (total >= attributes){break I_C_A;}//goto out
				total++;
				if (((intent[start_int] & (BIT << start_bit))) != 0 || (supps[start_int][start_bit] < min_support)){continue;}
				int[] new_intent = new int[int_count_a];
				int[] new_extent = new int[int_count_o];
				int supp = compute_closure (new_intent, new_extent, extent, cols[start_int][start_bit]);
				if (((new_intent[start_int] ^ intent[start_int]) & upto_bit[start_bit]) != 0) continue;
				for (int i = 0; i < start_int; i++){
					if ((new_intent[i] ^ intent[i]) != 0){continue ATTR;}//goto
				}
				if (supp < min_support){continue;}
				printConcept(new_intent, new_extent, -1);
				++seqConceptNum;
				if ((new_intent[int_count_a - 1] & BIT) != 0) {continue;}
				if (start_bit == 0){
					parallel_generate_from_node(new_intent, new_extent, start_int + 1, ARCHBIT, rec_level+1);
				}else{
					parallel_generate_from_node(new_intent, new_extent, start_int, start_bit - 1, rec_level+1);
				}
			}
			start_bit = ARCHBIT;
		}
		if (rec_level == 0) {
			System.out.println("concept = " + seqConceptNum + ", parallel start.");
			try{
				executor.invokeAll(KrajcaWorkers);
			}catch(Exception e){
				System.out.println("error");
			}
			executor.shutdown();
		}
	}
	public Task getVoidTask(){
		for(int i=0,n=conceptStack.size();i<n;++i){
			if(conceptStack.get(i).getID() == 0){
				conceptStack.get(i).setID(1);
				return conceptStack.get(i);
			}
		}
		int[] new_intent = generateInt(int_count_a);
		int[] new_extent = generateInt(int_count_o);
		Task task = new Task(new_intent, new_extent, 0, 0);
		task.setID(1);
		conceptStack.add(task);
		//System.out.println("size = " + conceptStack.size());
		return task;
	}
	final public int[] generateInt(int size){
		return new int[size];
	}
	//逐次
	private void generate_from_node_s(Task task, int start_int, int start_bit, int id){
		int[] intent = task.getIntent();
		int[] extent = task.getExtent();
		int total = start_int * (ARCHBIT + 1) + (ARCHBIT - start_bit);
		for (; start_int < int_count_a; start_int++) {
			ATTR: for (; start_bit >= 0; start_bit--) {
				if (total >= attributes){task.setID(0);return;}
				total++;
				if (((intent[start_int] & (BIT << start_bit))) != 0 || (supps[start_int][start_bit] < min_support)){continue;}
				Task new_task = getVoidTask();
				int[] new_intent = new_task.getIntent();
				int[] new_extent = new_task.getExtent();
				int supp = compute_closure (new_intent, new_extent, extent, cols[start_int][start_bit]);
				if (((new_intent[start_int] ^ intent[start_int]) & upto_bit[start_bit]) != 0){
					new_task.setID(0);
					continue;
				}
				for (int i = 0; i < start_int; i++){
					if ((new_intent[i] ^ intent[i]) != 0){
						new_task.setID(0);
						continue ATTR;
					}
				}
				if (supp < min_support){new_task.setID(0);continue;}
				if ((new_intent[int_count_a - 1] & BIT) != 0) {new_task.setID(0);continue;}
				
				printConcept(new_intent, new_extent, (start_int * ARCHBIT) + (ARCHBIT - start_bit));
				
				if (start_bit == 0){
					generate_from_node_s (new_task, start_int + 1, ARCHBIT, id);
				}else{
					generate_from_node_s (new_task, start_int, start_bit - 1, id);
				}
			}
			start_bit = ARCHBIT;
		}
		task.setID(0);//set empty
	}
	private void generate_from_node(int[] intent, int[] extent, int start_int, int start_bit, int id){
		int[] new_intent = generateInt(int_count_a);
		int[] new_extent = generateInt(int_count_o);
		int total = start_int * (ARCHBIT + 1) + (ARCHBIT - start_bit);
		for (; start_int < int_count_a; start_int++) {
			ATTR: for (; start_bit >= 0; start_bit--) {
				if (total >= attributes){return;}
				total++;
				if (((intent[start_int] & (BIT << start_bit))) != 0 || (supps[start_int][start_bit] < min_support)){continue;}
				
				int supp = compute_closure (new_intent, new_extent, extent, cols[start_int][start_bit]);
				if (((new_intent[start_int] ^ intent[start_int]) & upto_bit[start_bit]) != 0) continue;
				for (int i = 0; i < start_int; i++){
					if ((new_intent[i] ^ intent[i]) != 0){continue ATTR;}
				}
				if (supp < min_support){continue;}
				if ((new_intent[int_count_a - 1] & BIT) != 0) {continue;}
				
				printConcept(new_intent, new_extent, (start_int * ARCHBIT) + (ARCHBIT - start_bit));
				
				if (start_bit == 0){
					generate_from_node (new_intent, new_extent, start_int + 1, ARCHBIT, id);
				}else{
					generate_from_node (new_intent, new_extent, start_int, start_bit - 1, id);
				}
				new_intent = generateInt(int_count_a);
				new_extent = generateInt(int_count_o);
			}
			start_bit = ARCHBIT;
		}
	}

	private int compute_closure(int[] intent, int[] extent, int[] prev_extent, int[] atr_extent){

		//for(int i=0;i<int_count_a;++i){intent[i] = BIT_MAX;}
		Arrays.fill(intent, BIT_MAX);
		//for(int i=0;i<int_count_o;++i){extent[i] = 0;}
		Arrays.fill(extent, 0);

		if (atr_extent != null) {
			int supp = 0;
			for (int k = 0; k < int_count_o; ++k) {
				extent[k] = prev_extent[k] & atr_extent[k];
				if (extent[k] != 0){
					for (int l = 0; l <= ARCHBIT; ++l){//
						if ((extent[k] & (BIT << l)) != 0) {//32ごとに区切ったobjectのk番目のビット列の(BIT << l)番目が出現集合に含まれる
							for (int i = 0, j = int_count_a * (k * (ARCHBIT + 1) + l); i < int_count_a; ++i, ++j){
								intent[i] &= context[j];
								//++supp;//以前のプログラム.バグ 2行下へ
							}
							++supp;
						}
					}
				}
			}
			return supp;
		}
		else{
			for(int i=0;i<int_count_o;++i){extent[i] = BIT_MAX;}
			for (int j = 0; j < objects; ++j) {
				for (int i = 0; i < int_count_a; ++i)
					intent[i] &= context[int_count_a * j + i];
			}
			return 0;
		}
	}
	
	public void printBit(int bit) {
        String s = String.format("%32s", Integer.toBinaryString(bit)).replaceAll(" ", "0");
        System.out.println(s);
    }
	public String toStringBit(int bit) {
		return String.format("%32s", Integer.toBinaryString(bit)).replaceAll(" ", "0");
	}
	public final void printConcept(int in[], int ex[], int id) {
		int cnt = c_counter.incrementAndGet();
		//if(cnt % 5000 == 0){System.out.println(cnt);}
		boolean flag = true;
		if(flag)return;
        String str = id + ": { ";
        for(int j=0;j<int_count_o;++j){
	        for(int i=0;i<32;++i){
	        	if((ex[j] & 1<<i) != 0){str += ((i+j*ARCHBIT) + " ");}
	        }
        }
        str += "} / { ";
        for(int j=0;j<int_count_a;++j){
	        for(int i=31;i>=0;--i){
	        	if((in[j] & 1<<i) != 0){str += ((31-i+j*ARCHBIT) + " ");}
	        }
        }
        str += "}";
        System.out.println(str);
    }
	
/*-------------------------------------------------------------------------------------------------------------------------------*/
/*-------------------------------------------------------------------------------------------------------------------------------*/
	class Task{
		private int[] intent;
		private int[] extent;
		private int s_int,s_bit;
		private int ID = 0;
		private boolean doMyself = false;//trueの場合再帰呼び出しを自分で処理する(2017/03/08 追加)
		public Task(int[] in, int[] ex, int s_int, int s_bit){
			this.intent = in;
			this.extent = ex;
			this.s_int = s_int;
			this.s_bit = s_bit;
		}
		public int[] getIntent(){return intent;}
		public int[] getExtent(){return extent;}
		public int getInt(){return s_int;}
		public int getBit(){return s_bit;}
		public void setID(int n){ID = n;}
		public int getID(){return ID;}
		public void setStart(int i, int b){this.s_int = i;this.s_bit = b;}
		public void doMyself(){doMyself = true;}
		public boolean isDoMyself(){return doMyself;}
	}
	class KrajcaWorker implements Callable<Integer>{
		ArrayList<Task> TaskQueue = new ArrayList<Task>();
		int id = 0;
		long all_time = 0;
		int poll_num = 0;
		public KrajcaWorker(int id){
			this.id = id;
		}
		public long getTime(){
			return all_time;
		}
		public void postProcess(){
			System.out.println(id + " : " + all_time);
		}
		@Override
		public Integer call() {
			long StartTime = System.currentTimeMillis();
			if(para_fair){
				while(true){
					Task task = fairTasks.poll();
					if(task == null){break;}
					++poll_num;
					generate_from_node(task.getIntent(), task.getExtent(), task.getInt(), task.getBit(), id);
				};
			}else{
				for(Task task : TaskQueue){
					++poll_num;
					generate_from_node(task.getIntent(), task.getExtent(), task.getInt(), task.getBit(), id);
				}
			}
			all_time = (System.currentTimeMillis() - StartTime);
			//System.out.println(id + ":" + (System.currentTimeMillis() - StartTime));
			return 0;
		}
		public void add(Task task){
			TaskQueue.add(task);
		}
		private void generate_from_node(int[] intent, int[] extent, int start_int, int start_bit, int id){
			int total = start_int * (ARCHBIT + 1) + (ARCHBIT - start_bit);
			int[] new_intent = generateInt(int_count_a);
			int[] new_extent = generateInt(int_count_o);
			for (; start_int < int_count_a; start_int++) {
				ATTR: for (; start_bit >= 0; start_bit--) {
					if (total >= attributes){return;}
					total++;
					if (((intent[start_int] & (BIT << start_bit))) != 0 || (supps[start_int][start_bit] < min_support)){continue;}
					
					int supp = compute_closure (new_intent, new_extent, extent, cols[start_int][start_bit]);
					if (((new_intent[start_int] ^ intent[start_int]) & upto_bit[start_bit]) != 0) continue;
					for (int i = 0; i < start_int; i++){
						if ((new_intent[i] ^ intent[i]) != 0){continue ATTR;}
					}
					if (supp < min_support){continue;}
					if ((new_intent[int_count_a - 1] & BIT) != 0) {continue;}
					
					printConcept(new_intent, new_extent, (start_int * ARCHBIT) + (ARCHBIT - start_bit));
					
					if (start_bit == 0){
						generate_from_node (new_intent, new_extent, start_int + 1, ARCHBIT, id);
					}else{
						generate_from_node (new_intent, new_extent, start_int, start_bit - 1, id);
					}
					new_intent = generateInt(int_count_a);
					new_extent = generateInt(int_count_o);
				}
				start_bit = ARCHBIT;
			}
		}

		private int compute_closure(int[] intent, int[] extent, int[] prev_extent, int[] atr_extent){

			//for(int i=0;i<int_count_a;++i){intent[i] = BIT_MAX;}
			Arrays.fill(intent, BIT_MAX);
			//for(int i=0;i<int_count_o;++i){extent[i] = 0;}
			Arrays.fill(extent, 0);

			if (atr_extent != null) {
				int supp = 0;
				for (int k = 0; k < int_count_o; ++k) {
					extent[k] = prev_extent[k] & atr_extent[k];
					if (extent[k] != 0){
						for (int l = 0; l <= ARCHBIT; ++l){//
							if ((extent[k] & (BIT << l)) != 0) {
								for (int i = 0, j = int_count_a * (k * (ARCHBIT + 1) + l); i < int_count_a; ++i, ++j){
									intent[i] &= context[j];
								}
								++supp;
							}
						}
					}
				}
				return supp;
			}
			else{
				for(int i=0;i<int_count_o;++i){extent[i] = BIT_MAX;}
				for (int j = 0; j < objects; ++j) {
					for (int i = 0; i < int_count_a; ++i)
						intent[i] &= context[int_count_a * j + i];
				}
				return 0;
			}
		}
	}
/*-------------------------------------------------------------------------------------------------------------------------------*/
/*-------------------------------------------------------------------------------------------------------------------------------*/
	//true:処理続行
	//false:処理終了
	final private boolean ifLoop(){
		for (int i = 1; i < threads; ++i) {
			if(!FairWorkers.get(i).ifIdle()){return true;}
		}
		if(!fairTasks.isEmpty()){return true;}//前提:全workerがidleである
		return false;
	}
	private final ConcurrentLinkedQueue<Task> fairTasks = new ConcurrentLinkedQueue<Task>();
	class FairWorker implements Callable<Integer>{
		long for_time = 0;
		float proc_time = 0;
		boolean idleFlag;//=false;
		boolean loopFlag;//=false;
		int missCnt = 0;
		long missTime = 0;
		int succCnt = 0;
		int id = 0;
		int conceptCount = 0;
		public FairWorker(int id){
			this.id = id;
		}
		public float getTime(){
			return proc_time;
		}
		public long getForTime(){
			return for_time;
		}
		public int getConceptCount(){
			return conceptCount;
		}
		public void postProcess(){
			double missT = 0.000001 * missTime;
			proc_time = (float)(for_time - missT);
			System.out.println("id:" + id + ", for_time: "+ for_time//起動から終了までの時間
					+ ", idleTime: " + missT//タスクの処理をしていない時間
					+ ", procTime: " + proc_time//実際の稼働時間
					);
		}
		final public boolean ifIdle(){return idleFlag;}
		public Integer call() throws InterruptedException{
			if(id == 0){call_master();}
			else{call_slave();}
			return 0;
		}
		public Integer call_master() throws InterruptedException {
			long forStartTime = System.currentTimeMillis();
			int[] new_intent = new int[int_count_a];
			int[] new_extent = new int[int_count_o];
			long startTime = 0;
			START: while(true){
				if(idleFlag){
					if(!ifLoop()){
						break;
					}
				}
				Task task = fairTasks.poll();
				if (task == null){
					startTime = System.nanoTime();
					idleFlag = true;
					missTime += (System.nanoTime() - startTime);
					continue;
				}
				//++succCnt;
				idleFlag = false;

				int start_int = task.getInt();
				int start_bit = task.getBit();
				final int[] intent = task.getIntent();
				final int[] extent = task.getExtent();
				int total = start_int * (ARCHBIT + 1) + (ARCHBIT - start_bit);
				for (; start_int < int_count_a; start_int++) {
					ATTR: for (; start_bit >= 0; --start_bit) {
						if (total >= attributes){

							continue START;
						}
						total++;
						if (((intent[start_int] & (BIT << start_bit))) != 0 || (supps[start_int][start_bit] < min_support)){continue;}

						int supp = compute_closure (new_intent, new_extent, extent, cols[start_int][start_bit]);
						if (((new_intent[start_int] ^ intent[start_int]) & upto_bit[start_bit]) != 0){continue;}
						for (int i = 0; i < start_int; i++){
							if ((new_intent[i] ^ intent[i]) != 0){continue ATTR;}
						}
						if (supp < min_support){continue;}
						//printConcept(new_intent, new_extent, id);
						++conceptCount;
						if ((new_intent[int_count_a - 1] & BIT) != 0) {continue;}
							
						int s_int = start_int, s_bit = start_bit - 1;
						if (start_bit == 0){
							s_int = start_int + 1;
							s_bit = ARCHBIT;
						}
						Task new_task = new Task(new_intent, new_extent, s_int, s_bit);

						fairTasks.offer(new_task);
						//タスクが生成できたので配列を新たに生成
						new_intent = generateInt(int_count_a);
						new_extent = generateInt(int_count_o);

					}
					start_bit = ARCHBIT;//start_bitをリセットしstart_intを1進める
				}
			}
			for_time = (System.currentTimeMillis() - forStartTime);
			for (int i = 1; i < threads; ++i) {
				FairWorkers.get(i).end_slave();
			}
			return 0;
		}
		
		public void end_slave(){loopFlag = false;}
		public Task getTask(){
			
			Task task = fairTasks.poll();
			long startTime = System.nanoTime();
			while(task == null && loopFlag){
				idleFlag = true;
				missTime += (System.nanoTime() - startTime);
				task = fairTasks.poll();
				startTime = System.nanoTime();
			}
			//if(loopFlag){missTime += (System.nanoTime() - startTime);}
			return task;
		}
		public Integer call_slave() {
			int[] new_intent = new int[int_count_a];
			int[] new_extent = new int[int_count_o];
			long forStartTime = System.currentTimeMillis();
			loopFlag = true;
			START: while(loopFlag){
				Task task = getTask();
				if(!loopFlag){break;}
				//++succCnt;
				idleFlag = false;
				//Task new_task = this.getVoidTask();
				//new_intent = new_task.getIntent();
				//new_extent = new_task.getExtent();
				int start_int = task.getInt();
				int start_bit = task.getBit();
				final int[] intent = task.getIntent();
				final int[] extent = task.getExtent();
				int total = start_int * (ARCHBIT + 1) + (ARCHBIT - start_bit);
				for (; start_int < int_count_a; ++start_int) {
					ATTR: for (; start_bit >= 0; --start_bit) {
						if (total >= attributes){
							continue START;
						}
						total++;
						if (((intent[start_int] & (BIT << start_bit))) != 0 || (supps[start_int][start_bit] < min_support)){continue;}

						int supp = compute_closure (new_intent, new_extent, extent, cols[start_int][start_bit]);
						if (((new_intent[start_int] ^ intent[start_int]) & upto_bit[start_bit]) != 0){continue;}
						for (int i = 0; i < start_int; ++i){
							if ((new_intent[i] ^ intent[i]) != 0){continue ATTR;}
						}
						if (supp < min_support){continue;}
						//printConcept(new_intent, new_extent, id);
						++conceptCount;
						if ((new_intent[int_count_a - 1] & BIT) != 0) {continue;}

						int s_int = start_int, s_bit = start_bit - 1;
						if (start_bit == 0){
							s_int = start_int + 1;
							s_bit = ARCHBIT;
						}
						Task new_task = new Task(new_intent, new_extent, s_int, s_bit);
						fairTasks.offer(new_task);
						//タスクが生成できたので配列を新たに生成
						new_intent = generateInt(int_count_a);
						new_extent = generateInt(int_count_o);
					}
					start_bit = ARCHBIT;//start_bitをリセットしstart_intを1進める
				}
			}
			for_time = (System.currentTimeMillis() - forStartTime);
			return 0;
		}
		private int compute_closure(int[] intent, int[] extent, int[] prev_extent, int[] atr_extent){

			//for(int i=0;i<int_count_a;++i){intent[i] = BIT_MAX;}
			Arrays.fill(intent, BIT_MAX);
			//for(int i=0;i<int_count_o;++i){extent[i] = 0;}
			Arrays.fill(extent, 0);
			if (atr_extent != null) {
				int supp = 0;
				for (int k = 0; k < int_count_o; ++k) {
					extent[k] = prev_extent[k] & atr_extent[k];
					if (extent[k] != 0){
						for (int l = 0; l <= ARCHBIT; ++l){//
							if ((extent[k] & (BIT << l)) != 0) {
								for (int i = 0, j = int_count_a * (k * (ARCHBIT + 1) + l); i < int_count_a; ++i, ++j){
									intent[i] &= context[j];
									
								}
								++supp;
							}
						}
					}
				}
				return supp;
			}
			else{
				for(int i=0;i<int_count_o;++i){extent[i] = BIT_MAX;}
				for (int j = 0; j < objects; ++j) {
					for (int i = 0; i < int_count_a; ++i)
						intent[i] &= context[int_count_a * j + i];
				}
				return 0;
			}
		}
	}
	/*--------------------------------------------------------------------------------------------------------------------------*/
	
	//true:処理続行
	//false:処理終了
	final private boolean ifLoop2(){
		for (int i = 1; i < threads; ++i) {
			if(!FairWorkers2.get(i).ifIdle()){return true;}
		}
		if(!fairTasks.isEmpty()){return true;}//前提:全workerがidleである
		return false;
	}
	class FairWorker2 implements Callable<Integer>{
		long for_time = 0;
		float proc_time = 0;
		boolean idleFlag;//=false;
		boolean loopFlag;//=false;
		int missCnt = 0;
		long missTime = 0;
		int succCnt = 0;
		int id = 0;
		int conceptCount = 0;
		public FairWorker2(int id){
			this.id = id;
		}
		public float getTime(){
			return proc_time;
		}
		public int getConceptCount(){
			return conceptCount;
		}
		public void postProcess(){
			double missT = 0.000001 * missTime;
			proc_time = (float)(for_time - missT);
			System.out.println("id:" + id + ", for_time: "+ for_time
					+ ", idleTime: " + missT
					+ ", procTime: " + proc_time
					);
		}
		final public boolean ifIdle(){return idleFlag;}
		public Integer call() throws InterruptedException{
			if(id == 0){call_master();}
			else{call_slave();}
			return 0;
		}
		public Integer call_master() throws InterruptedException {
			long forStartTime = System.currentTimeMillis();
			int[] new_intent = new int[int_count_a];
			int[] new_extent = new int[int_count_o];
			long startTime = 0;
			START: while(true){
				if(idleFlag){
					if(!ifLoop2()){
						break;
					}
				}
				Task task = fairTasks.poll();
				if (task == null){
					startTime = System.nanoTime();
					idleFlag = true;
					missTime += (System.nanoTime() - startTime);
					continue;
				}
				
				//++succCnt;
				idleFlag = false;

				int start_int = task.getInt();
				int start_bit = task.getBit();
				final int[] intent = task.getIntent();
				final int[] extent = task.getExtent();
				int pace = 1;
				if(task.getID() != 0){pace = threads;}
				int total = start_int * (ARCHBIT + 1) + (ARCHBIT - start_bit);
				if(task.getID() != 0){start_bit -= (task.getID() - 1);}//for-paraの場合開始点をずらす
				for (; start_int < int_count_a; start_int++) {
					ATTR: for (; start_bit >= 0; start_bit -= pace) {
						if (total >= attributes){

							continue START;
						}
						total+=pace;
						if (((intent[start_int] & (BIT << start_bit))) != 0 || (supps[start_int][start_bit] < min_support)){continue;}

						int supp = compute_closure (new_intent, new_extent, extent, cols[start_int][start_bit]);
						if (((new_intent[start_int] ^ intent[start_int]) & upto_bit[start_bit]) != 0){continue;}
						for (int i = 0; i < start_int; i++){
							if ((new_intent[i] ^ intent[i]) != 0){continue ATTR;}
						}
						if (supp < min_support){continue;}
						//printConcept(new_intent, new_extent, id);
						++conceptCount;
						if ((new_intent[int_count_a - 1] & BIT) != 0) {continue;}
							
						int s_int = start_int, s_bit = start_bit - 1;
						if (start_bit == 0){
							s_int = start_int + 1;
							s_bit = ARCHBIT;
						}
						for(int i=0;i<threads;++i){
							Task new_task = new Task(new_intent, new_extent, s_int, s_bit);
							new_task.setID(i+1);
							fairTasks.offer(new_task);
						}
						//タスクが生成できたので配列を新たに生成
						new_intent = generateInt(int_count_a);
						new_extent = generateInt(int_count_o);

					}
					start_bit = ARCHBIT;//start_bitをリセットしstart_intを1進める
					if(task.getID() != 0){start_bit -= (task.getID() - 1);}
				}
			}
			for_time = (System.currentTimeMillis() - forStartTime);
			for (int i = 1; i < threads; ++i) {
				FairWorkers2.get(i).end_slave();
			}
			return 0;
		}
		
		public void end_slave(){loopFlag = false;}
		public Task getTask(){
			
			Task task = fairTasks.poll();
			long startTime = System.nanoTime();
			while(task == null && loopFlag){
				idleFlag = true;
				missTime += (System.nanoTime() - startTime);
				task = fairTasks.poll();
				startTime = System.nanoTime();
			}
			//if(loopFlag){missTime += (System.nanoTime() - startTime);}
			return task;
		}
		public Integer call_slave() {
			int[] new_intent = new int[int_count_a];
			int[] new_extent = new int[int_count_o];
			long forStartTime = System.currentTimeMillis();
			loopFlag = true;
			START: while(loopFlag){
				Task task = getTask();
				if(!loopFlag){break;}
				//++succCnt;
				idleFlag = false;
				
				//Task new_task = this.getVoidTask();
				//new_intent = new_task.getIntent();
				//new_extent = new_task.getExtent();
				int start_int = task.getInt();
				int start_bit = task.getBit();
				final int[] intent = task.getIntent();
				final int[] extent = task.getExtent();
				int pace = 1;
				if(task.getID() != 0){pace = threads;}
				int total = start_int * (ARCHBIT + 1) + (ARCHBIT - start_bit);
				if(task.getID() != 0){start_bit -= (task.getID() - 1);}//for-paraの場合開始点をずらす
				for (; start_int < int_count_a; ++start_int) {
					ATTR: for (; start_bit >= 0; start_bit -= pace) {
						if (total >= attributes){
							continue START;
						}
						total+=pace;
						if (((intent[start_int] & (BIT << start_bit))) != 0 || (supps[start_int][start_bit] < min_support)){continue;}

						int supp = compute_closure (new_intent, new_extent, extent, cols[start_int][start_bit]);
						if (((new_intent[start_int] ^ intent[start_int]) & upto_bit[start_bit]) != 0){continue;}
						for (int i = 0; i < start_int; ++i){
							if ((new_intent[i] ^ intent[i]) != 0){continue ATTR;}
						}
						if (supp < min_support){continue;}
						//printConcept(new_intent, new_extent, id);
						++conceptCount;
						if ((new_intent[int_count_a - 1] & BIT) != 0) {continue;}

						int s_int = start_int, s_bit = start_bit - 1;
						if (start_bit == 0){
							s_int = start_int + 1;
							s_bit = ARCHBIT;
						}
						for(int i=0;i<threads;++i){
							Task new_task = new Task(new_intent, new_extent, s_int, s_bit);
							new_task.setID(i+1);
							fairTasks.offer(new_task);
						}
						
						//タスクが生成できたので配列を新たに生成
						new_intent = generateInt(int_count_a);
						new_extent = generateInt(int_count_o);
					}
					start_bit = ARCHBIT;//start_bitをリセットしstart_intを1進める
					if(task.getID() != 0){start_bit -= (task.getID() - 1);}
				}
			}
			for_time = (System.currentTimeMillis() - forStartTime);
			return 0;
		}
		private int compute_closure(int[] intent, int[] extent, int[] prev_extent, int[] atr_extent){

			//for(int i=0;i<int_count_a;++i){intent[i] = BIT_MAX;}
			Arrays.fill(intent, BIT_MAX);
			//for(int i=0;i<int_count_o;++i){extent[i] = 0;}
			Arrays.fill(extent, 0);

			if (atr_extent != null) {
				int supp = 0;
				for (int k = 0; k < int_count_o; ++k) {
					extent[k] = prev_extent[k] & atr_extent[k];
					if (extent[k] != 0){
						for (int l = 0; l <= ARCHBIT; ++l){//
							if ((extent[k] & (BIT << l)) != 0) {
								for (int i = 0, j = int_count_a * (k * (ARCHBIT + 1) + l); i < int_count_a; ++i, ++j){
									intent[i] &= context[j];
									
								}
								++supp;
							}
						}
					}
				}
				return supp;
			}
			else{
				for(int i=0;i<int_count_o;++i){extent[i] = BIT_MAX;}
				for (int j = 0; j < objects; ++j) {
					for (int i = 0; i < int_count_a; ++i)
						intent[i] &= context[int_count_a * j + i];
				}
				return 0;
			}
		}
	}
	/*---------------------------------------------------------------------------------------------------------------------------------*/
	/*---------------------------------------------------------------------------------------------------------------------------------*/
	/*---------------------------------------------------------------------------------------------------------------------------------*/
	final private boolean ifLoop3(){
		for (int i = 1; i < threads; ++i) {
			if(!FairWorkers0308.get(i).ifIdle()){return true;}
		}
		if(!fairTasks.isEmpty()){return true;}//前提:全workerがidleである
		return false;
	}

	class FairWorker0308 implements Callable<Integer>{
		long for_time = 0;
		float proc_time = 0;
		boolean idleFlag;//=false;
		boolean loopFlag;//=false;
		int missCnt = 0;
		long missTime = 0;
		int succCnt = 0;
		int id = 0;
		int conceptCount = 0;
		int taskCount = 0;
		public FairWorker0308(int id){
			this.id = id;
		}
		public float getTime(){
			return proc_time;
		}
		public long getForTime(){
			return for_time;
		}
		public int getConceptCount(){
			return conceptCount;
		}
		public void postProcess(){
			double missT = 0.000001 * missTime;
			proc_time = (float)(for_time - missT);
			System.out.println("id:" + id + ", for_time: "+ for_time
					+ ", idleTime: " + missT
					+ ", procTime: " + proc_time
					+ ", taskCount: " + taskCount
					);
		}
		final public boolean ifIdle(){return idleFlag;}
		public Integer call() throws InterruptedException{
			if(id == 0){call_master();}
			else{call_slave();}
			return 0;
		}
		public Integer call_master() throws InterruptedException {
			long forStartTime = System.currentTimeMillis();
			int[] new_intent = new int[int_count_a];
			int[] new_extent = new int[int_count_o];
			long startTime = 0;
			START: while(true){
				if(idleFlag){
					if(!ifLoop3()){
						break;
					}
				}
				Task task = fairTasks.poll();
				if (task == null){
					startTime = System.nanoTime();
					idleFlag = true;
					missTime += (System.nanoTime() - startTime);
					continue;
				}
				++taskCount;
				idleFlag = false;

				int start_int = task.getInt();
				int start_bit = task.getBit();
				final int[] intent = task.getIntent();
				final int[] extent = task.getExtent();
				if(task.isDoMyself()){
					generate_from_node(intent, extent, start_int, start_bit, id);
					continue;
				}
				int total = start_int * (ARCHBIT + 1) + (ARCHBIT - start_bit);
				for (; start_int < int_count_a; start_int++) {
					ATTR: for (; start_bit >= 0; --start_bit) {
						if (total >= attributes){

							continue START;
						}
						total++;
						if (((intent[start_int] & (BIT << start_bit))) != 0 || (supps[start_int][start_bit] < min_support)){continue;}

						int supp = compute_closure (new_intent, new_extent, extent, cols[start_int][start_bit]);
						if (((new_intent[start_int] ^ intent[start_int]) & upto_bit[start_bit]) != 0){continue;}
						for (int i = 0; i < start_int; i++){
							if ((new_intent[i] ^ intent[i]) != 0){continue ATTR;}
						}
						if (supp < min_support){continue;}
						//printConcept(new_intent, new_extent, id);
						++conceptCount;
						if ((new_intent[int_count_a - 1] & BIT) != 0) {continue;}
							
						int s_int = start_int, s_bit = start_bit - 1;
						if (start_bit == 0){
							s_int = start_int + 1;
							s_bit = ARCHBIT;
						}
						
						Task new_task = new Task(new_intent, new_extent, s_int, s_bit);
						if(checkDoMyself(start_int * (ARCHBIT + 1) + (ARCHBIT - start_bit))){
							//条件を満たした場合doMyselfをtrueにする
							new_task.doMyself();
						}
						fairTasks.offer(new_task);

						//タスクが生成できたので配列を新たに生成
						new_intent = generateInt(int_count_a);
						new_extent = generateInt(int_count_o);

					}
					start_bit = ARCHBIT;//start_bitをリセットしstart_intを1進める
				}
			}
			for_time = (System.currentTimeMillis() - forStartTime);
			for (int i = 1; i < threads; ++i) {
				FairWorkers0308.get(i).end_slave();
			}
			return 0;
		}
		
		public void end_slave(){loopFlag = false;}
		public Task getTask(){
			
			Task task = fairTasks.poll();
			long startTime = System.nanoTime();
			while(task == null && loopFlag){
				idleFlag = true;
				missTime += (System.nanoTime() - startTime);
				task = fairTasks.poll();
				startTime = System.nanoTime();
			}
			//if(loopFlag){missTime += (System.nanoTime() - startTime);}
			return task;
		}
		public Integer call_slave() {
			int[] new_intent = new int[int_count_a];
			int[] new_extent = new int[int_count_o];
			long forStartTime = System.currentTimeMillis();
			loopFlag = true;
			START: while(loopFlag){
				Task task = getTask();
				if(!loopFlag){break;}
				++taskCount;
				idleFlag = false;
				//Task new_task = this.getVoidTask();
				//new_intent = new_task.getIntent();
				//new_extent = new_task.getExtent();
				int start_int = task.getInt();
				int start_bit = task.getBit();
				final int[] intent = task.getIntent();
				final int[] extent = task.getExtent();
				if(task.isDoMyself()){
					generate_from_node(intent, extent, start_int, start_bit, id);
					continue;
				}
				int total = start_int * (ARCHBIT + 1) + (ARCHBIT - start_bit);
				for (; start_int < int_count_a; ++start_int) {
					ATTR: for (; start_bit >= 0; --start_bit) {
						if (total >= attributes){
							continue START;
						}
						total++;
						if (((intent[start_int] & (BIT << start_bit))) != 0 || (supps[start_int][start_bit] < min_support)){continue;}

						int supp = compute_closure (new_intent, new_extent, extent, cols[start_int][start_bit]);
						if (((new_intent[start_int] ^ intent[start_int]) & upto_bit[start_bit]) != 0){continue;}
						for (int i = 0; i < start_int; ++i){
							if ((new_intent[i] ^ intent[i]) != 0){continue ATTR;}
						}
						if (supp < min_support){continue;}
						//printConcept(new_intent, new_extent, id);
						++conceptCount;
						if ((new_intent[int_count_a - 1] & BIT) != 0) {continue;}

						int s_int = start_int, s_bit = start_bit - 1;
						if (start_bit == 0){
							s_int = start_int + 1;
							s_bit = ARCHBIT;
						}
						
						//タスクとしてキューに投げる
						Task new_task = new Task(new_intent, new_extent, s_int, s_bit);
						if(checkDoMyself(start_int * (ARCHBIT + 1) + (ARCHBIT - start_bit))){
							//条件を満たした場合doMyselfをtrueにする
							new_task.doMyself();
						}
						fairTasks.offer(new_task);
						
						//タスクが生成できたので配列を新たに生成
						new_intent = generateInt(int_count_a);
						new_extent = generateInt(int_count_o);
					}
					start_bit = ARCHBIT;//start_bitをリセットしstart_intを1進める
				}
			}
			for_time = (System.currentTimeMillis() - forStartTime);
			return 0;
		}
		private boolean checkDoMyself(int att){
			if(att > 0.1 * attributes){
				return true;
			}
			else{
				return false;
			}
		}
		private int compute_closure(int[] intent, int[] extent, int[] prev_extent, int[] atr_extent){

			//for(int i=0;i<int_count_a;++i){intent[i] = BIT_MAX;}
			Arrays.fill(intent, BIT_MAX);
			//for(int i=0;i<int_count_o;++i){extent[i] = 0;}
			Arrays.fill(extent, 0);
			if (atr_extent != null) {
				int supp = 0;
				for (int k = 0; k < int_count_o; ++k) {
					extent[k] = prev_extent[k] & atr_extent[k];
					if (extent[k] != 0){
						for (int l = 0; l <= ARCHBIT; ++l){//
							if ((extent[k] & (BIT << l)) != 0) {
								for (int i = 0, j = int_count_a * (k * (ARCHBIT + 1) + l); i < int_count_a; ++i, ++j){
									intent[i] &= context[j];
									
								}
								++supp;
							}
						}
					}
				}
				return supp;
			}
			else{
				for(int i=0;i<int_count_o;++i){extent[i] = BIT_MAX;}
				for (int j = 0; j < objects; ++j) {
					for (int i = 0; i < int_count_a; ++i)
						intent[i] &= context[int_count_a * j + i];
				}
				return 0;
			}
		}
	}
}

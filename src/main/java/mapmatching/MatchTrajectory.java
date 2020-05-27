package mapmatching;

import entity.CarPoint;
import entity.MatchedPoint;
import entity.RoadPoint;
import filer.TrajectoryFromFile;
import hmmLib.SequenceState;
import hmmLib.Transition;
import hmmLib.ViterbiAlgorithm;
import rtree.RTree;

import java.util.*;

import static calculator.Calculator.*;
import static rtree.GenerateTreeFromFile.generateRTreeFromCFW_output;
import static rtree.GetRoads.candidates;


/**
 * 
 * @author rth
 * input:一条轨迹
 * output:一条匹配后的轨迹
 */

public class MatchTrajectory {
	
	// 所有概率值算出来之后,还要取log对数值,然后才能用
	// 发射概率:GPS点匹配到候选匹配点的概率 包含在下面的emissionLogProbabilities中
	// 转移概率:上一时刻候选匹配点->当前时刻候选匹配点的概率 包含在下面的transitionLogProbabilities中
	@SuppressWarnings("unchecked")

	/**
	 *

	public static ArrayList<RoadPoint> matching(ArrayList<CarPoint> trajectory, double range, RTree tree)
	{
		@SuppressWarnings("rawtypes")
		// 引入Viterbi算法
				ViterbiAlgorithm viterbi = new ViterbiAlgorithm();
		
		CarPoint observation_t1=trajectory.get(0);			// 第一个时刻的车辆GPS

		// 第一个时刻的所有候选匹配点
		ArrayList<RoadPoint> candidates_t1 = getCandidates(observation_t1,range,tree);
		
		Map<RoadPoint,Double> emissionLogProbabilities_t1 = new LinkedHashMap<RoadPoint,Double>();
		// 这是一个Hash表,里面的元素是一个个<候选匹配点,发射概率取对数的log值>键值对

		double emissionLogProbability;

		for (RoadPoint candidate:candidates_t1)
		{

		    //System.out.println("1th moment.");

			// 每个候选匹配点的发射概率
			emissionLogProbability = Math.log(getObservationProbability_onlydistance(candidate,observation_t1));

             如果这一时刻的所有emissionLogProbability都是负无穷,hmm链就会断裂
            if (emissionLogProbability == Double.NEGATIVE_INFINITY)
                System.out.println("invalid value!!!!the Log value of emission Probability is "+emissionLogProbability);
            else
                System.out.println("the Log value of emission Probability is "+emissionLogProbability);

			emissionLogProbabilities_t1.put(candidate,emissionLogProbability);
		}

		if (observation_t1==null||candidates_t1.isEmpty()||emissionLogProbabilities_t1.isEmpty())
			System.out.println("The initial parameters of viterbi have invalid values");


		// 初始化viterbi算法
		viterbi.startWithInitialObservation(observation_t1, candidates_t1, emissionLogProbabilities_t1);
		
		// Viterbi的前向演算
		for (int i = 1;i<trajectory.size();i++) // 从第2个时刻开始,遍历整个行车轨迹
		{// host:这一时刻的车辆轨迹点(observation)

			//System.out.println(i+"th moment!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			CarPoint observation = trajectory.get(i);			// 第i+1个时刻的车辆GPS点

			// 这是一个Hash表,里面的元素是一个个<候选匹配点,发射概率取对数的log值>键值对
			Map<RoadPoint,Double> emissionLogProbabilities = new LinkedHashMap<RoadPoint,Double>();

			// 这是一个Hash表,里面的元素是一个个<上一时刻候选匹配点->当前时刻候选匹配点,对应的转移概率取对数的log值>键值对
			Map<Transition<RoadPoint>, Double> transitionLogProbabilities = new LinkedHashMap<Transition<RoadPoint>, Double>();

			Collection<RoadPoint> candidates = getCandidates(observation,range,tree);//取出这一时刻车辆轨迹点的候选匹配点

            //double emissionLogProbability; // 存储每个(轨迹点,候选匹配点)的发射概率,并对概率值取自然对数值,在下一层循环中求出
			
			// 下面这个for循环用来生成emissionLogProbabilities和transitionLogProbabilities
			// 遍历这一时刻的全体候选匹配点
			for (RoadPoint candidate:candidates)
			{
			 //host:这一时刻的每一个候选匹配点(state)

				//////////////////////////////////////////////emissionLogProbabilities/////////////////////////

				// 计算每个候选匹配点的发射概率,并对概率值取自然对数值
				emissionLogProbability = Math.log(getObservationProbability_onlydistance(candidate,observation));

				// 如果这一时刻的所有emissionLogProbability都是负无穷,hmm链就会断裂
                //if (emissionLogProbability == Double.NEGATIVE_INFINITY)
                    //System.err.println("invalid value!!!!the Log value of emission Probability is "+emissionLogProbability);
                //else
                 //   System.out.println("the Log value of emission Probability is "+emissionLogProbability);
				
				emissionLogProbabilities.put(candidate,emissionLogProbability);

				/////////////////////////////////////////////transitionLogProbabilities/////////////////////////

				//采用了PPT 2.3 Hidden Markov MM through noise and sparseness 中计算转移概率的方法

				Map<RoadPoint, Double> message = viterbi.getMessage(); //其中包含了上一时刻的候选state(候选匹配点)

				double o_ij = getDistance(observation,trajectory.get(i-1));//两个观测点之间的欧氏距离

                double r_ij; // 存储两个候选匹配点的欧式距离

				//这个链表用来记录:|相邻时刻观测点的欧式距离-相邻时刻候选匹配点的欧式距离|,和transitions形成对应关系
				ArrayList<Double> distance_diff = new ArrayList<Double>();

				//这个列表用于存储所有(上一时刻候选点,这一时刻候选点)的转移组合,和distance_diff形成对应关系
				ArrayList<Transition<RoadPoint>> transitions = new ArrayList<Transition<RoadPoint>>();

				try{

					for (Map.Entry<RoadPoint, Double> entry:message.entrySet())
					{
						//host:上一时刻的每个候选匹配点

						// 用于描述:上一时刻候选匹配点->当前时刻候选匹配点 的状态转移
						Transition<RoadPoint> transition = new Transition<RoadPoint>(entry.getKey(),candidate);

						transitions.add(transition);

						RoadPoint candidate_last = entry.getKey();//获取上一时刻的候选匹配点

						r_ij = getDistance(candidate,candidate_last);

						distance_diff.add(Math.abs(o_ij-r_ij)); // 计算d_t

						//host:上一时刻的每个候选匹配点
					}
				}
				catch (NullPointerException e)
				{
					ArrayList<RoadPoint> error = new ArrayList<RoadPoint>();

					// 构造带有无效标记的匹配点,然后加入到匹配结果
					error.add(new RoadPoint("No number",observation.id+":Map-Matching failed because HMM broke",-1,-1));

					// 返回这个错误结果,说拜拜
					return error;
				}
				// 遍历上一时刻的全体候选匹配点

				double beta = getAverage(distance_diff);//距离差的平均值就是贝塔

                double transitionLogProbability; //用于存储每个(上一时刻候选点,这一时刻候选点)的转移概率

                double d_t; //公式中的d_t

                double exponent; //公式中E的指数

                // 遍历distance_diff的同时遍历transitions
				for(int k=0;k<distance_diff.size();k++)
				{
                    //host:上一时刻的每个候选匹配点

					d_t = distance_diff.get(k);

					Transition<RoadPoint> transition = transitions.get(k);

					exponent = (-1.0)*d_t/beta;

					// 求出转移概率
					transitionLogProbability = Math.log((1.0)/beta*Math.pow(Math.E,exponent));

					// 记录转移概率
					transitionLogProbabilities.put(transition,transitionLogProbability);

                    //host:上一时刻的每个候选匹配点
				}

			//host:这个时刻的每一个候选匹配点
			}

			// 应对 HMM Broken的异常处理
			try
			{
				viterbi.nextStep(observation, candidates, emissionLogProbabilities, transitionLogProbabilities);
			}
			catch (IllegalStateException e)
			{
				ArrayList<RoadPoint> error = new ArrayList<RoadPoint>();

				// 构造带有无效标记的匹配点,然后加入到匹配结果
				error.add(new RoadPoint("No number",observation.id+":Map-Matching failed because HMM broke",-1,-1));

				// 返回这个错误结果,说拜拜
				return error;
			}

		// host:这个时刻的车辆轨迹点(observation)
		}
		
		List<SequenceState<RoadPoint, CarPoint, Transition<RoadPoint>>> sequence = viterbi.computeMostLikelySequence();

		//创建一个空链表准备装匹配后的轨迹
		ArrayList<RoadPoint> trajectory_matched = new ArrayList<RoadPoint>();

		// 提取匹配后的轨迹中的state（状态点）,也就是匹配上的RoadPoint观测点序列
		for (SequenceState<RoadPoint, CarPoint, Transition<RoadPoint>> step:sequence)
		{
			CarPoint carPoint = step.observation;

			RoadPoint roadPoint = step.state;

			// 构造带有描述的匹配点,然后加入到匹配结果中
			trajectory_matched.add(
					new RoadPoint(roadPoint.getRoadName(),carPoint.id+" "+carPoint.timestamp.toString()
							,roadPoint.getLongitude(),roadPoint.getLatitude()));
		}

		return trajectory_matched;
	}*/

	/**
	 *
	 * @param trajectory 某个车辆的轨迹序列
	 * @param range 抓路范围
	 * @param tree 2D-Rtree
	 * @return 匹配后的车辆轨迹序列
	 */
	public static ArrayList<MatchedPoint> match(ArrayList<CarPoint> trajectory, double range, RTree tree)
	{
		@SuppressWarnings("rawtypes")
		// 引入Viterbi算法
				ViterbiAlgorithm viterbi = new ViterbiAlgorithm();

		CarPoint observation_t1=trajectory.get(0);			// 第一个时刻的车辆GPS

		// 第一个时刻的所有候选匹配点
		//ArrayList<RoadPoint> candidates_t1 = getCandidates(observation_t1,range,tree);

		ArrayList<MatchedPoint> candidates_t1 = candidates(observation_t1,range,tree);	// 取第一个时刻的候选匹配点

		Map<MatchedPoint,Double> emissionLogProbabilities_t1 = new LinkedHashMap<MatchedPoint,Double>(); //用于记录第一个时刻的发射概率
		// 这是一个Hash表,里面的元素是一个个<候选匹配点,发射概率取对数的log值>键值对

		double emissionLogProbability;

		// 遍历第一个时刻的全体候选匹配点
		for (MatchedPoint candidate:candidates_t1)
		{
			// 每个候选匹配点的发射概率
			emissionLogProbability = Math.log(observationProbability_onlyDistance(candidate,observation_t1));

            /* 如果这一时刻的所有emissionLogProbability都是负无穷,hmm链就会断裂
            if (emissionLogProbability == Double.NEGATIVE_INFINITY)
                System.out.println("invalid value!!!!the Log value of emission Probability is "+emissionLogProbability);
            else
                System.out.println("the Log value of emission Probability is "+emissionLogProbability);*/

			emissionLogProbabilities_t1.put(candidate,emissionLogProbability);
		}

		if (observation_t1==null||candidates_t1.isEmpty()||emissionLogProbabilities_t1.isEmpty())
			System.out.println("The initial parameters of viterbi have invalid values");

		// 初始化viterbi算法
		viterbi.startWithInitialObservation(observation_t1, candidates_t1, emissionLogProbabilities_t1);

		// Viterbi的前向演算
		for (int i = 1;i<trajectory.size();i++) // 从第2个时刻开始,遍历整个行车轨迹
		{// host:这一时刻的车辆轨迹点(observation)

			//System.out.println(i+"th moment!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			CarPoint observation = trajectory.get(i);			// 第i+1个时刻的车辆GPS点

			// 这是一个Hash表,里面的元素是一个个< 轨迹点->候选匹配点 , 发射概率取对数的log值>键值对
			Map<MatchedPoint,Double> emissionLogProbabilities = new LinkedHashMap<MatchedPoint,Double>();

			// 这是一个Hash表,里面的元素是一个个< 上一时刻候选匹配点->当前时刻候选匹配点 ,对应的转移概率取对数的log值>键值对
			Map<Transition<MatchedPoint>, Double> transitionLogProbabilities = new LinkedHashMap<Transition<MatchedPoint>, Double>();

			ArrayList<MatchedPoint> candidates = candidates(observation,range,tree);//取出这一时刻车辆轨迹点的候选匹配点

			// 下面这个for循环用来生成emissionLogProbabilities和transitionLogProbabilities
			// 遍历这一时刻的全体候选匹配点
			for (MatchedPoint candidate:candidates)
			{
				//host:这一时刻的每一个候选匹配点(state)

				////////////////////////////////////发射概率部分 emissionLogProbabilities/////////////////////////

				// 计算每个候选匹配点的发射概率,并对概率值取自然对数值
				emissionLogProbability = Math.log(observationProbability_onlyDistance(candidate,observation));

				// 如果这一时刻的所有emissionLogProbability都是负无穷,hmm链就会断裂
				//if (emissionLogProbability == Double.NEGATIVE_INFINITY)
				//System.err.println("invalid value!!!!the Log value of emission Probability is "+emissionLogProbability);
				//else
				//   System.out.println("the Log value of emission Probability is "+emissionLogProbability);

				emissionLogProbabilities.put(candidate,emissionLogProbability);

				/////////////////////////////转移概率部分  transitionLogProbabilities/////////////////////////

				//采用了PPT 2.3 Hidden Markov MM through noise and sparseness 中计算转移概率的方法

				Map<MatchedPoint, Double> message = viterbi.getMessage(); //其中包含了上一时刻的候选state(候选匹配点)

				double o_ij = getDistance(observation,trajectory.get(i-1));//两个观测点之间的欧氏距离

				double r_ij; // 存储两个候选匹配点的欧式距离

				//这个链表用来记录:|相邻时刻观测点的欧式距离-相邻时刻候选匹配点的欧式距离|,和transitions形成对应关系
				ArrayList<Double> distance_diff = new ArrayList<Double>();

				//这个列表用于存储所有(上一时刻候选点,这一时刻候选点)的转移组合,和distance_diff形成对应关系
				ArrayList<Transition<MatchedPoint>> transitions = new ArrayList<Transition<MatchedPoint>>();

				try{

					for (Map.Entry<MatchedPoint, Double> entry:message.entrySet())
					{
						//host:上一时刻的每个候选匹配点

						// 用于描述:上一时刻候选匹配点->当前时刻候选匹配点 的状态转移
						Transition<MatchedPoint> transition = new Transition<MatchedPoint>(entry.getKey(),candidate);

						transitions.add(transition);

						MatchedPoint candidate_last = entry.getKey();//获取上一时刻的候选匹配点

						r_ij = getDistance(candidate,candidate_last);  // 计算这个转移组合的欧式距离

						distance_diff.add(Math.abs(o_ij-r_ij)); // 计算d_t

						//host:上一时刻的每个候选匹配点
					}
				}
				catch (NullPointerException e)
				{
					ArrayList<MatchedPoint> error = new ArrayList<MatchedPoint>();

					System.err.println("MatchTrajectory.match 351:HMM Broken,goodbye.");

					// 构造带有无效标记的匹配点,然后加入到匹配结果
					error.add(new MatchedPoint("nothing","nothing",new Date(),0,0,0));

					// 返回这个错误结果,说拜拜
					return error;
				}
				// 遍历上一时刻的全体候选匹配点

				double beta = getAverage(distance_diff);//距离差的平均值就是贝塔

				double transitionLogProbability; //用于存储每个(上一时刻候选点,这一时刻候选点)的转移概率

				double d_t; //公式中的d_t

				double exponent; //公式中E的指数

				// 遍历distance_diff的同时遍历transitions
				for(int k=0;k<distance_diff.size();k++)
				{
					//host:上一时刻的每个候选匹配点

					d_t = distance_diff.get(k);

					Transition<MatchedPoint> transition = transitions.get(k);

					exponent = (-1.0)*d_t/beta;

					// 求出转移概率
					transitionLogProbability = Math.log((1.0)/beta*Math.pow(Math.E,exponent));

					// 记录转移概率
					transitionLogProbabilities.put(transition,transitionLogProbability);

					//host:上一时刻的每个候选匹配点
				}

				//host:这个时刻的每一个候选匹配点
			}

			// 应对 HMM Broken的异常处理
			try
			{
				viterbi.nextStep(observation, candidates, emissionLogProbabilities, transitionLogProbabilities);
			}
			catch (IllegalStateException e)
			{
				ArrayList<MatchedPoint> error = new ArrayList<MatchedPoint>();

				System.err.println("MatchTrajectory.match 401:HMM Broken,goodbye.");

				// 构造带有无效标记的匹配点,然后加入到匹配结果
				error.add(new MatchedPoint("nothing","nothing",new Date(),0,0,0));

				// 返回这个错误结果,说拜拜
				return error;
			}

			// host:这个时刻的车辆轨迹点(observation)
		}

		List< SequenceState< MatchedPoint, CarPoint, Transition<MatchedPoint> > > sequence = viterbi.computeMostLikelySequence();

		//创建一个空链表准备装匹配后的轨迹
		ArrayList<MatchedPoint> trajectory_matched = new ArrayList<MatchedPoint>();

		// 提取匹配后的轨迹中的state（状态点）,也就是匹配上的MatchedPoint观测点序列
		for (SequenceState<MatchedPoint, CarPoint, Transition<MatchedPoint>> step:sequence)
		{
			trajectory_matched.add(step.state);
		}

		return trajectory_matched;
	}

	/**
	 *
	 * @param filePath 出租车轨迹的原始文件路径
	 * @return 多辆出租车匹配之后的轨迹
	 */
	public static ArrayList<ArrayList<RoadPoint>> getTrajectoriesAfterMatching(String filePath)
	{
		return getTrajectoriesAfterMatching(filePath,20);
	}

	/**
	 *
	 * @param filePath 出租车轨迹的原始文件路径
	 * @param range 抓路正方形的边长的一半,单位是m
	 * @return 多辆出租车匹配之后的轨迹
	 */
	public static ArrayList<ArrayList<RoadPoint>> getTrajectoriesAfterMatching(String filePath, double range)
	{
		// 多条原始行车轨迹
		ArrayList<ArrayList<CarPoint>> trajectories_raw = TrajectoryFromFile.multipleTrajectoriesFromFile(filePath);
		
		// 用来存储多条匹配后的轨迹
		ArrayList<ArrayList<RoadPoint>> trajectories_matched=new ArrayList<ArrayList<RoadPoint>>();

		RTree tree = generateRTreeFromCFW_output("D:\\daochu.txt");
		
		// 遍历每一条原始行车轨迹
		for (ArrayList<CarPoint> trajectory_raw:trajectories_raw)
		{
			// 得到一整条匹配后的轨迹
			//ArrayList<RoadPoint> trajectory_matched = MatchTrajectory.matching(trajectory_raw,range,tree);
			
			//trajectories_matched.add(trajectory_matched); // 将这一条匹配后的轨迹轨迹加入集合
		}
		
		return trajectories_matched;
	}

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub


	}

}

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
 * input:һ���켣
 * output:һ��ƥ���Ĺ켣
 */

public class MatchTrajectory {
	
	// ���и���ֵ�����֮��,��Ҫȡlog����ֵ,Ȼ�������
	// �������:GPS��ƥ�䵽��ѡƥ���ĸ��� �����������emissionLogProbabilities��
	// ת�Ƹ���:��һʱ�̺�ѡƥ���->��ǰʱ�̺�ѡƥ���ĸ��� �����������transitionLogProbabilities��
	@SuppressWarnings("unchecked")

	/**
	 *

	public static ArrayList<RoadPoint> matching(ArrayList<CarPoint> trajectory, double range, RTree tree)
	{
		@SuppressWarnings("rawtypes")
		// ����Viterbi�㷨
				ViterbiAlgorithm viterbi = new ViterbiAlgorithm();
		
		CarPoint observation_t1=trajectory.get(0);			// ��һ��ʱ�̵ĳ���GPS

		// ��һ��ʱ�̵����к�ѡƥ���
		ArrayList<RoadPoint> candidates_t1 = getCandidates(observation_t1,range,tree);
		
		Map<RoadPoint,Double> emissionLogProbabilities_t1 = new LinkedHashMap<RoadPoint,Double>();
		// ����һ��Hash��,�����Ԫ����һ����<��ѡƥ���,�������ȡ������logֵ>��ֵ��

		double emissionLogProbability;

		for (RoadPoint candidate:candidates_t1)
		{

		    //System.out.println("1th moment.");

			// ÿ����ѡƥ���ķ������
			emissionLogProbability = Math.log(getObservationProbability_onlydistance(candidate,observation_t1));

             �����һʱ�̵�����emissionLogProbability���Ǹ�����,hmm���ͻ����
            if (emissionLogProbability == Double.NEGATIVE_INFINITY)
                System.out.println("invalid value!!!!the Log value of emission Probability is "+emissionLogProbability);
            else
                System.out.println("the Log value of emission Probability is "+emissionLogProbability);

			emissionLogProbabilities_t1.put(candidate,emissionLogProbability);
		}

		if (observation_t1==null||candidates_t1.isEmpty()||emissionLogProbabilities_t1.isEmpty())
			System.out.println("The initial parameters of viterbi have invalid values");


		// ��ʼ��viterbi�㷨
		viterbi.startWithInitialObservation(observation_t1, candidates_t1, emissionLogProbabilities_t1);
		
		// Viterbi��ǰ������
		for (int i = 1;i<trajectory.size();i++) // �ӵ�2��ʱ�̿�ʼ,���������г��켣
		{// host:��һʱ�̵ĳ����켣��(observation)

			//System.out.println(i+"th moment!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			CarPoint observation = trajectory.get(i);			// ��i+1��ʱ�̵ĳ���GPS��

			// ����һ��Hash��,�����Ԫ����һ����<��ѡƥ���,�������ȡ������logֵ>��ֵ��
			Map<RoadPoint,Double> emissionLogProbabilities = new LinkedHashMap<RoadPoint,Double>();

			// ����һ��Hash��,�����Ԫ����һ����<��һʱ�̺�ѡƥ���->��ǰʱ�̺�ѡƥ���,��Ӧ��ת�Ƹ���ȡ������logֵ>��ֵ��
			Map<Transition<RoadPoint>, Double> transitionLogProbabilities = new LinkedHashMap<Transition<RoadPoint>, Double>();

			Collection<RoadPoint> candidates = getCandidates(observation,range,tree);//ȡ����һʱ�̳����켣��ĺ�ѡƥ���

            //double emissionLogProbability; // �洢ÿ��(�켣��,��ѡƥ���)�ķ������,���Ը���ֵȡ��Ȼ����ֵ,����һ��ѭ�������
			
			// �������forѭ����������emissionLogProbabilities��transitionLogProbabilities
			// ������һʱ�̵�ȫ���ѡƥ���
			for (RoadPoint candidate:candidates)
			{
			 //host:��һʱ�̵�ÿһ����ѡƥ���(state)

				//////////////////////////////////////////////emissionLogProbabilities/////////////////////////

				// ����ÿ����ѡƥ���ķ������,���Ը���ֵȡ��Ȼ����ֵ
				emissionLogProbability = Math.log(getObservationProbability_onlydistance(candidate,observation));

				// �����һʱ�̵�����emissionLogProbability���Ǹ�����,hmm���ͻ����
                //if (emissionLogProbability == Double.NEGATIVE_INFINITY)
                    //System.err.println("invalid value!!!!the Log value of emission Probability is "+emissionLogProbability);
                //else
                 //   System.out.println("the Log value of emission Probability is "+emissionLogProbability);
				
				emissionLogProbabilities.put(candidate,emissionLogProbability);

				/////////////////////////////////////////////transitionLogProbabilities/////////////////////////

				//������PPT 2.3 Hidden Markov MM through noise and sparseness �м���ת�Ƹ��ʵķ���

				Map<RoadPoint, Double> message = viterbi.getMessage(); //���а�������һʱ�̵ĺ�ѡstate(��ѡƥ���)

				double o_ij = getDistance(observation,trajectory.get(i-1));//�����۲��֮���ŷ�Ͼ���

                double r_ij; // �洢������ѡƥ����ŷʽ����

				//�������������¼:|����ʱ�̹۲���ŷʽ����-����ʱ�̺�ѡƥ����ŷʽ����|,��transitions�γɶ�Ӧ��ϵ
				ArrayList<Double> distance_diff = new ArrayList<Double>();

				//����б����ڴ洢����(��һʱ�̺�ѡ��,��һʱ�̺�ѡ��)��ת�����,��distance_diff�γɶ�Ӧ��ϵ
				ArrayList<Transition<RoadPoint>> transitions = new ArrayList<Transition<RoadPoint>>();

				try{

					for (Map.Entry<RoadPoint, Double> entry:message.entrySet())
					{
						//host:��һʱ�̵�ÿ����ѡƥ���

						// ��������:��һʱ�̺�ѡƥ���->��ǰʱ�̺�ѡƥ��� ��״̬ת��
						Transition<RoadPoint> transition = new Transition<RoadPoint>(entry.getKey(),candidate);

						transitions.add(transition);

						RoadPoint candidate_last = entry.getKey();//��ȡ��һʱ�̵ĺ�ѡƥ���

						r_ij = getDistance(candidate,candidate_last);

						distance_diff.add(Math.abs(o_ij-r_ij)); // ����d_t

						//host:��һʱ�̵�ÿ����ѡƥ���
					}
				}
				catch (NullPointerException e)
				{
					ArrayList<RoadPoint> error = new ArrayList<RoadPoint>();

					// ���������Ч��ǵ�ƥ���,Ȼ����뵽ƥ����
					error.add(new RoadPoint("No number",observation.id+":Map-Matching failed because HMM broke",-1,-1));

					// �������������,˵�ݰ�
					return error;
				}
				// ������һʱ�̵�ȫ���ѡƥ���

				double beta = getAverage(distance_diff);//������ƽ��ֵ���Ǳ���

                double transitionLogProbability; //���ڴ洢ÿ��(��һʱ�̺�ѡ��,��һʱ�̺�ѡ��)��ת�Ƹ���

                double d_t; //��ʽ�е�d_t

                double exponent; //��ʽ��E��ָ��

                // ����distance_diff��ͬʱ����transitions
				for(int k=0;k<distance_diff.size();k++)
				{
                    //host:��һʱ�̵�ÿ����ѡƥ���

					d_t = distance_diff.get(k);

					Transition<RoadPoint> transition = transitions.get(k);

					exponent = (-1.0)*d_t/beta;

					// ���ת�Ƹ���
					transitionLogProbability = Math.log((1.0)/beta*Math.pow(Math.E,exponent));

					// ��¼ת�Ƹ���
					transitionLogProbabilities.put(transition,transitionLogProbability);

                    //host:��һʱ�̵�ÿ����ѡƥ���
				}

			//host:���ʱ�̵�ÿһ����ѡƥ���
			}

			// Ӧ�� HMM Broken���쳣����
			try
			{
				viterbi.nextStep(observation, candidates, emissionLogProbabilities, transitionLogProbabilities);
			}
			catch (IllegalStateException e)
			{
				ArrayList<RoadPoint> error = new ArrayList<RoadPoint>();

				// ���������Ч��ǵ�ƥ���,Ȼ����뵽ƥ����
				error.add(new RoadPoint("No number",observation.id+":Map-Matching failed because HMM broke",-1,-1));

				// �������������,˵�ݰ�
				return error;
			}

		// host:���ʱ�̵ĳ����켣��(observation)
		}
		
		List<SequenceState<RoadPoint, CarPoint, Transition<RoadPoint>>> sequence = viterbi.computeMostLikelySequence();

		//����һ��������׼��װƥ���Ĺ켣
		ArrayList<RoadPoint> trajectory_matched = new ArrayList<RoadPoint>();

		// ��ȡƥ���Ĺ켣�е�state��״̬�㣩,Ҳ����ƥ���ϵ�RoadPoint�۲������
		for (SequenceState<RoadPoint, CarPoint, Transition<RoadPoint>> step:sequence)
		{
			CarPoint carPoint = step.observation;

			RoadPoint roadPoint = step.state;

			// �������������ƥ���,Ȼ����뵽ƥ������
			trajectory_matched.add(
					new RoadPoint(roadPoint.getRoadName(),carPoint.id+" "+carPoint.timestamp.toString()
							,roadPoint.getLongitude(),roadPoint.getLatitude()));
		}

		return trajectory_matched;
	}*/

	/**
	 *
	 * @param trajectory ĳ�������Ĺ켣����
	 * @param range ץ·��Χ
	 * @param tree 2D-Rtree
	 * @return ƥ���ĳ����켣����
	 */
	public static ArrayList<MatchedPoint> match(ArrayList<CarPoint> trajectory, double range, RTree tree)
	{
		@SuppressWarnings("rawtypes")
		// ����Viterbi�㷨
				ViterbiAlgorithm viterbi = new ViterbiAlgorithm();

		CarPoint observation_t1=trajectory.get(0);			// ��һ��ʱ�̵ĳ���GPS

		// ��һ��ʱ�̵����к�ѡƥ���
		//ArrayList<RoadPoint> candidates_t1 = getCandidates(observation_t1,range,tree);

		ArrayList<MatchedPoint> candidates_t1 = candidates(observation_t1,range,tree);	// ȡ��һ��ʱ�̵ĺ�ѡƥ���

		Map<MatchedPoint,Double> emissionLogProbabilities_t1 = new LinkedHashMap<MatchedPoint,Double>(); //���ڼ�¼��һ��ʱ�̵ķ������
		// ����һ��Hash��,�����Ԫ����һ����<��ѡƥ���,�������ȡ������logֵ>��ֵ��

		double emissionLogProbability;

		// ������һ��ʱ�̵�ȫ���ѡƥ���
		for (MatchedPoint candidate:candidates_t1)
		{
			// ÿ����ѡƥ���ķ������
			emissionLogProbability = Math.log(observationProbability_onlyDistance(candidate,observation_t1));

            /* �����һʱ�̵�����emissionLogProbability���Ǹ�����,hmm���ͻ����
            if (emissionLogProbability == Double.NEGATIVE_INFINITY)
                System.out.println("invalid value!!!!the Log value of emission Probability is "+emissionLogProbability);
            else
                System.out.println("the Log value of emission Probability is "+emissionLogProbability);*/

			emissionLogProbabilities_t1.put(candidate,emissionLogProbability);
		}

		if (observation_t1==null||candidates_t1.isEmpty()||emissionLogProbabilities_t1.isEmpty())
			System.out.println("The initial parameters of viterbi have invalid values");

		// ��ʼ��viterbi�㷨
		viterbi.startWithInitialObservation(observation_t1, candidates_t1, emissionLogProbabilities_t1);

		// Viterbi��ǰ������
		for (int i = 1;i<trajectory.size();i++) // �ӵ�2��ʱ�̿�ʼ,���������г��켣
		{// host:��һʱ�̵ĳ����켣��(observation)

			//System.out.println(i+"th moment!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			CarPoint observation = trajectory.get(i);			// ��i+1��ʱ�̵ĳ���GPS��

			// ����һ��Hash��,�����Ԫ����һ����< �켣��->��ѡƥ��� , �������ȡ������logֵ>��ֵ��
			Map<MatchedPoint,Double> emissionLogProbabilities = new LinkedHashMap<MatchedPoint,Double>();

			// ����һ��Hash��,�����Ԫ����һ����< ��һʱ�̺�ѡƥ���->��ǰʱ�̺�ѡƥ��� ,��Ӧ��ת�Ƹ���ȡ������logֵ>��ֵ��
			Map<Transition<MatchedPoint>, Double> transitionLogProbabilities = new LinkedHashMap<Transition<MatchedPoint>, Double>();

			ArrayList<MatchedPoint> candidates = candidates(observation,range,tree);//ȡ����һʱ�̳����켣��ĺ�ѡƥ���

			// �������forѭ����������emissionLogProbabilities��transitionLogProbabilities
			// ������һʱ�̵�ȫ���ѡƥ���
			for (MatchedPoint candidate:candidates)
			{
				//host:��һʱ�̵�ÿһ����ѡƥ���(state)

				////////////////////////////////////������ʲ��� emissionLogProbabilities/////////////////////////

				// ����ÿ����ѡƥ���ķ������,���Ը���ֵȡ��Ȼ����ֵ
				emissionLogProbability = Math.log(observationProbability_onlyDistance(candidate,observation));

				// �����һʱ�̵�����emissionLogProbability���Ǹ�����,hmm���ͻ����
				//if (emissionLogProbability == Double.NEGATIVE_INFINITY)
				//System.err.println("invalid value!!!!the Log value of emission Probability is "+emissionLogProbability);
				//else
				//   System.out.println("the Log value of emission Probability is "+emissionLogProbability);

				emissionLogProbabilities.put(candidate,emissionLogProbability);

				/////////////////////////////ת�Ƹ��ʲ���  transitionLogProbabilities/////////////////////////

				//������PPT 2.3 Hidden Markov MM through noise and sparseness �м���ת�Ƹ��ʵķ���

				Map<MatchedPoint, Double> message = viterbi.getMessage(); //���а�������һʱ�̵ĺ�ѡstate(��ѡƥ���)

				double o_ij = getDistance(observation,trajectory.get(i-1));//�����۲��֮���ŷ�Ͼ���

				double r_ij; // �洢������ѡƥ����ŷʽ����

				//�������������¼:|����ʱ�̹۲���ŷʽ����-����ʱ�̺�ѡƥ����ŷʽ����|,��transitions�γɶ�Ӧ��ϵ
				ArrayList<Double> distance_diff = new ArrayList<Double>();

				//����б����ڴ洢����(��һʱ�̺�ѡ��,��һʱ�̺�ѡ��)��ת�����,��distance_diff�γɶ�Ӧ��ϵ
				ArrayList<Transition<MatchedPoint>> transitions = new ArrayList<Transition<MatchedPoint>>();

				try{

					for (Map.Entry<MatchedPoint, Double> entry:message.entrySet())
					{
						//host:��һʱ�̵�ÿ����ѡƥ���

						// ��������:��һʱ�̺�ѡƥ���->��ǰʱ�̺�ѡƥ��� ��״̬ת��
						Transition<MatchedPoint> transition = new Transition<MatchedPoint>(entry.getKey(),candidate);

						transitions.add(transition);

						MatchedPoint candidate_last = entry.getKey();//��ȡ��һʱ�̵ĺ�ѡƥ���

						r_ij = getDistance(candidate,candidate_last);  // �������ת����ϵ�ŷʽ����

						distance_diff.add(Math.abs(o_ij-r_ij)); // ����d_t

						//host:��һʱ�̵�ÿ����ѡƥ���
					}
				}
				catch (NullPointerException e)
				{
					ArrayList<MatchedPoint> error = new ArrayList<MatchedPoint>();

					System.err.println("MatchTrajectory.match 351:HMM Broken,goodbye.");

					// ���������Ч��ǵ�ƥ���,Ȼ����뵽ƥ����
					error.add(new MatchedPoint("nothing","nothing",new Date(),0,0,0));

					// �������������,˵�ݰ�
					return error;
				}
				// ������һʱ�̵�ȫ���ѡƥ���

				double beta = getAverage(distance_diff);//������ƽ��ֵ���Ǳ���

				double transitionLogProbability; //���ڴ洢ÿ��(��һʱ�̺�ѡ��,��һʱ�̺�ѡ��)��ת�Ƹ���

				double d_t; //��ʽ�е�d_t

				double exponent; //��ʽ��E��ָ��

				// ����distance_diff��ͬʱ����transitions
				for(int k=0;k<distance_diff.size();k++)
				{
					//host:��һʱ�̵�ÿ����ѡƥ���

					d_t = distance_diff.get(k);

					Transition<MatchedPoint> transition = transitions.get(k);

					exponent = (-1.0)*d_t/beta;

					// ���ת�Ƹ���
					transitionLogProbability = Math.log((1.0)/beta*Math.pow(Math.E,exponent));

					// ��¼ת�Ƹ���
					transitionLogProbabilities.put(transition,transitionLogProbability);

					//host:��һʱ�̵�ÿ����ѡƥ���
				}

				//host:���ʱ�̵�ÿһ����ѡƥ���
			}

			// Ӧ�� HMM Broken���쳣����
			try
			{
				viterbi.nextStep(observation, candidates, emissionLogProbabilities, transitionLogProbabilities);
			}
			catch (IllegalStateException e)
			{
				ArrayList<MatchedPoint> error = new ArrayList<MatchedPoint>();

				System.err.println("MatchTrajectory.match 401:HMM Broken,goodbye.");

				// ���������Ч��ǵ�ƥ���,Ȼ����뵽ƥ����
				error.add(new MatchedPoint("nothing","nothing",new Date(),0,0,0));

				// �������������,˵�ݰ�
				return error;
			}

			// host:���ʱ�̵ĳ����켣��(observation)
		}

		List< SequenceState< MatchedPoint, CarPoint, Transition<MatchedPoint> > > sequence = viterbi.computeMostLikelySequence();

		//����һ��������׼��װƥ���Ĺ켣
		ArrayList<MatchedPoint> trajectory_matched = new ArrayList<MatchedPoint>();

		// ��ȡƥ���Ĺ켣�е�state��״̬�㣩,Ҳ����ƥ���ϵ�MatchedPoint�۲������
		for (SequenceState<MatchedPoint, CarPoint, Transition<MatchedPoint>> step:sequence)
		{
			trajectory_matched.add(step.state);
		}

		return trajectory_matched;
	}

	/**
	 *
	 * @param filePath ���⳵�켣��ԭʼ�ļ�·��
	 * @return �������⳵ƥ��֮��Ĺ켣
	 */
	public static ArrayList<ArrayList<RoadPoint>> getTrajectoriesAfterMatching(String filePath)
	{
		return getTrajectoriesAfterMatching(filePath,20);
	}

	/**
	 *
	 * @param filePath ���⳵�켣��ԭʼ�ļ�·��
	 * @param range ץ·�����εı߳���һ��,��λ��m
	 * @return �������⳵ƥ��֮��Ĺ켣
	 */
	public static ArrayList<ArrayList<RoadPoint>> getTrajectoriesAfterMatching(String filePath, double range)
	{
		// ����ԭʼ�г��켣
		ArrayList<ArrayList<CarPoint>> trajectories_raw = TrajectoryFromFile.multipleTrajectoriesFromFile(filePath);
		
		// �����洢����ƥ���Ĺ켣
		ArrayList<ArrayList<RoadPoint>> trajectories_matched=new ArrayList<ArrayList<RoadPoint>>();

		RTree tree = generateRTreeFromCFW_output("D:\\daochu.txt");
		
		// ����ÿһ��ԭʼ�г��켣
		for (ArrayList<CarPoint> trajectory_raw:trajectories_raw)
		{
			// �õ�һ����ƥ���Ĺ켣
			//ArrayList<RoadPoint> trajectory_matched = MatchTrajectory.matching(trajectory_raw,range,tree);
			
			//trajectories_matched.add(trajectory_matched); // ����һ��ƥ���Ĺ켣�켣���뼯��
		}
		
		return trajectories_matched;
	}

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub


	}

}

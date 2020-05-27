package mapmatching;

import entity.RoadPoint;

import java.util.ArrayList;

public class Test1 {

	
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		
		// 用来存储多条匹配后的轨迹
		ArrayList<ArrayList<RoadPoint>> trajectories_matched = MatchTrajectory.getTrajectoriesAfterMatching(
				"D:\\学习！！！！！！！！！\\BigData\\课程设计\\data-gps\\taxi_gps\\2017-03-07.gz\\part-m-00000\\part-m-00000");
		// 第二个参数是抓路正方形的边长的一半,默认值是20,单位是m


		
	}

}

package mapmatching;

import entity.CarPoint;
import entity.MatchedPoint;
import filer.TrajectoryFromFile;
import rtree.RTree;

import java.util.ArrayList;

import static mapmatching.MatchTrajectory.match;
import static rtree.GenerateTreeFromFile.generateRTreeFromCFW_output;
import static rtree.TransformToCSV.trajectoryToCSV_afterMatching;
import static rtree.TransformToCSV.trajectoryToCSV_beforeMatching;

public class Test
{
    public static void main(String[] args)
    {
        // ��ԭʼ��������ȡ������ԭʼ�г��켣
        ArrayList<ArrayList<CarPoint>> trajectories_raw = TrajectoryFromFile.multipleTrajectoriesFromFile(
                "C:\\Users\\hp\\Desktop\\guiji_test.txt");

        // ��ȡ��һ���켣(�����Ĺ켣��
        ArrayList<CarPoint> trajectory_raw = trajectories_raw.get(4);

        //�����������ڵ�ͼ��������������ץ·���̣�
        RTree tree = generateRTreeFromCFW_output("C:\\Users\\hp\\Desktop\\node.txt");

        //tree.paintingPostOrder(tree.getFile().readNode(0));

        //���ƥ��
        ArrayList<MatchedPoint> trajectory_matched = match(trajectory_raw,200,tree);

        trajectoryToCSV_beforeMatching("D:\\before_matching.csv",trajectory_raw);

        trajectoryToCSV_afterMatching("D:\\after_matching3.csv",trajectory_matched);

    }
}

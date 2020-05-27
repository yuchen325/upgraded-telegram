package rtree;

import entity.CarPoint;
import entity.RoadPoint;
import entity.MatchedPoint;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;

import static calculator.Calculator.getCatchRectangle;
import static calculator.Calculator.getDistance;

public class GetRoads
{

    private static final long serialVersionUID = 1362045661105216022L;
    /**
     *
     * @param car_point һ���г��켣��
     * @param range     range��ץ·���ε�1/2���߳�,��λ:m
     * @param tree      2D-RTree
     * @return  ��·�κ�ѡ����ɵļ���

    public static ArrayList<RoadPoint> getCandidates(CarPoint car_point, double range, RTree tree)
    {
        ArrayList<RoadPoint> candidates = new ArrayList<RoadPoint>(); //���ڼ�¼·�κ�ѡ��

        Rectangle catchRectangle = getCatchRectangle(car_point,range);//��ȡץ·����

        RTNode root = tree.file.readNode(0);   // ��ȡ2D-Rtree��root�ڵ�

        //��ȡ��ѡ·�ξ�����ɵ��б�
        ArrayList<Data> candidateRoads = tree.intersection_Rectangles(catchRectangle,root);

        if (candidateRoads.size()<1)
            System.err.println("candidate roads does not exist!!!");

        //�������к�ѡ·��
        for (Data candidateRoad:candidateRoads)
        {
            Rectangle candidateRectangle = candidateRoad.mbr;   //��ȡ������ѡ·�ε�MBR��С�������

            ArrayList<RoadPoint> road_points = candidateRectangle.getRoad(); //ȡ����·�ε����й켣��

            // �μ� <<���2019.12.21 Map-Matching����1�ĸĽ�>>
            double min_distance=Double.POSITIVE_INFINITY;

            double distance;               // ���ڼ�¼����

            RoadPoint candidate = new RoadPoint("",0,0); // ���ڼ�¼��ѡ��

            for(RoadPoint roadpoint:road_points)      //��������·�е����е�
            {
                distance=getDistance(roadpoint,car_point); // ��¼�켣����·�ε�ľ���

                if (distance<min_distance)      //������߾������
                {
                    min_distance=distance;      //���������̾���

                    candidate.setRoadName(roadpoint.getRoadName());    // ��¼���·��ĵ�·���

                    candidate.setLongitude(roadpoint.getLongitude());   // ��¼���·��ľ���

                    candidate.setLatitude(roadpoint.getLatitude());    // ��¼���·���γ��
                }
            }

            candidates.add(candidate); // candidate�����������Ǹ�·�ε�
        }

        return candidates;  // ����·�κ�ѡ����ɵľۺ�
    }*/

    /**
     * �켣��:��ѡƥ��·��=1:n & ��ѡƥ��·��:��ѡƥ���=1:1 => �켣��:��ѡƥ��� = 1:n
     * @param car_point ��ǰʱ�̵Ĺ켣��
     * @param range ץ·��Χ
     * @param tree 2D-Rtree
     * @return ��ǰʱ�̵ĺ�ѡƥ�����ɵ�ArrayList
     */
    public static ArrayList<MatchedPoint> candidates(CarPoint car_point, double range, RTree tree)
    {
        ArrayList<MatchedPoint> c = new ArrayList<MatchedPoint>();  // ����װ��ѡ·��ƥ���

        Rectangle catchRectangle = getCatchRectangle(car_point,range);//��ȡץ·����

        RTNode root = tree.file.readNode(0);   // ��ȡ2D-Rtree��root�ڵ�

        //��ȡ��ѡ·�ξ�����ɵ��б�
        ArrayList<Data> candidateRoads = tree.intersection_Rectangles(catchRectangle,root);

        if (candidateRoads.size()<1)
            System.err.println("GetRoads.candidates:candidate roads does not exist!!!");

        String car_id = car_point.id;   //ȡ���ƺ�

        Date timestamp = car_point.timestamp;   //ȡʱ���

        //�������к�ѡ·��
        for (Data candidateRoad:candidateRoads)
        {
            Rectangle candidateRectangle = candidateRoad.mbr;   //��ȡ������ѡ·�ε�MBR��С�������(·��GPS����������)

            ArrayList<RoadPoint> road_points = candidateRectangle.getRoad(); //ȡ����·�ε�����GPS��

            // �μ� <<���2019.12.21 Map-Matching����1�ĸĽ�>>
            double min_distance = Double.POSITIVE_INFINITY;

            double distance = 0;               // ���ڼ�¼����

            double longitude = 0;

            double latitude = 0;

            String road_id = "";

            for(RoadPoint roadpoint:road_points)      //��������·�е����е�
            {
                distance=getDistance(roadpoint,car_point); // ����켣����·�ε�ľ���

                if (distance<min_distance)      //������߾������
                {
                    min_distance=distance;      //���������̾���

                    longitude = roadpoint.getLongitude(); // ���������ľ���

                    latitude = roadpoint.getLatitude(); // ����������γ��

                    road_id = roadpoint.getRoadName(); //   ���������������·
                }
            }

            c.add(new MatchedPoint(road_id,car_id,timestamp,longitude,latitude,distance));
        }

        return c;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
        CarPoint c = new CarPoint(113.989403,22.6695);//�����Ĺ켣��

        ////////////////////////////////////// ԭ����/////////////////////////////////////////////////////
        /*RTree tree_original = generateRTreeFromCFW_output("D:\\daochu.txt");

        ArrayList<RoadPoint> candidates = getCandidates(c,20,tree_original); // ���ر߳���Χ40m�������ڵĺ�ѡƥ���

        System.out.println("tree_original:all matching candidates for this point are shown below");

        // ������ѡƥ���
        for (RoadPoint candidate:candidates)
        {
            System.out.println("road number:"+candidate.getRoadName()+" coordinate:"+candidate.getLongitude()+","+candidate.getLatitude());
        }

        System.out.println("tree_original:I have "+tree_original.getTreeLevel()+" levels.");

        PageFile file_original = tree_original.getFile();

        // ���л�pageFile
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("D:\\stuff\\file1"));
        oos.writeObject(file_original);
        oos.close();
*/
        /////////////////////////////������/////////////////////////////////////////////////////////////////

        // �����л�,��������PageFile
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("D:\\stuff\\tree3"));
        RTree tree_regenerate = (RTree)ois.readObject();

        System.out.println(tree_regenerate.toString());

        System.out.println("tree_regenerate:I have "+tree_regenerate.getTreeLevel()+" levels.");


    }
}

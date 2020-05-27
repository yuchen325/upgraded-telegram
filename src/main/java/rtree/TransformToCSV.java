package rtree;

import entity.CarPoint;
import entity.MatchedPoint;
import entity.RoadPoint;
import filer.TrajectoryFromFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TransformToCSV
{
    /**
     * ���������ץ·����֮�ڵĺ�ѡ·�ε�GPS��д��һ��CSV�ļ���
     * @param filePath ��������ļ�·��
     * @param catchRectangle ץ·����
     * @param tree 2D-Rtree
     */
    public static void candidateRoadToCSV(String filePath, Rectangle catchRectangle, RTree tree)
    {
        RTNode root = tree.file.readNode(0);   // ��ȡ2D-Rtree��root�ڵ�

        ArrayList<Data> candidateRoads = tree.intersection_Rectangles(catchRectangle,root);//��ȡ��ѡ·�ξ�����ɵ��б�

        try { // ��ֹ�ļ��������ȡʧ�ܣ���catch��׽���󲢴�ӡ��Ҳ����throw

            String line = "";
            /* д��Txt�ļ� */
            File writename = new File(filePath); // ���·�������û����Ҫ����һ���µ�output��txt�ļ�
            writename.createNewFile(); // �������ļ�
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));

            //�������к�ѡ·��
            for (Data candidateRoad:candidateRoads)
            {
                Rectangle candidateRectangle = candidateRoad.mbr;   //��ȡ������ѡ·�ε�MBR��С�������

                ArrayList<RoadPoint> road_points = candidateRectangle.getRoad(); //ȡ����·�ε����й켣��

                for(RoadPoint roadpoint:road_points)      //��������·�е����е�
                {
                    out.write(roadpoint.toString()+"\n");
                }
            }

            out.close(); // ���ǵùر��ļ�

            } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * ��������������켣�е�GPS��д��һ��CSV�ļ���
     * @param filePath ����ļ�·��
     * @param trajectory �����켣
     */
    public static void trajectoryToCSV_beforeMatching(String filePath, ArrayList<CarPoint> trajectory)
    {
        try { // ��ֹ�ļ��������ȡʧ�ܣ���catch��׽���󲢴�ӡ��Ҳ����throw

            String line = "";
            /* д��Txt�ļ� */
            File writename = new File(filePath); // ���·�������û����Ҫ����һ���µ�output��txt�ļ�
            writename.createNewFile(); // �������ļ�
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));

            //�������к�ѡ·��
            for (CarPoint carPoint:trajectory)
            {
                out.write(carPoint.toString()+"\n");
            }

            out.close(); // ���ǵùر��ļ�

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*public static void trajectoryToCSV_afterMatching(String filePath, ArrayList<RoadPoint> trajectory)
    {
        try { // ��ֹ�ļ��������ȡʧ�ܣ���catch��׽���󲢴�ӡ��Ҳ����throw

            String line = "";

            File writename = new File(filePath); // ���·�������û����Ҫ����һ���µ�output��txt�ļ�
            writename.createNewFile(); // �������ļ�
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));

            //�������к�ѡ·��
            for (RoadPoint roadPoint:trajectory)
            {
                out.write(roadPoint.toString()+"\n");
            }

            out.close(); // ���ǵùر��ļ�

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }*/

    public static void trajectoryToCSV_afterMatching(String filePath, ArrayList<MatchedPoint> trajectory)
    {
        try { // ��ֹ�ļ��������ȡʧ�ܣ���catch��׽���󲢴�ӡ��Ҳ����throw

            String line = "";
            /* д��Txt�ļ� */
            File writename = new File(filePath); // ���·�������û����Ҫ����һ���µ�output��txt�ļ�
            writename.createNewFile(); // �������ļ�
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));

            //�������к�ѡ·��
            for (MatchedPoint roadPoint:trajectory)
            {
                out.write(roadPoint.toString()+"\n");
            }

            out.close(); // ���ǵùر��ļ�

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        ArrayList<ArrayList<CarPoint>> trajectories_raw = TrajectoryFromFile.multipleTrajectoriesFromFile(
                "D:\\data-gps\\taxi_gps\\2017-03-07.gz\\part-m-00000\\part-m-00000");

        System.out.println("��������"+trajectories_raw.size()+"�����Ĺ켣");

        System.out.println("������������һ���켣������,����Ĺ켣���ǰ���ʱ��������");

        ArrayList<CarPoint> trajectory = trajectories_raw.get(0);

        for(CarPoint c:trajectory)
            System.out.println(c.toString());

    }
}

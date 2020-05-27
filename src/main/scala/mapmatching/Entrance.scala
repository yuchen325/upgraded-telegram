package mapmatching

import java.util

import entity.{CarPoint, MatchedPoint, RoadPoint}
import rtree.RTree

import scala.collection.mutable.ArrayBuffer

/**
 * Spark�ڴ˴��뵥����Map-Matching�Խ�
 */
object Entrance
{
  /**
   * Ϊrdd.mapValue()׼���ĺ���,ǰ������:rdd�е�Ԫ�ر�����<���ƺ�,������Array[CarPoint]>�ļ�ֵ����ʽ
   * @param trajectory rdd <k,v>�е�value,Ҳ����һ����ʱ����������Ĺ켣,����:Array[CarPoint]
   * @param range ץ·��Χ,��λ:m
   * @param tree 2D-RTree
   * @return
   */
  def matchingEntranceForSpark(trajectory: Array[CarPoint],range: Double,tree:RTree): Array[MatchedPoint] =
  {
    val trajectory_raw = new util.ArrayList[CarPoint]() //���ڴ洢ƥ��ǰ�ĳ����켣

    // ����һ������ת��: Array[CarPoint] -> ArrayList<CarPoint> Ŀ����Ϊ����matching�����Խ�
    for (i <- trajectory)
      {
        trajectory_raw.add(i)
      }

    // scala�������ص�֮һ:�Զ����غ��������һ������ֵ
    val raw = MatchTrajectory.`match`(trajectory_raw,range,tree)

    val result = ArrayBuffer[MatchedPoint]()

    for (i <- 0 to raw.size()-1)
      {
        result.append(raw.get(i))
      }

    result.toArray
  }

}

package mapmatching

import java.util

import entity.{CarPoint, MatchedPoint, RoadPoint}
import rtree.RTree

import scala.collection.mutable.ArrayBuffer

/**
 * Spark在此处与单机版Map-Matching对接
 */
object Entrance
{
  /**
   * 为rdd.mapValue()准备的函数,前提条件:rdd中的元素必须是<车牌号,排序后的Array[CarPoint]>的键值对形式
   * @param trajectory rdd <k,v>中的value,也就是一条在时间轴上有序的轨迹,类型:Array[CarPoint]
   * @param range 抓路范围,单位:m
   * @param tree 2D-RTree
   * @return
   */
  def matchingEntranceForSpark(trajectory: Array[CarPoint],range: Double,tree:RTree): Array[MatchedPoint] =
  {
    val trajectory_raw = new util.ArrayList[CarPoint]() //用于存储匹配前的车辆轨迹

    // 做了一个类型转换: Array[CarPoint] -> ArrayList<CarPoint> 目的是为了与matching函数对接
    for (i <- trajectory)
      {
        trajectory_raw.add(i)
      }

    // scala函数的特点之一:自动返回函数体最后一个语句的值
    val raw = MatchTrajectory.`match`(trajectory_raw,range,tree)

    val result = ArrayBuffer[MatchedPoint]()

    for (i <- 0 to raw.size()-1)
      {
        result.append(raw.get(i))
      }

    result.toArray
  }

}

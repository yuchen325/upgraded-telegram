package rtree;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

// 用于描述位于内存中的page文件对象
public class MemoryPageFile extends PageFile // PageFile的子嗣之一
{
	private static Logger logger = Logger.getLogger(MemoryPageFile.class); //类成员1,这是一个日志记录器

	private Map<Integer, RTNode> file = new HashMap<Integer, RTNode>(); // 成员变量1,这是一个由一个个<Integer, RTNode>键值对组成的Map

	/**
	 * 成员变量1
	 * 初始化这个对象
	 * @param tree  一颗R-tree？
	 * @param dimension 树的维度
	 * @param fillFactor 填充因子
	 * @param capacity 树的容量?
	 * @param treeType 树的类型
	 */
	protected void initialize(RTree tree, int dimension, float fillFactor,
			int capacity, int treeType)
	{
		super.initialize(tree, dimension, fillFactor, capacity, treeType);
		file.clear();
	}

	/**
	 * 用于读取R-tree节点
	 * @param page 页编号
	 * @return 一个r-tree节点
	 * @throws PageFaultError
	 */
	@Override
	public RTNode readNode(int page) throws PageFaultError
	{
		if (page < 0) {
			logger.error("Page number cannot be negative.",
					new IllegalArgumentException(
							"Page number cannot be negative."));
			throw new IllegalArgumentException(
					"Page number cannot be negative.");
		}

		RTNode ret = file.get(page);

		if (ret == null)
		{
			logger.error("Invalid page number request.", new PageFaultError("Invalid page number request."));
			throw new PageFaultError("Invalid page number request.");
		}

		return ret;
	}

	/**
	 * 将节点写入page
	 * @param node 要写入的节点
	 * @return 节点的page编号
	 * @throws PageFaultError
	 */
	@Override
	protected int writeNode(RTNode node) throws PageFaultError {
		if (node == null) {
			throw new IllegalArgumentException("Node cannot be null.");
		}

		/*
		 * 如果node结点所在的pageNumber < 0,则从缓存文件Map中从0开始查找第一个没使用的Key作为存储的索引
		 * 如果node结点所在的pageNumber >= 0，则直接取出其pageNumber作为存储的Key
		 */
		int i = 0;
		if (node.pageNumber < 0) {
			while (true) {
				if (!file.containsKey(i)) {
					node.pageNumber = i;//新加的
					break;
				}
				i++;
			}
		} else {
			i = node.pageNumber;
		}

		file.put(i, node);

		return i;
	}

	/**
	 * 删除一个page,并返回它存储的节点
	 * @param page page编号
	 * @return page存储的节点
	 * @throws PageFaultError
	 */
	@Override
	protected RTNode deletePage(int page) throws PageFaultError {
		return file.remove(page);
	}

	/**
	 * 据说是:抛出内存中的内容????
	 */
	public void dumpMemory()
	{
		for (RTNode n : file.values()) {
			System.out.println(n);
		}
	}
}

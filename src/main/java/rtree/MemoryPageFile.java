package rtree;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

// ��������λ���ڴ��е�page�ļ�����
public class MemoryPageFile extends PageFile // PageFile������֮һ
{
	private static Logger logger = Logger.getLogger(MemoryPageFile.class); //���Ա1,����һ����־��¼��

	private Map<Integer, RTNode> file = new HashMap<Integer, RTNode>(); // ��Ա����1,����һ����һ����<Integer, RTNode>��ֵ����ɵ�Map

	/**
	 * ��Ա����1
	 * ��ʼ���������
	 * @param tree  һ��R-tree��
	 * @param dimension ����ά��
	 * @param fillFactor �������
	 * @param capacity ��������?
	 * @param treeType ��������
	 */
	protected void initialize(RTree tree, int dimension, float fillFactor,
			int capacity, int treeType)
	{
		super.initialize(tree, dimension, fillFactor, capacity, treeType);
		file.clear();
	}

	/**
	 * ���ڶ�ȡR-tree�ڵ�
	 * @param page ҳ���
	 * @return һ��r-tree�ڵ�
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
	 * ���ڵ�д��page
	 * @param node Ҫд��Ľڵ�
	 * @return �ڵ��page���
	 * @throws PageFaultError
	 */
	@Override
	protected int writeNode(RTNode node) throws PageFaultError {
		if (node == null) {
			throw new IllegalArgumentException("Node cannot be null.");
		}

		/*
		 * ���node������ڵ�pageNumber < 0,��ӻ����ļ�Map�д�0��ʼ���ҵ�һ��ûʹ�õ�Key��Ϊ�洢������
		 * ���node������ڵ�pageNumber >= 0����ֱ��ȡ����pageNumber��Ϊ�洢��Key
		 */
		int i = 0;
		if (node.pageNumber < 0) {
			while (true) {
				if (!file.containsKey(i)) {
					node.pageNumber = i;//�¼ӵ�
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
	 * ɾ��һ��page,���������洢�Ľڵ�
	 * @param page page���
	 * @return page�洢�Ľڵ�
	 * @throws PageFaultError
	 */
	@Override
	protected RTNode deletePage(int page) throws PageFaultError {
		return file.remove(page);
	}

	/**
	 * ��˵��:�׳��ڴ��е�����????
	 */
	public void dumpMemory()
	{
		for (RTNode n : file.values()) {
			System.out.println(n);
		}
	}
}

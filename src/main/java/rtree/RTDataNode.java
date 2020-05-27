package rtree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * �̳���Rt-node
 * @ClassName RTDataNode
 * @Description
 */
public class RTDataNode extends RTNode implements Serializable
{
	/**
	 * �������췽��
	 * @param tree
	 * @param parent
	 * @param pageNumber
	 */
	public RTDataNode(RTree tree, int parent, int pageNumber) {
		super(tree, parent, pageNumber, 0);// Ҷ�ӽ��Ĭ������0��
	}
	public RTDataNode(RTree tree, int parent) {
		super(tree, parent, -1, 0);// �ȴ���һ��Ĭ��ֵ������-1����ΪpageNumber
	}


	/**
	 * ��Ҷ�ڵ��в���Rectangle�����������丸�ڵ㲻Ϊ������Ҫ���ϵ�����ֱ�����ڵ㣻<br>
	 * ������Rectangle֮�󳬹������������Ҫ���ѽ��
	 * 
	 * @param rectangle
	 * @return rectangle�������Ҷ�ڵ��pageNumber
	 */
	public int insert(Rectangle rectangle, int page) {
		if (usedSpace < tree.getNodeCapacity()) {
			datas[usedSpace] = rectangle;
			branches[usedSpace] = page;
			usedSpace++;
			tree.file.writeNode(this);// �����ļ�
			RTDirNode parent = (RTDirNode) getParent();

			if (parent != null)
				parent.adjustTree(this, null);
			return pageNumber;

		} else {// �����������
			RTDataNode[] splitNodes = splitLeaf(rectangle, page);
			RTDataNode l = splitNodes[0];
			RTDataNode ll = splitNodes[1];

			if (isRoot()) {
				// root is full, so we must split it. From now on root will be
				// an Index and not a Leaf.
				l.parent = 0;
				l.pageNumber = -1;
				ll.parent = 0;
				ll.pageNumber = -1;
				tree.file.writeNode(l);
				tree.file.writeNode(ll);
				// ���ڵ���������Ҫ���ѡ������µĸ��ڵ�,����pageNumber=0��level=1
				RTDirNode r = new RTDirNode(tree, Constants.NIL, 0, 1);
				r.addData(l.getNodeRectangle(), l.pageNumber);
				r.addData(ll.getNodeRectangle(), ll.pageNumber);
				tree.file.writeNode(r);

			} else {// ���Ǹ��ڵ�
				// use old page number for left child,
				// a new page number for the right child.
				l.pageNumber = pageNumber;
				ll.pageNumber = -1;
				tree.file.writeNode(l);
				tree.file.writeNode(ll);
				RTDirNode parentNode = (RTDirNode) getParent();
				parentNode.adjustTree(l, ll);
			}

			for (int i = 0; i < l.usedSpace; i++) {
				if (l.branches[i] == page) {
					return l.pageNumber;
				}
			}

			for (int i = 0; i < ll.usedSpace; i++) {
				if (ll.branches[i] == page) {
					return ll.pageNumber;
				}
			}

			return -1;
		}
	}


	/**
	 * ���ڷ��ѹ��ȷ��ֵ�Ҷ�ӽڵ�
	 * ����Rectangle֮�󳬹�������Ҫ����
	 * 
	 * @param rectangle
	 * @param page
	 * @return
	 */
	public RTDataNode[] splitLeaf(Rectangle rectangle, int page) {
		int[][] group = null;

		switch (tree.getTreeType()) {
		case Constants.RTREE_LINEAR:
			break;
		case Constants.RTREE_QUADRATIC:
			group = quadraticSplit(rectangle, page);
			break;
		case Constants.RTREE_EXPONENTIAL:
			break;
		case Constants.RSTAR:
			break;
		default:
			throw new IllegalArgumentException("Invalid tree type.");
		}

		RTDataNode l = new RTDataNode(tree, parent);
		RTDataNode ll = new RTDataNode(tree, parent);

		int[] group1 = group[0];
		int[] group2 = group[1];

		for (int i = 0; i < group1.length; i++) {
			l.addData(datas[group1[i]], branches[group1[i]]);
		}

		for (int i = 0; i < group2.length; i++) {
			ll.addData(datas[group2[i]], branches[group2[i]]);
		}
		return new RTDataNode[] { l, ll };
	}

	/**
	 * Ϊʲô��ô��?
	 * @param rectangle
	 * @return
	 */
	@Override
	public RTDataNode chooseLeaf(Rectangle rectangle)
	{
		return this;
	}

	/**
	 * ����ɾ������ڵ��еľ���
	 *
	 * ��Ҷ�ڵ���ɾ������Ŀrectangle
	 * <p>
	 * ��ɾ����rectangle���ٵ���condenseTree()����ɾ�����ļ��ϣ������е�Ҷ�ӽ���е�ÿ����Ŀ���²��룻
	 * ��Ҷ�ӽ��ʹӴ˽�㿪ʼ�������н�㣬Ȼ������е�Ҷ�ӽ���е�������Ŀȫ�����²���
	 * 
	 * @param rectangle
	 * @return The data pointer of the deleted entry.
	 */
	protected int delete(Rectangle rectangle)
	{
		for (int i = 0; i < usedSpace; i++) {
			if (datas[i].equals(rectangle)) {
				int pointer = branches[i];
				deleteData(i);
				tree.file.writeNode(this);// ɾ�����ݺ���Ҫ����д�룬�������б仯������д��
				List<RTNode> deleteEntriesList = new ArrayList<RTNode>();
				condenseTree(deleteEntriesList);

				// ���²���ɾ�������ʣ�����Ŀ
				for (int j = 0; j < deleteEntriesList.size(); j++) {
					RTNode node = deleteEntriesList.get(j);
					if (node.isLeaf())// Ҷ�ӽ�㣬ֱ�Ӱ����ϵ��������²���
					{
						for (int k = 0; k < node.usedSpace; k++) {
							tree.insert(node.datas[k], node.branches[k]);
						}
					} else {// ��Ҷ�ӽ�㣬��Ҫ�Ⱥ�����������ϵ����н��
						List<RTNode> traNodes = tree.traversePostOrder(node);

						// �����е�Ҷ�ӽ���е���Ŀ���²���
						for (int index = 0; index < traNodes.size(); index++) {
							RTNode traNode = traNodes.get(index);
							if (traNode.isLeaf()) {
								for (int t = 0; t < traNode.usedSpace; t++) {
									tree.insert(traNode.datas[t],
											traNode.branches[t]);
								}
							}
							if (node != traNode)
								tree.file.deletePage(traNode.pageNumber);
							else {
								System.out.println("������ȡ�������");
							}
						}// end for
					}// end else
					tree.file.deletePage(node.pageNumber);
				}// end for

				return pointer;
			}// end if
		}// end for
		return Constants.NIL;
	}

	/**
	 * ���ڲ���
	 *
	 * ����һ������
	 * ������:�þ������ڵĽڵ�
	 *
	 * @param rectangle
	 * @return
	 */
	@Override
	protected RTDataNode findLeaf(Rectangle rectangle)
	{
		for (int i = 0; i < usedSpace; i++)
		{
			if (datas[i].enclosure(rectangle))
			{
				return this;
			}
		}
		return null;
	}

}

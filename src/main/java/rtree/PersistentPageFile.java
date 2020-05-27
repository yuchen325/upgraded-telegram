package rtree;

import java.io.*;
import java.util.Stack;

// �־û�page�ļ�
public class PersistentPageFile extends PageFile // a sub-class of PageFile
{

	private RandomAccessFile file; //��������ļ�����,���������ļ���ʱ�򣬲��ذ��ļ���ͷ����β������ϣ�������һ�����ݿ�һ���������������ط���һ���ļ���ĳ������
	private String fileName;	// �ļ���

	private Stack<Integer> emptyPages = new Stack<Integer>(); // һ��int���͵�ջ,���ڼ�¼��page��

	/**
	 * ���㹫ʽ���£� headerSize = dimension + fillFactor + nodeCapacity + pageSize +
	 * treeType
	 * ��_��
	 */
	private int headerSize = 20; // ?_?

	public static final int EMPTY_PAGE = -2; // һ������

	/**
	 * ���췽��1
	 * ������ʱ�ļ����˳������ʱɾ��
	 */
	public PersistentPageFile()
	{
		this(null);
	}

	/**
	 * ���fileNameΪ���򴴽���ʱ�ļ����˳������ʱɾ��
	 * ���췽��2
	 * @param fileName
	 */
	public PersistentPageFile(String fileName)
	{
		try {
			if (fileName == null)
			{
				File f = File.createTempFile("rtreeTemp", ".dat");
				this.fileName = f.getCanonicalPath();
				System.out.println(this.fileName);
				f.deleteOnExit();// ���������ֹʱ������ɾ���˳���·������ʾ���ļ���Ŀ¼��
			}
			else
				{
				file = new RandomAccessFile(fileName, "rw");
				this.fileName = fileName;

				file.seek(0);
				byte[] header = new byte[headerSize];
				if (headerSize == file.read(header)) {// �����
														// header.length�������ֽڴӴ��ļ�����
														// byte���顣������һ�������ֽڿ���ǰ���˷���һֱ������
					DataInputStream dis = new DataInputStream(
							new ByteArrayInputStream(header));
					dimension = dis.readInt();
					fillFactor = dis.readFloat();
					nodeCapacity = dis.readInt();
					pageSize = dis.readInt();
					treeType = dis.readInt();

					// �ҵ����еĿ�page,�������������emptyPagesջ��
					int i = 0;
					try {
						while (true) {
							if (EMPTY_PAGE == file.readInt()) {
								emptyPages.push(i);
							}
							i++;
							file.seek(headerSize + i * pageSize);
						}
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ʼ������
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
		emptyPages.clear();

		try {
			file.setLength(0);
			
			file.seek(0);
			file.writeInt(dimension);
			file.writeFloat(fillFactor);
			file.writeInt(nodeCapacity);
			file.writeInt(pageSize);
			file.writeInt(treeType);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��������
	 * @throws Throwable
	 */
	protected void finalize() throws Throwable {
		try {
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.finalize();
	}

	/**
	 * ��ȡpage�е�node
	 * @param page
	 * @return
	 * @throws PageFaultError
	 */
	@Override
	public RTNode readNode(int page) throws PageFaultError
	{
		if (page < 0) {
			throw new IllegalArgumentException(
					"Page number cannot be negative.");
		}

		try {
			file.seek(headerSize + page * pageSize);// �ȶ�λ��ָ��page

			byte[] b = new byte[pageSize];
			int l = file.read(b);
			if (-1 == l) {
				throw new PageFaultError("EOF found while trying to read page "
						+ page + ".");
			}

			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
					b));

			int parent = dis.readInt();
			if (parent == EMPTY_PAGE) {
				throw new PageFaultError("Page " + page + " is empty.");
			}

			int level = dis.readInt();
			int usedSpace = dis.readInt();

			RTNode node;
			if (level != 0) {
				node = new RTDirNode(tree, parent, page, level);
			} else {
				node = new RTDataNode(tree, parent, page);
			}

			// node.parent = page;//����
			// node.level = level;//����
			node.usedSpace = usedSpace;

			double[] p1 = new double[dimension];
			double[] p2 = new double[dimension];

			for (int i = 0; i < usedSpace; i++) {
				for (int j = 0; j < dimension; j++) {
					p1[j] = dis.readFloat();
					p2[j] = dis.readFloat();
				}

				node.datas[i] = new Rectangle(new Point(p1), new Point(p2));
				node.branches[i] = dis.readInt();
			}

			return node;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ��page�ж�ȡnode
	 * @param node
	 * @return
	 * @throws PageFaultError
	 */
	@Override
	protected int writeNode(RTNode node) throws PageFaultError
	{
		if (node == null) {
			throw new IllegalArgumentException("Node cannot be null.");
		}

		try {
			int page;
			if (node.pageNumber < 0) {
				if (emptyPages.empty()) {
					page = (int) ((file.length() - headerSize) / pageSize);
				} else {
					page = emptyPages.pop();
				}
				node.pageNumber = page;
			} else {
				page = node.pageNumber;
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream(pageSize);
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeInt(node.parent);
			dos.writeInt(node.level);
			dos.writeInt(node.usedSpace);

			for (int i = 0; i < tree.getNodeCapacity(); i++) {
				for (int j = 0; j < tree.getDimension(); j++) {
					if (node.datas[i] == null) {
						dos.writeDouble(Double.NaN);
						dos.writeDouble(Double.NaN);
					} else {
						dos.writeDouble(node.datas[i].getLow()
								.getDoubleCoordinate(j));
						dos.writeDouble(node.datas[i].getHigh()
								.getDoubleCoordinate(j));
					}
				}
				dos.writeInt(node.branches[i]);
			}
			dos.flush();

			file.seek(headerSize + page * pageSize);
			file.write(baos.toByteArray());

			return page;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * ɾ��page�е�node
	 * @param page
	 * @return
	 * @throws PageFaultError
	 */
	@Override
	protected RTNode deletePage(int page) throws PageFaultError
	{
		try {
			if (page < 0 || page > (file.length() - headerSize) / pageSize) {
				return null;
			} else {
				if (page == 5) {
					System.out.println("=======5=======");
				}
				System.out.println("----delete page " + page + "-----");
				RTNode node = readNode(page);
				file.seek(headerSize + page * pageSize);
				file.writeInt(EMPTY_PAGE);
				emptyPages.push(page);
				return node;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}

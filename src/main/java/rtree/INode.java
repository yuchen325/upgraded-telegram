package rtree;

// 推测:这个给node类用的接口
public interface INode  //这是一个接口,它里面的所有成员都是抽象成员,留给使用该接口的class去实现
{
	public RTNode getParent();    // 找妈妈
	public String getUniqueId();  // 获取独一无二的ID
	public int getLevel();        // 获取节点所在的层次
	public Rectangle getNodeRectangle(); // 获取节点的MBR矩形
	public boolean isLeaf();
    public boolean isRoot();
    public boolean isIndex(); // 问题:一个节点????what？？
}

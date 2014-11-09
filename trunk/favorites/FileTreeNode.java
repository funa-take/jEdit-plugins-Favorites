package favorites;

import java.io.*;
import java.util.*;
import javax.swing.tree.*;

import org.gjt.sp.jedit.*;

public class FileTreeNode implements TreeNode, Comparable, Cloneable {
  
  private String name;
  private String path;
  // private Vector children; // 子ノードの配列
  // private HashMap hash; // 小ノードのハッシュ
  private VectorHash children; // 子ノードのVectorHash
  private boolean leaf; // 自身が葉であるか
  private TreeNode parent; // 親ノード
  
  public static String TYPE_FILE = "File";
  public static String TYPE_DIRECTORY = "Directory";
  private String type; //種類：FileとかDirectoryとか
  
  
  public FileTreeNode(String name, String path, String type) {
    children = new VectorHash();
    // hash = new HashMap();
    this.name = name;
    this.path = path.replace('\\', '/');
    this.type = type;
    leaf = true;
  }
  
  
  public FileTreeNode(String name, String path) {
    this(name, path, TYPE_FILE);
  }
  
  
  public FileTreeNode(String name) {
    this(name, "");
    leaf = false;
  }
  
  
  public boolean renameGroup(FileTreeNode node, String newname) {
    if (node.isLeaf()) {
      return false;
    }
    
    if (children.containsKey(newname)) {
      return false;
    }
    
    if (!children.containsKey(node.getKey())) {
      return false;
    }
    int index = children.getIndex(node.getKey());
    children.remove(node.getKey());
    node.setName(newname);
    children.insert(node.getKey(), node, index);
    return true;
  }
  
  
  public boolean addNode(FileTreeNode node) {
    if (children.containsKey(node.getKey())) {
      return false;
    }
    
    // もし他の親に属している場合は、古い親から抜ける
    FileTreeNode oldParent = (FileTreeNode)node.getParent();
    if (oldParent != null && !oldParent.equals(this)){
      oldParent.deleteNode(node);
    }
    
    node.setParent(this);
    int index = 0;
    for (index = 0; index < children.size(); index++) {
      if (node.compareTo(children.get(index)) <= 0) {
        break;
      }
    }
    
    children.insert(node.getKey(), node, index);
    return true;
  }
  
  
  public boolean addNode(Buffer buffer) {
    return this.addNode(new FileTreeNode(buffer.getName(), buffer.getPath()));
  }
  
  
  public boolean addNode(String path, String type) {
    String name = MiscUtilities.getFileName(path);
    return this.addNode(new FileTreeNode(name, path, type));
  }
  
  
  public void deleteNode(FileTreeNode node) {
    if (node == null) {
      return;
    }
    children.remove(node.getKey());
  }
  
  
  public void setParent(FileTreeNode node) {
    this.parent = node;
  }
  
  
  //TreeNodeメソッドの実装
  //子ノードにアクセスするためのEnumerationを返す
  @Override
  public Enumeration children() {
    return children.elements();
  }
  
  
  //子ノードにアクセスできるかどうかを返す
  @Override
  public boolean getAllowsChildren() {
    return !leaf;
  }
  
  
  //インデックス番目の子ノードを返す
  @Override
  public TreeNode getChildAt(int index) {
    return (TreeNode)children.get(index);
  }
  
  
  //子ノードの数を返す
  @Override
  public int getChildCount() {
    return children.size();
  }
  
  
  //引数の子ノードが何番目なのかを返す
  public int getIndex(TreeNode node) {
    if (!(node instanceof FileTreeNode)) {
      return -1;
    }
    return children.getIndex(((FileTreeNode)node).getKey());
  }
  
  
  //子ノードを返す
  public TreeNode getChild(Object key) {
    return (TreeNode)children.get(key);
  }
  
  public boolean containsChild(TreeNode node){
    if (!(node instanceof FileTreeNode)) {
      return false;
    }
    return children.containsKey(((FileTreeNode)node).getKey());
  }
  
  
  // 指定されたノードが親として存在しているか再帰的に探す
  public boolean findParent(FileTreeNode node){
    TreeNode current = this;
    while( current != null ){
      if (current.equals(node)){
        return true;
      }
      current = current.getParent();
    }
    return false;
  }
  
  
  //親ノードを返す
  @Override
  public TreeNode getParent() {
    if (parent != null) {
      return parent;
    } else {
      return null;
    }
  }
  
  
  //葉であるかどうかを返す
  @Override
  public boolean isLeaf() {
    return leaf;
  }
  
  
  @Override
  public String toString() {
    if (isLeaf()) {
      return name + " (" + path + ")";
    } else {
      return name;
    }
    
  }
  
  
  public String getKey() {
    if (isLeaf()) {
      return path;
    } else {
      return name;
    }
  }
  
  
  public String getPath() {
    return path;
  }
  
  
  public String getName() {
    return name;
  }
  
  
  private void setName(String name) {
    this.name = name;
  }
  
  
  public String getType() {
    return type;
  }
  
  
  @Override
  public int compareTo(Object o) {
    if (!(o instanceof FileTreeNode)) {
      return 0;
    }
    FileTreeNode node = (FileTreeNode)o;
    if (isLeaf() && !node.isLeaf()) {
      return 1;
    }
    
    if (!isLeaf() && node.isLeaf()) {
      return -1;
    }
    
    if (TYPE_DIRECTORY.equals(getType()) && !TYPE_DIRECTORY.equals(node.getType())) {
      return -1;
    }
    
    if (!TYPE_DIRECTORY.equals(getType()) && TYPE_DIRECTORY.equals(node.getType())) {
      return 1;
    }
    
    // if (isLeaf()){
    // return getPath().compareToIgnoreCase(node.toString());
    // }
    
    // if (!isLeaf()){
    return toString().compareToIgnoreCase(node.toString());
    // }
    
    // return 0;
  }
  
  
  /**
  * FileTreeNode をコピーします。
  * コピーは子配列の複製を参照します。
  * 
  *@return    VectorHash のコピー
  */
  @Override
  public Object clone() {
    FileTreeNode clone = null;
    try {
      clone = (FileTreeNode)super.clone();
      clone.children = (VectorHash)clone.children.clone();
    } catch (Exception e) {}
    
    return clone;
  }
  
}

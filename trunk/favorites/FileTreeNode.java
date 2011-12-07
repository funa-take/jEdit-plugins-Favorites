package favorites;

import java.io.*;
import java.util.*;
import javax.swing.tree.*;

import org.gjt.sp.jedit.*;

public class FileTreeNode implements TreeNode, Comparable,Cloneable {
  
  private String name;
  private String path;
  // private Vector children; // �q�m�[�h�̔z��
  // private HashMap hash; // ���m�[�h�̃n�b�V��
  private VectorHash children; // �q�m�[�h��VectorHash
  private boolean leaf; // ���g���t�ł��邩
  private TreeNode parent; // �e�m�[�h
  
  public FileTreeNode(String name, String path) {
    children = new VectorHash();
    // hash = new HashMap();
    this.name = name;
    this.path = path.replace('\\','/');
    leaf = true;
  }
  
  public FileTreeNode(String name) {
    this(name, "");
    leaf = false;
  }
  
  public boolean renameGroup(FileTreeNode node, String newname){
    if (node.isLeaf()){
      return false;
    }
    
    if (children.containsKey(newname)){
      return false;
    }
    
    if (!children.containsKey(node.getKey())){
      return false;
    }
    int index = children.getIndex(node.getKey());
    children.remove(node.getKey());
    node.setName(newname);
    children.insert(node.getKey(),node,index);
    return true;
  }
  
  public boolean add(FileTreeNode node){
    if (children.containsKey(node.getKey())){
      return false;
    }
    
    node.setParent(this);
    int index = 0;
    for (index = 0; index < children.size(); index++){
      if (node.compareTo(children.get(index)) <= 0){
        break;
      }
    }
    
    children.insert(node.getKey(),node,index);
    return true;
  }
  
  public boolean add(Buffer buffer){
    return this.add(new FileTreeNode(buffer.getName(), buffer.getPath()));
  }
  
  public void delete(FileTreeNode node){
    if (node == null){
      return;
    }
    children.remove(node.getKey());
  }
  
  public void setParent(FileTreeNode node){
    this.parent = node;
  }
  
  //TreeNode���\�b�h�̎���
  
  //�q�m�[�h�ɃA�N�Z�X���邽�߂�Enumeration��Ԃ�
  public Enumeration children() {
    return children.elements();
  }
  
  //�q�m�[�h�ɃA�N�Z�X�ł��邩�ǂ�����Ԃ�
  public boolean getAllowsChildren() {
    return !leaf; 
  }
  
  //�C���f�b�N�X�Ԗڂ̎q�m�[�h��Ԃ�
  public TreeNode getChildAt(int index) {
    return (TreeNode)children.get(index);
  }
  
  //�q�m�[�h�̐���Ԃ�
  public int getChildCount() {
    return children.size();
  }
  
  //�����̎q�m�[�h�����ԖڂȂ̂���Ԃ�
  public int getIndex(TreeNode node) {
    if (!(node instanceof FileTreeNode)){
      return -1;
    }
    return children.getIndex(((FileTreeNode)node).getKey());
  }
  
  //�q�m�[�h��Ԃ�
  public TreeNode getChild(Object key) {
    return (TreeNode)children.get(key);
  }
  
  //�e�m�[�h��Ԃ�
  public TreeNode getParent() {
    if(parent != null) {
      return parent;
    } else { 
      return null;
    }
  }
  
  //�t�ł��邩�ǂ�����Ԃ�
  public boolean isLeaf() {
    return leaf;
  }
  
  public String toString(){
    if (isLeaf()){
      return name + " (" + path + ")";
    } else {
      return name;
    }
    
  }
  
  public String getKey(){
    if (isLeaf()){
      return path;
    } else {
      return name;
    }
  }
  
  public String getPath(){
    return path;
  }
  
  public String getName(){
    return name;
  }
  
  private void setName(String name){
    this.name = name;
  }
  
  public int compareTo(Object o){
    if (!(o instanceof FileTreeNode)){
      return 0;
    }
    FileTreeNode node = (FileTreeNode)o;
    if (isLeaf() && !node.isLeaf()){
      return 1;
    }
    
    if (!isLeaf() && node.isLeaf()){
      return -1;
    }
    
    // if (isLeaf()){
    // return getPath().compareToIgnoreCase(node.toString());
    // }
    
    // if (!isLeaf()){
    return getName().compareToIgnoreCase(node.toString());
    // }
    
    // return 0;
  }
  
  /**
  * FileTreeNode ���R�s�[���܂��B
  * �R�s�[�͎q�z��̕������Q�Ƃ��܂��B
  * 
  *@return    VectorHash �̃R�s�[
  */
  public Object clone(){
    FileTreeNode clone = null;
    try {
      clone = (FileTreeNode)super.clone();
      clone.children = (VectorHash)clone.children.clone();
    } catch (Exception e){
    }
    
    return clone;
  }
  
}
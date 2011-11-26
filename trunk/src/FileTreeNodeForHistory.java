package favorites;

import java.io.*;
import java.util.*;
import javax.swing.tree.*;

import org.gjt.sp.jedit.*;

public class FileTreeNodeForHistory extends  FileTreeNode {
  
  private int historyOrder;
  
  public FileTreeNodeForHistory(String name, String path, int historyOrder) {
    super(name,path);
    this.historyOrder = historyOrder;
  }
  
  
  public int compareTo(Object o){
    if (!(o instanceof FileTreeNodeForHistory)){
      return 0;
    }
    FileTreeNodeForHistory node = (FileTreeNodeForHistory)o;
    return this.historyOrder - node.historyOrder;
  }
  
}

package favorites;

import java.io.IOException;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;

public class FileTreeNodeTransferHandler extends TransferHandler {
  @Override
  protected  Transferable  createTransferable(JComponent c){
    if (!(c instanceof JTree)){
      return null;
    }
    
    JTree tree = (JTree)c;
    TreePath path = tree.getSelectionPath();
    if (path == null){
      return null;
    }
    // tree.setSelectionPath(path);
    // FileTreeNode node = (FileTreeNode)((FileTreeNode)path.getLastPathComponent()).clone();
    return new FileTreeNodeTransferable((FileTreeNode)path.getLastPathComponent());
  }
  
  @Override
  public int getSourceActions(JComponent c) {
    // 移動のみ
    return MOVE;
  }
  
  @Override
  public boolean canImport(JComponent c, DataFlavor[] transferFlavors) {
    return true;
  }
  
  @Override
  public boolean importData(JComponent c, Transferable t) {
    if (!canImport(c, t.getTransferDataFlavors())) {
      return false;
    }
    
    if (!(c instanceof JTree)) {
      return false;
    }
    
    try {
      // System.out.println("import");
      JTree tree = (JTree)c;
      TreePath path = tree.getDropLocation().getPath();
      final FileTreeNode parent = (FileTreeNode)path.getLastPathComponent();
      
      // 葉にはドロップしない
      if (parent.isLeaf()){
        return false;
      }
      DataFlavor[] dfa = t.getTransferDataFlavors();
      for (DataFlavor df : dfa) {
        FileTreeNode node = (FileTreeNode)t.getTransferData(df);
        // ルートは移動禁止
        if (node.getParent() == null){
          return false;
        }
        
        // 移動させるノードの親と移動先が同じ場合は何もしない
        if(node.getParent().equals(parent)){
          return false;
        }
        
        // 親を子へ移動させない
        if(parent.findParent(node)){
          return false;
        }
        
        // 既に同じノードが存在する場合は移動しない
        if (parent.containsChild(node)){
          return false;
        }
        
        // parent.addNode((FileTreeNode)node.clone());
        final FileTreeNode oldParent = (FileTreeNode)node.getParent();
        parent.addNode(node);
        
        // リロードは遅延処理する
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
              FavoritesList.reloadAll(oldParent);
              FavoritesList.reloadAll(parent);
            }
        });
        
        
        // FavoritesList.reloadAll();
      }
      return true;
    } catch (UnsupportedFlavorException ex) {
    } catch (IOException ex) {
    }
    return false;
  }
  
  @Override
  protected void exportDone(JComponent source, Transferable t, int action) {
    if (!(source instanceof JTree)) {
      return;
    }
    
    super.exportDone(source, t, action);
    // try {
    // JTree tree = (JTree)source;
    // if (action == MOVE) {
    // for (DataFlavor df : t.getTransferDataFlavors()) {
    // FileTreeNode node = (FileTreeNode)t.getTransferData(df);
    // FileTreeNode parent = (FileTreeNode)node.getParent();
    // parent.deleteNode(node);
    // FavoritesList.reloadAll(parent);
    // }
    // } else {
    // super.exportDone(source, t, action);
    // }
    // } catch (UnsupportedFlavorException ex) {
    // } catch (IOException ex) {
    // }
  }
}

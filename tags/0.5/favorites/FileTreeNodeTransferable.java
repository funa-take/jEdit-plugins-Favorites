package favorites;

import java.awt.datatransfer.*;

public class FileTreeNodeTransferable implements Transferable {
  protected FileTreeNode node;
  protected DataFlavor flavor = new DataFlavor(FileTreeNode.class, FileTreeNode.class.toString());
  
  
  public FileTreeNodeTransferable(FileTreeNode node){
    this.node = node;
  }
  
  @Override
  public Object getTransferData(DataFlavor flavor){
    return this.node;
  }
  
  @Override
  public DataFlavor[] getTransferDataFlavors(){
    return new DataFlavor[] {flavor};
  }
  
  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor){
    return this.flavor.equals(flavor);
  }
  
}
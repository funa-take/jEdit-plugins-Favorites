package favorites;

// from java:
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.tree.*;
import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.io.*;

// from jEdit:
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.io.*;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.util.Log;

/**
* ツリーを表示するパネル。
* ツリーに表示するノードは、複数の FavoritesList で共有される。
*/
public class FavoritesList extends JPanel implements EBComponent, DefaultFocusComponent {
  
  private View view = null;
  private JTree tree = null;
  private FileTreeNode root = null;
  private DefaultTreeModel treeModel = null;
  private JScrollPane scroller = null;
  private static Hashtable hash = new Hashtable();
  
  
  public FavoritesList( final View view ) {
    super( new BorderLayout() );
    this.view = view;
    
    MouseListener ml = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
          selectNode();
        }
      }
    };
    
    KeyListener kl = new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (ClassLoader.getSystemResource("org/gjt/sp/jedit/gui/UserKey.class") != null) {
          // org.gjt.sp.jedit.gui.UserKey.consume(e);
          int mod = org.gjt.sp.jedit.gui.UserKey.ALLOW_CTRL | org.gjt.sp.jedit.gui.UserKey.ALLOW_SHIFT;
          org.gjt.sp.jedit.gui.UserKey.consume(e, mod, mod, mod, mod, true);
          if (e.isConsumed()) {
            return;
          }
        }
        switch (e.getKeyCode()) {
        case KeyEvent.VK_ENTER:
          selectNode();
          if (e.isAltDown()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  view.getDockableWindowManager().showDockableWindow("favorites.dock");
                }
            }
            );
          }
          e.consume();
          break;
          
        case KeyEvent.VK_DELETE:
          // delete();
          FavoritesPlugin.delete();
          e.consume();
          break;
        case KeyEvent.VK_F2:
          // rename();
          FavoritesPlugin.rename();
          e.consume();
          break;
        }
      }
    };
    TreeSelectionListener tsl = new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent evt) {
        
        // if (tree.isSelectionEmpty() ) {
        // return;
        // }
        //
        // DefaultMutableTreeNode treenode = ( DefaultMutableTreeNode ) tree.getSelectionPath().getLastPathComponent();
        Object node = tree.getLastSelectedPathComponent();
        if (node == null) {
          return;
        }
        view.getStatus().setMessage(node.toString());
        
        // TigerNode node = ( TigerNode ) treenode.getUserObject();
        // view.getStatus().setMessage(node.toString());
        // // this.setPosition(selectedNode);
      }
    };
    
    // DefaultMutableTreeNode root = new DefaultMutableTreeNode( "test" );
    // DefaultMutableTreeNode test2 = new DefaultMutableTreeNode( "test2" );
    Enumeration element = hash.elements();
    if (element.hasMoreElements()){
      FavoritesList list = (FavoritesList)element.nextElement();
      root = list.getRoot();
    } else {
      root = FavoritesPlugin.createRoot();
    }
    
    treeModel = new DefaultTreeModel(root);
    tree = new JTree(treeModel);
    // drag and drop を有効にする
    tree.setDragEnabled(true);
    tree.setTransferHandler(new FileTreeNodeTransferHandler());
    
    tree.addKeyListener(kl);
    tree.addMouseListener(ml);
    tree.addTreeSelectionListener(tsl);
    tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK), "none");
    // tree.getInputMap().put(KeyStroke.getKeyStroke("control A"), "none");
    tree.setCellRenderer(
      new DefaultTreeCellRenderer(){
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus){
          Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
          
          if (leaf && value instanceof FileTreeNode && c instanceof JLabel){
            JLabel label = (JLabel)c;
            FileTreeNode node = (FileTreeNode)value;
            if (FileTreeNode.TYPE_DIRECTORY.equals(node.getType())){
              label.setIcon(GUIUtilities.loadIcon("Folder.png"));
            }
          }
          return c;
        }
        
      });
    
    scroller = new JScrollPane( tree );
    
    ActionListener al = new ActionListener(){
      public void actionPerformed(ActionEvent e){
        String cmd = e.getActionCommand();
        if (cmd.equals("add")){
          FavoritesPlugin.add();
        } else if (cmd.equals("addParentPath")) {
          FavoritesPlugin.addParentPath();
        } else if (cmd.equals("addGroup")) {
          FavoritesPlugin.addGroup();
        } else if (cmd.equals("addBuffers")) {
          FavoritesPlugin.addBuffers();
        } else if (cmd.equals("delete")) {
          FavoritesPlugin.delete();
        }
      }
    };
    RolloverButton addButton = new RolloverButton(
      GUIUtilities.loadIcon("Plus.png"));
    addButton.setActionCommand("add");
    addButton.addActionListener(al);
    RolloverButton deleteButton = new RolloverButton(
      GUIUtilities.loadIcon("Minus.png"));
    deleteButton.setActionCommand("delete");
    deleteButton.addActionListener(al);
    RolloverButton addPathButton = new RolloverButton(
      GUIUtilities.loadIcon("CurrentDir.png"));
    addPathButton.setActionCommand("addParentPath");
    addPathButton.addActionListener(al);
    RolloverButton addGroupButton = new RolloverButton(
      GUIUtilities.loadIcon("NewDir.png"));
    addGroupButton.setActionCommand("addGroup");
    addGroupButton.addActionListener(al);
    RolloverButton addBuffersButton = new RolloverButton(
      GUIUtilities.loadIcon("SaveAll.png"));
    addBuffersButton.setActionCommand("addBuffers");
    addBuffersButton.addActionListener(al);
    JPanel panel = new JPanel(new BorderLayout());
    JPanel leftPanel = new JPanel(new FlowLayout());
    panel.add(leftPanel, BorderLayout.WEST);
    leftPanel.add(addButton);
    leftPanel.add(deleteButton);
    leftPanel.add(addPathButton);
    leftPanel.add(addGroupButton);
    leftPanel.add(addBuffersButton);
    
    add( scroller, BorderLayout.CENTER );
    add( panel, BorderLayout.NORTH );
    
    hash.put(view, this);
  }
  
  
  public void handleMessage( EBMessage message ) {
    if ( message instanceof BufferUpdate ) {
      // BufferUpdate msg = ( BufferUpdate ) message;
      // if ( msg.getWhat().equals( BufferUpdate.SAVED ) || msg.getWhat().equals( BufferUpdate.LOADED ) ) {
      // parse();
      // }
    }
    // parse on view update
    else if ( message instanceof ViewUpdate ) {
      // ViewUpdate msg = ( ViewUpdate ) message;
      // if ( msg.getWhat().equals( ViewUpdate.EDIT_PANE_CHANGED ) ) {
      // parse();
      // }
    }
    // parse on edit pane update
    else if ( message instanceof EditPaneUpdate ) {
      // EditPaneUpdate msg = ( EditPaneUpdate ) message;
      // if ( msg.getWhat().equals( EditPaneUpdate.BUFFER_CHANGED ) ) {
      // parse();
      // }
    }
  }
  
  
  public void selectNode() {
    TreeSelectionModel selmodel = tree.getSelectionModel();
    TreePath[] tp = selmodel.getSelectionPaths();
    for (int i = 0; i < tp.length; i++) {
      FileTreeNode node = (FileTreeNode)(tp[i].getPathComponent(tp[i].getPathCount() - 1));
      if (node.isLeaf()) {
        if (FileTreeNode.TYPE_DIRECTORY.equals(node.getType())){
          VFSBrowser.browseDirectory(view,node.getPath());
        } else {
          jEdit.openFile(view, node.getPath());
        }
      }
    }
  }
  
  
  public void reload() {
    int rows[] = tree.getSelectionRows();
    treeModel.reload();
    tree.setSelectionRows(rows);
  }
  
  
  public void reload(FileTreeNode node){
    int rows[] = tree.getSelectionRows();
    treeModel.reload(node);
    tree.setSelectionRows(rows);
  }
  
  
  public static void reloadAll(){
    Enumeration element = hash.elements();
    while(element.hasMoreElements()){
      ((FavoritesList)element.nextElement()).reload();
    }
  }
  
  
  public static void reloadAll(FileTreeNode node){
    Enumeration element = hash.elements();
    while(element.hasMoreElements()){
      ((FavoritesList)element.nextElement()).reload(node);
    }
  }
  
  public void copyRoot(FileTreeNode root) {
    Enumeration e = this.root.children();
    while(e.hasMoreElements()) {
      FileTreeNode node = (FileTreeNode)e.nextElement();
      this.root.deleteNode(node);
    }
    
    e = root.children();
    while(e.hasMoreElements()) {
      FileTreeNode node = (FileTreeNode)e.nextElement();
      this.root.addNode(node);
    }
    reloadAll();
  }
  
  public static FavoritesList getFavoritesList(View view) {
    return (FavoritesList)hash.get(view);
    
  }
  
  public static FavoritesList removeFavoritesList(View view) {
    return (FavoritesList)hash.remove(view);
  }
  
  
  public void addGroup() {
    // String[] msg = {"Input group name", "a","b"};
    // String gname = GUIUtilities.input(view, "Group Name","test", "");
    String gname = JOptionPane.showInputDialog(view, "Input group name", "Add Group", JOptionPane.QUESTION_MESSAGE); 
    // System.out.println("add Group :" + gname);
    if (gname == null || gname.equals("")) {
      return;
    }
    FileTreeNode node = (FileTreeNode)tree.getLastSelectedPathComponent();
    if (node == null) {
      node = root;
    }
    if (node.isLeaf()) {
      node = (FileTreeNode)node.getParent();
    }
    if (node.addNode(new FileTreeNode(gname))){
      // treeModel.reload(node);
      FavoritesList.reloadAll(node);
    }
    
  }
  
  
  public void addPath() {
    String path = JOptionPane.showInputDialog(view, "Input Path", "Add Path", JOptionPane.QUESTION_MESSAGE); 
    if (path == null || path.equals("")) {
      return;
    }
    
    FileTreeNode node = (FileTreeNode)tree.getLastSelectedPathComponent();
    if (node == null) {
      node = root;
    }
    if (node.isLeaf()) {
      node = (FileTreeNode)node.getParent();
    }
    
    if (node.addNode(path,FileTreeNode.TYPE_DIRECTORY)){
      FavoritesList.reloadAll(node);
    }
  }
  
  
  public void addParentPath() {
    FileTreeNode node = (FileTreeNode)tree.getLastSelectedPathComponent();
    if (node == null) {
      node = root;
    }
    if (node.isLeaf()) {
      node = (FileTreeNode)node.getParent();
    }
    String path = view.getBuffer().getPath();
    path = MiscUtilities.getParentOfPath(path);
    
    if (node.addNode(path,FileTreeNode.TYPE_DIRECTORY)){
      FavoritesList.reloadAll(node);
    }
  }
  
  
  public void add() {
    // System.out.println("add");
    FileTreeNode node = (FileTreeNode)tree.getLastSelectedPathComponent();
    if (node == null) {
      node = root;
    }
    if (node.isLeaf()) {
      node = (FileTreeNode)node.getParent();
    }
    if (node.addNode(view.getBuffer())){
      // treeModel.reload(node);
      FavoritesList.reloadAll(node);
    }
    
  }
  
  
  public void addBuffers() {
    // System.out.println("add");
    FileTreeNode node = (FileTreeNode)tree.getLastSelectedPathComponent();
    if (node == null) {
      node = root;
    }
    if (node.isLeaf()) {
      node = (FileTreeNode)node.getParent();
    }
    // Buffer[] buffers = jEdit.getBuffers();
    Buffer[] buffers = jEdit.getActiveView().getEditPane().getBufferSet().getAllBuffers();
    boolean result = false;
    for (int i = 0; i < buffers.length; i++) {
      result |= node.addNode(buffers[i]);
    }
    if (result) {
      // treeModel.reload(node);
      FavoritesList.reloadAll(node);
    }
    
  }
  
  
  public void rename(){
    FileTreeNode node = (FileTreeNode)tree.getLastSelectedPathComponent();
    if (node.equals(root)){
      return;
    }
    
    // String gname = JOptionPane.showInputDialog(view, "Input group name", "Rename Group", JOptionPane.QUESTION_MESSAGE);
    String newName = JOptionPane.showInputDialog(view, "Input New Name", node.getName());
    if (newName == null || newName.equals("")) {
      return;
    }
    
    FileTreeNode parent = (FileTreeNode)node.getParent();
    if (parent == null){
      return;
    }
    
    if (parent.renameChild(node, newName)) {
      // treeModel.reload(parent);
      FavoritesList.reloadAll(parent);
    }
  }
  
  
  public void delete() {
    // System.out.println("delete");
    if (JOptionPane.showConfirmDialog(view,"Do you want to delete?","Delete favorites", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
      return;
    }
    
    TreeSelectionModel selmodel = tree.getSelectionModel();
    TreePath[] tp = selmodel.getSelectionPaths();
    FileTreeNode parent = null;
    TreePath parentPath = null;
    for (int i = 0; i < tp.length; i++) {
      FileTreeNode node = (FileTreeNode)(tp[i].getPathComponent(tp[i].getPathCount() - 1));
      parent = (FileTreeNode)node.getParent();
      parentPath = tp[i].getParentPath();
      if (parent == null) {
        continue;
      }
      
      parent.deleteNode(node);
    }
    if (tp.length == 1 && parent != null) {
      // treeModel.reload(parent);
      FavoritesList.reloadAll(parent);
    } else {
      // treeModel.reload();
      FavoritesList.reloadAll();
    }
    
    if (parentPath != null) {
      selmodel.setSelectionPath(parentPath);
    }
  }
  
  public java.util.List<FileTreeNode> getSelectedNodes() {
    java.util.List<FileTreeNode> selectedNode = new ArrayList<FileTreeNode>();
    
    TreeSelectionModel selmodel = tree.getSelectionModel();
    TreePath[] tp = selmodel.getSelectionPaths();
    for (int i = 0; i < tp.length; i++) {
      FileTreeNode node = (FileTreeNode)(tp[i].getPathComponent(tp[i].getPathCount() - 1));
      selectedNode.add(node);
    }
    return selectedNode;
  }
  
  public boolean hasFocus() {
    if (tree != null) {
      return tree.hasFocus();
    }
    return super.hasFocus();
  }
  
  
  public void focusOnDefaultComponent() {
    if (tree != null) {
      tree.requestFocus();
    }
  }
  
  
  public FileTreeNode getRoot() {
    return root;
  }
  
  
  public FileTreeNode getHistory() {
    return (FileTreeNode)root.getChild(FavoritesPlugin.getHistoryKey());
  }
}

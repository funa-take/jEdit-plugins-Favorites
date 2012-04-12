package favorites;

import java.io.*;
import java.util.*;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.BufferHistory;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.EBPlugin;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.msg.DockableWindowUpdate;
import org.gjt.sp.jedit.msg.PluginUpdate;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.jedit.msg.ViewUpdate;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.EditPaneUpdate;

import org.gjt.sp.util.Log;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.swing.SwingUtilities;

public class FavoritesPlugin extends EBPlugin {
  
  private FavoritesList lastList = null;
  public FavoritesPlugin(){
  }
  
  public void handleMessage( EBMessage msg ) {
    // // System.out.println(msg);
    // if (msg instanceof DockableWindowUpdate){
    // if (((DockableWindowUpdate)msg).getWhat().equals(DockableWindowUpdate.ACTIVATED)){
    // // System.out.println("++++++++++++++++++++++++++");
    // }
    // }
    // 
    
    if (msg instanceof BufferUpdate){
      BufferUpdate bmsg = (BufferUpdate)msg;
      if (bmsg.getWhat().equals(BufferUpdate.CLOSED)){
        if (bmsg.getView() == null){
          return;
        }
        // バッファを閉じたら、履歴をリロードする
        reloadHistory(bmsg.getView());
      }
    }
    
    if ( msg instanceof ViewUpdate ) {
      ViewUpdate vumsg = (ViewUpdate)msg;
      if (vumsg.getWhat().equals(ViewUpdate.CLOSED)){
        lastList = FavoritesList.removeFavoritesList((View)msg.getSource());
        // } else if (vumsg.getWhat().equals(ViewUpdate.ACTIVATED)){
        // FavoritesList fl = FavoritesList.getFavoritesList((View)msg.getSource());
        // if (fl != null){
        // fl.refresh();
        // }
      }
    }
    // else if ( msg instanceof PropertiesChanged ) {}
    // else if ( msg instanceof EditPaneUpdate ) {}
  }
  
  public static String getHistoryKey(){
    return "# Recent Files";
  }
  
  public static void reloadHistory(View view){
    final FavoritesList cur = FavoritesList.getFavoritesList(view);
    if (cur == null){
      return;
    }
    
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          FileTreeNode root = cur.getRoot();
          FileTreeNode history = cur.getHistory();
          boolean reloadAll = (history == null);
          
          root.deleteNode(history);
          history = loadHistory();
          root.addNode(history);
          if (reloadAll){
            FavoritesList.reloadAll();
          } else {
            FavoritesList.reloadAll(history);
          }
        }
    });
  }
  
  public void stop(){
    saveXML(lastList);
  }
  
  public static void add(){
    View view = jEdit.getActiveView();
    FavoritesList cur = FavoritesList.getFavoritesList(view);
    if (cur != null){
      cur.add();
      saveXML();
    }
  }
  
  public static void addPath(){
    View view = jEdit.getActiveView();
    FavoritesList cur = FavoritesList.getFavoritesList(view);
    if (cur != null){
      cur.addPath();
      saveXML();
    }
  }
  
  
  public static void addParentPath(){
    View view = jEdit.getActiveView();
    FavoritesList cur = FavoritesList.getFavoritesList(view);
    if (cur != null){
      cur.addParentPath();
      saveXML();
    }
  }
  
  
  public static void delete(){
    View view = jEdit.getActiveView();
    FavoritesList cur = FavoritesList.getFavoritesList(view);
    if (cur != null){
      cur.delete();
      saveXML();
    }
  }
  
  public static void addGroup(){
    View view = jEdit.getActiveView();
    FavoritesList cur = FavoritesList.getFavoritesList(view);
    if (cur != null){
      cur.addGroup();
      saveXML();
    }
  }
  
  public static void addBuffers(){
    View view = jEdit.getActiveView();
    FavoritesList cur = FavoritesList.getFavoritesList(view);
    if (cur != null) {
      cur.addBuffers();
      saveXML();
    }
  }
  
  private static FileTreeNode loadHistory(){
    FileTreeNode root = null;
    
    List entrys = BufferHistory.getHistory();
    BufferHistory.Entry entry = null;
    root = new FileTreeNode(getHistoryKey());
    Iterator it = entrys.iterator();
    int historyOrder = 0;
    while(it.hasNext()){
      entry = (BufferHistory.Entry)it.next();
      root.addNode(new FileTreeNodeForHistory(MiscUtilities.getFileName(entry.path), entry.path, historyOrder++));
    }
    return root;
  }
  
  public static FileTreeNode createRoot(){
    FileTreeNode root = loadXML();
    if (root == null){
      root = new FileTreeNode("root");
    }
    root.addNode(loadHistory());
    return root;
  }
  
  private static FileTreeNode loadXML(){
    String settingPath = MiscUtilities.constructPath(jEdit.getSettingsDirectory(), "favorites");
    FileTreeNode root = null;
    File file = null;
    try {
      file = new File(settingPath,"favorites.xml");
      if (!file.exists()) {
        return root;
      }
    } catch (Exception e){
      Log.log(Log.ERROR, FavoritesPlugin.class, e);
      return root;
    }
    
    try {
      // SAXパーサーファクトリを生成
      SAXParserFactory spfactory = SAXParserFactory.newInstance();
      // SAXパーサーを生成
      SAXParser parser = spfactory.newSAXParser();
      // XMLファイルを指定されたデフォルトハンドラーで処理します
      FavoritesXmlHandler handler = new FavoritesXmlHandler();
      parser.parse(file, handler);
      root = handler.getRoot();
      
    } catch (Exception e){
      Log.log(Log.ERROR, FavoritesPlugin.class, e);
    }
    
    return root;
  }
  
  private static void saveXML(){
    View view = jEdit.getActiveView();
    FavoritesList cur = FavoritesList.getFavoritesList(view);
    if (cur == null ){
      return;
    }
    saveXML(cur);
  }
  
  private static void saveXML(FavoritesList cur){
    if (cur == null ){
      return;
    }
    FileTreeNode root = (FileTreeNode)cur.getRoot().clone();
    root.deleteNode(cur.getHistory());
    
    String settingPath = MiscUtilities.constructPath(jEdit.getSettingsDirectory(), "favorites");
    BufferedWriter bw = null;
    try {
      File file = new File(settingPath);
      if (!file.exists()){
        file.mkdirs();
      }
      bw = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(settingPath,"favorites.xml")),"UTF-8"));
    } catch (Exception e){
      Log.log(Log.ERROR, FavoritesPlugin.class, e);
      return;
    }
    
    saveFavorites(bw, root, true);
    
    try {
      if (bw != null){
        bw.close();
      }
    } catch (Exception e){
      Log.log(Log.ERROR, FavoritesPlugin.class, e);
    }
  }
  
  private static boolean saveFavorites(BufferedWriter bw, FileTreeNode node, boolean rootnode){
    boolean result = false;
    try {
      if (rootnode){
        // write header
        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        bw.newLine();
        // bw.write("<!DOCTYPE Favorites SYSTEM \"Favorites.dtd\">");
        // bw.newLine();
        // bw.newLine();
        // write Favorites name
      }
      
      bw.write("<GROUP name=\"");
      bw.write(node.getName());
      bw.write("\">");
      bw.newLine();
      
      Enumeration en = node.children();
      while(en.hasMoreElements()){
        FileTreeNode el = (FileTreeNode)en.nextElement();
        if (el.isLeaf()){
          // write files
          bw.write("  <FILE path=\"");
          String path = el.getPath();
          path = path.replace('\\','/');
          bw.write(path);
          bw.write("\"  name=\"");
          bw.write(el.getName());
          bw.write("\"  type=\"");
          bw.write(el.getType());
          bw.write("\"/>");
          bw.newLine();
          // bw.write("  </FILE>");
          // bw.newLine();
        } else {
          saveFavorites(bw, el, false);
        }
      }
      
      bw.write("</GROUP>");
      bw.newLine();
      
      result = true;
    } catch (Exception e){
      Log.log(Log.ERROR, FavoritesPlugin.class, e);
    }
    
    return result;
  }
  
  private static class FavoritesXmlHandler extends DefaultHandler {
    FileTreeNode node = null;
    
    public FileTreeNode getRoot(){
      return node;
    }
    
    public void startElement(String uri,String localName,String qName,Attributes attributes) {
      // System.out.println("start Element " + name);
      if ("GROUP".equals(qName)){
        String name = attributes.getValue("name");
        if (name == null){
          return;
        }
        FileTreeNode newNode = new FileTreeNode(name);
        if (node != null){
          node.addNode(newNode);
        }
        node = newNode;
      } else if ("FILE".equals(qName)){
        String name = attributes.getValue("name");
        if (name == null){
          return;
        }
        String path = attributes.getValue("path");
        if (path == null){
          return;
        }
        
        String type = attributes.getValue("type");
        if (type == null){
          type = FileTreeNode.TYPE_FILE;
        }
        
        FileTreeNode newNode = new FileTreeNode(name,path,type);
        if (node != null){
          node.addNode(newNode);
        }
      }
    }
    
    public void characters(char[] ch,int offset,int length) {
      // System.out.println("テキストデータ：" + new String(ch, offset, length));
    }
    
    public void endElement(String uri,String localName,String qName) {
      // System.out.println("end Element" + name);
      if ("GROUP".equals(qName)){
        if (node != null && node.getParent() != null){
          node = (FileTreeNode)node.getParent();
        }
      }
    }
  }
}


package favorites;

import java.util.*;

public class VectorHash implements Cloneable {
  private Vector vec = null;
  private HashMap hash = null;
  
  public VectorHash(){
    vec = new Vector();
    hash = new HashMap();
  }
  
  public void put(Object key, Object value){
    int index = 0;
    if (hash.containsKey(key)){
      index = ((Entry)hash.get(key)).index;
      vec.set(index,key);
    } else {
      index = vec.size();
      vec.add(key);
      
    }
    Entry e = new Entry(index, value);
    hash.put(key, e);
  }
  
  public boolean containsKey(Object key){
    return hash.containsKey(key);
  }
  
  public Object get(Object key){
    if (!hash.containsKey(key)){
      return null;
    }
    return ((Entry)hash.get(key)).value;
  }
  
  public Object get(int index){
    Object key = vec.get(index);
    return this.get(key);
  }
  
  public Object remove(Object key){
    if (!hash.containsKey(key)){
      return null;
    }
    
    Entry e = (Entry)hash.get(key);
    hash.remove(key);
    vec.remove(e.index);
    reIndex(e.index);
    return e.value;
  }
  
  public Object remove(int index){
    Object key = vec.get(index);
    return this.remove(key);
  }
  
  public void insert(Object key, Object value,int index ){
    vec.insertElementAt(key,index);
    Entry e = new Entry(index, value);
    hash.put(key, e);
    reIndex(index+1);
  }
  
  private void reIndex(int index){
    int length = vec.size();
    Entry e = null;
    for (int i = index; i < length; i++){
      e = (Entry)hash.get(vec.get(i));
      e.index = i;
    }
  }
  
  public int size(){
    return vec.size();
  }
  
  public int getIndex(Object key){
    if (!hash.containsKey(key)){
      return -1;
    }
    return ((Entry)hash.get(key)).index;
  }
  
  public Enumeration elements(){
    return new Enumeration() {
      int count = 0;
      VectorHash hash = (VectorHash)VectorHash.this.clone();
      // VectorHash<K,V> hash = VectorHash.this;
      
      public boolean hasMoreElements() {
        return count < hash.size();
      }
      
      public Object nextElement() {
        if (count < hash.size()) {
          return hash.get(count++);
        }
        throw new NoSuchElementException("Vector Enumeration");
	    }
    };
  }
  
  
  /**
  * VectorHash をコピーします。
  * コピーは内部データ配列の複製を参照します。
  * 
  *@return    VectorHash のコピー
  */
  public Object clone()  {
    try {
      VectorHash clone = (VectorHash)super.clone();
      
      synchronized (vec){
        clone.vec = (Vector)VectorHash.this.vec.clone();
        // clone.hash = (HashMap)VectorHash.this.hash.clone();
        
        clone.hash = new HashMap();
        Object key = null;
        for (int i = 0; i < vec.size(); i++){
          key = vec.get(i);
          clone.hash.put(key, ((Entry)VectorHash.this.hash.get(key)).clone());
        }
        
        
      }
      
      return clone;
    } catch (Exception e){
      // e.printStackTrace();
      throw new InternalError();
    }
  }
  
  
  public String toString(){
    int length = vec.size();
    Entry e = null;
    Object key = null;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++){
      key = vec.get(i);
      e = (Entry)hash.get(key);
      sb.append(i + ":"+key+":"+e.index+":"+e.value);
      sb.append("\n");
    }
    return sb.toString();
  }
  
  private static class Entry implements Cloneable {
    int index;
    Object value;
    Entry(int index, Object value){
      this.index = index;
      this.value = value;
    }
    
    public Object clone(){
      try {
        return super.clone();
      } catch (Exception e){
        throw new InternalError();
      }
    }
  }
}
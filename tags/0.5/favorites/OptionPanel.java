package favorites;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.EditPlugin;


public class OptionPanel extends AbstractOptionPane {

   // private JCheckBox showOnToolbar = null;

   public OptionPanel(  ) {
      super( "favorites" );
   }

   public void _init() {
      // addComponent( new JLabel( "<html><h3>Navigator</h3>" ) );
      // showOnToolbar = new JCheckBox( "Show Navigator on toolbar" );
      // showOnToolbar.setSelected( jEdit.getBooleanProperty( getName() + ".showOnToolbar", false ) );
      // addComponent( showOnToolbar );
   }

   public void _save() {
      // jEdit.setBooleanProperty( getName() + ".showOnToolbar", showOnToolbar.isSelected() );
   }

   public String getName() {
      return "favorites";
   }
}

package com.jhe.hexed;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by IntelliJ IDEA.
 * User: laullon
 * Date: 09-abr-2003
 * Time: 12:47:32
 */
public class JHexEditorHEX extends JComponent implements MouseListener,KeyListener
{
    private final JHexEditor jHexEditor;
    private int cursor=0;

    public JHexEditorHEX(JHexEditor jHexEditor)
    {
        this.jHexEditor = jHexEditor;
        addMouseListener(this);
        addKeyListener(this);
        addFocusListener(jHexEditor);
    }

    public Dimension getPreferredSize()
    {
        debug("getPreferredSize()");
        return getMinimumSize();
    }

    public Dimension getMaximumSize()
    {
        debug("getMaximumSize()");
        return getMinimumSize();
    }

    public Dimension getMinimumSize()
    {
        debug("getMinimumSize()");

        Dimension d=new Dimension();
        FontMetrics fn=getFontMetrics(jHexEditor.font);
        int h=fn.getHeight();
        int nl= jHexEditor.getLineas();
        d.setSize(((fn.stringWidth(" ")+1)*+((16*3)-1))+(jHexEditor.border*2)+1,h*nl+(jHexEditor.border*2)+1);
        return d;
    }

    static Color lighterGray = new Color(239, 239, 239);

    public void paint(Graphics g)
    {
        debug("paint("+g+")");
        debug("cursor="+ jHexEditor.cursor+" buff.length="+ jHexEditor.buff.length);
        Dimension d=getMinimumSize();
        g.setColor(Color.white);
        g.fillRect(0,0,d.width,d.height);

        g.setColor(lighterGray);
        g.fillRect(0,0, d.width /4, d.height);
        g.fillRect(d.width /2,0, d.width /4, d.height);

        g.setColor(Color.black);

        g.setFont(jHexEditor.font);

        int ini= jHexEditor.getInicio()*16;
        int fin=ini+(jHexEditor.getLineas()*16);
        if(fin> jHexEditor.buff.length) fin= jHexEditor.buff.length;

        //datos hex
        int x=0;
        int y=0;
        for(int n=ini;n<fin;n++)
        {
            if(n== jHexEditor.cursor)
            {
                if(hasFocus())
                {
                    g.setColor(Color.black);
                    jHexEditor.fondo(g,(x*3),y,2);
                    g.setColor(Color.blue);
                    jHexEditor.fondo(g,(x*3)+cursor,y,1);
                } else
                {
                    g.setColor(Color.blue);
                    jHexEditor.cuadro(g,(x*3),y,2);
                }

                if(hasFocus()) g.setColor(Color.white); else g.setColor(Color.black);
            } else
            {
                g.setColor(Color.black);
            }

            String s=("0"+Integer.toHexString(jHexEditor.buff[n]));
            s=s.substring(s.length()-2);
            jHexEditor.printString(g,s,((x++)*3),y);
            if(x==16)
            {
                x=0;
                y++;
            }
        }
    }

    private void debug(String s)
    {
        if(jHexEditor.DEBUG) System.out.println("JHexEditorHEX ==> "+s);
    }

    // calcular la posicion del raton
    public int calcularPosicionRaton(int x,int y)
    {
        FontMetrics fn=getFontMetrics(jHexEditor.font);
        x=x/((fn.stringWidth(" ")+1)*3);
        y=y/fn.getHeight();
        debug("x="+x+" ,y="+y);
        return x+((y+ jHexEditor.getInicio())*16);
    }

    // mouselistener
    public void mouseClicked(MouseEvent e)
    {
        debug("mouseClicked("+e+")");
        jHexEditor.cursor=calcularPosicionRaton(e.getX(),e.getY());
        this.requestFocus();
        jHexEditor.repaint();
    }

    public void mousePressed(MouseEvent e)
    {
    }

    public void mouseReleased(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    //KeyListener
    public void keyTyped(KeyEvent e)
    {
        debug("keyTyped("+e+")");

        char c=e.getKeyChar();
        if(((c>='0')&&(c<='9'))||((c>='A')&&(c<='F'))||((c>='a')&&(c<='f')))
        {
            char[] str=new char[2];
            String n="00"+Integer.toHexString((int) jHexEditor.buff[jHexEditor.cursor]);
            if(n.length()>2) n=n.substring(n.length()-2);
            str[1-cursor]=n.charAt(1-cursor);
            str[cursor]=e.getKeyChar();
            jHexEditor.buff[jHexEditor.cursor]=(byte)Integer.parseInt(new String(str),16);

            if(cursor!=1) cursor=1;
            else if(jHexEditor.cursor!=(jHexEditor.buff.length-1)){ jHexEditor.cursor++; cursor=0;}
            jHexEditor.actualizaCursor();
        }
    }

    public void keyPressed(KeyEvent e)
    {
        debug("keyPressed("+e+")");
        jHexEditor.keyPressed(e);
    }

    public void keyReleased(KeyEvent e)
    {
        debug("keyReleased("+e+")");
    }

    public boolean isFocusTraversable()
    {
        return true;
    }
}

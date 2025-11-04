import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class GameF extends JFrame {
	public GameF() {
        Container contentPane = getContentPane();
        final Panel panel = new Panel();
        panel.setBackground(new Color(255, 182, 147));
        contentPane.setBackground(new Color(255, 182, 147));
        contentPane.add(panel);
        setSize(560, 560);
        setTitle("My Gobang");
        setResizable(false);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JMenuBar menuBar=new JMenuBar();
        JMenu menu=new JMenu("选项");
        JMenuItem menuStart=new JMenuItem("开始游戏");
        menuStart.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
           	  panel.ResetGame();
           	  panel.repaint();
           }
        });
        JMenuItem menuExit =new JMenuItem("退出");
        menuExit.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
            	  System.exit(0);
            }
         });
        
        // 添加主题菜单
        JMenu themeMenu = new JMenu("主题");
        JMenuItem blackTheme = new JMenuItem("黑色主题");
        blackTheme.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                panel.setTheme(Color.BLACK);
                panel.repaint();
            }
        });
        JMenuItem whiteTheme = new JMenuItem("白色主题");
        whiteTheme.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                panel.setTheme(Color.WHITE);
                panel.repaint();
            }
        });
        JMenuItem grayTheme = new JMenuItem("灰色主题");
        grayTheme.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                panel.setTheme(Color.GRAY);
                panel.repaint();
            }
        });
        
        menuBar.add(menu);
        menu.add(menuStart);
        menu.add(menuExit);
        menuBar.add(themeMenu);
        themeMenu.add(blackTheme);
        themeMenu.add(whiteTheme);
        themeMenu.add(grayTheme);
        this.setJMenuBar(menuBar);
      }
}

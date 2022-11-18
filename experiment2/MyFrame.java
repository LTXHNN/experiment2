package experiment2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author 李天翔
 * @date 2022/05/20
 **/
public class MyFrame extends JFrame {
    static JButton btnLL1 = new JButton("LL(1)分析");
    static JPanel MyPanel = new JPanel();
    static Vector<String> column = new Vector<String>();
    static Vector<String> row = new Vector<>();
    static JTable table;
    static JTextArea area = new JTextArea();

    public static void main(String[] args) {
        LL_1.init();
        LL_1.creatFirstMap();
        LL_1.creatFirstSMap();
        LL_1.createFollowMap();
        LL_1.creatTable();
        LL_1.print();

        new MyFrame("LL(1)分析界面");
    }

    MyFrame(String title) {
        super(title);
        setSize(600, 700);
        setResizable(false);
        MyPanel.setLayout(null);
        column.add("步骤");
        column.add("符号栈");
        column.add("输入串");
        column.add("产生式");
        table = new JTable(row, column);
        area.setFont(new Font("宋体", Font.PLAIN, 25));
        //area.setText("i+i*i#");
        JScrollPane scrollPanel1 = new JScrollPane(table);
        JScrollPane scrollPanel2 = new JScrollPane(area);
        MyPanel.add(btnLL1);
        MyPanel.add(scrollPanel1);
        MyPanel.add(scrollPanel2);
        btnLL1.setBounds(450, 160, 100, 30);
        scrollPanel1.setBounds(20, 200, 540, 450);
        scrollPanel2.setBounds(20, 5, 540, 150);
        btnLL1.addActionListener(new Listener());
        this.add(MyPanel);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}


class Listener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        if (actionEvent.getSource() == MyFrame.btnLL1) {
            LL_1.inputString = MyFrame.area.getText();
            LL_1.analyze();
            LL_1.showAnalysis();
            for (int i = 0; i < LL_1.stepsList.size(); i++) {
                Object[] o = new Object[4];
                o[0] = LL_1.stepsList.get(i).numStep;
                o[1] = LL_1.stepsList.get(i).stackString;
                o[2] = LL_1.stepsList.get(i).inString;
                o[3] = LL_1.stepsList.get(i).productString;
                ((DefaultTableModel) MyFrame.table.getModel()).addRow(o);
            }


        }
    }
}

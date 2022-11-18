package experiment2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * @author 李天翔
 * @date 2022/05/18
 **/
@SuppressWarnings({"all"})
public class LL_1 {
    private static String[] grammar = {"E->TG", "G->+TG|—TG", "G->ε", "T->FS", "S->*FS|/FS", "S->ε", "F->(E)", "F->i"};
    static HashSet<Character> vtSet = new HashSet<>();//终结符
    static HashSet<Character> vnSet = new HashSet<>();//非终结符
    static HashMap<Character, ArrayList<String>> productionMap = new HashMap<>();//产生式Map
    static HashMap<Character, HashSet<Character>> firstMap = new HashMap<>();//任意一个文化符号的first集
    static HashMap<String, HashSet<Character>> firstSMap = new HashMap<>();//非终极符任意候选的first集
    static HashMap<Character, HashSet<Character>> followMap = new HashMap<>();//follow集
    static ArrayList<Step> stepsList = new ArrayList<>();
    static Stack<Character> stack = new Stack<>();//符号栈
    static String[][] table;//构建分析表
    static char start;//开始符号
    static int rowl;//分析表总行
    static int coll;//分析表总列
    static String inputString = "";

    /**
     * 用来得到终结符集和非终结符集
     */
    public static void init() {
        start = grammar[0].charAt(0);
        //生成终结符和非终结符集
        for (int i = 0; i < grammar.length; i++) {
            char[] temp = grammar[i].replace("->", "").replace("|", "").toCharArray();//去除->

            for (int j = 0; j < temp.length; j++) {
                if (temp[j] >= 'A' && temp[j] <= 'Z') {
                    vnSet.add(temp[j]);//大写字母为非终结符
                } else {
                    vtSet.add(temp[j]);
                }
            }
        }

        //生成每个非终结符对应的产生式
        for (String str : grammar) {
            String[] strings = str.split("->")[1].split("\\|");
            char ch = str.charAt(0);
            ArrayList<String> list = productionMap.containsKey(ch) ? productionMap.get(ch) : new ArrayList<String>();
            for (String S : strings) {
                list.add(S);
            }
            productionMap.put(str.charAt(0), list);
            System.out.println(str.charAt(0) + "\t" + list);
        }

        System.out.println(vnSet + "***" + vnSet);
    }

    /**
     * 得到所有符号的first集
     */
    public static void creatFirstMap() {
        for (Character c : vtSet) {
            createFirstSet(c);
        }
        for (Character c : vnSet) {
            createFirstSet(c);
        }
    }

    /**
     * 得到一个符号的first集
     *
     * @param c
     */
    public static void createFirstSet(Character c) {
        HashSet<Character> set = firstMap.containsKey(c) ? firstMap.get(c) : new HashSet<>();
        if (vtSet.contains(c)) {//如果是终结符，直接加入本身
            set.add(c);
            firstMap.put(c, set);
        }
        if (vnSet.contains(c)) {//非终极符
            ArrayList<String> productionMap_C = productionMap.get(c);
            //ch为vn
            for (String str : productionMap_C) {
                int i = 0;
                while (i < str.length()) {
                    char v = str.charAt(i);
                    //递归
                    if(!firstMap.containsKey(v)){
                        createFirstSet(v);
                    }
                    HashSet<Character> tvSet = firstMap.get(v);
                    // 将其first集加入左部
                    for (Character tmp : tvSet) {
                        if (tmp != 'ε')
                            set.add(tmp);
                    }
                    // 若包含空串 处理下一个符号
                    if (tvSet.contains('ε'))
                        i++;
                        // 否则退出 处理下一个产生式
                    else
                        break;
                }
                if (i == str.length())
                    set.add('ε');
            }
        }
        firstMap.put(c, set);
    }

    /**
     * 得到一个候选式的first集
     *
     * @param s
     */
    public static void creatFirstSSet(String s) {
        HashSet<Character> set = (firstSMap.containsKey(s)) ? firstSMap.get(s) : new HashSet<Character>();
        // 从左往右扫描该式
        int i = 0;
        while (i < s.length()) {
            char v = s.charAt(i);
            if (!firstMap.containsKey(v))
                createFirstSet(v);
            HashSet<Character> vSet = firstMap.get(v);
            // 将其非空first集加入左部
            for (Character tmp : vSet)
                if (tmp != 'ε')
                    set.add(tmp);
            // 若包含空串处理下一个符号
            if (vSet.contains('ε'))
                i++;
                // 否则结束
            else
                break;
        }
        //所有符号的first集都包含空串 把空串加入
        if (i == s.length()) {
            set.add('ε');
        }
        firstSMap.put(s, set);
    }

    /**
     * 得到非终极符的任意候选式的first集
     */
    public static void creatFirstSMap() {
        Collection<ArrayList<String>> values = productionMap.values();
        for (ArrayList<String> arrayList : values) {//遍历候选式
            for (int i = 0; i < arrayList.size(); i++) {
                creatFirstSSet(arrayList.get(i));
            }
        }
    }

    /**
     * 得到follow集
     */
    public static void createFollowMap() {
        int l = 0;
        while (true) {
            int len = 0;
            for (Character ch : vnSet) {
                createFollowSet(ch);
            }
            for (Character ch : vnSet) {
                len += followMap.get(ch).size();
            }
            if (l != len)
                l = len;
            else
                break;
        }
    }

    /**
     * 创建一个非终极符的follow集
     *
     * @param ch 代表一个非终极符
     */
    public static void createFollowSet(Character ch) {
        HashSet<Character> hashSet = followMap.containsKey(ch) ? followMap.get(ch) : new HashSet<Character>();
        if (ch == start) {
            hashSet.add('#');
        }
        boolean isEnd = true;
        Set<Character> keySet = productionMap.keySet();
        for (Character key : keySet) {//对每一个非终极符，遍历所有的产生式，
            ArrayList<String> arrayList = productionMap.get(key);
            for (int i = 0; i < arrayList.size(); i++) {
                if (arrayList.get(i).contains(ch + "")) {//找到含有该终结符的产生式
                    char[] str = arrayList.get(i).toCharArray();
                    for (int j = 0; j < str.length; j++) {
                        isEnd = true;
                        if (str[j] == ch) {//找到该产生式中终结符的位置
                            int k = j + 1;
                            for (; k < str.length; k++) {
                                HashSet<Character> set = firstMap.get(str[k]);
                                for (Character character : set) {
                                    if(character!='ε'){
                                        hashSet.add(character);
                                    }
                                }
                                if (!set.contains('ε')) {
                                    isEnd = false;
                                    break;
                                }
                            }
                            //如果ch是产生式中最后一个非终极符，或者产生式后面的非终极符包含空
                            if (j == str.length - 1 || isEnd == true) {
                                HashSet<Character> hashSetA = followMap.containsKey(key) ? followMap.get(key) : new HashSet<Character>();
                                hashSet.addAll(hashSetA);
                            }
                        }
                    }
                }
            }
        }
        followMap.put(ch, hashSet);
    }

    /**
     * 创建分析表M[A,a]
     */
    public static void creatTable() {
        Set<Character> relVtSet = new HashSet<>();
        relVtSet.addAll(vtSet);
        relVtSet.remove('ε');
        //初始化table
        rowl = vnSet.size() + 1;
        coll = relVtSet.size() + 2;
        table = new String[rowl][coll];
        table[0][0] = "";
        for (int i = 1; i < rowl; i++) {
            for (int j = 1; j < coll; j++) {
                table[i][j] = "error";
            }
        }
        table[0][coll - 1] = '#' + "";
        int row = 1;
        int col = 1;
        for (Character ch : vnSet) {
            table[row][0] = ch + "";
            row++;
        }
        for (Character ch : relVtSet) {
            table[0][col] = ch + "";
            col++;
        }

        row = 1;
        for (int i = 1; i < rowl; i++) {
            String s = table[row][0];
            ArrayList<String> list = productionMap.get(s.charAt(0));
            col = 1;
            for (int j = 0; j < list.size(); j++) {
                HashSet<Character> hashSet = firstSMap.get(list.get(j));
                for (int k = 1; k < coll; k++) {
                    if (hashSet.contains(table[0][k].charAt(0))) {
                        table[row][k] = list.get(j);
                    }
                }

                if (hashSet.contains('ε')) {
                    for (int k = 1; k < coll; k++) {
                        if (followMap.get(s.charAt(0)).contains(table[0][k].charAt(0))) {
                            table[row][k] = list.get(j);
                        }
                    }
                }
            }
            row++;
        }
    }

    /**
     * 打印构建fist,follow和table
     */
    public static void print() {
        System.out.println("table表");
        for (int i = 0; i < rowl; i++) {
            for (int j = 0; j < coll; j++) {
                System.out.print(table[i][j] + '\t');
            }
            System.out.println();
        }
        System.out.println("fist集");
        for (Character ch : vnSet) {
            HashSet<Character> hashSet = firstMap.get(ch);
            System.out.print(ch + "\t");
            for (Character character : hashSet) {
                System.out.print(character + "\t");
            }
            System.out.println();
        }
        System.out.println("follow集");
        for (Character ch : vnSet) {
            System.out.print(ch + "\t");
            HashSet<Character> hashSet = followMap.get(ch);
            for (Character character : hashSet) {
                System.out.print(character + "\t");
            }
            System.out.println();
        }
        System.out.println("fistS集");

        Set<String> strings = firstSMap.keySet();
        for (String s : strings) {
            System.out.print(s + "\t");
            HashSet<Character> hashSet = firstSMap.get(s);
            for (Character ch : hashSet) {
                System.out.print(ch + "\t");
            }
            System.out.println();

        }
    }

    /**
     * 返回栈中所有字符
     * @return
     */
    static String getStack() {
        String str = "";
        for (Character ch : stack) {
            str += ch;
        }
        return str;
    }

    public static void analyze() {
        String s = inputString;
        int steps = 0;
        Step p;
        char[] input = s.toCharArray();
        String productionStr = "";
        stack.push('#');
        stack.push(start);
        p = new Step(steps, getStack(), s, productionStr);
        stepsList.add(p);
        steps++;
        productionStr = "";
        char temp;
        char topX;
        for (int i = 0; i < input.length; ) {
            topX = stack.peek();//栈顶元素
            temp = input[i];//当前输入字符
            if (topX == temp && topX == '#') {
                //System.out.println("分析结束");
                stack.pop();
                return;
            }
            if (topX == temp && topX != '#') {//如果栈
                stack.pop();
                i++;
                p = new Step(steps, getStack(), s.substring(i), productionStr);
                stepsList.add(p);
                steps++;
                productionStr = "";
                continue;
            }
            if (vnSet.contains(topX)) {//如果栈顶是非终结符
                for (int j = 1; j < rowl; j++) {
                    if (table[j][0].equals(topX + "")) {

                        for (int k = 1; k < coll; k++) {
                            if (table[0][k].equals(temp + "")) {//查找分析表
                                if (table[j][k].equals("error")) {
                                    System.out.println("error");
                                } else {
                                    productionStr = table[j][k];//表中存在产生式
                                    stack.pop();//
                                    if (!productionStr.equals("ε")) {//产生式不为空
                                        char[] pro = productionStr.toCharArray();
                                        for (int l = pro.length - 1; l >= 0; l--) {
                                            stack.push(pro[l]);//逆序入栈
                                        }
                                    }
                                    productionStr = topX + "->" + productionStr;
                                    p = new Step(steps, getStack(), s.substring(i), productionStr);
                                    stepsList.add(p);
                                    productionStr = "";
                                    steps++;
                                    break;
                                }
                            }
                        }
                        break;
                    }

                }
            }
        }

    }

    public static void showAnalysis() {
        System.out.println("步骤\t\t" + "符号栈\t\t" + "输入串\t\t" + "所用产生式");
        for (int i = 0; i < stepsList.size(); i++) {
            System.out.println(stepsList.get(i));
        }
    }

    public static void main(String[] args) {
        init();
        creatFirstMap();
        creatFirstSMap();
        createFollowMap();
        creatTable();
        print();
        Scanner s = new Scanner(System.in);
        inputString = s.nextLine();
        analyze();
        showAnalysis();
    }

}
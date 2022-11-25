//package src;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Counter {


	// TO DO  Add necessary fileds
    //映射方法和对应的信息
    public static HashMap<String, Integer> passLine;
    public static HashMap<String, Integer> pass_branch;
    public static HashMap<String, Integer> totalLine;
    public static HashMap<String, Integer> totalBranch;
    public static HashMap<String, Set<Integer>> bennLines;
    public static HashMap<String, Set<Integer>> bennBranch;
    static {
//      hook addShutdownHook，类似于junit的@AfterClass
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Counter.generateReport();
            } catch (IOException e) {
                System.err.println("Could not write report to file: " + e);
            }
        }));
    }
    //pan chong
    public static synchronized void recordLines(String name, String line_num_string){
        Integer line_num = Integer.parseInt(line_num_string);
        if(bennLines.get(name).contains(line_num))return;
        bennLines.get(name).add(line_num);
        passLine.merge(name, 1, Integer::sum);
    }
    public static synchronized void recordBranch(String name, Integer line_num){
        if(bennBranch.get(name).contains(line_num))return;
        bennBranch.get(name).add(line_num);
        pass_branch.merge(name, 1, Integer::sum);
    }
    //设置数量 不同的是，line的数量可以直接得到，branch的数量需要动态的获取 这里不用插桩
    public static void setTotalLine(String s, Integer num){
        totalLine.put(s, num);
    }
    public static void addTotalBranch(String s, Integer num){
        totalBranch.merge(s, num, Integer::sum);
    }
    // Add necessary memboer functions
    public static void  generateReport() throws IOException{
        File dir = new File("report");
        if(!dir.exists())
            dir.mkdir();
        FileWriter fw = new FileWriter("report/result.txt");
        for(Map.Entry<String, Integer> entry : passLine.entrySet()) {
            String key = entry.getKey();
            String ans;
            Double a1 = 1.0 * (double) passLine.get(key) / (double) totalLine.get(key);
            Double a2 = 1.0 * (double) pass_branch.get(key) / (double) totalBranch.get(key);
            ans = key + " pass_rate: " + a1.toString() + " branch_rate: " + a2.toString();
            System.out.println(ans);
        }
        fw.close();
    }
}

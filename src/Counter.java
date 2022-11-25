//package src;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Counter {


	// TO DO  Add necessary fileds

    public static HashMap<String, Integer> passLine  = new HashMap<>();
    public static HashMap<String, Integer> pass_branch = new HashMap<>();
    public static HashMap<String, Integer> totalLine = new HashMap<>();
    public static HashMap<String, Integer> totalBranch = new HashMap<>();
    public static HashMap<String, Set<Integer>> bennLines = new HashMap<>();
    public static HashMap<String, Set<Integer>> bennBranch = new HashMap<>();
    static {

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
        if(bennLines.get(name) == null){
            bennLines.put(name,  new HashSet<Integer>());
        }
        if(bennLines.get(name) != null){
        if(bennLines.get(name).contains(line_num))return;}
        bennLines.get(name).add(line_num);
        passLine.merge(name, 1, Integer::sum);
    }
    public static synchronized void recordBranch(String name, String line_num_string){
        Integer line_num = Integer.parseInt(line_num_string);
        if(bennBranch.get(name) == null){
            bennBranch.put(name,  new HashSet<Integer>());
        }
        if(bennBranch.get(name).contains(line_num))return;
        bennBranch.get(name).add(line_num);
        pass_branch.merge(name, 1, Integer::sum);
    }

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
            if(passLine.get(key) != null && totalLine.get(key) != null && pass_branch.get(key) != null && totalBranch.get(key) != null){
            Double a1 = 1.0 * (double) passLine.get(key) / (double) totalLine.get(key);
            Double a2 = 1.0 * (double) pass_branch.get(key) / (double) totalBranch.get(key);
            ans = key + " pass_rate: " + a1.toString() + " branch_rate: " + a2.toString();
            fw.write(ans);}
        }
        fw.close();
    }

}

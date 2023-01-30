//package src;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Counter {


    // TO DO  Add necessary fileds
    private static LinkedList<LineCounter> lineCounter = new LinkedList<>();
    private static LinkedList<BranchCounter> branchCounter = new LinkedList<>();
//    private static Map<String, String> reporter = new HashMap<>();


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

    // Add necessary memboer functions
    public static synchronized void recordTotalLineAndBranch(String className, String methodName, int totalLine, int totalBranch) {
        for (BranchCounter bc : branchCounter) {
            if (bc.className.equals(className) && bc.methodName.equals(methodName))
                return;
        }
        lineCounter.add(new LineCounter(className, methodName, new HashSet<>(), totalLine));
        branchCounter.add(new BranchCounter(className, methodName, new HashSet<>(), totalBranch));

//        reporter.putIfAbsent(className, "");
    }

    public static synchronized void recordRuntimeBranch(String className, String methodName, int flag) {
        for (BranchCounter bc : branchCounter) {
            if (bc.className.equals(className) && bc.methodName.equals(methodName)) {
                bc.passBranch.add(flag);
            }
        }
    }

    public static synchronized void recordRuntimeLine(String className, String methodName, int line) {
        for (LineCounter lc : lineCounter) {
            if (lc.className.equals(className) && lc.methodName.equals(methodName)) {
                lc.passLine.add(line);
            }
        }
    }

    /**
     * reports the counter content.
     */
    public static void generateReport() throws IOException {
        File dir = new File("report");
        if (!dir.exists())
            dir.mkdir();
        FileWriter fileWriter = new FileWriter("report/results.txt");

//        fileWriter.write("total GOTO statement\n");
//        for (Map.Entry entry : gotoCount.entrySet())
//            fileWriter.write(entry.getKey() + "\t" + entry.getValue() + "\n");

//        sum();
        fileWriter.write("MethodName\tCoveredStatement\tTotalStatement\tStatementCoverage\t" +
                "CoveredBranch\tTotalBranch\tBranchCoverage\n");
        for (LineCounter lc : lineCounter) {
            String className = lc.className;
            String methodSig = lc.methodName;
            String methodName = methodSig.substring(methodSig.indexOf(" ") + 1, methodSig.indexOf("("));
            float lineCoverage = lc.totalLine == 0 ? 0 : (float) lc.passLine.size() / (float) lc.totalLine;
            fileWriter.write(className + "." + methodName + "\t" + lc.passLine.size() + "\t" + lc.totalLine + "\t" +
                    lineCoverage + "\t");
            for (BranchCounter bc : branchCounter) {
                float bcCoverage = bc.totalBranch == 0 ? 0 : (float) bc.passBranch.size() / (float) bc.totalBranch;
                if (bc.className.equals(className) && bc.methodName.equals(methodSig)) {
                    fileWriter.write(+bc.passBranch.size() + "\t" + bc.totalBranch  + "\t" +
                            bcCoverage + "\n");
                }
            }
        }
        fileWriter.close();
        System.out.printf("report is generated at %s\n", "report/results.txt");
    }

//    static void sum() {
//        int passBranch;
//        int total;
//        for (Map.Entry<String, String> entry : reporter.entrySet()) {
//            passBranch = 0;
//            total = 0;
//            for (BranchCounter bc : branchCounter) {
//                if (entry.getKey().equals(bc.className)) {
//                    passBranch += bc.passBranch.size();
//                    total += bc.totalBranch;
//                }
//            }
//            entry.setValue(passBranch + "/" + total);
//        }
//    }
}

class BranchCounter {
    String className;
    String methodName;
    Set<Integer> passBranch;
    int totalBranch;

    public BranchCounter(String className, String methodName, Set<Integer> passBranch, int totalBranch) {
        this.className = className;
        this.methodName = methodName;
        this.passBranch = passBranch;
        this.totalBranch = totalBranch;
    }
}

class LineCounter {
    String className;
    String methodName;
    Set<Integer> passLine;
    int totalLine;

    public LineCounter(String className, String methodName, Set<Integer> passLine, int totalLine) {
        this.className = className;
        this.methodName = methodName;
        this.passLine = passLine;
        this.totalLine = totalLine;
    }
}

//package instrument;

import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JGotoStmt;

import soot.toolkits.graph.*;
import soot.util.Chain;

import java.util.*;

public class Instrumenter extends BodyTransformer {
    private static SootClass counterClass;
    //    private static SootMethod recordGoto;
    private static SootMethod recordTotalLineAndBranch;
    private static SootMethod recordRuntimeBranch;
    private static SootMethod recordRuntimeLine;

    //    protected static SootMethod counterV;
    private Set<Unit> visitedOnce;
    private int uniqueFlag;

    // TO DO  add necessary fields
    static {
        // TO DO add necessary logic
        //        注册 Counter 以及 recordGoto
        counterClass = Scene.v().loadClassAndSupport("Counter");
//        recordGoto = counterClass.getMethod("void recordGoto(java.lang.String)");
        recordTotalLineAndBranch = counterClass.getMethod("void recordTotalLineAndBranch(java.lang.String,java.lang.String,int,int)");
        recordRuntimeBranch = counterClass.getMethod("void recordRuntimeBranch(java.lang.String,java.lang.String,int)");
        recordRuntimeLine = counterClass.getMethod("void recordRuntimeLine(java.lang.String,java.lang.String,int)");
    }

    @Override
    protected synchronized void internalTransform(Body body, String s, Map<String, String> map) {
        // TO DO add necessary logic
        Set<Integer> lineSet = new HashSet<>();
        String clazz = body.getMethod().getDeclaringClass().getName();
        UnitGraph g = new BriefUnitGraph(body);
        visitedOnce = new HashSet<>();
        uniqueFlag = 0;

        Chain units = body.getUnits();
        SootMethod method = body.getMethod();
        System.out.println("instrumenting method : " + clazz + method.getSignature());

        // iterate unites
        Iterator stmtIt = units.snapshotIterator();
        while (stmtIt.hasNext()) {
            Stmt stmt = (Stmt) stmtIt.next();

            int line = stmt.getJavaSourceStartLineNumber();
            if (!lineSet.contains(line) && line != -1) {
                lineSet.add(line);
                InvokeExpr runtimeLineExpr = Jimple.v().newStaticInvokeExpr(
                        recordRuntimeLine.makeRef(),
                        StringConstant.v(g.getBody().getMethod().getDeclaringClass().getName()),
                        StringConstant.v(g.getBody().getMethod().getSubSignature()),
                        IntConstant.v(line));
                Stmt runtimeLineStmt = Jimple.v().newInvokeStmt(runtimeLineExpr);
                units.insertBefore(runtimeLineStmt, stmt);
            }


            if (stmt instanceof IfStmt || stmt instanceof TableSwitchStmt || stmt instanceof LookupSwitchStmt) {
//                   插入 Counter.recordRuntimeBranch(classname,methodSign,uniqueFlag)
                for (Unit u : g.getSuccsOf(stmt)) {
                    if (!visitedOnce.contains(u)) {
                        InvokeExpr runtimeBranchExpr = Jimple.v().newStaticInvokeExpr(
                                recordRuntimeBranch.makeRef(),
                                StringConstant.v(g.getBody().getMethod().getDeclaringClass().getName()),
                                StringConstant.v(g.getBody().getMethod().getSubSignature()),
                                IntConstant.v(uniqueFlag++));
                        Stmt runtimeBranchStmt = Jimple.v().newInvokeStmt(runtimeBranchExpr);
                        units.insertBefore(runtimeBranchStmt, u);
                        visitedOnce.add(u);
                    }
                }
            }
        }
        SootMethod clinit = getClassInitMethod(method.getDeclaringClass());
//      Counter.recordTotalBranch(classname,totalBranch)
        InvokeExpr totalBranchExpr = Jimple.v().newStaticInvokeExpr(
                recordTotalLineAndBranch.makeRef(),
                StringConstant.v(clazz),
                StringConstant.v(method.getSubSignature()),
                IntConstant.v(lineSet.size()),
                IntConstant.v(uniqueFlag));
        Stmt totalBranchStmt = Jimple.v().newInvokeStmt(totalBranchExpr);
        clinit.getActiveBody().getUnits().insertBefore(totalBranchStmt, clinit.getActiveBody().getUnits().getFirst());
    }

    // TO DO you can add necessary member functions
    public static SootMethod getClassInitMethod(SootClass sootClass) {
        SootMethod clinit = sootClass.getMethodUnsafe("void <clinit>()");
        if (clinit == null) {
            clinit = new SootMethod("<clinit>",
                    null,
                    VoidType.v(), Modifier.STATIC);
            sootClass.addMethod(clinit);
            JimpleBody body = Jimple.v().newBody(clinit);
            clinit.setActiveBody(body);
            body.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
        return clinit;
    }
}

//package instrument;

import com.sun.javafx.binding.IntegerConstant;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JGotoStmt;

import soot.toolkits.graph.*;
import soot.util.Chain;

import java.util.*;

public class Instrumenter extends BodyTransformer {
    static SootClass  counterClass;

    static SootMethod recordLines;
    static SootMethod recordBranch;
	// TO DO  add necessary fields
    static {
	// TO DO add necessary logic
        counterClass = Scene.v().loadClassAndSupport("Counter");
        recordLines = counterClass.getMethod("void recordLines(java.lang.String,java.lang.String)");
        recordBranch = counterClass.getMethod("void recordBranch(java.lang.String,java.lang.String)");
    }

    @Override
    protected synchronized void internalTransform(Body body, String s, Map<String, String> map) {
	// TO DO add necessary logic
        String clazz = body.getMethod().getDeclaringClass().getName();
        SootMethod method= body.getMethod();

        Chain units = body.getUnits();
        //System.out.println(method.toString());
        Counter.setTotalLine(method.toString(), units.size());
        Iterator stmtIt = units.snapshotIterator();
        UnitGraph g = new BriefUnitGraph(body);
        Integer line;
        Integer last_line = -2;
        while(stmtIt.hasNext()){
            Stmt stmt = (Stmt) stmtIt.next();
            line = stmt.getJavaSourceStartLineNumber();
            //System.out.println(method.getSignature());
            //System.out.println(method.getActiveBody().getUnits().size());
            if(method.toString().startsWith("<org.apache.commons.math3.util.FastMath: void <clinit>()>")){
                int a = 1;
                int b = a;
            }
            if(method.getActiveBody().getUnits().size() == 381 && method.toString().startsWith("<org.apache.commons.math3.util.FastMath: void <clinit>()>")){
                int a = 1;
                int b = a;
            }

            if (line != -1 && !line.equals(last_line)) {
                    InvokeExpr Count_fuc = Jimple.v().newStaticInvokeExpr(recordLines.makeRef(), StringConstant.v(method.toString()), StringConstant.v(line.toString()));
                    Stmt count_stmt = Jimple.v().newInvokeStmt(Count_fuc);
                    units.insertBefore(count_stmt, stmt);
            }
            last_line = line;

            if(stmt instanceof IfStmt || stmt instanceof  TableSwitchStmt || stmt instanceof LookupSwitchStmt){
                Counter.addTotalBranch(method.toString(), g.getSuccsOf(stmt).size());
                for(Unit u : g.getSuccsOf(stmt)){
                    // zai houji zhiqian cha zhuang
                    Stmt st = (Stmt)u;
                    Integer line2 = st.getJavaSourceStartLineNumber();
                    InvokeExpr Count_fuc2 = Jimple.v().newStaticInvokeExpr(recordBranch.makeRef(), StringConstant.v(method.toString()), StringConstant.v(line2.toString()));
                    Stmt count_stmt2 = Jimple.v().newInvokeStmt(Count_fuc2);
                    units.insertBefore(count_stmt2, st);
                }
            }
        }



        }

	// TO DO you can add necessary member functions
}

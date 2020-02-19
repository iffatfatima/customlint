package com.greense.detector.mylibrary;

import com.android.annotations.NonNull;
import com.android.tools.lint.client.api.JavaEvaluator;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.ClassContext;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiMethod;

import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode;
import org.jetbrains.uast.UCallExpression;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RLDetector extends Detector implements Detector.UastScanner, Detector.ClassScanner {

    private List<Method> mPendingMethods = new ArrayList<>();
    private String caller = "";

    @Override
    public List<String> getApplicableConstructorTypes() {
        return Collections.singletonList("android.hardware.Camera");
    }

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList("open", "release", "unsubscribe", "onStop");
    }

    @Override
    public void visitConstructor(JavaContext context, UCallExpression call, PsiMethod method) {
        System.out.print(call);
    }

    @Override
    public void visitMethod(JavaContext context, UCallExpression call, PsiMethod method) {
        JavaEvaluator evaluator = context.getEvaluator();
        mPendingMethods.add(new Method(context, call, method));
        if (evaluator.isMemberInClass(method, "android.hardware.Camera")) {
        }
        if(method.getName().equals("openCamera")) {
            caller = Objects.requireNonNull(Objects.requireNonNull(call.getUastParent()).getSourcePsi()).getFirstChild().getFirstChild().getText();
        }
    }

    @Override
    public void checkClass(@NonNull ClassContext context, @NonNull ClassNode classNode) {
        System.out.print(classNode.methods);

    }
    @Override
    public void afterCheckFile(Context context){
        for(Method method : mPendingMethods){
            if(getMethodName(method.call).equals("onStop()")){
                getFix(method.call);
                method.context.report(ISSUE_RL, method.call,
                        method.context.getLocation(method.call),
                        "Must not call async operations on main thread",
                        getFix(method.call)
                );
            }
        }
    }

    @Override
    public int[] getApplicableAsmNodeTypes(){
        return AbstractInsnNode.
    }
    public String getMethodName(UCallExpression call){
        return Objects.requireNonNull(Objects.requireNonNull(call.getUastParent()).getSourcePsi()).getFirstChild().getFirstChild().getText();

    }

    private LintFix getFix(UCallExpression call) {
        String fix = caller + ".release()";
        String logCallSource = call.asSourceString();
        call.resolve().getBody().getStatements();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().beginning().with(fix).build());
        return fixGrouper.build();
    }

    static final Issue ISSUE_RL=
            Issue.create("Heavy async Task",
                    "Must not call async operations on main thread",
                    "Async operations if carried out on main thread, may lead to blocking the UI for heavy operations",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(RLDetector.class, Scope.JAVA_FILE_SCOPE));
}
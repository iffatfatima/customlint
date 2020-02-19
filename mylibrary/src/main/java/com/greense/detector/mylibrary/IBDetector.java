package com.greense.detector.mylibrary;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UIfExpression;
import org.jetbrains.uast.ULambdaExpression;
import org.jetbrains.uast.UTypeReferenceExpression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class IBDetector extends Detector implements Detector.UastScanner {

    private static final String SERVICE_CLASS = "android.app.Service";
    private final String START_ACTIVITY = "startActivity";
    private final String START_ACTIVITIES = "startActivities";
    private boolean isService = true;

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList(START_ACTIVITY, START_ACTIVITIES);
    }

    @Override
    public List<String> applicableSuperClasses() {
        return Collections.singletonList("android.app.Service");
    }

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.singletonList(UIfExpression.class);
    }

//    @Override
//    public UElementHandler createUastHandler(JavaContext context){
//
//        return new MyUElementHandler();
//    }

    @Override
    public final void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression call, @NotNull PsiMethod method) {
        for (UClass myclass: Objects.requireNonNull(context.getUastFile()).getClasses()){
            if(Objects.equals(Objects.requireNonNull(myclass.getSuperClass()).getQualifiedName(), SERVICE_CLASS)){
                if (method.getName().equals(START_ACTIVITY) || method.getName().equals(START_ACTIVITIES)) {
                    new MyUElementHandler();
                    String pattern = "importance==ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND".trim();
                    String condition = Objects.requireNonNull(Objects.requireNonNull(call.getUastParent()).getSourcePsi()).getParent().getText();
                    condition = condition.replaceAll("\\s+", "");
                    if(!condition.trim().contains(pattern)) {
                        context.report(ISSUE_IB, call,
                                context.getLocation(call),
                                "Immortality Bug",
                                getFix(call)
                        );
                    }
                }
            }
        }
    }

    @Override
    public void visitClass(JavaContext context, ULambdaExpression lambda){
        lambda.getUastParent().getSourcePsi();
    }

    @Override
    public void visitClass(JavaContext context, UClass uclass){
        for (UTypeReferenceExpression utypeReferenceExpression: uclass.getUastSuperTypes()) {
            if(utypeReferenceExpression.getQualifiedName().equals(SERVICE_CLASS)){
                isService = true;
            }
            else{
                isService = false;
            }
        }
    }

    private LintFix getFix(UCallExpression call) {
        String fix = "ActivityManager activityManager = (ActivityManager) getSystemService( Context.ACTIVITY_SERVICE );\n" +
                "List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();\n" +
                "for(RunningAppProcessInfo appProcess : appProcesses){\n" +
                "    if(appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){"+
                Objects.requireNonNull(Objects.requireNonNull(call.getUastParent()).getSourcePsi()).getText()+
                "}\n}";
        String logCallSource = call.asSourceString();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).with(fix).build());
        return fixGrouper.build();
    }

    static final Issue ISSUE_IB =
            Issue.create("Immortality Bug",
                    "A screen should not be opened while application is in background",
                    "A screen should not be opened while application is in background",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(IBDetector.class, Scope.JAVA_FILE_SCOPE));

    private class MyUElementHandler extends UElementHandler {
        @Override
        public void visitIfExpression(UIfExpression expression){
            expression.getJavaPsi().getText().equals("Service");
        }

    }
}
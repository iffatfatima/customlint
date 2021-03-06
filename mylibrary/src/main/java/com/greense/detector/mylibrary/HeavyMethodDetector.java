package com.greense.detector.mylibrary;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HeavyMethodDetector extends Detector implements Detector.UastScanner {

    private static final String SERVICE_CLASS = "android.app.Service";
    private static final String [] methodList = {"onReceive", "onPreExecute", "doInBackground", "onPostExecute"};
    private static final int COMPLEXITY_THRESHOLD = 15; //Based on https://www.ndepend.com/docs/code-metrics#CC
    private JavaContext context;
    /*
    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList(methodList);
    }*/

    @Override
    public final void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression call, @NotNull PsiMethod method) {
        for (UClass myclass: Objects.requireNonNull(context.getUastFile()).getClasses()){
            int complexity = 0;
            if(Objects.equals(Objects.requireNonNull(myclass.getSuperClass()).getQualifiedName(), SERVICE_CLASS)){
                complexity += CyclomaticComplexityUtil.calculateComplexity(method);
            }
            if (complexity > COMPLEXITY_THRESHOLD){
                context.report(ISSUE_HSS, call,
                        context.getLocation(call),
                        "Heavy Start Service",
                        null
                );
            }
        }
        if (method.getName().equalsIgnoreCase("onPreExecute") ||
                method.getName().equalsIgnoreCase("onPostExecute") ||
                method.getName().equalsIgnoreCase("doInBackground")){
            int complexity = CyclomaticComplexityUtil.calculateComplexity(method);
            if (complexity > COMPLEXITY_THRESHOLD){
                context.report(ISSUE_HAT, call,
                        context.getLocation(call),
                        "Heavy Async Task",
                        null
                );
            }
        }
        if (method.getName().equalsIgnoreCase("onReceive")) {
            int complexity = CyclomaticComplexityUtil.calculateComplexity(method);
            if (complexity > COMPLEXITY_THRESHOLD) {
                context.report(ISSUE_HBR, call,
                        context.getLocation(call),
                        "Heavy Broadcast Receiver",
                        null
                );
            }
        }
    }


    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.singletonList(UClass.class);
    }

    @Override
    public UElementHandler createUastHandler(JavaContext context){
        this.context = context;
        return new MyUElementHandler();
    }

    private class MyUElementHandler extends UElementHandler {

        @Override
        public void visitClass(@NotNull UClass expression){
            for (UMethod method: expression.getMethods()){
                int complexity = 0;
                if(Objects.equals(Objects.requireNonNull(expression.getSuperClass()).getQualifiedName(), SERVICE_CLASS)){
                    complexity += CyclomaticComplexityUtil.calculateComplexity(method);
                    if (complexity > COMPLEXITY_THRESHOLD){
                        context.report(ISSUE_HSS, method,
                                context.getLocation(method),
                                ISSUE_HSS.getExplanation(TextFormat.TEXT),
                                null
                        );
                        complexity = 0;
                        break;
                    }
                }

                else if (method.getName().equalsIgnoreCase("onPreExecute") ||
                        method.getName().equalsIgnoreCase("onPostExecute") ||
                        method.getName().equalsIgnoreCase("doInBackground")){
                    complexity = CyclomaticComplexityUtil.calculateComplexity(method);
                    if (complexity > COMPLEXITY_THRESHOLD){
                        context.report(ISSUE_HAT, method,
                                context.getLocation(method),
                                ISSUE_HAT.getExplanation(TextFormat.TEXT),
                                null
                        );
                    }
                }
                else if (method.getName().equalsIgnoreCase("onReceive")) {
                    complexity = CyclomaticComplexityUtil.calculateComplexity(method);
                    if (complexity > COMPLEXITY_THRESHOLD) {
                        context.report(ISSUE_HBR, method,
                                context.getLocation(method),
                                ISSUE_HBR.getExplanation(TextFormat.TEXT),
                                null
                        );
                    }
                }
            }
        }
    }


    static final Issue ISSUE_HAT =
            Issue.create("Heavy Async Task",
                    "Heavy Async Task",
                    "Reduce the complexity of the main thread by offloading heavy operation.",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(HeavyMethodDetector.class, Scope.JAVA_FILE_SCOPE));

    static final Issue ISSUE_HBR =
            Issue.create("Heavy Start Service",
                    "Heavy Start Service",
                    "Reduce the complexity of the main thread by offloading heavy operation.",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(HeavyMethodDetector.class, Scope.JAVA_FILE_SCOPE));

    static final Issue ISSUE_HSS =
            Issue.create("Heavy Start Service",
                    "Heavy Start Service",
                    "Reduce the complexity of the main thread by offloading heavy operation.",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(HeavyMethodDetector.class, Scope.JAVA_FILE_SCOPE));

}
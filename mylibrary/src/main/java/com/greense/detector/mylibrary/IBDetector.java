package com.greense.detector.mylibrary;

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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class IBDetector extends Detector implements Detector.UastScanner {

    private static final String SERVICE_CLASS = "android.app.Service";
    private final String START_ACTIVITY = "startActivity";
    private final String START_ACTIVITIES = "startActivities";

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList(START_ACTIVITY, START_ACTIVITIES);
    }

    @Override
    public final void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression call, @NotNull PsiMethod method) {
        for (UClass myclass: Objects.requireNonNull(context.getUastFile()).getClasses()){
            if(Objects.equals(Objects.requireNonNull(myclass.getSuperClass()).getQualifiedName(), SERVICE_CLASS)){
                if (method.getName().equals(START_ACTIVITY) || method.getName().equals(START_ACTIVITIES)) {
                    context.report(ISSUE_IB, call,
                            context.getLocation(call),
                            "Immortality Bug",
                            getFix(call)
                    );
                }
            }
        }
    }

    private LintFix getFix(UCallExpression call) {
        String fix = "//TODO: Activity must nnot be called from a background service. Add forerground check or remove it from Service";
        String logCallSource = call.asSourceString();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).beginning().with(fix).build());
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

}
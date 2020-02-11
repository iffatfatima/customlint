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

import java.util.Arrays;
import java.util.List;

public class IWRDetector extends Detector implements Detector.UastScanner {

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList("invalidate");
    }

    @Override
    public final void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression call, @NotNull PsiMethod method) {
        if(method.getName().equals("invalidate") && !method.hasParameters()) {
            context.report(ISSUE_IWR, call,
                    context.getLocation(call),
                    "Must invalidate only a part of view as Rect",
                    getFix(call, method.getName()));
        }
    }

    private LintFix getFix(UCallExpression logCall, String methodName) {
        String fix = methodName + "(); //todo: invalidate(new Rect(w,x,y,z))";
        String logCallSource = logCall.asSourceString();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).with(fix).build());
        return fixGrouper.build();
    }


    static final Issue ISSUE_IWR =
            Issue.create("Invalidate Without Rect",
                    "Must invalidate only a part of view as Rect",
                    "Invalidate a part of view instead of whole View using Rect",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(IWRDetector.class, Scope.JAVA_FILE_SCOPE));
}
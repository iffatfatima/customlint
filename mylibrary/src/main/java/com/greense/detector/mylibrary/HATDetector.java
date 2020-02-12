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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HATDetector extends Detector implements Detector.UastScanner {

    @Override
    public List<String> getApplicableMethodNames() {
        return Collections.singletonList("execute");
    }

    @Override
    public final void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression call, @NotNull PsiMethod method) {
        if(method.getName().equals("execute")) {
            context.report(ISSUE_HAT, call,
                    context.getLocation(call),
                    "Must not call async operations on main thread",
                    getFix(call)
            );
        }
    }

    private LintFix getFix(UCallExpression call) {
        String caller = Objects.requireNonNull(Objects.requireNonNull(call.getUastParent()).getSourcePsi()).getFirstChild().getFirstChild().getText();
        String fix = caller + ".executeAsync()";
        String logCallSource = call.asSourceString();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).with(fix).build());
        return fixGrouper.build();
    }

    static final Issue ISSUE_HAT=
            Issue.create("Heavy async Task",
                    "Must not call async operations on main thread",
                    "Async operations if carried out on main thread, may lead to blocking the UI for heavy operations",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(HATDetector.class, Scope.JAVA_FILE_SCOPE));
}
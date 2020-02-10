package com.greense.detector.mylibrary;

import com.android.tools.lint.client.api.JavaEvaluator;
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
import org.jetbrains.uast.UExpression;

import java.util.Arrays;
import java.util.List;

public class SmartLoggerDetector extends Detector implements Detector.UastScanner {

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList("v", "d", "i", "w", "e", "wtf");
    }

    @Override
    public final void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression call, @NotNull PsiMethod method) {
        JavaEvaluator evaluator = context.getEvaluator();

        if (evaluator.isMemberInClass(method, "android.util.Log")) {
            LintFix fix = quickFixIssueLog(call);
            context.report(ISSUE_LOG, call, context.getLocation(call), "Using 'Log' instead of 'SmartLogger'", fix);
        }
//        if (evaluator.isMemberInClass(method, "android.location.LocationManager")) {
//            LintFix fix = quickFixIssueLog(call);
//            context.report(ISSUE_LOG, call, context.getLocation(call), "Using 'Location' comsumes energy", null);
//        }
    }

    private LintFix quickFixIssueLog(UCallExpression logCall) {
        List<UExpression> arguments = logCall.getValueArguments();
        String methodName = logCall.getMethodName();
        UExpression tag = arguments.get(0);
        String fixSource1 = "SmartLogger." + methodName +  "(" + tag.asSourceString();

        int numArguments = arguments.size();
        if (numArguments == 2) {
            UExpression msgOrThrowable = arguments.get(1);
            fixSource1 +=  ", " + msgOrThrowable.asSourceString() + ")";
        } else if (numArguments == 3) {
            UExpression msg = arguments.get(1);
            UExpression throwable = arguments.get(2);
            fixSource1 += ", " + throwable.asSourceString() + ", " + msg.asSourceString() + ")";
        } else {
            throw new IllegalStateException("android.util.Log overloads should have 2 or 3 arguments");
        }
        String logCallSource = logCall.asSourceString();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).with(fixSource1).build());
        return fixGrouper.build();
    }

    static Issue[] getIssues() {
        return new Issue[] {
                ISSUE_LOG
        };
    }

    public static final Issue ISSUE_LOG =
            Issue.create("LogNotSmartLogger", "Logging call to Log instead of SmartLogger",
                    "Since SmartLogger is included in the project, it is likely that calls to Log should instead"
                            + " be going to SmartLogger.", Category.CORRECTNESS, 6, Severity.WARNING,
                    new Implementation(SmartLoggerDetector.class, Scope.JAVA_FILE_SCOPE));

}
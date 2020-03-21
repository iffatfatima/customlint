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

public class PDDetector extends Detector implements Detector.UastScanner {

    private final String [] METHOD_LIST = {"getExternalFilesDir",
            "getExternalCacheDir",
            "getExternalCacheDirs",
            "getExternalStorageDirectory",
            "getExternalStoragePublicDirectory"};

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList(METHOD_LIST);
    }

    @Override
    public final void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression call, @NotNull PsiMethod method) {
        for(String methodName : METHOD_LIST) {
            if (method.getName().equals(methodName)) {


                context.report(ISSUE_PD, call,
                        context.getLocation(call),
                        "Use internal storage instead of extenral Storage",
                        getFix(call, method.getName())
                );
            }
        }
    }

    private LintFix getFix(UCallExpression call, String methodName) {
        String fix = "";
        if(methodName.toLowerCase().contains("cache")){
            fix = "getCacheDir";
        }
        else{
            fix = "getFilesDir";
        }
        String source = call.asSourceString();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true).with(fix).build());
        return fixGrouper.build();
    }

    static final Issue ISSUE_PD =
            Issue.create("Public Directory",
                    "Use internal storage instead of extenral Storage",
                    "",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(PDDetector.class, Scope.JAVA_FILE_SCOPE));

}
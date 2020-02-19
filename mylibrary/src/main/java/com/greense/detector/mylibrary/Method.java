package com.greense.detector.mylibrary;

import com.android.tools.lint.detector.api.JavaContext;
import com.intellij.psi.PsiMethod;

import org.jetbrains.uast.UCallExpression;

public class Method {
    final JavaContext context;
    final UCallExpression call;
    final PsiMethod method;

    public Method(JavaContext context, UCallExpression call, PsiMethod method) {
        this.context = context;
        this.call = call;
        this.method = method;
    }
}

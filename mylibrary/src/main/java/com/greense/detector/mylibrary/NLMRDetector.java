package com.greense.detector.mylibrary;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiElement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UClass;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NLMRDetector extends Detector implements Detector.UastScanner {

    @Override
    public List<String> applicableSuperClasses() {
        return Arrays.asList("androidx.appcompat.app.AppCompatActivity", "android.support.v7.app.AppCompatActivity",
                "android.accounts.AccountAuthenticatorActivity",
                "android.app.AliasActivity", "android.app.ExpandableListActivity", "android.app.ActivityGroup",
                "android.app.ListActivity", "android.app.NativeActivity", "android.app.LauncherActivity",
                "android.app.PreferenceActivity", "android.app.TabActivity", "android.app.Activity",
                "android.app.FragmentActivity", "androidx.fragment.app.FragmentActivity");
    }

    @Override
    public final void visitClass(@NotNull JavaContext context, @NotNull UClass classNode) {
        for(String className: Objects.requireNonNull(applicableSuperClasses())){
            String qualifiedClassName = Objects.requireNonNull(classNode.getJavaPsi().getSuperClass()).getQualifiedName();
            if(Objects.requireNonNull(qualifiedClassName).equals(className)){
                if(classNode.findMethodsByName("onTrimMemory").length == 0){
                    context.report(ISSUE_NLMR, classNode.getRBrace(),
                            context.getLocation(Objects.requireNonNull(classNode.getRBrace())),
                            "No Low Memory Resolver",
                            getFix(classNode.getRBrace())
                    );
                }
            }
        }
    }

    private LintFix getFix(PsiElement element) {
        String fix = "@Override\n" +
                "    public void onTrimMemory(int level){\n" +
                "        //todo: Free memory here\n" +
                "    }";
        String logCallSource = element.getText();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).beginning().with(fix).build());
        return fixGrouper.build();
    }

    static final Issue ISSUE_NLMR =
            Issue.create("No Low Memory Resolver",
                    "Once the device memory is full, release some memory from your application",
                    "Once the device memory is full, release some memory from your application",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(NLMRDetector.class, Scope.JAVA_FILE_SCOPE));

}
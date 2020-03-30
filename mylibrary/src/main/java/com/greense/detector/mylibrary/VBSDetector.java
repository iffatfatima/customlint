package com.greense.detector.mylibrary;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.java.JavaConstructorUCallExpression;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class VBSDetector extends Detector implements Detector.UastScanner {

    private final String START_SERVICE = "startService";
    private final String STOP_SERVICE = "stopService";
    private final String ON_STOP = "onStop";
    private HashMap<String, ServiceStatus> serviceStatus  = new HashMap<>();

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList(START_SERVICE, STOP_SERVICE, ON_STOP);
    }

    @Override
    public final void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression call, @NotNull PsiMethod method) {
        if(method.getName().equals(START_SERVICE)) {
            if (call.getValueArguments().get(0) instanceof JavaConstructorUCallExpression) {
                String intentExp = call.getValueArguments().get(0).getJavaPsi().toString();
                intentExp = intentExp.substring(intentExp.lastIndexOf(":") +1);
                serviceStatus.put(intentExp, new ServiceStatus( call));
                String onStopBody = "";
                PsiMethod onStopMethod = null;
                boolean onStopPresent = false;
                PsiClass psiCLass = null;
                for(PsiElement child: Objects.requireNonNull(context.getPsiFile()).getChildren()){
                    if (child instanceof PsiClass){
                        psiCLass = (PsiClass)child;
                        for (PsiMethod psiMethod : ((PsiClass)child).getMethods()) {
                            if(psiMethod.getName().equalsIgnoreCase("onStop")){
                                onStopPresent = true;
                                onStopBody = Objects.requireNonNull(psiMethod.getBody()).getText();
                                onStopBody = onStopBody.replace(" ", "");
                                onStopBody = onStopBody.replace("\n", "");
                                onStopMethod = psiMethod;
                                break;
                            }
                        }
                        break;
                    }
                }
                if(onStopPresent) {
                    String checkExp = "stopService("+ intentExp +")";
                    checkExp = checkExp.replace(" ", "");
                    if(!onStopBody.contains(checkExp)) {
                        context.report(ISSUE_VBS, psiCLass.getRBrace(),
                                context.getLocation(Objects.requireNonNull(Objects.requireNonNull(onStopMethod.getBody()).getLBrace())),
                                "Vacuous Background Service",
                                applyFix(onStopMethod, intentExp)
                        );
                    }
                }
                else{
                    context.report(ISSUE_VBS, psiCLass.getLBrace(),
                            context.getLocation(psiCLass.getLBrace()),
                            "Vacuous Background Service",
                            applyFix(psiCLass.getLBrace(), intentExp)
                    );
                }
            }
        }
    }

    @Override
    public void afterCheckFile(Context context){
        serviceStatus.clear();
    }

    private LintFix applyFix(PsiMethod method, String fixExp) {
        String fix = "stopService("+ fixExp +");\n";
        String sourceString = method.getBody().getLBrace().toString();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(sourceString).shortenNames().reformat(true).end().with(fix).build());
        return fixGrouper.build();
    }

    private LintFix applyFix(PsiElement element, String fixExp) {
        String source = element.getText();
        LintFix.GroupBuilder fixGrouper = fix().group();
        String fix = "\t@Override \n" +
                "\tpublic void onStop(){\n" +
                "\t\t stopService("+ fixExp +"); \n" +
                "\t\t super.onStop();\n"
                + "\t}";
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true).end().with(fix).build());
        return fixGrouper.build();
    }

    static final Issue ISSUE_VBS =
            Issue.create("Vacuous Background Service",
                    "A screen should not be opened while application is in background",
                    "A screen should not be opened while application is in background",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(VBSDetector.class, Scope.JAVA_FILE_SCOPE));

    class ServiceStatus{
        UCallExpression call;

        ServiceStatus(UCallExpression call) {
            this.call = call;
        }


    }

}
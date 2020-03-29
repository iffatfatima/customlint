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
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.java.JavaConstructorUCallExpression;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VBSDetector extends Detector implements Detector.UastScanner {

    private static final String SERVICE_CLASS = "android.app.Service";
    private final String START_SERVICE = "startService";
    private final String STOP_SERVICE = "stopService";
    private final String ON_STOP = "onStop";
    private HashMap<String, ServiceStatus> serviceStatus  = new HashMap<>();
    private UCallExpression stopCall;
    private JavaContext jContext;

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList(START_SERVICE, STOP_SERVICE, ON_STOP);
    }

    @Override
    public final void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression call, @NotNull PsiMethod method) {

        if(method.getName().equals(START_SERVICE)) {
            if (call.getValueArguments().get(0) instanceof JavaConstructorUCallExpression) {
                String key = call.getValueArguments().get(0).getJavaPsi().toString();
                key = key.substring(key.lastIndexOf(":") +1);
                if(serviceStatus.get(key) == null) {
                    serviceStatus.put(key, new ServiceStatus(true, false, call));
                }
                else{
                    serviceStatus.get(key).started = true;
                }
            }
        }
        else if (method.getName().equals(STOP_SERVICE)){
            if(call.getValueArguments().get(0) instanceof JavaConstructorUCallExpression){
                String key = call.getValueArguments().get(0).getJavaPsi().toString();
                key = key.substring(key.lastIndexOf(":") +1);
                if(serviceStatus.get(key) == null) {
                    serviceStatus.put(key, new ServiceStatus(false, true, call));
                }
                else{
                    serviceStatus.get(key).stopped = true;
                }
            }
        }
        else if (method.getName().equals(ON_STOP)){
            stopCall = call;
            jContext = context;
        }
    }

    @Override
    public void afterCheckFile(Context context){
        for (Map.Entry<String, ServiceStatus> element: serviceStatus.entrySet()){
            if(!element.getValue().stopped){
                if(stopCall != null) {
                    jContext.report(ISSUE_VBS, stopCall,
                            jContext.getLocation(element.getValue().call),
                            "Vacuous Background Service",
                            applyFix(stopCall, element.getKey())
                    );
                }
                else{
                    context.getDriver();
                }

            }
        }
        serviceStatus.clear();
        stopCall = null;

    }

    private LintFix applyFix(UCallExpression call, String fixExp) {
        String fix = "stopService("+ fixExp +");\n";
        String logCallSource = call.asSourceString();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).beginning().with(fix).build());
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
        boolean started = false;
        boolean stopped = false;
        UCallExpression call;

        ServiceStatus(boolean started, boolean stopped, UCallExpression call) {
            this.started = started;
            this.stopped = stopped;
            this.call = call;
        }


    }

}
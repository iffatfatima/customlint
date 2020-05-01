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
import org.jetbrains.uast.UField;
import org.jetbrains.uast.UMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LTRLDetector extends Detector implements Detector.UastScanner {

    @Override
    public List<String> applicableSuperClasses() {
        return Arrays.asList("androidx.appcompat.app.AppCompatActivity", "android.support.v7.app.AppCompatActivity",
                "androidx.fragment.app.Fragment", "android.support.v7.app.Fragment");
    }

    @Override
    public final void visitClass(@NotNull JavaContext context, @NotNull UClass classNode) {
        for (String className : Objects.requireNonNull(applicableSuperClasses())) {
            String qualifiedClassName = Objects.requireNonNull(classNode.getJavaPsi().getSuperClass()).getQualifiedName();
            if (Objects.requireNonNull(qualifiedClassName).equals(className)) {
                boolean onStopPresent = false;
                String onStopBody = "";
                UMethod onStopMethod = null;
                for (UMethod method: classNode.getMethods()) {
                    if(method.getName().equals("onStop")){
                        onStopPresent = true;
                        onStopMethod = method;
                        onStopBody = Objects.requireNonNull(method.getUastBody()).toString();
                    }
                }
                ArrayList<UField> handlerFields = new ArrayList<>();
                ArrayList<UField> camFields = new ArrayList<>();
                ArrayList<UField> mpFields = new ArrayList<>();
                for (UField field: classNode.getFields()){
                    String element = Objects.requireNonNull(field.getTypeReference()).getQualifiedName();
//                    element = element.substring(element.lastIndexOf(":") +1, element.length());
                    if (element.equalsIgnoreCase("android.os.Handler")){
                        handlerFields.add(field);
                    }
                    if (element.equalsIgnoreCase("android.hardware.Camera")){
                        camFields.add(field);
                    }
                    if (element.equalsIgnoreCase("android.media.MediaPlayer")){
                        mpFields.add(field);
                    }
                }
                if (handlerFields.size() > 0) {
                    for (UField field : handlerFields) {
                        if (onStopPresent){
                            if(!onStopBody.contains(field.getName() + ".remove")) {
                                context.report(ISSUE_LT, classNode.getRBrace(),
                                        context.getLocation(Objects.requireNonNull(Objects.requireNonNull(onStopMethod.getJavaPsi().getBody()).getLBrace())),
                                        "Leaking Thread",
                                        getFix(onStopMethod, field.getName())
                                );
                            }
                        }
                        else {
                            context.report(ISSUE_LT, classNode.getRBrace(),
                                    context.getLocation(field),
                                    "Leaking Thread",
                                    getFix(Objects.requireNonNull(classNode.getRBrace()), field.getName())
                            );
                        }
                    }
                }
                //Camera
                if (camFields.size() > 0) {
                    for (UField field : camFields) {
                        if (onStopPresent){
                            if(!onStopBody.contains(field.getName() + ".release")) {
                                context.report(ISSUE_RL, classNode.getRBrace(),
                                        context.getLocation(Objects.requireNonNull(Objects.requireNonNull(onStopMethod.getJavaPsi().getBody()).getLBrace())),
                                        "Camera Leak",
                                        getCamFix(onStopMethod, field.getName())
                                );
                            }
                        }
                        else {
                            context.report(ISSUE_RL, classNode.getRBrace(),
                                    context.getLocation(field),
                                    "Camera Leak",
                                    getCamFix(Objects.requireNonNull(classNode.getRBrace()), field.getName())
                            );
                        }
                    }
                }
                //MediaPlayer
                if (mpFields.size() > 0) {
                    for (UField field : mpFields) {
                        if (onStopPresent){
                            if(!onStopBody.contains(field.getName() + ".release")) {
                                context.report(ISSUE_RL, classNode.getRBrace(),
                                        context.getLocation(Objects.requireNonNull(Objects.requireNonNull(onStopMethod.getJavaPsi().getBody()).getLBrace())),
                                        "Media Player Leak",
                                        getMpFix(onStopMethod, field.getName())
                                );
                            }
                        }
                        else {
                            context.report(ISSUE_RL, classNode.getRBrace(),
                                    context.getLocation(field),
                                    "Media Player Leak",
                                    getMpFix(Objects.requireNonNull(classNode.getRBrace()), field.getName())
                            );
                        }
                    }
                }
            }
        }
    }

    private LintFix getFix(PsiElement element, String fieldName) {

        String source = element.getText();
        LintFix.GroupBuilder fixGrouper = fix().group();
        String fix = "\t@Override \n" +
                "\tpublic void onStop(){\n" + "\t\t if(" + fieldName + "!= null){" + fieldName + ".removeCallbacksAndMessages(null); } " +
                "\t\t super.onStop();\n"
                + "\t}";
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true).end().with(fix).build());
        return fixGrouper.build();
    }

    private LintFix getFix(UMethod element, String fieldName) {
        LintFix.GroupBuilder fixGrouper = fix().group();
        String fix = ("\t\t if(" + fieldName + "!= null){" + fieldName + ".removeCallbacksAndMessages(null); } ");
        fix = fix.replace("\r", "");
        String source = Objects.requireNonNull(element.getJavaPsi().getBody()).getLBrace().getText();
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true).end().with(fix).build());
        return fixGrouper.build();
    }

    //Camera
    private LintFix getCamFix(PsiElement element, String fieldName) {

        String source = element.getText();
        LintFix.GroupBuilder fixGrouper = fix().group();
        String fix = "\t@Override \n" +
                "\tpublic void onStop(){\n" + "\t\t if(" + fieldName + "!= null){" + fieldName + ".release();\n"+ fieldName + "= null;" +" } " +
                "\t\t super.onStop();\n"
                + "\t}";
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true).end().with(fix).build());
        return fixGrouper.build();
    }

    private LintFix getCamFix(UMethod element, String fieldName) {
        LintFix.GroupBuilder fixGrouper = fix().group();
        String fix = ("\t\t if(" + fieldName + "!= null){" + fieldName + ".release();\n"+ fieldName + "= null;" +" } ");
        fix = fix.replace("\r", "");
        String source = Objects.requireNonNull(element.getJavaPsi().getBody()).getLBrace().getText();
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true).end().with(fix).build());
        return fixGrouper.build();
    }

    ///Media Player
    //Camera
    private LintFix getMpFix(PsiElement element, String fieldName) {

        String source = element.getText();
        LintFix.GroupBuilder fixGrouper = fix().group();
        String fix = "\t@Override \n" +
                "\tpublic void onStop(){\n" + "\t\t if(" + fieldName + "!= null){" + fieldName + ".release();\n"+ fieldName + "= null;" +" } " +
                "\t\t super.onStop();\n"
                + "\t}";
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true).end().with(fix).build());
        return fixGrouper.build();
    }

    private LintFix getMpFix(UMethod element, String fieldName) {
        LintFix.GroupBuilder fixGrouper = fix().group();
        String fix = ("\t\t if(" + fieldName + "!= null){" + fieldName + ".release();\n"+ fieldName + "= null;" +" }");
        fix = fix.replace("\r", "");
        String source = Objects.requireNonNull(element.getJavaPsi().getBody()).getLBrace().getText();
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true).end().with(fix).build());
        return fixGrouper.build();
    }

    static final Issue ISSUE_LT =
            Issue.create("Leaking Thread",
                    "Leaking Thread",
                    "All callbacks for handler must be removed when app goes to background",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(LTRLDetector.class, Scope.JAVA_FILE_SCOPE));

    static final Issue ISSUE_RL =
            Issue.create("Resource Leak",
                    "Resource Leak",
                    "All callbacks for handler must be removed when app goes to background",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(LTRLDetector.class, Scope.JAVA_FILE_SCOPE));
}
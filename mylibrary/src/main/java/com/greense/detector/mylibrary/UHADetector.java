package com.greense.detector.mylibrary;


import com.android.annotations.NonNull;
import com.android.tools.lint.checks.ManifestDetector;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.android.tools.lint.detector.api.XmlContext;

import org.w3c.dom.Element;

import static com.android.SdkConstants.ANDROID_URI;
import static com.android.SdkConstants.TAG_APPLICATION;


public class UHADetector extends ManifestDetector implements Detector.XmlScanner {

    @Override
    public void visitElement(@NonNull XmlContext context, @NonNull Element element) {
        if (element.getTagName().equals(TAG_APPLICATION)){
            if(!element.hasAttributeNS(ANDROID_URI,"hardwareAccelerated")){
                context.report(ISSUE_UHA, element, context.getLocation(element), ISSUE_UHA.getExplanation(TextFormat.TEXT), addUHA());
            }
            else if(element.getAttributeNodeNS(ANDROID_URI, "hardwareAccelerated").getValue().equalsIgnoreCase("false")){
                context.report(ISSUE_UHA, element, context.getLocation(element), ISSUE_UHA.getExplanation(TextFormat.TEXT), addUHA());
            }
        }
    }

    private LintFix updateUHA() {
        LintFix.ReplaceStringBuilder fixGrouper = fix()
                .replace()
                .text("android:hardwareAccelerated='false'")
                .shortenNames()
                .reformat(true)
                .with("android:hardwareAccelerated='true'");
        return  fixGrouper.build();
    }

    private LintFix addUHA() {
        LintFix.SetAttributeBuilder fixGrouper = fix()
                .set(ANDROID_URI,"hardwareAccelerated", "true");
        return  fixGrouper.build();
    }

    static final Issue ISSUE_UHA =
            Issue.create("Unused Hardware Acceleration", "Add hardwareAccelerated=true",
                    "Add hardwareAccelerated=true", Category.PERFORMANCE, 6, Severity.WARNING,
                    new Implementation(UHADetector.class, Scope.MANIFEST_SCOPE));

}
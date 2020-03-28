package com.greense.detector.mylibrary;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Iffat on 12/2/2019.
 */
public class CustomLintRegistry extends IssueRegistry {

    @NotNull
    @Override
    public List<Issue> getIssues() { return Arrays.asList(LTDetector.ISSUE_LT, ERBDetector.ISSUE_ERB,
            DTWCDetector.ISSUE_DTWC, PDDetector.ISSUE_PD,
            LCDetector.ISSUE_LC, NLMRDetector.ISSUE_NLMR,
            VBSDetector.ISSUE_VBS,/* IDSDetector.ISSUE_IDS,*/
            BFUDetector.ISSUE_BFU, IBDetector.ISSUE_IB, IWRDetector.ISSUE_IWR, UHADetector.ISSUE_UHA,SmartLoggerDetector.ISSUE_LOG, LeakingListenerIssue.ISSUE, TextAppearanceIssue.ISSUE);
    }
}
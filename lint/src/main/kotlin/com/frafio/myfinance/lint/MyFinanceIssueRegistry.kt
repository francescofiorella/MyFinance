package com.frafio.myfinance.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

class MyFinanceIssueRegistry : IssueRegistry() {

    override val issues = listOf(
        HardcodedContentDescriptionDetector.ISSUE,
        ClockInCompositionDetector.ISSUE,
        FirebaseOutsideAdapterDetector.ISSUE,
        TestMethodPrefixDetector.ISSUE,
    )

    override val api: Int = CURRENT_API

    override val minApi: Int = 12

    override val vendor: Vendor = Vendor(
        vendorName = "MyFinance",
        identifier = "com.frafio.myfinance:lint",
    )
}

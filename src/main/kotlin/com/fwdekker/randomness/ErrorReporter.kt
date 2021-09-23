package com.fwdekker.randomness

import com.intellij.ide.BrowserUtil
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.util.Consumer
import java.awt.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


/**
 * A report submitter that opens a pre-filled issue creation form on Randomness' GitHub repository.
 *
 * This class pertains to reports of exceptions that are not caught by the plugin and end up being shown to the user
 * as a notification by the IDE.
 */
class ErrorReporter : ErrorReportSubmitter() {
    /**
     * Returns the text that is displayed in the button to report the error.
     *
     * @return the text that is displayed in the button to report the error
     */
    override fun getReportActionText() = Bundle("reporter.report")

    /**
     * Submits the exception by opening the browser to create an issue on GitHub.
     *
     * @param events ignored
     * @param additionalInfo additional information provided by the user
     * @param parentComponent ignored
     * @param consumer ignored
     * @return `true`
     */
    override fun submit(
        events: Array<IdeaLoggingEvent>,
        additionalInfo: String?,
        parentComponent: Component,
        consumer: Consumer<in SubmittedReportInfo>
    ): Boolean {
        val project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(parentComponent))
        object : Backgroundable(project, Bundle("reporter.opening")) {
            override fun run(indicator: ProgressIndicator) {
                BrowserUtil.open(getIssueUrl(additionalInfo))
                consumer.consume(
                    SubmittedReportInfo(
                        "https://github.com/FWDekker/intellij-randomness/issues",
                        Bundle("reporter.issue"),
                        SubmittedReportInfo.SubmissionStatus.NEW_ISSUE
                    )
                )
            }
        }.queue()
        return true
    }

    /**
     * Returns the privacy notice text.
     *
     * @return the privacy notice text
     */
    override fun getPrivacyNoticeText() = Bundle("reporter.privacy_notice")


    /**
     * Constructs a URL to create an issue with [additionalInfo] that is below the maximum URL limit.
     *
     * @param additionalInfo additional information about the exception provided by the user
     * @return a URL to create an issue with [additionalInfo] that is below the maximum URL limit
     */
    fun getIssueUrl(additionalInfo: String?): String {
        val baseUrl = "https://github.com/FWDekker/intellij-randomness/issues/new?body="

        val additionalInfoSection = createMarkdownSection(
            "Additional info",
            if (additionalInfo.isNullOrBlank()) MORE_DETAIL_MESSAGE else additionalInfo
        )
        val stacktraceSection = createMarkdownSection(
            "Stacktraces",
            STACKTRACE_MESSAGE
        )
        val versionSection = createMarkdownSection(
            "Version information",
            getFormattedVersionInformation()
        )

        return URLEncoder.encode(additionalInfoSection + stacktraceSection + versionSection, StandardCharsets.UTF_8)
            .replace("%2B", "+")
            .let { baseUrl + it }
    }

    /**
     * Creates a Markdown "section" containing the title in bold followed by the contents on the next line, finalized by
     * two newlines.
     *
     * @param title the title of the section
     * @param contents the contents of the section
     * @return a Markdown "section" with the [title] and [contents]
     */
    private fun createMarkdownSection(title: String, contents: String) = "**${title.trim()}**\n${contents.trim()}\n\n"

    /**
     * Returns version information on the user's environment as a Markdown-style list.
     *
     * @return version information on the user's environment as a Markdown-style list
     */
    private fun getFormattedVersionInformation() =
        """
        - Randomness version: ${pluginDescriptor?.version ?: "_Unknown_"}
        - IDE version: ${ApplicationInfo.getInstance().apiVersion}
        - Operating system: ${System.getProperty("os.name")}
        - Java version: ${System.getProperty("java.version")}
        """.trimIndent()


    /**
     * Holds constants.
     */
    companion object {
        /**
         * Message asking the user to provide more information about the exception.
         */
        const val MORE_DETAIL_MESSAGE =
            "Please describe your issue in more detail here. What were you doing when the exception occurred?"

        /**
         * Message asking the user to provide stacktrace information.
         */
        const val STACKTRACE_MESSAGE =
            "Please paste the full stacktrace from the IDE's error popup below.\n```java\n\n```"
    }
}

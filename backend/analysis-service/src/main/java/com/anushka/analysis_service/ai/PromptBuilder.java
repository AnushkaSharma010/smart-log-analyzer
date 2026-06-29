package com.anushka.analysis_service.ai;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.anushka.analysis_service.strategy.ParsedLogResult;

@Component
public class PromptBuilder {
    // Batch size chosen for prompt quality (focused attention per call),
    // NOT as a cap on total exceptions covered — every exception gets analyzed,
    // just across however many batches it takes.
    private static final int BATCH_SIZE = 8;

    public List<String> buildRootCausePrompts(ParsedLogResult parsedResult) {
        List<ParsedLogResult.ExceptionInfo> sortedExceptions = parsedResult.getExceptions().stream()
                .sorted(Comparator.comparingInt(ParsedLogResult.ExceptionInfo::getCount).reversed())
                .toList();

        List<String> prompts = new java.util.ArrayList<>();

        for (int i = 0; i < sortedExceptions.size(); i += BATCH_SIZE) {
            List<ParsedLogResult.ExceptionInfo> batch = sortedExceptions.subList(
                    i, Math.min(i + BATCH_SIZE, sortedExceptions.size())
            );
            prompts.add(buildPromptForBatch(parsedResult, batch, i + 1, sortedExceptions.size()));
        }

        return prompts;
    }

    private String buildPromptForBatch(ParsedLogResult parsedResult,
                                        List<ParsedLogResult.ExceptionInfo> batch,
                                        int startIndex,
                                        int totalExceptionCount) {

        String issuesBlock = batch.stream()
                .map(e -> """
                        Exception: %s
                        Message: %s
                        Occurred: %d times (first seen at %s)
                        Stack trace:
                        %s
                        """.formatted(
                                e.getType(),
                                e.getMessage() != null ? e.getMessage() : "(no message captured)",
                                e.getCount(),
                                e.getFirstOccurrence(),
                                e.getSampleStackTrace() != null ? e.getSampleStackTrace() : "(no stack trace captured)"
                        ))
                .collect(Collectors.joining("\n---\n"));

        return """
You are a Senior Production Support Engineer responsible for analyzing enterprise application logs.

You are provided with parsed exceptions extracted from a production log file.

Overall Log Statistics:
- Total Errors: %d
- Total Warnings: %d
- Total Infos: %d
- Total Distinct Exception Types: %d

Analyze ONLY the exceptions listed below.

%s

Instructions:

For EACH exception independently, produce the following sections exactly in this order.

Exception:
<Exception Class Name>

Occurrences:
<Occurrence Count>

Confidence:
High / Medium / Low

Rules:
- High = stack trace clearly indicates the failure location.
- Medium = root cause is likely but multiple possibilities exist.
- Low = insufficient evidence from the available stack trace.

Evidence:
Quote the most relevant exception message, method name, class name or stack trace line that supports your conclusion.

Likely Root Cause:
Explain the most probable technical reason behind the exception.
Do NOT invent missing information.
If several causes are possible, mention the possibilities and explain why certainty is limited.

Business Impact:
Describe what functionality may fail because of this exception.
Keep this practical (payments, login, shipping, notifications, reporting, etc.).

Recommended Investigation:
Provide 3-5 concrete investigation steps.
Examples:
• Check database connectivity
• Verify connection pool usage
• Inspect application configuration
• Review upstream service availability
• Check thread dumps
• Verify memory usage
• Inspect deployment configuration

Recommended Fix:
Provide practical engineering fixes.
If multiple root causes are possible, separate the fixes accordingly.

Formatting Rules:
- Analyze EVERY exception independently.
- Never merge two exceptions.
- Never skip an exception.
- Never assume information not visible in the provided data.
- Keep each exception analysis under 120 words.
- Use concise technical language suitable for production incident reports.
""".formatted(
        parsedResult.getTotalErrors(),
        parsedResult.getTotalWarnings(),
        parsedResult.getTotalInfos(),
        totalExceptionCount,
        issuesBlock
    );
    }
}

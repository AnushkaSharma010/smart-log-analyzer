package com.anushka.analysis_service.strategy;

import java.util.List;

public interface LogParsingStrategy {
    ParsedLogResult parse(List<String> logLines);

    String getStrategyName();
}

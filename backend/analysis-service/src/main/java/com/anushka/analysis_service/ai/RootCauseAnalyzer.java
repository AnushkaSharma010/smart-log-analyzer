package com.anushka.analysis_service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import com.anushka.analysis_service.strategy.ParsedLogResult;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RootCauseAnalyzer {

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;

    public RootCauseAnalyzer(ChatClient.Builder chatClientBuilder, PromptBuilder promptBuilder){
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
    }
    
    @CircuitBreaker(name = "mistralAi", fallbackMethod = "fallbackInsight")
    public String generateInsight(ParsedLogResult parsedResult){
        if(parsedResult.getExceptions() == null || parsedResult.getExceptions().isEmpty()){
            log.info("No exceptions found — skipping AI insight generation");
            return "No exceptions detected in this log file. No root cause analysis needed.";
        }
        
            String prompt = promptBuilder.buildRootCausePrompt(parsedResult);
            String response = chatClient.prompt().user(prompt).call().content();
            log.info("AI insight generated successfully ({} chars)", response.length());
            return response;
        
    }
    private String fallbackInsight(ParsedLogResult parsedResult, Throwable throwable) {
        log.error("AI insight generation failed — circuit breaker fallback triggered. Reason: {}",
                throwable.getMessage());
        return "AI insight unavailable at this time (service experiencing issues). " +
                "Error analysis based on parsed data is still complete above.";
    }

}

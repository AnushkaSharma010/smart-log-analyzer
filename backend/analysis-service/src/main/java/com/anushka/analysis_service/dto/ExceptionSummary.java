package com.anushka.analysis_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExceptionSummary {

    private String type;
    private int count;
    private String firstOccurrence;
    
}

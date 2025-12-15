package com.devblocker.solution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolutionStatsResponse {
    private long totalSolutions;
    private long acceptedSolutions;
}


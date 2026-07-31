package com.accounting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusPointsResultDto {
    private Integer previousPoints;
    private Integer pointsAwarded;
    private Integer newPointsBalance;
    private Boolean rewardTriggered;
    private BonusPointsHistoryDto rewardDetails;
}

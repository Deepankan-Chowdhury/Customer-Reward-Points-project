package com.retailer.rewardspoints.constants;

import lombok.Getter;

@Getter
public enum RewardPointsSlabs {
	FIRST_SLAB(50, 100, 1),
	SECOND_SLAB(100, Integer.MAX_VALUE, 2);
	
	public final int minimumThreshold;
	public final int maximumThreshold;
	public final int ponintsGainedPerDollar;

	RewardPointsSlabs(int minimumThreshold, int maximumThreshold, int ponintsGainedPerDollar) {
		this.minimumThreshold = minimumThreshold;
		this.maximumThreshold = maximumThreshold;
		this.ponintsGainedPerDollar = ponintsGainedPerDollar;				
	}

}

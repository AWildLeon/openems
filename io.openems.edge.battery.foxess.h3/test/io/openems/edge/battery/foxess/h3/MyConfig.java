package io.openems.edge.battery.foxess.h3;

import io.openems.common.test.AbstractComponentConfig;

@SuppressWarnings("all")
public class MyConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private String id;
		private String alias;
		private boolean enabled;
		private String batteryInverterId;
		private int capacityWh;
		private int minCellVoltage;
		private int maxCellVoltage;
		private int chargeMaxCurrent;
		private int dischargeMaxCurrent;

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setAlias(String alias) {
			this.alias = alias;
			return this;
		}

		public Builder setEnabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		public Builder setBatteryInverterId(String batteryInverterId) {
			this.batteryInverterId = batteryInverterId;
			return this;
		}

		public Builder setCapacityWh(int capacityWh) {
			this.capacityWh = capacityWh;
			return this;
		}

		public Builder setMinCellVoltage(int minCellVoltage) {
			this.minCellVoltage = minCellVoltage;
			return this;
		}

		public Builder setMaxCellVoltage(int maxCellVoltage) {
			this.maxCellVoltage = maxCellVoltage;
			return this;
		}

		public Builder setChargeMaxCurrent(int chargeMaxCurrent) {
			this.chargeMaxCurrent = chargeMaxCurrent;
			return this;
		}

		public Builder setDischargeMaxCurrent(int dischargeMaxCurrent) {
			this.dischargeMaxCurrent = dischargeMaxCurrent;
			return this;
		}

		public MyConfig build() {
			return new MyConfig(this);
		}
	}

	/**
	 * Create a Config builder.
	 *
	 * @return a {@link Builder}
	 */
	public static Builder create() {
		return new Builder();
	}

	private final Builder builder;

	private MyConfig(Builder builder) {
		super(Config.class, builder.id);
		this.builder = builder;
	}

	@Override
	public String alias() {
		return this.builder.alias;
	}

	@Override
	public boolean enabled() {
		return this.builder.enabled;
	}

	@Override
	public String batteryInverterId() {
		return this.builder.batteryInverterId;
	}

	@Override
	public int capacityWh() {
		return this.builder.capacityWh;
	}

	@Override
	public int minCellVoltage() {
		return this.builder.minCellVoltage;
	}

	@Override
	public int maxCellVoltage() {
		return this.builder.maxCellVoltage;
	}

	@Override
	public int chargeMaxCurrent() {
		return this.builder.chargeMaxCurrent;
	}

	@Override
	public int dischargeMaxCurrent() {
		return this.builder.dischargeMaxCurrent;
	}
}

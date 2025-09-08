package io.openems.edge.meter.foxess.h3;

import io.openems.common.types.MeterType;

@SuppressWarnings("all")
public class MyConfig implements Config {

	protected static class Builder {
		private String id;
		private String alias;
		private boolean enabled;
		private MeterType type;
		private String modbus_id;
		private int modbusUnitId;
		private boolean invert;

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

		public Builder setType(MeterType type) {
			this.type = type;
			return this;
		}

		public Builder setModbusId(String modbus_id) {
			this.modbus_id = modbus_id;
			return this;
		}

		public Builder setModbusUnitId(int modbusUnitId) {
			this.modbusUnitId = modbusUnitId;
			return this;
		}

		public Builder setInvert(boolean invert) {
			this.invert = invert;
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
		this.builder = builder;
	}

	@Override
	public String id() {
		return this.builder.id;
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
	public MeterType type() {
		return this.builder.type;
	}

	@Override
	public String modbus_id() {
		return this.builder.modbus_id;
	}

	@Override
	public int modbusUnitId() {
		return this.builder.modbusUnitId;
	}

	@Override
	public boolean invert() {
		return this.builder.invert;
	}

	@Override
	public String Modbus_target() {
		return Config.super.Modbus_target();
	}

	@Override
	public String webconsole_configurationFactory_nameHint() {
		return Config.super.webconsole_configurationFactory_nameHint();
	}
}

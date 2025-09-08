package io.openems.edge.meter.foxess.h3;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.PersistencePriority;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.meter.api.ElectricityMeter;

public interface MeterFoxessH3 extends ElectricityMeter {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

		// Grid measurements for each phase - from FoxESS H3 registers 31006-31020
		GRID_VOLTAGE_R(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.VOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //
		GRID_VOLTAGE_S(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.VOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //
		GRID_VOLTAGE_T(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.VOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //
		GRID_CURRENT_R(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //
		GRID_CURRENT_S(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //
		GRID_CURRENT_T(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //
		GRID_POWER_R(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //
		GRID_POWER_S(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //
		GRID_POWER_T(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //
		GRID_FREQUENCY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.HERTZ) //
				.persistencePriority(PersistencePriority.HIGH)), //

		// Smart meter energy counters - from FoxESS H3 registers 32013-32017
		SMARTMETER_IMPORT_TOTAL(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		SMARTMETER_IMPORT_DAILY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		SMARTMETER_EXPORT_TOTAL(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		SMARTMETER_EXPORT_DAILY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //

		// Load energy counters - from FoxESS H3 registers 32022-32023
		LOAD_ENERGY_TOTAL(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		LOAD_ENERGY_DAILY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		;

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}
}

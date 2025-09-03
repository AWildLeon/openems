package io.openems.edge.batteryinverter.foxess.h3;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.PersistencePriority;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.batteryinverter.api.SymmetricBatteryInverter;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.IntegerDoc;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.modbusslave.ModbusSlave;

public interface BatteryInverterFoxessH3 extends SymmetricBatteryInverter, OpenemsComponent, ModbusSlave {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

		// PV String 1
		PV1_VOLTAGE(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.VOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //
		PV1_CURRENT(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //
		PV1_POWER(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //

		// PV String 2
		PV2_VOLTAGE(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.VOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //
		PV2_CURRENT(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //
		PV2_POWER(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //

		// Grid measurements for each phase
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

		// Temperature measurements
		INVERTER_TEMPERATURE(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.DEGREE_CELSIUS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		INNER_TEMPERATURE(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.DEGREE_CELSIUS) //
				.persistencePriority(PersistencePriority.HIGH)), //

		// Battery measurements
		BATTERY_VOLTAGE(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.VOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //
		BATTERY_CURRENT(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //
		BATTERY_POWER(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //
		BATTERY_TEMPERATURE(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.DEGREE_CELSIUS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		BATTERY_SOC(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.PERCENT) //
				.persistencePriority(PersistencePriority.HIGH)), //

		// Status information
		INVERTER_STATE(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.persistencePriority(PersistencePriority.HIGH)), //

		// Energy counters
		PV_ENERGY_TOTAL(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		PV_ENERGY_DAILY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		BATTERY_CHARGE_TOTAL(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		BATTERY_CHARGE_DAILY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		BATTERY_DISCHARGE_TOTAL(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		BATTERY_DISCHARGE_DAILY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //
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
		LOAD_ENERGY_TOTAL(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		LOAD_ENERGY_DAILY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY) //
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)); //

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

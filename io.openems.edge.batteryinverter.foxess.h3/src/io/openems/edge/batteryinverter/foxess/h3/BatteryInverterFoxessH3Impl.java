package io.openems.edge.batteryinverter.foxess.h3;

import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_2;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.channel.AccessMode;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.batteryinverter.api.SymmetricBatteryInverter;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC4ReadInputRegistersTask;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;
import io.openems.edge.common.sum.GridMode;
import io.openems.edge.common.taskmanager.Priority;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Battery-Inverter.FoxESS.H3", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE //
})
public class BatteryInverterFoxessH3Impl extends AbstractOpenemsModbusComponent
		implements BatteryInverterFoxessH3, SymmetricBatteryInverter, ModbusComponent, OpenemsComponent, ModbusSlave, EventHandler {

	private final Logger log = LoggerFactory.getLogger(BatteryInverterFoxessH3Impl.class);

	@Reference
	protected ConfigurationAdmin cm;

	@Reference
	protected ComponentManager componentManager;

	public BatteryInverterFoxessH3Impl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				SymmetricBatteryInverter.ChannelId.values(), //
				BatteryInverterFoxessH3.ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsNamedException {
		if (super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm,
				"Modbus", config.modbus_id())) {
			return;
		}
		// Initial GridMode will be set by updateGridMode() after first Modbus read
		this._setGridMode(GridMode.UNDEFINED);
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() {
		return new ModbusProtocol(this, //
				// PV String measurements
				new FC4ReadInputRegistersTask(31000, Priority.HIGH, //
						m(BatteryInverterFoxessH3.ChannelId.PV1_VOLTAGE, new SignedWordElement(31000),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.PV1_CURRENT, new SignedWordElement(31001),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.PV1_POWER, new SignedWordElement(31002)), //
						m(BatteryInverterFoxessH3.ChannelId.PV2_VOLTAGE, new SignedWordElement(31003),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.PV2_CURRENT, new SignedWordElement(31004),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.PV2_POWER, new SignedWordElement(31005))), //

				// Grid measurements
				new FC4ReadInputRegistersTask(31006, Priority.HIGH, //
						m(BatteryInverterFoxessH3.ChannelId.GRID_VOLTAGE_R, new SignedWordElement(31006),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.GRID_VOLTAGE_S, new SignedWordElement(31007),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.GRID_VOLTAGE_T, new SignedWordElement(31008),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.GRID_CURRENT_R, new SignedWordElement(31009),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.GRID_CURRENT_S, new SignedWordElement(31010),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.GRID_CURRENT_T, new SignedWordElement(31011),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.GRID_POWER_R, new SignedWordElement(31012)), //
						m(BatteryInverterFoxessH3.ChannelId.GRID_POWER_S, new SignedWordElement(31013)), //
						m(BatteryInverterFoxessH3.ChannelId.GRID_POWER_T, new SignedWordElement(31014)), //
						m(BatteryInverterFoxessH3.ChannelId.GRID_FREQUENCY, new SignedWordElement(31015),
								SCALE_FACTOR_MINUS_2)), //

				// Temperature measurements
				new FC4ReadInputRegistersTask(31032, Priority.HIGH, //
						m(BatteryInverterFoxessH3.ChannelId.INVERTER_TEMPERATURE, new SignedWordElement(31032),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.INNER_TEMPERATURE, new SignedWordElement(31033),
								SCALE_FACTOR_MINUS_1)), //

				// Battery measurements
				new FC4ReadInputRegistersTask(31034, Priority.HIGH, //
						m(BatteryInverterFoxessH3.ChannelId.BATTERY_VOLTAGE, new SignedWordElement(31034),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.BATTERY_CURRENT, new SignedWordElement(31035),
								SCALE_FACTOR_MINUS_1), //
						m(SymmetricBatteryInverter.ChannelId.ACTIVE_POWER, new SignedWordElement(31036)), // Standard channel mapping
						m(BatteryInverterFoxessH3.ChannelId.BATTERY_TEMPERATURE, new SignedWordElement(31037),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.BATTERY_SOC, new SignedWordElement(31038))), //

				// Inverter status
				new FC4ReadInputRegistersTask(31041, Priority.HIGH, //
						m(BatteryInverterFoxessH3.ChannelId.INVERTER_STATE, new UnsignedWordElement(31041))), //

				// Energy counters - first block
				new FC4ReadInputRegistersTask(32001, Priority.LOW, //
						m(BatteryInverterFoxessH3.ChannelId.PV_ENERGY_TOTAL, new SignedWordElement(32001),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.PV_ENERGY_DAILY, new SignedWordElement(32002),
								SCALE_FACTOR_MINUS_1)), //

				// Energy counters - battery
				new FC4ReadInputRegistersTask(32004, Priority.LOW, //
						m(BatteryInverterFoxessH3.ChannelId.BATTERY_CHARGE_TOTAL, new SignedWordElement(32004),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.BATTERY_CHARGE_DAILY, new SignedWordElement(32005),
								SCALE_FACTOR_MINUS_1)), //

				new FC4ReadInputRegistersTask(32007, Priority.LOW, //
						m(BatteryInverterFoxessH3.ChannelId.BATTERY_DISCHARGE_TOTAL, new SignedWordElement(32007),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.BATTERY_DISCHARGE_DAILY, new SignedWordElement(32008),
								SCALE_FACTOR_MINUS_1)), //

				// Energy counters - smartmeter
				new FC4ReadInputRegistersTask(32013, Priority.LOW, //
						m(BatteryInverterFoxessH3.ChannelId.SMARTMETER_IMPORT_TOTAL, new SignedWordElement(32013),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.SMARTMETER_IMPORT_DAILY, new SignedWordElement(32014),
								SCALE_FACTOR_MINUS_1)), //

				new FC4ReadInputRegistersTask(32016, Priority.LOW, //
						m(BatteryInverterFoxessH3.ChannelId.SMARTMETER_EXPORT_TOTAL, new SignedWordElement(32016),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.SMARTMETER_EXPORT_DAILY, new SignedWordElement(32017),
								SCALE_FACTOR_MINUS_1)), //

				// Energy counters - load
				new FC4ReadInputRegistersTask(32022, Priority.LOW, //
						m(BatteryInverterFoxessH3.ChannelId.LOAD_ENERGY_TOTAL, new SignedWordElement(32022),
								SCALE_FACTOR_MINUS_1), //
						m(BatteryInverterFoxessH3.ChannelId.LOAD_ENERGY_DAILY, new SignedWordElement(32023),
								SCALE_FACTOR_MINUS_1)) //
		);
	}

	@Override
	public String debugLog() {
		return "ActivePower:" + this.getActivePower().asString() //
				+ "|GridMode:" + this.getGridModeChannel().value().asOptionString();
	}

	@Override
	public ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
		return new ModbusSlaveTable(//
				OpenemsComponent.getModbusSlaveNatureTable(accessMode), //
				SymmetricBatteryInverter.getModbusSlaveNatureTable(accessMode) //
		);
	}

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE:
			this.updateGridMode();
			break;
		}
	}

	/**
	 * Updates the GridMode channel based on the FoxESS inverter state.
	 * This method is called after each Modbus cycle to automatically 
	 * determine the correct grid mode from the device status.
	 */
	private void updateGridMode() {
		// Get the inverter state value from register 31041
		IntegerReadChannel inverterStateChannel = this.channel(BatteryInverterFoxessH3.ChannelId.INVERTER_STATE);
		var inverterStateOpt = inverterStateChannel.value().asOptional();
		
		if (inverterStateOpt.isPresent()) {
			var inverterState = inverterStateOpt.get();
			GridMode gridMode;
			
			// Map FoxESS H3 inverter states to OpenEMS GridMode
			// Based on FoxESS H3 documentation and nathanmarlor/foxess_modbus project
			switch (inverterState) {
			case 0: // Waiting - inverter is waiting to start
				gridMode = GridMode.UNDEFINED;
				this.logInfo(this.log, "FoxESS H3 Waiting -> UNDEFINED");
				break;
			case 1: // Checking - self-check/start-up process
				gridMode = GridMode.UNDEFINED;
				this.logInfo(this.log, "FoxESS H3 Checking -> UNDEFINED");
				break;
			case 2: // Fault - recoverable fault condition (as seen in your log)
				gridMode = GridMode.UNDEFINED;
				this.logWarn(this.log, "FoxESS H3 Fault mode (state: " + inverterState + ") -> UNDEFINED");
				break;
			case 3: // On Grid - normal grid-tied operation
				gridMode = GridMode.ON_GRID;
				this.logDebug(this.log, "FoxESS H3 On Grid -> ON_GRID");
				break;
			case 4: // Off Grid / EPS - emergency power supply mode
				gridMode = GridMode.OFF_GRID;
				this.logInfo(this.log, "FoxESS H3 Off Grid/EPS mode -> OFF_GRID");
				break;
			case 5: // Unrecoverable Fault - critical error
				gridMode = GridMode.UNDEFINED;
				this.logWarn(this.log, "FoxESS H3 Unrecoverable Fault (state: " + inverterState + ") -> UNDEFINED");
				break;
			default:
				gridMode = GridMode.UNDEFINED;
				this.logWarn(this.log, "FoxESS H3 unknown inverter state: " + inverterState + " -> UNDEFINED");
				break;
			}
			
			this._setGridMode(gridMode);
		} else {
			// No inverter state available - communication issue or device not responding
			this._setGridMode(GridMode.UNDEFINED);
			this.logDebug(this.log, "FoxESS H3 inverter state not available -> UNDEFINED");
		}
	}
}

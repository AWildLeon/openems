package io.openems.edge.meter.foxess.h3;

import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.INVERT_IF_TRUE;
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
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.MeterType;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC4ReadInputRegistersTask;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.meter.api.ElectricityMeter;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Meter.FoxESS.H3", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class MeterFoxessH3Impl extends AbstractOpenemsModbusComponent
		implements MeterFoxessH3, ElectricityMeter, ModbusComponent, OpenemsComponent {

	@Reference
	private ConfigurationAdmin cm;

	@Override
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	private MeterType meterType = MeterType.GRID;
	private boolean invert;

	public MeterFoxessH3Impl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //
				MeterFoxessH3.ChannelId.values() //
		);

		// Automatically calculate sum values from L1/L2/L3
		ElectricityMeter.calculateSumCurrentFromPhases(this);
		ElectricityMeter.calculateAverageVoltageFromPhases(this);
	}

	@Activate
	private void activate(ComponentContext context, Config config) throws OpenemsException {
		this.meterType = config.type();
		this.invert = config.invert();
		if (super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm,
				"Modbus", config.modbus_id())) {
			return;
		}
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public MeterType getMeterType() {
		return this.meterType;
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() {
		var modbusProtocol = new ModbusProtocol(this,

				// Grid measurements - voltage and frequency
				new FC4ReadInputRegistersTask(31006, Priority.HIGH, //
						m(MeterFoxessH3.ChannelId.GRID_VOLTAGE_R, new SignedWordElement(31006),
								SCALE_FACTOR_MINUS_1), //
						m(ElectricityMeter.ChannelId.VOLTAGE_L1, new SignedWordElement(31006),
								SCALE_FACTOR_MINUS_1), //
						m(MeterFoxessH3.ChannelId.GRID_VOLTAGE_S, new SignedWordElement(31007),
								SCALE_FACTOR_MINUS_1), //
						m(ElectricityMeter.ChannelId.VOLTAGE_L2, new SignedWordElement(31007),
								SCALE_FACTOR_MINUS_1), //
						m(MeterFoxessH3.ChannelId.GRID_VOLTAGE_T, new SignedWordElement(31008),
								SCALE_FACTOR_MINUS_1), //
						m(ElectricityMeter.ChannelId.VOLTAGE_L3, new SignedWordElement(31008),
								SCALE_FACTOR_MINUS_1)), //

				// Grid measurements - current
				new FC4ReadInputRegistersTask(31009, Priority.HIGH, //
						m(MeterFoxessH3.ChannelId.GRID_CURRENT_R, new SignedWordElement(31009),
								SCALE_FACTOR_MINUS_1), //
						m(MeterFoxessH3.ChannelId.GRID_CURRENT_S, new SignedWordElement(31010),
								SCALE_FACTOR_MINUS_1), //
						m(MeterFoxessH3.ChannelId.GRID_CURRENT_T, new SignedWordElement(31011),
								SCALE_FACTOR_MINUS_1)), //

				// Grid measurements - power and frequency
				new FC4ReadInputRegistersTask(31012, Priority.HIGH, //
						m(MeterFoxessH3.ChannelId.GRID_POWER_R, new SignedWordElement(31012)), //
						m(MeterFoxessH3.ChannelId.GRID_POWER_S, new SignedWordElement(31013)), //
						m(MeterFoxessH3.ChannelId.GRID_POWER_T, new SignedWordElement(31014)), //
						m(MeterFoxessH3.ChannelId.GRID_FREQUENCY, new SignedWordElement(31015),
								SCALE_FACTOR_MINUS_2), //
						m(ElectricityMeter.ChannelId.FREQUENCY, new SignedWordElement(31015),
								SCALE_FACTOR_MINUS_2)), //

				// Smart meter energy counters - import
				new FC4ReadInputRegistersTask(32013, Priority.LOW, //
						m(MeterFoxessH3.ChannelId.SMARTMETER_IMPORT_TOTAL, new SignedWordElement(32013),
								SCALE_FACTOR_MINUS_1), //
						m(MeterFoxessH3.ChannelId.SMARTMETER_IMPORT_DAILY, new SignedWordElement(32014),
								SCALE_FACTOR_MINUS_1)), //

				// Smart meter energy counters - export
				new FC4ReadInputRegistersTask(32016, Priority.LOW, //
						m(MeterFoxessH3.ChannelId.SMARTMETER_EXPORT_TOTAL, new SignedWordElement(32016),
								SCALE_FACTOR_MINUS_1), //
						m(MeterFoxessH3.ChannelId.SMARTMETER_EXPORT_DAILY, new SignedWordElement(32017),
								SCALE_FACTOR_MINUS_1)), //

				// Load energy counters
				new FC4ReadInputRegistersTask(32022, Priority.LOW, //
						m(MeterFoxessH3.ChannelId.LOAD_ENERGY_TOTAL, new SignedWordElement(32022),
								SCALE_FACTOR_MINUS_1), //
						m(MeterFoxessH3.ChannelId.LOAD_ENERGY_DAILY, new SignedWordElement(32023),
								SCALE_FACTOR_MINUS_1)) //
		);

		// Add tasks for the standard ElectricityMeter channels
		modbusProtocol.addTask(new FC4ReadInputRegistersTask(31009, Priority.HIGH, //
				m(ElectricityMeter.ChannelId.CURRENT_L1, new SignedWordElement(31009),
						SCALE_FACTOR_MINUS_1), //
				m(ElectricityMeter.ChannelId.CURRENT_L2, new SignedWordElement(31010),
						SCALE_FACTOR_MINUS_1), //
				m(ElectricityMeter.ChannelId.CURRENT_L3, new SignedWordElement(31011),
						SCALE_FACTOR_MINUS_1)));

		modbusProtocol.addTask(new FC4ReadInputRegistersTask(31012, Priority.HIGH, //
				m(ElectricityMeter.ChannelId.ACTIVE_POWER_L1, new SignedWordElement(31012)), //
				m(ElectricityMeter.ChannelId.ACTIVE_POWER_L2, new SignedWordElement(31013)), //
				m(ElectricityMeter.ChannelId.ACTIVE_POWER_L3, new SignedWordElement(31014))));

		// Apply inversion if configured
		if (this.invert) {
			// Note: Inversion will be handled by the standard ElectricityMeter calculations
		}

		// Calculate total active power from sum of L1/L2/L3
		ElectricityMeter.calculateSumActivePowerFromPhases(this);

		return modbusProtocol;
	}

	@Override
	public String debugLog() {
		return "ActivePower:" + this.getActivePower().asString() //
				+ "|MeterType:" + this.getMeterType() //
				+ "|L1:" + this.getActivePowerL1().asString() //
				+ "|L2:" + this.getActivePowerL2().asString() //
				+ "|L3:" + this.getActivePowerL3().asString();
	}
}

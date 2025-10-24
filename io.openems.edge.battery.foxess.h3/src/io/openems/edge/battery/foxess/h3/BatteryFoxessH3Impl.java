package io.openems.edge.battery.foxess.h3;

import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.battery.api.Battery;
import io.openems.edge.batteryinverter.foxess.h3.BatteryInverterFoxessH3;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.startstop.StartStop;
import io.openems.edge.common.startstop.StartStoppable;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Battery.FoxESS.H3", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE //
})
public class BatteryFoxessH3Impl extends AbstractOpenemsComponent
		implements BatteryFoxessH3, Battery, OpenemsComponent, EventHandler, StartStoppable {

	private final Logger log = LoggerFactory.getLogger(BatteryFoxessH3Impl.class);

	@Reference
	private ComponentManager componentManager;

	private Config config;
	private final AtomicReference<StartStop> startStopTarget = new AtomicReference<>(StartStop.UNDEFINED);
	private BatteryInverterFoxessH3 batteryInverter;

	public BatteryFoxessH3Impl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Battery.ChannelId.values(), //
				StartStoppable.ChannelId.values(), //
				BatteryFoxessH3.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;

		// Set static battery configuration
		this._setCapacity(config.capacityWh());
		this._setChargeMaxCurrent(config.chargeMaxCurrent());
		this._setDischargeMaxCurrent(config.dischargeMaxCurrent());

		// Get reference to the battery inverter
		try {
			this.batteryInverter = this.componentManager.getComponent(config.batteryInverterId());
		} catch (OpenemsNamedException e) {
			this.logError(this.log, "Failed to get battery inverter component '" + config.batteryInverterId() + "': " + e.getMessage());
			this.batteryInverter = null;
		}

		// Set some basic defaults
		this._setSoc(50); // Default 50% SOC
		this._setSoh(95); // Default 95% SOH  
		this._setVoltage(48); // Default 48V
		this._setStartStop(StartStop.START);
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
			this.updateBatteryData();
			break;
		}
	}

	/**
	 * Updates battery data from the FoxESS H3 battery inverter.
	 */
	private void updateBatteryData() {
		if (this.batteryInverter == null) {
			// Battery inverter not available, set connection lost
			this.channel(BatteryFoxessH3.ChannelId.BATTERY_INVERTER_CONNECTION_LOST).setNextValue(true);
			return;
		}

		try {
			// Clear connection lost flag
			this.channel(BatteryFoxessH3.ChannelId.BATTERY_INVERTER_CONNECTION_LOST).setNextValue(false);

			// Read SOC from battery inverter
			var socOpt = this.batteryInverter.channel(BatteryInverterFoxessH3.ChannelId.BATTERY_SOC).value().asOptional();
			if (socOpt.isPresent()) {
				this._setSoc((Integer) socOpt.get());
			}

			// Read voltage from battery inverter (convert from V to mV)
			var voltageOpt = this.batteryInverter.channel(BatteryInverterFoxessH3.ChannelId.BATTERY_VOLTAGE).value().asOptional();
			if (voltageOpt.isPresent()) {
				Integer voltageV = (Integer) voltageOpt.get();
				this._setVoltage(voltageV * 1000); // Convert V to mV
			}

			// Read current from battery inverter (already in mA)
			var currentOpt = this.batteryInverter.channel(BatteryInverterFoxessH3.ChannelId.BATTERY_CURRENT).value().asOptional();
			if (currentOpt.isPresent()) {
				this._setCurrent((Integer) currentOpt.get());
			}

			// Read temperature from battery inverter
			var temperatureOpt = this.batteryInverter.channel(BatteryInverterFoxessH3.ChannelId.BATTERY_TEMPERATURE).value().asOptional();
			if (temperatureOpt.isPresent()) {
				Integer temperature = (Integer) temperatureOpt.get();
				this._setMinCellTemperature(temperature);
				this._setMaxCellTemperature(temperature);
			}

		} catch (Exception e) {
			// If there's an error reading from battery inverter, set connection lost
			this.channel(BatteryFoxessH3.ChannelId.BATTERY_INVERTER_CONNECTION_LOST).setNextValue(true);
			this.logError(this.log, "Failed to read data from battery inverter: " + e.getMessage());
		}
	}

	@Override
	public String debugLog() {
		return "SOC:" + this.getSoc().asString() //
				+ "|Voltage:" + this.getVoltage().asString() //
				+ "|Current:" + this.getCurrent().asString() //
				+ "|InverterConnected:" + (this.batteryInverter != null ? "Yes" : "No") //
				+ "|ConnectionLost:" + this.channel(BatteryFoxessH3.ChannelId.BATTERY_INVERTER_CONNECTION_LOST).value().asString();
	}

	@Override
	public void start() throws OpenemsNamedException {
		// Battery start logic if needed
	}

	@Override
	public void stop() throws OpenemsNamedException {
		// Battery stop logic if needed  
	}

	@Override
	public void setStartStop(StartStop value) throws OpenemsNamedException {
		this.startStopTarget.set(value);
		this._setStartStop(value);
	}
}

package io.openems.edge.battery.foxess.h3;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.battery.api.Battery;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.startstop.StartStop;
import io.openems.edge.common.startstop.StartStoppable;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Battery.FoxESS.H3.Simple", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class SimpleBatteryImpl extends AbstractOpenemsComponent implements Battery, OpenemsComponent, StartStoppable {

	private Config config;

	public SimpleBatteryImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Battery.ChannelId.values(), //
				StartStoppable.ChannelId.values() //
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

		// Set some basic defaults
		this._setSoc(50); // Default 50% SOC
		this._setSoh(95); // Default 95% SOH  
		this._setVoltage(48000); // Default 48V in mV
		this._setCurrent(0); // No current flow initially
		this._setStartStop(StartStop.START); // Start by default
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void setStartStop(StartStop value) throws OpenemsNamedException {
		this._setStartStop(value);
	}
}

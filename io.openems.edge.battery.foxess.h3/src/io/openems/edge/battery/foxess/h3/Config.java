package io.openems.edge.battery.foxess.h3;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Battery FoxESS H3", //
		description = "Implements the FoxESS H3 Battery that reads data from FoxESS H3 Battery Inverter.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "battery0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Battery Inverter-ID", description = "ID of the FoxESS H3 Battery Inverter.")
	String batteryInverterId() default "batteryInverter0";

	@AttributeDefinition(name = "Capacity", description = "Battery capacity in Wh.")
	int capacityWh() default 10000;

	@AttributeDefinition(name = "Min Cell Voltage", description = "Minimum allowed cell voltage in mV.")
	int minCellVoltage() default 2800;

	@AttributeDefinition(name = "Max Cell Voltage", description = "Maximum allowed cell voltage in mV.")
	int maxCellVoltage() default 3650;

	@AttributeDefinition(name = "Charge Max Current", description = "Maximum allowed charge current in A.")
	int chargeMaxCurrent() default 40;

	@AttributeDefinition(name = "Discharge Max Current", description = "Maximum allowed discharge current in A.")
	int dischargeMaxCurrent() default 40;

	String webconsole_configurationFactory_nameHint() default "Battery FoxESS H3 [{id}]";
}

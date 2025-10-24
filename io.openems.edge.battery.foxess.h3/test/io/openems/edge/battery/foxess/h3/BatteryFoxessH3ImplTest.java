package io.openems.edge.battery.foxess.h3;

import org.junit.Test;

import io.openems.edge.common.test.ComponentTest;
import io.openems.edge.common.test.DummyComponentManager;

public class BatteryFoxessH3ImplTest {

	private static final String BATTERY_ID = "battery0";
	private static final String BATTERY_INVERTER_ID = "batteryInverter0";

	@Test
	public void test() throws Exception {
		new ComponentTest(new BatteryFoxessH3Impl()) //
				.addReference("componentManager", new DummyComponentManager()) //
				.activate(MyConfig.create() //
						.setId(BATTERY_ID) //
						.setBatteryInverterId(BATTERY_INVERTER_ID) //
						.setCapacityWh(10000) //
						.setMinCellVoltage(2800) //
						.setMaxCellVoltage(3650) //
						.setChargeMaxCurrent(40) //
						.setDischargeMaxCurrent(40) //
						.build()) //
		;
	}
}

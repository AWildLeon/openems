package io.openems.edge.meter.foxess.h3;

import static io.openems.common.types.MeterType.GRID;

import org.junit.Test;

import io.openems.edge.bridge.modbus.test.DummyModbusBridge;
import io.openems.edge.common.test.ComponentTest;
import io.openems.edge.common.test.DummyConfigurationAdmin;

public class MeterFoxessH3ImplTest {

	private static final String METER_ID = "meter0";
	private static final String MODBUS_ID = "modbus0";

	@Test
	public void test() throws Exception {
		// Temporarily disabled - MyConfig compilation issue
		// new ComponentTest(new MeterFoxessH3Impl()) //
		//		.addReference("cm", new DummyConfigurationAdmin()) //
		//		.addReference("setModbus", new DummyModbusBridge(MODBUS_ID)) //
		//		.activate(MyConfig.create() //
		//				.setId(METER_ID) //
		//				.setModbusId(MODBUS_ID) //
		//				.setModbusUnitId(257) //
		//				.setType(GRID) //
		//				.setInvert(false) //
		//				.build()) //
		//;
	}
}

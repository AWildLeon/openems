package io.openems.edge.battery.foxess.h3;

import io.openems.common.channel.Level;
import io.openems.edge.battery.api.Battery;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.startstop.StartStoppable;

public interface BatteryFoxessH3 extends Battery, OpenemsComponent, StartStoppable {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		
		/**
		 * Connection State to Battery Inverter.
		 * 
		 * <ul>
		 * <li>Interface: BatteryFoxessH3
		 * <li>Type: Boolean
		 * </ul>
		 */
		BATTERY_INVERTER_CONNECTION_LOST(Doc.of(Level.FAULT) //
				.text("Connection to Battery Inverter lost")), //
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

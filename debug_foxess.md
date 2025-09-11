# FoxESS H3 Debug Information

## Current Status
- Communication: Working (no Modbus errors)
- Values: UNDEFINED (ActivePower, GridMode)
- Unit ID: 247 (correct)
- Function Code: FC4 Read Input Registers (correct)

## Possible Issues

### 1. Register Values
The device might be returning:
- Zero values (which get converted to UNDEFINED)
- Invalid data ranges
- Null/empty responses

### 2. Scale Factors
Current scale factors in use:
- BATTERY_POWER: No scale factor (raw value)
- BATTERY_VOLTAGE: SCALE_FACTOR_MINUS_1 (divide by 10)
- BATTERY_CURRENT: SCALE_FACTOR_MINUS_1 (divide by 10)

### 3. Device State
The FoxESS H3 might be:
- In standby mode
- Not connected to battery
- In a fault state
- Not configured properly

## Debug Steps

### Step 1: Check Raw Register Values
Use a Modbus testing tool to read register 31036 (BATTERY_POWER) directly:
```
Unit ID: 247
Function: 04 (Read Input Registers)
Start Address: 31036
Count: 1
```

### Step 2: Check Device State
Read register 31041 (INVERTER_STATE):
```
Unit ID: 247
Function: 04 (Read Input Registers)  
Start Address: 31041
Count: 1
```

Expected values:
- 0: Standby
- 1: Self-check
- 2: Fault
- 3: Permanent fault
- 4: Normal operation
- 5: EPO mode

### Step 3: Verify Register Range
Read a range of registers to see what's actually available:
```
Unit ID: 247
Function: 04 (Read Input Registers)
Start Address: 31034
Count: 10
```

This should return:
- 31034: BATTERY_VOLTAGE
- 31035: BATTERY_CURRENT  
- 31036: BATTERY_POWER (this is what we need!)
- 31037: BATTERY_TEMPERATURE
- 31038: BATTERY_SOC
- 31039-31040: Unknown
- 31041: INVERTER_STATE

### Step 4: Check Alternative Registers
If 31036 doesn't work, try:
- Grid power: 31012-31014 (GRID_POWER_R/S/T)
- Total grid power calculation
- PV power: 31002, 31005 (PV1_POWER, PV2_POWER)

## Expected Solution

Once we identify which register contains valid power data:

1. **Update the mapping** in BatteryInverterFoxessH3Impl.java
2. **Adjust scale factors** if needed
3. **Set proper GridMode** based on INVERTER_STATE

## Testing Commands

To test the fix:

```bash
# Build the module
cd /home/leon/openems
./gradlew :io.openems.edge.batteryinverter.foxess.h3:build

# Deploy to Edge device
# Restart OpenEMS Edge service
# Check log output for the new values
```

## Current Modbus Protocol Fix

The ActivePower mapping has been added:
```java
m(SymmetricBatteryInverter.ChannelId.ACTIVE_POWER, new SignedWordElement(31036)), // Map to standard channel
```

This should make the ActivePower value appear correctly once the register returns valid data.

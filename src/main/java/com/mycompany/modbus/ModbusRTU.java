package com.mycompany.modbus;

public class ModbusRTU {
	
	public static byte[] GenMsg_SetDo(byte dev_id, int address, Boolean value) {
		byte[] ba = new byte[8];
		ba[0] = dev_id;
		ba[1] = 0x05;
		ba[2] = (byte) ((address >> 8) & 0xFF);
		ba[3] = (byte) (address & 0xFF);
		ba[4] = value ? (byte) 0xFF : 0x00;
		ba[5] = 0x00;
		int crc = ModRTU_CRC(ba, 6);
		ba[6] = (byte) (crc & 0xFF);
		ba[7] = (byte) (crc >> 8 & 0xFF);
		
		return ba;
	}
	
	// Compute the MODBUS RTU CRC
	public static int ModRTU_CRC(byte[] buf, int len)
	{
	  int crc = 0xFFFF;

	  for (int pos = 0; pos < len; pos++) {
	    crc ^= (int)buf[pos] & 0xFF;   // XOR byte into least sig. byte of crc

	    for (int i = 8; i != 0; i--) {    // Loop over each bit
	      if ((crc & 0x0001) != 0) {      // If the LSB is set
	        crc >>= 1;                    // Shift right and XOR 0xA001
	        crc ^= 0xA001;
	      }
	      else                            // Else LSB is not set
	        crc >>= 1;                    // Just shift right
	    }
	  }
	// Note, this number has low and high bytes swapped, so use it accordingly (or swap bytes)
	return crc;  
	}
}

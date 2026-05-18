library ieee;
use ieee.std_logic_1164.all;

entity Test_LCD is
port(
		LCD_DATA: out std_logic_vector(7 downto 4);
		LCD_RS: out std_logic;
		LCD_E: out std_logic
);

end Test_LCD;

architecture Structural of Test_LCD is

component UsbPort is
PORT(
		inputPort:  IN  STD_LOGIC_VECTOR(7 DOWNTO 0);
		outputPort :  OUT  STD_LOGIC_VECTOR(7 DOWNTO 0)
);

end component;

signal UsbPort_input : std_logic_vector(7 downto 0) :="00000000";
signal UsbPort_output : std_logic_vector(7 downto 0);

begin

LCD_DATA(4) <= UsbPort_output(0);
LCD_DATA(5) <= UsbPort_output(1);
LCD_DATA(6) <= UsbPort_output(2);
LCD_DATA(7) <= UsbPort_output(3);
LCD_RS <= UsbPort_output(4);
LCD_E <= UsbPort_output(5);
 
 
 Unit_usb_port: UsbPort port map(

	inputPort => UsbPort_input,
	outputPort => UsbPort_output
	
);

end Structural;

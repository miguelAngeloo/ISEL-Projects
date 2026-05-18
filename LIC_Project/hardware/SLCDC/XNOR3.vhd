LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY XNOR3 IS
	PORT (
		X : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
		match : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
		Q : OUT STD_LOGIC
	);
END XNOR3;

ARCHITECTURE logicFunction OF XNOR3 IS
	SIGNAL equalResult : STD_LOGIC_VECTOR(3 DOWNTO 0);
BEGIN
	equalResult <= X XNOR match;
	Q <= equalResult(0) AND equalResult(1) AND equalResult(2) AND equalResult(3);
END logicFunction;
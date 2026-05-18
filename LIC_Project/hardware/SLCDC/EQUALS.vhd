LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY EQUALS IS
	GENERIC (size : NATURAL := 4);
	PORT (
		X : IN STD_LOGIC_VECTOR((size - 1) DOWNTO 0);
		match : IN STD_LOGIC_VECTOR((size - 1) DOWNTO 0);
		Q : OUT STD_LOGIC
	);
END EQUALS;

ARCHITECTURE logicFunction OF EQUALS IS
	SIGNAL equalResult : STD_LOGIC_VECTOR((size - 1) DOWNTO 0);
	signal output : STD_LOGIC_VECTOR((size - 1) DOWNTO 0);
BEGIN
	equalResult <= X XNOR match;

	-- Generate the bitwise AND operation on the equalResult signal using a loop
	generate_bitwise_and : FOR i IN 0 TO size - 1 GENERATE
		first_and : IF i = 0 GENERATE
			output(i) <= equalResult(i); -- First element is directly assigned
		END GENERATE first_and;
		other_and : IF i > 0 GENERATE
			output(i) <= output(i - 1) AND equalResult(i); -- Connect to previous AND result
		END GENERATE other_and;
	END GENERATE generate_bitwise_and;
	
	Q <= output(size - 1); -- Assign the final AND result to Q
END logicFunction;
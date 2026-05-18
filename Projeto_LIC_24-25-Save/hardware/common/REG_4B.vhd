LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
ENTITY reg IS
    PORT (
        CLK : IN STD_LOGIC;
        RESET : IN STD_LOGIC;
        D : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
        EN : IN STD_LOGIC;
        Q : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
    );
END reg;
ARCHITECTURE logicFunction OF reg IS
    COMPONENT FFD
        PORT (
            CLK : IN STD_LOGIC;
            RESET : IN STD_LOGIC;
            SET : IN STD_LOGIC;
            D : IN STD_LOGIC;
            EN : IN STD_LOGIC;
            Q : OUT STD_LOGIC);
    END COMPONENT;
BEGIN
    U0 : FFD PORT MAP(CLK => clk, reset => reset, set => '0', D => D(0), EN => EN, Q => Q(0));
    U1 : FFD PORT MAP(CLK => clk, reset => reset, set => '0', D => D(1), EN => EN, Q => Q(1));
    U2 : FFD PORT MAP(CLK => clk, reset => reset, set => '0', D => D(2), EN => EN, Q => Q(2));
    U3 : FFD PORT MAP(CLK => clk, reset => reset, set => '0', D => D(3), EN => EN, Q => Q(3));
END LogicFunction;
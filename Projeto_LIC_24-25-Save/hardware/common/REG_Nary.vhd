LIBRARY IEEE;
USE ieee.std_logic_1164.ALL;

ENTITY REG_Nary IS
    GENERIC (size : NATURAL := 4);
    PORT (
        CLK : IN STD_LOGIC;
        RESET : IN STD_LOGIC;
        D : IN STD_LOGIC_VECTOR((size - 1) DOWNTO 0);
        EN : IN STD_LOGIC;
        Q : OUT STD_LOGIC_VECTOR((size - 1) DOWNTO 0)
    );
END REG_Nary;

ARCHITECTURE Structural OF REG_Nary IS

    COMPONENT FFD IS
        PORT (
            CLK : IN STD_LOGIC;
            RESET : IN STD_LOGIC;
            SET : IN STD_LOGIC;
            D : IN STD_LOGIC;
            EN : IN STD_LOGIC;
            Q : OUT STD_LOGIC
        );
    END COMPONENT;

BEGIN
    -- Generate the register using a loop
    circuit_gen : FOR ii IN 0 TO size - 1 GENERATE
        FFD_inst : FFD
        PORT MAP(
            CLK => CLK,
            RESET => RESET,
            SET => '0',
            D => D(ii),
            EN => EN,
            Q => Q(ii)
        );
    END GENERATE circuit_gen;
END ARCHITECTURE;
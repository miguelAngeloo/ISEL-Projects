LIBRARY IEEE;
USE IEEE.STD_LOGIC_1164.ALL;

ENTITY Counter IS
    PORT (
        Reset : IN STD_LOGIC;
        Clock : IN STD_LOGIC;
        Enabled : IN STD_LOGIC;
        S : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
    );
END ENTITY;

ARCHITECTURE ARCH OF Counter IS

    COMPONENT reg
        PORT (
            CLK : IN STD_LOGIC;
            RESET : IN STD_LOGIC;
            D : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
            EN : IN STD_LOGIC;
            Q : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
        );
    END COMPONENT;

    COMPONENT ADDER
        PORT (
            A : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
            B : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
            C0 : IN STD_LOGIC;
            S : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
            C4 : OUT STD_LOGIC
        );
    END COMPONENT;

    SIGNAL DIN : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL DOUT : STD_LOGIC_VECTOR(3 DOWNTO 0);

BEGIN
    reg_4 : reg PORT MAP(
        CLK => Clock,
        RESET => Reset,
        D => DIN,
        EN => Enabled,
        Q => DOUT
    );

    adder_4 : ADDER PORT MAP(
        A => DOUT,
        B => "0001",
        C0 => '0',
        S => DIN,
        C4 => OPEN
    );

    S <= DOUT;

END ARCHITECTURE;
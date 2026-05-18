LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY KEY_SCAN IS
    PORT (
        clk : IN STD_LOGIC;
        reset : IN STD_LOGIC;
        keyScan : IN STD_LOGIC;
        lines : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
        keyPress : OUT STD_LOGIC;
        keyData : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
        columns : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
    );
END KEY_SCAN;

ARCHITECTURE arch_kscan OF KEY_SCAN IS

    COMPONENT MUX41
        PORT (
            D : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
            S : IN STD_LOGIC_VECTOR(1 DOWNTO 0);
            M : OUT STD_LOGIC
        );
    END COMPONENT;

    COMPONENT COUNTER
        PORT (
            Reset : IN STD_LOGIC;
            Clock : IN STD_LOGIC;
            Enabled : IN STD_LOGIC;
            S : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
        );
    END COMPONENT;

    COMPONENT DECODER
        PORT (
            S0 : IN STD_LOGIC;
            S1 : IN STD_LOGIC;
            EX0 : OUT STD_LOGIC;
            EX1 : OUT STD_LOGIC;
            EX2 : OUT STD_LOGIC;
            EX3 : OUT STD_LOGIC
        );
    END COMPONENT;

    SIGNAL countData : STD_LOGIC_VECTOR(3 DOWNTO 0);
	 SIGNAL colDecoder : STD_LOGIC_VECTOR(3 DOWNTO 0);
	 SIGNAL Kpress : STD_LOGIC;

	 BEGIN
	 
    counter_A : COUNTER PORT MAP(
        Reset => reset,
        Clock => clk,
        Enabled => keyScan,
        S => countData
    );

    decoder_A : DECODER PORT MAP(
        S0 => countData(2),
        S1 => countData(3),
        EX0 => colDecoder(0),
        EX1 => colDecoder(1),
        EX2 => colDecoder(2),
        EX3 => colDecoder(3)
    );

    mux_A : MUX41 PORT MAP(
        D => lines,
        S => countData(1 DOWNTO 0),
        M => Kpress
    );

    keyData <= countData;
	 keyPress <= not Kpress;
	 columns <= not colDecoder;
END arch_kscan;
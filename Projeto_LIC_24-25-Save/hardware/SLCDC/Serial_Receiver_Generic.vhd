LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY Serial_Receiver_Generic IS
    GENERIC (shRegSize : NATURAL := 5); -- Generic parameter for shift register size
    PORT (
        dataMatch : IN STD_LOGIC_VECTOR(3 DOWNTO 0); -- Input for data match comparison
        parityMatch : IN STD_LOGIC_VECTOR(3 DOWNTO 0); -- Input for parity match comparison
        reset : IN STD_LOGIC; -- Reset signal
        clk : IN STD_LOGIC; -- Clock signal
        SS : IN STD_LOGIC; -- Enable signal for serial receiver
        SCLK : IN STD_LOGIC; -- Serial clock signal
        SDX : IN STD_LOGIC; -- Serial data input
        accept : IN STD_LOGIC; -- Accept signal
        DXval : OUT STD_LOGIC; -- Output indicating valid data
        Data : OUT STD_LOGIC_VECTOR((shRegSize - 1) DOWNTO 0)
    );
END Serial_Receiver_Generic;

ARCHITECTURE Structural OF Serial_Receiver_Generic IS

    COMPONENT Shift_Reg_Nary IS
        GENERIC (size : NATURAL := 5);
        PORT (
            Data : IN STD_LOGIC;
            clk : IN STD_LOGIC;
            E : IN STD_LOGIC;
            reset : IN STD_LOGIC;
            Sout : OUT STD_LOGIC;
            D : OUT STD_LOGIC_VECTOR((size - 1) DOWNTO 0)
        );
    END COMPONENT;
    
    COMPONENT XNOR3 IS
        PORT (
            X : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
            match : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
            Q : OUT STD_LOGIC
        );
    END COMPONENT;

    COMPONENT EQUALS IS
        GENERIC (size : NATURAL := 4);
        PORT (
            X : IN STD_LOGIC_VECTOR((size - 1) DOWNTO 0);
            match : IN STD_LOGIC_VECTOR((size - 1) DOWNTO 0);
            Q : OUT STD_LOGIC
        );
    END COMPONENT;

    COMPONENT Serial_Control IS
        PORT (
            reset : IN STD_LOGIC;
            clk : IN STD_LOGIC;
            enRx : IN STD_LOGIC;
            accept : IN STD_LOGIC;
            pFlag : IN STD_LOGIC;
            dFlag : IN STD_LOGIC;
            RXerror : IN STD_LOGIC;
            wr : OUT STD_LOGIC;
            init : OUT STD_LOGIC;
            DXval : OUT STD_LOGIC
        );
    END COMPONENT;

    COMPONENT COUNTER IS
        PORT (
            Reset : IN STD_LOGIC;
            Clock : IN STD_LOGIC;
            Enabled : IN STD_LOGIC;
            S : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
        );
    END COMPONENT;

    COMPONENT Parity_Check
        PORT (
            serialData : IN STD_LOGIC;
            serialClock : IN STD_LOGIC;
            init : IN STD_LOGIC;
            error : OUT STD_LOGIC
        );
    END COMPONENT;

    SIGNAL SCLK_signal : STD_LOGIC;
    SIGNAL SDX_signal : STD_LOGIC;
    SIGNAL WR_signal : STD_LOGIC;
    SIGNAL INIT_signal : STD_LOGIC;
    SIGNAL COUNTER_EXIT_signal : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL mD, mP : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL PFLAG_signal : STD_LOGIC;
    SIGNAL DFLAG_signal : STD_LOGIC;
    SIGNAL RXerror_signal : STD_LOGIC;
	SIGNAL Data_out : STD_LOGIC_VECTOR((shRegSize - 1) DOWNTO 0);

BEGIN
    SCLK_signal <= SCLK;
    SDX_signal <= SDX;
    mD <= dataMatch;
    mP <= parityMatch;
	Data <= Data_out;

    U_Shift_Register_Nary : Shift_Reg_Nary
    GENERIC MAP(size => shRegSize)
    PORT MAP(
        Data => SDX_signal,
        clk => SCLK_signal,
        E => WR_signal,
        reset => reset,
        Sout => OPEN,
        D => Data_out
    );

    --U_XNOR5 : XNOR3 PORT MAP(
    --    X => COUNTER_EXIT_signal,
    --    match => mD,
    --    Q => DFLAG_signal
    --);

    --U_XNOR6 : XNOR3 PORT MAP(
    --    X => COUNTER_EXIT_signal,
    --    match => mP,
    --    Q => PFLAG_signal
    --);

    U_EQUALS_8 : EQUALS PORT MAP(
        X => COUNTER_EXIT_signal,
        match => mD,
        Q => DFLAG_signal
    );

    U_EQUALS_9 : EQUALS PORT MAP(
        X => COUNTER_EXIT_signal,
        match => mP,
        Q => PFLAG_signal
    );

    U_Counter : COUNTER PORT MAP(
        Clock => SCLK,
        S => COUNTER_EXIT_signal,
        Reset => INIT_signal,
        Enabled => '1'
    );

    U_Serial_Control : Serial_Control PORT MAP(
        enRx => SS,
        accept => accept,
        DXval => DXval,
        wr => WR_signal,
        pFlag => PFLAG_signal,
        dFlag => DFLAG_signal,
        RXerror => RXerror_signal,
        reset => reset,
        clk => clk,
        init => INIT_signal
    );

    U_PARITY_CHECK : Parity_Check PORT MAP(
        error => RXerror_signal,
        init => INIT_signal,
        serialClock => SCLK,
        serialData => SDX
    );

END Structural;
LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY Memory_Address_Control IS
    PORT (
        clk : IN STD_LOGIC;
        reset : IN STD_LOGIC;
        putNGet : IN STD_LOGIC;
        incPut : IN STD_LOGIC;
        incGet : IN STD_LOGIC;
        address_out : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
        full : OUT STD_LOGIC;
        empty : OUT STD_LOGIC
    );
END Memory_Address_Control;

ARCHITECTURE arch OF Memory_Address_Control IS

    COMPONENT EQUALS IS
        GENERIC (size : NATURAL := 4);
        PORT (
            X : IN STD_LOGIC_VECTOR((size - 1) DOWNTO 0);
            match : IN STD_LOGIC_VECTOR((size - 1) DOWNTO 0);
            Q : OUT STD_LOGIC
        );
    END COMPONENT;

    COMPONENT Counter IS
        PORT (
            Reset : IN STD_LOGIC;
            Clock : IN STD_LOGIC;
            Enabled : IN STD_LOGIC;
            S : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
        );
    END COMPONENT;

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

    SIGNAL putAddress, getAddress : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL put : STD_LOGIC;
    SIGNAL get : STD_LOGIC;
    SIGNAL putNget_s : STD_LOGIC;
    SIGNAL incPutReg : STD_LOGIC;
    SIGNAL incGetReg : STD_LOGIC;
    SIGNAL isEqual : STD_LOGIC;
    SIGNAL putNgetEnable : STD_LOGIC;

BEGIN

    putNgetEnable <= incPut OR incGet;

    reg_putNget : FFD
    PORT MAP(
        CLK => clk,
        RESET => reset,
        SET => '0',
        D => putNGet,
        EN => putNgetEnable,
        Q => putNget_s
    );

    put <= putNget_s;
    get <= NOT putNget_S;

    putIndex_Counter : Counter
    PORT MAP(
        Reset => reset,
        Clock => clk,
        Enabled => incPut,
        S => putAddress
    );

    getIndex_Counter : Counter
    PORT MAP(
        Reset => reset,
        Clock => clk,
        Enabled => incGet,
        S => getAddress
    );

    equal_addresses : EQUALS
    GENERIC MAP(size => 4)
    PORT MAP(
        X => putAddress,
        match => getAddress,
        Q => isEqual
    );

    full <= isEqual AND putNget_s;
    empty <= isEqual AND NOT putNget_s;
    address_out <= putAddress WHEN putNget = '1' ELSE
        getAddress;

END ARCHITECTURE;
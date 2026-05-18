library ieee;
use ieee.std_logic_1164.all;

entity slcdc_tb is
end slcdc_tb;

architecture behavioral of slcdc_tb is

    -- componente a ser testado
    component slcdc
        port(
            reset         : in std_logic;
            mclk          : in std_logic;
            ss            : in std_logic;
            sclk          : in std_logic;
            sdx           : in std_logic;
            e             : out std_logic;
            dout          : out std_logic_vector(4 downto 0);
            serial_data   : out std_logic_vector(4 downto 0)
        );
    end component;

    -- constantes de tempo
    constant mclk_period      : time := 20 ns;
    constant mclk_half_period : time := mclk_period / 2;
    constant sclk_period      : time := 100 ns;

    -- sinais
    signal reset_tb       : std_logic := '0';
    signal mclk_tb        : std_logic := '0';
    signal ss_tb          : std_logic := '1'; -- ativo em baixo
    signal sclk_tb        : std_logic := '0';
    signal sdx_tb         : std_logic := '0';
    signal e_tb           : std_logic;
    signal dout_tb        : std_logic_vector(4 downto 0);
    signal serial_data_tb : std_logic_vector(4 downto 0);

begin

    uut: slcdc
        port map (
            reset        => reset_tb,
            mclk         => mclk_tb,
            ss           => ss_tb,
            sclk         => sclk_tb,
            sdx          => sdx_tb,
            e            => e_tb,
            dout         => dout_tb,
            serial_data  => serial_data_tb
        );

    -- Clock principal (mclk)
    mclk_gen : process
    begin
        while true loop
            mclk_tb <= '0';
            wait for mclk_half_period;
            mclk_tb <= '1';
            wait for mclk_half_period;
        end loop;
    end process;

    -- Clock serial (sclk)
    sclk_gen : process
    begin
        while true loop
            sclk_tb <= '0';
            wait for sclk_period / 2;
            sclk_tb <= '1';
            wait for sclk_period / 2;
        end loop;
    end process;

    -- Estimulos
    stimulus : process
    begin
        -- reset inicial
        reset_tb <= '1';
        wait for mclk_period * 5;
        reset_tb <= '0';
        wait for mclk_period * 5;

        -- ativar SS
        ss_tb <= '0';

        -- enviar 5 bits (ex: "10101")
        sdx_tb <= '1'; wait for sclk_period;
        sdx_tb <= '0'; wait for sclk_period;
        sdx_tb <= '1'; wait for sclk_period;
        sdx_tb <= '0'; wait for sclk_period;
        sdx_tb <= '1'; wait for sclk_period;

        -- desativar SS
        ss_tb <= '1';

        wait for mclk_period * 50; -- tempo para processar os dados

        wait;
    end process;

end behavioral;

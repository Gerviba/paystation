#include "ArduinoSpi3.h"

SPIClass * ArduinoSpi3::SPI3 = new SPIClass(HSPI);

ArduinoSpi3::ArduinoSpi3(int8_t csPin, int8_t dcPin, int8_t clkPin, int8_t mosiPin, uint32_t freq)
        : m_cs(csPin), m_dc(dcPin), m_clk(clkPin), m_mosi(mosiPin), m_frequency(freq) {
}

ArduinoSpi3::~ArduinoSpi3() {}

void ArduinoSpi3::begin() {
    if (m_cs >= 0)
        pinMode(m_cs, OUTPUT);
    if (m_dc >= 0)
        pinMode(m_dc, OUTPUT);
    SPI3->begin(m_clk, -1, m_mosi, m_cs);
}

void ArduinoSpi3::end() {
    SPI3->end();
}

void ArduinoSpi3::start() {
    SPI3->beginTransaction(SPISettings(m_frequency, MSBFIRST, SPI_MODE0));
    if (m_cs >= 0) {
        lcd_gpioWrite(m_cs, LCD_LOW);
    }
}

void ArduinoSpi3::stop() {
    if (m_cs >= 0) {
        lcd_gpioWrite(m_cs, LCD_HIGH);
    }
    SPI3->endTransaction();
}

void ArduinoSpi3::send(uint8_t data) {
    SPI3->transfer(data);
}

void ArduinoSpi3::sendBuffer(const uint8_t *buffer, uint16_t size) {
    while (size--) {
        SPI3->transfer(*buffer);
        buffer++;
    }
}
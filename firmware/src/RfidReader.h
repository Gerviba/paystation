#ifndef PAYMENTSYSTEM_RFIDREADER_H
#define PAYMENTSYSTEM_RFIDREADER_H

#include <stdint.h>
#include "DisplayManager.h"
#include <MFRC522.h>

class RfidReader {
    const uint8_t RST_PIN;
    const uint8_t CS_PIN;

    MFRC522_SPI * spiDevice;
    MFRC522 * mfrc522;
    DisplayManager * displayManager;

    void (*readHandler)(const char *);
    bool readingStarted;
    void readCard();
    void hash(char * input, char * output);
public:
    RfidReader(uint8_t rst, uint8_t cs, DisplayManager * displayManager)
            : RST_PIN(rst), CS_PIN(cs), displayManager(displayManager), readingStarted(false) {
        readHandler = defaultReadHandler;
    };

    void setupRfidReader();
    void handleReaderTasks();
    void startReading(void (*handler)(const char *));
    void stopReading();
    static void defaultReadHandler(const char * card);
    bool selfTest();
    void reset();
    void reInit();
    void setGain(int mode);
};


#endif //PAYMENTSYSTEM_RFIDREADER_H

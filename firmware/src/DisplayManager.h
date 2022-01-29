#ifndef PAYMENTSYSTEM_DISPLAYMANAGER_H
#define PAYMENTSYSTEM_DISPLAYMANAGER_H

#include <stdint.h>
#include <lcdgfx.h>
#include "ArduinoSpi3.h"

static const int canvasWidth = 96;
static const int canvasHeight = 64;
static uint8_t canvasData[canvasWidth * canvasHeight * 2];
static NanoCanvas16 canvas(canvasWidth, canvasHeight, canvasData);

class DisplayManager {
    const uint8_t DISPLAY_RST;
    const uint8_t DISPLAY_DC;
    const uint8_t HSPI_CLK;
    const uint8_t HSPI_MOSI;
    const uint8_t HSPI_MISO;
    const uint8_t DISPLAY_CS;
    DisplaySSD1331_96x64x16_CustomSPI<ArduinoSpi3> * display;

public:
    static const int INFO = 0;
    static const int FINE = 1;
    static const int ERROR = 2;

    DisplayManager(uint8_t rst, uint8_t dc, uint8_t clk, uint8_t mosi, uint8_t miso, uint8_t cs)
        : DISPLAY_RST(rst), DISPLAY_DC(dc), HSPI_CLK(clk), HSPI_MOSI(mosi), HSPI_MISO(miso), DISPLAY_CS(cs) {};
    void setupDisplay();
    void displayPaymentScreen(int amount);
    void displayAddEntry(int actual, int total);
    void displayAddNamedEntity(bool valid, char * item, int price);
    void displayAddNamedItem(char * item, int total);
    void displayLoadingScreen(int state);
    void displayCommandScreen(char * command, int length);
    void displayDoneStatus();
    void displayFailedStatus(String reason);
    void displayReadingScreen();
    void displayReadingScreen(const char* hashedTag);
    void displayReadCardAmountScreen();
    void displayCardAmountScreen(int amount, bool loan, bool allow);
    void displaySplashScreen(int mode);
    void displaySetupSplashScreen();
    void displayMenu();
    void displayEmpty();
    void displayRebootNowScreen();
    void updateSyncFlag();
    void updateErrorFlag(char * message);
    void updateConnFlag();
    void setOrientation(bool orientation);
    void setLedColor(int red, int green, int blue);
    void turnOffLeds();

    void showDebugInfo(int color);
    void showDebugInfo(int color, const char * title);
    void showDebugInfo(int color, const char * title, const char * line2);
    void showDebugInfo(int color, const char * title, const char * line2, const char * line3);
    void showDebugInfo(int color, const char * title, const char * line2, const char * line3, const char * line4);
    void showDebugInfo(int color, const char * title, const char * line2, const char * line3, const char * line4, const char * line5);
};

#endif //PAYMENTSYSTEM_DISPLAYMANAGER_H

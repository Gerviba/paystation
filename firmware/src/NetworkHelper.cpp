#include "NetworkHelper.h"
#include <WiFi.h>
#include <WiFiMulti.h>
#include <HTTPClient.h>
#include "Firmware.h"
#include "ScreenBase.h"

char * NetworkHelper::READING_URL = nullptr;
char * NetworkHelper::ACCOUNT_URL = nullptr;
char * NetworkHelper::PAYMENT_URL = nullptr;
char * NetworkHelper::VALIDATE_URL = nullptr;
char * NetworkHelper::QUERY_URL = nullptr;
char * NetworkHelper::STATUS_URL = nullptr;
char * NetworkHelper::TOKEN = nullptr;

const char* root_ca = \
"-----BEGIN CERTIFICATE-----\n"
"MIIDbzCCAlegAwIBAgIESMETdzANBgkqhkiG9w0BAQsFADBoMQswCQYDVQQGEwJI\n"
"VTEQMA4GA1UECBMHSHVuZ2FyeTERMA8GA1UEBxMIQnVkYXBlc3QxEDAOBgNVBAoT\n"
"B2RlZmF1bHQxEDAOBgNVBAsTB2RlZmF1bHQxEDAOBgNVBAMTB3BheXN0c24wHhcN\n"
"MjExMjEwMDAwMTMwWhcNMzExMjA4MDAwMTMwWjBoMQswCQYDVQQGEwJIVTEQMA4G\n"
"A1UECBMHSHVuZ2FyeTERMA8GA1UEBxMIQnVkYXBlc3QxEDAOBgNVBAoTB2RlZmF1\n"
"bHQxEDAOBgNVBAsTB2RlZmF1bHQxEDAOBgNVBAMTB3BheXN0c24wggEiMA0GCSqG\n"
"SIb3DQEBAQUAA4IBDwAwggEKAoIBAQCNUpjPTTLTVnGC4BFvzZONu7m2WQcy6hf9\n"
"+yc2m1f2VSEqo0VdwxiwXYxu2vrBptNN/AcqX4Y6BMR20CQ1JbTDwvk9Kg5uh5y1\n"
"npE8q29HtdV8Sm9elA8vLkyktjchLtj6DW52S2gHR+TmR/NSBYwUqacxOa2OY37/\n"
"PcIfk3IbDrj84aCj76sEb6Hvg6uqKoWnWBfutMRGcmd6tbyKM2oatEhDFMWQTd99\n"
"AozLErtKb3zeaZALvY3ZDsvkSGX7fGW3nNRojUIB3I8wW3SfrwapjYhlcINiU4lN\n"
"NufKrXuSP7YxIK3xtzNR+y618WEERLy3O60/GsEd6gUfwT74pmFrAgMBAAGjITAf\n"
"MB0GA1UdDgQWBBSEKyUXg4yRqI2fpwwJrNDshujNojANBgkqhkiG9w0BAQsFAAOC\n"
"AQEAIvcx3GMamfgFgdxagUkaDSsW9ANfo1PEdf8Xpw2Opmoixg7YELtS6GQEKRRN\n"
"6hmCd7JgLRS6NJOuqvaQF4hRdXXv9sAzFKGGwJHK+qLjF+6FyqlSz5eYAF+Ukxf7\n"
"Bd9+oxbzwnzf0ydNQJu5t8PR5gl8GccttiCdwvfmG+bScZvZEOgZzuRgqNqUKv6D\n"
"90qNP9pUWRhOleEe5b30VhT6TDO3a2Gx6nRq5fmkwCc1UfKW3eWFXemyWrPGFFX4\n"
"vGH/HzEEeIo6uathjFoLEpNT2CWi5rgq+YPE7V80LBSroKrNyXgphpcRk533flmI\n"
"b0uXwUgONDgb0LP6j2m6v+3qig==\n"
"-----END CERTIFICATE-----";

void NetworkHelper::setupUrls(char * baseUrl, char * gatewayName, char * token) {
    int baseLength = strlen(baseUrl);
    int gatewayLength = strlen(gatewayName);
    NetworkHelper::TOKEN = token;

    Serial.println("[API] API URLS:");

    READING_URL = new char[baseLength + 9 + gatewayLength + 1];
    strcpy(READING_URL, baseUrl);
    strcpy(READING_URL + baseLength, "/reading/");
    strcpy(READING_URL + baseLength + 9, gatewayName);
    Serial.println(READING_URL);

    ACCOUNT_URL = new char[baseLength + 9 + gatewayLength + 1];
    strcpy(ACCOUNT_URL, baseUrl);
    strcpy(ACCOUNT_URL + baseLength, "/balance/");
    strcpy(ACCOUNT_URL + baseLength + 9, gatewayName);
    Serial.println(ACCOUNT_URL);

    PAYMENT_URL = new char[baseLength + 5 + gatewayLength + 1];
    strcpy(PAYMENT_URL, baseUrl);
    strcpy(PAYMENT_URL + baseLength, "/pay/");
    strcpy(PAYMENT_URL + baseLength + 5, gatewayName);
    Serial.println(PAYMENT_URL);

    VALIDATE_URL = new char[baseLength + 10 + gatewayLength + 1];
    strcpy(VALIDATE_URL, baseUrl);
    strcpy(VALIDATE_URL + baseLength, "/validate/");
    strcpy(VALIDATE_URL + baseLength + 10, gatewayName);
    Serial.println(VALIDATE_URL);

    QUERY_URL = new char[baseLength + 7 + gatewayLength + 1];
    strcpy(QUERY_URL, baseUrl);
    strcpy(QUERY_URL + baseLength, "/query/");
    strcpy(QUERY_URL + baseLength + 7, gatewayName);
    Serial.println(QUERY_URL);

    STATUS_URL = new char[baseLength + 6 + 1];
    strcpy(STATUS_URL, baseUrl);
    strcpy(STATUS_URL + baseLength, "/status");
    Serial.println(STATUS_URL);
}

void NetworkHelper::setupWifi(char * ssid, char * password) {
    wifiMulti.addAP(ssid, password);
    while (wifiMulti.run() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.print("[SETUP] Connected to WiFi network with IP address: ");
    Serial.println(WiFi.localIP());
    Serial.println("[SETUP] Wifi: OK");
}

void NetworkHelper::sendReading(const char * cardHash) {
    Serial.print("[API] Sending card read: ");
    Serial.println(cardHash);

    for (int i = 0; i < 25; ++i) {
        if (i != 0)
            ScreenBase::displayManager->displayLoadingScreen(i % 4);

        if (wifiMulti.run() == WL_CONNECTED) {
            HTTPClient http;
            http.begin(READING_URL, root_ca);
            http.addHeader("Content-Type", "application/json");
            char message[64 + 64 + 28 + 1];
            sprintf(message, "{\"card\":\"%s\",\"gatewayCode\":\"%s\"}", cardHash, NetworkHelper::TOKEN);
            int httpResponseCode = http.PUT(message);

            Serial.print("[API] HTTP Response code: ");
            Serial.println(httpResponseCode);

            if (httpResponseCode == HTTP_CODE_OK) {
                Serial.print("[API] OK; payload: ");
                String payload = http.getString();
                Serial.println(payload);
            } else if (httpResponseCode < 0) {
                delay(400);
                http.end();
                continue;
            }

            http.end();
            return;
        } else {
            Serial.println("[API] WiFi Disconnected, retrying");
            delay(400);
        }
    }
}

AccountBalance NetworkHelper::getAccountBalance(const char * cardHash) {
    AccountBalance result;
    result.balance = 0;
    result.loan = true;
    result.allowed = false;

    Serial.print("[API] Sending account balance: ");
    Serial.println(cardHash);

    for (int i = 0; i < 25; ++i) {
        if (i != 0)
            ScreenBase::displayManager->displayLoadingScreen(i % 4);

        if (wifiMulti.run() == WL_CONNECTED) {
            HTTPClient http;
            http.begin(ACCOUNT_URL, root_ca);
            http.addHeader("Content-Type", "application/json");
            char message[64 + 64 + 28 + 1];
            sprintf(message, "{\"card\":\"%s\",\"gatewayCode\":\"%s\"}", cardHash, NetworkHelper::TOKEN);
            int httpResponseCode = http.PUT(message);

            Serial.print("[API] HTTP Response code: ");
            Serial.println(httpResponseCode);
            if (httpResponseCode == 200) {
                Serial.print("[API] OK; payload: ");
                String payload = http.getString();
                Serial.println(payload);

                char money[12] = { '\0' };
                char separator[] = { ';' };
                int moneyLength = strcspn(payload.c_str(), separator);
                memcpy(money, payload.c_str(), moneyLength);
                result.balance = atoi(money);
                result.loan = payload.charAt(moneyLength + 1) == '1';
                result.allowed = payload.charAt(moneyLength + 3) == '1';

            } else if (httpResponseCode < 0) {
                delay(400);
                http.end();
                continue;
            }

            http.end();
            return result;
        } else {
            Serial.println("[API] WiFi Disconnected, retrying");
            delay(400);
        }
    }

    return result;
}

void NetworkHelper::proceedPayment(const char * cardHash, uint32_t amount, char * comment) {
    Serial.println("[API] Sending payment: ");
    Serial.print(cardHash);
    Serial.print(" - with amount: ");
    Serial.println(amount);
    Serial.print("[API] Comment: ");
    comment += 1;
    Serial.println(comment);

    for (int i = 0; i < 25; ++i) {
        if (i != 0)
            ScreenBase::displayManager->displayLoadingScreen(i % 4);

        if (wifiMulti.run() == WL_CONNECTED) {
            HTTPClient http;
            http.begin(PAYMENT_URL, root_ca);
            http.addHeader("Content-Type", "application/json");
            char message[51 + 64 + 64 + 16 + strlen(comment) + 1];
            sprintf(message, "{\"card\":\"%s\",\"amount\":%d,\"gatewayCode\":\"%s\",\"details\":\"%s\"}",
                    cardHash, amount, NetworkHelper::TOKEN, comment);
            int httpResponseCode = http.POST(message);

            Serial.print("[API] HTTP Response code: ");
            Serial.println(httpResponseCode);

            if (httpResponseCode == HTTP_CODE_OK) {
                Serial.print("[API] OK; payload: ");
                String payload = http.getString();
                Serial.println(payload);

                if (payload.equals("ACCEPTED")) {
                    ScreenBase::displayManager->displayDoneStatus();
                    Serial.println("[API] Payment proceed!");
                } else if (payload.equals("INTERNAL_ERROR")) {
                    ScreenBase::displayManager->displayFailedStatus("SZERVER HIBA");
                } else if (payload.equals("NOT_ENOUGH_CASH")) {
                    ScreenBase::displayManager->displayFailedStatus("NINCS FEDEZET");
                } else if (payload.equals("VALIDATION_ERROR")) {
                    ScreenBase::displayManager->displayFailedStatus("NEM REGISZTRALT");
                } else if (payload.equals("CARD_REJECTED")) {
                    ScreenBase::displayManager->displayFailedStatus("LETILTVA");
                } else if (payload.equals("UNAUTHORIZED_TERMINAL")) {
                    ScreenBase::displayManager->displayFailedStatus("TERMINAL AUTH HIBA");
                } else {
                    ScreenBase::displayManager->displayFailedStatus("VARATLAN HIBA");
                }
            } else if (httpResponseCode < 0) {
                delay(400);
                http.end();
                continue;
            } else {
                ScreenBase::displayManager->displayFailedStatus("KOMM. HIBA");
            }

            http.end();
            return;
        } else {
            Serial.println("[API] WiFi Disconnected, retrying");
            delay(400);
        }
    }
}

void NetworkHelper::queryItem(const char * item, uint32_t * price) {
    Serial.print("[API] Querying item: ");
    Serial.println(item);

    for (int i = 0; i < 25; ++i) {
        if (wifiMulti.run() == WL_CONNECTED) {
            HTTPClient http;
            http.begin(QUERY_URL, root_ca);
            http.addHeader("Content-Type", "application/json");
            char message[29 + 10 + 64 + 1];
            sprintf(message, "{\"query\":\"%s\",\"gatewayCode\":\"%s\"}", item, NetworkHelper::TOKEN);
            int httpResponseCode = http.POST(message);

            Serial.print("[API] HTTP Response code: ");
            Serial.println(httpResponseCode);

            if (httpResponseCode == HTTP_CODE_OK) {
                Serial.print("[API] OK; payload: ");
                String payload = http.getString();
                Serial.println(payload);

                if (payload.startsWith("1;")) {
                    char name[32];
                    int secondSemicolon = payload.lastIndexOf(';');
                    memcpy(name, payload.c_str() + 2, secondSemicolon - 2);
                    name[secondSemicolon - 2] = '\0';
                    *price = payload.substring(secondSemicolon + 1).toInt();
                    ScreenBase::displayManager->displayAddNamedEntity(true, name, *price);

                } else {
                    *price = 0;
                    ScreenBase::displayManager->displayAddNamedEntity(false, "NINCS ILYEN", 0);
                }

            } else if (httpResponseCode < 0) {
                delay(400);
                http.end();
                continue;
            } else {
                ScreenBase::displayManager->displayAddNamedEntity(false, "KOMM. HIBA", 0);
            }

            http.end();
            return;
        } else {
            Serial.println("[API] WiFi Disconnected, retrying");
            delay(400);
        }
    }
}

void NetworkHelper::checkStatus() {
    Serial.print("[API] Checking status");

    for (int i = 0; i < 25; ++i) {
        if (wifiMulti.run() == WL_CONNECTED) {
            HTTPClient http;
            http.begin(STATUS_URL, root_ca);
            int httpResponseCode = http.GET();

            Serial.print("[API] HTTP Response code: ");
            Serial.println(httpResponseCode);
            if (httpResponseCode == 200) {
                Serial.print("[API] OK; payload: ");
                String payload = http.getString();
                Serial.println(payload);

                int index1 = payload.indexOf(';', 0);
                int index2 = payload.indexOf(';', index1 + 1);
                int index3 = payload.indexOf(';', index2 + 1);
                int index4 = payload.indexOf(';', index3 + 1);
                ScreenBase::displayManager->showDebugInfo(DisplayManager::FINE,
                                                          payload.substring(0, index1).c_str(),
                                                          payload.substring(index1 + 1, index2).c_str(),
                                                          payload.substring(index2 + 1, index3).c_str(),
                                                          payload.substring(index3 + 1, index4).c_str(),
                                                          payload.substring(index4 + 1).c_str());

            } else if (httpResponseCode < 0) {
                delay(400);
                http.end();
                continue;
            }

            http.end();
            return;
        } else {
            Serial.println("[API] WiFi Disconnected, retrying");
            delay(400);
            ScreenBase::displayManager->showDebugInfo(DisplayManager::ERROR, "WiFi disconnected", "retrying");
        }
    }
}

void NetworkHelper::validateConnection() {
    Serial.println("[API] Validating connection");

    for (int i = 0; i < 25; ++i) {
        if (wifiMulti.run() == WL_CONNECTED) {
            HTTPClient http;
            http.begin(VALIDATE_URL, root_ca);
            http.addHeader("Content-Type", "application/json");
            char message[64 + 18 + 1];
            sprintf(message, "{\"gatewayCode\":\"%s\"}", NetworkHelper::TOKEN);
            int httpResponseCode = http.PUT(message);

            Serial.print("[API] HTTP Response code: ");
            Serial.println(httpResponseCode);
            if (httpResponseCode == 200) {
                Serial.print("[API] OK; payload: ");
                String payload = http.getString();
                Serial.println(payload);
                ScreenBase::displayManager->showDebugInfo(DisplayManager::FINE, "[SERVER]", "Conn. status", payload.c_str());

            } else if (httpResponseCode < 0) {
                delay(400);
                http.end();
                continue;
            }

            http.end();
            return;
        } else {
            Serial.println("[API] WiFi Disconnected, retrying");
            delay(400);
            ScreenBase::displayManager->showDebugInfo(DisplayManager::ERROR, "[SERVER]", "Conn. status", "CAN'T REACH");
        }
    }
}
# Created by Gergely Szabo (github.com/gerviba)

# First time setup
# ----------------
# $ arduino-cli config init
# add https://dl.espressif.com/dl/package_esp32_index.json to the board_manager->additional_urls->[...]
# $ arduino-cli core update-index
# $ arduino-cli core install esp32:esp32
# $ sudo usermod -a -G tty <user>
# $ sudo usermod -a -G dialout <user>
# log out and in
# $ arduino-cli board list

all:
	@echo "cleanup compile build status serial"

cleanup:
	@echo "[#] Cleanup, status"
	rm output/* -rf
	./arduino-cli board list

compile: cleanup
	@echo "[#] Compile"
	./arduino-cli compile -b esp32:esp32:esp32 src/src.ino -v --build-path output/

build: compile
	@echo "[#] Uploading"
	./arduino-cli upload -b esp32:esp32:esp32 -v --port /dev/ttyUSB0 --input-dir output/
	@echo "[#] UPLOADED"

serial:
	minicom -b 115200

status:
	./arduino-cli board list

installib:
	./arduino-cli lib install "lcdgfx"
	./arduino-cli lib install "MFRC522-spi-i2c-uart-async"

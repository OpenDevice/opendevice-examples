#include <TVout.h>
#include "utility/pollserial.h"

TVout TV;
bool active = false;

const char* title = " MARCHA RE ACIONADA";
const char* legends = " L                R";


#define MAX_VALUE 200

// CMD IDs
#define SENSOR_A 1
#define SENSOR_B 2 // NOT USED
#define SENSOR_C 3 // NOT USEDi
#define SENSOR_D 4
#define REVERSE 5

byte VAL_SA = 0;
//byte VAL_SB = 0;
//byte VAL_SC = 0;
byte VAL_SD = 0;

pollserial pSerial;
byte cmd[2];
byte cmdIndex = 0;

void setup() {
  TV.begin(NTSC, 120, 96);

  pinMode(13, OUTPUT);

  TV.set_hbi_hook(pSerial.begin(9600));
  drawDisplayLayout();
  updateDisplay(MAX_VALUE+1, 0);
}

void drawDisplayLayout() {
  if(active){
    TV.select_font(font6x8);
    TV.println(title);
    TV.println();
    TV.println(legends);
  }
  TV.select_font(font8x8ext);
  // TV.bitmap(0,7,schematic);
  int marginTop = 33;
  int marginLeft = 40;
  TV.draw_line(0, TV.vres(), marginLeft, marginTop, WHITE); // diag-left
  TV.draw_line(TV.hres(), TV.vres(), TV.hres() - marginLeft, marginTop, WHITE); // diag-right
  TV.draw_line(marginLeft, marginTop, TV.hres() - marginLeft, marginTop, WHITE); // horiz-top
}

void updateDisplay(byte left, byte right) {

  if(!active) return;

  // Show top value
  if(left <= MAX_VALUE){
    if(left < right)
      TV.print(42, 20, (left / 100.0), 2); // show left
    else
      TV.print(42, 20, (right / 100.0), 2); // show right

    TV.print(76, 20, "m");
  }else{
    TV.clear_screen();
    drawDisplayLayout();
    TV.print(42, 20, "--- ");
    return;
  }

  byte barMarginTop = 32;
  byte barWidth = 10;
  float max_value = MAX_VALUE; // max value sensor
  byte max_heigh = TV.vres() - barMarginTop - 1;

  // Calculate bar size (inverse-black)
  float percentL = (left / max_value);
  float percentR = (right / max_value);
  byte sL_size = abs((int )((percentL * max_heigh) - max_heigh));
  byte sR_size = abs((int )((percentR * max_heigh) - max_heigh));

  // Left-Sensor
  TV.draw_rect(0, barMarginTop, barWidth, TV.vres() - barMarginTop - 1, WHITE,WHITE);
  TV.draw_rect(0, barMarginTop, barWidth, sL_size, WHITE, BLACK); // fill back top>down
  // Righ-Sensor
  TV.draw_rect(TV.hres() - barWidth - 1, barMarginTop, barWidth, TV.vres() - barMarginTop - 1, WHITE, WHITE);
  TV.draw_rect(TV.hres() - barWidth - 1, barMarginTop, barWidth, sR_size, WHITE, BLACK);  // fill back top>down
}

void loop() {

  while(pSerial.available()) {
    cmd[cmdIndex++] = pSerial.read();
    if(cmdIndex == 2){
      cmdIndex = 0;
      execute();
      pSerial.flush();
    }
  }

}

void execute() {
      switch(cmd[0]) {
        case SENSOR_A:
          VAL_SA = cmd[1];
          active = true;
          updateDisplay(VAL_SA * 10, VAL_SD * 10);
          break;
        case SENSOR_D:
          VAL_SD = cmd[1];
          active = true;
          updateDisplay(VAL_SA * 10, VAL_SD * 10);
          break;
        case REVERSE:
          active = cmd[1];
          digitalWrite(13, active);
          TV.clear_screen();
          drawDisplayLayout();
          break;
        default:
          break;
    };

    
}



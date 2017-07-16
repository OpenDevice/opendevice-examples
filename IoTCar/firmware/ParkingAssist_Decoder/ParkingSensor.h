
/*
 *  Author: Ricardo JL Rufino
 */

#ifndef ParkingSensor_H_
#define ParkingSensor_H_

#define PARKING_DATA_SIZE 16
#define PARKING_NO_RANGE_VAL 21

#include <Arduino.h>

class ParkingSensor
{
public:

  // const static uint8_t MAX_ANALOG_VALUE = 255;

  uint8_t pin;

  ParkingSensor(uint8_t ipin);

  bool isReady();
  void notifyStateChange(byte val, byte state_cnt);
  uint16_t getData();
  byte getDistance(int index);

private:
  int pwm_start;
  uint16_t parking_data; // raw data from parking module
  size_t parking_data_length;
  bool parking_started;
  bool parking_data_ready;
};

#endif


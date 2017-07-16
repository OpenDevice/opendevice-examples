
#include "ParkingSensor.h"

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Device
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

ParkingSensor::ParkingSensor(uint8_t ipin){
  ParkingSensor::pin = ipin;
  pinMode(ipin, INPUT);
  pwm_start = 0;
  parking_data_length = 0;
  parking_started = false;
  parking_data_ready = false;
}

bool ParkingSensor::isReady(){
  return parking_data_ready;
}

uint16_t ParkingSensor::getData(){
  parking_data_ready = false; // clear ready state
  return parking_data;
}

byte ParkingSensor::getDistance(int index){

  size_t moveBits = 0;
  byte max_scale = 20;
  if(index == 1) moveBits = 8;
  if(index == 2) moveBits = 8; // Same data = 1
  if(index == 3) moveBits = 0; // Same data = 4
  if(index == 4) moveBits = 0;
  byte v = parking_data >> moveBits; // select desired bytes from data

  // The data changa from 18 (0.30cm) to 4 (1.7m)
  // We need invert logic from 4 to 18 (considering max range 20), and apply a fix (-1)
  if(v != 0){
     v = max_scale - (v - 1); // invert scale
    // My sensor not read <= 0.20cm
    if(v <= 2) v = 0;
  }else{ // v=0 No obstacle (>2.0m)
    v = PARKING_NO_RANGE_VAL; // this will be ignored !
  }

  // v = v * 10; // fix scale (steps of 10cm)

  return v;
}

// byte ParkingSensor::getMinDistance(){
//
// }

void ParkingSensor::notifyStateChange(byte val, byte state_cnt) {

  // RISING, save time
  if(val){
    pwm_start = micros();
    return;
  }

  // FALING, check timing...
  int pwm_value = micros() - pwm_start;
  // Serial.println(pwm_value);


//  // RISING, save time
//  if(val){
//    pwm_start = 0;
//    return;
//  }
//
//  // FALING, check timing...
//  int pwm_value = state_cnt * 62;
//  // Serial.println(pwm_value);

  // Find start pulse (~1000 uS)
  if(parking_started == false && pwm_value > 500 && pwm_value < 1500){
     parking_started = true;
     // parking_data = 0;
     return;
  }

  // Only process after start pulse.
  if(parking_started){

    if(pwm_value < 180){ // ~150us = bit0

       parking_data <<= 1;

    }else if(pwm_value > 250 && pwm_value < 350){ // ~300us = bit1

      parking_data = (parking_data << 1) | 1;

    }else{
      // invalid data;
      parking_started = false;
      // parking_data = 0;
      parking_data_length = 0;
      return;
    }

    parking_data_length++;

    // End of transmission
    if(PARKING_DATA_SIZE == parking_data_length){
      parking_started = false;
      parking_data_ready = true;
      parking_data_length = 0;
      // parking_read = parking_data;
    }
  }

}


// Receive data from ParkingSensor and send to Display(another Arduino/TvOut)
#include <ESP8266WiFi.h>  // Enable ESP8266 Embedded
#include <ArduinoOTA.h>   // Remote Updates
#include "ParkingSensor.h"

#include <OpenDevice.h>

#define PIN_PARKING_SENSOR D3
#define PIN_REVERSE D2

#define ODEV_API_KEY "112233445566" //  SoftAP-Password
#define ODEV_MODULE_NAME "ODev-IoTCar" // SoftAP-SSID

ParkingSensor sensor(PIN_PARKING_SENSOR);

// CMD IDs
#define SENSOR_A 1
#define SENSOR_B 2 // NOT USED
#define SENSOR_C 3 // NOT USEDi
#define SENSOR_D 4
#define REVERSE 5

#define OUT Serial1 // TX=D4

uint8_t valueA = 0;
uint8_t valueD = 0;

void setup() {
  ODev.enableDebug();
  ODev.name(ODEV_MODULE_NAME);
  ODev.apiKey(ODEV_API_KEY);

  // Send parking data to App/Server
  ODev.addSensor("Reverse", PIN_REVERSE, Device::DIGITAL)->onChange(reverseCallback);
  ODev.addSensor("Reverse_SA", readSensorA);  
  ODev.addSensor("Reverse_SD", readSensorD);  
  
  OUT.begin(9600); // Used to send data do Arduino/TvOut
  Serial.begin(9600);
  pinMode(PIN_PARKING_SENSOR, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(PIN_PARKING_SENSOR), readParkingData, CHANGE);

  // Esp
  WiFi.mode(WIFI_AP);
  ODev.begin();
}

// Custon function to read sensor
unsigned long readSensorA(){
  return valueA;
}

// Custon function to read sensor
unsigned long readSensorD(){
  return valueD;
}

// Send state of reverse to Display
bool reverseCallback(uint8_t iid, unsigned long value){
  sendData(REVERSE,(byte) value);
  delay(50);
  sendData(REVERSE,(byte) value);
  return true;
}

void loop() {
  
  if(sensor.isReady()){
    
    uint16_t parking_data = sensor.getData();

    valueA = sensor.getDistance(1);
    valueD = sensor.getDistance(4);

    sendData(SENSOR_A,valueA);
    delay(50);
    sendData(SENSOR_D,valueD);
    
//    byte val = sensor.getDistance(1);
//    if(val != valueA){
//      valueA = sendData(SENSOR_A,val);
//    }
//
//    val = sensor.getDistance(4);
//    if(val != valueD){
//      valueD = sendData(SENSOR_D,val);
//    }
    
//    printBinary(parking_data);
//    Serial.print(">> 1=");
//    Serial.print(valueA, DEC);
//    Serial.print(", 4=");
//    Serial.print(valueD, DEC);
//    Serial.println("");
  }

  ODev.loop();

}


// attachInterrupt function
void readParkingData() {
   sensor.notifyStateChange(digitalRead(PIN_PARKING_SENSOR), 0);
}

byte sendData(byte cmdID,byte val) {
  OUT.write(cmdID); 
  OUT.write(val);
  return val;
}

void printBinary(unsigned int n) {
  // Reverse loop
  for (size_t i = 1 << 15; i > 0; i >>= 1)
      Serial.print(!!(n & i));
  Serial.print(" ");
}


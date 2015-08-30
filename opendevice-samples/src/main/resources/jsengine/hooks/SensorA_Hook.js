/**
 * @name ButtonHookDemo
 * @devices 5
 * @description TestCase
 * @type JavaScript
 */

// Sensor de presença ativado
// Quando alguem passar na sala a noite.


var led = findDevice(1);

if(device.isON()){

    print("Dev=ON -- led val: " + led.value + ", led: " +led.name);

    led.on();

}else{

    print("Dev=OFF -- led val: " + led.value + ", led: " +led.name);

    led.off();

}

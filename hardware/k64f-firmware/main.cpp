#include "mbed.h"
#include "MiCS6814_GasSensor.h"
#include <Adafruit_SGP30.h>

#define        COV_RATIO                       0.2            //ug/mmm / mv
#define        NO_DUST_VOLTAGE                 400            //mv
#define        SYS_VOLTAGE                     5000

RawSerial phone(USBTX, USBRX);
//RawSerial phone(D1, D0, 9600);

AnalogIn   dsensor(A3);
DigitalOut dsensorLed(D2);
Adafruit_SGP30 voc_sensor(D14,D15);
MiCS6814_GasSensor gas_sensor = MiCS6814_GasSensor(D14,D15);

volatile char   c = '\0'; // Initialized to the NULL character

void setup_bluetooth(){
    phone.printf("AT");
    ThisThread::sleep_for(100);
    phone.printf("AT+NAMEair_sense");
    ThisThread::sleep_for(100);
}

int read_dust(){
    /*
  covert voltage (mv)
  */
  int voltage = (SYS_VOLTAGE) * dsensor.read();
  
  /*
  voltage to density
  */
  if(voltage >= NO_DUST_VOLTAGE)
  {
    voltage -= NO_DUST_VOLTAGE;
    
    return voltage * COV_RATIO;
  }
  else
    return 0;
}

float smoothed_read_dust(){
    int max_readings = 30;
    int reading = 0;
    float readings = 0;
    while(reading < max_readings){
        dsensorLed.write(1);
        ThisThread::sleep_for(1); //wait for board to warm up
        readings += read_dust();
        reading ++;
        dsensorLed.write(0);
        ThisThread::sleep_for(100);
    }

    float avg = readings / max_readings;
    return avg;
    
}

void send_json_data(){
    phone.printf("Reading sensors");

    float dust = smoothed_read_dust();

    voc_sensor.IAQmeasure();
    phone.printf("{\"dust\":%4.2f,\"nh3\":%4.2f,\"co\":%4.2f,\"no2\":%4.2f,\"c3h8\":%4.2f,\"c4h10\":%4.2f,\"ch4\":%4.2f,\"h2\":%4.2f,\"c2h5Oh\":%4.2f,\"voc\":%u,\"eco2\":%u}\r\n",
    dust,
    gas_sensor.getGas(GAS_TYPE::NH3),
    gas_sensor.getGas(GAS_TYPE::CO),
    gas_sensor.getGas(GAS_TYPE::NO2),
    gas_sensor.getGas(GAS_TYPE::C3H8),
    gas_sensor.getGas(GAS_TYPE::C4H10),
    gas_sensor.getGas(GAS_TYPE::CH4),
    gas_sensor.getGas(GAS_TYPE::H2),
    gas_sensor.getGas(GAS_TYPE::C2H5OH),
    voc_sensor.TVOC,
    voc_sensor.eCO2);
}
 
void onCharReceived()
{
    c = phone.getc();
}
 
int main()
{
    voc_sensor.begin();
    voc_sensor.IAQinit();
    gas_sensor.initialize();

    //GAS_TYPE gases[] = [GAS_TYPE::NH3, GAS_TYPE::CO, GAS_TYPE::NO2, GAS_TYPE::C3H8, GAS_TYPE::C4H10, GAS_TYPE::CH4, GAS_TYPE::H2, GAS_TYPE::C2H5OH]
    //str gas_names[] = ["NH3"]

    phone.attach(&onCharReceived);
 
    while (true)
    {
        if (c == 'r')
        {
            c = '\0';  // To avoid execution of this block until a '1' is received again.
            send_json_data();
        }
 
        if (c == '?')
        {
            c = '\0';  // To avoid execution of this block until a '0' is received again.
            phone.printf("Hi, I'm the AirSense hardware device.\r\n");
        }
    }
}

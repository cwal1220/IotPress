#include <SPI.h>
#include <WiFiNINA.h> // NANO 33 IoT에서 Wi-Fi 기능을 사용하기 위한 라이브러리 입니다.
#include <PubSubClient.h>

const char   ssid[] = "lbbnee"; // Wi-Fi의 SSID(이름)를 입력합니다.
const char   password[] = "dltnqls9201"; // Wi-Fi의 페스워드를 입력합니다.

const char*   topic = "PRESS_221113/press";
const char*   mqttServer = "test.mosquitto.org";
const int     mqttPort = 1883;
const char*   mqttUser = "chan";
const char*   mqttPassword = "chan";

#define FSR_F A3 //앞쪽부분 로드셀
#define FSR_R A6 //뒷쪽부분 로드셀

WiFiClient espClient;
PubSubClient client(espClient);
long lastMsg = 0;
char msg[50];
int value = 0;

int status;

void setup_wifi()
{
    status = WL_IDLE_STATUS;
    Serial.println("try WiFi Connection");

    if (WiFi.status() == WL_NO_MODULE) 
    {
        Serial.println("WiFi module not present");
        while (true);
    }
    Serial.println("WiFi module check OK!");
    
    if (WiFi.status() == WL_NO_SHIELD) 
    {
        Serial.println("WiFi shield not present");
        while (true);
    }
    Serial.println("WiFi shield check OK!");
    
    while (status != WL_CONNECTED) // 연결될 때 까지 0.5초 마다 Wi-Fi 연결상태를 확인합니다.
    {
        status = WiFi.begin(ssid, password); // 앞서 설정한 ssid와 패스워드로 Wi-Fi에 연결합니다.
        Serial.print(".");
        delay(5000);
    }
    Serial.println("Connected to WiFi!!");
    printWifiStatus();
}

void callback(char* topic, byte* payload, unsigned int length) 
{
    String myString = String((char *)payload);
    Serial.println(myString);
}

void reconnect() 
{
    Serial.print("try MQTT Connect...");
    if (!client.connected())
    {
        String clientId = "ArduinoNANO33IoTClinet"; // 클라이언트 ID를 설정합니다.
        clientId += String(random(0xffff), HEX); // 같은 이름을 가진 클라이언트가 발생하는것을 방지하기 위해, 렌덤 문자를 클라이언트 ID에 붙입니다.
        if (client.connect(clientId.c_str())) // 앞서 설정한 클라이언트 ID로 연결합니다.
        { 
            // client.subscribe("inTopic"); // 지정한 토픽을 듣습니다.
            Serial.println("Connected!!");
            return;
        } 
        else 
        {
            Serial.print(".");
            delay(200);
        }
    }
}

void setup() 
{
    Serial.begin(9600);
    setup_wifi();
    client.setServer(mqttServer, 1883); // MQTT 서버에 연결합니다.
    // client.setCallback(callback);
}

void loop() 
{
    if(WiFi.status() != WL_CONNECTED)
    {
      
        setup_wifi();
    }
    else
    {
      if (!client.connected()) 
      {
          reconnect();
      }
      else
      {
          char buf[20];
          sprintf(buf, "%d %d", getPressFront(), getPressRear());
          client.publish(topic, buf, 0);
          Serial.println(buf);
          delay(300);
      }
      client.loop();
    }
    yield;
}

void printWifiStatus() 
{
    // print the SSID of the network you're attached to:
    Serial.print("SSID: ");
    Serial.println(WiFi.SSID());

    // print your board's IP address:
    IPAddress ip = WiFi.localIP();
    Serial.print("IP Address: ");
    Serial.println(ip);

    // print the received signal strength:
    long rssi = WiFi.RSSI();
    Serial.print("signal strength (RSSI):");
    Serial.print(rssi);
    Serial.println(" dBm");
}

int getPressFront()
{
  int valFSR_F = 0;
  valFSR_F = analogRead(FSR_F);
  return valFSR_F;
}

int getPressRear()
{
  int valFSR_R = 0;
  valFSR_R = analogRead(FSR_R);
  return valFSR_R;
}

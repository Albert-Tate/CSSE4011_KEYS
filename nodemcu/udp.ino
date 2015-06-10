#include <ESP8266WiFi.h>
#include <WiFiClient.h> 

/* Set these to your desired credentials. */
const char *ssid = "4011TEAMA";
const char *password = "4011TEAMA";

const char *keySend = "needinterwebz";
const char *keyPass = "robone3677";

byte pyIP[] = {118, 208, 13, 248};
IPAddress myIP;

const unsigned long oneMinute = 3 * 60 * 1000UL;

WiFiClient pyServer;

int sendLocation(String location_k) {
  
  WiFi.begin(keySend, keyPass);
  delay(5000);
  
  Serial.println("WiFi connected");  
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  
  myIP = WiFi.localIP();
  
  if (myIP[0] == 0 && myIP[1] == 0 && myIP[2] == 0 && myIP[3] == 0) {
    
    Serial.println("Could not connect to WiFi AP");
    
  } else {
  
    if (pyServer.connect(pyIP, 4011) != 1) {
      Serial.println("Connection failed");
      return 0;
    } else {
      Serial.println("Connection success, attempt to send location");
      pyServer.print("location_k " + location_k + '\n');
      return 1;
    }
    delay(6000);
    pyServer.stop();
  }
}

String macFormat(String macPart) {
 
  if (macPart.length() < 2) {
    return '0' + macPart;
  } else {
    return macPart;
  }
}

String scanAllNetworks() {
  
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();
  delay(1000);
  
  String bssid = "";
  String rssi = "";
  String chan = "";
  Serial.println("scan start");

  int n = WiFi.scanNetworks();
  Serial.println("scan done");
  if (n == 0)
    Serial.println("no networks found");
  else
  {
    Serial.print(n);
    Serial.println(" networks found");
    for (int i = 0; i < n; ++i)
    {
      uint8_t macPart = 0;
      
      bssid += "\"";
      bssid += macFormat(String(WiFi.BSSID(i)[0], HEX));
      bssid += ":";
      bssid += macFormat(String(WiFi.BSSID(i)[1], HEX));
      bssid += ":";
      bssid += macFormat(String(WiFi.BSSID(i)[2], HEX));
      bssid += ":";
      bssid += macFormat(String(WiFi.BSSID(i)[3], HEX));
      bssid += ":";
      bssid += macFormat(String(WiFi.BSSID(i)[4], HEX));
      bssid += ":";
      bssid += macFormat(String(WiFi.BSSID(i)[5], HEX));
      bssid += "\"";
      if (i == n-1){
        bssid += " ";
      } else {
        bssid += ",";
      }
      rssi += WiFi.RSSI(i);
      if (i != n-1){
        rssi += ",";
      } 
      
      chan += String(WiFi.CHANNEL(i));
      if (i != n-1){
        chan +=',';
      } else {
        chan += " ";
      }
    }   
    return bssid + chan + rssi;
  }
}

void setup() {
  delay(1000);
  Serial.begin(115200);
  Serial.println();
  Serial.print("Configuring access point...");
  WiFi.softAP(ssid);
  
  delay(1000);
  Serial.println("done");
  IPAddress myIP = WiFi.softAPIP();
  delay(1000);
  Serial.print("AP IP address: ");
  Serial.println(myIP);
}

void loop() {

  static unsigned long lastLocationTime = -oneMinute;
  
  unsigned long now = millis();

  if (now - lastLocationTime >= oneMinute) {
    lastLocationTime = now;
    
    String location_k = scanAllNetworks();
    
    Serial.println(location_k);
    
    // Connect and send the key location
    
    sendLocation(location_k);
    
    // back to normal operation
    delay(1000);
    Serial.println("Returning to AP mode");
    WiFi.softAP(ssid);
  
    delay(1000);
    Serial.println("done");
    IPAddress myIP = WiFi.softAPIP();
    delay(1000);
    Serial.print("AP IP address: ");
    Serial.println(myIP);
  }
  delay(1000);  	
}

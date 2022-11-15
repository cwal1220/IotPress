import paho.mqtt.client as mqtt

TOPIC = "PRESS_221113/press"

mqttc = mqtt.Client("client2")
mqttc.connect("test.mosquitto.org", 1883)
mqttc.publish(TOPIC, "abcdefg!!!!!!!!")
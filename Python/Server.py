import socket
import sys
import time
from Finder import get_location
from IPLocator import Locate_IP 

HOST = ''
PORT = 4011

MAC_EX = '"B4:75:0E:23:7C:46","14:CC:20:89:8E:A2'
CHAN_EX = "2,5"
RSSI_EX = "-87,-60"
LAST_KEY_CHECK_IN = [0, 0, 0, 0] #lat, lon, confidence, timestamp
f = open("KeyLOC", 'a')

def location_request(data, IP):
	data.rstrip()
	split = data.split(" ")
	
	MAC = split[1].split(",")				
	MAC_ADDR = []
	for i in range(0, len(MAC)):
		MAC_ADDR.append(str(MAC[i].strip('"')))
		#MAC_ADDR[i] = MAC[i]

	print MAC_ADDR
	CHANNEL = split[2].split(",")
	CHANNEL_LIST = []
	for i in range(0, len(CHANNEL)):
		CHANNEL_LIST.append(0)
		CHANNEL_LIST[i] = int(CHANNEL[i])
	
	RSSI = split[3].split(",")
	RSSI_LIST = []
	for i in range(0, len(RSSI)):
		RSSI_LIST.append(0)
		RSSI_LIST[i] = int(RSSI[i])

	rx = get_location(MAC_ADDR, CHANNEL_LIST, RSSI_LIST)
	if (rx[2] > 10000):
		rx = Location_IP(IP)
		rx = [rx[0], rx[1], 10000]
	print MAC_ADDR, CHANNEL_LIST, RSSI_LIST
	return rx


s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print "Socket Created"

RECV_BUFFER = 4096

try:
	s.bind((HOST, PORT))
except socket.error as msg:
	print "Bind Failed, Error Code : " + str(msg[0]) +  " Message " + msg[1]
	sys.exit()

print "Socket Bound"

s.listen(10)
print "Listening...."

while 1:
	conn, addr = s.accept()
	print "Connected with " + addr[0] + ":" + str(addr[1])
	while 1:
		try:
			data = conn.recv(RECV_BUFFER)
			if (data):
				print data		
				if ("location_k" in data):
					okay = 1
					try:
						rx = location_request(data, addr[0])
						LAST_KEY_CHECK_IN = [rx[0], rx[1], rx[2], time.time()]
						f.write(str(LAST_KEY_CHECK_IN) + "\n")
						print "Key at: ", LAST_KEY_CHECK_IN
					except Exception() as msg:
						conn.send("Bad Format " + msg)
						okay = 0
					if okay:
						try:
							conn.send(str(rx))
						except:
							print "User Terminated Session"				
				elif ("location_p" in data):
					okay = 1
					try:
						rx = location_request(data, addr[0])
						print "Phone at: ", rx[0], rx[1], rx[2]
					except Exception() as msg:
						conn.send("Bad Format " + msg)
						okay = 0
					if okay:
						try:
							conn.send(str(rx))
						except:
							print "User Terminated Session"
				elif ("request" in data):
					try:
						conn.send(str(LAST_KEY_CHECK_IN) + "\r\n")
					except:
						print "Unexpected Connection Reset"
				else:
					rx = "location "
					rx += MAC_EX + " "
					rx += CHAN_EX + " "
					rx += RSSI_EX + " "
					try:
						conn.send("Unkown format\nLOCATION REQUESTS OF FORM:\n")
						conn.send(rx)
					except:
						print "User Terminated Session"


				break
		except Exception() as msg:
			print "Unknown Error, Closing connection " + e
	conn.close()		
	print "Closed Connection"
s.close()
f.close()

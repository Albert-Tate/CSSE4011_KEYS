import socket
import sys
from Finder import get_location

HOST = ''
PORT = 4011

MAC_EX = '"B4:75:0E:23:7C:46","14:CC:20:89:8E:A2'
CHAN_EX = "2,5"
RSSI_EX = "-87,-60"

def location_request(data):
	data.rstrip()
	split = data.split(" ")
	
	MAC = split[1].split(",")				
	MAC_ADDR = []
	for i in range(0, len(MAC)):
		MAC_ADDR.append("")
		MAC_ADDR[i] = MAC[i]

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
				
				if ("location" in data):
					okay = 1
					try:
						rx = location_request(data)
					except Exception() as msg:
						conn.send("Bad Format " + msg)
						okay = 0
					if okay:
						conn.send(str(rx))
				else:
					rx = "location "
					rx += MAC_EX + " "
					rx += CHAN_EX + " "
					rx += RSSI_EX + " "
					conn.send("Unkown format\nLOCATION REQUESTS OF FORM:\n")
					conn.send(rx)
					print data

				break
		except Exception() as msg:
			print "Unknown Error, Closing connection " + e
	conn.close()		

s.close()

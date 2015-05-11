import socket
import sys
from Finder import get_location

HOST = ''
PORT = 4011



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
					rx = location_request(data)
					conn.send(str(rx))
				else:
					conn.send("Received comms\n")
					print data

				break
		except error as msg:
			print "Unknown Error, Closing connection"
	s.close()		

s.close()

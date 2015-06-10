import requests

def locate_IP(IP):
	rx = requests.get("http://freegeoip.net/csv/" + str(IP))
	#8 = lat, 9 = lon
	splitted = rx.content.split(',')
	print [float(splitted[8]), float(splitted[9])]

	return [float(splitted[8]), float(splitted[9])]

if __name__ == '__main__':
	locate_IP("130.102.82.116")

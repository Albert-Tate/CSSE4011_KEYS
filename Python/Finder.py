import requests


def get_location(MAC_ADDRS, CHANNELS, SIGNALS):

	if (len(MAC_ADDRS) != len(CHANNELS) or len(MAC_ADDRS) != len(SIGNALS)):
		return -1 #u dun goofed son
	
	json_dict = {"wifi" : []}

	for i, j in enumerate(MAC_ADDRS):
		json_dict["wifi"].append({})
		json_dict["wifi"][i] = {"key" : MAC_ADDRS[i], 
			"channel" : CHANNELS[i], "signal" : SIGNALS[i]}	

	r = requests.post('https://location.services.mozilla.com/v1/search?key=test', 
		json = json_dict)

	#Check if error first
	print [r.json()['lat'], r.json()['lon'], r.json()['accuracy'] ]
	return [r.json()['lat'], r.json()['lon'], r.json()['accuracy'] ] 



if (__name__ == "__main__"):
#	get_location(["00:25:45:37:16:FF", "00:25:45:37:16:FD", "00:25:45:37:16:F0"],[149, 149, 6], [-43, -45, -58])
	get_location(["1c:de:a7:e8:95:72", "00:24:c4:d2:8b:b0", "1c:de:a7:e8:95:71", "20:e5:2a:22:09:f5"], [11, 11, 11, 6], [-81, -73, -82, -91])

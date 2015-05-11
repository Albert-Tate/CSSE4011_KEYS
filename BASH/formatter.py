

f = open('MAC', 'r+')

s = f.readlines()

formatted = ""
netcat_arg = ""

for lines in s:
	formatted += '"'+ lines + '",'

formatted = formatted.translate(None, '\n')
formatted = formatted[0:len(formatted)-1]

f.seek(0)
f.write(formatted)
f.close()

f = open('CHAN', 'r+')

s = f.read()

RSSI_GEN_LEN = len(s.translate(None, ","))
RSSI_GEN = ""


for i in range(1, RSSI_GEN_LEN-1):
	RSSI_GEN += "-60,"

RSSI_GEN = RSSI_GEN[0:len(RSSI_GEN)-1]


netcat_arg = "location " + formatted + " " + RSSI_GEN + " " + s

print netcat_arg

f.close()

f = open('NC_CMD', 'w+')
f.write(netcat_arg)

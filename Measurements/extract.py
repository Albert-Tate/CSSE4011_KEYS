import csv
import matplotlib.pyplot as plt


#Read file
DATA = []
#csvfile = open("Scene1/Held/unsorted-10sticks.csv", "r")
csvfile = open("-77.csv", "r")
#####OFFSETS
#-77
#96 
#-104
#34

reader = csv.DictReader(csvfile)
for row in reader:
    #print row["RSSI"], row["Azimuth"]
    DATA.append([row["Azimuth"], row["RSSI"]])


#for line in DATA:
#    print line

#put into long format
L_DATA = [None]*361 #360 element array
for row in DATA:
    L_DATA[int(row[0])+180] = int(row[1]) #Very sparse array, what we have in java




#L_DATA is what we have from Java Class [0-360] [-180 - +180]
    #This strips nones
B_DATA = []
Xo = []
Yo = []
for index, element in enumerate(L_DATA):
    if element != None:
        B_DATA.append([index, element])
        Xo.append(index)
        Yo.append(element)

#Calculate SMA
SMA_WIDTH = 7
SMA_OLD = 0 #Try to keep SMA float?
SMA = []
for index, data in enumerate(B_DATA): #SMA_OLD + REST
    #SMA_OLD = SMA_OLD + (B_DATA[index - SMA_WIDTH])[1]/SMA_WIDTH - data[1]/SMA_WIDTH
    for i in xrange(0,SMA_WIDTH):
        SMA_OLD += B_DATA[index - i][1]
        
    SMA_OLD = SMA_OLD/float(SMA_WIDTH)
    #SMA_OLD = ((B_DATA[index - 1])[1] + data[1])/2.0
    SMA.append([data[0], SMA_OLD])
    SMA_OLD = 0
    #print SMA_OLD


###############################################################################
    #METHOD 1: Use dSMA/dRSSI
#Need to find dSMA/dRSSI
dSMA = []
Xd = []
Yd = []
for i,data in enumerate(SMA):
    if i == (len(SMA) - 1):
        break
    dSMA.append( [SMA[i][0], float(SMA[i+1][1] - SMA[i][1])/float(SMA[i+1][0] - SMA[i][0])] )
    Xd.append(dSMA[i][0])
    Yd.append(dSMA[i][1])
#print dSMA
        
in_zero_sequence = 0
begin = 0
end = 0
count = 0
SEQUENCES = []
for line in dSMA: #its been rounded for us too!
    if(line[1] == 0):
        count = count + 1
        if(in_zero_sequence == 1):
            end = line[0]
        else:
            begin = line[0]
            in_zero_sequence = 1
    else:
        in_zero_sequence = 0
        
        if(count > 0):
            end = line[0]
            SEQUENCES.append([begin, end, count])
            begin = 0
            end = 0
            count = 0
#if finish on a 0 streak
if count > 0:
    SEQUENCES.append([begin, end, count])

RETURN_VAL = []
TOTAL_VAL = 0

for data in SEQUENCES:
    if(data[1] != 0):
        RETURN_VAL.append([ data[0] + (data[1] - data[0])/2 - 180, data[2]])
    else:
        RETURN_VAL.append([ data[0] + (360 - data[0])/2 - 180, data[2]])
    TOTAL_VAL += data[2]
for i,data in enumerate(RETURN_VAL):
    RETURN_VAL[i][1] = float(RETURN_VAL[i][1])/TOTAL_VAL * 100
print RETURN_VAL
###############################################################################
#Method 2: Absolute Maximum of SMA data

cur_max = -200
cur_ang = 0
for i,data in enumerate(SMA):
    if data[1] > cur_max:
        cur_max = data[1]
        cur_ang = data[0]
Xf = cur_ang
Yf = cur_max
print Xf - 180, Yf


#build x,y for graph
X = []; Xp = []
Y = []; Yp = []
for i in SMA:
    X.append(i[0])
    Y.append(i[1])
    
for i in RETURN_VAL:
    Xp.append(i[0]+180)
    Yp.append(0)

#plt1
plt.subplot(2,1,1)
plt.plot(X,Y, 'b-')
plt.plot(Xo,Yo, 'ro')
plt.plot(Xf, Yf, 'bo')
plt.title('Original & SMA')
plt.ylabel('RSSI (dBm)')
plt.xlabel('Azimuth')


#plt2
plt.subplot(2,1,2)
plt.plot(Xp,Yp, 'ro')
plt.plot(Xd, Yd, 'r-')
plt.title('Heavily Filtered')
plt.ylabel('Discrete Derivative of RSSI w.r.t Azimuth')
plt.xlabel('Azimuth')

plt.show()


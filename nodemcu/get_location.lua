function listap(t)
    a = "location_k "
    b = ""
    c = ""
    d = ""
    for ssid,v in pairs(t) do
        authmode, rssi, bssid, channel = string.match(v, "(%d),(-?%d+),(%x%x:%x%x:%x%x:%x%x:%x%x:%x%x),(%d+)")
        if ssid ~= "needinterwebz" and ssid ~= "4011TEAMA" then
            a = a .. "\"" .. bssid .. "\"," 
            b = b .. channel .. "," 
            c = c .. rssi .. "," 
        end
        d = d .. ssid
    end
    location = string.sub(a, 1, -2) .. " " .. string.sub(b, 1, -2) .. " " .. string.sub(c, 1, -2)
    
    -- Have we found an ap we can connect to?
    if string.find(d, 'needinterwebz', 1, true) ~= nil then
        foundAP = 1
        print("AP present")
    else
        foundAP = 0
        connectedAP = 0
    end

    -- Have we found the phone?
    if string.find(d, '4011TEAMA', 1, true) ~= nil then 
        foundPhone = 1
        print("Phone present")
    else
        foundPhone = 0
        connectedPhone = 0
        connectSocket = 0
        gotGateway = 0
    end

end

wifi.sta.getap(listap)
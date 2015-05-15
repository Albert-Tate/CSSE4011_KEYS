function listap(t)
    a = "location_k "
    b = ""
    c = ""
    d = 0
    for ssid,v in pairs(t) do
        authmode, rssi, bssid, channel = string.match(v, "(%d),(-?%d+),(%x%x:%x%x:%x%x:%x%x:%x%x:%x%x),(%d+)")
        if ssid == "needinterwebz" then
            d = d + 1
        else
            a = a .. "\"" .. bssid .. "\"," 
            b = b .. channel .. "," 
            c = c .. rssi .. "," 
        end
    end
    location = string.sub(a, 1, -2) .. " " .. string.sub(b, 1, -2) .. " " .. string.sub(c, 1, -2)
end

wifi.sta.getap(listap)
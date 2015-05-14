location = ''

wifi.setmode(wifi.STATION)
tmr.delay(100000)
wifi.sta.config("Townhouse of Awesome", "robone3677")
tmr.delay(100000)
wifi.sta.connect()
tmr.delay(3000000)


socket = net.createConnection(net.TCP, 0)
tmr.delay(100000)
socket:on('receive', function(sck, c) print(c) end)
tmr.delay(100000)
socket:connect(4011, "118.208.13.248")
tmr.delay(3000000)

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
socket:send(location, nil)
function wifi_connect()
    if wifi.sta.getip == nil then
        wifi.setmode(wifi.STATION)
        wifi.sta.config("Townhouse of Awesome", "robone3677")
        wifi.sta.connect()
    end
end

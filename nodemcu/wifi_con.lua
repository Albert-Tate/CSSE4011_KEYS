function wifi_connect()
    if wifi.sta.getip == nil then
        wifi.setmode(wifi.STATION)
        wifi.sta.config("needinterwebz", "robone3677")
        wifi.sta.connect()
    end
end


function wifi_connect()
    wifi.sta.config("needinterwebz", "robone3677")
    wifi.sta.connect()
    connectedAP = 1
    connectedPhone = 0
end

wifi_connect()

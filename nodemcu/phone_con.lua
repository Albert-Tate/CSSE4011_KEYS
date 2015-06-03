function phone_connect()
    wifi.sta.config("4011TEAMA", "4011TEAMA")
    wifi.sta.connect()
    connectedAP = 0
    connectedPhone = 1
end

phone_connect()

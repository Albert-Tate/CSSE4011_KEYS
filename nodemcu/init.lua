--init.lua, something like this
location = ''
foundPhone = 0
foundAP = 0
connectedPhone = 0
connectedAP = 0
scan = 1
wifi.setmode(wifi.STATION)
wifi.sta.autoconnect(0)
socket = net.createConnection(net.TCP, 0)
connectSocket = 0
gotGateway = 0

tmr.alarm(0,11000,1, function()

    if scan == 1 then
        print("Scanning surrounding APs")
        s,err = pcall(function() dofile("get_location.lua") end)
        scan = 0
    else

        if connectedPhone == 1 then
           print("connected")
           if connectSocket == 0 then
                if gotGateway ~= 0 then
                    print("trying to open a port")
                    socket:connect(4011, gotGateway)
                    socket:on("receive", function(sck, c) print(c) end )
                    connectSocket = 1
                else
                    q,w,gotGateway = wifi.sta.getip()
                end
           end
        -- if we have found the phone but havent connected to it
        elseif foundPhone == 1 then
            print("connecting to phone")
            s,err = pcall(function() dofile("phone_con.lua") end)

        -- if found ap and not connected, connect.
        elseif foundAP == 1 and connectedAP == 0 then
            print("Connecting to wifi ap")
            s,err = pcall(function() dofile("wifi_con.lua") end)
        -- else if we are already connected to the wifi
        elseif connectedAP == 1 then
            print("Sending location")
            s,err = pcall(function() dofile("send_location.lua") end)
        end
        scan = 1
    end

    if not s then print(err) end
end)



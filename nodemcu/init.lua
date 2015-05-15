--init.lua, something like this
location = ''
tmr.alarm(0,5000,1,function()
    local s,err
    if wifi.sta.getip() == nil then
        print("Attempting to connect to wifi")
        s,err = pcall(function() dofile("wifi_con.lua") end)
    else
        --print("Getting location data")
        s,err = pcall(function() dofile("get_location.lua") end)
    end
    if not s then print(err) end
end)

socket = net.createConnection(net.TCP, 0)
socket:on('receive', function(sck, c) print(c) end)
socket:connect(4011, "118.208.13.248")
socket:send(location, nil)
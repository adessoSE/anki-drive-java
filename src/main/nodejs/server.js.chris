var net = require('net');
var noble = require('noble');
var util = require('util');

var server = net.createServer(function(client) {
  client.vehicles = [];

  noble.on('statechange', function(state) {
	  if (state == 'poweredOn') {
		  console.log("Noble on");
	  } else {
		  noble.log("Noble not on");
	  }
  });
  client.on("error", (err) => {
    console.log("connection error"); // client disconnected?
    client.vehicles.forEach((vehicle) => vehicle.disconnect());
  });
  client.on("data", function(data) {
    data.toString().split("\r\n").forEach(function(line) {
	  var command = line.toString().trim().split(";");
	  switch(command[0])
	  {
	    case "SCAN":
	      console.log("Beginning scan");
	      //if (noble.state === 'poweredOn') {
	    	console.log("Is powered on");
	        var discover = function(device) {
	          client.write(util.format("SCAN;%s;%s;%s\n",
	              device.id,
	              device.advertisement.manufacturerData.toString('hex'),
	              new Buffer(device.advertisement.localName).toString('hex')));
	        };
	
	        noble.on('discover', discover);
	        noble.startScanning(['be15beef6186407e83810bd89c4d8df4']);
	
	        setTimeout(function() {
	           noble.stopScanning();
	           noble.removeListener('discover', discover)
	           client.write("SCAN;COMPLETED\n");
	        }, 2000);
	      //}
	      //else {
	    	//console.log("Noble not powered on");
	        //client.write("SCAN;ERROR\n");
	      //}
	      break;
	      
	    case "CONNECT":
	      console.log("connect begin");
	      if (command.length != 2) {
	        client.write("CONNECT;ERROR-BAD-COMMAND\n");
	        break;
	      }
	
	      var vehicle = noble._peripherals[command[1]];
	      if (vehicle === undefined) {
	        client.write("CONNECT;ERROR-CONNECTING-VEHICLE\n");
	        break;
	      }
	
	      var success = false;
	
	      vehicle.connect(function(error) {
	        vehicle.discoverSomeServicesAndCharacteristics(
	            ["be15beef6186407e83810bd89c4d8df4"],
	            ["be15bee06186407e83810bd89c4d8df4", "be15bee16186407e83810bd89c4d8df4"],
	            function(error, services, characteristics) {
	              vehicle.reader = characteristics.find(x => !x.properties.includes("write"));
	              vehicle.writer = characteristics.find(x => x.properties.includes("write"));
	
	              vehicle.reader.notify(true);
	              vehicle.reader.on('read', function(data, isNotification) {
	                client.write(util.format("%s;%s\n", vehicle.id, data.toString("hex")));
	                //console.log(util.format("%s;%s\n", vehicle.id, data.toString("hex")));
	              });
	              client.write("CONNECT;SUCCESS\n");
                      client.vehicles.push(vehicle);
	              console.log("connect success");
	              success = true;
	            }
	        );
	      });
	
	      setTimeout(() => {
	        if (!success) {
	      	  client.write("CONNECT;ERROR-TIMEOUT\n");
	          console.log("connect error");
	        }
	      }, 500);
	
	      break;
	      
	    case "DISCONNECT":
	      if (command.length != 2) {
	        client.write("DISCONNECT;ERROR\n");
	        break;
	      }
	
	      var vehicle = noble._peripherals[command[1]];
	      if (vehicle === undefined) {
	        client.write("DISCONNECT;ERROR\n");
	        break;
	      }
	
	      vehicle.disconnect();
	      client.write("DISCONNECT;SUCCESS\n");
	      break;
	      
	    case "EXIT":
	    	client.write("BYE\n");
	    	server.close(function () { console.log('Server closed!'); });
	    	client.destroy();
	    	break;
	    default:
	      if (command.length == 2 && noble._peripherals[command[0]] !== undefined) {
	        var vehicle = noble._peripherals[command[0]];
	        vehicle.writer.write(new Buffer(command[1], 'hex'));
	      }
	  }
	});
  });
});

server.listen(5000);

console.log("Server gestartet")

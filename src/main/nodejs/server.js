var net = require('net');
var noble = require('noble');
var util = require('util');

var server = net.createServer(function(client) {
  client.vehicles = [];

  client.on("error", (err) => {
    console.log("connection error (client disconnected?)"); // client disconnected?
    client.vehicles.forEach((vehicle) => vehicle.disconnect());
  });
  client.on("data", function(data) {
    data.toString().split("\r\n").forEach(function(line) {
	  var command = line.toString().trim().split(";");
	  if (command[0])
	    console.log(command)
	  switch(command[0])
	  {
          case "SCAN":
	      console.log(noble);
// Bastian Tenbergen: removed - node never seems to show this state.
// However, causes AnkiConnector to get NullPointerExceptions sometimes.
//	      if (noble.state === 'poweredOn') {
	        var discover = function(device) {
                                //Peter Muir: edited to more reliably connect to the cars. Hardcoded localName
				//and txPowerLevel. Should change to a dynamic setup if necessary.
				//(Context: some cars connect with undefined localName, crashing the server.)
				if(undefined === device.advertisement.localName){
					console.log("No localName. Defaulting");
					device.advertisement.txPowerLevel = 0;
					//The two cars have the same localName and have uuids starting with 'e'
					if(device.id.charAt(0) === 'e'){
						device.advertisement.localName = "\u0001`0\u0001    Drive\u0000";
					}else{
						device.advertisement.localName = "\u0010`0\u0001    Drive\u0000";
					};
				};
				//DEBUG message
				console.log(util.format("SCAN;%s;%s\n",
	              device.id,
	              device.advertisement.manufacturerData.toString('hex')));
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
//	      }
//	      else {
//	        client.write("SCAN;ERROR\n");
//	      }
	      break;
	      
	    case "CONNECT":
	      console.log("connect begin");
	      if (command.length != 2) {
	        client.write("CONNECT;ERROR\n");
	        break;
	      }
	
	      var vehicle = noble._peripherals[command[1]];
	      if (vehicle === undefined) {
	        client.write("CONNECT;ERROR\n");
	        break;
	      }
	
	      var success = false;
	
	      vehicle.connect(function(error) {
	        vehicle.discoverSomeServicesAndCharacteristics(
	            ["be15beef6186407e83810bd89c4d8df4"],
	            ["be15bee06186407e83810bd89c4d8df4", "be15bee16186407e83810bd89c4d8df4"],
	            function(error, services, characteristics) {
		      //  console.log("!!!!!!!!!!!!!!!!!");
                      //  console.log(characteristics);
		      //  console.log("!!!!!!!!!!!!!!!!!");
	              vehicle.reader = characteristics[1];//.find(x => !x.properties.includes("write"));
	              vehicle.writer = characteristics[0];//.find(x => x.properties.includes("write"));
	
	              vehicle.reader.notify(true);
	              vehicle.reader.on('data', function(data, isNotification) {
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
	      	  client.write("CONNECT;ERROR\n");
	          console.log("connect error (timeout)");
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

console.log("Server started")

import 'package:flutter/material.dart';
import 'package:locator/locator.dart';
import 'package:permission_handler/permission_handler.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> with SingleTickerProviderStateMixin {
  String latitude = "?";
  String longitude = "?";
  String time = "?";
  String currentLocation = "?";
  Animation<double> animation;
  AnimationController controller;
  @override
  void initState() {
    controller =
        AnimationController(vsync: this, duration: Duration(seconds: 2));
    animation = Tween<double>(begin: 0, end: 300).animate(controller)
      ..addListener(() {
        setState(() {});
      })
      ..addStatusListener((status) {
        if (status == AnimationStatus.completed) {
          controller.reverse();
        } else if (status == AnimationStatus.dismissed) {
          controller.forward();
        }
      });

    controller.forward();

    super.initState();
    Locator.getLocations((location) {
      setState(() {
        this.latitude = location.latitude.toString();
        this.longitude = location.longitude.toString();
        this.time = DateTime.fromMillisecondsSinceEpoch(location.time.toInt())
            .toString();
      });

      debugPrint("""\n
      Latitude:  $latitude
      Longitude: $longitude
      Time: $time
      """);
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Locator'),
        ),
        body: Center(
          child: ListView(
            children: <Widget>[
              locationData("Latitude: " + latitude),
              locationData("Longitude: " + longitude),
              locationData("CurrentLocation: " + currentLocation),
              RaisedButton(
                  onPressed: () async {
                    var permission = await checkPermissions();
                    if (permission) {
                      await Locator.start(
                        notificationTitle: "Service Rota",
                        notificationText: "Son Konum Zamanı :",
                        updateIntervalInSecond: 10,
                      );
                    }
                  },
                  child: Text("Start")),
              RaisedButton(
                  onPressed: () {
                    Locator.stop();
                  },
                  child: Text("Stop")),
              RaisedButton(
                  onPressed: () async {
                    var location = await Locator.getLastLocation();
                    if (location != null) {
                      setState(() {
                        currentLocation = location.latitude.toString() +
                            'x' +
                            location.longitude.toString();
                      });
                    }
                  },
                  child: Text("Get Current Location")),
              Container(
                color: Colors.blue,
                height: animation.value,
                width: animation.value,
              )
            ],
          ),
        ),
      ),
    );
  }

  Widget locationData(String data) {
    return Text(
      data,
      style: TextStyle(
        fontWeight: FontWeight.bold,
        fontSize: 18,
      ),
      textAlign: TextAlign.center,
    );
  }

  @override
  void dispose() {
    controller.dispose();
    Locator.stop();
    super.dispose();
  }

  static Future<bool> checkPermissions() async {
    PermissionStatus permission = await Permission.locationAlways.status;
    if (!permission.isGranted) {
      permission = await Permission.locationAlways.request();
    }
    return permission.isGranted;
  }
}

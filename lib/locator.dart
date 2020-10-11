import 'dart:async';
import 'dart:ui';
import 'package:flutter/services.dart';

class Locator {
  static const MethodChannel _channel = const MethodChannel('exm_locator');

  static Future<void> start({
    String notificationTitle,
    String notificationText,
    int updateIntervalInSecond,
  }) {
    notificationTitle ??= "";
    notificationText ??= "";
    updateIntervalInSecond ??= 10;
    var arguments = <String, dynamic>{
      "notificationTitle": notificationTitle,
      "notificationText": notificationText,
      "updateIntervalInSecond": updateIntervalInSecond
    };
    return _channel.invokeMethod('start_location_service', arguments);
  }

  static Future<void> stop() {
    return _channel.invokeMethod('stop_location_service');
  }

  /// Get the last location once.
  static Future<Location> getLastLocation() async {
    Map<dynamic, dynamic> map = await _channel.invokeMethod('last_location');

    return Location(
      latitude: map["latitude"],
      longitude: map["longitude"],
      altitude: map["altitude"],
      accuracy: map["accuracy"],
      bearing: map["bearing"],
      speed: map["speed"],
      time: map["time"],
    );
  }

  /// Register a function to recive location updates as long as the location
  /// service has started
  static void getLocations(Function(Location) location) {
    // add a handler on the channel to recive updates from the native classes
    _channel.setMethodCallHandler((MethodCall methodCall) async {
      if (methodCall.method == "location") {
        Map locationData = Map.from(methodCall.arguments);
        // Call the user passed function
        location(
          Location(
            latitude: locationData["latitude"],
            longitude: locationData["longitude"],
            altitude: locationData["altitude"],
            accuracy: locationData["accuracy"],
            bearing: locationData["bearing"],
            speed: locationData["speed"],
            time: locationData["time"],
          ),
        );
      }
    });
  }
}

class Location {
  Location({this.latitude, this.longitude, this.altitude, this.accuracy, this.bearing, this.speed, this.time});

  final double latitude;
  final double longitude;
  final double altitude;
  final double bearing;
  final double accuracy;
  final double speed;
  final double time;

  @override
  bool operator ==(Object o) {
    return o is Location &&
        o.latitude == latitude &&
        o.longitude == longitude &&
        o.altitude == altitude &&
        o.accuracy == accuracy &&
        o.bearing == bearing &&
        o.speed == speed &&
        o.time == time;
  }

  @override
  int get hashCode => hashValues(latitude, longitude, altitude, accuracy, bearing, speed, time);
}

class LocatorException {}

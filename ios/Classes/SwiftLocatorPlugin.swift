import Flutter
import UIKit
import CoreLocation

public class SwiftLocatorPlugin: NSObject, FlutterPlugin, CLLocationManagerDelegate {
    static var locationManager: CLLocationManager?
    static var channel: FlutterMethodChannel?
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let instance = SwiftLocatorPlugin()
        
        SwiftLocatorPlugin.channel = FlutterMethodChannel(name: "exm_locator", binaryMessenger: registrar.messenger())
        registrar.addMethodCallDelegate(instance, channel: SwiftLocatorPlugin.channel!)
        SwiftLocatorPlugin.channel?.setMethodCallHandler(instance.handle)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        SwiftLocatorPlugin.locationManager = CLLocationManager()
        SwiftLocatorPlugin.locationManager?.delegate = self
        SwiftLocatorPlugin.locationManager?.requestAlwaysAuthorization()
        SwiftLocatorPlugin.locationManager?.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
        SwiftLocatorPlugin.locationManager?.distanceFilter = 20
        SwiftLocatorPlugin.locationManager?.allowsBackgroundLocationUpdates = true
        if #available(iOS 11.0, *) {
            SwiftLocatorPlugin.locationManager?.showsBackgroundLocationIndicator = true;
        }
        SwiftLocatorPlugin.locationManager?.pausesLocationUpdatesAutomatically = false
        
        if (call.method == "start_location_service") {
            SwiftLocatorPlugin.locationManager?.startUpdatingLocation()
            result(true)
        } else if (call.method == "stop_location_service") {
            SwiftLocatorPlugin.locationManager?.stopUpdatingLocation()
            result(true)
        } else if (call.method == "last_location") {
            result(locationToMap(location:SwiftLocatorPlugin.locationManager?.location));
        } else {
            result(FlutterMethodNotImplemented);
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        if status != .authorizedAlways {
           manager.requestAlwaysAuthorization()
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let map=locationToMap(location:locations.last!);
        if(map != nil)
        {
            SwiftLocatorPlugin.channel?.invokeMethod("location", arguments: map)
        }
    }

    func locationToMap(location: CLLocation?) -> [String:Any]? {
        if(location==nil)
        {
            return nil;
        }
      let map = [
            "speed": location!.speed,
            "altitude": location!.altitude,
            "latitude": location!.coordinate.latitude,
            "longitude": location!.coordinate.longitude,
            "accuracy": location!.horizontalAccuracy,
            "bearing": location!.course,
            "time":location!.timestamp.timeIntervalSince1970 * 1000
       ] as [String : Any]
        return map;
    }
}

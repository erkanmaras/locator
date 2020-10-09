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

        SwiftLocatorPlugin.locationManager?.allowsBackgroundLocationUpdates = true
        if #available(iOS 11.0, *) {
            SwiftLocatorPlugin.locationManager?.showsBackgroundLocationIndicator = true;
        }
        SwiftLocatorPlugin.locationManager?.pausesLocationUpdatesAutomatically = false

        SwiftLocatorPlugin.channel?.invokeMethod("location", arguments: "method")

        if (call.method == "start_location_service") {
            SwiftLocatorPlugin.channel?.invokeMethod("location", arguments: "start_location_service")            
            SwiftLocatorPlugin.locationManager?.startUpdatingLocation() 
        } else if (call.method == "stop_location_service") {
            SwiftLocatorPlugin.channel?.invokeMethod("location", arguments: "stop_location_service")
            SwiftLocatorPlugin.locationManager?.stopUpdatingLocation()
        } else if (call.method == "last_location") {
            result.success(locationToMap(SwiftLocatorPlugin.locationManager.location));
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        if status == .authorizedAlways {
           
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        SwiftLocatorPlugin.channel?.invokeMethod("location", arguments: locationToMap(locations.last!))
    }


    func locationToMap(location: CLLocation) -> [String:Any] {
     let location = [
            "speed": locations.last!.speed,
            "altitude": locations.last!.altitude,
            "latitude": locations.last!.coordinate.latitude,
            "longitude": locations.last!.coordinate.longitude,
            "accuracy": locations.last!.horizontalAccuracy,
            "bearing": locations.last!.course,
            "time": locations.last!.timestamp.timeIntervalSince1970 * 1000
        ] as [String : Any]
        return location;
}
}

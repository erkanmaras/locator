#import "LocatorPlugin.h"
#if __has_include(<locator/locator-Swift.h>)
#import <locator/locator-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "locator-Swift.h"
#endif

@implementation LocatorPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftLocatorPlugin registerWithRegistrar:registrar];
}
@end

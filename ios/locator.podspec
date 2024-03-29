#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint locator.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'locator'
  s.version          = '0.0.1'
  s.summary          = 'Flutter location plugin'
  s.homepage         = 'https://github.com/erkanmaras/locator'
  s.description      = <<-DESC
  Flutter location plugin.
                         DESC
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'erkan maras' => 'erkanmaras@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '8.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'
end

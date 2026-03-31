{ pkgs, ... }: {
  channel = "stable-23.11";
  packages = [
    pkgs.jdk17
    pkgs.android-sdk
  ];
  env = {
    ANDROID_HOME = "${pkgs.android-sdk}/libexec/android-sdk";
    JAVA_HOME = pkgs.jdk17.home;
  };
  idx.previews = {
    enable = true;
    previews = {
      android = {
        command = ["./gradlew" "installDebug"];
        manager = "android";
      };
    };
  };
}
